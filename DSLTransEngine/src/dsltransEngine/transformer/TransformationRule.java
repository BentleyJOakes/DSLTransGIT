package dsltransEngine.transformer;

import dsltrans.impl.AttributeEqualityImpl;
import dsltrans.impl.AttributeInequalityImpl;
import dsltransEngine.metamodel.DSLTransAttribute;
import dsltransEngine.metamodel.MetaEntity;
import dsltransEngine.metamodel.MetaModelDatabase;
import dsltransEngine.model.*;
import dsltransEngine.transformer.exceptions.InvalidAttributeRelationException;
import dsltransEngine.transformer.exceptions.InvalidLayerRequirement;
import dsltransEngine.transformer.exceptions.MissingFeatureException;
import dsltransEngine.transformer.filter.*;

import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

public class TransformationRule {

	private final dsltrans.Rule _rule;
	private List<MatchFilter> _matchFilters;
	private Applyer _applyFilter;
	private boolean _processed;
	private TermProcessor _termprocessor;
	@SuppressWarnings("rawtypes")
	private List<List<Hashtable>> solutionSet;
	private TransformationLayer _layer=null;

	public TransformationRule(TransformationLayer tl, dsltrans.Rule rule, InstanceDatabaseManager instanceDatabaseManager) {
		_rule = rule;
		setLayer(tl);
		_matchFilters = new LinkedList<MatchFilter>();
		setTermProcessor(new TermProcessor(_matchFilters));
				
		for (Object obj : rule.getMatch()) {
			dsltrans.MatchModel mm = (dsltrans.MatchModel) obj;
			MatchFilter mf = new MatchFilter(this,rule, mm, instanceDatabaseManager);
			if (mm.getExplicitSource() != null) {
				TransformationSource tu = tl.getSource(mm.getExplicitSource());
				if (tu != null) {
					mf.set_explicitMatchMetaModel(tu.getMetaDatabase());
					mf.set_explicitMatchModel(tu.getOutputModelDatabase());
				}
			}
			
			addMatchFilter(mf);
		}
		
		
		
		setApplyFilter(new Applyer(this, instanceDatabaseManager));	
		setProcessed(false);
	}

	public void MarkTemporalRelations(InstanceDatabase instanceDatabase) {
		// move time forward...
		for(InstanceEntity ie : instanceDatabase.getInstanceEntities())
			ie.setFreshness(false);
	}
	
	public void setMatchFilter(List<MatchFilter> _matchFilter) {
		this._matchFilters = _matchFilter;
	}
	
	public void addMatchFilter(MatchFilter _matchFilter) {
		this._matchFilters.add(_matchFilter);
	}

	public List<MatchFilter> getMatchFilter() {
		return _matchFilters;
	}

	public void setApplyFilter(Applyer _applyFilter) {
		this._applyFilter = _applyFilter;
	}

	public Applyer getApplyFilter() {
		return _applyFilter;
	}
	
	public void clean() {
		for (MatchFilter mf : getMatchFilter()) {
			mf.clean();
		}
	}
	
	class Cartesian<X> {
		public List<List<X>> product(List<List<X>> sets) {
		    if (sets.size() < 2)
		        throw new IllegalArgumentException(
		                "Can't have a product of fewer than two sets (got " +
		                sets.size() + ")");

		    return _product(0, sets);
		}

		private List<List<X>>  _product(int index, List<List<X>> sets) {
			List<List<X>> ret = new LinkedList<List<X>>();
		    if (index == sets.size()) {
		        ret.add(new LinkedList<X>());
		    } else {
		        for (X obj : sets.get(index)) {
		            for (List<X> set : _product(index+1, sets)) {
		                set.add(0,obj);
		                ret.add(set);
		            }
		        }
		    }
		    return ret;
		}
	};
	
	
	@SuppressWarnings("rawtypes")
	public boolean performMatch(
			TransformationController control,
			InstanceDatabase matchModel, 
			InstanceDatabase applyModel,
			MetaModelDatabase matchMetaModel,
			MetaModelDatabase applyMetaModel) throws Throwable {
		
		System.out.println("Processing rule: " + this.toString());
		
		if(!isProcessed()) { // initial queries
			List<List<Hashtable>> results = new LinkedList<List<Hashtable>>();
			for (MatchFilter mf : getMatchFilter()) {
				if(mf.process(control, matchModel,applyModel,matchMetaModel,applyMetaModel,getTermProcessor()))
					results.add(mf.getBindingList());					
				else
					return false; //1 match failed, no need to continue
			}
			if(results.size() > 1) {
				solutionSet = new Cartesian<Hashtable>().product(results);
			} else {
				solutionSet = new LinkedList<List<Hashtable>>();
				for(Hashtable result: results.get(0)) {
					List<Hashtable> one = new LinkedList<Hashtable>();
					solutionSet.add(one);
					one.add(result);
				}
			}
			setProcessed(true);
		}
		
		if(solutionSet.size() == 0) return false; // no more solutions
		
		
		
		boolean validSolution;
		do {
			validSolution = true;
			List<Hashtable> solution = solutionSet.get(0);
			solutionSet.remove(0); // remove the head of the list
			
			for(int i = 0; i < getMatchFilter().size() && validSolution; i++) {
				MatchFilter mf = getMatchFilter().get(i);
				System.out.println("try rule again: " + this.toString());
				mf.updateFilters(solution.get(i),matchModel, applyModel);
				validSolution = mf.isValid();
			}
			
			// Aditional check for inter match filter relations
			if (validSolution) {
				/*
				 * If the solution is still valid, check if the existing 
				 * attribute relations are being obeyed.
				 */
				for (Object obj : getRule().getAttributeRelations()) {
					dsltrans.AbstractAttributeRelation aatr = (dsltrans.AbstractAttributeRelation) obj;

					if (!isObeyed(aatr)){
						validSolution = false;
						break;
					}
				}
			}
			
		} while (!validSolution && hasMoreSolutions());
		
		if (!validSolution) {
			assert !hasMoreSolutions();
			return false;
		}
		
		
		
		for(int i = 0; i < getMatchFilter().size(); i++) {
			MatchFilter mf = getMatchFilter().get(i);
			mergeApplyFilters(mf);
		}
		
		return true; //all matches passed
	}

	private boolean isObeyed(dsltrans.AbstractAttributeRelation aatr) throws InvalidAttributeRelationException, MissingFeatureException {
		/*
		 * his method assumes all the matchfilters have been filled with 
		 * the corresponding bindings.
		 */

		dsltrans.Attribute source = aatr.getSourceAttribute();
		dsltrans.Attribute target = aatr.getTargetAttribute();
		
		MatchAttributeFilter source_maf = null;
		ApplyAttributeFilter source_aaf = null;
		MatchAttributeFilter target_maf = null;
		ApplyAttributeFilter target_aaf = null;
		
	
		 // There can be all combinations of attribute filters 
		 // corresponding to the source and target attributes.
		
		
		source_maf = findMatchAttributeFilter(source);
		if (source_maf==null) {
			// The attribute relation doesn't start in the match model.
			// It must be in the apply model. Or else it's an error...
			source_aaf = findApplyAttributeFilter(source);
			if (source_aaf == null) {
				// probably the attribute relation connects attributes 
				// from different rules, or different layers
				throw new InvalidAttributeRelationException("Attribute relations have to be contained in a single rule.", aatr);
			}
		}
		assert source_maf!=null || source_aaf != null;
		
		target_maf = findMatchAttributeFilter(target);
		if (target_maf==null) {
			// The attribute relation doesn't start in the match model.
			// It must be in the apply model. Or else it's an error...
			target_aaf = findApplyAttributeFilter(target);
			if (target_aaf == null) {
				// probably the attribute relation connects attributes 
				// from different rules, or different layers
				throw new InvalidAttributeRelationException("Attribute relations have to be contained in a single rule.", aatr);
			}
		}
		assert target_maf!=null || target_aaf != null;
		
		// Exactly one of the following combinations will hold.
		assert (source_maf!=null && target_maf!=null) 
				|| (source_maf!=null && target_aaf!=null)
				|| (source_aaf!=null && target_maf!=null)
				|| (source_aaf!=null && target_aaf!=null);
		
		if (source_maf!=null && target_maf!=null) {
			// One of these must hold.
			if (aatr instanceof AttributeEqualityImpl) {
				return (source_maf.getCurrentAttribute().getValue().equals(target_maf.getCurrentAttribute().getValue()));
			}
			if (aatr instanceof AttributeInequalityImpl) {
				return !(source_maf.getCurrentAttribute().getValue().equals(target_maf.getCurrentAttribute().getValue()));
			}
			throw new MissingFeatureException("Cannot recognize new AbstractAttributeRelation.");
		}
		if (source_maf!=null && target_aaf!=null) {
			// One of these must hold.
			if (aatr instanceof AttributeEqualityImpl) {
				return (source_maf.getCurrentAttribute().getValue().equals(target_aaf.getCurrentAttribute().getValue()));
			}
			if (aatr instanceof AttributeInequalityImpl) {
				return !(source_maf.getCurrentAttribute().getValue().equals(target_aaf.getCurrentAttribute().getValue()));
			}
			throw new MissingFeatureException("Cannot recognize new AbstractAttributeRelation.");
		}
		if (source_aaf!=null && target_maf!=null) {
			// One of these must hold.
			if (aatr instanceof AttributeEqualityImpl) {
				return (source_aaf.getCurrentAttribute().getValue().equals(target_maf.getCurrentAttribute().getValue()));
			}
			if (aatr instanceof AttributeInequalityImpl) {
				return !(source_aaf.getCurrentAttribute().getValue().equals(target_maf.getCurrentAttribute().getValue()));
			}
			throw new MissingFeatureException("Cannot recognize new AbstractAttributeRelation.");
		}
		if (source_aaf!=null && target_aaf!=null) {
			// One of these must hold.
			if (aatr instanceof AttributeEqualityImpl) {
				return (source_aaf.getCurrentAttribute().getValue().equals(target_aaf.getCurrentAttribute().getValue()));
			}
			if (aatr instanceof AttributeInequalityImpl) {
				return !(source_aaf.getCurrentAttribute().getValue().equals(target_aaf.getCurrentAttribute().getValue()));
			}
			throw new MissingFeatureException("Cannot recognize new AbstractAttributeRelation.");
		}
		
		// It is bad to get here...
		throw new InvalidAttributeRelationException("Attribute relations have to be contained in a single rule.", aatr);
	}

	private boolean hasMoreSolutions() {
		return !solutionSet.isEmpty();
	}

	private void mergeApplyFilters(MatchFilter mf) {
		for(ApplyEntityFilter maef: mf.getApplyEntityFilters()) {
			for(ApplyEntity ae : this.getApplyFilter().getEntities()) {
				if(ae.getApplyClass() == maef.getApplyClass()) {
					System.out.println("filled " + ae.getDotNotation() +" with " + maef.getDotNotation());
					ae.setCurrentEntity(maef.getCurrentEntity());
					ae.setPastEntity(true);
					
				}
			}
		}
	}

	private List<InstanceEntity> copyChildren(InstanceDatabase inputModel, InstanceDatabase outputModel, InstanceEntity rootIn, MetaModelDatabase applyMetaModel) {
		List<InstanceEntity> entities = new LinkedList<InstanceEntity>();
		outputModel.getInstanceEntities().add(rootIn);
		outputModel.createTemporalRelation(rootIn,rootIn);
		if(!rootIn.getTemporalChildren().contains(rootIn))
			rootIn.getTemporalChildren().add(rootIn);
		
		entities.add(rootIn);
		for (InstanceAttribute ia : inputModel.getAllAttributesOf(rootIn))
			outputModel.getInstanceAttributes().add(ia);
		
		for (InstanceRelation ir :inputModel.getRelationsLeaving(rootIn)) {
			if (ir.getRelation().isContainment()) {
				MetaEntity me = ir.getTarget().getMetaEntity();
				try {
					MetaEntity meA = applyMetaModel.getMetaEntityByName(me.getNamespace(), me.getName());
					ir.getTarget().setMetaEntity(meA);
				} catch (InvalidLayerRequirement e) {
					e.printStackTrace();
				}
				outputModel.getInstanceRelations().add(new InstanceRelation(rootIn, ir.getRelation(), ir.getTarget()));	
				entities.addAll(copyChildren(inputModel, outputModel, ir.getTarget(), applyMetaModel));
			}
		}
		return entities;
	}
	
	private void fillNonContainments(InstanceDatabase inputModel, InstanceDatabase outputModel, List<InstanceEntity> ies) {
		for (InstanceEntity ie : ies) {
			for (InstanceRelation ir : inputModel.getRelationsLeaving(ie)) {
				if (!ir.getRelation().isContainment()) {
					if (ies.contains(ir.getSource()) && ies.contains(ir.getTarget()))
						outputModel.getInstanceRelations().add(new InstanceRelation(ir.getSource(), ir.getRelation(), ir.getTarget()));
				}
			}
		}
	}
	
	
	public void performApply(InstanceDatabase outputModelInstanceDatabase, MetaModelDatabase applyMetaModel, InstanceDatabase matchModel) throws InvalidLayerRequirement, SecurityException, IllegalArgumentException, ClassNotFoundException, NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException{
		// apply the transformation
		getApplyFilter().performApply(outputModelInstanceDatabase, applyMetaModel, matchModel);
		processMappings();
		instantiateTemporalRelations(outputModelInstanceDatabase);
		getApplyFilter().updateFilters();
	}

	private void instantiateTemporalRelations(InstanceDatabase instanceDatabase) {
		// instantiate Temporal Relations (history)
		for (MatchFilter mf : this.getMatchFilter()) {
			for(MatchEntityFilter mef : mf.getMatchEntityFilters()) {
				if(MatchFilter.isPositive(mef.getMatchClass())) {
					for(ApplyEntity ae: this.getApplyFilter().getEntities()) {
						boolean hasDefault = false;
						for(ApplyAttributeFilter aaf : ae.getFilterAttributes()) {
							if(aaf.getName().equals(DSLTransAttribute.DSLTRANS_DEFAULT)) {
								hasDefault = true;
								break;
							}
						}
						if(!ae.isPastEntity() && !ae.isImported() && hasDefault) {
							instanceDatabase.createTemporalRelation(mef.getCurrentEntity(),ae.getCurrentEntity());
						}
					}
				}
			}
		}
	}

	private void processMappings() {
		
		// process attribute mappings
		for (Object obj : getRule().getApply().getClass_()) {
			dsltrans.ApplyClass ac = (dsltrans.ApplyClass) obj;
			AbstractFilter af = getApplyFilter().getFilter(ac);
			ApplyEntityFilter aef = (ApplyEntityFilter)af;
			for (Object obj2 : ac.getAttribute()) {
				dsltrans.ApplyAttribute at = (dsltrans.ApplyAttribute) obj2;
				for (ApplyAttributeFilter aaf : aef.getFilterAttributes()) {
					if (aaf.getAttribute().equals(at)) {
						dsltrans.Term attValue = at.getAttributeValue();
						String attributeValue;
						
						//Attribute might've been processed through another's reference
						if (getTermProcessor().hasTerm(attValue))
							attributeValue = getTermProcessor().getTerm(attValue);
						else
							attributeValue = getTermProcessor().processTerm(at.getAttributeValue(), getRule());
						
						InstanceAttribute ia = aaf.getCurrentAttribute();
						if (ia == null)
							break;
						if (attributeValue.isEmpty())
							attributeValue = ia.getMetaAttribute().getDefaultValueString();
						if (attributeValue != null) {
							if (ia.getMetaAttribute().getType().equals("int"))
								aaf.getCurrentAttribute().setValue(Integer.parseInt(attributeValue));
							else
								aaf.getCurrentAttribute().setValue(attributeValue);
						}
					}
				}
			}
		}
		getTermProcessor().Clear();
	}
	
	private MatchAttributeFilter findMatchAttributeFilter(dsltrans.Attribute a) {
		// Percorre todos os MatchFilters e em cada um deles procura o MatchAttributeFilter e devolve o primeiro que encontrar.
		
		MatchAttributeFilter result = null;
		
		for (MatchFilter mf : getMatchFilter()) {
			result = mf.findMatchAttributeFilter(a);
			if (result != null) {
				return result;
			}
		}
		assert result == null;
		return null;
	}
	
	private ApplyAttributeFilter findApplyAttributeFilter(dsltrans.Attribute a) {
		// Percorre todos os MatchFilters e em cada um deles procura o ApplyAttributeFilter e devolve o primeiro que encontrar.
		
		ApplyAttributeFilter result = null;
		
		for (MatchFilter mf : getMatchFilter()) {
			result = mf.findApplyAttributeFilter(a);
			if (result != null) {
				return result;
			}
		}
		assert result == null;
		return null;
	}

	public void processImports(ApplyEntityFilter aef, InstanceDatabase instanceDatabase,
			MetaModelDatabase applyMetaModel, InstanceDatabase matchModel) {
		//process imports
		dsltrans.Import imp = null;
		dsltrans.ApplyClass ac = aef.getApplyClass();
		for(Object obj : this.getRule().getImports()) {
			dsltrans.Import sr = (dsltrans.Import) obj;
			if(sr.getTarget() == ac) {
				for (Object obj2 : this.getRule().getMatch()){
					dsltrans.MatchModel mm = (dsltrans.MatchModel) obj2;
					if(mm.getExplicitSource() != null && mm.getClass_().contains(sr.getSource())) {
						imp = sr;
						break;
					}
				}
			}
			if(imp != null)
				break;
		}
		if(imp == null) return;
		
		//get InstanceEntity from MatchModel - old root
		InstanceEntity ie = null;
		@SuppressWarnings("unused")
		MatchFilter currMatch = null;
		{
			dsltrans.MatchClass match = imp.getSource();
			AbstractFilter af = null;
			for (MatchFilter mf : getMatchFilter()) {
				currMatch = mf;
				af = mf.getFilter(match);
				if (af != null)
					break;
			}
			if(af != null && match instanceof dsltrans.PositiveMatchClass) {
				MatchEntityFilter mef = (MatchEntityFilter) af;
				ie = mef.getCurrentEntity();
			}
		}

		InstanceDatabase input = null;
		for (MatchFilter mf : getMatchFilter()) {
			if (mf.get_matchModel().getClass_().contains(imp.getSource()) &&
					mf.get_explicitMatchModel() != null)
				input = mf.get_explicitMatchModel();
		}
		if(input == null) return;
			
		//copy all children to output and link with new root
		List<InstanceEntity> entitiesCreated = copyChildren(input, instanceDatabase, ie, applyMetaModel);
		
		//fill non containment relations between subtree copied 
		fillNonContainments(input, instanceDatabase, entitiesCreated);

		// instantiate Temporal Relations (history)
		for (MatchFilter mf : this.getMatchFilter()) {
			for(MatchEntityFilter mef : mf.getMatchEntityFilters()) {
				if(MatchFilter.isPositive(mef.getMatchClass())) {
					//for(InstanceEntity ieC : entitiesCreated) {
					instanceDatabase.createTemporalRelation(mef.getCurrentEntity(),ie);
					if(!mef.getCurrentEntity().getTemporalChildren().contains(ie))
						mef.getCurrentEntity().getTemporalChildren().add(ie);
					//}
				}
			}
		}
		
		for(ApplyAttributeFilter aaf : aef.getFilterAttributes()) {
			if(aaf.getName().equals(DSLTransAttribute.DSLTRANS_DEFAULT)) {
				for(InstanceEntity newentity: entitiesCreated) {
					InstanceAttribute ia;
					try {
						ia = new InstanceAttribute(newentity,applyMetaModel.getMetaAttributeByName(DSLTransAttribute.DSLTRANS_DEFAULT));
						dsltrans.Term term = aaf.getAttribute().getAttributeValue();
						ia.setValue(((dsltrans.Atom)term).getValue());
						instanceDatabase.getInstanceAttributes().add(ia);
						aaf.setCurrentAttribute(ia);
					} catch (InvalidLayerRequirement e) {
						e.printStackTrace();
					}
				}
				break; // only one per instance
			}
		}
		
		aef.setCurrentEntity(ie);
	}
	
	public dsltrans.Rule getRule() {
		return _rule;
	}

	public void setProcessed(boolean _processed) {
		this._processed = _processed;
	}

	public boolean isProcessed() {
		return _processed;
	}

	public void setTermProcessor(TermProcessor _termprocessor) {
		this._termprocessor = _termprocessor;
	}

	public TermProcessor getTermProcessor() {
		return _termprocessor;
	}

	public boolean hasImportClass(dsltrans.ApplyClass mc) {
		for(Object obj : this.getRule().getImports()) {
			dsltrans.Import sr = (dsltrans.Import) obj;
			if(sr.getTarget() == mc)
				return true;
		}
		return false;
	}

	void setLayer(TransformationLayer _layer) {
		this._layer = _layer;
	}

	public TransformationLayer getLayer() {
		return _layer;
	}

	public boolean hasMayBeSameRelation(dsltrans.MatchClass mc_i, dsltrans.MatchClass mc_j) {
		
		/*
		 * Look in every association that is a MayBeSameRelation {
		 *  if the source and/or target correspond to the mc_i and mc_j then {
		 *   return true.
		 *  }
		 * }
		 * return false if none is found
		 * 
		 */
		
		for (Object obj : getRule().getMatch()) {
			dsltrans.MatchModel mm = (dsltrans.MatchModel) obj;
			for (Object obj2: mm.getAssociation()) {
				dsltrans.MatchAssociation ma = (dsltrans.MatchAssociation) obj2;
				if (ma instanceof dsltrans.MatchMayBeSameRelation) {
					if ( (mc_i == ma.getSource() && mc_j == ma.getTarget()) ||
							( mc_j == ma.getSource() && mc_i == ma.getTarget() )) {
						return true;
					}
				}
			}
		}
		
		return false;
	}

	public boolean hasMayBeSameRelation(dsltrans.ApplyClass ac_i, dsltrans.ApplyClass ac_j) {
		
		/*
		 * Look in every association that is a MayBeSameRelation {
		 *  if the source and/or target correspond to the mc_i and mc_j then {
		 *   return true.
		 *  }
		 * }
		 * return false if none is found
		 * 
		 */

		dsltrans.ApplyModel mm = getRule().getApply();
		for (Object obj: mm.getAssociation()) {
			dsltrans.ApplyAssociation ma = (dsltrans.ApplyAssociation) obj;
			if (ma instanceof dsltrans.ApplyMayBeSameRelation) {
				if ( (ac_i == ma.getSource() && ac_j == ma.getTarget()) ||
						( ac_j == ma.getSource() && ac_i == ma.getTarget() )) {
					return true;
				}
			}
		}
		
		return false;
	}

	@Override
	public String toString() {
		return _rule.getDescription();
	}
	
	
	
}

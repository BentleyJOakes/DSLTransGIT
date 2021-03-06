package dsltransEngine.transformer;

import dsltransEngine.io.ModelExporter;
import dsltransEngine.io.PersistenceLayer;
import dsltransEngine.metamodel.MetaEntity;
import dsltransEngine.metamodel.MetaModelDatabase;
import dsltransEngine.model.InstanceDatabase;
import dsltransEngine.model.InstanceEntity;
import dsltransEngine.transformer.exceptions.InvalidAttributeRelationException;
import dsltransEngine.transformer.exceptions.InvalidLayerRequirement;
import dsltransEngine.transformer.exceptions.TransformationLayerException;
import dsltransEngine.transformer.exceptions.TransformationRefinementLayerException;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public abstract class TransformationLayer  extends TransformationUnit {
	private final dsltrans.Layer _layer;
	private TransformationUnit _precedingUnit;
	private MetaModelDatabase _matchMetaModel;
	private InstanceDatabase _matchModel;
	private List<TransformationRule> _rules = null;
	private final TransformationController _controller;
	protected final PersistenceLayer persistenceLayer;
	
	TransformationLayer(TransformationController tc, dsltrans.Layer l, PersistenceLayer persistenceL) {
		super();
		_layer = l;
		setPrecedingUnit(null);
		setMatchMetaModel(null);
		setMatchModel(null);
		setRules(new LinkedList<TransformationRule>());
		_controller = tc;
		persistenceLayer = persistenceL;
	}

	public void setRules(LinkedList<TransformationRule> linkedList) {
		_rules = linkedList;
	}
	
	public dsltrans.Layer getLayer() {
		return _layer;
	}
	
	public List<dsltrans.AbstractSource> getRequirements() {
		List<dsltrans.AbstractSource> requirements = new LinkedList<dsltrans.AbstractSource>();
		requirements.addAll(this.getLayer().getPreviousSource());
		for(Object obj : this.getLayer().getHasRule()) {
			dsltrans.Rule rule = (dsltrans.Rule) obj;
			for (Object obj2 : rule.getMatch()) {
				dsltrans.MatchModel mm = (dsltrans.MatchModel) obj2;
				if (mm.getExplicitSource() != null) {
					requirements.add(mm.getExplicitSource());
				}
			}
		}
		return requirements;
	}

	protected void executeRules() throws Throwable {
		System.out.println("\nExecution rules from layer: " + this.getLayer().getName());
		boolean hasmatch;
		
//DEBUG
//if (this.getLayer().getDescription().equals("2nd Layer"))
//	System.out.println("");

		for(TransformationRule rule : getTransformationRules()) {
			hasmatch = true; // everytime we get a new rule we admit to have new match
			while(hasmatch) {
				hasmatch = rule.performMatch(_controller,
								this.getMatchModel(),
								this.getOutputModelDatabase(),
								this.getMatchMetaModel(),
								this.getMetaDatabase()
							);
				if(hasmatch) 
					rule.performApply(this.getOutputModelDatabase(), this.getMetaDatabase(), this.getMatchModel());
			}
			rule.clean();
		}
		
		for(TransformationRule rule : getTransformationRules()) {
			rule.MarkTemporalRelations(this.getOutputModelDatabase());
		}
		this.getOutputModelDatabase().refreshTemporals();
	}

	@Override
	public void Check() {
		
	}

	public void Execute(TransformationUnit preUnit) throws Throwable {
		setProcessed(true);		
		this.setPrecedingUnit(preUnit);
		prepareInputModel();
		try {
			prepareOutputModel(_controller);
		} catch (TransformationRefinementLayerException e) {
			System.err.println("ERROR: RefinementLayer Output failed");
		}
		buildRules();
		Check();
		try {
			executeRules();
			output();
		} catch (SecurityException e) {
			throw new TransformationLayerException("SecurityException at:", this, e);
		} catch (IllegalArgumentException e) {
			throw new TransformationLayerException("IllegalArgumentException at:", this, e);
		} catch (ClassNotFoundException e) {
			throw new TransformationLayerException("ClassNotFoundException at:", this, e);
		} catch (NoSuchFieldException e) {
			throw new TransformationLayerException("NoSuchFieldException at:", this, e);
		} catch (IllegalAccessException e) {
			throw new TransformationLayerException("IllegalAccessException at:", this, e);
		} catch (InvocationTargetException e) {
			throw new TransformationLayerException("InvocationTargetException at:", this, e);
		} catch (NoSuchMethodException e) {
			throw new TransformationLayerException("NoSuchMethodException at:", this, e);
		} catch (InvalidLayerRequirement e) {
			throw new TransformationLayerException("InvalidLayerRequirements at:", this, e);
		} catch (IOException e) {
			throw new TransformationLayerException("IOException at:", this, e);
		} catch (InvalidAttributeRelationException e) {
			throw new TransformationLayerException("InvalidAttributeRelationException at:", this, e);
		}
	}

	protected abstract void prepareOutputModel(TransformationController control) throws Throwable;

	protected abstract void prepareInputModel();

	private void buildRules() {
		System.out.println("\nBuilding rules from layer: " + this.getLayer().getName());
		for(Object obj : this.getLayer().getHasRule()) {
			dsltrans.Rule rule = (dsltrans.Rule) obj;
			getTransformationRules().add(new TransformationRule(this, rule, this._controller.getDatabaseManager()));
		}
	}

	private List<TransformationRule> getTransformationRules() {
		return _rules;
	}

	public String getOutputFilePathURI() {
		return this.getLayer().getOutputFilePathURI();
	}
	
	public String getName() {
		return this.getLayer().getName();
	}
	
	public String getGroupName() {
		if(this.getLayer().getGroupName() == null)
			return "";
		return this.getLayer().getGroupName();
	}
	
	private void output() throws Throwable {
		System.out.println("#$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
		this.getOutputModelDatabase().dump();
		System.out.println("#$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");

		String outputpath = getOutputFilePathURI();
		
		if (outputpath != null) {
			if(outputpath.isEmpty()) return;
		}
		else
			return;
		
		if(this.getOutputModelDatabase().getRootElement() == null) {
			//MetaEntity rootEntity = this.getMetaDatabase().getRootMetaEntity();
			HashSet<MetaEntity> rootEntities = this.getMetaDatabase().getRootMetaEntities();

			for (MetaEntity rootEntity : rootEntities){
				List<InstanceEntity> ielist = this.getOutputModelDatabase().getAllInstancesOf(rootEntity);
//				if(ielist.isEmpty()) {
//					System.err.println("Error - Could not find root entity: " + rootEntity.getName());
//					return;
//				} else
//					this.getOutputModelDatabase().setRootElement(ielist.get(0));
				if (!ielist.isEmpty()){
					this.getOutputModelDatabase().setRootElement(ielist.get(0));
					break;
				}
			}

		}
		ModelExporter exporter = persistenceLayer.buildModelExporter();
		exporter.setMetaModelDatabase(this.getMetaDatabase());
		exporter.setInstanceDatabase(this.getOutputModelDatabase());

		exporter.saveTo(outputpath);
	}

	public InstanceDatabase getMatchModel() {
		return _matchModel;
	}

	public MetaModelDatabase getMatchMetaModel() {
		return _matchMetaModel;
	}	
	
	protected void setMatchModel(InstanceDatabase database) {
		_matchModel = database;
	}

	protected void setMatchMetaModel(MetaModelDatabase metaDatabase) {
		_matchMetaModel = metaDatabase;
	}

	public TransformationSource getTransformationSource(
			TransformationUnit precedingUnit) {
		if(precedingUnit instanceof TransformationSource) {
			return (TransformationSource) precedingUnit;
		}
		if(precedingUnit instanceof TransformationLayer) {
			return getTransformationSource(((TransformationLayer)precedingUnit).getPrecedingUnit());
		}
		return null;
	}

	public void setPrecedingUnit(TransformationUnit _precedingUnit) {
		this._precedingUnit = _precedingUnit;
	}

	public TransformationUnit getPrecedingUnit() {
		return _precedingUnit;
	}
	
	public TransformationSource getSource(dsltrans.FilePort as) {
		return _controller.getSource(as);
	}
	
	public String getMetamodelIdentifier() {
		dsltrans.MetaModelIdentifier mmi = getLayer().getMetaModelId();
		return mmi.getMetaModelName();
	}
}

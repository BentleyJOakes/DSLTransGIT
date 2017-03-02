package dsltransEngine.transformer.filter;

import dsltrans.MatchAttribute;
import dsltransEngine.metamodel.MetaEntity;
import dsltransEngine.metamodel.MetaModelDatabase;
import dsltransEngine.model.InstanceAttribute;
import dsltransEngine.model.InstanceDatabase;
import dsltransEngine.model.InstanceDatabaseManager;
import dsltransEngine.model.InstanceEntity;
import dsltransEngine.transformer.TransformationRule;
import dsltransEngine.transformer.exceptions.InvalidLayerRequirement;

import java.util.LinkedList;
import java.util.List;

public class MatchEntityFilter extends AbstractFilter {

	private final dsltrans.MatchClass _class;
	private InstanceEntity _currentEntity;
	private List<MatchAttributeFilter> _filterAttributes;
	private TransformationRule _transformationrule;
	
	public MatchEntityFilter(TransformationRule tr, dsltrans.MatchClass mc, String id, InstanceDatabaseManager instanceDatabaseManager) {
		super(id, instanceDatabaseManager);
		setTransformationRule(tr);
		_class = mc;
		setCurrentEntity(null);
		setFilterAttributes(new LinkedList<MatchAttributeFilter>());
		
		for(Object obj : getMatchClass().getAttribute()) {
			MatchAttribute ma = (MatchAttribute) obj;
			getFilterAttributes().add(new MatchAttributeFilter(ma,id, this, instanceDatabaseManager));
		}
	}

	public dsltrans.MatchClass getMatchClass() {
		return _class;
	}

	@Override
	public boolean process(InstanceDatabase database, MetaModelDatabase metamodel) throws InvalidLayerRequirement {
		
		MetaEntity me = metamodel.getMetaEntityByName(this.getMatchClass().getPackageName(), this.getMatchClass().getClassName());
		
		for(InstanceEntity ie : database.getInstanceEntities()) {
			// Eh aqui que fica a implementacao do allowInheritance nas match classes.
			if(( (me == ie.getMetaEntity()) || ( allowsInheritance() && ie.getMetaEntity().isSubTypeOf(me) ) )   && performMatchAttributes(ie,database,metamodel)) {
				ie.setDotNotation(this.getDotNotation());
				this.getFilterDatabase().getInstanceEntities().add(ie);
			}
		}
		return !this.getFilterDatabase().isEmpty();
	}

	private boolean allowsInheritance() {
		return getMatchClass().isAllowInheritance();
	}

	private boolean performMatchAttributes(InstanceEntity ie, InstanceDatabase database,
			MetaModelDatabase metamodel) {
		
		for(MatchAttributeFilter af : getFilterAttributes()) {
			this.setCurrentEntity(ie);
			if(!af.process(database, metamodel)) return false;
			
			for(InstanceAttribute ia : database.getInstanceAttributes()) {
				if(ia.getMetaAttribute().getName().equals(af.getAttribute().getAttributeName()) &&
					ia.getEntity().hashCode() == ie.hashCode())
				{
					af.getFilterDatabase().getInstanceAttributes().add(ia);
				}
			}			
		}
		return true; // for now we accept if the attribute instance do not exist on the database
	}

	public void setCurrentEntity(InstanceEntity _currentSolution) {
		this._currentEntity = _currentSolution;
	}

	public InstanceEntity getCurrentEntity() {
		return _currentEntity;
	}

	public void setFilterAttributes(List<MatchAttributeFilter> _filterAttributes) {
		this._filterAttributes = _filterAttributes;
	}

	public List<MatchAttributeFilter> getFilterAttributes() {
		return _filterAttributes;
	}

	public void setCurrentByHashId(InstanceDatabase instanceDatabase, int parseInt) {
		for(InstanceEntity ie : instanceDatabase.getInstanceEntities()) {
			if(ie.hashCode() == parseInt) {
				for(MatchAttributeFilter af:getFilterAttributes())
					af.setCurrentByEntity(ie);
				setCurrentEntity(ie);
				return;
			}
		}	
	}

	public String getDotNotation() {
		return "'"+this.getMatchClass().getPackageName()+"."+this.getMatchClass().getClassName()+"'";
	}

	public void setTransformationRule(TransformationRule _transformationrule) {
		this._transformationrule = _transformationrule;
	}

	public TransformationRule getTransformationRule() {
		return _transformationrule;
	}

	public MatchAttributeFilter findMatchAttributeFilter(dsltrans.Attribute a) {
		// Percorre todos os MatchAttributeFilters e retorna aquele que corresponde ao Attribute dado.
		for(MatchAttributeFilter af : getFilterAttributes()) {
			if (af.correspondsTo(a)) {
				return af;
			}
		}
		// No attributeFilter in this match entity filter for the given attribute.
		return null;
	}

}

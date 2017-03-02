package dsltransEngine.transformer.filter;

import dsltransEngine.metamodel.MetaEntity;
import dsltransEngine.metamodel.MetaModelDatabase;
import dsltransEngine.model.InstanceDatabase;
import dsltransEngine.model.InstanceDatabaseManager;
import dsltransEngine.model.InstanceRelation;
import dsltransEngine.transformer.exceptions.InvalidLayerRequirement;

public class MatchRelationFilter  extends AbstractFilter {
	
	private final dsltrans.MatchAssociation _association;
	private InstanceRelation _currentRelation;

	public MatchRelationFilter(dsltrans.MatchAssociation ma, String id, InstanceDatabaseManager instanceDatabaseManager) {
		super(id, instanceDatabaseManager);
		_association = ma;
		setCurrentRelation(null);
	}

	public dsltrans.MatchAssociation getAssociation() {
		return _association;
	}
	
	public dsltrans.MatchClass getSourceClass() {
		return getAssociation().getSource();
	}

	public dsltrans.MatchClass getTargetClass() {
		return getAssociation().getTarget();
	}	
	
	@Override
	public boolean process(InstanceDatabase database, MetaModelDatabase metamodel) throws InvalidLayerRequirement {

		MetaEntity sme = metamodel.getMetaEntityByName(this.getAssociation().getSource().getPackageName(), this.getAssociation().getSource().getClassName());
		MetaEntity tme = metamodel.getMetaEntityByName(this.getAssociation().getTarget().getPackageName(), this.getAssociation().getTarget().getClassName());

		if(isDirect()) {
						
			for(InstanceRelation ir : database.getInstanceRelations()) {
				if(ir.getRelation().getName().
						equals(getAssociation().getAssociationName())
				   &&
				   (ir.getSource().getMetaEntity().isSubTypeOf(sme))
				   &&
				   (ir.getTarget().getMetaEntity().isSubTypeOf(tme))
				)
				{
					getFilterDatabase().getInstanceRelations().add(ir);
				}
			}			
		} else return !this.getFilterDatabase().isEmpty();

		return !this.getFilterDatabase().isEmpty();
	}

	public boolean isDirect() { return getAssociation() instanceof dsltrans.PositiveMatchAssociation ||
		getAssociation() instanceof dsltrans.NegativeMatchAssociation; }

	public boolean isIndirect() { return getAssociation() instanceof dsltrans.PositiveIndirectAssociation ||
		getAssociation() instanceof dsltrans.NegativeIndirectAssociation; }
	
	public void setCurrentRelation(InstanceRelation _currentRelation) {
		this._currentRelation = _currentRelation;
	}

	public InstanceRelation getCurrentRelation() {
		return _currentRelation;
	}

	public void setCurrentByHashId(InstanceDatabase instanceDatabase, int parseInt) {
		for(InstanceRelation ir : instanceDatabase.getInstanceRelations()) {
			if(ir.hashCode() == parseInt) {
				setCurrentRelation(ir);
				return;
			}
		}	
	}

}

package dsltransEngine.transformer.filter;

import dsltransEngine.metamodel.MetaModelDatabase;
import dsltransEngine.model.InstanceDatabase;
import dsltransEngine.model.InstanceDatabaseManager;
import dsltransEngine.model.InstanceRelation;
import dsltransEngine.transformer.exceptions.InvalidLayerRequirement;

import java.lang.reflect.InvocationTargetException;

public class ApplyRelation extends AbstractFilter {
	
	private final dsltrans.ApplyAssociation _association;
	private InstanceRelation _currentRelation;

	public ApplyRelation(dsltrans.ApplyAssociation ma, String id, InstanceDatabaseManager instanceDatabaseManager) {
		super(id, instanceDatabaseManager);
		_association = ma;
		setCurrentRelation(null);
	}

	public dsltrans.ApplyAssociation getAssociation() {
		return _association;
	}
	
	public dsltrans.ApplyClass getSourceClass() {
		return getAssociation().getSource();
	}

	public dsltrans.ApplyClass getTargetClass() {
		return getAssociation().getTarget();
	}	
	
	public boolean performApply(InstanceDatabase database, MetaModelDatabase metamodel) {
		for(InstanceRelation ir : database.getInstanceRelations()) {
			if(ir.getRelation().getName().
					equals(getAssociation().getAssociationName())
			)
			{
				getFilterDatabase().getInstanceRelations().add(ir);
			}
		}			
		return !this.getFilterDatabase().isEmpty();
	}

	public void setCurrentRelation(InstanceRelation _currentRelation) {
		this._currentRelation = _currentRelation;
	}

	public InstanceRelation getCurrentRelation() {
		return _currentRelation;
	}

	public void setCurrentByHashId(int parseInt) {
		for(InstanceRelation ir : getFilterDatabase().getInstanceRelations()) {
			if(ir.hashCode() == parseInt) {
				setCurrentRelation(ir);
				return;
			}
		}	
	}

	@Override
	public boolean process(InstanceDatabase model, MetaModelDatabase mmodel)
			throws SecurityException,
			IllegalArgumentException, ClassNotFoundException,
			NoSuchFieldException, IllegalAccessException,
			NoSuchMethodException, InvocationTargetException,
			InvalidLayerRequirement {
		return false;
	}

}

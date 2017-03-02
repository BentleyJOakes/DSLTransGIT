package dsltransEngine.transformer.filter;

import dsltransEngine.metamodel.MetaModelDatabase;
import dsltransEngine.model.InstanceDatabase;
import dsltransEngine.model.InstanceDatabaseManager;
import dsltransEngine.model.InstanceRelation;
import dsltransEngine.transformer.exceptions.InvalidLayerRequirement;

import java.lang.reflect.InvocationTargetException;

public class TemporalRelationFilter  extends AbstractFilter {
	
	private final dsltrans.AbstractTemporalRelation _association;
	private InstanceRelation _currentRelation;

	public TemporalRelationFilter(dsltrans.AbstractTemporalRelation ma, String id, InstanceDatabaseManager instanceDatabaseManager) {
		super(id, instanceDatabaseManager);
		_association = ma;
		setCurrentRelation(null);
	}

	public dsltrans.AbstractTemporalRelation getAssociation() {
		return _association;
	}
	
	public dsltrans.MatchClass getSourceClass() {
		return getAssociation().getSourceClass();
	}

	public dsltrans.ApplyClass getTargetClass() {
		return getAssociation().getTargetClass();
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

	public void setCurrentByHashId(InstanceDatabase filterDatabase, int parseInt) {
		for(InstanceRelation ir : filterDatabase.getInstanceRelations()) {
			if(ir.hashCode() == parseInt) {
				setCurrentRelation(ir);
				return;
			}
		}
	}

}
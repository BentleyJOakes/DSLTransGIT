package dsltransEngine.transformer.filter;

import dsltransEngine.metamodel.MetaModelDatabase;
import dsltransEngine.model.InstanceDatabase;
import dsltransEngine.model.InstanceDatabaseManager;
import dsltransEngine.transformer.exceptions.InvalidLayerRequirement;

import java.lang.reflect.InvocationTargetException;

public abstract class AbstractFilter {
	private final String _id;
	private final InstanceDatabase _FilterDatabase;
	
	public AbstractFilter(String id, InstanceDatabaseManager instanceDatabaseManager) {
		_id = id;
		_FilterDatabase = instanceDatabaseManager.createInstanceDatabase();
	}

	public String getId() {
		return _id;
	}
	
	public InstanceDatabase getFilterDatabase() {
		return _FilterDatabase;
	}

	public abstract boolean process(InstanceDatabase model, MetaModelDatabase mmodel)
			throws SecurityException, IllegalArgumentException, ClassNotFoundException, NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException, InvalidLayerRequirement;
}

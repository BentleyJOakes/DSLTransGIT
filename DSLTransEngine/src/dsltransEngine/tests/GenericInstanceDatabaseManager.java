package dsltransEngine.tests;

import dsltransEngine.model.InstanceDatabase;
import dsltransEngine.model.InstanceDatabaseManager;

public class GenericInstanceDatabaseManager implements InstanceDatabaseManager {
	
	InstanceDatabase outputInstanceDatabase;
	
	@Override
	public InstanceDatabase createInstanceDatabase() {
		return new GenericInstanceDatabase();
	}

}

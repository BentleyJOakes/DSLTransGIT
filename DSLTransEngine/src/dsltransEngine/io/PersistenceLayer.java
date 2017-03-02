package dsltransEngine.io;

import dsltransEngine.model.InstanceDatabaseManager;

public interface PersistenceLayer {
	ModelExporter buildModelExporter();
	ModelLoader buildModelLoader(InstanceDatabaseManager instanceDatabaseManager);
}

package dsltransEngine.tests;

import dsltransEngine.io.ModelExporter;
import dsltransEngine.io.ModelLoader;
import dsltransEngine.io.PersistenceLayer;
import dsltransEngine.metamodel.MetaModelDatabase;
import dsltransEngine.model.InstanceDatabaseManager;

public class GenericPersistenceLayer implements PersistenceLayer {
	
	public GenericInstanceDatabase inputModel,outputModel;
	public MetaModelDatabase inputMetamodel,outputMetamodel;
	
	@Override
	public ModelExporter buildModelExporter() {
		return new GenericModelExporter(this);
	}

	@Override
	public ModelLoader buildModelLoader(
			InstanceDatabaseManager instanceDatabaseManager) {
		return new GenericModelLoader(this);
	}

}

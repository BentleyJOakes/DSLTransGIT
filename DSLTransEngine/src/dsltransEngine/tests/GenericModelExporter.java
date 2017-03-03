package dsltransEngine.tests;

import dsltransEngine.io.ModelExporter;
import dsltransEngine.metamodel.MetaModelDatabase;
import dsltransEngine.model.InstanceDatabase;
import dsltransEngine.transformer.exceptions.UnsupportedMetamodelException;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class GenericModelExporter implements ModelExporter {
	
	private InstanceDatabase instanceDatabase;
	private MetaModelDatabase metamodelDatabase;
	private GenericPersistenceLayer persistenceLayer;
	
	public GenericModelExporter(GenericPersistenceLayer genericPersistenceLayer) {
		persistenceLayer = genericPersistenceLayer;
	}

	@Override
	public void setInstanceDatabase(InstanceDatabase database) {
		instanceDatabase = database;
	}

	@Override
	public void setMetaModelDatabase(MetaModelDatabase _metaModelDatabase) {
		metamodelDatabase = _metaModelDatabase;
	}

	@Override
	public void saveTo(String path) throws SecurityException,
			IllegalArgumentException, ClassNotFoundException,
			NoSuchFieldException, IllegalAccessException, IOException,
			InvocationTargetException, NoSuchMethodException,
            UnsupportedMetamodelException {
		assert instanceDatabase != null;
		assert metamodelDatabase != null;
		persistenceLayer.outputModel = (GenericInstanceDatabase) instanceDatabase;
		persistenceLayer.outputMetamodel = metamodelDatabase;
	}

}

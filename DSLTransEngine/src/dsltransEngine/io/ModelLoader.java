package dsltransEngine.io;

import dsltransEngine.metamodel.MetaModelDatabase;
import dsltransEngine.model.InstanceDatabase;

import java.io.IOException;

public interface ModelLoader {
	
	public void loadInstances(String metamodelName, String metamodelLocation) throws Throwable;
	public void loadMetaModel(String metamodelLocation) throws IOException;

	public MetaModelDatabase getMetaModelDatabase();

	public void setMetaModelDatabase(MetaModelDatabase metaModelDatabase);

	public InstanceDatabase getInstanceDatabase();
	
	public void prepareInstanceDatabase() throws Throwable;
	
}

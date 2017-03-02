package dsltransEngine.io;

import dsltransEngine.metamodel.MetaModelDatabase;
import dsltransEngine.model.InstanceDatabase;

public interface ModelExporter {
	
	public void setInstanceDatabase(InstanceDatabase database);

	public void setMetaModelDatabase(MetaModelDatabase metamodelDatabase);

	public void saveTo(String location) throws Throwable;
	
}

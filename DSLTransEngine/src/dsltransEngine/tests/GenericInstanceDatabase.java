package dsltransEngine.tests;

import dsltransEngine.metamodel.MetaEntity;
import dsltransEngine.model.InstanceDatabase;
import dsltransEngine.model.InstanceEntity;

import java.lang.reflect.InvocationTargetException;

public class GenericInstanceDatabase extends InstanceDatabase {

	public GenericInstanceDatabase(){
		
	}
	
	@Override
	public InstanceDatabase createEmptyClone() {
		return new GenericInstanceDatabase();
	}

	@Override
	public InstanceEntity createInstance(MetaEntity me)
			throws ClassNotFoundException, SecurityException,
			NoSuchFieldException, IllegalArgumentException,
			IllegalAccessException, NoSuchMethodException,
			InvocationTargetException {
		return new GenericInstanceEntity(me);
	}
	
}

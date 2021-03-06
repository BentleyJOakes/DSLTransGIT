package dsltransEngine.tests;

import dsltransEngine.metamodel.MetaAttribute;
import dsltransEngine.metamodel.MetaEntity;

public class GenericMetaAttribute implements MetaAttribute {
	
	private String type;
	private GenericMetaEntity containnerEntity;
	private Object defaultValue;
	private String name;
	
	public GenericMetaAttribute(String name, GenericMetaEntity containner, String type, Object defaultValue) {
		this.name = name;
		this.containnerEntity = containner;
		this.type = type;
		this.defaultValue = defaultValue;
	}
	
	@Override
	public String getType() {
		return type;
	}

	@Override
	public MetaEntity getContainerMetaEntity() {
		return containnerEntity;
	}

	@Override
	public Object getDefaultValue() {
		return defaultValue;
	}

	@Override
	public String getDefaultValueString() {
		return defaultValue.toString();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean isInheritedFrom(MetaEntity me) {
		if(me == this.getContainerMetaEntity()) return true;
		return me.isSubTypeOf(this.getContainerMetaEntity());
	}

	@Override
	public String print() {
		return name;
	}

	@Override
	public boolean isDSLTransType() {
		return false;
	}
	
}

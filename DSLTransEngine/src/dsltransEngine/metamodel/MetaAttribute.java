package dsltransEngine.metamodel;


public interface MetaAttribute {
	
	public String getType();

	public MetaEntity getContainerMetaEntity();

	public Object getDefaultValue();
	
	public String getDefaultValueString();
	
	public String getName();

	public boolean isInheritedFrom(MetaEntity me);
	
	public String print();
	
	public boolean isDSLTransType();
}

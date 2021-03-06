package dsltransEngine.metamodel;

import dsltransEngine.transformer.exceptions.InvalidLayerRequirement;
import dsltransEngine.transformer.exceptions.UnsupportedMetamodelException;

import java.util.*;

public class MetaModelDatabase {
	// data information
	private List<MetaEntity> _classes;
	private List<MetaAttribute> _attributes;
	private List<MetaRelation> _relations;
	
	public MetaModelDatabase() {
		_classes = new LinkedList<MetaEntity>();
		_attributes = new LinkedList<MetaAttribute>();
		_attributes.add(new DSLTransAttribute()); // with the default dsltransEngine attribute
		_relations = new LinkedList<MetaRelation>();
	}
	
	public List<MetaEntity> getMetaEntities() {
		return _classes;
	}

	public List<MetaAttribute> getMetaAttributes() {
		return _attributes;
	}

	public List<MetaRelation> getMetaRelations() {
		return _relations;
	}
	
	public List<MetaAttribute> getMetaAttributesFromEntity(MetaEntity me) {
		List<MetaAttribute> result = new LinkedList<MetaAttribute>();
		
		for(MetaEntity superme : me.getSuperEntities()) {
			List<MetaAttribute> list = getMetaAttributesFromEntity(superme);
			for(MetaAttribute ma: list) {
				if(!result.contains(ma))
					result.add(ma);
			}
		}{
			for(MetaAttribute ma : getMetaAttributes()) {
				if(ma.getContainerMetaEntity() == me)
					if(!result.contains(ma))
						result.add(ma);
			}
		}
		return result;
	}

	public MetaAttribute getMetaAttributesFromEntityByName(MetaEntity me, String id) {
		for(MetaAttribute ma: getMetaAttributesFromEntity(me)) {
			if(ma.getName().equals(id))
				return ma;
		}
		return null;
	}	
	
	public MetaEntity getMetaEntityByName(String packageName, String name) throws InvalidLayerRequirement {
//DEBUG
//System.out.println("PACKAGENAME: "+packageName);
//System.out.println("NAME: "+name);
//System.out.println("SIZE: "+this.getClasses().size());
		for(MetaEntity me: this.getMetaEntities()) {
//DEBUG
//if (me.getName().equals(name)) {
//System.out.println("NameSpace: "+me.getNamespace());
//System.out.println("ME.Name: "+me.getName());
//}
			if(me.getNamespace().equals(packageName) && me.getName().equals(name))
				return me;
		}
		throw new InvalidLayerRequirement("Cannot resolve package name: " + packageName + " or class name: " + name);		
	}
	
	public MetaRelation getMetaRelationByName(String name, MetaEntity source, MetaEntity target) throws InvalidLayerRequirement {
//System.out.println("RELATIONNAME "+name);		
		for(MetaRelation me: this.getMetaRelations()) {
//			if (me.getName().equals("ownedModules"))
//				System.out.println();
//System.out.println("METANAME :"+me.getName());
//System.out.println("MetaEntitySource :"+me.getSource().getName());
//System.out.println("MetaEntityTarget :"+me.getTarget().getName());
//System.out.println("Source :"+source.getName());
//System.out.println("Target :"+target.getName());
//if (me.getName().equals("typeElementType") && name.equals("typeElementType"))
//	System.out.println();
				
			if(me.getName().equals(name) &&
					   (source.isSubTypeOf(me.getSource())))
//					   &&
	//				   (target.isSubTypeOf(me.getTarget())))
				return me;
		}
		throw new InvalidLayerRequirement("Cannot resolve a subtype relation of: " + name + " source: " + source.getQualifiedName() + " target: " + target.getQualifiedName());
	}

	public MetaAttribute getMetaAttributeByName(String name) throws InvalidLayerRequirement {
		for(MetaAttribute me: this.getMetaAttributes()) {
			if(me.getName().equals(name))
				return me;
		}
		throw new InvalidLayerRequirement("Cannot resolve attribute name: " + name);
	}
	
	public HashSet<MetaEntity> getRootMetaEntities() throws UnsupportedMetamodelException {
		System.out.println("Finding root entities in metamodel... ");
		// root entity is the one such that there are no relations which have it on target role

		HashSet<MetaEntity> rootEntities = new HashSet<MetaEntity>();

		// so search on MetaRelations and gather all target MetaEntities
		Set<MetaEntity> targetEntities = Collections.synchronizedSet(new HashSet<MetaEntity>());
		for(MetaRelation relation : getMetaRelations()) {
			if(relation.isContainment() && (relation.getSource() != relation.getTarget())) { // notice that we only take into account these kind of relations
				targetEntities.add(relation.getTarget());
			}
		}
		
		// And then select all Entities that are not target Entities
		//MetaEntity chosen = null;
		//int counter = 0;
		for(MetaEntity entity : getMetaEntities()) {

			if(!isContainedIn(entity,targetEntities) 
					&& !isSubTypeContainedIn(entity,targetEntities) 
					&& !entity.isAbstract()) {
//				if(chosen == null) {
//					chosen = entity;
//				}
				System.out.println("Candidate root found: " + entity.getQualifiedName());
				//counter++;
				rootEntities.add(entity);
			}
		}
		
//		if(counter > 1) {
//			String errorMsg = "Error: Found " + counter + " roots!";
//			errorMsg += " Please create a simple root in the metamodel that contains entity " + chosen.getQualifiedName();
//			//throw new UnsupportedMetamodelException(errorMsg);
//		}
		
		if (rootEntities.size() == 0) {
			throw new UnsupportedMetamodelException("All the metamodels must have a root EClass.");
		}

		System.out.println("Finding root entity in metamodel... DONE");
		
		return rootEntities;
	}
	
	private boolean isSubTypeContainedIn(MetaEntity entity, Set<MetaEntity> set) {
		boolean result = false;
		List<MetaEntity> subtypes = generateSubTypes(entity);
		for(MetaEntity subtype : subtypes) {
			result |= isContainedIn(subtype,set);
		}
		return result;		
	}
	
	private List<MetaEntity> generateSubTypes(MetaEntity entity) {
		List<MetaEntity> subtypes = new LinkedList<MetaEntity>();
		for(MetaEntity candidate : getMetaEntities()) {
			if(candidate.isSubTypeOf(entity) && candidate != entity) {
				subtypes.add(candidate);
			}
		}
		return subtypes;
	}

	private boolean isContainedIn(MetaEntity entity, Set<MetaEntity> set) {
		boolean result = set.contains(entity);
		for(MetaEntity superEntity : entity.getSuperEntities()) {
			result |= isContainedIn(superEntity,set);
		}
		return result;
	}
}

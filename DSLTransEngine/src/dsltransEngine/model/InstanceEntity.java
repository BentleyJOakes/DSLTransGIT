package dsltransEngine.model;

import dsltransEngine.metamodel.MetaAttribute;
import dsltransEngine.metamodel.MetaEntity;
import dsltransEngine.metamodel.MetaRelation;

import java.util.*;

public abstract class InstanceEntity {
	private MetaEntity _metaentity;
	private List<MetaRelation> _loadedmetarelations;
	private List<MetaAttribute> _loadedmetaattributes;
	private List<InstanceEntity> _parents;
	private String _dotnotation;
	private List<InstanceEntity> _temporalParents;
	private List<InstanceEntity> _temporalChildren;	
	private boolean _fresh = true;
	private Hashtable<MetaRelation,InstanceEntity> _lastPerRelation;
	
	public InstanceEntity(MetaEntity entity) {
		setMetaEntity(entity);
		_parents = new LinkedList<InstanceEntity>();
		_temporalParents = new LinkedList<InstanceEntity>();		
		_temporalChildren = new LinkedList<InstanceEntity>();		
		_loadedmetarelations = new LinkedList<MetaRelation>();
		_lastPerRelation = new Hashtable<MetaRelation,InstanceEntity>();
		_loadedmetaattributes = new LinkedList<MetaAttribute>();
		setDotNotation(getMetaEntity().getDotNotation());
	}
	
	public void setMetaEntity(MetaEntity namespace) {
		this._metaentity = namespace;
	}
	
	public MetaEntity getMetaEntity() {
		return _metaentity;
	}

	public boolean hasLoadedRelation(MetaRelation mr) {
		Iterator<MetaRelation> iter = this._loadedmetarelations.iterator();
		while(iter.hasNext())
		{
			MetaRelation meta = iter.next();
			if(meta.getName() == mr.getName())
				return true;
		}
		return false;
	}

	public void addRelation(MetaRelation ma) {
		if(hasLoadedRelation(ma)) return;
		_loadedmetarelations.add(ma);
	}

	public void addAttribute(MetaAttribute ma) {
		if(hasLoadedAttribute(ma)) return;
		_loadedmetaattributes.add(ma);
	}

	public boolean hasLoadedAttribute(MetaAttribute ma) {
		Iterator<MetaAttribute> iter = _loadedmetaattributes.iterator();
		while(iter.hasNext())
		{
			MetaAttribute meta = iter.next();
			if(meta.getName() == ma.getName() && meta.getType() == ma.getType())
				return true;
		}
		return false;
	}
	
	public abstract String print();

	public abstract boolean isEqual(InstanceEntity source);

	public void setDotNotation(String _dotnotation) {
		this._dotnotation = _dotnotation;
	}
	
	public String getDotNotation() {
		return _dotnotation;
	}

	public void setParents(List<InstanceEntity> _parents) {
		this._parents = _parents;
	}

	public List<InstanceEntity> getParents() {
		return _parents;
	}

	public void addParentHierarchy(InstanceEntity ie) {
		setParents(new LinkedList<InstanceEntity>(ie.getParents()));
		getParents().add(ie);
	}

	public void addTemporalParent(InstanceEntity imef) {
		_temporalParents.add(imef);
	}	
	
	public Collection<InstanceEntity> getTemporalParents() {
		return _temporalParents;
	}

	public Collection<InstanceEntity> getTemporalChildren() {
		return _temporalChildren;
	}
	
	
	public void setFreshness(boolean _fresh) {
		this._fresh = _fresh;
	}

	public boolean isFresh() {
		return _fresh;
	}

	public void setLast(MetaRelation mr, InstanceEntity target) {
		_lastPerRelation.put(mr,target);
	}

	public InstanceEntity getLast(MetaRelation mr) {
		return _lastPerRelation.get(mr);
	}

	public Enumeration<MetaRelation> getLastRelations() {
		return _lastPerRelation.keys();
	}
}

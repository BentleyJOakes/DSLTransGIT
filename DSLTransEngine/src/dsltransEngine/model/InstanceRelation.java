package dsltransEngine.model;

import dsltransEngine.metamodel.MetaRelation;

public class InstanceRelation {
	private InstanceEntity _source;
	private InstanceEntity _target;
	private MetaRelation _relation;
	
	public InstanceRelation(InstanceEntity source, MetaRelation relation, InstanceEntity target) {

		boolean isIssue = source == null || source.getMetaEntity() == null ||
				target == null || target.getMetaEntity() == null ||
				relation == null;
		if (isIssue) {
			String errorMsg = "Error: Issue creating an InstanceRelation!\n";
			String srcStr = source==null?"null":source.getDotNotation();
			String trgtStr = target==null?"null":target.getDotNotation();
			String relStr = relation==null?"null":relation.getName();
			errorMsg += "Source: " + srcStr + "\n";
			errorMsg += "Target: " + trgtStr + "\n";
			errorMsg += "Relation: " + relStr + "\n";
			for(StackTraceElement ste : Thread.currentThread().getStackTrace()){
				errorMsg += ste + "\n";
			}
			throw new IllegalArgumentException(errorMsg);
		}
		setSource(source);
		setTarget(target);
		setRelation(relation);
	}

	public void setSource(InstanceEntity _object) {
		this._source = _object;
	}

	public InstanceEntity getSource() {
		return _source;
	}

	public void setTarget(InstanceEntity _object) {
		this._target = _object;
	}

	public InstanceEntity getTarget() {
		return _target;
	}	
	
	public void setRelation(MetaRelation relation) {
		this._relation = relation;
	}

	public MetaRelation getRelation() {
		return _relation;
	}
	
	public boolean isEqual(InstanceRelation ie) {
		return this.getSource().isEqual(ie.getSource()) &&
				this.getTarget().isEqual(ie.getTarget()) &&
				this.getRelation().isEqual(ie.getRelation());
	}
	
	public String print() {
		if(getRelation().isContainment())
			return getSource().print()+"<"+ getRelation().getName() +">-->" + getTarget().print();
		return getSource().print()+"("+ getRelation().getName() +")-->" + getTarget().print();
	}
}

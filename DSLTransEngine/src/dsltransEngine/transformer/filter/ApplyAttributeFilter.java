package dsltransEngine.transformer.filter;

import dsltrans.Atom;
import dsltrans.Term;
import dsltransEngine.metamodel.DSLTransAttribute;
import dsltransEngine.metamodel.MetaModelDatabase;
import dsltransEngine.model.InstanceAttribute;
import dsltransEngine.model.InstanceDatabase;
import dsltransEngine.model.InstanceDatabaseManager;
import dsltransEngine.model.InstanceEntity;
import dsltransEngine.transformer.exceptions.InvalidLayerRequirement;

import java.lang.reflect.InvocationTargetException;

public class ApplyAttributeFilter extends AbstractFilter {

	private final dsltrans.ApplyAttribute _attribute;
	private InstanceAttribute _currentAttribute;
	private MetaModelDatabase _metaModel;
	private ApplyEntityFilter _entity;
	private dsltrans.Rule _rule;
	
	public ApplyAttributeFilter(dsltrans.ApplyAttribute ma, String id, ApplyEntityFilter aef, dsltrans.Rule r, InstanceDatabaseManager instanceDatabaseManager) {
		super(id, instanceDatabaseManager);
		_attribute = ma;
		setCurrentAttribute(null);
		setEntity(aef);
		_rule = r;
		String attributename = ma.getAttributeName();
		if(attributename==null || attributename.isEmpty()
				|| attributename.equals("ApplyAttribute")) {
			ma.setAttributeName(DSLTransAttribute.DSLTRANS_DEFAULT);
		}
	}
	
	public dsltrans.ApplyAttribute getAttribute() {
		return _attribute;
	}

	public void setCurrentAttribute(InstanceAttribute _currentAttribute) {
		this._currentAttribute = _currentAttribute;
	}

	public InstanceAttribute getCurrentAttribute() {
		return _currentAttribute;
	}

	public boolean process(InstanceDatabase database, MetaModelDatabase metamodel)
			throws SecurityException, IllegalArgumentException, ClassNotFoundException,
			NoSuchFieldException, IllegalAccessException,
			NoSuchMethodException, InvocationTargetException,
			InvalidLayerRequirement {
		
		setMetaModel(metamodel);
		if((getAttribute()!=null) && (getAttribute().getAttributeValue()!=null)) {
			for (InstanceAttribute ia : database.getAllAttributesOf(_entity.getCurrentEntity())) {
				if (ia.getMetaAttribute().getName().equals(getAttribute().getAttributeName())) {
					String attName = getAttribute().getAttributeName();
					String condition = getEntity().getTermProcessor().processTerm(getAttribute().getAttributeValue(), _rule);
					if (ia.getMetaAttribute().getName().equals(attName) && ia.getValue() != null) {
						if(getEntity().getTermProcessor().parseCondition(condition, ia.getValue().toString())){
							this.getFilterDatabase().getInstanceAttributes().add(ia);
							return true;
						}
					}
				}
			}
		}
		return false;		
	}

	public void setMetaModel(MetaModelDatabase _metaModel) {
		this._metaModel = _metaModel;
	}

	public MetaModelDatabase getMetaModel() {
		return _metaModel;
	}

	private void setEntity(ApplyEntityFilter _entity) {
		this._entity = _entity;
	}

	public ApplyEntityFilter getEntity() {
		return _entity;
	}

	public boolean setCurrentByEntity(InstanceEntity ie) {
		for(InstanceAttribute ia: this.getFilterDatabase().getInstanceAttributes()) {
			if(ia.getEntity().hashCode() == ie.hashCode()) {
				setCurrentAttribute(ia);
				return true;
			}
		}
		return false;
		// not found.. so create a new one..
//		for(MetaAttribute ma : getMetaModel().getAttributesFromEntity(ie.getMetaEntity()))
//		{
//			System.out.println("creating a new attribute for the filter: " + ma.toString());
//			if(ma.getName().compareTo(getAttribute().getAttributeName())== 0) {
//				setCurrentAttribute(new InstanceAttribute(ie,ma));
//			}
//		}
	}

	public boolean isAtomValue() {
		Term term = getAttribute().getAttributeValue();
		return term instanceof Atom;
	}

	public String getAtomValue() {
		Term term = getAttribute().getAttributeValue();
		return "'"+((Atom)term).getValue()+"'";
	}

	public String getName() {
		return this.getAttribute().getAttributeName();
	}

	public boolean correspondsTo(dsltrans.Attribute a) {
		return getAttribute() == a;
	}
}

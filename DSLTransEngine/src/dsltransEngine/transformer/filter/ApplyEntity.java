package dsltransEngine.transformer.filter;

import dsltrans.ApplyAttribute;
import dsltrans.ApplyClass;
import dsltransEngine.metamodel.DSLTransAttribute;
import dsltransEngine.metamodel.MetaAttribute;
import dsltransEngine.metamodel.MetaEntity;
import dsltransEngine.metamodel.MetaModelDatabase;
import dsltransEngine.model.InstanceAttribute;
import dsltransEngine.model.InstanceDatabase;
import dsltransEngine.model.InstanceDatabaseManager;
import dsltransEngine.model.InstanceEntity;
import dsltransEngine.transformer.TransformationRule;
import dsltransEngine.transformer.exceptions.InvalidLayerRequirement;

import java.lang.reflect.InvocationTargetException;

public class ApplyEntity extends ApplyEntityFilter {

	private boolean _pastEntity = false;
	
	public ApplyEntity(TransformationRule tr, ApplyClass mc, String id, InstanceDatabaseManager instanceDatabaseManager) {
		super(tr, mc, id, instanceDatabaseManager);
	}

	public boolean performApply(InstanceDatabase database,
			MetaModelDatabase metamodel, InstanceDatabase matchmodel) throws SecurityException, IllegalArgumentException, ClassNotFoundException, NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException, InvalidLayerRequirement {
		
		if(getCurrentEntity() == null) {
			MetaEntity me = metamodel.getMetaEntityByName(
					this.getApplyClass().getPackageName(),
					this.getApplyClass().getClassName()
			);

			if(isImported()) {
				this.getTransformationRule().processImports(this,database,metamodel,matchmodel);
			} else {
				InstanceEntity ie = database.createInstance(me);
				setCurrentEntity(ie);
				database.getInstanceEntities().add(ie);
				System.out.println(": "+"i"+Integer.toString(ie.hashCode()));
				{
					for(ApplyAttributeFilter aaf : getFilterAttributes()) {
						if(aaf.getName().equals(DSLTransAttribute.DSLTRANS_DEFAULT)) {
							InstanceAttribute ia = new InstanceAttribute(ie,metamodel.getMetaAttributeByName(DSLTransAttribute.DSLTRANS_DEFAULT));
							database.getInstanceAttributes().add(ia);
							aaf.setCurrentAttribute(ia);
							break; // only one per instance
						}
					}
				}
				
				// create default attributes
				for(MetaAttribute ma : metamodel.getMetaAttributesFromEntity(me)) {			
					database.getInstanceAttributes().add(new InstanceAttribute(ie,ma));
				}
				
				for(ApplyAttributeFilter aaf: this.getFilterAttributes()) {
					ApplyAttribute aa = aaf.getAttribute();
					for(InstanceAttribute ia : database.getAllAttributesOf(ie)) {
						if(ia.getMetaAttribute().getName().equals(aa.getAttributeName()))
							aaf.setCurrentAttribute(ia);
					}
				}
			}
		}
		return true;
	}

	public void setPastEntity(boolean _pastEntity) {
		this._pastEntity = _pastEntity;
	}

	public boolean isPastEntity() {
		return _pastEntity;
	}
}

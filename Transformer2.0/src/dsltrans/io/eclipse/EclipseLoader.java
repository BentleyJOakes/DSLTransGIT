package dsltrans.io.eclipse;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.impl.EClassImpl;
import org.eclipse.emf.ecore.impl.EClassifierImpl;
import org.eclipse.emf.ecore.impl.EModelElementImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;
import org.eclipse.emf.ecore.impl.EPackageImpl;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

import dsltrans.io.ModelLoader;
import dsltrans.metamodel.MetaAttribute;
import dsltrans.metamodel.MetaEntity;
import dsltrans.metamodel.MetaModelDatabase;
import dsltrans.metamodel.MetaRelation;
import dsltrans.model.InstanceAttribute;
import dsltrans.model.InstanceEntity;
import dsltrans.model.InstanceRelation;
import dsltrans.transformer.exceptions.UnsuportedMetamodelException;

public class EclipseLoader extends EclipseHandler implements ModelLoader {

	// meta information
	private MetaModelDatabase _metamodeldatabase;
	private Stack<String> _namespace;
	private EclipseMetaEntity _currentEntity;
	
	private final String classDir;
	
	// instance model
	private EclipseInstanceDatabase _database;

	public EclipseLoader(EclipseInstanceDatabaseManager instanceDatabaseManager, String classDir) {
		this.classDir = classDir;
		setMetaModelDatabase(new MetaModelDatabase());
		_namespace = new Stack<String>();
		_currentEntity = null;
		_database = (EclipseInstanceDatabase) instanceDatabaseManager.createInstanceDatabase();
	}

	public EclipseInstanceDatabase getInstanceDatabase() {
		return _database;
	}

	public void print() {
		System.out.println();
		{
			Iterator<MetaEntity> it = getMetaModelDatabase().getMetaEntities().iterator();
			System.out.println("printing meta entities: ");
			while (it.hasNext()) {
				MetaEntity me = it.next();
				System.out.println(me.print());
			}
		}
		System.out.println();
		{
			Iterator<MetaRelation> it = getMetaModelDatabase().getMetaRelations().iterator();
			System.out.println("printing meta relations: ");
			while (it.hasNext()) {
				MetaRelation me = it.next();
				System.out.println(me.print());
			}
		}
		System.out.println();
		{
			Iterator<MetaAttribute> it = getMetaModelDatabase().getMetaAttributes().iterator();
			System.out.println("printing meta attributes: ");
			while (it.hasNext()) {
				MetaAttribute me = it.next();
				System.out.println(me.print());
			}
		}
		System.out.println();
		{
			Iterator<InstanceEntity> it = getInstanceDatabase().getInstanceEntities().iterator();
			System.out.println("printing instance entities: ");
			while (it.hasNext()) {
				InstanceEntity me = it.next();
				System.out.println(me.print());
			}
		}
		System.out.println();
		{
			Iterator<InstanceRelation> it = getInstanceDatabase().getInstanceRelations().iterator();
			System.out.println("printing instance relations: ");
			while (it.hasNext()) {
				InstanceRelation me = it.next();
				System.out.println(me.print());
			}
		}
		System.out.println();
		{
			Iterator<InstanceAttribute> it = getInstanceDatabase().getInstanceAttributes().iterator();
			System.out.println("printing instance attributes: ");
			while (it.hasNext()) {
				InstanceAttribute me = it.next();
				System.out.println(me.print());
			}
		}
	}
	
	@Override
	public void loadInstances(String metamodelName, String url) throws ClassNotFoundException, SecurityException, NoSuchFieldException, IllegalArgumentException,
			IllegalAccessException, NoSuchMethodException, InvocationTargetException, UnsuportedMetamodelException {
		// debug
		System.out.println("LOADING database");
		
		if (metamodelName.equals("ecore.Ecore"))
			metamodelName = "org.eclipse.emf.ecore.Ecore";
		String classname = metamodelName + "Package";
		String factoryname = metamodelName + "Factory";

		ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(Resource.Factory.Registry.DEFAULT_EXTENSION, new XMIResourceFactoryImpl());

		URL[] urlPath = {};
		List<URL> urlList = new LinkedList<URL>();
		try {
			urlList.add(new File(classDir + "/tempClasses").toURI().toURL());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		urlPath = urlList.toArray(urlPath);

		System.out.println("loading classname: " + classname);
		// DEBUG
		// for(String fact : getDatabase().getFactorys().keySet()) {
		// System.out.println("Factory: "+fact);
		// }
		Map<String, Object> knownFactories = getInstanceDatabase().getFactorys();
		if (!knownFactories.containsKey(classname)) {
			this.getClass().getClassLoader().clearAssertionStatus();
			ClassLoader customLoader = new URLClassLoader(urlPath, this.getClass().getClassLoader());
			{
				Class<?> cc = Class.forName(classname, false, customLoader);
				Field f1 = cc.getField("eNS_URI");
				f1.getType().cast(f1.get(cc));
				Field f2 = cc.getField("eINSTANCE");
				f2.getType().cast(f2.get(cc));
				Object factory = f2.get(cc);
				resourceSet.getPackageRegistry().put((String) f1.get(cc), factory);
				knownFactories.put(classname, factory);
			}
			{
				Class<?> cc = Class.forName(factoryname, true, customLoader);
				Field f2 = cc.getField("eINSTANCE");
				Object factory = (Object) f2.get(cc);
				knownFactories.put(factoryname, factory);
			}
		} else {
			Object factory = knownFactories.get(classname);
			resourceSet.getPackageRegistry().put(classname, factory);
		}
		
		URI URIurl = URI.createURI(url);
		Resource resource = null;
		if (URIurl.isRelative())
			resource = resourceSet.getResource(URI.createFileURI(classDir + "/" + url), true);
		else
			resource = resourceSet.getResource(URI.createFileURI(url), true);
		// Resource resource =
		// resourceSet.getResource(URI.createFileURI(classpath+"/"+url),true);
		// EList<EObject> list = resource.getContents();

		TreeIterator<EObject> titer = resource.getAllContents();
		while (titer.hasNext()) { // process class instances and containment
									// relations
			EObject obj = (EObject) titer.next();
			if (obj instanceof EObjectImpl) {
				process((EObjectImpl) obj);
			} else if (obj instanceof EModelElementImpl) {
				processEModel((EModelElementImpl) obj);
			}
		}
		{// process relations
			Iterator<InstanceEntity> iter = getInstanceDatabase().getInstanceEntities().iterator();
			while (iter.hasNext()) {
				EclipseInstanceEntity me = (EclipseInstanceEntity) iter.next();
				processNonContainments(me);
			}
		}

		processRootElement();

		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().clear();
		resourceSet.getPackageRegistry().clear();

	}

	private InstanceEntity processEModel(EModelElementImpl objinstance) throws SecurityException, IllegalArgumentException, NoSuchMethodException, IllegalAccessException, InvocationTargetException,
			ClassNotFoundException {

		{ // we do not want to process the same class two times
			Iterator<InstanceEntity> it = getInstanceDatabase().getInstanceEntities().iterator();
			while (it.hasNext()) {
				EclipseInstanceEntity me = (EclipseInstanceEntity) it.next();
				if (me.getObjectMeta() == objinstance)
					return me;
			}
		}
		{ // ok now we can proceed
			Iterator<MetaEntity> it = getMetaModelDatabase().getMetaEntities().iterator();
			while (it.hasNext()) {
				MetaEntity me = it.next();
				if (objinstance.getClass().getCanonicalName().equals("org.eclipse.emf." + me.getNamespace() + ".impl." + me.getName() + "Impl")) {
					EclipseInstanceEntity instanceEntity = new EclipseInstanceEntity(objinstance, me);
					getInstanceDatabase().getInstanceEntities().add(instanceEntity);
					processModelAttributes(instanceEntity);
					processModelContainments(instanceEntity);
					return instanceEntity;
				}
			}
		}
		return null;
	}

	private void processModelContainments(EclipseInstanceEntity instanceEntity) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException,
			ClassNotFoundException {
		MetaEntity me = instanceEntity.getMetaEntity();
		EModelElementImpl obj = instanceEntity.getObjectMeta();
		Iterator<MetaRelation> iter = getMetaModelDatabase().getMetaRelations().iterator();
		while (iter.hasNext()) {
			MetaRelation ma = iter.next();
			if (ma.isContainment() && me.isSubTypeOf(ma.getSource()) && !instanceEntity.hasLoadedRelation(ma)) {
				String name = ma.getName();
				String first = name.substring(0, 1);
				String remainder = name.substring(1, name.length());
				Method method = obj.getClass().getMethod("get" + first.toUpperCase() + remainder + (((first.toUpperCase() + remainder).equals("Class")) ? "_" : ""));
				Object value = method.invoke(obj);

				if (value == null)
					continue; // no further processing here..
				InstanceEntity ie = null;
				if (ma.isSet()) {
					Class<?> elistclass = Class.forName("org.eclipse.emf.common.util.EList");
					EList<?> list = (EList<?>) elistclass.cast(value);
					Iterator<?> iterator = list.iterator();
					while (iterator.hasNext()) {
						Object objx = iterator.next();
						if (objx instanceof EModelElementImpl) {
							EModelElementImpl objimpl = (EModelElementImpl) objx;
							ie = processEModel(objimpl);
							if (ie != null) {
								InstanceRelation ir = new InstanceRelation(instanceEntity, ma, ie);
								instanceEntity.addRelation(ma);
								getInstanceDatabase().getInstanceRelations().add(ir);
							}
						}
					}
				} else if (value instanceof EModelElementImpl) {
					ie = processEModel((EModelElementImpl) value);
					if (ie != null) {
						InstanceRelation ir = new InstanceRelation(instanceEntity, ma, ie);
						instanceEntity.addRelation(ma);
						getInstanceDatabase().getInstanceRelations().add(ir);
					}
				}
			}
		}
	}

	private void processModelAttributes(EclipseInstanceEntity instanceEntity) throws SecurityException, NoSuchMethodException, ClassNotFoundException, IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		MetaEntity me = instanceEntity.getMetaEntity();

		EModelElementImpl obj = instanceEntity.getObjectMeta();
		Iterator<MetaAttribute> iter = getMetaModelDatabase().getMetaAttributes().iterator();
		while (iter.hasNext()) {
			MetaAttribute ma = iter.next();
			if (ma.isInheritedFrom(me) && !instanceEntity.hasLoadedAttribute(ma)) {
				String type = ma.getType();
				String name = ma.getName();
				String first = name.substring(0, 1);
				String remainder = name.substring(1, name.length());
				Method method = null;
				if (type == null || type.compareTo("boolean") != 0) {
					method = obj.getClass().getMethod("get" + first.toUpperCase() + remainder + (((first.toUpperCase() + remainder).equals("Class")) ? "_" : ""));
				} else {
					method = obj.getClass().getMethod("is" + first.toUpperCase() + remainder);
				}
				Object value = method.invoke(obj);
				if (value != null) {
					InstanceAttribute ia = new InstanceAttribute(instanceEntity, ma, value);
					instanceEntity.addAttribute(ma);
					getInstanceDatabase().getInstanceAttributes().add(ia);
				}
			}
		}
	}

	public void processRootElement() throws UnsuportedMetamodelException {
		MetaEntity rootEntity = this.getMetaModelDatabase().getRootMetaEntity();
		List<InstanceEntity> ielist = this.getInstanceDatabase().getAllInstancesOf(rootEntity);

		this.getInstanceDatabase().setRootElement(ielist.get(0));

	}

	private void processNonContainments(EclipseInstanceEntity instanceEntity) throws SecurityException, NoSuchMethodException, ClassNotFoundException, IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		if (instanceEntity.getObject() != null) {
			processRegularNonContainments(instanceEntity);
			return;
		}
		if (instanceEntity.getObjectMeta() != null) {
			processMetaNonContainments(instanceEntity);
			return;
		}
	}

	private void processMetaNonContainments(EclipseInstanceEntity instanceEntity) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException,
			InvocationTargetException, ClassNotFoundException {
		MetaEntity me = instanceEntity.getMetaEntity();
		EModelElementImpl obj = instanceEntity.getObjectMeta();
		Iterator<MetaRelation> iter = getMetaModelDatabase().getMetaRelations().iterator();
		while (iter.hasNext()) {
			MetaRelation ma = iter.next();
			if (!ma.isContainment() && me.isSubTypeOf(ma.getSource()) && !instanceEntity.hasLoadedRelation(ma)) {
				String name = ma.getName();
				String first = name.substring(0, 1);
				String remainder = name.substring(1, name.length());
				Method method = obj.getClass().getMethod("get" + first.toUpperCase() + remainder + (((first.toUpperCase() + remainder).equals("Class")) ? "_" : ""));
				Object value = method.invoke(obj);

				if (value == null)
					continue; // no further processing here..
				InstanceEntity ie = null;
				if (ma.isSet()) {
					Class<?> elistclass = Class.forName("org.eclipse.emf.common.util.EList");
					EList<?> list = (EList<?>) elistclass.cast(value);
					Iterator<?> iterator = list.iterator();
					while (iterator.hasNext()) {
						Object objx = iterator.next();
						if (objx instanceof EModelElementImpl) {
							EModelElementImpl objimpl = (EModelElementImpl) objx;
							ie = getInstanceEntity(objimpl);
							if (ie != null) {
								InstanceRelation ir = new InstanceRelation(instanceEntity, ma, ie);
								instanceEntity.addRelation(ma);
								getInstanceDatabase().getInstanceRelations().add(ir);
							}
						}
					}
				} else if (value instanceof EModelElementImpl) {
					ie = getInstanceEntity((EModelElementImpl) value);
					if (ie != null) {
						InstanceRelation ir = new InstanceRelation(instanceEntity, ma, ie);
						instanceEntity.addRelation(ma);
						getInstanceDatabase().getInstanceRelations().add(ir);
					}
				}
			}
		}
	}

	private void processRegularNonContainments(EclipseInstanceEntity instanceEntity) throws SecurityException, NoSuchMethodException, ClassNotFoundException, IllegalArgumentException,
			IllegalAccessException, InvocationTargetException {

		MetaEntity me = instanceEntity.getMetaEntity();
		EObjectImpl obj = instanceEntity.getObject();
		Iterator<MetaRelation> iter = getMetaModelDatabase().getMetaRelations().iterator();
		while (iter.hasNext()) {
			MetaRelation ma = iter.next();
			if (!ma.isContainment() && me.isSubTypeOf(ma.getSource()) && !instanceEntity.hasLoadedRelation(ma)) {
				String name = ma.getName();
				String first = name.substring(0, 1);
				String remainder = name.substring(1, name.length());
				Method method = obj.getClass().getMethod("get" + first.toUpperCase() + remainder + (((first.toUpperCase() + remainder).equals("Class")) ? "_" : ""));
				Object value = method.invoke(obj);

				if (value == null)
					continue; // no further processing here..
				InstanceEntity ie = null;
				if (ma.isSet()) {
					Class<?> elistclass = Class.forName("org.eclipse.emf.common.util.EList");
					EList<?> list = (EList<?>) elistclass.cast(value);
					Iterator<?> iterator = list.iterator();
					while (iterator.hasNext()) {
						EObjectImpl objimpl = (EObjectImpl) iterator.next();
						ie = getInstanceEntity(objimpl);
						if (ie != null) {
							InstanceRelation ir = new InstanceRelation(instanceEntity, ma, ie);
							instanceEntity.addRelation(ma);
							getInstanceDatabase().getInstanceRelations().add(ir);
						}
					}
				} else {
					ie = getInstanceEntity((EObjectImpl) value);
					if (ie != null) {
						InstanceRelation ir = new InstanceRelation(instanceEntity, ma, ie);
						instanceEntity.addRelation(ma);
						getInstanceDatabase().getInstanceRelations().add(ir);
					}
				}
			}
		}
	}

	private InstanceEntity getInstanceEntity(EModelElementImpl value) {
		Iterator<InstanceEntity> it = getInstanceDatabase().getInstanceEntities().iterator();
		while (it.hasNext()) {
			EclipseInstanceEntity me = (EclipseInstanceEntity) it.next();
			if (me.getObjectMeta() == value)
				return me;
		}
		return null;
	}

	private InstanceEntity getInstanceEntity(EObjectImpl value) {
		Iterator<InstanceEntity> it = getInstanceDatabase().getInstanceEntities().iterator();
		while (it.hasNext()) {
			EclipseInstanceEntity me = (EclipseInstanceEntity) it.next();
			if (me.getObject() == value)
				return me;
		}
		return null;
	}

	private void processContainments(EclipseInstanceEntity instanceEntity) throws SecurityException, NoSuchMethodException, ClassNotFoundException, IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {

		MetaEntity me = instanceEntity.getMetaEntity();
		EObjectImpl obj = instanceEntity.getObject();
		Iterator<MetaRelation> iter = getMetaModelDatabase().getMetaRelations().iterator();
		while (iter.hasNext()) {
			MetaRelation ma = iter.next();
			if (ma.isContainment() && me.isSubTypeOf(ma.getSource()) && !instanceEntity.hasLoadedRelation(ma)) {
				String name = ma.getName();
				String first = name.substring(0, 1);
				String remainder = name.substring(1, name.length());
				Method method = obj.getClass().getMethod("get" + first.toUpperCase() + remainder + (((first.toUpperCase() + remainder).equals("Class")) ? "_" : ""));
				Object value = method.invoke(obj);

				if (value == null)
					continue; // no further processing here..
				InstanceEntity ie = null;
				if (ma.isSet()) {
					Class<?> elistclass = Class.forName("org.eclipse.emf.common.util.EList");
					EList<?> list = (EList<?>) elistclass.cast(value);
					Iterator<?> iterator = list.iterator();
					while (iterator.hasNext()) {
						EObjectImpl objimpl = (EObjectImpl) iterator.next();
						ie = process(objimpl);
						if (ie != null) {
							InstanceRelation ir = new InstanceRelation(instanceEntity, ma, ie);
							instanceEntity.addRelation(ma);
							getInstanceDatabase().getInstanceRelations().add(ir);
						}
					}
				} else {
					ie = process((EObjectImpl) value);
					if (ie != null) {
						InstanceRelation ir = new InstanceRelation(instanceEntity, ma, ie);
						instanceEntity.addRelation(ma);
						getInstanceDatabase().getInstanceRelations().add(ir);
					}
				}
			}
		}
	}

	private InstanceEntity process(EObjectImpl objinstance) throws SecurityException, NoSuchMethodException, ClassNotFoundException, IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {

		{ // we do not want to process the same class two times
			Iterator<InstanceEntity> it = getInstanceDatabase().getInstanceEntities().iterator();
			while (it.hasNext()) {
				EclipseInstanceEntity me = (EclipseInstanceEntity) it.next();
				if (me.getObject() == objinstance)
					return me;
			}
		}
		{ // ok now we can proceed
			Iterator<MetaEntity> it = getMetaModelDatabase().getMetaEntities().iterator();
			while (it.hasNext()) {
				EclipseMetaEntity me = (EclipseMetaEntity) it.next();
				if (me.isMetaTypeOf(objinstance)) {
					EclipseInstanceEntity instanceEntity = new EclipseInstanceEntity(objinstance, me);
					getInstanceDatabase().getInstanceEntities().add(instanceEntity);
					processAttributes(instanceEntity);
					processContainments(instanceEntity);
					return instanceEntity;
				}
			}
		}
		return null;
	}

	private void processAttributes(EclipseInstanceEntity instanceEntity) throws SecurityException, NoSuchMethodException, ClassNotFoundException, IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		MetaEntity me = instanceEntity.getMetaEntity();

		EObjectImpl obj = instanceEntity.getObject();
		Iterator<MetaAttribute> iter = getMetaModelDatabase().getMetaAttributes().iterator();
		while (iter.hasNext()) {
			MetaAttribute ma = iter.next();
			if (ma.isInheritedFrom(me) && !instanceEntity.hasLoadedAttribute(ma)) {
				String type = ma.getType();
				String name = ma.getName();
				String first = name.substring(0, 1);
				String remainder = name.substring(1, name.length());
				Method method = null;
				if (type == null || type.compareTo("boolean") != 0) {
					method = obj.getClass().getMethod("get" + first.toUpperCase() + remainder + (((first.toUpperCase() + remainder).equals("Class")) ? "_" : ""));
				} else {
					method = obj.getClass().getMethod("is" + first.toUpperCase() + remainder);
				}
				Object value = method.invoke(obj);
				if (value != null) {
					InstanceAttribute ia = new InstanceAttribute(instanceEntity, ma, value);
					instanceEntity.addAttribute(ma);
					getInstanceDatabase().getInstanceAttributes().add(ia);
				}
			}
		}
	}

	public void loadMetaModel(String path) throws IOException {
		System.out.println("Loading metamodel...");
		
		System.out.println("classdir: " + classDir);
		System.out.println("path: " + path);
		
		ResourceSet resourceSet = new ResourceSetImpl();

		// Register the appropriate resource factory to handle all file
		// extensions.
		//
		// Register the Ecore resource Factory
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore", new EcoreResourceFactoryImpl());
		
		URI metamodelURI = createAbsoluteHierarchicalURI(classDir, path);
		
		Resource resource = resourceSet.getResource(metamodelURI, true);
		EList<EObject> list = resource.getContents();

		if (list.get(0) instanceof EPackageImpl)
			metaProcess((EPackageImpl) list.get(0));

		metaProcessForeignClasses();
		{// process relations
			Iterator<MetaEntity> iter = getMetaModelDatabase().getMetaEntities().iterator();
			while (iter.hasNext()) {
				EclipseMetaEntity me = (EclipseMetaEntity) iter.next();
				metaRelationProcess(me);
			}
		}
		{// process class hierarchy
			Iterator<MetaEntity> iter = getMetaModelDatabase().getMetaEntities().iterator();
			while (iter.hasNext()) {
				EclipseMetaEntity me = (EclipseMetaEntity) iter.next();
				metaSuperProcess(me);
			}
		}

		resourceSet.getPackageRegistry().clear();
		
		System.out.println("Loading metamodel... DONE");
		
		generateMetaModelClasses(classDir, path);
	}

	public URI tryToFindModel(URI uri) {
		return uri;
	}

	private void metaProcessForeignClasses() {
		List<EPackage> packs = new LinkedList<EPackage>();
		Iterator<MetaEntity> iter = getMetaModelDatabase().getMetaEntities().iterator();
		while (iter.hasNext()) {
			EclipseMetaEntity me = (EclipseMetaEntity) iter.next();
			getPacks(me, packs);
		}
		for (EPackage pack : packs)
			metaProcess((EPackageImpl) pack);
	}

	private void getPacks(EclipseMetaEntity me, List<EPackage> packs) {
		EList<EReference> containments = me.getObject().getEAllReferences();
		Iterator<EReference> iter = containments.iterator();
		while (iter.hasNext()) {
			EReference c = iter.next();
			Iterator<MetaEntity> classiter = getMetaModelDatabase().getMetaEntities().iterator();
			boolean found = false;
			while (classiter.hasNext()) {
				EclipseMetaEntity mnext = (EclipseMetaEntity) classiter.next();
				if (mnext.getObject() == c.getEType()) {
					found = true;
				}
			}
			if (!found && c.getEType() instanceof EClassImpl) {
				if (!packs.contains(((EClassifierImpl) c.getEType()).basicGetEPackage()))
					packs.add(((EClassifierImpl) c.getEType()).basicGetEPackage());
			}
		}
	}

	private void metaRelationProcess(EclipseMetaEntity me) {
		EList<EReference> containments = me.getObject().getEAllReferences();
		Iterator<EReference> iter = containments.iterator();
		while (iter.hasNext()) {
			EReference c = iter.next();
			Iterator<MetaEntity> classiter = getMetaModelDatabase().getMetaEntities().iterator();
			@SuppressWarnings("unused")
			boolean found = false;
			while (classiter.hasNext()) {
				EclipseMetaEntity mnext = (EclipseMetaEntity) classiter.next();
				if (mnext.getObject() == c.getEType()) {
					found = true;
					EclipseMetaRelation mr = new EclipseMetaRelation(me, c, mnext);
					getMetaModelDatabase().getMetaRelations().add(mr);
				}
			}
		}
	}

	private void metaSuperProcess(EclipseMetaEntity me) {
		EList<EClass> superlist = me.getObject().getEAllSuperTypes();
		Iterator<EClass> iter = superlist.iterator();
		while (iter.hasNext()) {
			EClass c = iter.next();
			Iterator<MetaEntity> classiter = getMetaModelDatabase().getMetaEntities().iterator();
			while (classiter.hasNext()) {
				EclipseMetaEntity mnext = (EclipseMetaEntity) classiter.next();
				if (mnext.getObject() == c)
					me.addSuperEntity(mnext);
			}
		}
	}

	private void metaProcess(EPackageImpl obj) {
		EPackageImpl pack = (EPackageImpl) obj;

		_namespace.push(pack.getName());
		EList<EObject> list = obj.eContents();
		Iterator<EObject> iterator = list.iterator();
		while (iterator.hasNext()) {
			EObject objnext = iterator.next();
			if (objnext instanceof EPackageImpl)
				metaProcess((EPackageImpl) objnext);
			else if (objnext instanceof EClassImpl)
				metaProcess((EClassImpl) objnext, getMetaModelDatabase().getMetaEntities());
		}
		_namespace.pop();

		// debug
		// System.out.println("package: "+pack.getName());
	}

	private void metaProcess(EClassImpl obj, List<MetaEntity> classes) {
		EclipseMetaEntity tempEntity = new EclipseMetaEntity(obj, _namespace.lastElement(), serialize(_namespace));

		for (MetaEntity me : classes) {
			if (me.getNamespace().equals(tempEntity.getNamespace()) && me.getName().equals(tempEntity.getName()))
				return;
		}
		_currentEntity = tempEntity;
		classes.add(_currentEntity);

		EList<EAttribute> attrlist = obj.getEAttributes();
		Iterator<EAttribute> iterator = attrlist.iterator();
		while (iterator.hasNext())
			metaProcess(iterator.next());

	}

	private void metaProcess(EAttribute obj) {
		MetaAttribute a = new EclipseMetaAttribute(_currentEntity, obj);
		getMetaModelDatabase().getMetaAttributes().add(a);
	}

	private String serialize(Stack<String> x) {
		Iterator<String> iterator = x.iterator();
		String result = "";
		while (iterator.hasNext()) {
			result += iterator.next();
			if (iterator.hasNext())
				result += ".";
		}
		return result;
	}

	public void setMetaModelDatabase(MetaModelDatabase _metamodeldatabase) {
		this._metamodeldatabase = _metamodeldatabase;
	}

	public MetaModelDatabase getMetaModelDatabase() {
		return _metamodeldatabase;
	}
	
	private void prepareDatabase(MetaEntity me) throws ClassNotFoundException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, MalformedURLException {
		this.getInstanceDatabase().synchFactories();
		
		String packageName = me.getPackage().substring(1);
		packageName = Character.toUpperCase(me.getPackage().charAt(0)) + packageName;
		String className = me.getNamespace()+"."+packageName+"Package";
		
		URL[] urlPath = {};
		List<URL> urlList = new LinkedList<URL>();
		urlList.add(new File(classDir+"/tempClasses").toURI().toURL());
		urlPath = urlList.toArray(urlPath);

		URLClassLoader customLoader = new URLClassLoader(urlPath,this.getClass().getClassLoader());	
		Map<String, Object> knownFactories = ((EclipseInstanceDatabase)this.getInstanceDatabase()).getFactorys();
		if(!knownFactories.containsKey(className)) {
			Object factory = null;
			Class<?> cc = Class.forName(className,false,customLoader);
			Field f2 = cc.getField("eINSTANCE");
			factory = (Object)f2.get(cc);
			knownFactories.put(className, factory);
			
			String factoryName = me.getNamespace()+"."+packageName+"Factory";
			if (!knownFactories.containsKey(factoryName)) {
				Object factory1 = null;
				Class<?> cc1 = Class.forName(factoryName,false,customLoader);
				Field f21 = cc1.getField("eINSTANCE");
				factory1 = (Object)f21.get(cc1);
				knownFactories.put(factoryName, factory1);
			}
		}
		
	}
	
	@Override
	public void prepareInstanceDatabase() throws ClassNotFoundException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, MalformedURLException {
		for (MetaEntity me : this.getMetaModelDatabase().getMetaEntities()) {	
			this.prepareDatabase(me);
		}
	}


	
}

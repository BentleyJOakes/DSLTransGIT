package dsltransEngine.tests;

import dsltrans.*;
import dsltrans.impl.DsltransFactoryImpl;
import dsltransEngine.metamodel.MetaEntity;
import dsltransEngine.transformer.TransformerProcessor;
import org.junit.Assert;
import org.junit.Test;

public class Test_Generic_IO_API {

	@Test
	public void test() throws Throwable {
		
		TransformationModel sampleTransformation = buildSampleTransformation();
		GenericPersistenceLayer persistence = new GenericPersistenceLayer();
		TransformerProcessor tP = new TransformerProcessor(persistence, 
				new GenericInstanceDatabaseManager());
		
		tP.LoadModel(sampleTransformation);
		tP.Execute();
		
		MetaEntity classA = persistence.outputMetamodel.getMetaEntityByName("samplenamespace", "ClassA");
		Assert.assertTrue(persistence.outputModel.getAllInstancesOf(classA).size()==1);
	}

	public TransformationModel buildSampleTransformation(){
		DsltransFactory factory = new DsltransFactoryImpl();
		
		TransformationModel transformation = factory.createTransformationModel();
		
		MetaModelIdentifier input_mm_id = factory.createMetaModelIdentifier();
		input_mm_id.setMetaModelName("Input Metamodel");
		input_mm_id.setMetaModelURI("Input/Metamodel/URI");
		
		MetaModelIdentifier output_mm_id = factory.createMetaModelIdentifier();
		output_mm_id.setMetaModelName("Output Metamodel");
		output_mm_id.setMetaModelURI("Output/Metamodel/URI");
		
		FilePort inputFilePort = factory.createFilePort();
		inputFilePort.setFilePathURI("Input/Model/URI");
		inputFilePort.setMetaModelId(input_mm_id);
		inputFilePort.setName("inputModel");
		
		Sequential firstLayer = factory.createSequential();
		firstLayer.setDescription("first layer");
		firstLayer.setName("firstLayer");
		firstLayer.setMetaModelId(output_mm_id);
		firstLayer.setOutputFilePathURI("Output/Model/URI");
		
		firstLayer.getPreviousSource().add(inputFilePort);
		
		transformation.getSource().add(inputFilePort);
		transformation.getSource().add(firstLayer);
		
		Rule rule = factory.createRule();
		rule.setDescription("First Rule");
		
		MatchModel matchModel = factory.createMatchModel();
		
		AnyMatchClass matchA = factory.createAnyMatchClass();
		matchA.setClassName("ClassA");
		matchA.setPackageName("samplenamespace");
		
		matchModel.getClass_().add(matchA);
		
		rule.getMatch().add(matchModel);
		
		ApplyModel applyModel = factory.createApplyModel();
		
		ApplyClass applyClass = factory.createApplyClass();
		
		applyClass.setClassName("ClassA");
		applyClass.setPackageName("samplenamespace");
		
		applyModel.getClass_().add(applyClass);
		
		rule.setApply(applyModel);
		
		firstLayer.getHasRule().add(rule);
		
		return transformation;
	}
}

package dsltransEngine.transformer.exceptions;

import dsltransEngine.transformer.TransformationLayer;

public class TransformationLayerException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public TransformationLayerException(String string,
			TransformationLayer transformationLayer, Exception e) {
		super(string);
		_message = string;
		_source = transformationLayer;
		_originException = e;
	}
	private String _message;
	private TransformationLayer _source;
	private Exception _originException;
	
	@Override
	public void printStackTrace() {
		System.err.println("Exception: " + _message + " from: " + _source.toString());
		_originException.printStackTrace();
	}
	
}

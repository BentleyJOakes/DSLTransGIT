package dsltransEngine.transformer.exceptions;

public class InvalidAttributeRelationException extends Exception {

	
	private static final long serialVersionUID = 1L;
	
	private dsltrans.AbstractAttributeRelation _relation;
	
	public InvalidAttributeRelationException(String string, dsltrans.AbstractAttributeRelation relation) {
		super(string);
		_relation = relation;
	}

	public dsltrans.AbstractAttributeRelation getRelation() {
		return _relation;
	}
	
	
}

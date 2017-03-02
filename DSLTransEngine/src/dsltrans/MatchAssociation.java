/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package dsltrans;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Match Association</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link dsltrans.MatchAssociation#getSource <em>Source</em>}</li>
 *   <li>{@link dsltrans.MatchAssociation#getTarget <em>Target</em>}</li>
 * </ul>
 * </p>
 *
 * @see dsltrans.DsltransPackage#getMatchAssociation()
 * @model abstract="true"
 *        annotation="gmf.link source='source' target='target' target.decoration='arrow' width='2'"
 * @generated
 */
public interface MatchAssociation extends Association {
	/**
	 * Returns the value of the '<em><b>Source</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Source</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Source</em>' reference.
	 * @see #setSource(MatchClass)
	 * @see dsltrans.DsltransPackage#getMatchAssociation_Source()
	 * @model required="true"
	 * @generated
	 */
	MatchClass getSource();

	/**
	 * Sets the value of the '{@link dsltrans.MatchAssociation#getSource <em>Source</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Source</em>' reference.
	 * @see #getSource()
	 * @generated
	 */
	void setSource(MatchClass value);

	/**
	 * Returns the value of the '<em><b>Target</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Target</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Target</em>' reference.
	 * @see #setTarget(MatchClass)
	 * @see dsltrans.DsltransPackage#getMatchAssociation_Target()
	 * @model required="true"
	 * @generated
	 */
	MatchClass getTarget();

	/**
	 * Sets the value of the '{@link dsltrans.MatchAssociation#getTarget <em>Target</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Target</em>' reference.
	 * @see #getTarget()
	 * @generated
	 */
	void setTarget(MatchClass value);

} // MatchAssociation

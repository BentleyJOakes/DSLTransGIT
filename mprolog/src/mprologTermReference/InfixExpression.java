/**
 * <copyright>
 * </copyright>
 *
 * $Id: InfixExpression.java,v 1.1 2011/12/28 01:45:32 bfb Exp $
 */
package mprologTermReference;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Infix Expression</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link mprologTermReference.InfixExpression#getLeftTerm <em>Left Term</em>}</li>
 *   <li>{@link mprologTermReference.InfixExpression#getRightTerm <em>Right Term</em>}</li>
 *   <li>{@link mprologTermReference.InfixExpression#getOwnedOperator <em>Owned Operator</em>}</li>
 * </ul>
 * </p>
 *
 * @see mprologTermReference.MprologTermReferencePackage#getInfixExpression()
 * @model
 * @generated
 */
public interface InfixExpression extends Term {
	/**
	 * Returns the value of the '<em><b>Left Term</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Left Term</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Left Term</em>' containment reference.
	 * @see #setLeftTerm(Term)
	 * @see mprologTermReference.MprologTermReferencePackage#getInfixExpression_LeftTerm()
	 * @model containment="true" required="true"
	 * @generated
	 */
	Term getLeftTerm();

	/**
	 * Sets the value of the '{@link mprologTermReference.InfixExpression#getLeftTerm <em>Left Term</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Left Term</em>' containment reference.
	 * @see #getLeftTerm()
	 * @generated
	 */
	void setLeftTerm(Term value);

	/**
	 * Returns the value of the '<em><b>Right Term</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Right Term</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Right Term</em>' containment reference.
	 * @see #setRightTerm(Term)
	 * @see mprologTermReference.MprologTermReferencePackage#getInfixExpression_RightTerm()
	 * @model containment="true" required="true"
	 * @generated
	 */
	Term getRightTerm();

	/**
	 * Sets the value of the '{@link mprologTermReference.InfixExpression#getRightTerm <em>Right Term</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Right Term</em>' containment reference.
	 * @see #getRightTerm()
	 * @generated
	 */
	void setRightTerm(Term value);

	/**
	 * Returns the value of the '<em><b>Owned Operator</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Owned Operator</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Owned Operator</em>' containment reference.
	 * @see #setOwnedOperator(Operator)
	 * @see mprologTermReference.MprologTermReferencePackage#getInfixExpression_OwnedOperator()
	 * @model containment="true" required="true"
	 * @generated
	 */
	Operator getOwnedOperator();

	/**
	 * Sets the value of the '{@link mprologTermReference.InfixExpression#getOwnedOperator <em>Owned Operator</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Owned Operator</em>' containment reference.
	 * @see #getOwnedOperator()
	 * @generated
	 */
	void setOwnedOperator(Operator value);

} // InfixExpression

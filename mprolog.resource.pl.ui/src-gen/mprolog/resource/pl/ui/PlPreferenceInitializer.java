/**
 * <copyright>
 * </copyright>
 *
 * 
 */
package mprolog.resource.pl.ui;

/**
 * A class used to initialize default preference values.
 */
public class PlPreferenceInitializer extends org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer {
	
	private final static mprolog.resource.pl.ui.PlAntlrTokenHelper tokenHelper = new mprolog.resource.pl.ui.PlAntlrTokenHelper();
	
	public void initializeDefaultPreferences() {
		
		initializeDefaultSyntaxHighlighting();
		initializeDefaultBrackets();
		
		org.eclipse.jface.preference.IPreferenceStore store = mprolog.resource.pl.ui.PlUIPlugin.getDefault().getPreferenceStore();
		// Set default value for matching brackets
		store.setDefault(mprolog.resource.pl.ui.PlPreferenceConstants.EDITOR_MATCHING_BRACKETS_COLOR, "192,192,192");
		store.setDefault(mprolog.resource.pl.ui.PlPreferenceConstants.EDITOR_MATCHING_BRACKETS_CHECKBOX, true);
		
	}
	
	private void initializeDefaultBrackets() {
		org.eclipse.jface.preference.IPreferenceStore store = mprolog.resource.pl.ui.PlUIPlugin.getDefault().getPreferenceStore();
		initializeDefaultBrackets(store, new mprolog.resource.pl.mopp.PlMetaInformation());
	}
	
	public void initializeDefaultSyntaxHighlighting() {
		org.eclipse.jface.preference.IPreferenceStore store = mprolog.resource.pl.ui.PlUIPlugin.getDefault().getPreferenceStore();
		initializeDefaultSyntaxHighlighting(store, new mprolog.resource.pl.mopp.PlMetaInformation());
	}
	
	private void initializeDefaultBrackets(org.eclipse.jface.preference.IPreferenceStore store, mprolog.resource.pl.IPlMetaInformation metaInformation) {
		String languageId = metaInformation.getSyntaxName();
		// set default brackets for ITextResource bracket set
		mprolog.resource.pl.ui.PlBracketSet bracketSet = new mprolog.resource.pl.ui.PlBracketSet(null, null);
		final java.util.Collection<mprolog.resource.pl.IPlBracketPair> bracketPairs = metaInformation.getBracketPairs();
		if (bracketPairs != null) {
			for (mprolog.resource.pl.IPlBracketPair bracketPair : bracketPairs) {
				bracketSet.addBracketPair(bracketPair.getOpeningBracket(), bracketPair.getClosingBracket(), bracketPair.isClosingEnabledInside());
			}
		}
		store.setDefault(languageId + mprolog.resource.pl.ui.PlPreferenceConstants.EDITOR_BRACKETS_SUFFIX, bracketSet.getBracketString());
	}
	
	private void initializeDefaultSyntaxHighlighting(org.eclipse.jface.preference.IPreferenceStore store, mprolog.resource.pl.IPlMetaInformation metaInformation) {
		String languageId = metaInformation.getSyntaxName();
		String[] tokenNames = metaInformation.getTokenNames();
		if (tokenNames == null) {
			return;
		}
		for (int i = 0; i < tokenNames.length; i++) {
			if (!tokenHelper.canBeUsedForSyntaxHighlighting(i)) {
				continue;
			}
			
			String tokenName = tokenHelper.getTokenName(tokenNames, i);
			if (tokenName == null) {
				continue;
			}
			mprolog.resource.pl.IPlTokenStyle style = metaInformation.getDefaultTokenStyle(tokenName);
			if (style != null) {
				String color = getColorString(style.getColorAsRGB());
				setProperties(store, languageId, tokenName, color, style.isBold(), true, style.isItalic(), style.isStrikethrough(), style.isUnderline());
			} else {
				setProperties(store, languageId, tokenName, "0,0,0", false, false, false, false, false);
			}
		}
	}
	
	private void setProperties(org.eclipse.jface.preference.IPreferenceStore store, String languageID, String tokenName, String color, boolean bold, boolean enable, boolean italic, boolean strikethrough, boolean underline) {
		store.setDefault(mprolog.resource.pl.ui.PlSyntaxColoringHelper.getPreferenceKey(languageID, tokenName, mprolog.resource.pl.ui.PlSyntaxColoringHelper.StyleProperty.BOLD), bold);
		store.setDefault(mprolog.resource.pl.ui.PlSyntaxColoringHelper.getPreferenceKey(languageID, tokenName, mprolog.resource.pl.ui.PlSyntaxColoringHelper.StyleProperty.COLOR), color);
		store.setDefault(mprolog.resource.pl.ui.PlSyntaxColoringHelper.getPreferenceKey(languageID, tokenName, mprolog.resource.pl.ui.PlSyntaxColoringHelper.StyleProperty.ENABLE), enable);
		store.setDefault(mprolog.resource.pl.ui.PlSyntaxColoringHelper.getPreferenceKey(languageID, tokenName, mprolog.resource.pl.ui.PlSyntaxColoringHelper.StyleProperty.ITALIC), italic);
		store.setDefault(mprolog.resource.pl.ui.PlSyntaxColoringHelper.getPreferenceKey(languageID, tokenName, mprolog.resource.pl.ui.PlSyntaxColoringHelper.StyleProperty.STRIKETHROUGH), strikethrough);
		store.setDefault(mprolog.resource.pl.ui.PlSyntaxColoringHelper.getPreferenceKey(languageID, tokenName, mprolog.resource.pl.ui.PlSyntaxColoringHelper.StyleProperty.UNDERLINE), underline);
	}
	
	private String getColorString(int[] colorAsRGB) {
		if (colorAsRGB == null) {
			return "0,0,0";
		}
		if (colorAsRGB.length != 3) {
			return "0,0,0";
		}
		return colorAsRGB[0] + "," +colorAsRGB[1] + ","+ colorAsRGB[2];
	}
}

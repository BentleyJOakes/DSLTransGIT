package dsltransEngine.transformer;

import dsltransEngine.transformer.filter.*;

import java.util.HashMap;
import java.util.List;


public class TermProcessor {
	
	public static final String SEQUENCER_STRING = "#seq";
	
	public HashMap<dsltrans.Term, String> processedTerms;
	private List<MatchFilter> _matchFilterList;

	public TermProcessor(List<MatchFilter> mflist) {
		processedTerms = new HashMap<dsltrans.Term,String>();
		setMatchFilterList(mflist);
	}
	
	public void Clear(){ processedTerms.clear(); }
	
	public boolean hasTerm(dsltrans.Term attValue) {
		return processedTerms.containsKey(attValue);
	}
	
	// decode Attribute Terms
	public String processTerm(dsltrans.Term t, dsltrans.Rule r){
		String result = new String();
		if (t instanceof dsltrans.Atom) {
			result += ((dsltrans.Atom)t).getValue();
		} else if (t instanceof dsltrans.Sequencer) {
			// Add here the other base case considering the sequencer term.
			result += SEQUENCER_STRING;
		} else if (t instanceof dsltrans.Operator) {
			if (t instanceof dsltrans.Concat) {
				dsltrans.Concat cc = (dsltrans.Concat)t;
				result += processTerm(cc.getLeftTerm(), r) + processTerm(cc.getRightTerm(), r);
			}
			else if (t instanceof dsltrans.TypeOf) {
				dsltrans.TypeOf to = (dsltrans.TypeOf)t;
				dsltrans.Attribute at = to.getAttributeRef();
				if (at instanceof dsltrans.MatchAttribute) {
					dsltrans.MatchAttribute ma = (dsltrans.MatchAttribute)at;
					dsltrans.MatchClass mc = (dsltrans.MatchClass)ma.eContainer();
					AbstractFilter af = null;
					for (MatchFilter mf : getMatchFilterList()){
						af = mf.getFilter(mc);
						if (af != null)
							break;
					}
					MatchEntityFilter mef = (MatchEntityFilter)af;
					for (MatchAttributeFilter mf : mef.getFilterAttributes())
						if (mf.getAttribute().equals(ma)) {
							result += mf.getCurrentAttribute().getMetaAttribute().getType();
							break;
						}
				}
			}
		}
		else if (t instanceof dsltrans.Ref) {
			if (t instanceof dsltrans.AttributeRef) {
				dsltrans.Attribute at = ((dsltrans.AttributeRef)t).getAttributeRef();
				if (at instanceof dsltrans.MatchAttribute) {
					dsltrans.MatchAttribute ma = (dsltrans.MatchAttribute)at;
					dsltrans.MatchClass mc = (dsltrans.MatchClass)ma.eContainer();
					AbstractFilter af = null;
					for (MatchFilter mf : getMatchFilterList()){
						af = mf.getFilter(mc);
						if (af != null)
							break;
					}
					if (af != null) {
						MatchEntityFilter mef = (MatchEntityFilter)af;
						for (MatchAttributeFilter mf : mef.getFilterAttributes())
							if (mf.getAttribute().equals(ma)) {
								result += (mf.getCurrentAttribute().getValue() == null? "" : mf.getCurrentAttribute().getValue());
								break;
							}
					}
				}
				else if (at instanceof dsltrans.ApplyAttribute){
					dsltrans.ApplyClass backClass = (dsltrans.ApplyClass)at.eContainer();
					if (r.getBackwards().contains(backClass)) {
						for (MatchFilter mf : getMatchFilterList()) {
							if (r.getMatch().contains(mf.get_matchModel())) {
								for (ApplyEntityFilter aef : mf.getApplyEntityFilters()) {
									if (aef.getApplyClass() == backClass) {
										for (ApplyAttributeFilter aaf : aef.getFilterAttributes()) {
											if (aaf.getAttribute().getAttributeName() == at.getAttributeName()) {
												result += aaf.getCurrentAttribute().getValue().toString();
											}
										}
									}
								}
							}
						}
					}
					else {
						dsltrans.Term value = ((dsltrans.ApplyAttribute) at).getAttributeValue();
						if (processedTerms.containsKey(value))
							result += processedTerms.get(value);
						else
							result += processTerm(value, r);
					}
				}
			}
			else if (t instanceof dsltrans.ClassRef) {
				dsltrans.ClassRef cr = (dsltrans.ClassRef)t;
				result += cr.getClassRef().getClassName();
			}
				
		}
		else if (t instanceof dsltrans.Wildcard) {
			result += '*';
		}
		
		processedTerms.put(t, result);
		return result;
	}
	
	public boolean parseCondition(String condition, String attValue) {
		boolean result = true;
		if (condition.contains("*")) {
			while (!condition.isEmpty()) {
				String subcondition = condition.substring(0, condition.indexOf('*'));
				if(subcondition.isEmpty()) {
					condition = condition.substring(condition.indexOf('*')+1, condition.length());
					result &= attValue.endsWith(condition);
					break;
				} else if (condition.startsWith(subcondition)) {
					result &= attValue.startsWith(subcondition);
					if(result) {
						attValue = attValue.substring(attValue.indexOf(subcondition)+subcondition.length(), attValue.length());
						condition = condition.substring(condition.indexOf('*')+1, condition.length());
					} else {
						result = false;
						break;						
					}
				} else {
					result = false;
					break;
				}
			}
		} else {
			result = attValue.equals(condition);
		}
		
		return result;
	}

	public String getTerm(dsltrans.Term attValue) {
		return processedTerms.get(attValue);
	}

	private void setMatchFilterList(List<MatchFilter> _matchFilterList) {
		this._matchFilterList = _matchFilterList;
	}

	private List<MatchFilter> getMatchFilterList() {
		return _matchFilterList;
	}	
	
}
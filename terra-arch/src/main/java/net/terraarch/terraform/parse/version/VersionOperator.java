package net.terraarch.terraform.parse.version;

import java.io.Serializable;
import java.util.Arrays;

public enum VersionOperator implements Serializable {

	EQUAL("==", (cv, cs, iv, is) -> Arrays.equals(cv, iv) && cs.equals(is)),
	NOT_EQUAL("!=", (cv, cs, iv, is)-> !Arrays.equals(cv, iv) || !cs.equals(is)),
	GREATER(">", (cv, cs, iv, is)-> cs.length()==0 && is.length()==0 && VersionConstraint.comp.compare(iv, cv)>0),
	GREATER_OR_EQUAL(">=", (cv, cs, iv, is)-> cs.length()==0 && is.length()==0 && VersionConstraint.comp.compare(iv, cv)>=0),
	LESSTHAN("<", (cv, cs, iv, is)->  cs.length()==0 && is.length()==0 && VersionConstraint.comp.compare(iv, cv)<0),
	LESSTHAN_OR_EQUALS("<=", (cv, cs, iv, is)-> cs.length()==0 && is.length()==0 && VersionConstraint.comp.compare(iv, cv)<=0),
	SMART_RANGE("~>", (cv, cs, iv, is)-> {
		return cs.length()==0 && is.length()==0 
				 && VersionConstraint.comp.compare(iv, cv)>=0
	             && VersionConstraint.comp.compare(VersionConstraint.nextSignificant(iv), cv)<0;
	});
	
	public final String text;
	private final VersionOperatorBehavior vbo;
	
	
	
	
	VersionOperator(String text, VersionOperatorBehavior vbo) {
		this.text = text;
		this.vbo = vbo;
	}
	
	boolean isValid(int[] constraintVer, String constraintSuff, int[] input, String inputSuff) {
		if (null!=constraintSuff && null!=inputSuff) {
			return vbo.isValid(constraintVer, constraintSuff, input, inputSuff);
		} else {
			throw new NullPointerException("empty suffix must be empty quote never null");
		}
	}
		
	
}

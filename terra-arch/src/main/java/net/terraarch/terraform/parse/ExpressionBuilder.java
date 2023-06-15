package net.terraarch.terraform.parse;

import net.terraarch.terraform.parse.ParseState.NAMESPACES;
import net.terraarch.util.AppendableBuilderReader;

public class ExpressionBuilder {

	protected byte[] lastAssignmentArgumentName;
	protected int lastAssignmentArgumentPosition;
	

	public void setAssignmentArgument(byte[] lastAssignmentArgumentName, int lastAssignmentArgumentPosition) {
		this.lastAssignmentArgumentName = lastAssignmentArgumentName;
		this.lastAssignmentArgumentPosition = lastAssignmentArgumentPosition;
	}
	
	public void textBegin(int filePosition, byte[] quotes) {
	}
	public void appendTextByte(int b) {
	}
	public AppendableBuilderReader textEnd(int endPos, final AppendableBuilderReader reader) {
		return reader;
	}
	public void iterateOpen(int filePosition, int size) {
	}
	public void iterElse() {
	}
	public void iterEndIf() {
	}
	public void iterEndFor() {
	}
	public void iterIf() {
	}
	
	
	
	public void parenClose(int filePosition) {
	}
	public void parenOpen(int filePosition) {
	}
		
	
	public String pushOperation(int filePosition, String op) {
		return op;
	}
	
	
	public String interpolateOpen(int filePosition, int size, String text) {
		return text;
	}
	public void interpolateClose(int filePosition) {
	}
	
	
	public int arrayOpen(int filePosition, int depth) {
		return depth;
	}
	public int arrayClose(int filePosition, int depth) {
		return depth;
	}
	public void arrayItem(int filePosition) {
	}
	
	
	public void numberBegin(int filePosition) {
	}
	public void pushNumber(int filePosition, long m, int e) {
	}
	public void pushNumber(int filePosition, long i) {
	}
	public void pushNumberSciNotation(int filePosition, String text, long i) {
	}
	public String numberComplete(int filePosition, String numberText) {
		return numberText;
	}
	
	public void functionEnd(int filePosition) {
	}
	public void functionEndWithArrayAsArgs(int filePosition) {
	}
	public void functionPushParam(int filePosition) {
	}
	public AppendableBuilderReader functionOpen(int filePosition, AppendableBuilderReader name) {
		return name;
	}
	
	public int forStart(int filePosition, int value) {
		return value;
	}
	protected void collectForArg(int depth, int endPos, AppendableBuilderReader reader) {
	}
	public void forComma(int filePosition) {
	}
	public void forIn(int filePosition) {
	}
	public int forEnd(int filePosition, int depth) {
		return depth;
	}
	
	public void reference(NAMESPACES nameSpace, BlockType type, int nameParts, int endPos2,
			AppendableBuilderReader reader2, int endPos1, AppendableBuilderReader reader1) {
	}
	///////////////////////////////////////////////////
	
    public void clear() {
	}

	public TFExpression getExpressionRoot() {
		return null;
	}
	
	
}

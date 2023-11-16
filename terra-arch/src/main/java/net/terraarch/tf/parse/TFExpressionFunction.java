package net.terraarch.terraform.parse;

import java.util.Arrays;

public class TFExpressionFunction extends TFExpression {

	private final int startPos;
	private final byte[] name;
	private TFExpression active;
	private boolean arrayArgs = false;
	
	public TFExpressionFunction(TFExpression parent, int position, byte[] name) {
		super(parent);
		this.startPos = position;
		this.name = name;		
		active = new TFExpression(this);
	}

	public void add(TFExpression item) {
		active.add(item);
	}

	public void pushParam(int filePosition) {
		super.add(active);
		active = new TFExpression(this);
	}

	public void finish(int filePosition) {
		super.add(active);
		active = null;
	}

	public void finishAsArrayArgs(int filePosition) {
		super.add(active);
		active = null;
		arrayArgs = true;
	}
	

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TFExpressionFunction) {
			TFExpressionFunction that = (TFExpressionFunction)obj;
			return Arrays.equals(this.name, that.name) && this.arrayArgs==that.arrayArgs && super.equals(obj);
		} else {
			return false;
		}
	}

}

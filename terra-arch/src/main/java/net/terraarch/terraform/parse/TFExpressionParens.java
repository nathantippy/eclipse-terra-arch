package net.terraarch.terraform.parse;

public class TFExpressionParens extends TFExpression {

	
	private int startPos;
	private int stopPos;
	
	public TFExpressionParens(TFExpression parent, int position) {
		super(parent);
		this.startPos = position;
	}

	public void closePos(int position) {
		this.stopPos = position;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TFExpressionParens) {
			return super.equals(obj);
		} else {
			return false;
		}
	}
	
}

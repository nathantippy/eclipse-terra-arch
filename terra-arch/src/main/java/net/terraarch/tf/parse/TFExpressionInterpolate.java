package net.terraarch.tf.parse;

public class TFExpressionInterpolate extends TFExpression {

	private final int position;
	private final int typeSize;
	private int closePosition;
	
	public TFExpressionInterpolate(TFExpression parent, int position, int typeSize) {
		super(parent);
		this.position = position;
		this.typeSize = typeSize;
	}

	public void closePos(int position) {
		this.closePosition = position;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TFExpressionInterpolate) {
			TFExpressionInterpolate that = (TFExpressionInterpolate)obj;
			return this.typeSize==that.typeSize && super.equals(obj);
		} else {
			return false;
		}
	}
	
	
}

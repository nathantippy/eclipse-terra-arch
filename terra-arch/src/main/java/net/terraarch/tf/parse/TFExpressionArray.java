package net.terraarch.tf.parse;

public class TFExpressionArray extends TFExpression {

	private TFExpression active;
	
	public TFExpressionArray(TFExpression parent) {
		super(parent);
		active = new TFExpression(this);		
	}

	public void add(TFExpression item) {
		active.add(item);
	}
	
	public void arrayItem(int filePosition) {
		super.add(active);
		active = new TFExpression(this);
	}

	public void finish() {
		super.add(active);
		active = null;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TFExpressionArray) {
			return super.equals(obj);
		} else {
			return false;
		}
	}
	
	
}

package net.terraarch.terraform.parse;

public class TFExpressionIterator extends TFExpression {

	static class TFExpressionIteratorFlag extends TFExpressionChild {

		private final byte[] flag; //warning these must be followed but not used for equals.
		
		public TFExpressionIteratorFlag(TFExpression parent, byte[] flag) {
			super(parent);
			this.flag = flag;
		}
		
	}
	
	private final int position;
	private final int typeSize;
	private int closePosition;
	private TFExpression active;
	
	public TFExpressionIterator(TFExpression parent, int position, int size) {
		super(parent);
		this.position = position;
		this.typeSize = size;		
		this.active = new TFExpression(this);	
	}

	public void closePos(int position) {
		this.closePosition = position;
		super.add(active);
		active = null;
	}

	public void iterElse() {
		super.add(active);
		super.add(new TFExpressionIteratorFlag(this,"else".getBytes()));
		active = new TFExpression(this);
	}

	public void iterEndIf() {
		super.add(active);
		super.add(new TFExpressionIteratorFlag(this,"elseif".getBytes()));
		active = new TFExpression(this);
	}

	public void iterEndFor() {
		super.add(active);
		super.add(new TFExpressionIteratorFlag(this,"endfor".getBytes()));
		active = new TFExpression(this);
	}

	public void iterIf() {
		super.add(active);
		super.add(new TFExpressionIteratorFlag(this,"if".getBytes()));
		active = new TFExpression(this);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TFExpressionIterator) {
			TFExpressionIterator that = (TFExpressionIterator)obj;
			return this.typeSize==that.typeSize && super.equals(obj);
		} else {
			return false;
		}
	}
	
}

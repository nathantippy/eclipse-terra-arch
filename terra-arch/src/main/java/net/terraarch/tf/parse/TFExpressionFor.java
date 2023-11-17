package net.terraarch.tf.parse;

public class TFExpressionFor extends TFExpression {

	static class TFExpressionForArg extends TFExpressionChild {

		private final byte[] arg; //warning these must be followed but not used for equals.
		private final int pos;
		
		public TFExpressionForArg(TFExpression parent, byte[] arg, int pos) {
			super(parent);
			this.arg = arg;
			this.pos = pos;
		}
		
		//not used for equals because the usages of these found in ref is where equals is checked.
		@Override
		public boolean equals(Object obj) {//all are equal if they are the same type
			return (obj instanceof TFExpressionForArg);
		}
	}
	
	static class TFExpressionForComma extends TFExpressionChild {

		private final int pos;
		
		public TFExpressionForComma(TFExpression parent, int pos) {
			super(parent);
			this.pos = pos;
		}
		
		//not used for equals because the usages of these found in ref is where equals is checked.
		@Override
		public boolean equals(Object obj) {//all are equal if they are the same type
			return (obj instanceof TFExpressionForComma);
		}
	}

	static class TFExpressionForIn extends TFExpressionChild {

		private final int pos;
		
		public TFExpressionForIn(TFExpression parent, int pos) {
			super(parent);
			this.pos = pos;
		}
		
		//not used for equals because the usages of these found in ref is where equals is checked.
		@Override
		public boolean equals(Object obj) {//all are equal if they are the same type
			return (obj instanceof TFExpressionForIn);
		}
	}
	
	private TFExpression active;
	private final int filePosition;
	private final int value;
	
	public TFExpressionFor(TFExpression parent, int filePosition, int value) {
		super(parent);
		this.active = new TFExpression(this);	
		this.filePosition = filePosition;
		this.value = value;		
	}	
	
	public void add(TFExpression item) {
		active.add(item);
	}
	
	public void collectForArg(int depth, int endPos, byte[] bytes) {
		super.add(active);
		super.add(new TFExpressionForArg(this, bytes, endPos));
		active = new TFExpression(this);
	}

	public void forComma(int filePosition) {
		super.add(active);
		super.add(new TFExpressionForComma(this, filePosition));
		active = new TFExpression(this);
	}

	public void forIn(int filePosition) {
		super.add(active);
		super.add(new TFExpressionForIn(this, filePosition));
		active = new TFExpression(this);
	}

	public void forEnd(int filePosition) {
		super.add(active);
		active = null;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TFExpressionFor) {
			return super.equals(obj);
		} else {
			return false;
		}
	}
	
}

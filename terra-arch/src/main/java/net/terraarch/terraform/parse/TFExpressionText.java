package net.terraarch.terraform.parse;

import java.util.Arrays;

import net.terraarch.util.AppendableBuilder;
import net.terraarch.util.AppendableBuilderReader;

public class TFExpressionText extends TFExpression {

	static class TFExpressionTextChild extends TFExpressionChild {

		private byte[] active;
		
		public TFExpressionTextChild(TFExpression parent, AppendableBuilderReader active) {
			super(parent);
			this.active = active.toBytes();
		}

		@Override
		public int hashCode() {
			final int prime = 131;
			int result = 1;
			result = prime * result + ((active == null) ? 0 : active.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			TFExpressionTextChild other = (TFExpressionTextChild) obj;
			if (active == null) {
				if (other.active != null)
					return false;
			} else if (!Arrays.equals(this.active, other.active))
				return false;
			return true;
		}
				
	}
	
	
	AppendableBuilder active = new AppendableBuilder();
	
	private final int startPos;
	private  int endPos;
	private final byte[] quotes;
	
	public TFExpressionText(TFExpression parent, int pos, byte[] quotes) {
		super(parent);
		this.startPos = pos;
		this.quotes = quotes;
	}

	//store bytes with other children between
		
	
	public void add(TFExpression item) {
		if (active.byteLength()>0) {
			super.add(new TFExpressionTextChild(this, active.reader()));
			active = new AppendableBuilder();
		}
		super.add(item);
	}
	
	public void appendTextByte(int b) {
		active.writeByte(b);
	}

	public void appendText(byte[] bytes) {
		active.write(bytes);
	}	
	public void finish(int endPos) {
		super.add(new TFExpressionTextChild(this,active.reader()));
		this.endPos = endPos;
		active = null;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TFExpressionText) {
			return super.equals(obj);
		} else {
			return false;
		}
	}
	
}

package net.terraarch.tf.parse;

public class TFExpressionOperator extends TFExpressionChild {

	private final int position;
	private final String op;
	
	public TFExpressionOperator(TFExpression parent, int position, String op) {
		super(parent);
		this.position = position;
		this.op = op;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((op == null) ? 0 : op.hashCode());
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
		TFExpressionOperator other = (TFExpressionOperator) obj;
		if (op == null) {
			if (other.op != null)
				return false;
		} else if (!op.equals(other.op))
			return false;
		return true;
	}

	


}

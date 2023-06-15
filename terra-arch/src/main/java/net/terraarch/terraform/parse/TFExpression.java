package net.terraarch.terraform.parse;

import java.util.ArrayList;
import java.util.List;

public class TFExpression extends TFExpressionChild {

	
	public TFExpression(TFExpression parent) {
		super(parent);
	}
	
	private final List<TFExpressionChild> children = new ArrayList<>();
	
	public void add(TFExpressionChild item) {
		children.add(item);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((children == null) ? 0 : children.hashCode());
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
		TFExpression other = (TFExpression) obj;
		if (children == null) {
			if (other.children != null)
				return false;
		} else if (!children.equals(other.children))
			return false;
		return true;
	}
	
	
	
}

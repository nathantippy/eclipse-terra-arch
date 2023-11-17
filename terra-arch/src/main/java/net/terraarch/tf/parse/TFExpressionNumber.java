package net.terraarch.tf.parse;

public class TFExpressionNumber extends TFExpression {

	private long m;
	private int e;
	private int numPos;
	private String sci;
	private long   sciE;
	private int sciPos;
	private String fullText; //used for equals, TODO: AA convert to text child like text block??
	private int endPos;
		
	public TFExpressionNumber(TFExpression parent, int position) {
		super(parent);
	}


	public void setValue(long m, int e, int filePosition) {
		this.m = m;
		this.e = e;
		this.numPos = filePosition;
	}

	public void setSci(String sci, long e, int filePosition) {
		this.sci = sci;
		this.sciE = e;
		this.sciPos = filePosition;
	}

	public void finish(String fullText, int filePosition) {
		this.fullText = fullText;
		this.endPos = filePosition;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fullText == null) ? 0 : fullText.hashCode());
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
		TFExpressionNumber other = (TFExpressionNumber) obj;
		if (fullText == null) {
			if (other.fullText != null)
				return false;
		} else if (!fullText.equals(other.fullText))
			return false;
		return true;
	}


	
	
}

package net.terraarch.tf.parse.version;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;

import net.terraarch.util.Appendables;

public class VersionConstraint implements Serializable {

	private VersionOperator operator;
	private int[] version;
	private String tag="";//must never be null 
	
	
    public final boolean hasTag() {
    	return tag.length()>0;
    }
	
	public final static Comparator<int[]> comp = new Comparator<int[]>() {

		@Override
		public int compare(int[] o1, int[] o2) {
			
			int i = 0;
			while (i<o1.length && i<o2.length) {
				if (o1[i]<o2[i]) {
					return -1;
				}
				if (o1[i]>o2[i]) {
					return 1;
				}
				i++;
			}
			if (o1.length < o2.length) {
				return -1;
			}
			if (o1.length > o2.length) {
				return 1;
			}
			
			return 0;
		}
	};
	
	public final static int[] nextSignificant(int[] value) {
		
		//TODO: we can make a smarter comparitor and avoid this GC,  future work.
		if (value.length>=2) {
			
			int[] result = Arrays.copyOfRange(value, 0, value.length-1);
			result[result.length-1]++;
			return result;			
			
		} else {
			return value; //cant compute, this is not defined
		}
		
	}

	public VersionConstraint(int activeOperator, int[] version, String tag) {
		this.operator = VersionOperator.values()[activeOperator];
		this.version = version;
		if (null==tag) {
			throw new NullPointerException();
		}
		this.tag = tag;
	}
	
	public VersionConstraint(int activeOperator, int[] version) {
		this(activeOperator,version,"");
	}

	public boolean isValid(int[] version, String suffix) {
		return operator.isValid(this.version, this.tag, version, suffix);
	}
	
	public boolean isValid(VersionDTO dto) {
		return operator.isValid(this.version, this.tag, dto.digits, dto.label);
	}
	
	public String toString() {
		return toString(new StringBuilder()).toString();
	}
	
	 public <T extends Appendable> T toString(T target) {
		 	
		    try {
		    	
				target.append(operator.text).append(' ');
				Appendables.appendArray(target, '[', version, ']');
				target.append(' ');
				target.append(tag.trim());
				
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		    
		 	return target;
	 }
	 
	 
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + operator.ordinal();
		result = prime * result + ((tag == null) ? 0 : tag.hashCode());
		result = prime * result + Arrays.hashCode(version);
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
		VersionConstraint other = (VersionConstraint) obj;
		if (operator != other.operator)
			return false;
		if (tag == null) {
			if (other.tag != null)
				return false;
		} else if (!tag.equals(other.tag))
			return false;
		if (!Arrays.equals(version, other.version))
			return false;
		return true;
	}

}

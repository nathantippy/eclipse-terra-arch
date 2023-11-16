package net.terraarch.terraform.parse.version;

import java.util.Arrays;

import net.terraarch.pipe.util.hash.MurmurHash;
import net.terraarch.util.Appendables;

public class VersionDTO implements Comparable<VersionDTO> {

	public final int[] digits;
	public final String label;
	public String textCache;
	
	public static final VersionDTO NONE = new VersionDTO(new int[0], "");
	
	public VersionDTO(int[] digits, String label) {
		this.digits = digits;
		this.label = label;
	}
	
	public String toString() {
		if (textCache==null) {
			StringBuilder builder = new StringBuilder();
			
			for(int d: digits) {
				if (builder.length()>0) {
					builder.append('.');
				}
				Appendables.appendValue(builder, d);
			}
			if (label!=null && label.length()>0) {
				builder.append('-');
				builder.append(label);				
			}
			textCache = builder.toString();
		}
		return textCache;
		
	}

	public static int computeVersionHash(String[] keySet) {
		int hash = 123456;
		for(String key:keySet) {							
			hash = hash^MurmurHash.hash32(key, 313);
		}
		return hash;
	}

	public boolean isGreater(VersionDTO that) {
		return VersionOperator.GREATER.isValid(digits, label, that.digits, that.label);
	}

	@Override
	public int compareTo(VersionDTO that) {
		int comp = VersionConstraint.comp.compare(this.digits, that.digits);
		if (0==comp) {
			comp = label.compareTo(that.label);
		}
		return comp;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(digits);
		result = prime * result + ((label == null) ? 0 : label.hashCode());
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
		VersionDTO other = (VersionDTO) obj;
		if (!Arrays.equals(digits, other.digits))
			return false;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		return true;
	}
	
}

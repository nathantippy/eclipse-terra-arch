package net.terraarch.struct;

public interface StructFloatListener {

	void value(float value, boolean isNull, int[] position, int[] size, int instance, int totalCount);
	
}

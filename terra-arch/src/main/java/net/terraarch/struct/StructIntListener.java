package net.terraarch.struct;

public interface StructIntListener {

	void value(int value, boolean isNull, int[] position, int[] size, int instance, int totalCount);
	
}

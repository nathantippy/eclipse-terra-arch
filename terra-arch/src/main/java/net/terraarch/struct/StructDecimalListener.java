package net.terraarch.struct;

public interface StructDecimalListener {

	void value(byte e, long m, boolean isNull, int[] position, int[] size, int instance, int totalCount);
	
}

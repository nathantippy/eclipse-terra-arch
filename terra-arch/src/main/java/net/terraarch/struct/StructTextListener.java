package net.terraarch.struct;

import net.terraarch.pipe.TextReader;

public interface StructTextListener {

	void value(TextReader reader, boolean isNull, int[] position, int[] size, int instance, int totalCount);
	
}

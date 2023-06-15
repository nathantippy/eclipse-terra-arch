package net.terraarch.struct;

import net.terraarch.pipe.ChannelReader;

public interface StructBlobListener {

	void value(ChannelReader reader, int[] position, int[] size, int instance, int totalCount);
	
}

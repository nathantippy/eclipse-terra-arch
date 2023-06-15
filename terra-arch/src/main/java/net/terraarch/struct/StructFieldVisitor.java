package net.terraarch.struct;

import net.terraarch.pipe.ChannelReader;

public interface StructFieldVisitor<T> {
	
	public void read(T value, ChannelReader reader, long fieldId);
	
}

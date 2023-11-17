package net.terraarch;

import net.terraarch.tf.structure.StructureDataFile;

public class ProviderFileVersionDTO implements Comparable<ProviderFileVersionDTO>{

	public final StructureDataFile sdr;
	public final long count;
	
	public ProviderFileVersionDTO(StructureDataFile sdr, long count) {
		this.sdr = sdr;
		this.count = count;
	}

	@Override
	public int compareTo(ProviderFileVersionDTO that) {
		return Long.compare(that.count, count);
	}
	
}

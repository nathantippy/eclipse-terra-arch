package net.terraarch.tf.structure;

import net.terraarch.tf.parse.version.VersionDTO;

public class ProviderRecord {

	public final VersionDTO selectedVersion;
	public final Provider provider;
	
	public ProviderRecord(VersionDTO selectedVersion, Provider prov) {
		this.selectedVersion = selectedVersion;
		this.provider = prov;
	}

}

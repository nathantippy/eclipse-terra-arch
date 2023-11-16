package net.terraarch.terraform.structure;

import net.terraarch.terraform.parse.version.VersionDTO;

public class ProviderRecord {

	public final VersionDTO selectedVersion;
	public final Provider provider;
	
	public ProviderRecord(VersionDTO selectedVersion, Provider prov) {
		this.selectedVersion = selectedVersion;
		this.provider = prov;
	}

}

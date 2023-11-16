package net.terraarch.terraform.parse.doc;

import java.util.ArrayList;
import java.util.List;

import net.terraarch.terraform.parse.version.VersionConstraint;
import net.terraarch.terraform.parse.version.VersionDTO;
import net.terraarch.terraform.structure.ModuleFileIndexParse;
import net.terraarch.terraform.structure.StorageCache;

public class ProviderConstraintImpl {

	final StorageCache storageCache;
	
	public ProviderConstraintImpl(StorageCache storageCache) {	
		this.storageCache = storageCache;
	}
	

	
	private final List<ProviderDTO> providers = new ArrayList<ProviderDTO>();
	

	
	
	public StorageCache storageCache() {
		return storageCache;
	}
		
	public void setPoviderConstraintsLocal(DocumentMap that,
			String localAlias, final String sourceNamespace, final String sourceName, final List<VersionConstraint> constraints) {
		//NOTE: this is getting called on every parse and must be quick.
				
		final String key = sourceNamespace+":"+sourceName;
		String id;
		if ("hashicorp".equals(sourceNamespace)) {
			id = sourceName;
		} else {
			id = key;			
		}				
		boolean addProvider = selectVersion(that, sourceName, constraints, key, id, null);				
		if (addProvider) {		
			//these are collected for addition later.
			providers.add(new ProviderDTO(sourceNamespace, sourceName, constraints));
		}
					
	}
	
	private boolean selectVersion(DocumentMap that, final String sourceName, final List<VersionConstraint> constraints,
			final String key, String id, VersionDTO[] lookupVersionDTOs) {
		boolean addProvider = false;
		if (null != lookupVersionDTOs) {
			
			VersionDTO selectedVersion = selectBestVersion(constraints, lookupVersionDTOs);
	
			if (null==selectedVersion) {
				that.providerVersion.put(key, VersionDTO.NONE);
				that.providerVersion.put(id, VersionDTO.NONE);
				//this will trigger both versions and data files later and in order
				//can we do this by the caller upon boolen response..
				addProvider = true;
			} else {
				that.providerVersion.put(key, selectedVersion);
				that.providerVersion.put(id, selectedVersion);
								
				//TODO: AA need a new background task to down load this binary data....
				//TODO: AA we have a selected version but has it been downloaded?
			}
				
			that.definedProviders.setUTF8Value(sourceName+"_", 1);
		}
		return addProvider;
	}

	static VersionDTO selectBestVersion(List<VersionConstraint> constraints, VersionDTO[] versions) {
		VersionDTO selectedVersion = null;
		int v = versions.length;
		while (--v >= 0) {
			boolean isValid = true;
			VersionDTO possible = versions[v];
			int c = constraints.size();
			while (--c >= 0) {
				isValid |= constraints.get(c).isValid(possible);
			}
			if (isValid) {
				//keep only the newest possible.
				if (null==selectedVersion) {
					selectedVersion = possible;
				} else {
					if (selectedVersion.isGreater(possible)) {
						selectedVersion = possible;
					}						
				}
			}
		}
		return selectedVersion;
	}
	
	
}



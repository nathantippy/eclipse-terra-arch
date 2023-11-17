package net.terraarch.tf.parse;

import java.util.List;

public class ModuleDetails {

	public List<AuthorDetails> authorDetails;
	public long mostRecentUpdate;
	public String SHA;
	public String cannonicalLocation;
	public long newDbId = -1;
	public long oldDbId = -1;
	public String localPath;
	
}

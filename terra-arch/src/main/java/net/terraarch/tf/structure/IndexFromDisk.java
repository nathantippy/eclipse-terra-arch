package net.terraarch.tf.structure;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import net.terraarch.util.AppendableBuilder;


public class IndexFromDisk {

	static StructureDataFile reloadFromDisk(StructureDataModule module, File f, long lastMod, StructureDataFile sdc, StorageCache storageCache) {
		AppendableBuilder data = new AppendableBuilder();
		try {
			FileInputStream input = new FileInputStream(f);
			data.consumeAll(input);
			input.close();
			sdc = module.parseAndIndex(f, lastMod, data.toBytes(), true, sdc, storageCache);
		} catch (IOException e) {
			e.printStackTrace();
			sdc.isClean(false);
		}	
		return sdc;
	}

	public static StructureDataFile indexFromDisk(StructureDataModule module, File f, StorageCache storageCache) {
		
		StructureDataFile sdr = module.getSDR(f);
		long lastMod = -1;
		if ( (sdr.getLastModified()<=0) || (lastMod=f.lastModified() ) >= sdr.getLastModified()   ) {
			  //the disk is newer 
			sdr.clearBlocksKnownInThisFile();				
			return reloadFromDisk(module, f, lastMod, sdr, storageCache);
			
		} else {
			//the disk is older than what we have in memory
			//nothing needs to be done	
			return sdr;
		}
	}

	static File[] peers(StructureDataModule module) {
		
		return module.moduleFolder.listFiles((d,n)-> {
			return (n.endsWith(".tf") || n.endsWith(".tfvars") || n.endsWith(".tpl")) 
					//do not include the override file since it does not contain children details
					//this will be loaded as needed when the module source is processed, not here
					&& !n.endsWith("override.tf"); 
		});
	}

	public static void indexModule(StructureDataModule module, StorageCache storageCache) {
		File[] peers = peers(module);
		for(File f : peers) {
			try {
				if (null!=f) {
					indexFromDisk(module, f.getAbsoluteFile(), storageCache);	
				}
			} catch (Throwable t) {
				module.getSDR(f).isClean(false);//flag this file as unclean if we had a parse issue
				t.printStackTrace();
			}
		}
	}

}

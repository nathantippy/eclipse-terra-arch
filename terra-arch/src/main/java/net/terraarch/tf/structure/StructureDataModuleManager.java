package net.terraarch.tf.structure;

import java.io.File;

import net.terraarch.tf.parse.ParseBuffer;
import net.terraarch.util.TrieParser;


public class StructureDataModuleManager {


	///////////////////////////////////////////////////////////////////
	//These modules index positions are never modified once set
	//modules using other modules will reference these index valuees 
	///////////////////////////////////////////////////////////////////
	private long moduleCount = 0;
	private StructureDataModule[] modules = new StructureDataModule[64];
	
	private final ParseBuffer pathBuffer = new ParseBuffer();	
	private final TrieParser pathParser =new TrieParser(2048, 2, false, false, true);
	
	StructureDataModuleManager() {
	}

	public static StructureDataModuleManager instance = new StructureDataModuleManager();
	
	public StructureDataModule moduleByIdx(int idx) {
		return modules[idx];
	}
	
	//for local files which may be under development we want to force a reindex
	public StructureDataModule indexModuleFolder(File myFolder, StorageCache storageCache, boolean forceReIndex) {
				
		//update the module and only release it for use when its fully populated.
		synchronized (this.modules) { 
			//must be here it may cause modules to grow
			int id = pathIndex(myFolder.getAbsolutePath().getBytes());//warning, nested lock
			if (forceReIndex || null == this.modules[id]) {			
				
				StructureDataModule module = new StructureDataModule(id, myFolder);				
				//this is everything directly from disk all editors are ignored just this once
				IndexFromDisk.indexModule(module, storageCache);
				//This is only assigned after we have populated all the values
				this.modules[id] = module;
			}
			return this.modules[id];
		}
	}


	public int pathIndex(byte[] path) {
		int id = 0;
		final long idx;
		synchronized (pathBuffer) {
			idx = pathBuffer.matchBytes(path, pathParser);
			if (idx >= 0) {
				id = (int) idx;
			}  else {
				long newId = moduleCount++;	    	
				pathParser.setValue(path, newId);
				id = (int)newId;
			}
			/////////////////////////////////////
			if (id >= this.modules.length) {
				StructureDataModule[] newArray = new StructureDataModule[id<<1];
				System.arraycopy(this.modules, 0, newArray, 0, this.modules.length);
				this.modules = newArray;
			}
		}
		return id;
	}

	
	
}

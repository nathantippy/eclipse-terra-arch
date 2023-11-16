package net.terraarch.terraform.structure;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.eclipse.jgit.api.LogCommand;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.terraarch.terraform.parse.version.VersionDTO;

public class StorageCache {

	private static Logger log = LoggerFactory.getLogger(StorageCache.class);
	
	public final Map<String, VersionDTO[]> versionsCache = new HashMap<String, VersionDTO[]>();
	
	private static final String user = "sa";
	private static final String pass = "";
	private final File providersDatFile;
		   
	private static boolean cleanNow = false; ///set to true if we want to clear the DB by deleting it.
    
	private boolean dbIsInit = false;
	
	StorageCache(File providersDatFile) {
		this.providersDatFile = providersDatFile;
	}
	
	private static PreparedStatement buildSelectProviderHash(Connection dbCon) {		
	    try {
			return dbCon.prepareStatement("SELECT hash FROM providers WHERE provider = ?");
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}	      
	}
	
	private static PreparedStatement buildSelectProviderId(Connection dbCon) {		
	    try {
			return dbCon.prepareStatement("SELECT id FROM providers WHERE provider = ?");
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}	      
	}
	
	private static PreparedStatement buildInsertProvider(Connection dbCon) {		
	    try {
			return dbCon.prepareStatement("INSERT INTO providers (provider, hash) VALUES (?, ?)");
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}	      
	}
	
	private static PreparedStatement buildUpdateProviderHash(Connection dbCon) {		
	    try {
			return dbCon.prepareStatement("UPDATE providers SET hash = ? WHERE id = ?");
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}	      
	}
	
	private static PreparedStatement buildSelectProviderVersions(Connection dbCon) {		
	    try {
			return dbCon.prepareStatement("SELECT version FROM provider_versions WHERE id = ?");
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}	      
	}
	
	private static PreparedStatement buildSelectProviderData(Connection dbCon) {		
	    try {
			return dbCon.prepareStatement("SELECT data FROM provider_versions WHERE id = ? AND version = ?");
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}	      
	}
	
	private static PreparedStatement buildInsertProviderVersion(Connection dbCon) {		
	    try {
			return dbCon.prepareStatement("INSERT INTO provider_versions (id, version, data) VALUES (?, ?, ?)");
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}	      
	}
	
	private static PreparedStatement buildUpdateProviderData(Connection dbCon) {		
	    try {
			return dbCon.prepareStatement("UPDATE provider_versions SET data = ? WHERE id = ? AND version = ?");
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}	      
	}
	

	private static PreparedStatement buildDeleteProviderVersion(Connection dbCon) {		
	    try {
			return dbCon.prepareStatement("DELETE FROM provider_versions WHERE id = ? AND version = ?");
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}	      
	}



	
	private static StorageCache instance;
	
	public final static StorageCache instance(File providersDatFile) {
			if (null!=instance) {
				return instance;
			} else {
				instance = new StorageCache(providersDatFile);
				return instance;			
			}
	}
	

	
}

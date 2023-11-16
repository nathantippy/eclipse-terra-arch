package net.terraarch.terraform.structure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import net.terraarch.util.TrieParser;

public class BlockDetails implements Serializable {

	private static final int OPTIONAL_BIT = 0x1000;

	private static final long serialVersionUID = -6945454128088948069L;
	
	private TrieParser argumentParser = new TrieParser(8, 1, false, false, true);
	private TrieParser attributeParser = new TrieParser(8, 1, false, false, true);
	private List<String> attributeDescriptions = new ArrayList<>();
	private List<String> argumentDescriptions = new ArrayList<>();
	private String fullDoc;
	private String imports;
	
	
	public void addArgument(String name, boolean optional, String description) {
		
		argumentParser.setUTF8Value(name,  (optional ? OPTIONAL_BIT : 0 ) |  argumentDescriptions.size());
		argumentDescriptions.add(description);
		
	}
	
	public void addAttribute(String name, String description) {
				
		attributeParser.setUTF8Value(name, attributeDescriptions.size());
		attributeDescriptions.add(description);
	
	}
	
    public TrieParser argumentParser() {
    	return argumentParser;    	
    }
    public TrieParser attributeParser() {
    	return attributeParser;
    }
    public String attributeDescription(int idx) {
    	return attributeDescriptions.get(idx & 0xFFF);
    }
    public String argumentDescription(int idx) {
    	return argumentDescriptions.get(idx & 0xFFF);
    }
    public boolean isOptional(int idx) {
    	return 0 != (OPTIONAL_BIT&idx);
    }

    
    public String fullDocument() {
    	return fullDoc;
    }
	public void fullDocument(String fullDoc) {
		this.fullDoc = fullDoc;
	}

	public void imports(String impots) {
		this.imports = imports;
	}
	public String imports() {
		return imports;
	}

	
	private String version;  //here for convenience
	private String provider;  //here for convenience
    private String type;
    private String name;
	private int recVersion;
    
	public void version(String version) {
		this.version = version;
	}
	public String version() {
		return version;
	}

	public void provider(String provider) {
		this.provider = provider;
	}
	public String provider() {
		return provider;
	}

	public void type(String type) {
		this.type = type;
	}
	public String type() {
		return type;
	}

	public void name(String name) {
		this.name = name;
	}
	public String name() {
		return name;
	}

	public void recVersion(int recVersion) {
		this.recVersion = recVersion;
	}
	public int recVersion() {
		return recVersion;
	}
	
	
}

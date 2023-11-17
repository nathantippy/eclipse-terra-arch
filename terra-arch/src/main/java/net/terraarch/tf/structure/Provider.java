package net.terraarch.tf.structure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import net.terraarch.util.TrieParser;

/////////////////////////////////////////////////////
//Shared, this is used by lambda and the eclipse client
//this object is serialized and sent to the client
public class Provider implements Serializable {

	/**
	 * Do not modify
	 */
	private static final long serialVersionUID = 7559876555867101765L;

	//no extraction and ignore case for trie parser
	private TrieParser resourceNames = new TrieParser(8, 1, false, false, true);
	private TrieParser datasourceNames = new TrieParser(8, 1, false, false, true);
	private String name;
	private String version;
    private List<BlockDetails> resources = new ArrayList<>();
    private List<BlockDetails> dataSources = new ArrayList<>();
    
	public Provider() {
	}
	
	public Provider(String name, String version) {
		this.name = name;
		this.version = version;
	}
	
	public void addResource(String name, BlockDetails resource) {
		resourceNames.setUTF8Value(name, resources.size());
		resources.add(resource);
	}
	
	public void addDataSource(String name, BlockDetails dataSource) {
		datasourceNames.setUTF8Value(name, dataSources.size());
		dataSources.add(dataSource);
	}
	
	public String name() {
		return name;
	}
	public String version() {
		return version;
	}
	public TrieParser resourceParser() {
		return resourceNames;
	}
	public TrieParser datasourceParser() {
		return resourceNames;
	}
	public BlockDetails resourceDetails(int idx) {
		return resources.get(idx);
	}
	public BlockDetails datasourceDetails(int idx) {
		return dataSources.get(idx);
	}
		
}

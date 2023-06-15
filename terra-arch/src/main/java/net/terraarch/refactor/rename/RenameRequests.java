package net.terraarch.refactor.rename;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;


public class RenameRequests {

	public static final int VAR_TYPE   = 0;
	public static final int LOCAL_TYPE = 1;
	public static final int BLOCK_TYPE = 2;
		
	private List<Replacement> variableReq = new ArrayList<Replacement>();
	private List<Replacement> localReq = new ArrayList<Replacement>();
	private List<Replacement> moduleReq = new ArrayList<Replacement>();
	private List<Replacement> dataReq = new ArrayList<Replacement>();
	private List<Replacement> resourceReq = new ArrayList<Replacement>();
    private List<Replacement> providerReq = new ArrayList<Replacement>();		
	
	public static class Replacement {
		
		public final byte[] namespace;
		public final byte[] oldName;
		public final String newName;
		
		public Replacement(String namespace, String oldName, String newName) {
			this.namespace = null==namespace ? null :namespace.getBytes();
			this.oldName = oldName.getBytes();
			this.newName = newName;
		}
	}
	
	
	public void vistVarReplacements(Consumer<? super Replacement> action) {
		variableReq.forEach(action);		
	}
	public void vistLocalReplacements(Consumer<? super Replacement> action) {
		localReq.forEach(action);		
	}
	public void vistModuleNameReplacements(Consumer<? super Replacement> action) {
		moduleReq.forEach(action);		
	}
	public void vistDataNameReplacements(Consumer<? super Replacement> action) {
		dataReq.forEach(action);		
	}
	public void vistResourceNameReplacements(Consumer<? super Replacement> action) {
		resourceReq.forEach(action);		
	}
	public void vistProviderNameReplacements(Consumer<? super Replacement> action) {
		providerReq.forEach(action);		
	}
	
	
	public void addVariableReplacement(String oldText, String newText) {
		variableReq.add(new Replacement(null, oldText, newText));
	}
	public void addLocalReplacement(String oldText, String newText) {
		localReq.add(new Replacement(null, oldText, newText));
	}
	public void addModuleReplacement(String oldText, String newText) {
		moduleReq.add(new Replacement(null, oldText, newText));
	}
	public void addDataReplacement(String blockName, String oldText, String newText) {
		dataReq.add(new Replacement(blockName, oldText, newText));
	}
	public void addResourceReplacement(String blockName, String oldText, String newText) {
		resourceReq.add(new Replacement(blockName, oldText, newText));
	}
	public void addProviderReplacement(String blockName, String oldText, String newText) {
		providerReq.add(new Replacement(blockName, oldText, newText));
	}
	
	public boolean hasRequest() {
		return !variableReq.isEmpty() 
				|| !localReq.isEmpty() 
				|| !moduleReq.isEmpty() 
				|| !dataReq.isEmpty() 
				|| !resourceReq.isEmpty()
				|| !providerReq.isEmpty();
	}
	
}

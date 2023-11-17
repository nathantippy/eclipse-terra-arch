package net.terraarch.tf.parse.doc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.terraarch.tf.parse.ParseState;
import net.terraarch.tf.parse.version.VersionConstraint;
import net.terraarch.tf.parse.version.VersionDTO;
import net.terraarch.tf.structure.StructureDataModule;
import net.terraarch.util.AppendableBuilderReader;
import net.terraarch.util.TrieParser;
import net.terraarch.util.TrieParserReader;

public abstract class DocumentMap extends ParseState {
	
    final StructureDataModule module;
    final boolean isModuleClean;
    final boolean isDisabled;
	final TrieParserReader localReader = new TrieParserReader(true);
	private List<VersionConstraint> tfConstraints;
	public final Map<String, VersionDTO> providerVersion = new HashMap<>();
	public final TrieParser definedProviders = new TrieParser();
    //NOTE: this stack is built by the forEnd,forStart, collectForArg methods
    //      but it is needed for identifier recongintion in termOneProcessing
    //      holds all nested for variables/args 
    final List<TrieParser> forArgStack = new ArrayList<TrieParser>();
    
    public int lastPosition; 

    
    private int tokenLastPos = -1;
    public int tokenLastPos() {
    	return tokenLastPos;
    }
    public void tokenLastPos(int value) {
    	tokenLastPos=value;
    }
    
    
	//to detect errors, this could be done by a stack but would require GC
	//we can add as many types here as we would like to track for capturing
	//invalid combindations as they are needed
	protected final int TEXT_ID  = 1;
	protected final int IDENT_ID = 2;
	protected final int UNDEF_ID = -1;
	int lastToken = -1;
    
	public DocumentMap(StructureDataModule module, boolean isDisabled, ProviderConstraintImpl pci) {
		super("");		
		this.module = module;
		this.isModuleClean = null==module ? true :module.isClean();
		this.isDisabled = isDisabled;		
		this.pci = pci;
		
	}

	public String startFile(String cannonicalLocation, String localPathLocation) {
		lastPosition = 0;
		return super.fileStart(cannonicalLocation, localPathLocation);
	}
	
	@Override
	public boolean isDisabled() {
		return isDisabled;
	}
	
	
	//if true we must allow all self fields
	protected boolean isUnderLifecycleChild() {
		return (blockIdentiferStack.size()>=2)
			&& arrayDepth==1  // ignore_changes holds an array of children
		    && Arrays.equals(blockIdentiferStack.get(1),"lifecycle".getBytes());
	}
	
	@Override
	protected void collectForArg(int depth, int endPos, AppendableBuilderReader reader) {
		lastToken = UNDEF_ID;
		reader.addToTrieParser(forArgStack.get(depth-1),11);
	
		super.collectForArg(depth, endPos, reader);
	}
    	

	public TrieParser definedProviders() {
		return definedProviders;
	}	
	
	public List<VersionConstraint> terraformVersionConstraints() {
		return tfConstraints;
	}
	
	// specific terraform version constraints
	@Override
	protected void terraformVersionConstraints(List<VersionConstraint> constraints, int position, int len, int pos) {
		this.tfConstraints = constraints;
	}

	private final ProviderConstraintImpl pci;

	// load a legacy provider
	@Override
	protected void providerVersionConstraints(
			String name, List<VersionConstraint> constraints, 
			int position, int len, int pos) { // file location
		pci.setPoviderConstraintsLocal(this, name, "hashicorp", name, constraints);
	}

	// provide the expected name for a specific namespace and name which may not be
	// loaded from hashi
	@Override
	protected void providerVersionConstraints(
			String name, String sourceNamespace, String sourceName,
			List<VersionConstraint> constraints,
			int position,	int len, int pos) { // file location?? for module
		pci.setPoviderConstraintsLocal(this, name,sourceNamespace,sourceName,constraints);																		// only?
	}

	// provides a second alias name for this instance?? how to tell which this is?
	@Override
	protected void providerVersionConstraints(
			String alias, String name, 
			List<VersionConstraint> constraints,
			int position, int len, int pos) { // file location
		pci.setPoviderConstraintsLocal(this, alias, "hashicorp", name, constraints);
	}
	
	
}

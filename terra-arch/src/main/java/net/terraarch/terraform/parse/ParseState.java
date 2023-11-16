package net.terraarch.terraform.parse;

import java.io.File;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.terraarch.terraform.parse.version.VersionConstraint;
import net.terraarch.terraform.parse.version.VersionConstraints;
import net.terraarch.util.AppendableBuilder;
import net.terraarch.util.AppendableBuilderReader;
import net.terraarch.util.Appendables;
import net.terraarch.util.TrieParser;
import net.terraarch.util.TrieParserReader;

public class ParseState {

	private static final byte[] ALIAS = "alias".getBytes();

	private static final byte[] PROVIDER = "provider".getBytes();

	private static final byte[] DEPENDS_ON = "depends_on".getBytes();

	private static final byte[] DEFAULT = "default".getBytes();

	private static final byte[] EXPERIMENTS = "experiments".getBytes();

	private static final byte[] REQUIRED_VERSION = "required_version".getBytes();

	private static final byte[] SOURCE = "source".getBytes();

	private static final byte[] VERSION = "version".getBytes();

	private static final byte[] REQUIRED_PROVIDERS = "required_providers".getBytes();

	private static final Logger logger = LoggerFactory.getLogger(ParseState.class);



	public static boolean captureCommentsToFile = false; // default is off, may turn on for SQL.

	public static final TrieParser variableFieldsParser =  BlockType.VARIABLE.exclusiveValidChildren();
	public static final TrieParser outputFieldsParser = BlockType.OUTPUT.exclusiveValidChildren();
	public static final TrieParser lifecycleFieldsParser = BlockType.LIFECYCLE.exclusiveValidChildren();
	public static final TrieParser terraformFieldsParser = BlockType.TERRAFORM.exclusiveValidChildren();

	public static enum TERRAFORM_FIELDS implements LiteralTerm {
		EXPERIMENTS("experiments"), BACKEND("backend"), WORKSPACE("workspace"), REQUIRED_VERSION("required_version"),
		REQUIRED_PROVIDERS("required_providers"); //TODO: unexpected, hang when we edit inside this block !!!
		private String value;
				
		public String value() {
			return value;
		}
		TERRAFORM_FIELDS(String value) {
			this.value = value;
		}
	}
	public static enum LIFECYCLE_FIELDS implements LiteralTerm {
		CREATE_BEFORE_DISTROY("create_before_distroy"), PREVENT_DISTROY("prevent_distroy"),
		IGNORE_CHANGES("ignore_changes");
		private String value;
		public String value() {
			return value;
		}
		LIFECYCLE_FIELDS(String value) {
			this.value = value;
		}
	}
	public static enum VARIABLE_FIELDS implements LiteralTerm {
		TYPE("type"), DESCRIPTION("description"), VALIDATION("validation"), DEFAULT("default");
		private String value;
		public String value() {
			return value;
		}
		VARIABLE_FIELDS(String value) {
			this.value = value;
		}
	}
	public static enum OUTPUT_FIELDS implements LiteralTerm {
		VALUE("value"), DESCRIPTION("description"), SENSITIVE("sensitive"), DEPENDS_ON("depends_on");
		private String value;
		public String value() {
			return value;
		}
		OUTPUT_FIELDS(String value) {
			this.value = value;
		}
	}
	static {
	    	populateTrieWithLiterals(OUTPUT_FIELDS.values(), outputFieldsParser);
	    	populateTrieWithLiterals(VARIABLE_FIELDS.values(), variableFieldsParser);
	    	populateTrieWithLiterals(LIFECYCLE_FIELDS.values(), lifecycleFieldsParser);
	    	populateTrieWithLiterals(TERRAFORM_FIELDS.values(), terraformFieldsParser);
	}
	

	
	
	
	
	
	
	
	
	// protected static final TrieParser providerFieldsParser = new
	// TrieParser(PROVIDER_FIELDS.values().length,1,false,false);

	
	// only the common ones, we need to do this from lookup
//	public static enum PROVIDER_FIELDS implements LiteralTerm {
//		ALIAS("alias"), REGION("region");
//		private String value;
//		public String value() {
//			return value;
//		}
//		PROVIDER_FIELDS(String value) {
//			this.value = value;
//		}		
//	}

	// common ones
//	public static enum DATA_FIELDS implements LiteralTerm {
//		MOST_RECENT("most_recent"),
//		OWNERS("owners"),
//		//NOTE: lifecycle is reserved for future versions but unused in .12
//		TAGS("tags"),
//		DEPENDS_ON("depends_on"),
//		COUNT("count"),
//		TEMPLATE("template"),
//		STATEMENT("statement"),
//		BACKEND("backend"),
//		CONFIG("config"),		
//		FOR_EACH("for_each"),
//		PROVIDER("provider"),
//		FILTER("filter");
//		private String value;
//		public String value() {
//			return value;
//		}
//		DATA_FIELDS(String value) {
//			this.value = value;
//		}		
//	}


	public static enum FUNCTIONS implements LiteralTerm {  // checked against 1.5.x

		// numeric
		ABS("abs"), CEIL("ceil"), FLOOR("floor"), LOG("log"), MAX("max"), MIN("min"), PARSEINT("parseint"), POW("pow"), SIGNUM("signum"), 
		// string
		CHOMP("chomp"), ENDSWITH("endswith"), FORMAT("format"), FORMATLIST("formatlist"), INDENT("indent"), JOIN("join"), LOWER("lower"),
		REGEX("regex"), REGEXALL("regexall"), REPLACE("replace"), SPLIT("split"), STARTSWITH("startswith"), STRCONTAINS("strcontains"),
		STRREV("strrev"), SUBSTR("substr"), TITLE("title"), TRIM("trim"), TRIMPREFIX("trimprefix"), TRIMSUFFIX("trimsuffix"), TRIMSPACE("trimspace"),
		UPPER("upper"),
		// collection
		ALLTRUE("alltrue"),ANYTRUE("anytrue"),CHUNKLIST("chunklist"), COALESCE("coalesce"), COALESCELIST("coalescelist"), COMPACT("compact"),
		CONCAT("concat"), CONTAINS("contains"), DISTINCT("distinct"), ELEMENT("element"), FLATTEN("flatten"),
		INDEX("index"), KEYS("keys"), LENGTH("length"), //LIST("list"),
		LOOKUP("lookup"), //MAP("map")
		MATCHKEYS("matchkeys"), MERGE("merge"), ONE("one"), RANGE("range"), REVERSE("reverse"), SETINTERSECTION("setintersection"), SETPRODUCT("setproduct"),
		SETSUBTRACT("setsubtract"), SETUNION("setunion"), SLICE("slice"), SORT("sort"), SUM("sum"), TRANSPOSE("transpose"), VALUES("values"),
		ZIPMAP("zipmap"),
		// encoding
		BASE64DECODE("base64decode"), BASE64ENCODE("base64encode"), BASE64GZIP("base64gzip"), CSVDECODE("csvdecode"),
		JSONDECODE("jsondecode"), JSONENCODE("jsonencode"), TEXTDECODEBASE64("textdcodebase64"), TEXTENCODEBASE64("textencodebase64"),
		URLENCODE("urlencode"), YAMLDECODE("yamldecode"),YAMLENCODE("yamlencode"),
		// filesystem
		ABSPATH("abspath"), DIRNAME("dirname"), PATHEXPAND("pathexpand"), BASENAME("basename"), FILE("file"),
		FILEEXISTS("fileexists"), FILESET("fileset"), FILEBASE64("filebase64"), TEMPLATEFILE("templatefile"),
		// date and time
		FORMATDATE("formatdate"), PLANTIMESTAMP("plantimestamp"), TIMEADD("timeadd"), TIMECMP("timecmp"), TIMESTAMP("timestamp"),
		// hash and crypto
		BASE64SHA256("base64sha256"), BASE64SHA512("base64sha512"), BCRYPT("bcrypt"),
		FILEBASE64SHA256("filebase64sha256"), FILEBASE64SHA512("filebase64sha512"), FILEMD5("filemd5"),	FLESHA1("filesha1"), 
		FILESHA256("filesha256"), FILESHA512("filesha512"), MD5("md5"), RSADECRYPT("rsadecrypt"),
		SHA1("sha1"), SHA256("sha256"), SHA512("sha512"), UUID("uuid"), UUIDV5("uuidv5"),
		// ip network
		CIDRHOST("cidrhost"), CIDRNETMASK("cidrnetmask"), CIDRSUBNET("cidrsubnet"), CIDRSUBNETS("cidrsubnets"),
		// type conversions
		CAN("can"), NONSENSITIVE("nonsensitive"), SENSITIVE("sensitive"), TOBOOL("tobool"), TOLIST("tolist"), TOMAP("tomap"),
		TONUMBER("tonumber"), TOSET("toset"), TOSTRING("tostring"), TRY("try"), TYPE("type"),
		// type definitions, colections and structures
		LIST("list"), MAP("map"), SET("set"), OBJECT("object"), TUPLE("tuple");

		private String value;

		public String value() {
			return value;
		}

		FUNCTIONS(String value) { // TODO: update with return type, input args and compute method
			this.value = value;
		}
	}




	// path.<TYPE>. TYPE can be cwd, module, or root
	public static enum PATH_TYPE implements LiteralTerm {

		CWD("cwd"), MODULE("module"), ROOT("root");

		private String value;

		public String value() {
			return value;
		}

		PATH_TYPE(String value) {
			this.value = value;
		}
	}


	
	public static enum VARIABLE_TYPE implements LiteralTerm {

		TERRA("terra"), // NOTE: just for the logo color

		MAP("map"), // shorthand for map(any,any)
		LIST("list"), // shorthand for list(any)

		ANY("any"), //constraint for any type to be determined at usage time, not an actual type
		STRING("string"), NUMBER("number"), BOOL("bool");

		private String value;

		public String value() {
			return value;
		}

		VARIABLE_TYPE(String value) {
			this.value = value;
		}

	}
	
	
	

	
	
	public static enum NAMESPACES implements LiteralTerm {

		VAR("var"), LOCAL("local"), MODULE("module"), DATA("data"), 
		PATH("path"), COUNT("count"), EACH("each"),
		SELF("self"),

		TERRAFORM("terraform");

		private String value;

		public String value() {
			return value;
		}

		NAMESPACES(String value) {
			this.value = value;
		}
	}

	public static enum LITERALS implements LiteralTerm {

		TRUE("true"), FALSE("false"), NULL("null"), 
		
		//TODO: these need more testing and peraps to be moved elsewhere.
		CONTINUE("continue"), FAIL("fail"), // on_failure = continue
		DESTROY("destroy"), // when = destroy
		ALL("all"), // lifecycle ignore_changes = all
		
		SPLAT("*"); // not a literal but behaves like one for painting.

		private String value;

		public String value() {
			return value;
		}

		LITERALS(String value) {
			this.value = value;
		}
	}

  
	public static final TrieParser functionNameParser = new TrieParser(FUNCTIONS.values().length, 1, false, false);
	public static final TrieParser literalsParser = new TrieParser(LITERALS.values().length, 1, false, false);
	public static final TrieParser namespaceParser = new TrieParser(NAMESPACES.values().length, 1, false, false);
	public static final TrieParser pathTypeParser = new TrieParser(PATH_TYPE.values().length, 1, false, false);
	public static final TrieParser variableTypeParser = new TrieParser(VARIABLE_TYPE.values().length, 1, false, false);
	public static final TrieParser blockTypeParser = new TrieParser(BlockType.values().length, 1, false, false);
	

	static {

		// populateTrieWithLiterals(PROVIDER_FIELDS.values(), providerFieldsParser);
		populateTrieWithLiterals(FUNCTIONS.values(), functionNameParser);
		populateTrieWithLiterals(LITERALS.values(), literalsParser);
		populateTrieWithLiterals(NAMESPACES.values(), namespaceParser);

		populateTrieWithLiterals(PATH_TYPE.values(), pathTypeParser);
		populateTrieWithLiterals(VARIABLE_TYPE.values(), variableTypeParser);

		populateTrieWithLiterals(BlockType.values(), blockTypeParser);
	}

	
	private static void populateTrieWithLiterals(LiteralTerm[] values, TrieParser target) {
		for (int y = 0; y < values.length; y++) {
			target.setUTF8Value(values[y].value(), values[y].ordinal());
		}
	}
	


	protected final static int bufferBits = 6;
	protected final static int bufferSize = 1 << bufferBits;
	protected final static int bufferMask = bufferSize - 1;
	protected final byte[] identScanBuffer = new byte[bufferSize];

	public static boolean reportParseErrors = true; // turn this off when used in the editor.

	// private final String rootOfGitStorageFolder;

	private static final String debuggingFile1 = null;// "/home/nate/tf-git-cache/github-com/terraform-alicloud-modules/terraform-alicloud-slb-listener/master/v1_2_0/main.tf";
	private static final String debuggingFile2 = null;// "/home/nate/tf-git-cache/github-com/corpit-consulting-public/terraform-aws-security-group/master/v3_1_0/rules.tf";

	public static boolean isFileDebug(String path) {
		return path != null && ((debuggingFile1 != null && path.contains(debuggingFile1))
				|| (debuggingFile2 != null && path.contains(debuggingFile2)));
	}

	public static boolean hasFileDebug() {
		return debuggingFile1 != null || debuggingFile2 != null;
	}
	


	public ParseState(String rootOfGitStorageFolder) {
		// this.rootOfGitStorageFolder = rootOfGitStorageFolder;
	}

	public static class IdentTerm {

		public final List<String> ident = new ArrayList<String>();

		public IdentTerm(String first) {
			ident.add(first);
		}

		public String toString() {
			return ident.toString();
		}
	}

	private List<NodeProcessor> nodePostProcessorStack = new ArrayList<NodeProcessor>();
	private List<NodeProcessor> nodePreProcessorStack = new ArrayList<NodeProcessor>();

	public void visitPostStack(Consumer<NodeProcessor> visitor) {
		tempNodePostProcessorStack.forEach(visitor);
		nodePostProcessorStack.forEach(visitor);
	}

	public void visitPreStack(Consumer<NodeProcessor> visitor) {
		nodePreProcessorStack.forEach(visitor);
	}

	public boolean hasProcessingStack() {
		return hasPostProcessingsStack() || !nodePreProcessorStack.isEmpty();
	}

	public boolean hasPostProcessingsStack() {
		return !nodePostProcessorStack.isEmpty() || !tempNodePostProcessorStack.isEmpty();
	}

	public void pushPreProcessing(NodeProcessor np) {
		nodePreProcessorStack.add(np);
	}

	private List<NodeProcessor> tempNodePostProcessorStack = new ArrayList<NodeProcessor>();

	public void pushPostProcessing(NodeProcessor np) {
		if (np == NodeProcessor.HereDoc) { // SPECIAL CASE WE MUST ALWAYS FINISH A HEREDOC FIRST
			nodePreProcessorStack.add(np);
		} else {
			tempNodePostProcessorStack.add(np);
		}
	}

	public void processInProcessStack(TrieParserReader reader, boolean isEOF) {

		// must combine with any existing stack items and ensure nothing is lost.
		if (!tempNodePostProcessorStack.isEmpty()) {
			int i = tempNodePostProcessorStack.size();
			while (--i >= 0) {
				nodePostProcessorStack.add(0, tempNodePostProcessorStack.get(i));
			}
			tempNodePostProcessorStack.clear();

		}

		//
		while (!nodePreProcessorStack.isEmpty()) {
			// System.out.println("pre process running: "+nodePreProcessorStack.get(0));
			TrieNext followOnProcessing = nodePreProcessorStack.get(0).followOnProcessing(reader, this);
			if (null == followOnProcessing) {
				nodePreProcessorStack.remove(0);
			} else {
				return;// try again later
			}
		}

		/////////////////////////////////////////////

		if (lastTrie == TrieNext.NO_OP) {
			lastTrie = null;
		}
		if (!processLastTrie(reader, isEOF)) {
			return; // could not finish this so return later.
		}
		;

		////////////////////////////////////////////

		// NOTE: we must hold stack local here for processing
		// just in case processing causes new items to be added
		// when that happens the remainder of the stack must be after that point.

		List<NodeProcessor> localStack = new ArrayList<NodeProcessor>(nodePostProcessorStack);
		nodePostProcessorStack.clear();

		if (isFileDebug()) {
			System.out.println("resume stack processing element count of: " + localStack.size());
		}

		// wrap up any of its follow on values
		while (!localStack.isEmpty()) {

			// must process from inner to outer since they were stored that way.
			int idx = 0;

			if (isFileDebug()) {
				System.out.println("                process from stack: " + localStack.get(idx) + "  position: "
						+ TrieParserReader.bytesConsumedAfterSetup(reader));
			}

			TrieNext followOnProcessing = localStack.get(idx).followOnProcessing(reader, this);
			if (null == followOnProcessing) {
				localStack.remove(idx);
			} else {

				if (isEOF) {
					nodePostProcessorStack.clear(); // the last call to followOnProcessing may have repopulated this.
					return;// all done
				}

				nodePostProcessorStack.addAll(tempNodePostProcessorStack);
				tempNodePostProcessorStack.clear();

				nodePostProcessorStack.addAll(localStack);

				if (isFileDebug()) {
					System.out.println("unable to follow on with item: " + localStack.get(idx));
					nodePostProcessorStack.forEach(np -> {
						System.out.println("     added back to stack after failure: " + np);

					});

				}
				if (followOnProcessing.trie != null) {
					lastTrie = followOnProcessing;
				}
				return;
			}
		}

		if (isFileDebug()) {
			System.out.println("stack is now empty...");
		}

		///////////// resume a normal processing loop
		TrieNext result = null;
		TrieNext trieTop = this.isTemplate ? TFConstants.trieTemplate() : TFConstants.trieTop();
		long remainingLen = -1;
		do {
			remainingLen = reader.sourceLen;
			result = TFConstants.tokenize(reader, this, trieTop);// //TFConstants.trieResume());
			this.storeLastTrie(result); // hold so we can resume from here if not null

		} while (isEOF && null == result && !hasProcessingStack() && reader.sourceLen > 0 && !isFileHalted(this)
				&& remainingLen != reader.sourceLen);

	}

	private boolean processLastTrie(TrieParserReader reader, boolean isEOF) {
		// continue the last open stack if provided
		if (null != lastTrie) {
			if (isFileDebug()) {
				System.out.println("RESUME NOW RUNNING trie at " + TrieParserReader.bytesConsumedAfterSetup(reader));
				System.out.println("             stack size  of: " + nodePostProcessorStack.size());
				// if (lastTrie.trie!=null) {
				// System.out.println(" trie looking for:
				// "+ParseState.buildExpectedTokensList(lastTrie));
				// }

			}
			TrieNext tempTrie = TFConstants.tokenize(reader, this, lastTrie);
			if (null != tempTrie) {
				if (isFileDebug()) {
					System.out.println("unable to finish given trie ...");
				}
				if (isEOF) {
					// nodePostProcessorStack.clear(); //the last call to followOnProcessing may
					// have repopulated this.
					lastTrie = null;

				}
				if (tempTrie.trie != null) {
					lastTrie = tempTrie;
				}

			} else {
				lastTrie = null;
			}
		} else {

			if (isFileDebug()) {
				System.out.println("RESUME WITHOUT ");
			}
		}
		return lastTrie == null;
	}

	private TrieNext lastTrie = null;

	public void storeLastTrie(TrieNext trie) {
		lastTrie = trie;
	}

	public void unableToParse(TrieParserReader reader, TrieNext trieNext) {

		if (reportParseErrors) {
			final StringBuilder expectingChars = buildExpectedTokensList(trieNext);
			final int column = 1 + TrieParserReader.bytesConsumedAfterSetup(reader) - lineNumberIndexPositon;

			String note = "";
			ModuleDetails md = activeModules.get(canonicalLocation);
			if (null != md) {
				if (md.authorDetails.isEmpty()) {
					logger.trace("WARN: no author details for: {} ", canonicalLocation);
				} else {

					StringBuilder collectedNames = new StringBuilder();
					collectImportantAuthors(md.authorDetails, collectedNames);
					if (collectedNames.length() > 0) {
						note = "You may wish to contact the author(s): " + collectedNames.toString();
					}
				}
			} else {
				logger.error("ERROR: no module found for: {} ", canonicalLocation);
			}
			// While a user is typing this will be hidden
			logger.error(
					"\nUnable to parse file {}\n at location {}:{} idx:{} expected one of the following {}\n pulled files from {}\n {}",
					localPathLocation, lineNumber, column, 1 + TrieParserReader.bytesConsumedAfterSetup(reader),
					expectingChars, canonicalLocation, note);

			logger.error("pos: {} remaining bytes: {}", reader.sourcePos, reader.sourceLen);
			TrieParserReader.debugAsUTF8(reader, System.out, 200);

//			trieNext.builtAt.printStackTrace(); //this is just to find what is getting parsed at the end...
////
//			visitPreStack(np-> {
//				System.out.println("remaining pre stack: "+np);
//			});
//			visitPostStack(np-> {
//				System.out.println("remaining post stack: "+np);
//			});
//			
//			
//			//			

			// System.exit(-1);

			// show where we built this
			// trieNext.builtAt.printStackTrace();
		}
		fileHalt = true;
		moduleHalt = true;
		// needded for debug, nodeProcessorStack.clear();//not need to keep since we are
		// halted in this file

	}

	private void collectImportantAuthors(List<AuthorDetails> authorDetails, StringBuilder collectedNames) {

		Set<AuthorDetails> mostImportantAuthors = extractMostImportanntAuthors(authorDetails);

		boolean added = false;
		for (AuthorDetails author : mostImportantAuthors) {
			collectedNames.append(author.name).append('<').append(author.email).append(">, ");
			added = true;
		}
		if (added) {
			collectedNames.setLength(collectedNames.length() - 2);
		}
	}

	public Set<AuthorDetails> extractMostImportanntAuthors(List<AuthorDetails> authorDetails) {
		long maxWindow = -1;
		AuthorDetails largestWindow = null;

		long maxRecent = -1;
		AuthorDetails mostRecent = null;

		long maxUpdates = -1;
		AuthorDetails mostUpdates = null;

		for (AuthorDetails ad : authorDetails) {
			if (ad.window > maxWindow) {
				maxWindow = ad.window;
				largestWindow = ad;
			}
			if (ad.last > maxRecent) {
				maxRecent = ad.last;
				mostRecent = ad;
			}
			if (ad.count > maxUpdates) {
				maxUpdates = ad.count;
				mostUpdates = ad;
			}
		}
		Set<AuthorDetails> mostImportantAuthors = new HashSet<AuthorDetails>();
		if (null != largestWindow) {
			mostImportantAuthors.add(largestWindow);
		}
		if (null != mostRecent) {
			mostImportantAuthors.add(mostRecent);
		}
		if (null != mostUpdates) {
			mostImportantAuthors.add(mostUpdates);
		}
		return mostImportantAuthors;
	}

	public static StringBuilder buildExpectedTokensList(TrieNext trieNext) {
		final StringBuilder expectingChars = new StringBuilder();
		trieNext.trie.visitPatterns((b, l, v) -> {
			if (b[0] > 32) {// do not report white space as expected, only real chars to help with debugging
				for (int i = 0; i < l; i++) {
					int ch = b[i];
					if (TrieParser.TYPE_VALUE_BYTES == ch) {
						break;// do not show the capture part, it will just confuse.
					}
					if (ch <= 32) {
						Appendables.appendFixedHexDigitsRaw(expectingChars.append("#"), ch, 8);
					} else {
						expectingChars.append((char) ch);// only true because we know this parser only looks for single
															// byte chars
					}
				}
				expectingChars.append(' ');
			}
		});
		return expectingChars;
	}

	protected byte[] nextIdentifier = null;
	protected byte[] blockLabelTextIdentifier = null;
	
	protected final List<byte[]> blockIdentiferStack = new ArrayList<byte[]>();

	public int lastBlockOpenPos = -1;

	protected int blockDepth = 0;

	public int blockOpen(int filePosition) {
		stringTypeConstraint = StringConstraint.NONE;

		blockIdentiferStack.add(nextIdentifier);

		lastBlockOpenPos = filePosition - lineNumberIndexPositon;

		blockLabelTextIdentifier = null; //must clear or it gets picked up inside the block
				
		collectingBlockLabels = false;// we now have all the block labels since we found the {
		return ++blockDepth;
	}

	public int blockClose(int filePosition) {

		blockIdentiferStack.remove(blockIdentiferStack.size() - 1);


				
		if (3 == blockDepth) { // THESE ARE THE NEW .13 PROVIDERS
			
			////////////////////////////////////////////
			// this writes all the captured provider data
			///////////////////////////////////////////
			if (       null != required_provider
					&& required_provider.length > 0
					&& null != required_provider_contraints
					&& required_provider_contraints.size() > 0  ) {

				String name = new String(required_provider, 0, required_provider.length);

				String sourceNamespace = "hashicorp";
				String sourceName = name;
				if (null != required_provider_source && required_provider_source.length() > 0) {
					int slash = required_provider_source.indexOf('/');
					if (-1 == slash) {
						sourceName = required_provider_source;
					} else {
						sourceNamespace = required_provider_source.substring(0, slash);
						sourceName = required_provider_source.substring(slash + 1);
					}
				}

				//TODO: AAAAAAAAA caution this should be threaded before implementation.
				this.providerVersionConstraints(name, 
						sourceNamespace, sourceName, required_provider_contraints,
						requried_provider_constraints_failure_position, required_provider_constraints_length,
						required_provider_constraints_file_position);

				/////////// clear
				required_provider_contraints = null;
				required_provider = null;
				required_provider_source = null;
				requried_provider_constraints_failure_position = -1;
				required_provider_constraints_length = -1;
				required_provider_constraints_file_position = -1;
			}

		}
		if (1 == blockDepth) { // THIS IS THE LEGACY .12 PROVIDERS w/ and w/o alias w/ and w/o constraints

			if (null != required_provider && required_provider.length > 0) {
				
				String name = new String(required_provider, 0, required_provider.length);

				if (null != required_provider_alias) {
					if (null != required_provider_contraints && required_provider_contraints.size() > 0) {
						this.providerVersionConstraints(required_provider_alias, name, required_provider_contraints,
								requried_provider_constraints_failure_position, required_provider_constraints_length,
								required_provider_constraints_file_position);
					} else {
						this.providerVersionConstraints(required_provider_alias, name, Collections.emptyList(), -1, 0,
								-1);
					}
					required_provider_alias = null;
				} else {
					// No alias so call it
					if (null != required_provider_contraints && required_provider_contraints.size() > 0) {
						this.providerVersionConstraints(name, required_provider_contraints,
								requried_provider_constraints_failure_position, required_provider_constraints_length,
								required_provider_constraints_file_position);
					} else {
						this.providerVersionConstraints(name, Collections.emptyList(), -1, 0, -1);
					}
				}

				required_provider_contraints = null;
				required_provider = null;
				required_provider_source = null;
				requried_provider_constraints_failure_position = -1;
				required_provider_constraints_length = -1;
				required_provider_constraints_file_position = -1;
				
			}

		}

		return --blockDepth;
	}

	protected void reset() {

		blockIdentiferStack.clear();
		requestProviderAliasUsage = false;
		terraformRequiredVersionCapture = false;
		required_provider = null;
		required_provider_contraints = null;
		requried_provider_constraints_failure_position = -1;
		required_provider_constraints_length = -1;
		required_provider_constraints_file_position = -1;
		required_provider_source = null;
		required_provider_alias = null;
		stringTypeConstraint = StringConstraint.NONE;

		textQuoteStack.clear();

		moduleGitURLSourcesToRequest.clear();

		textBuilder.clear();
		interpolateDepth = 0;

		activeModules.clear();

		lineNumberIndexPositon = 0;
		lineNumber = 0; // current line number
		localPathLocation = null;
		canonicalLocation = null;
		utfBytesAdjust = 0;

		isTemplate = false;
		templateFileLength = 0;

		fileHalt = false; // set to true when we find a parse error and must stop clean
		moduleHalt = false;
	}

	public void iterateOpen(int filePosition, int size) {

	}

	////////////////////////////
	// for capture begin
	///////////////////////////

//	ForExpr = forTupleExpr | forObjectExpr;
//	forTupleExpr = "[" forIntro Expression forCond? "]";
//	forObjectExpr = "{" forIntro Expression "=>" Expression "..."? forCond? "}";
//	forIntro = "for" Identifier ("," Identifier)? "in" Expression ":";
//	forCond = "if" Expression;

	private boolean collectingForIdentifiers = false;
	private int nestedForDepth = 0;

	public int forStart(int filePosition) {
		collectingForIdentifiers = true; // TODO: A, needs to be a stack for nested for values.
		return ++nestedForDepth;

	}

	public int forEnd(int bytePosition) {
		return --nestedForDepth;
	}

	public void forComma(int filePosition) {
	}

	// called once per argument
	protected void collectForArg(int depth, int endPos, AppendableBuilderReader reader) {

	}

	public void forIn(int filePosition) {
		collectingForIdentifiers = false;

	}

	public void forColon(int filePosition) {

		// the following expression belongs to this for expression

	}

	public void forIf(int filePosition) {

		// the following expression belongs to this if expression

	}

	public void forLambda(int filePosition) {
		// the following expression belongs to this lambda
	}

	public void forLambdaExpand(int filePosition) {
		// ... following the lambda
	}

	public void forLambdaIf(int filePosition) {
		// the following expression belongs to this if expression found after a lambda
	}
	///////////////////////////////////
	// for end of capture
	///////////////////////////////////



	////////////////////////////////////////////
	////////////////////////////////////////////
	public void ternaryCondStart(int filePosition) {
		// expression and ? just discovered
	}

	public void ternaryCondMid(int filePosition) {
		// expression and : just discovered
	}

	public void ternaryEnd(int filePosition) {
		// end of the ternary after the last expression result
	}
	////////////////////////////////////////////
	/////////////////////////////////////////////

	public void functionEnd(int filePosition) {
		paraDepth--;
	}

	public void functionEndWithArrayAsArgs(int filePosition) {
		paraDepth--;
	}

	public void functionPushParam(int filePosition) {
	}

	public AppendableBuilderReader functionOpen(int filePosition) {
		paraDepth++;
		return finishIdentifierTerm(filePosition);
	}

	public AppendableBuilderReader convertLastIdentifierToTuple(int filePosition) {
		return finishIdentifierTerm(filePosition);
	}

	private byte[] moduleNameForSourceStorage = null;
	private boolean fieldIsModuleSource = false;

	private boolean providerVersionForNextTextLiteral = false;
	private boolean providerSourceForNextTextLiteral = false;
	private boolean providerAliasForNextTextLiteral = false;

	protected boolean requestProviderAliasUsage = false;
	private boolean terraformRequiredVersionCapture = false;
	private boolean terraformExperimentsCapture = false;

	
	
	///////////////////
	// collect provider data
	///////////////////
	private byte[] required_provider;
	private List<VersionConstraint> required_provider_contraints;
	private int requried_provider_constraints_failure_position = -1;
	private int required_provider_constraints_length = -1;
	private int required_provider_constraints_file_position = -1;
	private String required_provider_source;
	private String required_provider_alias;
	//////////////////

	protected StringConstraint stringTypeConstraint = StringConstraint.NONE;


	protected byte[] lastAssignmentArgumentName;
	protected int lastAssignmentArgumentPosition; //eg position directly after the =

	public void convertLastIdentifierToAssignement(int filePosition) {
		lastAssignmentArgumentName = nextIdentifier;
		lastAssignmentArgumentPosition = filePosition;
	}
	// this is called directly after the =
	public void endOfAssignement(int filePosition) { // is this a better place to roll up the expressions?
	}

	public boolean isCollectingBlockLabels() {
		return collectingBlockLabels;
	}
	
	protected boolean isInExperimentsBlock() {
		return terraformExperimentsCapture;
	}

	private boolean collectingBlockLabels = false; // when true all idents and strings are part of block name

	public void convertLastIdentifierToBlockLabel(int filePosition) {
		collectingBlockLabels = true;
		stringTypeConstraint = StringConstraint.SINGLE_TERM_IDENTITY;
	}

	///////////////////////////////////////////////
	///////////////////////////////////////////////

	private final StringBuilder numberCollector = new StringBuilder();

	public void pushNumber(int filePosition, long m, int e) {
		numberCollector.setLength(0);
		Appendables.appendDecimalValue(numberCollector, m, (byte) e);
	}

	public void pushNumber(int filePosition, long i) {
		numberCollector.setLength(0);
		Appendables.appendValue(numberCollector, i);
	}

	public void pushNumberSciNotation(int filePosition, String text, long i) {
		Appendables.appendValue(numberCollector.append(text), i).toString();
		numberComplete(filePosition);
	}

	public String numberComplete(int filePosition) {
		return numberCollector.toString();
	}

	public void numberBegin(int filePosition) {
	}
	///////////////////////////////////////////////

	protected int arrayDepth = 0;

	public int arrayOpen(int filePosition) {
		return ++arrayDepth;
	}

	public int arrayClose(int filePosition) {
		terraformExperimentsCapture = false;
		return --arrayDepth;
	}

	public void arrayItem(int filePosition) {
	}

	///////////////////////////////////////////////

	public void setHasComment(int filePosition, boolean hasComment) {

	}

	///////////////////////////////////////////////

	int paraDepth = 0;

	public void parenClose(int filePosition) {
		paraDepth--;
	}

	public void parenOpen(int filePosition) {
		paraDepth++;
	}

	/////////////////////////////////////
	////////////////////////////////////////
	// identifier term begin
	////////////////////////////////////////
	private AppendableBuilder identBuilder = new AppendableBuilder();

	protected boolean appendTerm = false;

	public final void appendIdentTerm() {
		appendTerm = true;
	}

	public void appendIdentifierByte(int singleByte) {
		
		if (' '==singleByte || '{'==singleByte || '}'==singleByte) {
			throw new UnsupportedOperationException("value: "+((char)singleByte));
		}
		
		identBuilder.writeByte(singleByte);
	}

	/*
	 * provider notes: Since providers and their versions are fundemental to
	 * what/how to parse the details this must be parsed at this lowest level so all
	 * other projects can make use of this key information.
	 * 
	 * 
	 * terraform { required_version = ">= 0.10.0" # call terraformVersion(VC)
	 * 
	 * required_providers { aws = ">= 2.12.0" # call setProviderVer(prov, vc) ver 12
	 * 
	 * aws = { # call setProviderVer(prov, namespace, name, vc) ver 13 source =
	 * "hashicorp/aws" version = ">= 2.7.0" } }
	 * 
	 * } provider aws { # call setProviderVer(pro, vc) region = "${var.region}"
	 * version = ">= 1.0.0" #deprecated }
	 * 
	 * provider "random" { # call setProviderVer(alias, pro, vc) alias def pos alias
	 * = "usw2" version = "~> 2.1" #deprecated }
	 * 
	 * provider "aws" { # call setProviderVer(alias, pro, vc) alias def pos alias =
	 * "usw2" region = "us-east-1" #deprecated }
	 * 
	 * provider "template" { version = "~> 2.1" #deprecated }
	 * 
	 * 
	 * 
	 * end of provider notes and examples
	 */

	// specific terraform version constraints
	protected void terraformVersionConstraints(
			List<VersionConstraint> constraints, 
			int falurePosition, int len, int pos) {
		// confirmed working
		// System.out.println("terraform Version: "+constraints);
	}

	// load a legacy provider
	protected void providerVersionConstraints(
			String name, 
			List<VersionConstraint> constraints, 
			int falurePosition, int len, int pos) { // file location
		// confirmed working
		// System.out.println("A providerVersion: "+name+" "+constraints);

	}

	// provide the expected name for a specific namespace and name which may not be
	// loaded from hashi
	protected void providerVersionConstraints(
			String name, String sourceNamespace, String sourceName,
			List<VersionConstraint> constraints,
			int falurePosition,	int len, int pos) { // file location?? for module
																							// only?
		// confirmed working 0.13
		// System.out.println("B providerVersion: "+name+" "+sourceNamespace+" "+sourceName+" "+constraints);
	}

	// provides a second alias name for this instance?? how to tell which this is?
	protected void providerVersionConstraints(
			String alias, String name, 
			List<VersionConstraint> constraints,
			int falurePosition, int len, int pos) { // file location
		// confirmed working
		 //System.out.println("C providerVersion: "+alias+" "+name+" "+constraints);
	}

	/////////////////////////////
	////////////////////////////

	private AppendableBuilderReader finishIdentifierTerm(int filePosition) {

		// when we have arrays ending in , we will get a zero length identifier.
		/////////////////////
		AppendableBuilderReader lastIdentifierTerm = completeIdentifier();

		if (!collectingForIdentifiers) {
			// NOTE: if the identifier string is true, false or null it is now a literal,
			// tokenizer will not make this distinction.

			appendTerm = false;
		} else {
			collectForArg(nestedForDepth, filePosition, lastIdentifierTerm);
		}

		/////////////////////////////
		// set the string constraint type so we can validate what it contains
		// this will have been set before the first open quote so special logic can be
		///////////////////////////// applied
		/////////////////////////////
		if (0 == blockDepth) {
			// these strings must not have any interplation

			if (isCollectingBlockLabels()) {
				stringTypeConstraint = StringConstraint.SINGLE_TERM_IDENTITY;
			} else {
				stringTypeConstraint = StringConstraint.NONE;
			}

		} else {
			stringTypeConstraint = StringConstraint.NONE;
			if (1 == blockDepth) {
				final byte[] bs = this.blockIdentiferStack.get(0);
				
				if (BlockType.MODULE.isEqual(bs)) {
					if (lastIdentifierTerm.isEqual(SOURCE)) {
						fieldIsModuleSource = true;
						moduleNameForSourceStorage = blockLabelTextIdentifier;
					}
				}
				if (BlockType.PROVIDER.isEqual(bs)) {
					if (lastIdentifierTerm.isEqual(VERSION)) {
						providerVersionForNextTextLiteral = true; // legacy approach
						if (this.blockIdentiferStack.size() >= 2) {
							required_provider = this.blockIdentiferStack.get(1); // why is this missing?
						} else {
							if (null != blockLabelTextIdentifier) {
								required_provider = blockLabelTextIdentifier; // the name has quotes
								blockLabelTextIdentifier = null;
							}
						}
					}
					if (lastIdentifierTerm.isEqual(ALIAS)) {
						providerAliasForNextTextLiteral = true;
						if (this.blockIdentiferStack.size() >= 2) {
							required_provider = this.blockIdentiferStack.get(1); // why is this missing?
						} else {
							if (null != blockLabelTextIdentifier) {
								required_provider = blockLabelTextIdentifier; // the name has quotes
								blockLabelTextIdentifier = null;
							} // the name has quotes
						}
					}

					// todo: GATHER THE OTHER FIELDS??S

				} else {
					// not in provider so ensure this is clear
					providerVersionForNextTextLiteral = false;
				}

				if (BlockType.RESOURCE.isEqual(bs) || BlockType.DATA.isEqual(bs)) {
					if (lastIdentifierTerm.isEqual(PROVIDER)) {
						requestProviderAliasUsage = true;

						// TODO: where is tis called from? this can be text or labels... set to false after used by text or label.

					}
				} else {
					requestProviderAliasUsage = false;
				}

				if (BlockType.RESOURCE.isEqual(bs)) {
					if (lastIdentifierTerm.isEqual(DEPENDS_ON)) {
						// these must be references
						stringTypeConstraint = StringConstraint.REFERENCE_IDENTIFIER;
					}
				}

				if (BlockType.VARIABLE.isEqual(bs)) {
					if (lastIdentifierTerm.isEqual(DEFAULT)) {
						// these are literals only
						stringTypeConstraint = StringConstraint.NO_INTERPOLATION;
					}
				}

				// required_version is the next string value we need to parse
				if (BlockType.TERRAFORM.isEqual(bs)) {
					if (lastIdentifierTerm.isEqual(REQUIRED_VERSION)) {
						terraformRequiredVersionCapture = true;
					}
					if (lastIdentifierTerm.isEqual(EXPERIMENTS)) {
						terraformExperimentsCapture = true;
					}
				}
				// required_providers
			} else if (2 == blockDepth) {
				if (BlockType.TERRAFORM.isEqual(this.blockIdentiferStack.get(0))) {
					if (Arrays.equals(this.blockIdentiferStack.get(1), REQUIRED_PROVIDERS)) {
						required_provider = lastIdentifierTerm.toBytes();
					}
				}
			} else if (3 == blockDepth) {
				// https://www.terraform.io/upgrade-guides/0-13.html

				providerVersionForNextTextLiteral = false;

				if (BlockType.TERRAFORM.isEqual(this.blockIdentiferStack.get(0))) {
					if (Arrays.equals(this.blockIdentiferStack.get(1), REQUIRED_PROVIDERS)
							&& Arrays.equals(this.blockIdentiferStack.get(2), required_provider)) {

						if (lastIdentifierTerm.isEqual(VERSION)) {
							providerVersionForNextTextLiteral = true;
						}

						if (lastIdentifierTerm.isEqual(SOURCE)) {
							// NAMESPACE/NAME THE NAMESPACE IS OPTIONAL AND IS hashicorp WHEN MISSING
							providerSourceForNextTextLiteral = true;
						}

					}
				}
			}
		}
		return lastIdentifierTerm;
	}

	////////////////////////////////////////
	// identifier term end
	////////////////////////////////////////

	public AppendableBuilderReader completeIdentifier() {
		AppendableBuilderReader result = identBuilder.reader();

		identBuilder.clear();
		return result;
	}

	///////////////////////////////////////////
	// multi line comment begin
	//////////////////////////////////////////
	private BytesCollectorBase multiLineComment = null;

	public BytesCollectorBase multiLineComment(int filePosition) {
		if (null == multiLineComment) {
			setHasComment(filePosition, true);
			multiLineComment = captureCommentsToFile ? new BytesCollector("comment") : new BytesCollectorBase();
		}
		return multiLineComment;
	}

	public File multiLineCommentFinish(int filePosition) {
		lineNumber += multiLineComment.totalNewLinesCaptured();
		lineNumberIndexPositon = filePosition;
		File result = multiLineComment.finish();
		multiLineComment = null;
		return result;
	}
	////////////////////////////////////////
	// multi line comment end
	////////////////////////////////////////

	private BytesCollectorBase singleLineComment = null;

	public BytesCollectorBase singleLineComment(int filePosition) {
		if (null == singleLineComment) {
			setHasComment(filePosition, true);
			singleLineComment = captureCommentsToFile ? new BytesCollector("comment") : new BytesCollectorBase();
		}
		return singleLineComment;
	}

	public File singleLineCommentFinish(int filePosition) {
		lineNumber++;
		lineNumberIndexPositon = filePosition;
		File result = singleLineComment.finish();
		singleLineComment = null;

//		if (isFileDebug()) {
//			byte[] data = new byte[(int) result.length()];
//			
//			FileInputStream fist;
//			try {
//				fist = new FileInputStream(result);
//				fist.read(data);
//				fist.close();
//				System.out.println("SINGLE LINE COMMENT: "+new String(data));
//				
//				
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}

		return result;
	}

	////////////////////////////////////////
	// push operators begin
	///////////////////////////////////////

	public String pushOperation(int filePosition, final String op) {
		return op;
	}

	public final String opAdd = "+";
	public final String opDiv = "/";
	public final String opEq = "==";
	public final String opGt = ">";
	public final String opGtE = ">=";
	public final String opLt = "<";
	public final String opLtE = "<=";
	public final String opAnd = "&";
	public final String opOr = "|";
	public final String opMod = "%";
	public final String opMul = "*";
	public final String opNotEq = "!=";
	public final String opSub = "-";
	public final String opSign = "S";
	public final String opNot = "N";

	///////////////////////////////////////
	// operators end
	//////////////////////////////////////

	protected final List<byte[]> textQuoteStack = new ArrayList<byte[]>();

	public void hereDocTextBegin(int filePosition, byte[] quotes) {
		assert (quotes != null);
		textQuoteStack.add(quotes);
		textBuilder.clear();
	}

	public void textBegin(int filePosition, byte[] quotes) {
		assert (quotes != null);
		textQuoteStack.add(quotes);
		textBuilder.clear();

	}

	public byte[] textQuote() {
		return textQuoteStack.isEmpty() ? null : textQuoteStack.get(textQuoteStack.size() - 1);
	}

	public void appendTextByte(int b) {
		textBuilder.writeByte(b);
	}

	public final List<String> moduleGitURLSourcesToRequest = new ArrayList<String>();
	public byte[] activeModuleSource = null;
	
	public AppendableBuilderReader textEnd(int filePosition) {

		AppendableBuilderReader reader = textBuilder.reader();
		textBuilder.clear();
	
		if (fieldIsModuleSource) {	
			activeModuleSource = reader.toBytes();					
			fieldIsModuleSource = false;
		}
		
		if (null != blockLabelTextIdentifier) {
				String url = null;
				if (reader.startsWith("git::".getBytes())) { // ref=tags/0.12.0
					url = reader.toString();
					// github.com/muvaki/terraform-google-project
					// git::https://github.com/cloudposse/terraform-null-label.git?ref=tags/0.16.0
				}
				if (reader.startsWith("github.com/".getBytes())) {
					url = "git::https://" + reader.toString();
				}
				if (null != url && url.length() > 4) {		
					moduleGitURLSourcesToRequest.add(url);		
				}
				
				blockLabelTextIdentifier = null;
	
			} else if (terraformRequiredVersionCapture) {
					//TODO: AAAAAAAAAA large paste causing parse to have a problem??
					List<VersionConstraint> results = new ArrayList<>();
					int falurePosition = VersionConstraints.parse(reader, vc->results.add(vc));
		
					if (falurePosition != -1) {
		
						//	new Exception("bad vale here: "+reader.toString()).printStackTrace();
					} else {
					
						this.terraformVersionConstraints(results, falurePosition, reader.byteLength(), filePosition);
						terraformRequiredVersionCapture = false;
					}
				
			
			} else if (providerSourceForNextTextLiteral) {
	
				required_provider_source = reader.toString();
				providerSourceForNextTextLiteral = false;
	
			} else if (providerVersionForNextTextLiteral) {
				
				List<VersionConstraint> results = new ArrayList<>();
	
				required_provider_constraints_file_position = filePosition;
				requried_provider_constraints_failure_position = VersionConstraints.parse(reader, vc->results.add(vc));
				required_provider_constraints_length = reader.byteLength();
				required_provider_contraints = results;
				providerVersionForNextTextLiteral = false;
	
			} else if (providerAliasForNextTextLiteral) {
	
				required_provider_alias = reader.toString();
				providerAliasForNextTextLiteral = false;
	
			} else if (required_provider != null && required_provider.length > 0) {
				// this is the v 0.12 direct version constraint
				List<VersionConstraint> results = new ArrayList<>();
				int failurePosition = VersionConstraints.parse(reader, vc->results.add(vc)); //TODO: AAAAAAA  does this hang here???
				// this is special in that we must record and clear it now.
				if (!results.isEmpty()) {
					this.providerVersionConstraints(new String(required_provider, 0, required_provider.length), results,
						failurePosition, reader.byteLength(), filePosition);
				}
				required_provider = null;
			}

		
		if (isCollectingBlockLabels()) {
			blockLabelTextIdentifier = reader.toBytes();
		} else {
			blockLabelTextIdentifier = null;
			
			//ABOVE BLOCK SHOULD BE HERE?
			
			
		}

		textQuoteStack.remove(textQuoteStack.size() - 1);

		return reader;
	}

	protected boolean isInterpolationValidHere() {
		return StringConstraint.NONE == stringTypeConstraint;
	}

	protected StringConstraint stringConstraint() {
		return stringTypeConstraint;
	}

	AppendableBuilder textBuilder = new AppendableBuilder();
	int interpolateDepth = 0;

	public String interpolateOpen(int filePosition, int typeSize) {

		// this method can be used by overriding methods to determin if this elment is
		// ok or not.
		// isInterpolationValidHere();

		String lastText = textBuilder.toString().intern();

		textBuilder.clear();
		interpolateDepth++;

		return lastText;
	}

	public void interpolateClose(int filePosition) {

		interpolateDepth--;
	}

	/////////////////////////
	// end of text
	/////////////////////////

	private final Map<String, ModuleDetails> activeModules = new HashMap<String, ModuleDetails>();

	// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

	public ModuleDetails moduleStart(String cannonicalLocation, String rootFolder, List<AuthorDetails> authorDetails,
			long lastUpdate, String lastSHA) {

		moduleHalt = false;
		ModuleDetails md = new ModuleDetails();
		md.authorDetails = authorDetails;
		md.mostRecentUpdate = lastUpdate;
		md.SHA = lastSHA;
		md.cannonicalLocation = cannonicalLocation;
		md.localPath = rootFolder;

		activeModules.put(cannonicalLocation, md);
		return md;
	}

	protected int identPosition() {
		return identTermStack.isEmpty() ? 0 : identTermStack.get(identTermStack.size() - 1).get();
	}

	public ModuleDetails moduleEnd(String cannonicalLocation) {
		return activeModules.remove(cannonicalLocation);

	}

	//////////////////
	// current file
	protected int lineNumberIndexPositon;
	protected int lineNumber; // current line number
	public String localPathLocation;
	public String canonicalLocation;
	private int utfBytesAdjust;

	public boolean isTemplate;
	public long templateFileLength;

	// do not modify, this boolean is part of expire logic.
	private boolean fileHalt = false; // set to true when we find a parse error and must stop clean
	private boolean moduleHalt = false;
	//////////////////

	public static boolean isFileHalted(ParseState state) {
		return state.fileHalt || state.isDisabled();
	}
	
	public int lineNumber() {
		return lineNumber;
	}
	

	protected boolean isDisabled() {
		return false; // by default this to be overridden with custom logic
	}

	public static boolean isModuleHalted(ParseState state) {
		return state.moduleHalt;
	}

	public static void clearModuleHalted(ParseState state) {
		state.moduleHalt = false;
	}

	public static void clearFileHalted(ParseState state) {
		state.fileHalt = false;
	}

	public int blockDepth() {
		return blockDepth;
	}
	
	public String fileStart(String cannonicalLocation, String localPathLocation) {
		this.lineNumber = 1;
		this.lineNumberIndexPositon = 0;
		this.localPathLocation = localPathLocation;
		this.canonicalLocation = cannonicalLocation;

		this.fileHalt = moduleHalt; // stop the rest of the module processing if one file fails
		this.nodePostProcessorStack.clear();
		this.nodePreProcessorStack.clear();
		this.tempNodePostProcessorStack.clear();
		this.utfBytesAdjust = 0;

		this.isTemplate = localPathLocation.endsWith(".tpl");
		this.templateFileLength = this.isTemplate ? new File(localPathLocation).length() : -1;

		this.blockDepth = 0;
		this.appendTerm = false;
		this.identBuilder.clear();
		this.collectingBlockLabels = false;
		this.collectingForIdentifiers = false;
		this.nestedForDepth = 0;
		this.identTermStack.clear();
		this.interpolateDepth = 0;
		this.lastBlockOpenPos = -1;
		this.numberCollector.setLength(0);
		this.paraDepth = 0;
		this.blockLabelTextIdentifier = null;
		this.textBuilder.clear();
		this.textQuoteStack.clear();
		this.utfBytesAdjust = 0;

		ModuleDetails md = activeModules.get(cannonicalLocation);
		if (null != md) {
			if (localPathLocation.startsWith(md.localPath)) {
				localPathLocation = localPathLocation.substring(md.localPath.length());
			}
		}
		return localPathLocation;
	}

	public void fileEnd(int filePosition) {
				
		// if comment is in progess mark the end of it
		if (null != singleLineComment) {			
			singleLineCommentFinish(Math.min(getDataSizeLimit(),filePosition-1)); //NOTE: does not allow for values past the file end.
		}
		
		// if heredoc is in progress mark the end of it
//		if (null!=hereDoc) {
//			hereDocClose(filePosition);
//		}
	}

	
	protected int byteLength;
	public void setDataSizeLimit(int size) {
		byteLength = size;
	}
	public int getDataSizeLimit() {
		return byteLength;
	}
	
	
	public void endOfExpr(int filePosition) {

	}

	public int incLineNo(int filePosition) {
		lineNumberIndexPositon = filePosition;
		return ++lineNumber;
	}

	public void whiteSpace(int filePosition) {

	}

	public void iterElse() {

	}

	public void iterEndIf() {

	}

	public void iterEndFor() {

	}

	public void iterIf() {

	}

	public boolean isFileDebug() {
		return isFileDebug(localPathLocation);
	}

	public int nodeProcessorStackSize() {
		return nodePostProcessorStack.size() + tempNodePostProcessorStack.size();
	}

	protected final List<TermLayer> identTermStack = new ArrayList<TermLayer>();
	private boolean lastTermWasIdx = false;
    
	public boolean lastTermWasIdx() {
		return lastTermWasIdx;
	}
	
	public void identifierIdxClose(int filePosition) {
		lastTermWasIdx = true;
	}

	public AppendableBuilderReader identifierIdxExpr(int filePosition) {
		int size = identTermStack.size();
		if (size > 0) { // only inc when this is a real identifier, not a generated array
			identTermStack.get(size - 1).inc();
		}
		AppendableBuilderReader value = finishIdentifierTerm(filePosition);
		nextIdentifier = value.toBytes();
		return value;
	}

	public AppendableBuilderReader identifierGet(int filePosition) {
		int size = identTermStack.size();
		if (size > 0) {// only inc when this is a real identifier, not a generated array
			identTermStack.get(size - 1).inc();
		}
		AppendableBuilderReader value = finishIdentifierTerm(filePosition);
		nextIdentifier = value.toBytes();

		return value;
	}

	public void identifierTopStart(int filePosition) {
		identTermStack.add(new TermLayer());

	}

	public AppendableBuilderReader identifierNominalEnd(int filePosition) {
		if (!identTermStack.isEmpty()) {
			identTermStack.remove(identTermStack.size() - 1);
		}
		AppendableBuilderReader value = finishIdentifierTerm(filePosition);

		nextIdentifier = value.toBytes();
		lastTermWasIdx = false;
		return value;
	}

	public void utfBytesAdj(int bytesPerChar) {
		utfBytesAdjust += bytesPerChar;
	}

	public int utfBytesAdj() {
		return utfBytesAdjust;
	}

	public void reportProcessingStack() {

		if (!nodePostProcessorStack.isEmpty()) {
			System.out.println("npostps: " + nodePostProcessorStack.size());
			nodePostProcessorStack.forEach(n -> {
				System.out.println(" node post stack: " + n);
			});
		}
		if (!tempNodePostProcessorStack.isEmpty()) {
			System.out.println("tnpps: " + tempNodePostProcessorStack.size());
			tempNodePostProcessorStack.forEach(n -> {
				System.out.println(" temp node stack: " + n);
			});
		}
		if (!nodePreProcessorStack.isEmpty()) {
			System.out.println("npreps: " + nodePreProcessorStack.size());
			nodePreProcessorStack.forEach(n -> {
				System.out.println(" node pre stack: " + n);
			});
		}

	}

}

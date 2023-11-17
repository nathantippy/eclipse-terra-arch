package net.terraarch.tf.parse;

import java.util.Arrays;

import net.terraarch.util.TrieParser;

public enum BlockType implements LiteralTerm, LiteralBlockParent {

	VARIABLE("variable", "var . ", true, false, new TrieParser(16, 1, false,false)),
	OUTPUT("output", "", true, false, new TrieParser(16, 1, false,false)), 
	LOCALS("locals", "local . ", true, false, null), // NOTE:
															// can
															// be
															// almost
															// anything,
															// since
															// this
															// is
															// definitive
															// not
															// referencial
	MODULE("module", "module . ", true, false, null), // NOTE: children only known from module definition

	DATA("data", "data . ", true, true, null), // NOTE: children only known from provider
	RESOURCE("resource", "", true, true, null), // NOTE: children only known from provider
	PROVIDER("provider", "", true, true, null), // NOTE: children only known from provider

	LIFECYCLE("lifecycle", "", false, false, new TrieParser(16, 1, false,false)), // special internal
	TERRAFORM("terraform", "", true, false, new TrieParser(16, 1, false,false));

	private String value;
	private byte[] bytesValue;
	public final boolean isCatigorized;

	private TrieParser childrenParser;
	private boolean topOnly;

	public boolean isEqual(byte[] bytes) {
		if (null == bytes && null == bytesValue) {
			return true;
		}
		if (null == bytes || null == bytesValue) {
			return false;
		}
		return Arrays.equals(bytes, bytesValue);
	}

	public String value() {
		return value;
	}

	public String label() {
		return outlineLabel;
	}

	private String outlineLabel;
	public final int categoryLength;

	BlockType(String value, String outlineLabel, boolean topOnly, boolean isCatigorized, TrieParser children) {
		this.value = value;
		this.childrenParser = children;
		this.topOnly = topOnly;
		this.bytesValue = null==value ? null : value.getBytes();
		this.isCatigorized = isCatigorized;
		this.outlineLabel = outlineLabel;
		this.categoryLength = null==outlineLabel ? 0 : outlineLabel.replaceAll(" . ", ".").length();
	}

	@Override
	public TrieParser exclusiveValidChildren() {
		return childrenParser;
	}

	@Override
	public boolean topOnly() {
		return topOnly;
	}

}
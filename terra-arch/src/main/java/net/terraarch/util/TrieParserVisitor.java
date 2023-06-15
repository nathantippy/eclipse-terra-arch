package net.terraarch.util;

public interface TrieParserVisitor {

	void visit(byte[] backing, int length, long value);
	
}

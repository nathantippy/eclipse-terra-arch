package net.terraarch.terraform.parse;

import net.terraarch.util.TrieParser;

public interface LiteralBlockParent {

 	TrieParser exclusiveValidChildren();
 	boolean topOnly();
 	
}

package net.terraarch.tf.parse;

import net.terraarch.util.TrieParser;

public interface LiteralBlockParent {

 	TrieParser exclusiveValidChildren();
 	boolean topOnly();
 	
}

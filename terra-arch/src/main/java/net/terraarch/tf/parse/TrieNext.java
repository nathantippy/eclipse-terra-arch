package net.terraarch.tf.parse;

import net.terraarch.util.TrieParser;

public class TrieNext {
	
	public static TrieNext NO_OP = new TrieNext(null);
	
	public final int valueIfNotFound;
	public final TrieParser trie;
    public final Exception builtAt;
	
	public TrieNext(TrieParser trie) {
		this.trie = trie;
		this.valueIfNotFound = -1;
		this.builtAt = new Exception();
	}
	
	public TrieNext(TrieParser trie, int valueIfNotFound) {
		this.trie = trie;
		this.valueIfNotFound = valueIfNotFound;
		this.builtAt = new Exception();
	}	
}

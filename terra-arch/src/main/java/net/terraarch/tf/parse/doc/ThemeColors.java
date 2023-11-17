package net.terraarch.tf.parse.doc;

public enum ThemeColors { //NOTE: do not change order, ordinal position is used.
	vivid("Best for dark themes"),
	muted("Best for bright themes");

	String description;
	ThemeColors(String description) {
		this.description = description;
	}
	public String description() {
		return this.description;
	} 		
}
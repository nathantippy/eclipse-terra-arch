package net.terraarch.tf.parse.version;

public interface VersionVisitor {

	void accept(int[] values, String tagLabel);

}

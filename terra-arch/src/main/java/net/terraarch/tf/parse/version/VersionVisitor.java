package net.terraarch.terraform.parse.version;

public interface VersionVisitor {

	void accept(int[] values, String tagLabel);

}

package net.terraarch.terraform.parse.version;

import java.io.Serializable;

@FunctionalInterface
public interface VersionOperatorBehavior extends Serializable {

	boolean isValid(int[] constraintVer, String constraintSuff, int[] input, String inputSuff);

}

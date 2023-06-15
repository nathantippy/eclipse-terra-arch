package net.terraarch.refactor.rename;

public interface FileChangeVisitor {

	public void newChange(int offset, int length, String newText);
	
}

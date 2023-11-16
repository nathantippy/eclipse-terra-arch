package net.terraarch.terraform.structure;

@FunctionalInterface
public interface RecordVisitor<T> {

	boolean accept(T sdr);

}

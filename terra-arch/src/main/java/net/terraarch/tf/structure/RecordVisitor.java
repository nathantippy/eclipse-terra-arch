package net.terraarch.tf.structure;

@FunctionalInterface
public interface RecordVisitor<T> {

	boolean accept(T sdr);

}

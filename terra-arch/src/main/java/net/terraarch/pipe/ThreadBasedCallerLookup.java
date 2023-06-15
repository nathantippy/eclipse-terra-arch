package net.terraarch.pipe;

public interface ThreadBasedCallerLookup {

	int getCallerId();
	int getProducerId(int pipeId);
	int getConsumerId(int pipeId);

}

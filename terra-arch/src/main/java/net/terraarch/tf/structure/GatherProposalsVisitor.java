package net.terraarch.terraform.structure;

import net.terraarch.util.AppendableBuilderReader;
import net.terraarch.util.TrieParserVisitor;

public interface GatherProposalsVisitor extends TrieParserVisitor {

	//return true to visit all the known instances of this type
	boolean foundInDocument(GatherProposals<?> that, int endPos, AppendableBuilderReader value, int preBytes, GatheredFieldType type);
	boolean foundInDocument(GatherProposals<?> that, int typeEndPos, AppendableBuilderReader typeValue, int endPos, AppendableBuilderReader value, int preBytes, GatheredFieldType type);
		
	void activeRecord(StructureDataFile sdr);
	void consume(GatherProposals<?> gatherProposals, AppendableBuilderReader value, int preBytes, byte[] bytes, int length);
}

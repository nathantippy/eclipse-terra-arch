package net.terraarch.tf.parse;

import java.util.Arrays;

import net.terraarch.tf.parse.ParseState.NAMESPACES;
import net.terraarch.tf.structure.IndexNodeDefinition;
import net.terraarch.tf.structure.StructureDataModule;
import net.terraarch.util.AppendableBuilderReader;

public class TFExpressionReference extends TFExpressionChild {

	
	private final NAMESPACES nameSpace; 
	private final BlockType type;
	
	private final int position;
	
	private final int endPos2;
	private final byte[] category; 
	
	private final int endPos1;
	private final byte[] name;
    
	private final StructureDataModule structureDataModule;
	
	public TFExpressionReference(TFExpression parent, 
			                     NAMESPACES nameSpace, BlockType type, 
			                     int position, 
			                     int endPos2, AppendableBuilderReader reader2, 
			                     int endPos1, AppendableBuilderReader reader1, 
			                     StructureDataModule structureDataModule) {
		super(parent);
		
		this.nameSpace = nameSpace; 
		this.type = type; 
        this.position = position; 
        this.endPos2 = endPos2; 
        this.category = null==reader2 ? new byte[0] : reader2.toBytes(); 
        this.endPos1 = endPos1; 
        this.name = reader1.toBytes();
		this.structureDataModule = structureDataModule;
        
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((nameSpace == null) ? 0 : nameSpace.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TFExpressionReference other = (TFExpressionReference) obj;
		if (nameSpace != other.nameSpace)
			return false;
		if (type != other.type)
			return false;
		
		if (type.isCatigorized) {
			if (!Arrays.equals(this.category, other.category)) {
				return false;
			}			
		}
		
		//late by design to capture all details
		//also late because building the tree must be very fast
		IndexNodeDefinition thisDef = this.def();
		IndexNodeDefinition otherDef = other.def();
		
		//TODO: AAAAA, WE MUST JUMP TO THE DEFINITION AND CHECK ITS EQUALS...
		
		
		//TODO: AA, must go up the tree and find the matching for local variable in use at this posion.
		
		
		return true;
	}


	private IndexNodeDefinition def() {
		return StructureDataModule.lookupDef(structureDataModule, type, category, name);
	}

	

	
	
}

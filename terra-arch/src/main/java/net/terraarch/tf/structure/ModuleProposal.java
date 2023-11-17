package net.terraarch.tf.structure;

import static net.terraarch.tf.structure.StructureDataModule.lookupDef;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ModuleProposal {

	public final int levelNumber;
	public final IndexNodeDefinition current;
	public final List<IndexNodeDefinition> insideDefs;
	
	public final Set<List<ModuleProposal>> greaters = new HashSet<>();
	public final Set<List<ModuleProposal>> lessers = new HashSet<>();
	
	
	public ModuleProposal(StructureDataModule sdm, int levelNumber, IndexNodeDefinition v) {
		this.levelNumber = levelNumber;
		this.current = v;
		this.insideDefs = new ArrayList<>();//list so they stay in order
		StructureDataModule.visitUsagesInsideDefinition(sdm, v.type(), v.category(), v.name(), in->{
			
			//TODO: AAAA, this order is a concern since the aruments are each named..
			
			insideDefs.add(StructureDataModule.lookupDef(sdm, in.type(), in.category(), in.name())); 
			return true;
		});		
	}
	
	public String toString() {
		StringBuilder builder = new StringBuilder();
		
		int x = levelNumber;
		while (--x>=0) {
			builder.append("  ");
			
		}
		if(0==levelNumber) {
			builder.append("GOAL: ");
		} else {
			builder.append(levelNumber);
		}
		
		builder.append(" ").append(current.toString().replace(" . ","."));		
		
		if (0==levelNumber) {
			builder.append(" ");
			
			for(List<ModuleProposal> list: lessers) {
				builder.append("supersetOfID:").append(list.hashCode()).append(" ");
			}
			
			for(List<ModuleProposal> list: greaters) {
				builder.append("subsetOfID:").append(list.hashCode()).append(" ");
			}
			
		}		
		
		return builder.toString();
		
	}
	
	public void buildNext(List<ModuleProposal> target, StructureDataModule sdm, int level, Set<IndexNodeDefinition> doneDefs, Set<IndexNodeDefinition> newForLevel) {
    		scanTree(sdm, level, doneDefs, newForLevel, target, current);	
    
	}

	public void scanTree(StructureDataModule sdm, int level, Set<IndexNodeDefinition> doneDefs,
			             Set<IndexNodeDefinition> newForLevel, List<ModuleProposal> results,
			             IndexNodeDefinition local) {
		
		
		StructureDataModule.visitUsagesInsideDefinition(sdm, local.type(), local.category(), local.name(), v->{			
			//required by the lower level
			IndexNodeDefinition nextLevelDef = lookupDef(sdm, v.type(), v.category(), v.name());
			
			if (null!=nextLevelDef) {
				if (!newForLevel.contains(nextLevelDef) //not already done in this level pass
				 && !doneDefs.contains(nextLevelDef)) { //not already done by any previous pass
					//only create this one if all its reqirments are already defined.
					Set<IndexNodeDefinition> required = captureRequired(sdm, nextLevelDef);
					if (required.isEmpty() || doneDefs.containsAll(required)) {  //all dependencies are already done  				
						results.add(new ModuleProposal(sdm, level, nextLevelDef));
						newForLevel.add(nextLevelDef);
					} else {
						Set<IndexNodeDefinition> loopProtect = new HashSet<>();
						loopProtect.addAll(doneDefs);
						loopProtect.add(local);
						scanTree(sdm, level, loopProtect, newForLevel, results, nextLevelDef);
					}
				}
			} else {
				System.out.println("ERROR unable to find def for: "+v+" used by "+local);
			}
			return true;
		});
	}

	private Set<IndexNodeDefinition> captureRequired(StructureDataModule sdm, IndexNodeDefinition node) {
		HashSet<IndexNodeDefinition> required = new HashSet<IndexNodeDefinition>();
		if (null!=node) {
			StructureDataModule.visitUsagesInsideDefinition(sdm, node.type(), node.category(), node.name(), v->{
				required.add( StructureDataModule.lookupDef(sdm, v.type(), v.category(), v.name()));
				return true;
			});	
		}
			
		return required;	
	}

		
	public void addRollInto(List<ModuleProposal> greater) {
		this.greaters.add(greater);
	}

	public void addRollFrom(List<ModuleProposal> lesser) {
		this.lessers.add(lesser);
	}
		
}

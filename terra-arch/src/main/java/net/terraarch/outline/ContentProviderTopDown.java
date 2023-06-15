package net.terraarch.outline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.eclipse.jface.viewers.ITreeContentProvider;

import net.terraarch.util.Appendables;
import net.terraarch.terraform.structure.IndexNodeDefinition;
import net.terraarch.terraform.structure.IndexNodeUsage;
import net.terraarch.terraform.structure.RecordVisitor;
import net.terraarch.terraform.structure.StructureDataModule;

public class ContentProviderTopDown implements ITreeContentProvider  {

// TODO:  AAAAAA,  check for lock case in the edit loop.
// TODO:  AAAAAA, relese with tf 1.0 version.	
	
	public final StructureDataModule sdm;
	
	public ContentProviderTopDown(StructureDataModule sdm) {
		this.sdm = sdm;
	}	

	@Override
	public Object[] getElements(Object ignore) {

		final List<OutlineNode> results = new ArrayList<OutlineNode>();
		
		sdm.visitRecords(sdr-> {			
			sdr.visitNodeDefs(def-> {
				results.add(new OutlineNode(null, def));
				return true;
			});
			
			return true;
		});
		 		
 		Collections.sort(results);
 		return results.toArray(new OutlineNode[results.size()]);
	}
	
	@Override
	public Object getParent(Object node) {
		return ((OutlineNode)node).parent;
	}

	
	@Override
	public boolean hasChildren(Object node) {
		if (node instanceof OutlineNode) {
			OutlineNode outlineNode = (OutlineNode)node;
			if (outlineNode.isLooped) {
				return false;
			}
			return StructureDataModule.hasUsagesInsideDefinition(sdm, 
					   outlineNode.type, outlineNode.category, outlineNode.name);
		} else {
			throw new UnsupportedOperationException("unsupported class: "+node.getClass());
		}
	}
	
	@Override
	public Object[] getChildren(Object node) {
		//System.out.println("call to has children: "+obj);
		List<OutlineNode> results = new ArrayList<OutlineNode>();
		
		//NOTE: this apears to be the logic for the reverse tree..
		if (node instanceof OutlineNode) {
			buildChildrenList(sdm, (OutlineNode)node, results);
		} else {
			throw new UnsupportedOperationException(" unsupported class: "+node.getClass() );
		}
		Collections.sort(results); ///we would like the tree to show the fields in the order in which they appear
		return results.toArray(new OutlineNode[results.size()]);
	}

	private static void buildChildrenList(StructureDataModule sdm, OutlineNode parentOutlineNode, List<OutlineNode> results) {
		if (!parentOutlineNode.isLooped) {
						
			RecordVisitor<IndexNodeUsage> recordVisitor = (indexNodeUsage) -> {
				results.add( new OutlineNode(parentOutlineNode, indexNodeUsage) );
				return true;
			};
			StructureDataModule.visitUsagesInsideDefinition(sdm, parentOutlineNode.type, 
					                 parentOutlineNode.category, parentOutlineNode.name, 
					                 recordVisitor
					);
		}
	}

	public static StringBuilder buildIndexOfCount(StructureDataModule sdm, final IndexNodeUsage indexNodeUsage) {
		final AtomicInteger count = new AtomicInteger();
		 final AtomicInteger instance = new AtomicInteger();
		 StructureDataModule.visitDefUsages(sdm, indexNodeUsage.type(), indexNodeUsage.category(), indexNodeUsage.name(), v-> {
			count.incrementAndGet();
			if (indexNodeUsage == v) {
				instance.set(count.intValue());
			}
			return true;
		 });

		 StringBuilder instanceId = new StringBuilder();
		 if (count.get()>1) {
			 Appendables.appendValue( 
					instanceId.append("("),count.get()).append(")");
		 }
		return instanceId;
	}

}

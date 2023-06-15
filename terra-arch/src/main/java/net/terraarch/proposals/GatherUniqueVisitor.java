package net.terraarch.proposals;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;

import net.terraarch.util.AppendableBuilderReader;

import net.terraarch.terraform.parse.FieldNamesParse;
import net.terraarch.terraform.structure.GatherProposals;
import net.terraarch.terraform.structure.GatherProposalsVisitor;
import net.terraarch.terraform.structure.StructureDataFile;
import net.terraarch.terraform.structure.walker.GatherInBoundProposals;
import net.terraarch.terraform.structure.walker.GatherProposalsAdapter;

import net.terraarch.TerraArchActivator;

public class GatherUniqueVisitor extends GatherProposalsAdapter {

	    public Set<String> dups = new HashSet<String>();
		
	    public final List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
	    public final List<IContextInformation> context   = new ArrayList<IContextInformation>();
		
	    private final boolean enabled;
	    private final int cursorPosition;
	    
	    public GatherUniqueVisitor(int cursorPosition) {
	    	this.enabled = true;
	    	this.cursorPosition = cursorPosition;
	    }
	   
		@Override
		public void consume(GatherProposals<?> gatherProposals, AppendableBuilderReader value, int preBytes, byte[] bytes, int length) {
			if (!enabled) {
				return;//never add any proposals if this feature is not enabled;
			}
			
			
			if (length >= preBytes && (0==preBytes || value.startsWith(bytes, preBytes)) ) {
				String replacementString = new String(bytes, 0, length); //preBytes, length-preBytes );
				
				
				//System.out.println("accepted: "+replacementString+" matching "+value);
				if (!dups.contains(replacementString)) {
									
					Image image = null; 
					IContextInformation contextInformation = null;
					
					int callReplacementOffset = cursorPosition - preBytes;
					
					int cursorPosAfterApply = replacementString.length();
					//System.out.println("added new proposal: "+replacementString+"  "+endPos+"  "+callReplacementOffset+" "+ value.toString().length()+" "+cursorPosAfterApply);
					CompletionProposal completionProposal = new CompletionProposal(
							               replacementString, 
							               callReplacementOffset, 
							               value.toString().length(), 
							               cursorPosAfterApply
							               ,image, replacementString /*+"     "+description+" "+proposalDetails*/
							               , contextInformation, null
					         );
					//Point point = completionProposal.getSelection(document);
					//System.out.println("add proposal: "+replacementString+" "+callCurrentPos+" point: "+point);
					
					proposals.add(completionProposal);
					
					dups.add(replacementString);
				}
				
			}
		}
	
}

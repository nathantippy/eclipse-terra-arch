package net.terraarch;

import java.io.File;

import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.core.filebuffers.IDocumentSetupParticipantExtension;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.text.IDocument;

import net.terraarch.index.IndexModuleFile;


public class ValidatorDocumentSetupParticipant implements IDocumentSetupParticipant, IDocumentSetupParticipantExtension {

	private static final ILog logger = Platform.getLog(ValidatorDocumentSetupParticipant.class);

	
	//this update is outside the text editor and can allow for heaver work loads
	//the display code should avoid heavy work and simply use this  information as indexed
	
	//Example tasks:
	//            index all the known fields in the project
	//           find matching module sources if checked out as peers in project
	//           check out module source and add it to the known dictionary
	
	
	@Override
	public void setup(IDocument document) {	
		throw new UnsupportedOperationException("Eclipse should not have called this since we implemented IDocumentSetupParticipantExtension");
	}
	

	@Override
	public void setup(IDocument document, IPath location,  LocationKind locationKind) {
		
		
		///System.out.println("document setup: "+location);
		try {
			if (locationKind == LocationKind.IFILE) {
				IFile file = ResourcesPlugin.getWorkspace()
							               .getRoot()
							               .getFile(location);
				if (null != file) {
					//System.out.println("added listener 1");
					document.addDocumentListener(new IndexModuleFile(file));
				} else {
					//System.out.println("added listener 2");
					//this file is NOT in our workspace or a project
					File jFile = location.toFile();
					File folder = jFile.getParentFile();
					document.addDocumentListener(new IndexModuleFile(folder, jFile));
				}			
			} else {
				if (location!=null) {
					//System.out.println("added listener 3");
					//this file is NOT in our workspace or a project
					File jFile = location.toFile();
					File folder = jFile.getParentFile();
					document.addDocumentListener(new IndexModuleFile(folder, jFile));
				} //else {
				//	System.out.println("no listener");
				//}
			}
		} catch (Throwable t) {	
			t.printStackTrace();
			logger.error("Unable to build document listener "+t.getMessage(), t);
		}
		
	}

}

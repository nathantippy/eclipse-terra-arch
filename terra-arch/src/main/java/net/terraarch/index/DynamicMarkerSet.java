package net.terraarch.index;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IMarkerResolutionGenerator;

import net.terraarch.util.AppendableBuilder;

import net.terraarch.tf.parse.ParseBuffer;
import net.terraarch.tf.parse.doc.DocumentTokenMap;
import net.terraarch.tf.parse.doc.ProviderConstraintImpl;
import net.terraarch.tf.structure.IndexNodeUsage;
import net.terraarch.tf.structure.StructureDataFile;
import net.terraarch.tf.structure.StructureDataModule;

import net.terraarch.TerraArchActivator;
import net.terraarch.preferences.TerraPreferences;

public class DynamicMarkerSet {
	
	private static final ILog logger = Platform.getLog(DynamicMarkerSet.class);

	public static void deleteOtherMarkers(IFile file, int keepRevision) {

		try {
			IMarker[] markers = file.findMarkers("net.terraarch.problemmarker", true, IResource.DEPTH_INFINITE);
			int x = markers.length;
			while (--x>=0) {
				IMarker m = markers[x];
				if ((null!=m) && (m.getAttribute("TF-REV", -1)!=keepRevision)) {
					//System.out.println("found rev "+m.getAttribute("TF-REV", -1)+" looking for "+keepRevision+" so deleting "+m.getAttribute(IMarker.MESSAGE, ""));
					m.delete();
					markers[x] = null;				
				}
			}
		} catch (Throwable e) {
			logger.info("Unable to delete markers: ",e);
		}
		
	}
	
	/////////// IMPORTANT LOCK TO PREVENT ECLIPSE HANG ////////////////////////////////////
	// methods getAttribute and createMarker can deadlock if called from different threads
	// to avoid this we use this object to syncronize use of "newMarker"
	private static Object markerLock = new Object(); 
	///////////////////////////////////////////////////////////////////////////////////////
	
	/*
	 * IMarker.SEVERITY_
	 */
	public static void newMarker(final IFile file, String message, 
			            int charStart, int charEnd, int lineNumber, int revision,
			            boolean addNew, int severity, IMarkerResolutionGenerator qfg) {
		
		//Critical: newMarker is called from different threads and we must prevent deadlock so we sync.
		synchronized (markerLock) {
			try {
				if (file.exists()) {			
					IMarker[] markers = file.findMarkers("net.terraarch.problemmarker", true, IResource.DEPTH_INFINITE);
					int x = markers.length;
					while (--x >= 0) {
						IMarker m = markers[x];
						
						if (null!=m 					
							&& m.getAttribute(IMarker.MESSAGE, "").equals(message)
							&& m.getAttribute(IMarker.CHAR_START, -1)==charStart
							&& m.getAttribute(IMarker.CHAR_END, -1)==charEnd
								) {
							//found so bump up revision
							try {
								m.setAttribute("TF-REV", revision);
								//System.out.println(revision+"  update maker with on "+file+" "+message+" line "+lineNumber);
								return;
							} catch (CoreException e) {
								logger.info("unable to refresh marker "+e.getMessage());
								try {
									m.delete();
								} catch (Throwable t) {	
									//ignore
								}
								//upon error we fall out and add a new one
							}
						} 
					}
				
					if ( addNew) {//only add this mark when true	
						
						//run on the display thread since we are creating a UI artifact
						Display.getDefault().asyncExec(new Runnable() {
							public void run() {	
								try {
									IMarker marker = file.createMarker("net.terraarch.problemmarker"); //will show up in problems
									
									if (marker.exists()) {
										marker.setAttribute("TF-REV", revision);
										marker.setAttribute(IMarker.SEVERITY, severity);
										marker.setAttribute(IMarker.MESSAGE, message);
										if (lineNumber>=0) { //we do not always use this field.
											marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
										}
										marker.setAttribute(IMarker.CHAR_START, charStart);
										marker.setAttribute(IMarker.CHAR_END, charEnd);
										//System.out.println(revision+"  create new maker with on "+file+" "+message+" line "+lineNumber);
										
										if (null!=qfg) {
											marker.setAttribute("QuickFixGenerator", qfg);					
										}
									} else {
										logger.info("Unable to create new marker: "+message+" on line: "+lineNumber+" in file: "+file.getName());
									}	
								} catch (Throwable e) {
									logger.info("Unable to create marker: "+message+" on line: "+lineNumber+" in file: "+file.getName(),e);
								}
							}
							
						});
						
					}
				}
				
			} catch (Throwable e) {
				logger.info("Unable to create marker: "+message+" on line: "+lineNumber+" in file: "+file.getName(),e);
			}
		}
		
	}


    private static boolean warningShown = false;
    
	public static DocumentTokenMap recordNewMarks(final int markerRev, IFile ifile, byte[] data,
			StructureDataModule module, final List<IndexNodeUsage> looped) {
		
		ProviderConstraintImpl providerConstraintImpl = new ProviderConstraintImpl(TerraArchActivator.getDefault().storageCache());
		boolean isDisabled = false;
		final DocumentTokenMap tokenMap = new DocumentTokenMap(module, isDisabled, providerConstraintImpl);
		
		

		 
		ParseBuffer marksBuffer = new ParseBuffer();	
		
		marksBuffer.tokenizeDocument(data, tokenMap);
	
		
		IEclipsePreferences node = InstanceScope.INSTANCE.getNode(TerraArchActivator.PLUGIN_ID);			
		//get key but if its not there then use the demo key.

				
		tokenMap.tc.tokenBlocks().forEach(tb -> { 
			    if (tb.hasErrorMessages() && null!=tb.selectedErrorMessage()  ) {
				   DynamicMarkerSet.newMarker(ifile,tb.selectedErrorMessage(),tb.start,tb.stop,tb.lineNumber,markerRev, true, IMarker.SEVERITY_ERROR,null);
				}}
				);
		
		looped.forEach(ind->{
			assert(ifile.equals( TerraArchActivator.getIFile(ind.sdr()) ));
			
			StringBuilder message = new StringBuilder();

			message.append("looping segment:  ");
			ind.getParentDef().buildStringName(message);
			message.append(" -> ");
			ind.buildStringName(message);
			
			DynamicMarkerSet.newMarker(ifile, message.toString(),
										ind.blockPositionStart(),
										ind.blockPositionEnd(),
										-1,	markerRev, true, IMarker.SEVERITY_ERROR,null);
			
		});
		
		
		deleteOtherMarkers(ifile, markerRev);//remove the old
		return tokenMap;
	}

	public static void recordFileMarks(final int markerRev, StructureDataModule module, final StructureDataFile sdr, IFile ifile) {
			
			if (null!=ifile) {
				try {
					AppendableBuilder builder = new AppendableBuilder();
					builder.consumeAll(ifile.getContents());
					//NOTE:this does a full parse to discover possible versions defined and may other things
					DocumentTokenMap results = recordNewMarks(markerRev, ifile, builder.toBytes(), module, sdr.loopedNodes());
					
					sdr.setTerraformConstraints(results.terraformVersionConstraints());
					sdr.setDefinedProviders(results.definedProviders());
					
				} catch (Throwable e) {
					logger.error("unable to read IFile "+ifile+" "+e.getMessage());
				}
			}
	
	}

}

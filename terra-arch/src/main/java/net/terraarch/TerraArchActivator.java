package net.terraarch;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.eclipse.core.runtime.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
//import org.eclipse.equinox.p2.repository.metadata.IMetadataRepositoryManager;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import net.terraarch.outline.TerraArchOutlineView;
import net.terraarch.preferences.TerraPreferences;
import net.terraarch.terraform.parse.ParseState;
import net.terraarch.terraform.structure.StorageCache;
import net.terraarch.terraform.structure.StructureDataFile;
import net.terraarch.terraform.structure.StructureDataModule;
import net.terraarch.terraform.structure.StructureDataModuleManager;
import net.terraarch.util.NetworkUtil;
import net.terraarch.util.TrieParserReader;


// for docs
// https://help.eclipse.org/2019-06/index.jsp?topic=%2Forg.eclipse.platform.doc.isv%2Fguide%2Feditors_genericeditor.htm


/**
 * The activator class controls the plug-in life cycle
 */
public class TerraArchActivator extends AbstractUIPlugin implements BundleActivator{
	
	public static final ILog logger = Platform.getLog(TerraArchActivator.class); 
	
	public static final String LICENSE_VERIFICATION = "License Verification";

	// The plug-in ID
	public static final String PLUGIN_ID = "net.TerraArch.editor"; //$NON-NLS-1$

	// The shared instance
	public static TerraArchActivator plugin;
	//private BundleContext context;
	//private TrayItem trayItem;
	//private Image image;

	
	public ReentrantLock pluginLock = new ReentrantLock();
	
    public final String identifier;
 
    final boolean isTest = true;
    final boolean isEternal = true;
	long paidUntilSeconds = System.currentTimeMillis()/1000L;
	public final boolean requestLicenseAgreement = false;
	public final boolean warnInstallsCount = false;
	
	public static final StructureDataModuleManager sdmm = StructureDataModuleManager.instance;
	
	private StorageCache sCache; 
	
    public StorageCache storageCache() {  
       if (null==sCache) {    
    	    buildStorageCache();
       }    	
	   return sCache;    	
    }

	private void buildStorageCache() {
		File dbFile = null;
		try {
			//only place we can save cached info
			File baseFolder = TerraArchActivator.getDefault().getStateLocation().toFile();	   
			dbFile = null==baseFolder ? null : new File(baseFolder, "providers.mv.db");
			//logger.info("FYI: db location: "+dbFile);
			sCache = StorageCache.instance(dbFile);
		} catch (Exception ex) {
		    logger.error("unable to load database: "+dbFile, ex);
		    //leaving the sCache as null;
		}
	}
    
		
    ///////////////////////////
	//from account
	public String email;
	public String company;
	public String name;
	public String product;
	public boolean autoRenew;
	public long nextChargeInSeconds;
	public int installs;
	public int copyright_version=-1;
	public int license_version=-1;
	///////////////////////////


	
	/*
	 *       marks as warnning that the module should provide provider range >= something
	 *       until the new advaned feature is done assume the latest version is ok.
	 *       
	 *       provider "aws " {     }  //can have version but its not recommended.
	 *       resource "" {
	 *          provider = aws.west
	 *       }
	 *       moudle ""  {
	 *       	source=""
	 *          providers = {
	 *          	aws = aws.west
	 *          }
	 *       }
	 *       
	 *       terraform {
	 *           required_providers {
	 *              my_http = {
	 *           		source = "NAMESPACE/NAME"  //  source-addresses  HOSTNAME/NAMESPACE/NAME
	 *              	version = "~> 2.0" 
	 *              }
	 *           } // registry.terraform.io is the default HOSTNAME and matches our current db.
	 *       
	 *       }
	 *       provider "my_http" {
	 *       
	 *       }
	 *       TODO: BB new feature, determine which versions supports all we have used.
	 *       given all resources used find the oldest version supporting them.
	 *       populate DB with new field, introduced with version XXXX
	 *      
	 */

	public final static int LICENSE_VERSION   = 1;
	public final static int COPYRIGHT_VERSION = 0;

	private final static IPartListener partListenerForCursor = new CursorPartListener();
		
	

	
	/**
	 * The constructor
	 */
	public TerraArchActivator() {
		//System.out.println("activator has run");
		this.identifier = UUID.nameUUIDFromBytes(NetworkUtil.getAllMacs()).toString();
		
		IMAGE_EXPAND_ALL = imageDescriptorFromPlugin("org.eclipse.ui", "icons/full/elcl16/expandall.png");
		IMAGE_COLLAPSE_ALL = imageDescriptorFromPlugin("org.eclipse.ui", "icons/full/elcl16/collapseall.png");
		
			
		PlatformUI.getWorkbench().addWindowListener(new WindowPagePartListener(partListenerForCursor));
	
		
		//////////////////////
		//this block only required for the existing window which is open upon startup
		//it does not call activate or open with the above
		////////////////////
		try {
			IWorkbenchWindow activeWorkbenchWindow = null;
			activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			if (null != activeWorkbenchWindow) {
				for(IWorkbenchPage page: activeWorkbenchWindow.getPages()) {
					page.removePartListener(partListenerForCursor);
					page.addPartListener(partListenerForCursor);
				}
			}
		} catch (Throwable t) {
			logger.error("add part listener on startup",t);
		}
		
	}

	public final ImageDescriptor IMAGE_EXPAND_ALL;
	public final ImageDescriptor IMAGE_COLLAPSE_ALL;
		
	private TerraArchOutlineView activeOutlineView;
	public void setActiveOutlineView(TerraArchOutlineView activeOutlineView) {
		this.activeOutlineView = activeOutlineView;
	}	
	public TerraArchOutlineView getActiveOutlineView() {
		return activeOutlineView;
	}
	
//
//	public RecommendationView activeRecommendationView;
//	public void setActiveRecommendationView(RecommendationView view) {
//		this.activeRecommendationView = view;
//	}
//	public RecommendationView getActiveRecommendationView() {
//		return activeRecommendationView;
//	}
	
	
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		//this.context = context;
		plugin = this;


	}
	
	
	public boolean isEternal() {
		return isEternal;
	}	
	
	public long paidUntilSeconds() {
		return paidUntilSeconds;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		try {	
			plugin = null;
			context = null;
			super.stop(context);
		} catch (Throwable t) {
			//ignore we are in shutdown.
		}
//		if (trayItem != null) {
//			Display.getDefault().asyncExec(trayItem::dispose);
//		}
//		if (image != null) {
//			Display.getDefault().asyncExec(image::dispose);
//		}
		
	}

	//TODO: B add to the preferences dialog to choose, workspace, select or use temp.
	//      until then the normal get will just use temp.
	public void setCheckoutFolder(File target) {
		try {
			IEclipsePreferences pref = InstanceScope.INSTANCE.getNode(TerraArchActivator.PLUGIN_ID);
	
			if (!target.exists()) {		
				logger.error("selected target folder: "+target+" does not exist");
			} 
			pref.put("checkout-folder", target.getAbsolutePath());
		} catch (Throwable t) {
			logger.error("setCheckoutFolder",t);
		}
	}
	
	//if anything goes wrong this will create a temp folder for use.
	public File getCheckoutFolder() {
		try {
			IEclipsePreferences pref = InstanceScope.INSTANCE.getNode(TerraArchActivator.PLUGIN_ID);
		    String result = pref.get("checkout-folder", "");
		    if (result.length()>0) {
		    	File f = new File(result);
		    	if (f.exists()) {
		    		return f;
		    	}// else {
		    	//	logger.error("stored target folder: "+f+" does not exist");
		    		//we fall out here and create a new temp folder	
		    	//}
		    } 
		    
		    try {
				result = Files.createTempDirectory("terraarch_working").toAbsolutePath().toString();
			} catch (IOException e) {
				logger.error("unable to create checkout folder",e);
				return null;
			}
	    	pref.put("checkout-folder", result);
	    	return new File(result);
		} catch(Throwable t) {
			logger.error("getCheckoutFolder",t);
			return null;
		}
	}
		
	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static TerraArchActivator getDefault() {
		return plugin;
	}

	
	static StructureDataFile terraformTargetFile = null;
	private static String[] preferredTerraformVersionFiles = new String[] {"terraform.tf","main.tf","variables.tf"};
	static Map<String, List<ProviderFileVersionDTO>> missingProviderVersions = new HashMap<>();
	
	
	static int fileImportance(String fileText) {
		int x = preferredTerraformVersionFiles.length;
		while  (--x >= 0) {
			if (fileText.endsWith(preferredTerraformVersionFiles[x])) {
				return x;
			}
		}		
		// the low value is the better choice
		return Integer.MAX_VALUE;
	}
		
	static final TrieParserReader primaryActionsReader = new TrieParserReader(true);
	
	/////////////////////////////////////////////////////////////
	
	public static final AtomicInteger markerRevsion = new AtomicInteger();
	
	///////////////////////////////////
	///////////////////////////////////
	
	public static final byte DEEP_ACTION_OUTLINE = 0;
	public static final byte DEEP_ACTION_RECOMMENDATION = 1;
	static final int MAX_ACTIONS = 128;	
	
	/////////////////////////////////////////////////////////////
	Consumer<StructureDataModule>[] deepReviewActions = new Consumer[MAX_ACTIONS];
	public void clearDeepReviewActions() {
		Arrays.fill(deepReviewActions, null);
	}	
	public void addDeepReviewAction(byte id, Consumer<StructureDataModule> con) {
		deepReviewActions[id]=con;
	}	
	public int getTextColorsId() {
		return InstanceScope.INSTANCE.getNode(TerraArchActivator.PLUGIN_ID).getInt(TerraPreferences.KEY_TEXT_COLORS, 0); 
	}
		
	

	public static IFile getIFile(StructureDataFile sdr) {
		try {
			IFile result = null!=sdr ? ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(
						              Path.fromOSString(sdr.getFile().getAbsolutePath())) : null;
			if (result!=null && result.exists()) {
					//slow check to confirm that this file is still valid
					if ( -1 != result.getContents().read() ) {			
						return result;
					} else {
						return null;
					}
			} else {
				return null;
			}
		} catch (Throwable t) {
			return null;
		}
			
	}
	    
    public final void versionDTOsClear(String providerKey, StorageCache storageCache) {
    	storageCache.versionsCache.remove(providerKey);
    }
   
	
}

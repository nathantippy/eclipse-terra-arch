package net.terraarch.outline;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import net.terraarch.terraform.parse.BlockType;
import net.terraarch.terraform.structure.StructureDataModule;

import net.terraarch.TerraArchActivator;
import net.terraarch.index.IndexModuleFile;
import net.terraarch.menu.JumpToDef;

public class TerraArchOutlineView extends Page implements IContentOutlinePage, ISelectionChangedListener { 
	   

	public static final ILog logger = Platform.getLog(TerraArchOutlineView.class);
		
	final IndexModuleFile imf;
    OutlineLabelProvider labelProvider;
	
	public OutlineNode lastNode = null;

	
    Control composite;
    TreeViewer treeViewer;

    private ListenerList selectionChangedListeners = new ListenerList();
	
	public TerraArchOutlineView(IndexModuleFile imf) {
		super();
		this.imf = imf;
		
		if (null==imf) {
			throw new NullPointerException();
		}
		if (null==imf.module) {
			throw new NullPointerException();
		}
		
	//	System.out.println("new outline view");
	}
	

	public final OutlineReactiveJobState reactiveCursorJobState = new OutlineReactiveJobState(this);
	
	
	public StructureDataModule module() {
		return imf.module;
	}
	
	public boolean matchesModule(AbstractDecoratedTextEditor adaptableObject) {
		try {
			if (null!=adaptableObject) {
				IEditorInput editor = adaptableObject.getEditorInput();
				if (null!=editor) {
					IDocumentProvider documentProvider = adaptableObject.getDocumentProvider();
					IDocument document = documentProvider.getDocument(editor);
					IndexModuleFile localImf = IndexModuleFile.extractModuleFileService(document);
					if (null!=localImf && null!=localImf.module && null!=imf && null!=imf.module) {
						return localImf.module==this.imf.module;
					}
				}
			}
		} catch (Throwable t) {
			logger.info("exception",t);
		}
		
		return false;
	}
	
	
	
	 /* (non-Javadoc)
     * Method declared on IPage (and Page).
     */
    public Control getControl() {
        if (composite == null) {
			return null;
		} 
        return composite;
    }

 

    private static ImageRegistry ir;
    
    boolean wasCreated = false;
	public void createControl(Composite parent) {
		if (wasCreated) {
			//this is required because we re-use the outline view when the module is the
			//same and the caller does not know this is already build once.
			return; //nothing to do, ths is already set up.
		}
		
		//System.out.println("create contorl");
		
		wasCreated = true;
		if (null==ir) {
			try {
				LocalResourceManager lrm = new LocalResourceManager(JFaceResources.getResources(), parent);
				ir = new ImageRegistry(lrm);
				ir.put(BlockType.VARIABLE.name(), ImageDescriptor.createFromURL(getClass().getResource("/V.png")));
				ir.put(BlockType.LOCALS.name(),   ImageDescriptor.createFromURL(getClass().getResource("/L.png")));
				ir.put(BlockType.OUTPUT.name(),   ImageDescriptor.createFromURL(getClass().getResource("/O.png")));
				ir.put(BlockType.DATA.name(),     ImageDescriptor.createFromURL(getClass().getResource("/D.png")));
				ir.put(BlockType.RESOURCE.name(), ImageDescriptor.createFromURL(getClass().getResource("/R.png")));
				ir.put(BlockType.PROVIDER.name(), ImageDescriptor.createFromURL(getClass().getResource("/P.png")));
				ir.put(BlockType.MODULE.name(),   ImageDescriptor.createFromURL(getClass().getResource("/M.png")));
				ir.put("err-loop",                  ImageDescriptor.createFromURL(getClass().getResource("/loop.png")));
				//  rgb(138, 185, 255) light purple.
			} catch (Throwable t) {
				logger.error("unable to load images", t);
			}
			
		}
		
		final TerraArchActivator instance = TerraArchActivator.getDefault();
				
		IActionBars actionBars = this.getSite().getActionBars();
		  IMenuManager dropDownMenu = actionBars.getMenuManager();
		   IToolBarManager toolBar = actionBars.getToolBarManager();
		  
		   dropDownMenu.removeAll();
		   // dropDownMenu.add(action);
		   toolBar.removeAll();
		  
		   toolBar.add(new Action() {
				@Override
				public ImageDescriptor getImageDescriptor() {
					return instance.IMAGE_EXPAND_ALL;
				}
				@Override
				public void run() {
					treeViewer.expandAll();
				} 
			});
		   
		   toolBar.add(new Action() {
				@Override
				public ImageDescriptor getImageDescriptor() {
					return instance.IMAGE_COLLAPSE_ALL;
				}
				@Override
				public void run() {
					treeViewer.collapseAll();
				} 
			});
		   
		   actionBars.updateActionBars();

	    
		
		Composite north = new Composite(parent,0);//was sp
		//north.setLayout(new FillLayout(SWT.VERTICAL));
		
		north.setLayout(new GridLayout(1, true));
		north.setLayoutData(new GridData(GridData.FILL_BOTH));
		
//		barBuilder(parent, north, true); //optional we do need this for south
		treeViewer = new TreeViewer(north, SWT.H_SCROLL | SWT.V_SCROLL); 
		treeViewer.setUseHashlookup(true); //must be true
		
		GridData gd = new GridData(GridData.FILL_BOTH);
		treeViewer.getTree().setLayoutData(gd);


		
//		Composite south = new Composite(sp, 0);
//		south.setLayout(new GridLayout(1, true));
//		south.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
//		barBuilder(parent, south, false);
//		treeViewerUp = new TreeViewer(south, SWT.H_SCROLL | SWT.V_SCROLL);
		
//		Splitter sp = new Splitter(parent, 0);
//		composite = sp;
//		sp.init(north, south, SWT.HORIZONTAL, 75);
		composite = north;//treeViewerDown.getControl();
		
		treeViewer.addSelectionChangedListener(this);
		if (null!= imf && null != imf.module) {
			treeViewer.setContentProvider(new ContentProviderTopDown(imf.module));
			
			treeViewer.setInput(((ContentProviderTopDown)treeViewer.getContentProvider()).getElements(null) );
		}
		labelProvider = treeViewerSetup(parent, treeViewer/*, treeViewerUp*/);
	      
		
		
//		treeViewerUp.addSelectionChangedListener(this);
//		if (null != imf && null != imf.module) {
//			treeViewerUp.setContentProvider(imf.module.getTreeNavUp() );
//			treeViewerUp.setInput( imf.module.getTreeNavUp().getElements(null) );
//		}
//		treeViewerSetup(parent, treeViewerUp, treeViewerDown);
	  
		
	   }

	
	private OutlineLabelProvider treeViewerSetup(Composite parent, final TreeViewer primaryViewer/*, final TreeViewer secondaryViewer*/) {
		primaryViewer.setAutoExpandLevel(1);
		primaryViewer.addSelectionChangedListener(e-> {
			  	if (e.getSelection() instanceof IStructuredSelection) {
			  		
			  		IStructuredSelection iss = (IStructuredSelection)e.getSelection();
			  		OutlineNode node = (OutlineNode)iss.getFirstElement();
			  		
			  		//we must keep these because if a new file is loaded
			  		//we may need to restore these values to hide the fact a new outline was opened.
			  		if (null!=node && (lastNode==null ||  node!=lastNode) ) {

			  			///////////////////////////
			  			lastNode = node;//this stops us from setting the same thing multiple times
			  			///////////////////////////

			  			OutlineReactiveJobState.decorateActiveUsage(labelProvider, imf, node.node);			  			
			  									
			  			
				  		if (treeViewer.getControl().isFocusControl()) {
				  		
					  		IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				  			if (null!=activeWorkbenchWindow 
				  				//NOTE: at this point the editor is modified.
				  				&& (!TerraArchOutlineView.selectPositionInEditor(node, activeWorkbenchWindow.getActivePage()))) {
					  				//log we have detected the creation of a new outline
					  				//needReset.set(true);
				  			} 
			  			
				  		     SafeRunner.run(new SafeRunnable() {
				                  public void run() {
				                	  treeViewer.getControl().setFocus();
				                	  treeViewer.refresh();
				  			  }});
			  			} else {
				  			  SafeRunner.run(new SafeRunnable() {
				                  public void run() {
				                	  treeViewer.refresh();
				  			  }});
			  			}
			  		     
			  		     
			  		}		
			  		
//			  		System.out.println("expand alll secondary");
//	  				secondaryViewer.expandAll();
//	  				//selectMatchingObject(pimaryViewer.getTree(), selectedData);
//					selectMatchingObject(secondaryViewer.getTree(), selectedData);
					
					
			  	}
     });
		
     OutlineLabelProvider outlineLabelProvider = new OutlineLabelProvider(ir, imf.module);
	 primaryViewer.setLabelProvider(outlineLabelProvider);
	 return outlineLabelProvider;
	}

	//called by deep updates..
	public void syncOutline() {
		
		final TreeViewer tv = treeViewer;
		Display.getDefault().asyncExec(()-> {
			try {
				if (null!=tv) {        	
					ContentProviderTopDown contentProvider = (ContentProviderTopDown)treeViewer.getContentProvider();
					if (null!=contentProvider) {
											
						Object[] elements = contentProvider.getElements(null);
						tv.setInput(elements );
						tv.refresh(true); //looking for loops...
											
					}
				}
			} catch (Throwable t) {
				logger.error("syncOutline",t);
			}
						
		});
	}
	
	

	@Override
    public void addSelectionChangedListener(ISelectionChangedListener listener) {
		for(Object item : selectionChangedListeners.getListeners()) {
			if (item == listener) {
				return; //do not add an already present listener
			}
		}
        selectionChangedListeners.add(listener);
    }

   

    /**
     * Fires a selection changed event.
     *
     * @param selection the new selection
     */
    protected void fireSelectionChanged(ISelection selection) {
        // create an event
        final SelectionChangedEvent event = new SelectionChangedEvent(this, selection);

        // fire the event
        Object[] listeners = selectionChangedListeners.getListeners();
        for (int i = 0; i < listeners.length; ++i) {
            final ISelectionChangedListener l = (ISelectionChangedListener) listeners[i];
            SafeRunner.run(new SafeRunnable() {
                public void run() {
                    l.selectionChanged(event);
                }
            });
        }
    }

   
    private boolean isInit = false;
    @Override
    public void init(IPageSite pageSite) {
    	if (isInit) {
    		return; //this page is re-used for the same module so nothing to do.
    	}
    	isInit = true;
    	
        super.init(pageSite);
        pageSite.setSelectionProvider(this);
    }

    @Override
    public void removeSelectionChangedListener(
            ISelectionChangedListener listener) {
        selectionChangedListeners.remove(listener);
    }

    @Override
    public void selectionChanged(SelectionChangedEvent event) {
        fireSelectionChanged(event.getSelection());
    }

    @Override
    public void setFocus() {
        treeViewer.getControl().setFocus();
    }

    /////////////////////////////////////////////////////
    
    @Override
    public void setSelection(ISelection selection) {
        if (composite != null) {
        	OutlineViewSelection ovs = (OutlineViewSelection)selection;
        
        	//treeViewerUp.setSelection(ovs.up);
			treeViewer.setSelection(ovs.down);
			
		}
    }


    @Override
    public ISelection getSelection() {
        return new OutlineViewSelection(this);
    }

    
	public static boolean selectPositionInEditor(OutlineNode node, IWorkbenchPage page) {
				
		IFile optionalIFile = node.optionalIFile;		
		final int itemLen = node.itemLength();				
		final int startOfItem = node.itemStart();
		final boolean enable = node.editorOffsetPosition>=0;
		
		return JumpToDef.selectPositionInEditor(node, page, optionalIFile, itemLen, startOfItem, enable);
	}
	
	
}

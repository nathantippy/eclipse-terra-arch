package net.terraarch.util;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.AbstractTextEditor;

import net.terraarch.preferences.TerraPreferences;

public class DisableSmartInsert {
	
	public static void applyListeners() {
	
		IWindowListener winListener = new IWindowListener() {

			@Override
			public void windowActivated(IWorkbenchWindow workbenchWindow) {	
			
				if (TerraPreferences.instance.isDisableSmartInsert()) {
					
					//NOTE: can we find the menu item for this and disable it?
					
					for(IWorkbenchPage page : workbenchWindow.getPages()) {
						updatePage(page);
					};
					
					removeMenuItem();
				}
			}

			@Override
			public void windowClosed(IWorkbenchWindow arg0) {
			}
			@Override
			public void windowDeactivated(IWorkbenchWindow arg0) {
			}
			@Override
			public void windowOpened(IWorkbenchWindow arg0) {
			}
		};
		PlatformUI.getWorkbench().addWindowListener(winListener);
		
	}
	
    private static void removeMenuItem() {
    	
    	/*

    	
    	        
    	       WorkbenchWindow window = (WorkbenchWindow) Workbench.getInstance().getActiveWorkbenchWindow();
//    	        int i = windows.length;
//    	        while (--i>=0) {
//    	        
//    	        	IWorkbenchWindow window = windows[i];
    	        
    			if(window instanceof WorkbenchWindow) {
    			    MenuManager menuManager = ((WorkbenchWindow)window).getMenuManager();

//    			    //NOTE: you may need to remove items from the coolbar as well
//    			    ICoolBarManager coolBarManager = null;
//    			    if(((WorkbenchWindow) window).getCoolBarVisible()) {
//    			        coolBarManager = ((WorkbenchWindow)window).getCoolBarManager2();
//    			    }

    			    Menu menu = menuManager.getMenu();

    			    
    			    //NOTE: we need a better way to find menu items.
    			    for(MenuItem item:  menu.getItems()) {
    			    	if (item.getText().contains("Edit")) {
    			    		
    			    		//System.out.println("menu item :"+item.getID()+"  "+item.getText());
    			    		for(MenuItem editItem:	item.getMenu().getItems()) {
    			    			
    			    			if (editItem.getText().contains("Insert Mode")) {
    			    		
    			    				//System.out.println("found menu item "+editItem.getText());
    			    				
//    			    				SelectionListener listener = new SelectionListener() {
//										
//										@Override
//										public void widgetSelected(SelectionEvent arg0) {
//											editItem.setEnabled(false);    	
//										}
//										
//										@Override
//										public void widgetDefaultSelected(SelectionEvent arg0) {
//											editItem.setEnabled(false);    	
//										}
//									};
//									editItem.addSelectionListener(listener);
    			    				editItem.setEnabled(false);    		
    			    				
    			    				if (editItem instanceof IActionSetContributionItem) {
    			    					System.out.println("here is the action set to remove: "+((IActionSetContributionItem)editItem).getActionSetId());
    			    				}
    			    				
    			    			}
    			    			
    			    		};
    			    		
    			    	}
    			    	
    			    }
    			    
//    			    //you'll need to find the id for the item
//    			    String itemId = "menuId";
//    			    IContributionItem item = menuManager.find(itemId);
//
//    			    // remember position, this is protected
//    			    int controlIdx = menu.indexOf(mySaveAction.getId());
//
//    			    if (item != null) {
//    			        // clean old one
//    			        menuManager.remove(item);
//
//    			        // refresh menu gui
//    			        menuManager.update();
//    			    }
    			}
    			//  */
    			
    			
    	   //     }
    			
    			
    			
    			
    }
	
	//addCaretFix
	
//	private static void updatePageWithCaretFix(IWorkbenchPage activePage) {
//		if (null != activePage) {
//			
//			IViewReference[] ref = activePage.getViewReferences();
//			int x = ref.length;
//			while (--x >= 0) {
//
//				 IViewPart p = ref[x].getView(false);
//				p.getAdapter(ITextViewer.class);
//				
//				//NOTE: how to get all the
//				// ITextViewer s;
//		
//				
////				if (p instanceof AbstractTextEditor) {
////					AbstractTextEditor ate = (AbstractTextEditor) p;
////					ate.setInsertMode(org.eclipse.ui.texteditor.ITextEditorExtension3.INSERT);
////				}
//			}
//		}
//	}	

	private static void updatePage(IWorkbenchPage activePage) {
		if (null != activePage) {
			
			IEditorReference[] ref = activePage.getEditorReferences();
			int x = ref.length;
			while (--x >= 0) {

				IEditorPart p = ref[x].getEditor(true);
				
				if (p instanceof AbstractTextEditor) {
					AbstractTextEditor ate = (AbstractTextEditor) p;
					ate.setInsertMode(org.eclipse.ui.texteditor.ITextEditorExtension3.INSERT);
				}
			}
		}
	}	
		
	
	public static void disableSmartInsertOnAllEditors() {
		IWorkbench workbench = PlatformUI.getWorkbench();
		
		if (null != workbench) {
			IWorkbenchWindow activeWorkbenchWindow = workbench.getActiveWorkbenchWindow();
		
			
			if (null != activeWorkbenchWindow) {
				IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
			
				updatePage(activePage);
			}
		}
	}
	
	
}

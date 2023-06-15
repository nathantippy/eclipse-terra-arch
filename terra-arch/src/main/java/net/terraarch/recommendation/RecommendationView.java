package net.terraarch.recommendation;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;



import net.terraarch.TerraArchActivator;
import net.terraarch.outline.TerraArchOutlineView;



//this view follows and is driven by the module the outline is looking at.
public class RecommendationView extends ViewPart {

	public RecommendationView() {
	}


    private TableViewer viewer;
    private TerraArchOutlineView outlineView;
           
    
    class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {
        public String getColumnText(Object obj, int index) {
            return getText(obj);
        }
        public Image getColumnImage(Object obj, int index) {
            return getImage(obj);
        }
        public Image getImage(Object obj) {
            return PlatformUI.getWorkbench().
                    getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
        }
    }
  
	public void setRecommendations(final Object[] recommendations, TerraArchOutlineView outlineView) {
					
				viewer.setContentProvider(new IStructuredContentProvider() {
					@Override
					public Object[] getElements(Object arg0) {
						return recommendations;
					}
				});
				viewer.refresh(true);
			    viewer.setInput(getViewSite());
				System.out.println("new content provider!!");
						
		
		
		this.outlineView = outlineView;
		
	}
	
    
    /**
     * This is a callback that will allow us
     * to create the viewer and initialize it.
     */
    public void createPartControl(Composite parent) {
        viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);

        final String[] startValue =  new String[0];
        
        viewer.setContentProvider(new IStructuredContentProvider() {//empty to start until outline appears
			@Override
			public Object[] getElements(Object arg0) {
				return startValue;
			}
		});
        viewer.setLabelProvider(new ViewLabelProvider());
      //  viewer.setSorter(new NameSorter()); //future idea
        viewer.setInput(getViewSite());

        viewer.addPostSelectionChangedListener( e -> {
        	
        	System.out.println("new selection: "+e.getSelection());
        	//TODO: AA highlight the items in outline modified for extract
        	//     outlineView
        	
        });
        viewer.addDoubleClickListener(e -> {
        	        	
        	System.out.println("double click and apply  "+e.getSource());
        	
        	//TODO: BB e.getSource().apply();
        	//build this as a normal refactor for extration of modules.
        	        	
        });
        	
        
        // TODO: A Create the help context id for the viewer's control
        //PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), "com.example.helloworld2.viewer");
    
            
        
    }

	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}





    //TODO: AA refactoring menu for extract module where we select which specifically in outline
    //TODO: AA refactoring menu for inline module of simple selected module
    
    //TODO: A Recommendation, upgrade to version X, with broad changes
    //TODO: A Recommendation, use remote state... 
    //TODO: A Recommendation, avoid AWS specific resources but then what?? Just flag??
    //TODO: B new project wizards to build clean K8S projectswith remote state etc.
    
    //  Recomendations are driven from the machine, Wizards/Refactoring tools are driven by the user.

}

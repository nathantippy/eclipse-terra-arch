package net.terraarch;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class TerraArchPerspective implements IPerspectiveFactory {

	@Override
	public void createInitialLayout(IPageLayout layout) {

		layout.setEditorAreaVisible(true);
		String editorArea = layout.getEditorArea();
	
		layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.file");
		layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.folder");
				
		IFolderLayout left  = layout.createFolder("left", IPageLayout.LEFT,    (float) 0.15, editorArea);
		IFolderLayout right = layout.createFolder("right", IPageLayout.RIGHT,  (float) 0.80, editorArea);
		IFolderLayout down  = layout.createFolder("down", IPageLayout.BOTTOM,  (float) 0.80, editorArea);

		//IFolderLayout rightBottom = layout.createFolder("rightBottom", IPageLayout.BOTTOM, (float) 0.70, "right");
		
/////////////////////////////////////////////////////////////////////////////////////////////////////////////		
//	    relationship - the position relative to the reference part; one of TOP, BOTTOM, LEFT, or RIGHT
//	    ratio - a ratio specifying how to divide the space currently occupied by the reference part, in the
//	    range 0.05f to 0.95f. Values outside this range will be clipped to facilitate direct manipulation.
//	    For a vertical split, the part on top gets the specified ratio of the current space and the part on 
//	    bottom gets the rest. Likewise, for a horizontal split, the part at left gets the specified ratio of 
//	    the current space and the part at right gets the rest.
////////////////////////////////////////////////////////////////////////////////////////////////////
		
		 left.addView(IPageLayout.ID_PROJECT_EXPLORER);
		right.addView(IPageLayout.ID_OUTLINE);
		right.addView(IPageLayout.ID_MINIMAP_VIEW);
		
		 down.addView(IPageLayout.ID_PROBLEM_VIEW);
//		 down.addView("net.terraarch.recommendation.RecommendationView");
		 
//		 down.addView("org.eclipse.ui.console.ConsoleView"); //in the future here we will launch terraform plan ...
				 
	    layout.addShowViewShortcut("net.terraarch.recommendation.RecommendationView");
		layout.addShowViewShortcut("org.eclipse.ui.console.ConsoleView");
		layout.addShowViewShortcut(IPageLayout.ID_PROBLEM_VIEW);
		layout.addShowViewShortcut(IPageLayout.ID_OUTLINE);
	}

}

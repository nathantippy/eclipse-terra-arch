package net.terraarch.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Sash;

public class Splitter extends Composite {

	  private static final int SASH_LIMIT = 20;

	  public Splitter(Composite parent, int style) {
	    super(parent, style);
	    this.setLayout(new FormLayout());
	  }

	  public void init(Control first, Control second, int sasherStyle, int percent) {
	    Sash sash = new Sash(this, sasherStyle);
	          
	    // create position instructions
	    final FormData firstData = new FormData();
	    final FormData sashData = new FormData();
	    final FormData secondData = new FormData();

	    // setup constant positions
	    firstData.left = new FormAttachment(0);
	    firstData.top = new FormAttachment(0);
	    sashData.left = new FormAttachment(0);
	    sashData.top = new FormAttachment(percent);
	    secondData.right = new FormAttachment(100);
	    secondData.bottom = new FormAttachment(100);

	    if ((sasherStyle & SWT.VERTICAL) != 0) {
	      firstData.right = new FormAttachment(sash);
	      firstData.bottom = new FormAttachment(100);
	      sashData.bottom = new FormAttachment(100);
	      secondData.left = new FormAttachment(sash);
	      secondData.top = new FormAttachment(0);
	      
 	      sash.addListener(SWT.Selection, e -> resizeVertical(e, this, sash, sashData));
		    
	    } else {
	      firstData.right = new FormAttachment(100);
	      firstData.bottom = new FormAttachment(sash);
	      sashData.right = new FormAttachment(100);
	      secondData.left = new FormAttachment(0);
	      secondData.top = new FormAttachment(sash);
	      
 	      sash.addListener(SWT.Selection, e -> resizeHorizontal(e, this, sash, sashData));
		  
	    }

	    // set the layouts
	    first.setLayoutData(firstData);
	    sash.setLayoutData(sashData);
	    second.setLayoutData(secondData);
	   
	   // sash.setBackground(ColorConstants.white);
	    
	    sash.setBackground(sash.getDisplay().getSystemColor(SWT.COLOR_BLACK));
	    //sash.set
	    
	  }

	  private static final void resizeHorizontal(Event e, Splitter that, Sash sash, FormData sashData) {
	    e.y = Math.max(Math.min(e.y, that.getBounds().height - sash.getBounds().height - SASH_LIMIT), SASH_LIMIT);
	    if (e.y != sash.getBounds().y)  {
	      sashData.top = new FormAttachment(0, e.y);
	      that.layout();
	    }
	  }

	  private static final void resizeVertical(Event e, Splitter that, Sash sash, FormData sashData) {
	    e.x = Math.max(Math.min(e.x, that.getBounds().width - sash.getBounds().width - SASH_LIMIT), SASH_LIMIT);
	    if (e.x != sash.getBounds().x)  {
	      sashData.top = new FormAttachment(0, e.x);
	      that.layout();
	    }
	  }
	
}

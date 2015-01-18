package org.ttdc.flipcards.client.ui;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;

/**
 * Resources used by the entire application.
 */
public interface Resources extends ClientBundle {
  @Source("Style.css")
  Style style();

  @Source("no-up.png")
  ImageResource noUp();
  @Source("no-down.png")
  ImageResource noDown();
  @Source("no-hover.png")
  ImageResource noUpHover();
  @Source("no-disabled.png")
  ImageResource noDisabled();
  
  
  @Source("yes-up.png")
  ImageResource yesUp();
  @Source("yes-down.png")
  ImageResource yesDown();
  @Source("yes-hover.png")
  ImageResource yesUpHover();
  @Source("yes-disabled.png")
  ImageResource yesDisabled();
  
  @Source("flip-up.png")
  ImageResource flipUp();
  @Source("flip-down.png")
  ImageResource flipDown();
  @Source("flip-hover.png")
  ImageResource flipUpHover();
  @Source("flip-disabled.png")
  ImageResource flipDisabled();
  

  public interface Style extends CssResource {
    String mainBlock();
    String nameSpan();
    String removeButtonDefaults();
//    Sprite userPictureSprite();
  }
}

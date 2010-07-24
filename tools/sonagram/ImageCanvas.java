// $Id: ImageCanvas.java,v 1.1 2004/10/26 19:20:23 greg Exp $

import java.awt.*;
import java.awt.image.*;

public class ImageCanvas extends Canvas implements ImageObserver
{
  private Image image;
  private boolean scale = false;

  public ImageCanvas() {}

  public ImageCanvas( boolean scale ) {
    this.scale = scale;
  }

  public ImageCanvas( Image image ) {
    this.image = image;
  }

  public ImageCanvas( Image image, boolean scale ) {
    this.image = image;
    this.scale = scale;
  }

  public void setImage( Image image ) {
    this.image = image;
    repaint();
  }

  public void paint( Graphics g ) {
     if (image==null) return;

     if (scale)
       g.drawImage( image, 0, 0, getSize().width, getSize().height, this );
     else
       g.drawImage( image, 0, 0, this );
  }

  public boolean imageUpdate( Image image, int flags,
    int x, int y, int w, int h ) {
    repaint();
    return true;
  }
}

// $Id: Sonagram.java,v 1.2 2002/04/12 19:51:23 mito Exp $

import java.awt.*;
import java.awt.image.*;
import javax.swing.*;

public class Sonagram extends JPanel
{
  static private ColorModel rgbModel = ColorModel.getRGBdefault();
  private int width, height;
  private Signal[] signals;
  private Image image;

  public Sonagram( Signal signals[] ) {
    this.signals = signals;
    width = signals.length;
    height = signals[0].length();

    setLayout( new BorderLayout() );
    image = generateImage();
    add( "Center", new ImageCanvas( image ) );
  }

  public Sonagram( Signal signal, int blocksize ) {
    this( magphase( Analyze.analyze( signal, blocksize ) ) );
  }

  public Sonagram( Sound sound, int blocksize ) {
    this( SoundSignal.convert( sound ), blocksize );
  }

  public Image getImage() { return image; }

  public JFrame makeFrame() {
    JFrame jf = new JFrame();
    jf.getContentPane().setLayout( new BorderLayout() );
    jf.getContentPane().add( "Center", this );
    jf.setSize( width+9, height+27 );
    return jf;
  }

  public Dimension getPreferredSize() {
    return new Dimension( width, height );
  }

  public Dimension getMinimumSize() {
    return new Dimension( width, height );
  }

  public Dimension getMaximumSize() {
    return new Dimension( width, height );
  }

double poop = 0;
  private Image generateImage() {
    int bits[] = new int[width*height];
    for (int i=0; i<signals.length; ++i) {
      Signal signal = signals[i];
      double raw[][] = signal.raw();
      int len = signal.length();
      for (int j=0; j<len; ++j) {
        double val = raw[0][j];
        int c = (int)(val*256);
        if (c<0) c=0; else if (c>=256) c=255;
c = 255-c;
//        int pix = 0xff000000 | (c<<16) | (c<<8) | c;
int pix = makeColor( c, c, c );
        bits[i+j*width] = pix;
      }
    }

    MemoryImageSource mis =
      new MemoryImageSource( width, height, rgbModel, bits, 0, width );
    Image image = Toolkit.getDefaultToolkit().createImage( mis );

    return image;
  }

  static private Signal[] magphase( Signal signals[] ) {
    Signal nss[] = new Signal[signals.length];
    for (int i=0; i<signals.length; ++i) {
      nss[i] = signals[i].magphase().real();
    }
    return nss;
  }

  static public int makeColor( int r, int g, int b ) {
    return makeColor( r, g, b, 0xff );
  }

  static public int makeColor( int r, int g, int b, int alpha ) {
    return ((alpha&0xff)<<24)|((r&0xff)<<16)|((g&0xff)<<8)|(b&0xff);
  }

  static public void main( String args[] ) throws Exception {
    String filename = args[0];
    int blocksize = Integer.parseInt( args[1] );
    String outfile = args[2];
    Sound sound = new Sound( filename );
    Sonagram sg = new Sonagram( sound, blocksize );
    sg.makeFrame().setVisible( true );
    if (outfile!=null) {
      WriteImage.writeImage( outfile, sg.getImage() );
    }
  }
}

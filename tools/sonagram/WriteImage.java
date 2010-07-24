// $Id: WriteImage.java,v 1.2 2002/04/12 19:51:24 mito Exp $

import java.awt.*;
import java.io.*;
import Acme.JPM.Encoders.*;

public class WriteImage
{
  static public void writeImage( String filename, Image image )
      throws IOException {
    FileOutputStream fout = new FileOutputStream( filename );
    writeImage( fout, image );
    fout.close();
  }

  static public void writeImage( OutputStream out, Image image )
      throws IOException {
    try {
      GifEncoder ge = new GifEncoder( image, out );
      ge.encode();
    } catch( IOException ie ) {
      ie.printStackTrace();
    }
  }
}

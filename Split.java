// $Id: Split.java,v 1.1 2004/10/26 19:20:23 greg Exp $

import java.io.*;
import javax.sound.sampled.*;

public class Split
{
  static public void split( String filename, long size ) throws Exception {
    AudioInputStream ain = AudioSystem.getAudioInputStream( new File( filename ) );
    long len = ain.getFrameLength();
    ain.close();

    int c = filename.toLowerCase().indexOf( ".wav" );
    if (c==-1) {
      throw new RuntimeException( "Can't generate names for "+filename );
    }
    String stub = filename.substring( 0, c );

    long left = len;
    int sofar = 0;
    int inx=0;
    while (left > 0) {
      long nextSize = size;
      if (nextSize > left)
        nextSize = left;
      //System.out.println( "and "+sofar+" "+nextSize );
      String nfilename = stub+"_"+inx+".wav";
      Extract.extract( nfilename, filename, sofar, sofar+nextSize );
      sofar += nextSize;
      left -= nextSize;
      inx++;
    }
  }

  static public void main( String args[] ) throws Exception {
    long size = Long.parseLong( args[0] );
    for (int a=1; a<args.length; ++a) {
      split( args[a], size );
    }
  }
}

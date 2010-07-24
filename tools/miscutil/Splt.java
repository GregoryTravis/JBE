// $Id$

import java.io.*;

public class Splt
{
  static public void main( String args[] ) throws Exception {
    long middle = 613828630;
//    long middle = 242;
    byte buffer[] = new byte[65536*8];
//    byte buffer[] = new byte[2];
    File file = new File( "l:/merciless/merciless.wav" );
//    File file = new File( "h:/cygwin/home/mito/backup.txt" );
    System.out.println( file.length() );
    FileInputStream fin = new FileInputStream( file );
    FileOutputStream fout = new FileOutputStream( "o:/merciless.wav.part.1" );

    long sofar = 0;
    boolean firstHalf = true;

    while( true ) {
      int r = fin.read( buffer );
      if (r<=0) {
        break;
      }
      fout.write( buffer, 0, r );

      sofar += r;

      if (firstHalf && sofar > middle) {
        fout.close();
        fout = new FileOutputStream( "o:/merciless.wav.part.2" );
        firstHalf = false;
      }
    }

    fout.close();
    fin.close();
  }
}

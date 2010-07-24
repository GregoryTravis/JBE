// $Id: Extract.java,v 1.1 2004/10/26 19:20:23 greg Exp $

import java.io.*;

public class Extract
{
  static public void extract( String dest, String source, long start, long end )
      throws IOException {
    System.out.println( dest+" <== "+source+"( "+start+", "+end+" )" );
    AudOutputStream aout = new AudOutputStream( (int)(end-start), new File( dest ) );
    aout.concatenateFile( new File( source ), start, end );
  }

  static public void main( String args[] ) throws Exception {
    String dest = args[0];
    String source = args[1];
    long start = Long.parseLong( args[2] );
    long end = Long.parseLong( args[3] );

    extract( dest, source, start, end );
  }
}

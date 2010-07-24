// $Id: Dump.java,v 1.1 2004/10/26 19:20:23 greg Exp $

public class Dump
{
  static public void dump( Sound sound, int start, int end ) {
    short raw[] = sound.raw();
    for (int i=start; i<=end; ++i)
      System.out.println( i+" "+raw[i] );
  }

  static public void main( String args[] ) throws Exception {
    Sound sound = new Sound( args[0] );
    int start = Integer.parseInt( args[1] );
    int end = Integer.parseInt( args[2] );
    dump( sound, start, end );
  }
}

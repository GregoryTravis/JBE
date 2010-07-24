// $Id: MaxDelta.java,v 1.1 2004/10/26 19:20:23 greg Exp $

public class MaxDelta
{
  static public void main( String args[] ) throws Exception {
    Sound sound = new Sound( args[0] );
    short raw[] = sound.raw();
    int mx = 0;
    for (int i=0; i<raw.length-1; ++i) {
      int d = raw[i+1]-raw[i];
      if (d<0) d=-d;
      if (d>mx) mx = d;
    }

    System.out.println( "max delta "+mx );
  }
}

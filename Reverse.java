// $Id: Reverse.java,v 1.1 2004/10/26 19:20:23 greg Exp $

public class Reverse
{
  static public Sound reverse( Sound sound ) {
    Sound rsound = sound.dup();
    short raw[] = rsound.raw();
    int len = sound.length(), l2 = len/2;
    for (int i=0; i<l2; ++i) {
      short b = raw[i];
      raw[i] = raw[len-i-1];
      raw[len-i-1] = b;
    }
    return rsound;
  }

  static public void main( String args[] ) throws Exception {
    String infile = args[0], outfile=args[1];

    Sound sound = new Sound( infile );
    Sound rsound = reverse( sound );
    rsound.saveTo( outfile );
  }
}

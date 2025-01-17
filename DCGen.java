// $Id: DCGen.java,v 1.1 2004/10/26 19:20:23 greg Exp $

public class DCGen
{
  static public Sound dcgen( int len ) {
    Sound nsound = new Sound( len );
    short raw[] = nsound.raw();
    for (int i=0; i<raw.length; ++i)
      raw[i] = 16384;
    return nsound;
  }

  static public void main( String args[] ) throws Exception {
    String outfile = args[0];
    int len = Integer.parseInt( args[1] );

    Sound dc = dcgen( len );
    dc.saveTo( outfile );
  }
}

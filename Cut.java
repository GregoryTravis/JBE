// $Id: Cut.java,v 1.1 2004/10/26 19:20:23 greg Exp $

public class Cut
{
  static public void main( String args[] ) throws Exception {
    String infile = args[0];
    int start = Integer.parseInt( args[1] );
    int end = Integer.parseInt( args[2] );
    String outfile = args[3];

    Sound sound = new Sound( infile );
    Sound nsound = sound.subSound( start, end );
    nsound.saveTo( outfile );
  }
}

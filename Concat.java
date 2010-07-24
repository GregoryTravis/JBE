// $Id: Concat.java,v 1.1 2004/10/26 19:20:23 greg Exp $

public class Concat
{
  static public void main( String args[] ) throws Exception {
    String outfile = args[0];
    Sound infiles[] = new Sound[args.length-1];
    for (int i=0; i<args.length-1; ++i) {
      infiles[i] = new Sound( args[i+1] );
    }
    Sound.concat( infiles ).saveTo( outfile );
  }
}

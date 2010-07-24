// $Id: Hamming.java,v 1.1 2004/10/26 19:20:23 greg Exp $

public class Hamming
{
  static public double hamming( double t ) {
    return 0.54 - 0.46 * Math.cos( t );
  }

  static public void main( String args[] ) throws Exception {
    int len = Integer.parseInt( args[0] );
    String outfile = args[1];

    Signal signal = new Signal( len );
    double raw[][] = signal.raw();
    for (int i=0; i<len; ++i) {
      double t = Math.PI*2*((double)i/(double)(len-1));
      double h = hamming( t );
      raw[0][i] = h;
    }

    SoundSignal.convert( signal ).saveTo( outfile );
  }
}

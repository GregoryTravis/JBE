// $Id: Sine.java,v 1.1 2004/10/26 19:20:23 greg Exp $

public class Sine
{
  static public Sound sine( int len, double frequency ) {
    return sine( len, frequency, 0 );
  }

  static public Sound sine( int len, double frequency, double phase ) {
    Sound nsound = new Sound( len );
    short raw[] = nsound.raw();
    double rps = (2*Math.PI*(double)frequency)/(double)Constants.sampRate;
    for (int i=0; i<raw.length; ++i) {
      double ph = rps*i + phase;
      double s = Math.cos( ph );
      raw[i] = (short)(s*32767);
    }
    return nsound;
  }

  static public void main( String args[] ) throws Exception {
    String outfile = args[0];
    int len = Integer.parseInt( args[1] );
    double frequency = Double.parseDouble( args[2] );
    double phase = Double.parseDouble( args[3] );

    Sound dc = sine( len, frequency, phase );
    dc.saveTo( outfile );
  }
}

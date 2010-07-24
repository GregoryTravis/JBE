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
    int len = new Integer( args[1] ).intValue();
    double frequency = new Double( args[2] ).doubleValue();
    double phase = new Double( args[3] ).doubleValue();

    Sound dc = sine( len, frequency, phase );
    dc.saveTo( outfile );
  }
}

// $Id: Pts.java,v 1.1 2004/10/26 19:20:23 greg Exp $

public class Pts
{
  static private final int defaultWindowLen = 5500;

  static public Sound pts( Sound sound, int newlen ) {
    return pts( sound, newlen, defaultWindowLen );
  }

  static public Sound pts( Sound sound, int newlen, int windowLen ) {
System.out.println( "old "+sound.length()+" new "+newlen );
    int len = sound.length();
    short raw[] = sound.raw();

    Sound nsound = new Sound( newlen );
    short nraw[] = nsound.raw();

    double scale = (double)newlen/(double)len;

    for (int ows=0; ows<newlen; ows+=windowLen/3) {
      int iws = (int)(ows/scale);
      for (int i=0; i<windowLen; ++i) {
        int ip = iws+i, op=ows+i;
        if (ip<len && op<newlen)
          nraw[op] += raw[ip] * window( i, windowLen );
      }
    }

    return nsound;
  }

  static public double window( int s, int w ) {
    double t = (double)s/(double)w;
if (t<0 || t>=1.0)
  throw new RuntimeException( "bad t "+t+" "+s+" "+w );
    t *= (2*Math.PI);
    double wv = 0.5 - 0.5 * Math.cos( t );
if (wv>1.0 || wv<-1.0)
  throw new RuntimeException( "bad wv "+wv );
    return wv;
  }

  static public void main( String args[] ) throws Exception {
    String infile = args[0];
    double scale = Double.parseDouble( args[1] );
    String outfile = args[2];
    int windowLen = Integer.parseInt( args[3] );

    Sound sound = new Sound( infile );
    int newlen = (int)(sound.length()*scale);
    Sound nsound = Pts.pts( sound, newlen, windowLen );
    nsound.saveTo( outfile );
  }
}

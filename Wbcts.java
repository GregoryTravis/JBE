// $Id: Wbcts.java,v 1.1 2004/10/26 19:20:23 greg Exp $

public class Wbcts
{
  static public Sound wbcts( Sound sound, int newlen, int windowLen ) {
System.out.println( "old "+sound.length()+" new "+newlen );
    int len = sound.length();
    short raw[] = sound.raw();

    Sound nsound = new Sound( newlen );
    short nraw[] = nsound.raw();

    int nwindowLen = (int)((double)windowLen*((double)newlen/(double)len));

    for (int op=0; op<newlen; ++op) {
      int n = op / nwindowLen;
      int rr = op % nwindowLen;
      int m = rr / windowLen;
      int r = rr % windowLen;
int top = n*nwindowLen + m*windowLen + r;
if (top != op)
  System.out.println( "op "+op+" "+top );
      int ip = n * windowLen + r;
try {
      if (ip>=len) ip=len-1;
      nraw[op] = (short)(raw[ip] * window( r, windowLen ));
} catch( ArrayIndexOutOfBoundsException ae ) {
  System.out.println( op+" of "+nraw.length+" <-- "+ip+" of "+raw.length );
  System.out.println( "n "+n+" m "+m+" r "+r+" wl "+windowLen+" nwl "+
    nwindowLen );
  throw ae;
}
    }

//    return nsound;

    Sound padding = new Sound( windowLen/3 );
    Sound sub0 = nsound.concat( padding ).concat( padding );
sub0.gain( 0.666666666 );
    Sound sub1 = padding.concat( nsound ).concat( padding );
sub1.gain( 0.666666666 );
    Sound sub2 = padding.concat( padding ).concat( nsound );
sub2.gain( 0.666666666 );
    Sound dbsound = SoundAdd.add( SoundAdd.add( sub0, sub1 ), sub2 );

/*
    Sound padding = new Sound( windowLen/2 );
    Sound sub0 = nsound.concat( padding );
    Sound sub1 = padding.concat( nsound );
    Sound dbsound = SoundAdd.add( sub0, sub1 );
*/

    return dbsound;
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
    double scale = new Double( args[1] ).doubleValue();
    String outfile = args[2];
    int windowLen = new Integer( args[3] ).intValue();

    Sound sound = new Sound( infile );
    int newlen = (int)(sound.length()*scale);
    Sound nsound = Wbcts.wbcts( sound, newlen, windowLen );
    nsound.saveTo( outfile );
  }
}

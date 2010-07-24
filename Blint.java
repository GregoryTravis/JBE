// $Id: Blint.java,v 1.1 2004/10/26 19:20:23 greg Exp $

public class Blint
{
  static final int Nz = 13;

  static public Signal blint( Signal signal, int newlen ) {

    int oldlen = signal.length();

    Signal nsignal = new Signal( newlen );

    double raw[][] = signal.raw();
    double nraw[][] = nsignal.raw();

    boolean shorter = newlen < oldlen;

    double scale = shorter ? (double)newlen/(double)oldlen : 1.0;

    // window half-length
    double whl = (double)Nz * scale;
    double wl = whl*2;

    for (int nt = 0; nt<newlen; ++nt) {
if ((nt%16384)==0) System.out.println( nt+" of "+newlen );
      double ot = ((double)nt*(double)oldlen)/(double)newlen;

      double acc = 0;

      int startoi = (int)Math.ceil( ot-whl );
      if (startoi<0) startoi=0;
      int endoi = (int)Math.floor( ot+whl );
      if (endoi >= oldlen) endoi = oldlen-1;

      for (int oi=startoi; oi<=endoi; ++oi) {
        double sincarg = (oi-ot)*scale;
        double sinc = Sinc.sinc( sincarg );
        double harg = Math.PI + Math.PI*2*((oi-ot)/wl);
        double hamming = Hamming.hamming( harg );
        double p = raw[0][oi] * sinc * hamming;
        //System.out.println( "filt "+nt+"<--"+ot+" "+oi+" of ("+
        //  startoi+".."+endoi+"), sinc "+sinc+" oiot "+(oi-ot)+
        //  " harg "+harg+" hamming "+hamming );
        acc += p;
      }

      nraw[0][nt] = acc;
    }

    return nsignal;
  }

  static public void main( String args[] ) throws Exception {
    Sound sound = new Sound( args[0] );
    double scale = new Double( args[1] ).doubleValue();

    Signal signal = SoundSignal.convert( sound );
    Signal nsignal = blint( signal, (int)(signal.length()*scale) );

    Sound nsound = SoundSignal.convert( nsignal );
    nsound.saveTo( args[2] );
  }
}

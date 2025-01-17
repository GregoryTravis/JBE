// $Id: Rblint.java,v 1.1 2004/10/26 19:20:23 greg Exp $

public class Rblint
{
  static public Signal Rblint( Signal signal, int newlen ) {

    int oldlen = signal.length();

    Signal nsignal = new Signal( newlen );

    double raw[][] = signal.raw();
    double nraw[][] = nsignal.raw();

    boolean shorter = newlen < oldlen;

    double scale = shorter ? (double)newlen/(double)oldlen : 1.0;

int nterms = 200;

    for (int nt = 0; nt<newlen; ++nt) {
if ((nt%16384)==0) System.out.println( nt+" of "+newlen );
      double ot = ((double)nt*(double)oldlen)/(double)newlen;

      double acc = 0;

int startoi = ((int)ot)-nterms;
if (startoi < 0) startoi = 0;
int endoi = ((int)ot)+nterms;
if (endoi >= oldlen) endoi = oldlen-1;

//      for (int oi=0; oi<oldlen; ++oi) {
for (int oi=startoi; oi<=endoi; ++oi) {
        double sincarg = (oi-ot)*Math.PI*scale;
        double sinc = sincarg==0.0 ?
          1.0 : Math.sin( sincarg ) / sincarg;
        double p = raw[0][oi] * sinc;
        acc += p;
      }

      nraw[0][nt] = acc;
    }

    return nsignal;
  }

  static public void main( String args[] ) throws Exception {
    Sound sound = new Sound( args[0] );
    double scale = Double.parseDouble( args[1] );

    Signal signal = SoundSignal.convert( sound );
    Signal nsignal = Rblint( signal, (int)(signal.length()*scale) );

    Sound nsound = SoundSignal.convert( nsignal );
    nsound.saveTo( args[2] );
  }
}

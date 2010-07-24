// $Id: Shint.java,v 1.1 2004/10/26 19:20:23 greg Exp $

public class Shint
{
  static public Signal shint( Signal signal, int newlen ) {

    int oldlen = signal.length();

    Signal nsignal = new Signal( newlen );

    double raw[][] = signal.raw();
    double nraw[][] = nsignal.raw();

    for (int nt = 0; nt<newlen; ++nt) {
if ((nt%16384)==0) System.out.println( nt+" of "+newlen );
      double ot = ((double)nt*(double)oldlen)/(double)newlen;

      nraw[0][nt] = raw[0][(int)ot];
    }

    return nsignal;
  }

  static public void main( String args[] ) throws Exception {
    Sound sound = new Sound( args[0] );
    double scale = new Double( args[1] ).doubleValue();

    Signal signal = SoundSignal.convert( sound );
    Signal nsignal = shint( signal, (int)(signal.length()*scale) );

    Sound nsound = SoundSignal.convert( nsignal );
    nsound.saveTo( args[2] );
  }
}

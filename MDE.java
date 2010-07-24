// $Id: MDE.java,v 1.1 2004/10/26 19:20:23 greg Exp $

import java.io.*;

public class MDE
{
  static public Sound mde( Sound sound, int largeRadius, int smallRadius,
      double scale ) throws IOException {

    int length = sound.length();

    Sound large = RMS.rms( sound, largeRadius );
    Sound small = RMS.rms( sound, smallRadius );
    Sound nu = new Sound( length );

    short raw[] = sound.raw();
    short largeRaw[] = large.raw();
    short smallRaw[] = small.raw();
    short nuRaw[] = nu.raw();

Sound gains = new Sound( length );
short gainRaw[] = gains.raw();
Sound diff = new Sound( length );
short diffRaw[] = diff.raw();
Sound nuenv = new Sound( length );
short nuenvRaw[] = nuenv.raw();

    for (int i=0; i<raw.length; ++i) {
      short l = largeRaw[i];
      short s = smallRaw[i];
diffRaw[i] = (short)(s-l);
      double ns = l + scale*(s-l);
      double gain = ns / s;
if (i==41993) {
  System.out.println( "s "+s+" l "+l+" ns "+ns+" gain "+gain );
}
nuenvRaw[i] = (short)ns;
gainRaw[i] = (short)(gain * 100);
      short samp = raw[i];
      int newSamp = (int)(gain * samp);
      if (newSamp>32767) {
        newSamp = 32767;
      } else if (newSamp<-32768) {
        newSamp = -32768;
      }
      nuRaw[i] = (short)newSamp;
    }
gains.saveTo( "gain.wav" );
large.saveTo( "large.wav" );
small.saveTo( "small.wav" );
diff.saveTo( "diff.wav" );
nuenv.saveTo( "nuenv.wav" );

    return nu;
  }

  static public void main( String args[] ) throws Exception {
    String infile = args[0];
    String outfile = args[1];
    int largeRadius = Integer.parseInt( args[2] );
    int smallRadius = Integer.parseInt( args[3] );
    double scale = Double.parseDouble( args[4] );

    Sound sound = new Sound( infile );
    sound = mde( sound, largeRadius, smallRadius, scale );
    sound.saveTo( outfile );
  }
}

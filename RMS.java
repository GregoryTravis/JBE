// $Id: RMS.java,v 1.1 2004/10/26 19:20:23 greg Exp $

public class RMS
{
  static public Sound rms( Sound sound, int windowRadius ) {
    int length = sound.length();

    short raw[] = sound.raw();

    Sound rms = new Sound( length );
    short rmsraw[] = rms.raw();

    for (int i=0; i<length; ++i) {
      long total = 0;
      for (int j=-windowRadius; j<=windowRadius; ++j) {
        int s = i + j;
        int samp=0;
        if (s>=0 && s<length) {
          samp = raw[s];
        }
        total += samp * samp;
      }
      int ms = (int)(total / ((windowRadius*2)+1));
      int r = (int)Math.sqrt( ms );
      if (r>32767 || r<-32768) {
        System.err.println( "err "+r );
        System.exit( 1 );
      }
      rmsraw[i] = (short)r;
    }

    return rms;
  }
}

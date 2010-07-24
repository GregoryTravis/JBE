// $Id: SoundSignal.java,v 1.1 2004/10/26 19:20:23 greg Exp $

public class SoundSignal
{
  static public Signal convert( Sound sound ) {
    int length = sound.length();
    Signal signal = new Signal( length );
    double draw[][] = signal.raw();
    short raw[] = sound.raw();

    for (int i=0; i<length; ++i) {
      draw[0][i] = ((double)raw[i])/32768;
    }

    return signal;
  }

  static public Sound convert( Signal signal ) {
    int length = signal.length();
    Sound sound = new Sound( length );

    double draw[][] = signal.raw();
    short raw[] = sound.raw();

    for (int i=0; i<length; ++i) {
      raw[i] = (short)(draw[0][i]*32768);
    }

    return sound;
  }
}

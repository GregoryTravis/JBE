// $Id: Util.java,v 1.1 2004/10/26 19:20:23 greg Exp $

import javax.sound.sampled.*;

public class Util
{
  static public AudioFormat stdFormat =
    new AudioFormat( AudioFormat.Encoding.PCM_SIGNED, 44100.0f, 16, 1, 2,
      44100.0f, false );

  static public AudioFormat stdFormatStereo =
    new AudioFormat( AudioFormat.Encoding.PCM_SIGNED, 44100.0f, 16, 2, 2,
      44100.0f, false );

  static public AudioFormat stdFormatStereo48 =
    new AudioFormat( AudioFormat.Encoding.PCM_SIGNED, 48000.0f, 16, 2, 2,
      48000.0f, false );

  static public AudioFormat stdFormat48 =
    new AudioFormat( AudioFormat.Encoding.PCM_SIGNED, 48000.0f, 16, 1, 2,
      48000.0f, false );

  static public SourceDataLine getOutputLine() {
    try {
      DataLine.Info info =
        new DataLine.Info( SourceDataLine.class, Util.stdFormat );
AudioFormat formats[] = info.getFormats();
for (int i=0; i<formats.length; ++i) {
  System.out.println( "Format "+formats[i] );
}
      SourceDataLine sdl = (SourceDataLine)AudioSystem.getLine( info );
      return sdl;
    } catch( LineUnavailableException lue ) {
      throw new RuntimeException( "Can't get output line" );
    }
  }
}

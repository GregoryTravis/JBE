// $Id: AudioReport.java,v 1.1 2004/10/26 19:20:23 greg Exp $

import java.io.*;
import java.util.*;
import javax.sound.sampled.*;

public class AudioReport
{
  static public void main( String args[] ) throws Exception {
    AudioFileFormat.Type types[] = AudioSystem.getAudioFileTypes();
    System.out.println( "Audio File Types" );
    for (int i=0; i<types.length; ++i) {
      System.out.println( "  "+types[i]+" (."+types[i].getExtension()+")" );
    }

    Mixer.Info mixers[] = AudioSystem.getMixerInfo();
    System.out.println( "Mixers" );
    for (int i=0; i<mixers.length; ++i) {
      System.out.println( "  "+mixers[i] );

      Mixer mixer = AudioSystem.getMixer( mixers[i] );

      Line.Info sources[] = mixer.getSourceLineInfo();
      System.out.println( "    Sources" );
      for (int j=0; j<sources.length; ++j) {
        System.out.println( "      "+sources[j] );
      }

      Line.Info targets[] = mixer.getTargetLineInfo();
      System.out.println( "    Targets" );
      for (int j=0; j<targets.length; ++j) {
        System.out.println( "      "+targets[j] );
      }
    }
    System.exit( 0 );
  }
}

// $Id: Normalize.java,v 1.1 2004/10/26 19:20:23 greg Exp $

public class Normalize
{
  static public Sound normalize( Sound sound ) {
    double scale = sound.maxVolume();
    sound = sound.dup();
    sound.gain( 1.0 / scale );
    return sound;
  }

  static public void main( String args[] ) throws Exception {
    for (int a=0; a<args.length; ++a) {
      String filename = args[a];
      if (Sound.isStereo( filename )) {
        Sound ss[] = Sound.loadStereoFrom( filename );
        ss[0] = normalize( ss[0] );
        ss[1] = normalize( ss[1] );
System.out.println( Util.stdFormatStereo48 );
        Sound.saveStereoTo( ss, filename, Util.stdFormatStereo48 );
      } else {
        Sound sound = new Sound( filename );
        sound = normalize( sound );
        sound.saveTo( filename );
      }
    }
  }
}

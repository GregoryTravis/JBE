// $Id: Volume.java,v 1.1 2004/10/26 19:20:23 greg Exp $

public class Volume
{
  static public Sound volume( double volume, Sound sound ) {
    double scale = sound.maxVolume();
    sound = sound.dup();
    sound.gain( volume / scale );
    return sound;
  }

  static public void main( String args[] ) throws Exception {
    double volume = Double.parseDouble( args[0] );

    for (int a=1; a<args.length; ++a) {
      String filename = args[a];
      if (Sound.isStereo( filename )) {
        Sound ss[] = Sound.loadStereoFrom( filename );
        ss[0] = volume( volume, ss[0] );
        ss[1] = volume( volume, ss[1] );
System.out.println( Util.stdFormatStereo48 );
        Sound.saveStereoTo( ss, filename, Util.stdFormatStereo48 );
      } else {
        Sound sound = new Sound( filename );
        sound = volume( volume, sound );
        sound.saveTo( filename );
      }
    }
  }
}

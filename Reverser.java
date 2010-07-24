// $Id: Reverser.java,v 1.1 2004/10/26 19:20:23 greg Exp $

public class Reverser implements PlugIn
{
  public Sound process( Sound sound ) {
    Sound rev = new Sound( sound.length() );
    short raw[] = sound.raw();
    short revraw[] = rev.raw();

    int oi = 0;
    for (int i=raw.length-1; i>=0; --i) {
      revraw[i] = raw[oi++];
    }

    return rev;
  }
}

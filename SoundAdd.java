// $Id: SoundAdd.java,v 1.1 2004/10/26 19:20:23 greg Exp $

public class SoundAdd
{
  static public Sound add( Sound sound0, Sound sound1 ) {
    int len0 = sound0.length();
    int len1 = sound1.length();
    int len = len0 > len1 ? len0 : len1;

    Sound nsound = new Sound( len );
    short nraw[] = nsound.raw();
    short raw0[] = sound0.raw();
    short raw1[] = sound1.raw();

    for (int i=0; i<len0; ++i)
      nraw[i] = raw0[i];

    for (int i=0; i<len1; ++i)
      nraw[i] += raw1[i];

    return nsound;
  }

  static public void main( String args[] ) throws Exception {
    Sound sound0 = new Sound( args[0] );
    Sound sound1 = new Sound( args[1] );
    String outfile = args[2];
    Sound sum = add( sound0, sound1 );
    sum.saveTo( outfile );
  }
}

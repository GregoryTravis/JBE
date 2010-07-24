// $Id: IdentityTest.java,v 1.1 2004/10/26 19:20:23 greg Exp $

public class IdentityTest
{
  static public void main( String args[] ) throws Exception {
    Sound sound = new Sound( args[0] );
    Signal signal = SoundSignal.convert( sound );
    Sound sound2 = SoundSignal.convert( signal );

    int length = sound.length();
    short raw[] = sound.raw();
    short raw2[] = sound2.raw();

    long acc=0;
    for (int i=0; i<length; ++i) {
      int ds = raw[i]-raw2[i];
      if (ds<0) ds = -ds;
      acc += ds;
    }

    double avg = (double)acc/(double)length;
    System.out.println( "diff: total "+acc+" avg "+avg );
  }
}

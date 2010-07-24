// $Id: Diff.java,v 1.1 2004/10/26 19:20:23 greg Exp $

public class Diff
{
  static public void main( String args[] ) throws Exception {
    Sound sound0 = new Sound( args[0] );
    Sound sound1 = new Sound( args[1] );

    if (sound0.length() != sound1.length()) {
      System.out.println( "lengths differ "+
        sound0.length()+" "+sound1.length() );
    }

    int length = Math.min( sound0.length(), sound1.length() );
    short raw0[] = sound0.raw();
    short raw1[] = sound1.raw();

    long acc=0;
    for (int i=0; i<length; ++i) {
      int ds = raw0[i]-raw1[i];
      if (ds<0) ds = -ds;
      acc += ds;
    }

    double avg = (double)acc/(double)length;
    System.out.println( "diff: total "+acc+" avg "+avg );
  }
}

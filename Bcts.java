// $Id: Bcts.java,v 1.1 2004/10/26 19:20:23 greg Exp $

public class Bcts
{
  static public Sound bcts( Sound sound, int newlen, int windowLen ) {
System.out.println( "old "+sound.length()+" new "+newlen );
    int len = sound.length();
    short raw[] = sound.raw();

    Sound nsound = new Sound( newlen );
    short nraw[] = nsound.raw();

    int nwindowLen = (int)((double)(windowLen+1)*((double)newlen/(double)len));

    for (int op=0; op<newlen; ++op) {
      int n = op / nwindowLen;
      int rr = op % nwindowLen;
      int m = rr / windowLen;
      int r = rr % windowLen;
int top = n*nwindowLen + m*windowLen + r;
if (top != op)
  System.out.println( "op "+op+" "+top );
      int ip = n * windowLen + r;
try {
      if (ip>=len) ip=len-1;
      nraw[op] = raw[ip];
} catch( ArrayIndexOutOfBoundsException ae ) {
  System.out.println( op+" of "+nraw.length+" <-- "+ip+" of "+raw.length );
  System.out.println( "n "+n+" m "+m+" r "+r+" wl "+windowLen+" nwl "+
    nwindowLen );
  throw ae;
}
    }

    return nsound;
  }

  static public void main( String args[] ) throws Exception {
    String infile = args[0];
    double scale = new Double( args[1] ).doubleValue();
    String outfile = args[2];
    int windowLen = new Integer( args[3] ).intValue();

    Sound sound = new Sound( infile );
    int newlen = (int)(sound.length()*scale);
    Sound nsound = Bcts.bcts( sound, newlen, windowLen );
    nsound.saveTo( outfile );
  }
}

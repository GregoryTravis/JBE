// $Id: Nblint.java,v 1.1 2004/10/26 19:20:23 greg Exp $

public class Nblint
{
  static {
    System.loadLibrary( "Nblint" );
  }

  synchronized static public Sound nblint( Sound sound, int newlen ) {
    short raw[] = sound.raw();

    Sound nsound = new Sound( newlen );
    short nraw[] = nsound.raw();

    native_nblint( nraw, raw );
    //System.out.println( "nblint "+sound.length()+" --> "+newlen );

    return nsound;
  }

  synchronized static public Sound nshint( Sound sound, int newlen ) {
    short raw[] = sound.raw();

    Sound nsound = new Sound( newlen );
    short nraw[] = nsound.raw();

    native_nshint( nraw, raw );
System.out.println( "nshint "+sound.length()+" --> "+newlen );

    return nsound;
  }

  static private native void native_nblint( short nraw[], short raw[] );
  static private native void native_nshint( short nraw[], short raw[] );

  static public void main( String args[] ) throws Exception {
    String infile = args[0];
    double scale = new Double( args[1] ).doubleValue();
    String outfile = args[2];
    int ntimes = args.length == 3 ? 1 : Integer.parseInt( args[3] );

    Sound sound = new Sound( infile );
    Sound nsound = null;
    for (int i=0; i<ntimes; ++i)
      nsound = nblint( sound, (int)(sound.length()*scale) );
    nsound.saveTo( outfile );
  }
}

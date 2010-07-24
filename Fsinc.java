// $Id: Fsinc.java,v 1.1 2004/10/26 19:20:23 greg Exp $

public class Fsinc
{
  public int oversamp;
  public int zc;
  public double val[];

  // width is ~ number 0-crossing
  public Fsinc( int oversamp, int zc ) {
    this.oversamp = oversamp;
    this.zc = zc;

    int len = zc*2*oversamp + 1;

    val = new double[len];

    double t = -zc;
    double dt = ((double)zc*2)/((double)len-1);
    for (int i=0; i<len; ++i) {
      val[i] = Sinc.sinc( t );
//System.out.println( "i "+i+" t "+t+" val "+val[i] );
      t += dt;
    }
  }

  public double sinc( double t ) {
    t *= oversamp;

    int len = val.length;
    int it = (int)Math.floor( t );
    double at = t - it;
    it += (len-1)/2;

    if (it==len-1)
      return val[it];
    else if (it<0 || it>=len) {
      System.out.println( "fsinc out of range t="+t+" zc="+zc );
      return 0;
    }

    double val0 = val[it], val1 = val[it+1];
    double val = val0*(1-at) + val1*at;

    return val;
  }

  static public void main( String args[] ) throws Exception {
    int oversamp = Integer.parseInt( args[0] );
    int zc = Integer.parseInt( args[1] );
    int len = Integer.parseInt( args[2] );
    String filename = args[3];

    Fsinc fsinc = new Fsinc( oversamp, zc );
    Signal signal = new Signal( len );
    double raw[][] = signal.raw();
    for (int i=0; i<len; ++i) {
//System.out.println( "i "+i+" fr "+((((double)i/(double)(len-1))*2*zc)-(double)zc) );
      double t = ((((double)i/(double)(len-1))*2*zc)-(double)zc);
//      double t = (2*zc*((double)i/(double)len-1))-zc;
      raw[0][i] = fsinc.sinc( t );
    }
    SoundSignal.convert( signal ).saveTo( filename );
  }
}

// $Id: Divide.java,v 1.1 2004/10/26 19:20:23 greg Exp $

public class Divide
{
  static public Sound divide( Sound a, Sound b, double unit ) {
    short araw[] = a.raw(), braw[] = b.raw();
    Sound ret = new Sound( araw.length );
    short rraw[] = ret.raw();
    unit *= 32767;
    long total = 0;
    for (int i=0; i<araw.length; ++i) {
      short as = araw[i], bs = braw[i];
      short rs = (short)(32767*((double)as/(double)bs));
      total += rs;
      rraw[i] = rs;
    }
    System.out.println( "avg "+(total/araw.length) );
    return ret;
  }

  static public void main( String args[] ) throws Exception {
    String rf = args[0], af = args[1], bf = args[2];
    double unit = new Double( args[3] ).doubleValue();
    Sound as = new Sound( af );
    Sound bs = new Sound( bf );
    Sound rs = divide( as, bs, unit );
    rs.saveTo( rf, Util.stdFormat48 );
    Sound haha = new Sound( rf );
  }
}

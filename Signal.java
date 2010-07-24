// $Id: Signal.java,v 1.1 2004/10/26 19:20:23 greg Exp $

public class Signal
{
  private double raw[][];

  public Signal( int length ) {
    raw = new double[2][length];
  }

  public int length() { return raw[0].length; }

  public double [][] raw() { return raw; }

  public Signal subSignal( int start, int len ) {
    Signal ns = new Signal( len );
    double nraw[][] = ns.raw();
    System.arraycopy( raw[0], start, nraw[0], 0, len );
    System.arraycopy( raw[1], start, nraw[1], 0, len );
    return ns;
  }

  public Signal dup() {
    return subSignal( 0, length() );
  }

  public void putSubSignal( Signal ssignal, int start ) {
    putSubSignal( ssignal, start, ssignal.length() );
  }

  public void putSubSignal( Signal ssignal, int start, int len ) {
    double ssraw[][] = ssignal.raw();
    System.arraycopy( raw[0], start, ssraw[0], 0, len );
    System.arraycopy( raw[1], start, ssraw[1], 0, len );
  }

  public Signal diff( Signal signal ) {
    int length = length();
    double sraw[][] = signal.raw();
    Signal ns = new Signal( length );
    double nraw[][] = ns.raw();
    for (int i=0; i<length; ++i) {
      nraw[0][i] = raw[0][i]-sraw[0][i];
      nraw[1][i] = raw[1][i]-sraw[1][i];
    }
    return ns;
  }

  public void compare( Signal signal ) {
    Signal diff = diff( signal );
    double raw[][] = diff.raw();
    double racc=0, iacc=0;
    for (int i=0; i<raw.length; ++i) {
      double re = raw[0][i], im = raw[1][i];
      re = re<0?-re:re;
      im = im<0?-im:im;
      racc += re;
      iacc += im;
    }
    System.out.println( "Real: Total "+racc+" avg "+(racc/raw.length) );
    System.out.println( "Imag: Total "+iacc+" avg "+(iacc/raw.length) );
  }

  public Signal magphase() {
    int length = length();
    Signal ns = new Signal( length );
    double nraw[][] = ns.raw();
    for (int i=0; i<length; ++i) {
      double sr = raw[0][i], si = raw[1][i];
      double m = Math.sqrt( sr*sr+si*si );
      double p = Math.atan2( sr, si );
      nraw[0][i] = m;
      nraw[1][i] = p;
    }
    return ns;
  }

  public Signal real() {
    return term( true );
  }

  public Signal imaginary() {
    return term( false );
  }

  public Signal term( boolean realp ) {
    Signal ns = new Signal( length() );
    double draw[][] = ns.raw(), sraw[][] = raw();    
    System.arraycopy( sraw[realp?0:1], 0, draw[0], 0, length() );
    return ns;
  }

  public void scale( double s ) {
    for (int i=0; i<raw.length; ++i) {
      raw[0][i] *= s;
      raw[1][i] *= s;
    }
  }

  static public void copy( Signal dest, int doff, Signal src, int soff,
      int count ) {
    double draw[][] = dest.raw(), sraw[][] = src.raw();
    System.arraycopy( sraw[0], soff, draw[0], doff, count );
    System.arraycopy( sraw[1], soff, draw[1], doff, count );
  }

  public Signal pad( int length ) {
    Signal nu = new Signal( length );
    copy( nu, 0, this, 0, this.length() );
    return nu;
  }

  public void add( Signal b ) {
    if (length() != b.length()) {
      throw new IllegalArgumentException(
        "Can't add signals, lens "+length()+"!="+b.length() );
    }

    double raw[][] = raw(), braw[][] = b.raw();
    for (int t=0; t<2; ++t) {
      double rraw[] = raw[t], brraw[] = braw[t];
      for (int i=0; i<rraw.length; ++i) {
        rraw[i] += brraw[i];
      }
    }
  }

  static public Signal add( Signal a, Signal b ) {
    Signal da = a.dup();
    da.add( b );
    return da;
  }

  public void sub( Signal b ) {
    if (length() != b.length()) {
      throw new IllegalArgumentException(
        "Can't sub signals, lens "+length()+"!="+b.length() );
    }

    double raw[][] = raw(), braw[][] = b.raw();
    for (int t=0; t<2; ++t) {
      double rraw[] = raw[t], brraw[] = braw[t];
      for (int i=0; i<rraw.length; ++i) {
        rraw[i] -= brraw[i];
      }
    }
  }

  static public Signal sub( Signal a, Signal b ) {
    Signal da = a.dup();
    da.sub( b );
    return da;
  }

  public double realrms() {
    double r[] = raw[0];
    double acc=0;
    for (int i=0; i<r.length; ++i) {
      double ri = r[i];
      acc += ri*ri;
    }
    return Math.sqrt( acc );
  }

  // Maybe this is right?
  public double rms() {
    return magphase().realrms();
  }

  public void mul( double d ) {
    double raw[][] = raw();
    for (int t=0; t<2; ++t) {
      double rraw[] = raw[t];
      for (int i=0; i<rraw.length; ++i) {
        rraw[i] *= d;
      }
    }
  }

  public int maxAbsBin( boolean realp ) {
    double raw[] = raw()[realp?0:1];

    if (raw.length==0)
      return 0;

    int b=0;
    double bv=raw[0];
    for (int i=1; i<raw.length; ++i) {
      double v = raw[i];
      if (v<0) v = -v;
      if (v>bv) {
        b = i;
        bv = v;
      }
    }

    return b;
  }

  public void normInPlace() {
    int b = magphase().maxAbsBin( true );
    double raw[][] = raw();
    double r = raw[0][b], i = raw[1][b];
    double len = Math.sqrt( r*r+i*i );
    mul( 1.0/len );
  }

  public Signal norm() {
    Signal ns = dup();
    ns.normInPlace();
    return ns;
  }
}

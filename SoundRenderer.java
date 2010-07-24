// $Id: SoundRenderer.java,v 1.1 2004/10/26 19:20:23 greg Exp $

import java.util.*;

public class SoundRenderer
{
  private Hashtable cache = new Hashtable();
  static private boolean useNative = true;

  static {
    try {
      Class.forName( "Nblint" );
    } catch( ClassNotFoundException cnfe ) {
      System.out.println( "Native Band-limited interpolator not found, using pure Java." );
      useNative = false;
    } catch( UnsatisfiedLinkError ule ) {
      System.out.println( "Native Band-limited interpolator not found, using pure Java." );
      useNative = false;
    }
  }

  public Sound render( Segment segment, int startMoment, int endMoment ) {
    String key = segment.state()+startMoment+""+endMoment;
    SoundRendererCacheEntry srce = (SoundRendererCacheEntry)cache.get( key );
    if (srce == null) {
      //System.out.println( "rendering sound" );
      Sound sound = render1( segment, startMoment, endMoment );
      if (sound != null) {
        srce = new SoundRendererCacheEntry( sound );
        cache.put( key, srce );
      }
    } else {
      //System.out.println( "getting sound from cache" );
    }

    if (srce == null)
      return null;

    srce.use();
    return srce.sound;
  }

  public void trimCache() {
    int removed=0;
    for (Enumeration e=cache.keys(); e.hasMoreElements();) {
      String key = (String)e.nextElement();
      SoundRendererCacheEntry srce = (SoundRendererCacheEntry)cache.get( key );
      if (srce.age()) {
        cache.remove( key );
        removed++;
      }
    }
    if (removed!=0) {
      System.out.println( "trim cache: "+removed+" size "+cacheSize() );
    }
  }

  public int cacheSize() {
    int size=0;
    for (Enumeration e=cache.keys(); e.hasMoreElements();) {
      String key = (String)e.nextElement();
      SoundRendererCacheEntry srce = (SoundRendererCacheEntry)cache.get( key );
      size += srce.sound.length();
    }
    return size;
  }

  public Sound render1( Segment segment, int startMoment, int endMoment ) {

    int ss = segment.getStartSample();
    int se = segment.getEndSample();

    //System.out.println( "ss "+ss+" se "+se );

    int ms = segment.getStartMoment();
    int me = segment.getEndMoment();

    //System.out.println( "ms "+ms+" me "+me );

    double rs = segment.getStretchFactor();

    //System.out.println( "rs "+rs+" "+(rs-1.0) );

    double is = rs * ss;
    double ie = rs * se;

    //System.out.println( "is "+is+" ie "+ie );

    double ri = (me-ms) / (ie-is);

    //System.out.println( "ri "+ri+" "+(ri-1.0) );

    int msp = Math.max( startMoment, ms );
    int mep = Math.min( endMoment, me );

    //System.out.println( "msp "+msp+" mep "+mep+" len "+(mep-msp) );

    double isp = ((msp-ms)/ri) + is;
    double iep = ((mep-ms)/ri) + is;

    //System.out.println( "isp "+isp+" iep "+iep+" len "+(iep-isp) );

    double ssp = ((isp-is)/rs) + ss;
    double sep = ((iep-is)/rs) + ss;
    //System.out.println( "ssp "+ssp+" sep "+sep+" len "+(sep-ssp) );

    Sound startSound = segment.getSound().subSound( (int)ssp, (int)sep );

    if (startSound == null)
      return null;

    int slenp = (int)(iep-isp);
    Sound strSound = null;
    if (rs==1.0) {
      strSound = startSound;
      //System.out.println( "SKIPPING STRETCH" );
    } else {
      strSound = Pts.pts( startSound, slenp );
    }

    int ilenp = (int)(mep-msp);
    Sound intSound = null;
    if (ri==1.0) {
      intSound = strSound;
      //System.out.println( "SKIPPING INTERPOLATION" );
    } else {
      if (useNative) {
        intSound = Nblint.nblint( strSound, ilenp );
      } else {
        intSound = Jblint.jblint( strSound, ilenp );
      }
    }

    return intSound;
  }
}

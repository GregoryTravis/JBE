// $Id: Clik.java,v 1.1 2004/10/26 19:20:23 greg Exp $

import java.io.*;

public class Clik
{
  static private String cliksdir = "/Users/gmt/proj/jbe";

  static public Sound generate( double bpm, int measures, int timeSigNum,
      int subdiv ) {
    int length =
      (int) ((((double)(measures*timeSigNum))/(bpm/60.0))*Constants.sampRate);
    Sound sound = new Sound( length );

    Sound lo = null, hi = null, first = null;

    try {
      lo = SoundCache.load( cliksdir + "/loclik.wav" );
      hi = SoundCache.load( cliksdir + "/hiclik.wav" );
      first = SoundCache.load( cliksdir + "/firstclik.wav" );
    } catch( IOException ie ) {
      System.out.println( "Can't load cliks." );
    }

    int nbeats = measures*subdiv*timeSigNum;
    int beatgap = length / nbeats;

    for (int i=0; i<nbeats; ++i) {
      sound.mixOnto( i==0?first:((i%(subdiv*timeSigNum))==0?lo:hi),
        beatgap*i );
    }

    return sound;
  }
}

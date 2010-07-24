// $Id: Groove.java,v 1.1 2004/10/26 19:20:23 greg Exp $

public class Groove
{
  private double bpm;
  private int measures;
  private int timeSigNum;
  private int subdiv;

  private int moments[];

  public Groove( int startMoment, double bpm, int measures, int timeSigNum, int subdiv ) {
    this.bpm = bpm;
    this.measures = measures;
    this.timeSigNum = timeSigNum;
    this.subdiv = subdiv;

    int numBeats = measures*timeSigNum*subdiv;
    double sdlen = (60.0/(bpm*subdiv))*Constants.sampRate;

    moments = new int[numBeats+1];

    for (int i=0; i<=numBeats; ++i)
      moments[i] = startMoment + (int)(i*sdlen);
  }

  public int closestMoment( int m ) {
    int best=-1, bestd=0;
    for (int i=0; i<moments.length; ++i) {
      int d = Math.abs( m-moments[i] );
      if (d<bestd || best==-1) {
        best = i;
        bestd = d;
      }
    }
    return best==-1 ? m : moments[best];
  }

  public int[] moments() { return moments; }
}

// $Id: SoundAccumulator.java,v 1.1 2004/10/26 19:20:23 greg Exp $

public class SoundAccumulator
{
  private SoundRenderer soundRenderer;
  private int startMoment, endMoment;
  private int raw[];

  public SoundAccumulator( SoundRenderer soundRenderer,
      int startMoment, int endMoment ) {
    this.soundRenderer = soundRenderer;
    this.startMoment = startMoment;
    this.endMoment = endMoment;

    raw = new int[endMoment-startMoment];
  }

  public void add( Segment segment ) {
    if (segment.getEndMoment() <= startMoment ||
        segment.getStartMoment() >= endMoment) {
      return;
    }

    int ms = segment.getStartMoment();
    int me = segment.getEndMoment();
    int msp = Math.max( startMoment, ms );
    int mep = Math.min( endMoment, me );

    Sound rsound = soundRenderer.render( segment, startMoment, endMoment );

    if (rsound != null) {
      double gain = segment.getGain();

      short rraw[] = rsound.raw();
      for (int i=0; i<rraw.length; ++i) {
        raw[msp+i-startMoment] += (int)(rraw[i] * gain);
      }
    }
  }

  private void normalize() {
    int mx = 0;
    for (int i=0; i<raw.length; ++i) {
      int s = raw[i];
      if (s<0) s = -s;
      if (s>mx)
        mx = s;
    }

    if (mx==0)
      return;

    double scale = 32767.0 / mx;

    for (int i=0; i<raw.length; ++i) {
      raw[i] *= scale;
    }
  }

  public Sound getSound() {
    normalize();
    short sraw[] = new short[raw.length];
    for (int i=0; i<raw.length; ++i)
      sraw[i] = (short)raw[i];
    return new Sound( sraw );
  }
}

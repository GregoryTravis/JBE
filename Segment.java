// $Id: Segment.java,v 1.1 2004/10/26 19:20:23 greg Exp $

import java.awt.*;
import java.io.*;
import javax.swing.*;

public class Segment extends JPanel
{
  private JBE jbe;
  private Sound sound;
  private int startMoment, endMoment, startSample, endSample;
  private Mark startMark, endMark;
  private double stretchFactor = 1.0;
  private int wid, ht;
  private int row;
  private int colorInx=-1;
  static private final Color waveColor = Color.green;
  static private final Color mutedWaveColor = Color.black;
  static private final Color rectColor = Color.green;
  static private final Color selectedRectColor = Color.white;
  static private final Color linkRectColor = Color.white;
  static private final Color stickyCircleColor = Color.white;
  static private final int linkRectWidth = 3;
  private boolean muted = false;
  private double gain = 1.0;
  private double drawGain = 1.0;
  private boolean selected;

  public Segment( JBE jbe, Sound sound ) {
    this( jbe, sound, 0, sound.length(), 0, sound.length() );
  }

  public Segment( JBE jbe, Sound sound, int startMoment ) {
    this( jbe, sound, startMoment, startMoment+sound.length(), 0, sound.length() );
  }

  public Segment( JBE jbe, Sound sound, int startMoment, int endMoment ) {
    this( jbe, sound, startMoment, endMoment, 0, sound.length() );
  }

  public Segment( JBE jbe, Sound sound, int startMoment, int endMoment,
      int startSample, int endSample ) {
    this.jbe = jbe;
    this.sound = sound;
    this.startMoment = startMoment;
    this.endMoment = endMoment;
    this.startSample = startSample;
    this.endSample = endSample;

    startMark = new Mark( this, true );
    endMark = new Mark( this, false );

//    double mv = sound.maxVolume();
//    if (mv != 0)
//      drawGain = 1/mv;

    setOpaque( true );
  }

  public boolean getMuted() { return muted; }
  public void setMuted( boolean muted ) {
    this.muted = muted;
    setDirty( true );
  }

  public boolean getSelected() { return selected; }
  public void setSelected( boolean selected ) {
    this.selected = selected;
  }

  public double getGain() { return gain; }
  public void setGain( double gain ) {
    this.gain = gain;
  }

  public double getDrawGain() { return drawGain; }
  public void setDrawGain( double drawGain ) {
    this.drawGain = drawGain;
  }

  public int getRow() { return row; }
  public void setRow( int row ) {
    this.row = row;
    setDirty( true );
  }

  public JBE getJBE() { return jbe; }
  public Sound getSound() { return sound; }

  public Mark getStartMark() { return startMark; }
  public Mark getEndMark() { return endMark; }

  public boolean ownsMark( Mark mark ) {
    return mark.equals( getStartMark() ) || mark.equals( getEndMark() );
  }

  public int getStartMoment() { return startMoment; }
  public int getEndMoment() { return endMoment; }
  public int getStartSample() { return startSample; }
  public int getEndSample() { return endSample; }

  public int getSampleLen() {
    return getEndSample() - getStartSample();
  }

  public int getMomentLen() {
    return getEndMoment() - getStartMoment();
  }

  public double getStretchFactor() { return stretchFactor; }
  public void setStretchFactor( double stretchFactor ) {
    this.stretchFactor = stretchFactor;
  }

  public void setStartMoment( int startMoment ) {
    this.startMoment = startMoment;
    resetBounds();
    setDirty( true );
  }

  public void setEndMoment( int endMoment ) {
    this.endMoment = endMoment;
    resetBounds();
    setDirty( true );
  }

  public void setStartSample( int startSample ) {
    this.startSample = startSample;
    setDirty( true );
  }

  public void setEndSample( int endSample ) {
    this.endSample = endSample;
    setDirty( true );
  }

  public int screenToMoment( int x ) {
    return startMoment +
      (int)((endMoment-startMoment) *((double)x/(double)wid));
  }

  public int momentToScreen( int m ) {
    return (int)(wid *
      ((double)(m-startMoment)/(double)(endMoment-startMoment)));
  }

  private int componentToSample( int x ) {
    return startSample +
      (int)((endSample-startSample) * ((double)x/(double)wid));
  }

  public int getCenterMoment() {
    return (int)(startMoment + 0.5 * (endMoment-startMoment));
  }

  public int getCenterScreen() {
    return momentToScreen( getCenterMoment() );
  }

  public Sound getSubSound() {
    return sound.subSound( startSample, endSample );
  }

  void resetBounds() {
    jbe.resetBounds( this );
  }

  public int momentToSample( int m ) {
    double rat = ((double)(m-startMoment)) /
                 ((double)(endMoment-startMoment));
    return (int)(startSample + rat*(endSample-startSample));
  }

  public void slideToMoment( int m ) {
    endMoment += m-startMoment;
    startMoment = m;
    resetBounds();
    updateNeighbors();
    setDirty( true );
  }

  public void slideToSample( int s ) {
    endSample += s-startSample;
    startSample = s;
    updateNeighbors();
    setDirty( true );
  }

  private void updateNeighbors() {
    startMark.updateOther();
    endMark.updateOther();
  }

  private void updateDimensions() {
    Dimension d = getSize();
    wid = d.width;
    ht = d.height;
  }

  public void paintComponent( Graphics g ) {
    updateDimensions();

    short raw[] = sound.raw();

    g.setColor( getColor( true ) );
    g.fillRect( 0, 0, wid-1, ht-1 );

    g.setColor( muted ? mutedWaveColor : waveColor );

    // find limits of on-screen sample
    Rectangle r = getBounds();
    Rectangle jber = getJBE().getBounds();
    int lx = r.x, hx = r.x+r.width;
    if (lx<jber.x) lx = jber.x;
    if (hx>jber.x+jber.width) hx = jber.x+jber.width;
    lx -= r.x;
    hx -= r.x;

    double si = componentToSample( lx );
    double dsi = ((double)(endSample-startSample)) / wid;

    for (int x=lx; x<hx; ++x) {
//      int s = componentToSample( x );

      int s = (int)si;
      si += dsi;

      if (s<0 || s>=raw.length)
        continue;
      short sample = raw[s];
      int h = (int)(((double)sample/32768) * (ht/2) * drawGain);
      g.drawLine( x, (ht/2), x, (ht/2)-h );
    }

    //g.setColor( rectColor );
    g.setColor( selected ? selectedRectColor : rectColor );
    g.drawRect( 0, 0, wid-1, ht-1 );
    if (selected)
      g.drawRect( 1, 1, wid-3, ht-3 );

    Mark m = getStartMark();
    if (m != null) {
      if (m.isLinked()) {
        g.setColor( linkRectColor );
        g.fillRect( 0, ht/7, linkRectWidth, ht/7 );
      }
      if (m.isSticky()) {
        g.setColor( stickyCircleColor );
        g.fillRect( 0, (5*ht)/7, linkRectWidth, ht/7 );
      }
    }
    m = getEndMark();
    if (m != null) {
      if (m.isLinked()) {
        g.setColor( linkRectColor );
        g.fillRect( wid-linkRectWidth-1, ht/7, linkRectWidth, ht/7 );
      }
      if (m.isSticky()) {
        g.setColor( stickyCircleColor );
        g.fillRect( wid-linkRectWidth-1, (5*ht)/7, linkRectWidth, ht/7 );
      }
    }
  }

  public String toString() {
    return "["+sound+" ("+startMoment+","+endMoment+") ("+
      startSample+", "+endSample+") {"+row+"}]";
  }

  public boolean contains( int x, int y ) {
    return getBounds().contains( x, y );
  }

  public Color getColor( boolean selected ) {
    if (colorInx == -1)
      colorInx = pickColors();
    return allColors[colorInx][selected?1:0];
  }

  public int getColorInx() { return colorInx; }
  public void setColorInx( int colorInx ) {
    this.colorInx = colorInx;
  }

  static private int colorRGB[][] = {
    { 127, 40, 40 }, { 40, 127, 40 }, { 40, 40, 127 },
    { 127, 127, 40 }, { 127, 40, 127 }, { 40, 127, 127 },
    { 127, 127, 127 }
  };
  static private Color allColors[][];

  static {
    allColors = new Color[colorRGB.length][2];
    for (int i=0; i<colorRGB.length; ++i) {
      allColors[i][0] =
        new Color( colorRGB[i][0], colorRGB[i][1], colorRGB[i][2] );
      allColors[i][1] =
        new Color( (int)((colorRGB[i][0]*4)/3),
                   (int)((colorRGB[i][1]*4)/3),
                   (int)((colorRGB[i][2]*4)/3) );
    }
  }

  static private int pickColorInx;
  synchronized static private int pickColors() {
    int ci = pickColorInx;
    pickColorInx = (pickColorInx+1)%allColors.length;
    return ci;
  }

  // This doesnt respect stretchFactor
  public Segment[] split( int m ) {
    if (m<=startMoment || m>=endMoment)
      return null;
    int ms = momentToSample( m );
    Segment sss[] = new Segment[2];
    sss[0] = new Segment( jbe, sound, startMoment, m, startSample, ms );
    sss[1] = new Segment( jbe, sound, m, endMoment, ms, endSample );
    sss[0].getEndMark().link( sss[1].getStartMark() );

    sss[0].getStartMark().link( getStartMark().getLink() );
    sss[1].getEndMark().link( getEndMark().getLink() );

    sss[0].setColorInx( colorInx );
    sss[1].setColorInx( colorInx );

    sss[0].setGain( gain );
    sss[1].setGain( gain );

    sss[0].setDrawGain( drawGain );
    sss[1].setDrawGain( drawGain );

    return sss;
  }

  public void setDirty( boolean dirty ) {
    jbe.setDirty( dirty );
  }

  DB writeToDB() {
    DB db = new DB();
    db.put( "name", sound.getSource() );
    db.put( "startMoment", startMoment );
    db.put( "endMoment", endMoment );
    db.put( "startSample", startSample );
    db.put( "endSample", endSample );
    db.put( "row", row );
    db.put( "gain", gain );
    db.put( "muted", muted );
    db.put( "colorInx", colorInx );
    DB sticky = new DB();
    sticky.put( "start", getStartMark().isSticky() );
    sticky.put( "end", getEndMark().isSticky() );
    db.put( "sticky", sticky );
    return db;
  }

  static Segment readFromDB( JBE jbe, DB db ) throws IOException {
    String name = db.getString( "name" );
    int startMoment = db.getInt( "startMoment" );
    int endMoment = db.getInt( "endMoment" );
    int startSample = db.getInt( "startSample" );
    int endSample = db.getInt( "endSample" );
    int row = db.getInt( "row" );

    double gain = db.getDouble( "gain" );
    boolean muted = db.getBoolean( "muted" );
    int colorInx = db.getInt( "colorInx" );

    Segment s = new Segment( jbe, SoundCache.load( name ),
        startMoment, endMoment, startSample, endSample );

    s.setRow( row );
    s.setMuted( muted );
    s.setGain( gain );
    s.setColorInx( colorInx );

    DB sticky = db.getDB( "sticky" );
    if (sticky != null) {
      s.getStartMark().setSticky( sticky.getBoolean( "start" ) );
      s.getEndMark().setSticky( sticky.getBoolean( "end" ) );
    }

    return s;
  }

  // Copy data into new sound object, and create
  // new segment to fit it so that it sounds the same
  public Segment copyOut() {
    Sound nus = getIncludedSound();
    return new Segment( jbe, nus, startMoment, endMoment );
  }

  public Sound getIncludedSound() {
    Sound nus = sound.subSound( startSample, endSample );
    return nus;
  }

  public Segment dup() {
    Segment seg = new Segment( jbe, sound, startMoment, endMoment,
      startSample, endSample );
    seg.setColorInx( colorInx );
    seg.setGain( gain );
    seg.setDrawGain( drawGain );
    return seg;
  }

  public String state() {
    return sound.hashCode()+""+startMoment+""+endMoment+""+
      startSample+""+endSample+""+stretchFactor;
  }

  // Join segment to segments on either side.
  // Okay if only one bounding segment
  // If none on either side, dont bother
  public Segment joinBoth() {
    if (startMark.isLinked() && endMark.isLinked()) {
      Segment ab = startMark.join();
      Segment abc = ab.getEndMark().join();
      return abc;
    } else if (startMark.isLinked()) {
      Segment ab = startMark.join();
      return ab;
    } else if (endMark.isLinked()) {
      Segment bc = endMark.join();
      return bc;
    }

    return null;
  }

  public Segment getPrevNeighbor() {
    return startMark.getLink().getLeftSegment();
  }

  public Segment getNextNeighbor() {
    return endMark.getLink().getRightSegment();
  }
}

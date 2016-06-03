// $Id: JBE.java,v 1.2 2004/10/26 20:19:54 greg Exp $

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;

public class JBE extends JPanel implements Runnable, LooperListener
{
  private Frame frame;
  private Groove groove;
  private double bpm = 120;
  private int measures = 2, timeSigNum=4;
  private int subdiv = 4;
  private int leftMoment, rightMoment;
  private int loopLeftMoment, loopRightMoment;
  private int numRows = 1;
  private Vector segments = new Vector();
  private int wid, ht;
  private int mousex, mousey;
  private Looper looper;
  private Sound currentRendering;
  private Object updateLock = new Object();
  private boolean needsUpdate = false;
  private String docName;
  private DirectoryCompleter directoryCompleter = new DirectoryCompleter();
  private boolean guibug = false;

  private boolean dirty = false;
  private final int gapSize = 10;
  static private int realTop = 35;

  private boolean snapping = true;

  private boolean audition = false;
  private boolean pause = false;
  private boolean auditionGap = false;
  static private final double maxAuditionLength = 4.0;
  static private final int maxAuditionSamples =
    (int)(maxAuditionLength * Constants.sampRate);

  private final double auditionBlankLen = 1.0;
  private final int auditionBlankLenS =
    (int)(auditionBlankLen*Constants.sampRate);

  private SoundRenderer soundRenderer = new SoundRenderer();

  // uncovering
  private Mark uncoveringMark;
  private int markSample, markMoment;
  private int lastMarkScreen;
  private int movingDX, movingDY;
  // stretching
  private Mark stretchingMark;
  // tstretching
  private Mark tstretchingMark;
  private int tstretchLeftLen, tstretchRightLen;
  // moving segment
  private Segment movingSegment;
  private long movingStartX;
  private int movingStartTime;
  // sliding segment
  private Segment slidingSegment;
  private int slidingStartMoment;
  private int slidingStartSample;
  private int slidingDeltaMoment;
  // moving all
  private boolean movingAll = false;
  private int movingAllMoment;

  private int grooveColorLevels[] = { 255, 240, 225, 210, 195, 180 };
  private Color grooveColors[] = new Color[grooveColorLevels.length];
  
  private Color textColor = new Color( 190, 190, 190 );
  private Color filenameColor = new Color( 220, 220, 220 );

  static private final Font bigFont = new Font( "Courier", Font.BOLD, 48 );
  private FontMetrics bigFM = getFontMetrics( bigFont );
  static private final Font smallFont = new Font( "Courier", Font.BOLD, 24 );
  private FontMetrics smallFM = getFontMetrics( smallFont );
  static private final Font tinyFont = new Font( "Courier", Font.BOLD, 16 );
  private FontMetrics tinyFM = getFontMetrics( tinyFont );

  private UndoStack undoStack = new UndoStack( 20 );

  private boolean showSample = false;

  private Keys keys;
  private String keyStatus;

  public JBE() {
    setupKeys();

    setLayout( null );
    setBackground( Color.black );
    setOpaque( true );

    initGrooveColors();

    addListeners();

    setupLoop();
    fullPosition();

    looper = new Looper( this );

    new Thread( this, "Update Sound" ).start();
  }

  private void setupKeys() {
    keys = new Keys( this );

    keys.add( "[%32]", "pageForward" );
    keys.add( "\\s[%32]", "pageBackward" );
    keys.add( "\\c[s]", "makeClean" );
    keys.add( "\\c[x]", "deleteSegment" );
    keys.add( "[s] [s]", "saveSegment" );
    keys.add( "[s] [n]", "nameSelectedSegment" );
    keys.add( "\\s[g] [b]", "setBPM" );
    keys.add( "\\s[g] [m]", "changeMeasures" );
    keys.add( "\\c[z]", "undo" );
    keys.add( "\\c[r]", "redo" );
    keys.add( "\\s[r] [s]", "showRender" );
    keys.add( "\\s[r] [r]", "renderRow" );
    keys.add( "\\s[r] \\s[r]", "replaceRowWithRendering" );
    keys.add( "\\s[x] [s]", "exportSegment" );
    keys.add( "\\s[x] [r]", "exportRow" );
    keys.add( "\\s[x] [d]", "exportRenderedSegment" );
    keys.add( "\\s[x] \\s[d]", "exportRenderedSegments" );
    keys.add( "\\s[x] \\s[r]", "exportRows" );
    keys.add( "[p] [a]", "applyPlugIn" );
    keys.add( "[B] [n]", "browseNext" );
    keys.add( "[B] [p]", "browsePrev" );
    keys.add( "[B] [t]", "browseToStep" );
    keys.add( "[B] [B]", "browse" );

    keys.add( "[A]", "flipAudition" );
    keys.add( "[7]", "flipAuditionGap" );
    keys.add( "[m]", "splitSegment" );
    keys.add( "[J]", "joinSegmentBoth" );
    keys.add( "[c]", "flipSticky" );
    keys.add( "[C]", "flipStickyRow" );
    keys.add( "[j]", "joinSegments" );
    keys.add( "[n]", "nameSelectedSegment" );
    keys.add( "[z]", "zoomIn" );
    keys.add( "[x]", "zoomOut" );
    keys.add( "[l]", "loadSound" );
    keys.add( "[N]", "addRow" );
    //keys.add( "[V]", "changeMeasures" );
    //keys.add( "[B]", "setBPM" );
    //keys.add( "[D]", "deleteSegment" );
    keys.add( "[d]", "dumpInfo" );
    keys.add( "[M]", "muteSegment" );
    keys.add( "[4]", "muteRow" );
    keys.add( "[5]", "unMuteRow" );
    keys.add( "[a]", "aB" );
    keys.add( "[F]", "fullPosition" );
    keys.add( "[Q]", "doQuit" );
    keys.add( "[S]", "makeClean" );
    keys.add( "[Z]", "dupInPlace" );
    keys.add( "[v]", "dup" );
    keys.add( "[V]", "copyOut" );
    //keys.add( "[y]", "exportSegment" );
    keys.add( "[w]", "fileSegment" );
    //keys.add( "[T]", "exportRow" );
    //keys.add( "[W]", "exportRows" );
    keys.add( "[P]", "flipSnapping" );
    keys.add( "[9]", "removeEmptyRows" );
    keys.add( "[(]", "collectCache" );
    keys.add( "[K]", "makeClik" );
    keys.add( "[g]", "setGain" );
    keys.add( "[1]", "vzoomOut" );
    keys.add( "[2]", "vzoomIn" );
    keys.add( "[3]", "vzoomNorm" );
    keys.add( "[!]", "vzoomOutRow" );
    keys.add( "[@]", "vzoomInRow" );
    keys.add( "[#]", "vzoomNormRow" );
    //keys.add( "[O]", "showRender" );
    //keys.add( "[u]", "undo" );
    //keys.add( "[r]", "redo" );
    //keys.add( "[R]", "renderRow" );
    keys.add( "[t]", "report" );
    //keys.add( "[s]", "saveSegment" );
    keys.add( "[*]", "breakRowLinks" );
    keys.add( "[b]", "breakLink" );
    keys.add( "[L]", "makeLink" );
    keys.add( "[8]", "flipShowSample" );
    keys.add( "[i]", "dupAndWriteSegment" );
    keys.add( "[I] [I]", "loopToSegment" );
    keys.add( "[I] [l]", "leftLoopToMark" );
    keys.add( "[I] [r]", "rightLoopToMark" );
    keys.add( "[6]", "nextSound" );
    keys.add( "[k]", "loopFit" );
    keys.add( "[o]", "nextSoundAndSameSize" );
    keys.add( "[^]", "halveLoop" );
    keys.add( "[&]", "doubleLoop" );
    keys.add( "[-]", "backwardLoop" );
    keys.add( "[=]", "forwardLoop" );
    //keys.add( "[%]", "replaceRowWithRendering" );
    keys.add( "[)]", "deleteRow" );
    keys.add( "[:]", "flipPause" );
    keys.add( "\\c[m]", "moveSegmentToSecond" );
  }

  private void setFrame( Frame frame ) {
    this.frame = frame;
  }

  private void initGrooveColors() {
    for (int i=0; i<grooveColorLevels.length; ++i) {
      int v = grooveColorLevels[i];
      grooveColors[i] = new Color( v, v, v );
    }
  }

  private void addListeners() {
    addMouseListener( new MouseListener() {
      public void mousePressed( MouseEvent me ) {
        mousex = me.getX();
        mousey = me.getY();

        int mods = me.getModifiers() | me.getModifiers();
        boolean alt = (mods&InputEvent.ALT_MASK)==InputEvent.ALT_MASK;
        boolean control = (mods&InputEvent.CTRL_MASK)==InputEvent.CTRL_MASK;
        boolean shift = (mods&InputEvent.SHIFT_MASK)==InputEvent.SHIFT_MASK;

        System.out.println( alt+" "+control+" "+shift );

        if (!alt && !control && !shift) {
          Object nt = nearestThing();

          if (nt != null) {
            if (nt instanceof Mark) {
              Segment ms = getContainingSegment();
              selectSegment( ms );

              stretchingMark = (Mark)nt;
              if (stretchingMark != null) {
                movingDX = stretchingMark.getScreen()-mousex;
                undoSaveChange();
              }
            } else if (nt instanceof Segment) {
              movingSegment = (Segment)nt;
              selectSegment( movingSegment );
              if (movingSegment != null) {
                undoSaveChange();
                movingDX = mousex-movingSegment.getStartMark().getScreen();
                movingDY = mousey-centerOfRow( whichRow() );
                movingStartX = mousex;
                movingStartTime = (int) System.currentTimeMillis();
              }
            } else if (nt instanceof JBE) {
              // moving background
              movingAll = true;
              movingAllMoment = screenToMoment( mousex );
            } else {
              throw new RuntimeException( "Don't know what to do with "+nt );
            }
          }
        } else if (!alt && control && shift) {
          Segment ms = getContainingSegment();
          selectSegment( ms );

          tstretchingMark = nearestMark();

          if (tstretchingMark != null) {
            undoSaveChange();

            if (tstretchingMark.getLeftSegment()!=null)
              tstretchLeftLen =
                tstretchingMark.getLeftSegment().getMomentLen();

            if (tstretchingMark.getRightSegment()!=null)
              tstretchRightLen =
                tstretchingMark.getRightSegment().getMomentLen();

            movingDX = tstretchingMark.getScreen()-mousex;
          }
        } else if (!alt && !control && shift) {
          uncoveringMark = nearestMark();
          if (uncoveringMark != null) {
            undoSaveChange();
            markSample = uncoveringMark.getSample();
            markMoment = uncoveringMark.getMoment();
            lastMarkScreen = mousex;
            movingDX = uncoveringMark.getScreen()-mousex;
          }
/*
        } else if (!alt && control && shift) {
          movingSegment = getContainingSegment();
          if (movingSegment != null) {
            undoSaveChange();
            movingDX = mousex-movingSegment.getStartMark().getScreen();
            movingDY = mousey-centerOfRow( whichRow() );
          }
*/
        } else if (alt && !control && shift) {
          slidingSegment = getContainingSegment();
          if (slidingSegment != null) {
            undoSaveChange();
            slidingStartMoment = slidingSegment.getStartMoment();
            slidingStartSample = slidingSegment.getStartSample();
            slidingDeltaMoment = slidingStartMoment - screenToMoment( mousex );
          }
        } else if (!alt && control && !shift) {
          movingAll = true;
          movingAllMoment = screenToMoment( mousex );
        } else {
          System.out.println( alt+" "+control+" "+shift+"?" );
        }
      }
      public void mouseEntered( MouseEvent me ) {
      }
      public void mouseExited( MouseEvent me ) {
      }
      public void mouseReleased( MouseEvent me ) {
        if (stretchingMark != null) {
          stretchingMark = null;
          updateSound();
        } else if (tstretchingMark != null) {

          Segment seg = tstretchingMark.getLeftSegment();
          if (seg !=null) {
            int newlen = seg.getMomentLen();
            double npfm = (double)newlen / (double)tstretchLeftLen;
            seg.setStretchFactor( npfm * seg.getStretchFactor() );
          }

          seg = tstretchingMark.getRightSegment();
          if (seg !=null) {
            int newlen = seg.getMomentLen();
            double npfm = (double)newlen / (double)tstretchRightLen;
            seg.setStretchFactor( npfm * seg.getStretchFactor() );
          }

          tstretchingMark = null;
          updateSound();
        } else if (uncoveringMark != null) {
          uncoveringMark = null;
          updateSound();
        } else if (movingSegment != null) {
          long now = System.currentTimeMillis();
          long clickLen = now - movingStartTime;
          System.out.println( "Moved in "+clickLen );
          movingSegment = null;
          updateSound();
        } else if (slidingSegment != null) {
          slidingSegment = null;
          updateSound();
        } else if (movingAll) {
          movingAll = false;
        }
      }
      public void mouseClicked( MouseEvent me ) {
      }
    } );

    addMouseMotionListener( new MouseMotionListener() {
      public void mouseDragged( MouseEvent me ) {
        mousex = me.getX();
        mousey = me.getY();

        if (stretchingMark != null) {
          stretchingMark.setMoment(
            snap( screenToMoment( mousex+movingDX ) ) );
        } else if (tstretchingMark != null) {
          tstretchingMark.setMoment(
            snap( screenToMoment( mousex+movingDX ) ) );
        } else if (uncoveringMark != null) {
          int rs = mousex+movingDX;
          int nm = snap( screenToMoment( rs ) );
          uncoveringMark.setMoment( markMoment );
          uncoveringMark.setSample( markSample );
          int ns = uncoveringMark.sidedGetSample( nm, mousex<lastMarkScreen );
          uncoveringMark.setMoment( nm );
          uncoveringMark.setSample( ns );
          lastMarkScreen = mousex;

          //uncoveringMark.uncoverSetMoment(
          //  snap( screenToMoment( mousex+movingDX ) ) );
        } else if (movingSegment != null) {
System.out.println( "Moving by "+movingDX );
          movingSegment.slideToMoment(
            snap( screenToMoment( mousex-movingDX ) ) );
          int newCenter = mousey-movingDY;
          int nr = whichRow( newCenter );
          if (nr != movingSegment.getRow()) {
            movingSegment.setRow( nr );
            movingSegment.repaint();
          }
        } else if (slidingSegment != null) {
          slidingSegment.slideToMoment( slidingStartMoment );
          slidingSegment.slideToSample( slidingStartSample );
          int newStartMoment = screenToMoment( mousex ) + slidingDeltaMoment;
          newStartMoment = snap( newStartMoment );
          int newStartSample = slidingSegment.momentToSample( newStartMoment );
          slidingSegment.slideToMoment( newStartMoment );
          slidingSegment.slideToSample( newStartSample );
          slidingSegment.slideToMoment( newStartMoment );
          slidingSegment.repaint();
        } else if (movingAll) {
          slideMomentToScreen( movingAllMoment, mousex );
        }
      }
      public void mouseMoved( MouseEvent me ) {
        mousex = me.getX();
        mousey = me.getY();
        if (showSample)
          repaint();
      }
    } );
    addKeyListener( new KeyListener() {
      public void keyPressed( KeyEvent ke ) {
        //try {
        //EventQueue eq = Toolkit.getDefaultToolkit().getSystemEventQueue();
        //AWTEvent ae = eq.peekEvent();
        //System.out.println( ae );

        ke.consume();

        //ae = eq.peekEvent();
        //System.out.println( ae );

        String ks = keys.process( ke );
        if (ks != keyStatus) {
          keyStatus = ks;
          repaint();
        }
        //} catch( InterruptedException ie ) { }
      }
      public void keyReleased( KeyEvent ke ) {
        ke.consume();
      }
      public void keyTyped( KeyEvent ke ) {
        ke.consume();
      }
    } );
    addComponentListener( new ComponentListener() {
      public void componentHidden( ComponentEvent ce ) {
      }
      public void componentMoved( ComponentEvent ce ) {
      }
      public void componentResized( ComponentEvent ce ) {
        relayout();
      }
      public void componentShown( ComponentEvent ce ) {
      }
    } );
  }

  public void setBPM() {
    setBPM( InputString.readDouble( this, bpm ) );
  }

  private void setBPM( double bpm ) {
    setBPM( bpm, true );
  }

  private void setBPM( double bpm, boolean carrySegments ) {
    int nbeats = measures * timeSigNum;
    double minutes = (double)nbeats / (double)bpm;
    double seconds = minutes * 60;
    int looplen = (int)(seconds * Constants.sampRate);
    loopRightMoment = loopLeftMoment + looplen;

    double obpm = this.bpm;
    this.bpm = bpm;
    setupGroove();
    if (carrySegments) {
      double ratio = obpm / bpm;
      chunkRescale( ratio );
    }
    updateSound();
  }

  private void chunkRescale( double ratio ) {
    for (int i=0; i<segments.size(); ++i) {
      Segment segment = (Segment)segments.elementAt( i );
      Mark left = segment.getStartMark();
      if (!left.isLinked()) {
        int nmoment = (int)(left.getMoment()*ratio);
        left.moveChain( nmoment );
      }
    }
  }

  private void setupLoop() {
    loopLeftMoment = 0;
    loopRightMoment =
      (int) ((((double)(measures*timeSigNum))/(bpm/60.0))*Constants.sampRate);
    setupGroove();
  }

  private void setView( int lm, int rm ) {
    leftMoment = lm;
    rightMoment = rm;
    relayout();
  }

  private void setLoop( int lm, int rm ) {
    // synthesize a bpm for this
    bpm = bpmFromLoop( lm, rm );

    prepareForStickiness();

    loopLeftMoment = lm;
    loopRightMoment = rm;

    setupGroove();

    carryOutStickiness();

    updateSound();
    setDirty( true );
    repaint();
  }

  private Hashtable stickyPositions;
  private Hashtable stickyLengths;
  private void prepareForStickiness() {
    if (stickyPositions == null) {
      stickyPositions = new Hashtable();
      stickyLengths = new Hashtable();
    }

    stickyPositions.clear();
    stickyLengths.clear();

    for (Enumeration e = getMarks(); e.hasMoreElements();) {
      Mark m = (Mark)e.nextElement();
      if (m.isSticky()) {
        int mm = m.getMoment();
        double relative = (double)(mm-loopLeftMoment)/(loopRightMoment-loopLeftMoment);
        stickyPositions.put( m, new Double( relative ) );
      }
      Mark om = m.getOtherSide();
      if (!om.isSticky()) {
        int length = om.getMoment() - m.getMoment();
        stickyLengths.put( m, new Integer( length ) );
      }
    }
  }

  private void carryOutStickiness() {
    for (Enumeration e = stickyPositions.keys(); e.hasMoreElements();) {
      Mark m = (Mark)e.nextElement();
      double relative = ((Double)stickyPositions.get( m )).doubleValue();
      int mm = loopLeftMoment + (int)(relative*(loopRightMoment-loopLeftMoment));
      m.setMoment( mm );
      Mark om = m.getOtherSide();
      if (om != null && stickyLengths.containsKey( m )) {
        int length = ((Integer)stickyLengths.get( m )).intValue();
        om.setMoment( m.getMoment()+length );
      }
    }
  }

  private double bpmFromLoop( int lm, int rm ) {
    int dm = rm-lm;
    double sec = (double)dm/Constants.sampRate;
    double min = sec/60.0;
    int nbeats = measures*timeSigNum;
    double bpm = (double)nbeats / min;
    return bpm;
  }

  private void setupGroove() {
    //System.out.println( "lal "+(loopRightMoment - loopLeftMoment) );
    groove = new Groove( loopLeftMoment, bpm, measures, timeSigNum, subdiv );
  }

  private final int snapDist = 20;
  private int snap( int m ) {
    if (!snapping) return m;    int sm = groove.closestMoment( m );
    int dx = momentToScreen( m ) - momentToScreen( sm );
    if (dx<0) dx=-dx;
    return dx<snapDist ? sm : m;
  }

  public void fullPosition() {
    setView( getLoopLeftMoment(), getLoopRightMoment() );
  }

  public Enumeration getMarks() {
    return new SegmentMarkEnumeration( segments );
  }

  int getLoopLeftMoment() { return loopLeftMoment; }
  int getLoopRightMoment() { return loopRightMoment; }
  int getLeftMoment() { return leftMoment; }
  int getRightMoment() { return rightMoment; }
  int getNumRows() { return numRows; }

  int screenToMoment() {
    return screenToMoment( mousex );
  }

  int screenToMoment( int x ) {
    return leftMoment +
      (int)((rightMoment-leftMoment) *((double)x/(double)wid));
  }

  int momentToScreen( int m ) {
    return (int)(wid *
      ((double)(m-leftMoment)/(double)(rightMoment-leftMoment)));
  }

  private void relayout() {
    for (int i=0; i<segments.size(); ++i) {
      Segment segment = (Segment)segments.elementAt( i );
      resetBounds( segment );
    }
    repaint();
  }

  void resetBounds( Segment segment ) {
    updateDimensions();

    int ulx = momentToScreen( segment.getStartMoment() );
    int urx = momentToScreen( segment.getEndMoment() );
    int width = urx-ulx;
    int uly = (segment.getRow()*ht)/getNumRows();
    int height = ht / getNumRows();

    uly += gapSize;
    height -= gapSize*2;

    Rectangle r = new Rectangle( ulx, uly, width, height );
    segment.setBounds( r );
    segment.repaint();
  }

  public Segment newSegment( Sound sound ) {
    return new Segment( this, sound, loopLeftMoment );
  }

  public Segment addSound( int row, Sound sound ) {
    undoSaveChange();
    Segment segment = newSegment( sound );
    addSegment( row, segment );
    return segment;
  }

  public Segment addSound( Sound sound ) {
    return addSound( -1, sound );
  }

  public void addSegment( Segment segment ) {
    addSegment( -1, segment );
  }

  public void addSegment( int row, Segment segment ) {
    setDirty( true );

    if (row==-1)
      row = firstUnoccupiedRow();

    boolean fullRepaint = false;

    if (row >= numRows) {
      numRows = row+1;
      fullRepaint = true;
    }

    segment.setRow( row );

    segments.addElement( segment );
    resetBounds( segment );

    add( segment );

    if (fullRepaint) {
      relayout();
    } else {
      segment.repaint();
    }

    updateSound();
  }

  public void removeSegment( Segment segment ) {
    setDirty( true );

    remove( segment );
    segments.removeElement( segment );

    updateSound();

    repaint();
  }

  public void removeAllSegments() {
    while (segments.size()>0) {
      Segment segment = (Segment)segments.elementAt( 0 );
      removeSegment( segment );
    }
  }

  private void updateDimensions() {
    Dimension d = getSize();
    wid = d.width;
    ht = d.height;
  }

  static public void main( String args[] ) throws Exception {
    JBE jbe = new JBE();

    Frame frame = new Frame( "JBE" );
    frame.setSize( 900, 400 );
    frame.setLayout( new BorderLayout() );
    frame.add( "Center", jbe );
    frame.setVisible( true );
    frame.setLocation( 100, 100 );
    jbe.setFrame( frame );

    try {
      for (int a=0; a<args.length; ++a)
        jbe.doLoad( args[a] );
    } catch( Exception ie ) {
      ie.printStackTrace();
    }

    jbe.requestFocus();
  }

  private int whichRow() {
    return whichRow( mousey );
  }

  private int whichRow( int y ) {
    return (y*numRows)/ht;
  }

  private int centerOfRow( int r ) {
    return rowSize()*r + (rowSize()/2);
  }

  private int rowSize() {
    return ht / numRows;
  }

  private int firstUnoccupiedRow() {
    int uo = 0;
    for (int i=0; i<segments.size(); ++i) {
      Segment segment = (Segment)segments.elementAt( i );
      int row = segment.getRow();
      if (row >= uo)
        uo = row+1;
    }
   return uo;
  }

  static private final int markClickDist = 15;
  private Mark nearestMark() {
    return nearestMark( mousex, mousey );
  }

  private Mark nearestMark( int x, int y ) {
    return nearestMark( x, y, null );
  }

  private Mark[] twoNearestMarks() {
    return twoNearestMarks( mousex, mousey );
  }

  private Mark[] twoNearestMarks( int x, int y ) {
    Mark ms[] = new Mark[2];
    ms[0] = nearestMark( x, y );
    ms[1] = nearestMark( x, y, ms[0] );
    return (ms[0]==null || ms[1]==null) ? null : ms;
  }

  private Mark nearestMark( int x, int y, Mark notThis ) {
    int row = whichRow( y );

    Mark best = null;
    int bestDist = 0;

    for (Enumeration e = getMarks(); e.hasMoreElements();) {

      Mark m = (Mark)e.nextElement();

      if (m.getRow() != row || m==notThis)
        continue;

      int d = m.hdist( x );

      if (d<bestDist || best==null) {
        best = m;
        bestDist = d;
      }
    }
    return bestDist < markClickDist ? best : null;
  }

  private Mark nearestMarkInSegment() {
    return nearestMarkInSegment( mousex, mousey );
  }

  private Mark nearestMarkInSegment( int x, int y ) {
    Segment s = getContainingSegment( x, y );
    if (s==null)
      return null;
    Mark sm = s.getStartMark();
    Mark em = s.getEndMark();
    int sd = sm.hdist( x );
    int ed = em.hdist( x );
    return sd < ed ? sm : em;
  }

  public Segment getContainingSegment() {
    return getContainingSegment( mousex, mousey );
  }

  public Segment getContainingSegment( int x, int y ) {
    for (int i=0; i<segments.size(); ++i) {
      Segment segment = (Segment)segments.elementAt( i );
      if (segment.contains( x, y ))
        return segment;
    }
    return null;
  }

  // If close to a mark, return that.  If close to a
  // segment center, return that.  If both, then return
  // the one that's closer.  If neither, then it's the
  // background, so return the JBE.
  public Object nearestThing() {
    Mark nm = nearestMark();
    Segment cs = getContainingSegment();

    if (nm == null && cs == null) {
      return this;
    } else if (nm==null && cs != null) {
      return cs;
    } else if (nm != null && cs == null) {
      return cs;
    } else {
      // Which one?
      int nms = nm.getScreen();
      int css = cs.getCenterScreen();
      int nmd = Math.abs( nms-mousex );
      int csd = Math.abs( css-mousex );

      return nmd < csd ? (Object)nm : (Object)cs;
    }
  }

/*
  private void key( char c ) {
    switch( c ) {
      case 'A':
        flipAudition();
        break;
      case '7':
        flipAuditionGap();
        break;
      case 'm':
        splitSegment();
        break;
      case 'J':
        joinSegmentBoth();
        break;
      case 'c':
        flipSticky();
        break;
      case 'C':
        flipStickyRow();
        break;
      case 'j':
        joinSegments();
        break;
      case 'n':
        nameSelectedSegment();
        break;
      case 'z':
        zoomIn();
        break;
      case 'x':
        zoomOut();
        break;
      case 'l':
        loadSound();
        break;
      case 'N':
        addRow();
        break;
      case 'V':
        changeMeasures();
        break;
      case 'B':
        setBPM();
        break;
      case 'D':
        deleteSegment();
        break;
      case 'M':
        muteSegment();
        break;
      case '4':
        muteRow();
        break;
      case '5':
        unMuteRow();
        break;
      case 'a':
        aB();
        break;
      case 'F':
        fullPosition();
        break;
      case 'Q':
        doQuit();
        break;
      case 'S':
        makeClean();
        break;
      case 'Z':
        dupInPlace();
        break;
      case 'v':
        dup();
        break;
      case 'p':
        copyOut();
        break;
      case 'y':
        exportSegment();
        break;
      case 'w':
        fileSegment();
        break;
      case 'T':
        exportRow();
        break;
      case 'W':
        exportRows();
        break;
      case 'P':
        flipSnapping();
        break;
      case '9':
        removeEmptyRows();
        break;
      case '(':
        collectCache();
        break;
      case 'K':
        makeClik();
        break;
      case 'g':
        setGain();
        break;
      case '1':
        vzoomOut();
        break;
      case '2':
        vzoomIn();
        break;
      case '3':
        vzoomNorm();
        break;
      case '!':
        vzoomOutRow();
        break;
      case '@':
        vzoomInRow();
        break;
      case '#':
        vzoomNormRow();
        break;
      case 'O':
        showRender();
        break;
      case 'u':
        undo();
        break;
      case 'r':
        redo();
        break;
      case 'R':
        renderRow();
        break;
      case 't':
        report();
        break;
      case 's':
        saveSegment();
        //renderRow(); // error..?
        break;
      case '*':
        breakRowLinks();
        break;
      case 'b':
        breakLink();
        break;
      case 'L':
        makeLink();
        break;
      case '8':
        flipShowSample();
        break;
      case 'i':
        dupAndWriteSegment();
        break;
      case 'I':
        loopToSegment();
        break;
      case '6':
        nextSound();
        break;
      case 'k':
        loopFit();
        break;
      case 'o':
        nextSoundAndSameSize();
        break;
      case '^':
        halveLoop();
        break;
      case '&':
        doubleLoop();
        break;
      case '%':
        replaceRowWithRendering();
        break;
      case ')':
        deleteRow();
        break;
      default:
        System.out.println( "?? "+c+" "+(int)c );
        break;
    }
  }
*/

  public void repaint() {
    if (guibug)
      System.out.println( "jbe repaint" );
    updateDimensions();
    super.repaint();
  }

  public void splitSegment() {
    startUpdateHold();

    Segment s = getContainingSegment();
    if (s == null) return;
    undoSaveChange();
    Segment ss[] = s.split( screenToMoment( mousex ) );
    if (ss==null) return;
    int row = s.getRow();
    removeSegment( s );
    addSegment( row, ss[0] );
    addSegment( row, ss[1] );

    endUpdateHold();
  }

  public void dupAndWriteSegment() {
    Segment s = getContainingSegment();
    if (s == null) return;
    dupAndWriteSegment( s );
  }

  private void dupAndWriteSegment( Segment s ) {
    try {
      Segment ns = s.dup();
      addSegment( ns );
      nameSegment( ns );
      ensureSoundsWritten();
      repaint();
    } catch( IOException ie ) {
      ie.printStackTrace();
    }
  }

  public void joinSegments() {
    startUpdateHold();

    Mark m = nearestMark();
    if (m==null)
      return;
    Segment newSegment = m.join();
    if (newSegment != null) {
      undoSaveChange();
      int row = m.getLeftSegment().getRow();
      removeSegment( m.getLeftSegment() );
      removeSegment( m.getRightSegment() );
      addSegment( row, newSegment );
    }
    endUpdateHold();
  }

  public void joinSegmentBoth() {
    Segment segment = getSingleSelectedSegment();

    if (segment==null)
      return;

    int row = segment.getRow();

    Segment prev = segment.getPrevNeighbor();
    Segment next = segment.getNextNeighbor();

    Segment nu = segment.joinBoth();

    removeSegment( segment );
    removeSegment( prev );
    removeSegment( next );

    addSegment( row, nu );
  }

  private void slideMomentToScreen( int m, int x ) {
    int dm = m-screenToMoment( x );
    setView( getLeftMoment()+dm, getRightMoment()+dm );
  }

  public void zoomIn() {
    zoom( true );
  }

  public void zoomOut() {
    zoom( false );
  }

  private void zoom( boolean inp ) {
    int m = screenToMoment( mousex );
    if (inp) {
      setView( getLeftMoment()+(m-getLeftMoment())/2,
               getRightMoment()-(getRightMoment()-m)/2 );
    } else {
      setView( getLeftMoment() - (m-getLeftMoment()),
               getRightMoment() + (getRightMoment()-m) );
    }
  }

  private boolean updateHold = false;
  private boolean updateHoldUpdate = false;

  private void startUpdateHold() {
    updateHold = true;
    updateHoldUpdate = false;
  }

  private void endUpdateHold() {
    updateHold = false;
    if (updateHoldUpdate)
      updateSound();
  }

  private void updateSound() {
    if (updateHold) {
      updateHoldUpdate = true;
    } else {
      synchronized( updateLock ) {
        needsUpdate = true;

        repaint();
        updateLock.notifyAll();
      }
    }
  }

  private Sound doUpdateSound() {
    long time = System.currentTimeMillis();

    Sound s = null;

    int gap = auditionGap ? auditionBlankLenS : 0;

    if (pause) {
      SoundAccumulator sa =
          new SoundAccumulator( soundRenderer, loopLeftMoment, loopRightMoment + gap );
      s = sa.getSound();
    } else if (audition) {
      Segment seg = getSingleSelectedSegment();
      if (seg != null) {
        SoundAccumulator sa =
          new SoundAccumulator( soundRenderer, seg.getStartMoment(),
            seg.getEndMoment()+gap );
        sa.add( seg );
        s = sa.getSound();
      }
    } else {
      SoundAccumulator sa =
       new SoundAccumulator( soundRenderer, loopLeftMoment, loopRightMoment + gap );
      for (int i=0; i<segments.size(); ++i) {
        Segment segment = (Segment)segments.elementAt( i );
        if (!segment.getMuted())
          sa.add( segment );
      }
      s = sa.getSound();
    }

    soundRenderer.trimCache();

    time = System.currentTimeMillis() - time;
    //System.out.println( "update "+time+"ms" );
    return s;
  }

  public void loadSound() {
    String filename = InputString.readString( this, directoryCompleter );
    if (filename==null || filename.equals( "" )) return;
    doLoadWAV( filename );
  }

  private Sound renderRow( int row ) {
    SoundAccumulator sa =
      new SoundAccumulator( soundRenderer, loopLeftMoment, loopRightMoment );
    for (int i=0; i<segments.size(); ++i) {
      Segment segment = (Segment)segments.elementAt( i );
      if (!segment.getMuted() && segment.getRow()==row)
        sa.add( segment );
    }
    return sa.getSound();
  }

  public void exportRenderedSegment() {
    exportRenderedSegment( getSingleSelectedSegment() );
  }

  private void exportRenderedSegment( Segment segment ) {
    if (segment==null) {
      return;
    }

    String name = InputString.readString( this );
    if (name==null || name.equals( "" ))
      return;

    exportRenderedSegment( segment, name );
  }

  private void exportRenderedSegment( Segment segment, String name ) {
    SoundAccumulator sa = new SoundAccumulator( soundRenderer,
      segment.getStartMoment(), segment.getEndMoment() );
    sa.add( segment );

    try {
      Sound rendered = sa.getSound();
      rendered.setSource( name );
      SoundCache.ensureWritten( rendered );
      System.out.println( "Wrote "+name );
    } catch( IOException ie ) {
      ie.printStackTrace();
    }
  }

  public void exportRenderedSegments() {
    String stub = InputString.readString( this );
    if (stub==null || stub.equals( "" ))
      return;

    for (int i=0; i<segments.size(); ++i) {
      Segment segment = (Segment)segments.elementAt( i );
      if (!segment.getMuted()) {
        exportRenderedSegment( segment, stub+i+".wav" );
      }
    }
  }

  public void renderRow() {
    Sound s = renderRow( whichRow() );
    Segment segment = new Segment( this, s, loopLeftMoment );
    segment.setMuted( true );
    addSegment( segment );
  }

  public void showRender() {
    Sound s = doUpdateSound();
    Segment segment = new Segment( this, s, loopLeftMoment );
    segment.setMuted( true );
    addSegment( segment );
  }

  public void exportRow() {
    exportRow( whichRow() );
  }

  private void exportRow( int row ) {
    String name = InputString.readString( this );

    if (name==null || name.equals( "" ))
      return;

    try {
      Sound s = renderRow( row );
      s.setSource( name );
      SoundCache.ensureWritten( s );
      System.out.println( "Wrote "+name );
    } catch( IOException ie ) {
      System.err.println( "Failed to write "+name );
      ie.printStackTrace();
    }
  }

  public void exportRows() {
    String prefix = InputString.readString( this );

    if (prefix==null || prefix.equals( "" ))
      return;

    for (int i=0; i<numRows; ++i) {
      String name = null;
      try {
        Sound s = renderRow( i );
        name = prefix+i+".wav";
        s.setSource( name );
        SoundCache.ensureWritten( s );
        System.out.println( "Wrote "+name );
      } catch( IOException ie ) {
        System.err.println( "Failed to write "+name );
        ie.printStackTrace();
      }
    }
  }

  public void run() {
    while (true) {
      synchronized( updateLock ) {
        if (needsUpdate) {
          currentRendering = doUpdateSound();
          looper.setSound( currentRendering );
        }
        try {
          updateLock.wait();
        } catch( InterruptedException ie ) {}
      }
    }
  }

  private void drawGroove( Graphics g ) {
    int moments[] = groove.moments();
    for (int i=0; i<moments.length; ++i) {
        boolean wide = (i%4) == 0;
      int ya = moments.length/measures;
      int l=0;
      while (l<grooveColors.length) {
        if (((i/ya)*ya)==i) {
          break;
        }
        l++;
        ya >>= 1;
      }
      if (l>grooveColors.length) l = grooveColors.length-1;
      g.setColor( grooveColors[l] );
      int x = momentToScreen( moments[i] );
      g.drawLine( x, 0, x, ht-1 );
      if (wide) {
      g.drawLine( x-1, 0, x-1, ht-1 );
      g.drawLine( x+1, 0, x+1, ht-1 );
      }
    }
  }

  public void paintComponent( Graphics g ) {
//System.out.println( "paintC "+Thread.currentThread() );
    if (guibug)
      System.out.println( "jbe pc" );
    super.paintComponent( g );
    drawGroove( g );
  }

  private void drawString( Graphics g, String s, int x, int y ) {
    Color color = g.getColor();
    g.setColor( Color.black );
    g.drawString( s, x+2, y+2 );
    g.setColor( color );
    g.drawString( s, x, y );
  }

  public void paint( Graphics g ) {
    super.paint( g );

    g.setFont( bigFont );
    g.setColor( textColor );
    if (needsUpdate)
      drawString( g, "*", wid-bigFM.stringWidth( "*" ), realTop );

    boolean selectedDirty = (getSingleSelectedSegment()==null?false:
      !getSingleSelectedSegment().getSound().getWritten());
    drawString( g, "-"+(dirty?"*":"-")+(selectedDirty?"*":"-")+"-", 5, ht-5 );

    Segment sel = getSingleSelectedSegment();
    if (sel != null) {
      String name = sel.getSound().getSource();
      if (name.equals( "" ))
        name = "????????.???";
      g.setFont( smallFont );
      g.setColor( filenameColor );

      String gain = "x"+sel.getGain();

      String info = name+" "+gain;

      drawString( g, info, 133, ht-13 );
    }

    if (showSample) {
      int s = screenToMoment();
      g.setFont( smallFont );
      drawString( g, (""+s), wid-100, ht-13 );
    }

    if (keyStatus != null) {
      g.setFont( tinyFont );
      drawString( g, keyStatus, 5, realTop-15 );
    }
  }

  public void setDirty( boolean dirty ) {
    this.dirty = dirty;
    repaint();
  }

  public void makeClean() {
    if (doSave()) {
      dirty = false;
      repaint();
    }
  }

  private boolean doSave() {
    if (docName==null) {
      docName = InputString.readString( this );
      if (new File( docName ).exists()) {
        if (!InputString.yorn( this, "Overwrite "+docName+" (y/n)?_" ))
          return false;
      }
    }
    System.out.println( "Wrote to "+docName );
    if (docName==null) return false;
    try {
      write( docName );
      return true;
    } catch( IOException ie ) {
      System.out.println( "Can't save "+docName+": "+ie );
      docName = null;
    }
    return false;
  }

  private void write( String filename ) throws IOException {
    ensureSoundsWritten();
    DB db = writeToDB( false );
    db.write( filename );
  }

  // write out endmark,startmark pairs, terminate with -1
  private DB writeMarksToDB() {
    DB marks = new DB();

    int i=0;
    for (Enumeration e=getMarks(); e.hasMoreElements();) {
      Mark m = (Mark)e.nextElement();
      m.saveId = i++;
    }

    int index=0;
    for (Enumeration e=segments.elements(); e.hasMoreElements();) {
      Segment s = (Segment)e.nextElement();
      Mark m = s.getEndMark();
      Mark lm = m.getLink();
      if (lm != null) {
        DB pair = new DB();
        pair.put( 0, m.saveId );
        pair.put( 1, lm.saveId );
        marks.put( index++, pair );
      }
    }

    return marks;
  }

  private void ensureSoundsWritten() throws IOException {
    for (int i=0; i<segments.size(); ++i) {
      Segment segment = (Segment)segments.elementAt( i );
      Sound sound = segment.getSound();
      SoundCache.ensureWritten( sound );
//      sound.ensureWritten();
    }
  }

  private DB writeToDB( boolean memOnly ) {
    DB db = new DB();
    db.put( "bpm", bpm );
    db.put( "measures", measures );
    db.put( "timeSigNum", timeSigNum );

    int selectedSegIndex = -1;

    DB segs = new DB();
    for (int i=0; i<segments.size(); ++i) {
      Segment segment = (Segment)segments.elementAt( i );
      if (segment.getSelected())
        selectedSegIndex = i;
      DB seg = segment.writeToDB();
      segs.put( i, seg );
    }

    db.put( "segments", segs );

    DB loop = db.newDB( "loop" );
    loop.put( "left", getLoopLeftMoment() );
    loop.put( "right", getLoopRightMoment() );

    DB view = db.newDB( "view" );
    view.put( "left", getLeftMoment() );
    view.put( "right", getRightMoment() );

    if (memOnly) {
      if (selectedSegIndex != -1) {
        db.put( "selectedsegment", selectedSegIndex );
      }
    }

    if (!memOnly) {
      DB window = new DB();
      Dimension size = frame.getSize();
      Point location = frame.getLocation();
      window.put( "width", size.width );
      window.put( "height", size.height );
      window.put( "x", location.x );
      window.put( "y", location.y );
      db.put( "windowlocation", window );
    }

    DB marks = writeMarksToDB();

    db.put( "marks", marks );

    return db;
  }

  private void doLoad( String filename ) {
    if (filename.toLowerCase().endsWith( ".jbe" ))
      doLoadJBE( filename );
    else if (filename.toLowerCase().endsWith( ".wav" ))
      doLoadWAV( filename );
    else
      System.out.println( "What is "+filename+"??" );
  }

  private void doLoadWAV( String filename ) {
    try {
      suspendUndoSaving();
      addSound( SoundCache.load( filename ) );
      resumeUndoSaving();
    } catch( IOException ie ) {
      System.out.println( "Can't open "+filename+": "+ie );
      ie.printStackTrace();
    }
  }

  private void doLoadJBE( String filename ) {
    try {
      read( filename );
      docName = filename;
      setDirty( false );
    } catch( IOException ie ) {
      System.out.println( "Can't load "+docName+": "+ie );
    }
  }

  private void read( String filename ) throws IOException {
    DB db = new DB( filename );
    readFromDB( db );
  }

  private void readMarksFromDB( DB db ) {
    Mark ms[] = new Mark[segments.size()*2];
    int i=0;
    for (Enumeration e=getMarks(); e.hasMoreElements();) {
      ms[i++] = (Mark)e.nextElement();
    }

    for (int ii=0; ii<db.size(); ++ii) {
      DB pair = db.getDB( ii );
      int a = pair.getInt( 0 );
      int b = pair.getInt( 1 );
      ms[a].link( ms[b] );
    }
  }

  private void readFromDB( DB db ) throws IOException {
    setBPM( db.getDouble( "bpm" ) );
    measures = db.getInt( "measures" );
    timeSigNum = db.getInt( "timeSigNum" );

    DB segs = db.getDB( "segments" );

    startUpdateHold();
    suspendUndoSaving();

    int selectedSegIndex = -1;
    if (db.existsInt( "selectedsegment" )) {
      selectedSegIndex = db.getInt( "selectedsegment" );
    }

    if (db.existsDB( "windowlocation" )) {
      DB window = db.getDB( "windowlocation" );
      frame.setSize( window.getInt( "width" ), window.getInt( "height" ) );
      frame.setLocation( window.getInt( "x" ), window.getInt( "y" ) );
      frame.validate();
    }

    if (db.existsDB( "loop" )) {
      DB loop = db.getDB( "loop" );
      int lm = loop.getInt( "left" );
      int rm = loop.getInt( "right" );
      setLoop( lm, rm );
    }

    if (db.existsDB( "view" )) {
      DB view = db.getDB( "view" );
      int lm = view.getInt( "left" );
      int rm = view.getInt( "right" );
      setView( lm, rm );
    }

    for (int i=0; i<segs.size(); ++i) {
      Segment segment = Segment.readFromDB( this, segs.getDB( i ) );
      if (selectedSegIndex==i)
        segment.setSelected( true );
      addSegment( segment.getRow(), segment );
    }

    resumeUndoSaving();
    endUpdateHold();

    DB marks = db.getDB( "marks" );

    readMarksFromDB( marks );
  }

  public void doQuit() {
    if (dirty) {
      int ret = InputString.ynorc( this, "Save (y/n)?_" );

      if (ret==InputString.CANCEL)
        return;

      if (ret==InputString.YES) {
        makeClean();
        if (dirty) {
          return;
        }
      }
    }

    System.exit( 0 );
  }

  private void selectSegment( Segment segment ) {
    if (segment!=null) {
      if (!segment.getSelected()) {
        unselectAll();
        segment.setSelected( true );
        segment.repaint();
        repaint();
        if (audition)
          updateSound();
      }
    }
  }

  private void unselectAll() {
    for (int i=0; i<segments.size(); ++i) {
      Segment segment = (Segment)segments.elementAt( i );
      if (segment.getSelected()) {
        segment.setSelected( false );
        segment.repaint();
        repaint();
      }
    }
  }

  // return the selected segment, but only if theres only one
  private Segment getSingleSelectedSegment() {
    Segment s = null;
    for (int i=0; i<segments.size(); ++i) {
      Segment segment = (Segment)segments.elementAt( i );
      if (segment.getSelected()) {
        if (s != null)
          return null;
        s = segment;
      }
    }
    return s;
  }

  public void nameSelectedSegment() {
    Segment s = getSingleSelectedSegment();
    if (s==null) return;
    nameSegment( s );
  }

  private void nameSegment( Segment s ) {
    String name = s.getSound().getSource();
    name = InputString.readString( this, name );
    if (name != null && !name.equals( "" )) {
      s.getSound().setSource( name );
      repaint();
    }
  }

  public void exportSegment() {
    exportSegment( getSingleSelectedSegment() );
  }

  private void exportSegment( Segment segment ) {
    try {
      if (segment==null)
        return;

      Sound export = segment.getSubSound();
      String name = InputString.readString( this );

      if (name == null || name.equals( "" )) {
        return;
      }

      export.setSource( name );
      SoundCache.ensureWritten( export );
      System.out.println( "Wrote "+name );

    } catch( IOException ie ) {
      System.out.println( ie );
    }
  }

  public void fileSegment() {
    fileSegment( getSingleSelectedSegment() );
  }

  private void fileSegment( Segment segment ) {
    try {
      if (segment==null)
        return;

      Sound export = segment.getSubSound();
      String category = InputString.readString( this );

      String name = FileUtil.getNumberedFile( category );

      if (name == null || name.equals( "" )) {
        return;
      }

      export.setSource( name );
      SoundCache.ensureWritten( export );
      System.out.println( "Wrote "+name );

    } catch( IOException ie ) {
      System.out.println( ie );
    }
  }  

  public void loopToSegment() {
    Segment segment = getSingleSelectedSegment();

    if (segment==null)
      return;

    undoSaveChange();
    setLoop( segment.getStartMoment(), segment.getEndMoment() );
  }

  public void leftLoopToMark() {
    Mark mark = nearestMark();
    if (mark==null) {
      return;
    }
    undoSaveChange();
    setLoop( mark.getMoment(), getLoopRightMoment() );
  }

  public void rightLoopToMark() {
    Mark mark = nearestMark();
    if (mark==null) {
      return;
    }
    undoSaveChange();
    setLoop( getLoopLeftMoment(), mark.getMoment() );
  }

  public void looperListenerAlert( Sound sound ) {
    if (sound==currentRendering) {
      needsUpdate = false;
      repaint();
    }
  }

  public void collectCache() {
    SoundCache.markAllUnused();

    for (int i=0; i<segments.size(); ++i) {
      Segment segment = (Segment)segments.elementAt( i );
      SoundCache.markUsed( segment.getSound() );
    }

    SoundCache.removeUnused();
  }

  public void deleteRow() {
    deleteRow( whichRow() );
  }

  private void deleteRow( int row ) {
    undoSaveChange();
    Vector removeEm = new Vector();
    for (Enumeration e=segments.elements(); e.hasMoreElements();) {
      Segment s = (Segment)e.nextElement();
      if (s.getRow()==row) {
        removeEm.addElement( s );
      }
    }
    for (Enumeration e=removeEm.elements(); e.hasMoreElements();) {
      Segment s = (Segment)e.nextElement();
      removeSegment( s );
    }
  }

  public void deleteSegment() {
    deleteSegment( mousex, mousey );
  }

  private void deleteSegment( int x, int y ) {
    Segment s = getContainingSegment();
    if (s != null) {
      undoSaveChange();
      removeSegment( s );
      collectCache();
    }
  }

  public void setGain() {
    setGain( getContainingSegment() );
  }

  private void setGain( Segment segment ) {
    if (segment==null) return;
    undoSaveChange();
    double ng = InputString.readDouble( this );
    if (ng != Double.NaN)
      segment.setGain( ng );
    updateSound();
    repaint();
    setDirty( true );
  }

  private Segment lastMuted[] = new Segment[2];
  public void muteSegment() {
    Segment s = getContainingSegment();
    if (s != null) {
      undoSaveChange();
      s.setMuted( !s.getMuted() );
      s.repaint();
      updateSound();

      lastMuted[1] = lastMuted[0];
      lastMuted[0] = s;
    }
  }

  public void muteRow() {
    muteRow( true );
  }

  public void unMuteRow() {
    muteRow( false );
  }

  private void muteRow( boolean mute ) {
    muteRow( whichRow(), mute );
  }

  private void muteRow( int row, boolean mute ) {
    undoSaveChange();
    for (Enumeration e=segments.elements(); e.hasMoreElements();) {
      Segment s = (Segment)e.nextElement();
      if (s.getRow()==row) {
        s.setMuted( mute );
        s.repaint();
      }
    }
    updateSound();
  }

  public void aB() {
    if (lastMuted[1]==null)
      return;
    lastMuted[1].setMuted( lastMuted[0].getMuted() );
    lastMuted[0].setMuted( !lastMuted[0].getMuted() );
    lastMuted[0].repaint();
    lastMuted[1].repaint();
    updateSound();
  }

  public void makeClik() {
    Sound clik = Clik.generate( bpm, measures, timeSigNum, 1 );
    addSound( clik );
  }

  public void vzoomIn() {
    vzoom( 2.0, true );
  }

  public void vzoomOut() {
    vzoom( 0.5, true );
  }

  public void vzoomNorm() {
    vzoom( 0.0, true );
  }

  public void vzoomInRow() {
    vzoom( 2.0, false );
  }

  public void vzoomOutRow() {
    vzoom( 0.5, false );
  }

  public void vzoomNormRow() {
    vzoom( 0.0, false );
  }

  private void vzoom( double factor, boolean wholeRow ) {
    undoSaveChange();

    if (wholeRow) {
      int row = whichRow( mousey );
      for (int i=0; i<segments.size(); ++i) {
        Segment segment = (Segment)segments.elementAt( i );
        if (segment.getRow()==row) {
          vzoom( segment, factor );
        }
      }
    } else {
      Segment s = getContainingSegment();
      if (s != null) {
        vzoom( s, factor );
      }
    }
  }

  // use factor=0 for reset
  private void vzoom( Segment s, double factor ) {
    double z = s.getDrawGain();
    z = (factor==0.0) ? 1.0 : z*factor;
    s.setDrawGain( z );
    s.repaint();
  }

  private byte[] freeze() {
    try {
      ensureSoundsWritten();
      DB db = writeToDB( true );
      return db.toByteArray();
    } catch( IOException ie ) {
      System.out.println( "Can't freeze!" );
    }
    return null;
  }

  private void unfreeze( byte frozen[] ) {
    removeAllSegments();
    try {
      DB db = DB.fromByteArray( frozen );
      readFromDB( db );
    } catch( IOException ie ) {
      System.out.println( "Can't unfreeze!" );
ie.printStackTrace();
    }
  }

  public void undo() {
    if (!undoStack.inHistory()) {
      undoSaveChange();
      undoStack.undo();
    }

    byte undo[] = undoStack.undo();
    if (undo!=null) {
      unfreeze( undo );
    } else {
      System.out.println( "Can't undo" );
    }
  }

  public void redo() {
    byte redo[] = undoStack.redo();
    if (redo!=null) {
      unfreeze( redo );
    } else {
      System.out.println( "Can't redo" );
    }
  }

  private boolean undoSavingSuspended = false;
  private void undoSaveChange() {
    if (undoSavingSuspended)
      return;
    undoStack.put( freeze() );
  }

  private void suspendUndoSaving() {
    undoSavingSuspended = true;
  }

  private void resumeUndoSaving() {
    undoSavingSuspended = false;
  }

  public void saveSegment() {
    try {
      Segment segment = getContainingSegment();
      Sound sound = segment.getSound();
      SoundCache.ensureWritten( sound );
      repaint();
    } catch( IOException ie ) {
      System.out.println( "Can't write segment!" );
    }
  }

  private void message( String message ) {
    System.out.println( message );
  }

  public void breakLink() {
    Mark m = nearestMark();
    if (m==null)
      return;
    undoSaveChange();
    m.unlink();
    repaint();
  }

  public void breakRowLinks() {
    breakRowLinks( whichRow() );
  }

  private void breakRowLinks( int row ) {
    for (Enumeration e=segments.elements(); e.hasMoreElements();) {
      Segment s = (Segment)e.nextElement();
      if (s.getRow()==row) {
        s.getStartMark().unlink();
        s.getEndMark().unlink();
      }
    }
    updateSound();
  }

  public void makeLink() {
    Mark ms[] = twoNearestMarks();
    if (ms==null)
      return;
    if (ms[0].isLinked() || ms[1].isLinked()) {
      message( "Already linked" );
      return;
    }

    undoSaveChange();
    ms[0].link( ms[1] );
    repaint();
  }

  public void removeEmptyRows() {
    int rows[] = new int[numRows];

    for (Enumeration e=segments.elements(); e.hasMoreElements();) {
      Segment seg = (Segment)e.nextElement();
      int row = seg.getRow();
      rows[row] = 1;
    }

    int r=0;
    for (int i=0; i<rows.length; ++i) {
      System.out.print( i+" "+rows[i] );
      if (rows[i]==0) {
        rows[i] = -1;
      } else {
        rows[i] = r++;
      }
      System.out.println( " "+rows[i] );
    }
    int newNumRows = r;

    if (newNumRows==0)
      newNumRows = 1;

    for (Enumeration e=segments.elements(); e.hasMoreElements();) {
      Segment seg = (Segment)e.nextElement();
      int row = seg.getRow();
      System.out.print( row+" -> " );
      row = rows[row];
      System.out.println( row );
      seg.setRow( row );
    }

    numRows = newNumRows;
    relayout();
  }

  public void report() {
    System.out.println( "measures "+measures );
    System.out.println( "bpm "+bpm );
    System.out.println( "timeSigNum "+timeSigNum );
    System.out.println( "subdiv "+subdiv );
    System.out.println( "leftMoment "+leftMoment );
    System.out.println( "rightMoment "+rightMoment );
    System.out.println( "width "+(rightMoment-leftMoment) );
    System.out.println( "loopLeftMoment "+loopLeftMoment );
    System.out.println( "loopRightMoment "+loopRightMoment );
    System.out.println( "loop width "+(loopRightMoment-loopLeftMoment) );
  }

  private void replaceSegment( Segment oldSeg, Segment newSeg ) {
    replaceSegment( oldSeg, newSeg, false );
  }

  private void replaceSegment( Segment oldSeg, Segment newSeg, boolean sameSize ) {
    if (oldSeg == null) {
      addSegment( newSeg );
    } else {
      int row = oldSeg.getRow();
      boolean sel = oldSeg.getSelected();
      int sm = oldSeg.getStartMoment();
      int em = oldSeg.getEndMoment();
      removeSegment( oldSeg );
      addSegment( row, newSeg );
      newSeg.slideToMoment( sm );
      if (sameSize)
        newSeg.setEndMoment( em );
    }
  }

  static private File lastFile;
  public Segment nextSound() {
    Segment seg = getContainingSegment();
    return nextSound( seg, false );
  }

  public void nextSoundAndSameSize() {
    Segment seg = getContainingSegment();
    nextSound( seg, true );
  }

  private Segment nextSound( Segment seg, boolean sameSize ) {
    File current = null;

    if (seg != null) {
      current = new File( seg.getSound().getSource() );
    } else {
      current = lastFile;
    }

    try {
      File next = FileUtil.nextFile( current );
      if (next != null) {
        Sound newsound = SoundCache.load( next );
        undoSaveChange();
        Segment newSeg = newSegment( newsound );
        replaceSegment( seg, newSeg, sameSize );
        return newSeg;
      }
    } catch( IOException ie ) {
      ie.printStackTrace();
    }

    return seg;
  }

  public void loopFit() {
    Segment seg = getContainingSegment();
    loopFit( seg );
  }

  private void loopFit( Segment seg ) {
    undoSaveChange();
    seg.setStartMoment( loopLeftMoment );
    seg.setEndMoment( loopRightMoment );
    updateSound();
  }

  public void flipStickyRow() {
    flipStickyRow( whichRow() );
  }

  public void flipStickyRow( int row ) {
    undoSaveChange();
    for (Enumeration e=segments.elements(); e.hasMoreElements();) {
      Segment s = (Segment)e.nextElement();
      if (s.getRow()==row) {
        Mark sm = s.getStartMark();
        sm.setSticky( !sm.isSticky() );
        sm.setDirty();
      }
    }
  }

  public void flipSticky() {
    Mark m = nearestMarkInSegment();
    if (m==null)
      return;
    undoSaveChange();
    m.setBothSticky( !m.isSticky() );
    m.setDirty();
  }

  public void doubleLoop() {
    scaleLoop( 2 );
  }

  public void halveLoop() {
    scaleLoop( 0.5 );
  }

  public void forwardLoop() {
    forwardLoop(1.0);
  }

  public void backwardLoop() {
    forwardLoop(-1.0);
  }

  public double interp(double x0, double x1, double t) {
    return x0 + t * (x1 - x0);
  }

  public void forwardLoop(double t) {
    undoSaveChange();
    int lm = loopLeftMoment;
    int rm = loopRightMoment;
    int nlm = rm;
    int nrm = rm + (rm - lm);
    setLoop((int) interp(lm, nlm, t), (int) interp(rm, nrm, t));
  }

  private void scaleLoop( double scale ) {
    undoSaveChange();
    int lm = loopLeftMoment;
    int rm = loopRightMoment;
    setLoop( lm, (int)(lm + scale*(rm-lm)) );
  }

  public void replaceRowWithRendering() {
    replaceRowWithRendering( whichRow() );
  }

  public void replaceRowWithRendering( int row ) {
    Sound sound = renderRow( row );
    undoSaveChange();
    deleteRow( row );
    addSound( row, sound );
  }

  public void flipAudition() {
    audition = !audition;
    updateSound();
  }


  public void flipAuditionGap() {
    auditionGap = !auditionGap;
    updateSound();
  }

  public void flipPause() {
    pause = !pause;
    updateSound();
  }

  public void addRow() {
    numRows++;
    relayout();
  }

  public void changeMeasures() {
    measures = InputString.readInt( this, measures );
    fullPosition();
  }

  public void dup() {
    Segment s = getContainingSegment();
    if (s != null) {
      undoSaveChange();
      Segment ns = s.dup();
      ns.setMuted( s.getMuted() );
      addSegment( ns );
    }
  }

  public void dupInPlace() {
    Segment s = getContainingSegment();
    if (s != null) {
      undoSaveChange();
      Segment ns = s.dup();
      ns.setMuted( s.getMuted() );
      addSegment( s.getRow(), ns );
    }
  }

  public void copyOut() {
    Segment s = getContainingSegment();
    if (s != null) {
      undoSaveChange();
      Segment ns = s.copyOut();
      addSegment( ns );
    }
  }

  public void flipSnapping() {
    snapping = !snapping;
  }

  public void flipShowSample() {
    showSample = !showSample;
    repaint();
  }

  private final int browseSize = 2500000;
  private File browseFile;
  private double browseStep = 0;

  public void browseNext() {
    browse( 1 );
  }

  public void browsePrev() {
    browse( -1 );
  }

  public void browse( int inc ) {
    browse( browseFile, browseStep+inc );
  }

  public void browseToStep() {
    double nextStep = InputString.readDouble( this, browseStep, -1 );
    browse( browseFile, nextStep );
  }

  public void browse() {
    String nextFileName = InputString.readString( this, directoryCompleter,
      browseFile == null ? "" : browseFile.getPath() );
    if (nextFileName != null) {
      double nextStep = InputString.readDouble( this, 0, -1 );
      if (nextStep != -1)
        browse( new File( nextFileName ), nextStep );
    }
  }

  private void browse( File nextFile, double nextStep ) {
    Segment seg = getContainingSegment();

    try {
      long start = (long)(nextStep * browseSize);
      long end = (long)((nextStep+1) * browseSize);
      long len = Sound.getFrameLength( nextFile );

      if (len < end) {
        end = len;
      }

      if (end <= start) {
        System.out.println( "File too short." );
        return;
      }

      undoSaveChange();

      int row=-1;
      boolean sel=false;

      if (seg != null) {
        row = seg.getRow();
        sel = seg.getSelected();
        removeSegment( seg );
        collectCache();
      }

      FastExtract.extract( "browsetemp.wav", nextFile, start, end );
      Sound sound = SoundCache.load( "browsetemp.wav" );

      Segment nextSeg = newSegment( sound );

      addSegment( row, nextSeg );
      nextSeg.setSelected( sel );

      browseFile = nextFile;
      browseStep = nextStep;
    } catch( IOException ie ) {
      ie.printStackTrace();
    }
  }

  public Segment applyPlugIn( PlugIn pi, Segment seg ) {
    Sound sound = seg.getIncludedSound();
    Sound nsound = pi.process( sound );
    Segment nseg = new Segment( this, nsound, loopLeftMoment );
    return nseg;
  }

  public void applyPlugIn() {
    String pluginName = InputString.readString( this );

    if (pluginName == null)
      return;

    PlugIn pi = PlugInUtils.getPlugIn( pluginName );

    Segment seg = getContainingSegment();
    if (seg==null)
      return;

    undoSaveChange();
    Segment newseg = applyPlugIn( pi, seg );
    replaceSegment( seg, newseg, true );
  }

  public void pageForward() {
      page(1);
  }

  public void pageBackward() {
      page(-1);
  }

  public void page(int way) {
    Segment s = getContainingSegment();
    if (s != null) {
      undoSaveChange();
      int loopWidth = loopRightMoment - loopLeftMoment;
      s.slideToMoment( s.getStartMark().getMoment() - (way * loopWidth) );
      updateSound();
    }
  }

  public void moveSegmentToSecond() {
    Segment s = getSingleSelectedSegment();
    if (s != null) {
      undoSaveChange();
      double seconds = InputString.readDouble( this );
      s.slideToMoment((int)(-seconds * Constants.sampRate));
      updateSound();
    } else {
      System.err.println("No segment selected.");
    }
  }

    public void dumpInfo() {
        System.err.println("BPM " + bpm);
        System.err.println("measures " + measures);
        System.err.println("timeSigNum " + timeSigNum);
        System.err.println("loop " + loopLeftMoment + " " + loopRightMoment);
    }
}

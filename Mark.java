// $Id: Mark.java,v 1.1 2004/10/26 19:20:23 greg Exp $

import java.awt.Rectangle;
import java.util.Enumeration;

public class Mark
{
  private Segment segment;
  private boolean leftp;
  private Mark link;
  private boolean sticky;

  // for saving links
  public int saveId;

  public Mark( Segment segment, boolean leftp ) {
    this.segment = segment;
    this.leftp = leftp;
  }

  public int getMoment() {
    return leftp ? segment.getStartMoment() : segment.getEndMoment();
  }

  public int getSample() {
    return leftp ? segment.getStartSample() : segment.getEndSample();
  }

  public int getScreen() {
    return getSegment().getJBE().momentToScreen( getMoment() );
  }

  public Mark getOtherSide() {
    return leftp ? segment.getEndMark() : segment.getStartMark();
  }

  private Segment getSegment() { return segment; }

  public Segment getLeftSegment() {
    if (!leftp) {
      return segment;
    } else {
      if (link != null)
        return link.getSegment();
      else
        return null;
    }
  }

  public Segment getRightSegment() {
    if (leftp) {
      return segment;
    } else {
      if (link != null)
        return link.getSegment();
      else
        return null;
    }
  }

  public boolean isSticky() {
    return sticky;
  }

  public void setSticky( boolean sticky ) {
    this.sticky = sticky;
  }

  public int getRow() {
    return segment.getRow();
  }

  public void setMoment( int m ) {
    setMoment( m, true );
  }

  private void setMoment( int m, boolean doLink ) {
    if (leftp) {
      segment.setStartMoment( m );
    } else {
      segment.setEndMoment( m );
    }

    if (link != null && doLink)
      link.setMoment( m, false );
  }

  public int sidedGetSample( int moment, boolean lp ) {
    Segment seg = (lp!=leftp  || link == null) ? segment : link.getSegment();
    return seg.momentToSample( moment );
  }

  public void setSample( int sample ) {
    setSample( sample, true );
  }

  public void setSample( int sample, boolean doLink ) {
    if (leftp) {
      segment.setStartSample( sample );
    } else {
      segment.setEndSample( sample );
    }

    if (link != null && doLink)
      link.setSample( sample, false );
  }

  public void updateOther() {
    if (link==null)
      return;
    link.setMoment( getMoment() );
    link.setSample( getSample() );
  }

  public int hdist( int x ) {
    Rectangle r = getSegment().getBounds();
    int xx = leftp ? r.x : r.x+r.width;
    int dx = x-xx;
    dx = dx<0 ? -dx : dx;
    return dx;
  }

  public void link( Mark mark ) {
    if (mark==null) return;
    link = mark;
    mark.link = this;
  }

  public void unlink() {
    if (link==null) return;
    link.link = null;
    link = null;
  }

  public Mark getLink() { return link; }

  public boolean isLinked() { return link!=null; }

  public Segment join() {
    if (link==null)
      return null;
    System.out.println( this+" "+link );
    if (leftp)
      return join( link, this );
    else
      return join( this, link );
  }

  /* this could be moved to Segment
     this doesn't respect stretchFactor */
  static public Segment join( Mark right, Mark left ) {
    Segment lseg = right.getSegment();
    Segment rseg = left.getSegment();

    if (lseg.getEndMoment() != rseg.getStartMoment() ||
        lseg.getEndSample() != rseg.getStartSample()) {
      System.out.println( "Can't join mark: do not match" );
      return null;
    } else {
      int sm = lseg.getStartMoment();
      int em = rseg.getEndMoment();
      int ss = lseg.getStartSample();
      int es = rseg.getEndSample();
      Segment ns = new Segment( lseg.getJBE(), lseg.getSound(),
        sm, em, ss, es );
      ns.getStartMark().link( lseg.getStartMark().getLink() );
      ns.getEndMark().link( rseg.getEndMark().getLink() );
      ns.setColorInx( lseg.getColorInx() );
      ns.setGain( (lseg.getGain()+rseg.getGain())/2 );
      ns.setDrawGain( (lseg.getDrawGain()+rseg.getDrawGain())/2 );
      return ns;
    }
  }

  public String toString() {
    return "["+(leftp?"left":"right")+" of "+segment+"]";
  }

  public void moveChain( int nmoment ) {
    moveChainDelta( nmoment - getMoment() );
  }

  public void moveChainDelta( int dmoment ) {
    for (Enumeration e = getChain(); e.hasMoreElements();) {
      Mark mark = (Mark)e.nextElement();
      mark.setMoment( mark.getMoment()+dmoment );
    }
  }

  public Mark getNextLeft() {
    if (!leftp) {
      throw new IllegalStateException( "Can't getNextLeft() of right mark" );
    }
    return getSegment().getEndMark().getLink();
  }

  public Enumeration getChain() {
    if (!leftp) {
      throw new IllegalStateException( "Can't getChain() of right mark" );
    }

    final Mark thees = this;
    return new Enumeration() {
      private Mark startMark = thees;
      private Mark currentMark = thees;

      public boolean hasMoreElements() {
        return currentMark != null;
      }

      public Object nextElement() {
        if (currentMark==null)
          return null;
        Mark next = currentMark;
        if (currentMark.leftp) {
          currentMark = currentMark.getNextLeft();
          if (next!=null && currentMark==null) {
            currentMark = next.getSegment().getEndMark();
          }
        } else {
          currentMark = null;
        }

        return next;
      }
    };
  }

  public void dumpChain() {
    System.out.println( "Chain:" );
    for (Enumeration e = getChain(); e.hasMoreElements();) {
      Mark mark = (Mark)e.nextElement();
      System.out.println( "  "+mark.getSegment() );
    }
  }

  void setDirty() {
    segment.setDirty( true );
    Mark link = getLink();
    if (link != null)
      link.getSegment().setDirty( true );
  }

  void setBothSticky( boolean sticky ) {
    setSticky( sticky );
    if (link != null)
      link.setSticky( sticky );
  }
}

// $Id: SegmentMarkEnumeration.java,v 1.1 2004/10/26 19:20:23 greg Exp $

import java.util.*;

public class SegmentMarkEnumeration implements Enumeration
{
  private Enumeration segments;
  private Segment current;

  public SegmentMarkEnumeration( Vector segments ) {
    this.segments = segments.elements();
  }

  public boolean hasMoreElements() {
    return current != null || segments.hasMoreElements();
  }

  public Object nextElement() {
    if (current != null) {
      Mark m = current.getEndMark();
      current = null;
      return m;
    } else {
      current = (Segment)segments.nextElement();
      return current == null ? null : current.getStartMark();
    }
  }
}

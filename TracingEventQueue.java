// $Id: TracingEventQueue.java,v 1.1 2004/10/26 19:20:23 greg Exp $

import java.awt.*;

public class TracingEventQueue extends EventQueue
{
  public void dispatchEvent( AWTEvent e ) {
    System.out.println( e );
    super.dispatchEvent( e );
  }
}

// $Id: NullCompleter.java,v 1.1 2004/10/26 19:20:23 greg Exp $

public class NullCompleter implements Completer
{
  public String complete( String incomplete ) {
    return incomplete;
  }

  public void reset() {
  }
}

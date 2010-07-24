// $Id: Completer.java,v 1.1 2004/10/26 19:20:23 greg Exp $

public interface Completer
{
  public String complete( String incomplete );
  public void reset();
}

// $Id: PlugInUtils.java,v 1.1 2004/10/26 19:20:23 greg Exp $

public class PlugInUtils
{
  static public PlugIn getPlugIn( String name ) {
    try {
      Class clasz = Class.forName( name );
      PlugIn pi = (PlugIn)clasz.newInstance();
      return pi;
    } catch( Exception e ) {
      return null;
    }
  }
}

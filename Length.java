// $Id: Length.java,v 1.1 2004/10/26 19:20:23 greg Exp $

public class Length
{
  static public void main( String args[] ) throws Exception {
    for (int i=0; i<args.length; ++i) {
      System.out.println( args[i]+": "+new Sound( args[i] ).length() );
    }
  }
}

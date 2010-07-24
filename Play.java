// $Id: Play.java,v 1.1 2004/10/26 19:20:23 greg Exp $

public class Play
{
  static public void main( String args[] ) throws Exception {
    for (int i=0; i<args.length; ++i) {
      new Sound( args[i] ).play();
    }
  }
}

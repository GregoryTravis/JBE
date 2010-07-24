// $Id: Monoify.java,v 1.1 2004/10/26 19:20:23 greg Exp $

public class Monoify
{
  static public void main( String args[] ) throws Exception {
    Sound sound = new Sound( args[0] );
    sound.saveTo( args[1] );
  }
}

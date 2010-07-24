// $Id: KeyMap.java,v 1.1 2004/10/26 19:20:23 greg Exp $

public class KeyMap
{
  private String prefix;
  private Object table[][] = new Object[256][];

  public KeyMap( String prefix ) {
    this.prefix = prefix;
  }

  public String getPrefix() { return prefix; }

  public Object lookup( int keyCode ) {
    int bkc = keyCode & 0xff;
    int mods = keyCode >> 8;
    Object ma[] = table[bkc];
    if (ma ==null)
      return null;
    return ma[mods];
  }

  public void set( int keyCode, Object o ) {
    int bkc = keyCode & 0xff;
    int mods = keyCode >> 8;
    Object ma[] = table[bkc];
    if (ma==null) {
      table[bkc] = ma = new Object[8];
    }
    ma[mods] = o;
  }
}

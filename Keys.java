// $Id: Keys.java,v 1.1 2004/10/26 19:20:23 greg Exp $

import java.awt.event.*;
import java.lang.reflect.*;
import java.util.*;

public class Keys
{
  private Object target;
  private final int SHIFT = 1<<8;
  private final int CTRL = 1<<9;
  private final int ALT = 1<<10;
  private Hashtable keyCodes = new Hashtable();
  private int tolowers[] = new int[256];
  private boolean ignores[] = new boolean[256];
  private KeyMap rootMap = new KeyMap( "" );
  private KeyMap cursor = rootMap;
  private String keyString = "";

  public Keys( Object target ) {
    this.target = target;
  }

  public String process( KeyEvent ke ) {
    int kc = toKeyCode( ke );
    if (ignores[kc&0xff])
      return keyString;
    Object o = cursor.lookup( kc );
    if (o==null) {
      cursor = rootMap;
      keyString = "";

      return "????";
    } else if (o instanceof String) {
      String command = (String)o;
      runCommand( command );
      String ks = keyString;

      cursor = rootMap;
      keyString = "";

      return command;
    } else if (o instanceof KeyMap) {
      cursor = (KeyMap)o;
      keyString = cursor.getPrefix()+" --";
      return keyString;
    } else {
      throw new RuntimeException( "Shouldn't reach here" );
    }
  }

  private String dummys[] = new String[0];
  private String[] splitKeySpec( String keySpec ) {
    ArrayList list = new ArrayList();
    int lastc=0;
    while (true) {
      int c = keySpec.indexOf( ' ', lastc );
      if (c==-1) {
        String elem = keySpec.substring( lastc );
        if (elem.length()>0)
          list.add( elem );
        break;
      }
      String elem = keySpec.substring( lastc, c );
      if (elem.length()>0)
        list.add( elem );
      lastc = c+1;
      if (c>=keySpec.length())
        break;
    }
    return (String[])list.toArray( dummys );
  }

  public void add( String keySpec, String command ) {
    String keySpecs[] = splitKeySpec( keySpec );
    int keys[] = new int[keySpecs.length];

    for (int i=0; i<keys.length; ++i) {
      keys[i] = parseSpec( keySpecs[i] );
    }

    KeyMap km = rootMap;

    for (int i=0; i<keys.length; ++i) {
      int kc = keys[i];
      Object o = km.lookup( kc );
      if (i==keys.length-1) {
        if (o instanceof KeyMap) {
          throw new RuntimeException( "Prefix/sequence collision: "+keySpec );
        } else {
          km.set( kc, command );
        }
      } else {
        if (o instanceof String) {
          throw new RuntimeException( "Prefix/sequence collision: "+keySpec );
        } else {
          KeyMap skm = (KeyMap)o;
          if (skm==null) {
            skm = new KeyMap( km.getPrefix()+" "+keySpecs[i] );
            km.set( kc, skm );
          }
          km = skm;
        }
      }
    }
  }

  private int parseSpec( String key ) {
    StringBuffer sb = new StringBuffer( key );
    boolean shift = false, ctrl = false, alt = false;
    int i=0;

    while (true) {
      if (sb.charAt( i ) != '\\')
        break;

      i++;
      char c = sb.charAt( i );

      if (c=='s' || c=='S')
        shift = true;
      else if (c=='a' || c=='A')
        alt = true;
      else if (c=='c' || c=='C')
        ctrl = true;
      else
        throw new RuntimeException( "Error: can't parse key "+key );

      i++;
    }

    int lb = key.indexOf( "[", i );
    int rb = key.indexOf( "]", i );
    if (lb == -1 || rb == -1) {
      throw new RuntimeException( "Error: can't parse key "+key );
    }
    String baseKey = sb.substring( lb+1, rb );

    if (baseKey.charAt( 0 )=='%') {
      int k = Integer.parseInt( baseKey.substring( 1 ) );
      baseKey = ""+(char)k;
    }

    if (baseKey.length()==1) {
      int c = baseKey.charAt( 0 );
      if (tolowers[c] != -1) {
        shift = true;
        c = tolowers[c];
        baseKey = ""+(char)c;
      }
    }

    Integer ii = (Integer)keyCodes.get( baseKey );
    if (ii == null) {
      throw new RuntimeException( "Unknown key: "+key );
    }
    int baseKeyCode = ii.intValue();

    int keyCode = baseKeyCode;
    if (alt)
      keyCode |= ALT;
    if (ctrl)
      keyCode |= CTRL;
    if (shift)
      keyCode |= SHIFT;

    return keyCode;
  }

  private int toKeyCode( KeyEvent ke ) {
    int keyCode = ke.getKeyCode();
    int mods = ke.getModifiers();
    if ((mods & KeyEvent.ALT_MASK) == KeyEvent.ALT_MASK) {
      keyCode |= ALT;
    }
    if ((mods & KeyEvent.CTRL_MASK) == KeyEvent.CTRL_MASK) {
      keyCode |= CTRL;
    }
    if ((mods & KeyEvent.SHIFT_MASK) == KeyEvent.SHIFT_MASK) {
      keyCode |= SHIFT;
    }
    return keyCode;
  }

  {
    for (int i=0; i<tolowers.length; ++i)
      tolowers[i] = -1;

    tolowers[(int)'~'] = '`';
    tolowers[(int)'!'] = '1';
    tolowers[(int)'@'] = '2';
    tolowers[(int)'#'] = '3';
    tolowers[(int)'$'] = '4';
    tolowers[(int)'%'] = '5';
    tolowers[(int)'^'] = '6';
    tolowers[(int)'&'] = '7';
    tolowers[(int)'*'] = '8';
    tolowers[(int)'('] = '9';
    tolowers[(int)')'] = '0';
    tolowers[(int)'_'] = '-';
    tolowers[(int)'+'] = '=';
    tolowers[(int)'|'] = '\\';
    tolowers[(int)'{'] = '[';
    tolowers[(int)'}'] = ']';
    tolowers[(int)':'] = ';';
    tolowers[(int)'\"'] = '\'';
    tolowers[(int)'<'] = ',';
    tolowers[(int)'>'] = '.';
    tolowers[(int)'?'] = '/';
    tolowers[(int)'Q'] = 'q';
    tolowers[(int)'W'] = 'w';
    tolowers[(int)'E'] = 'e';
    tolowers[(int)'R'] = 'r';
    tolowers[(int)'T'] = 't';
    tolowers[(int)'Y'] = 'y';
    tolowers[(int)'U'] = 'u';
    tolowers[(int)'I'] = 'i';
    tolowers[(int)'O'] = 'o';
    tolowers[(int)'P'] = 'p';
    tolowers[(int)'A'] = 'a';
    tolowers[(int)'S'] = 's';
    tolowers[(int)'D'] = 'd';
    tolowers[(int)'F'] = 'f';
    tolowers[(int)'G'] = 'g';
    tolowers[(int)'H'] = 'h';
    tolowers[(int)'J'] = 'j';
    tolowers[(int)'K'] = 'k';
    tolowers[(int)'L'] = 'l';
    tolowers[(int)'Z'] = 'z';
    tolowers[(int)'X'] = 'x';
    tolowers[(int)'C'] = 'c';
    tolowers[(int)'V'] = 'v';
    tolowers[(int)'B'] = 'b';
    tolowers[(int)'N'] = 'n';
    tolowers[(int)'M'] = 'm';

    for (int i=0; i<10; ++i) {
      String keyName = ""+(char)(i+'0');
      int code = i+48;
      keyCodes.put( keyName, new Integer( code ) );
    }

    for (int i=0; i<26; ++i) {
      String keyName = ""+(char)(i+'a');
      int code = i+65;
      keyCodes.put( keyName, new Integer( code ) );
    }

    keyCodes.put( "-", new Integer( 45 ) );
    keyCodes.put( "+", new Integer( 61 ) );
    keyCodes.put( "|", new Integer( 92 ) );
    keyCodes.put( "lsqb", new Integer( 91 ) );
    keyCodes.put( "rsqb", new Integer( 93 ) );
    keyCodes.put( "~", new Integer( 192 ) );
    keyCodes.put( ";", new Integer( 59 ) );
    keyCodes.put( "'", new Integer( 222 ) );
    keyCodes.put( ",", new Integer( 44 ) );
    keyCodes.put( ".", new Integer( 46 ) );
    keyCodes.put( "/", new Integer( 47 ) );
    keyCodes.put( " ", new Integer( 32 ) );

    ignores[16] = true;
    ignores[17] = true;
    ignores[18] = true;
  }

  private void runCommand( String command ) {
    try {
      Class clas = target.getClass();
      Class argsProto[] = new Class[0];
      Method method = clas.getMethod( command, argsProto );
      Object args[] = new Object[0];

      try {
        method.invoke( target, args );
      } catch( RuntimeException e ) {
        e.printStackTrace();
      }
    } catch( IllegalAccessException iae ) {
      System.out.println( "Can't execute "+command );
      iae.printStackTrace();
    } catch( NoSuchMethodException nsme ) {
      System.out.println( "Can't execute "+command );
      nsme.printStackTrace();
    } catch( InvocationTargetException ite ) {
      System.out.println( "Can't execute "+command );
      ite.printStackTrace();
    }
  }
}

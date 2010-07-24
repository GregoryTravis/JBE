// $Id: InputString.java,v 1.1 2004/10/26 19:20:23 greg Exp $

import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class InputString
{
  static private final Font bigFont = new Font( "Courier", Font.BOLD, 48 );
  static private Color inputColor = Color.white;

  static private Vector history = new Vector();
  static private final int historyMax = 4;

  static private String inputString = "";

  static private void showInputString( Component comp, FontMetrics bigFM ) {
    Graphics g = comp.getGraphics();
    Dimension d = comp.getSize();
    comp.update( g );
    int w = bigFM.stringWidth( inputString );
    int h = bigFM.getAscent()+bigFM.getDescent();
    int x = (d.width-w)/2;
    int y = (d.height-h)/2 + h;

    if (x+w > d.width) {
      x = d.width-w;
    }

    g.setFont( bigFont );
    g.setColor( Color.black );
    g.drawString( inputString, x+4, y+4 );
    g.setColor( inputColor );
    g.drawString( inputString, x, y );
    g.dispose();
  }

  static public String readString( Component comp ) {
    return readString( comp, new NullCompleter() );
  }

  static public String readString( Component comp, String init ) {
    return readString( comp, new NullCompleter(), init );
  }

  static public String readString( Component comp, Completer completer ) {
    return readString( comp, completer, "" );
  }

  static public String readString( Component comp, Completer completer,
      String init ) {
    FontMetrics bigFM = comp.getFontMetrics( bigFont );
    int historyCursor = history.size();
    String historySave = "";
    Vector savedEvents = new Vector();

    EventQueue eq = Toolkit.getDefaultToolkit().getSystemEventQueue();
    String s = init;

    inputString = "% "+s+"_";
    showInputString( comp, bigFM );

    completer.reset();

    while (true) {
      try {
        AWTEvent ae = eq.getNextEvent();

        if (ae.getID() == KeyEvent.KEY_PRESSED) {
          KeyEvent ke = (KeyEvent)ae;

          int kc = ke.getKeyCode();
          if (kc==16 || kc==17 || kc==18) {
            continue;
          }

          char c = ke.getKeyChar();
          if (c==27) {
            s = null;
            break;
          } else if (c==8) {
            if (s.length()>0)
              s = s.substring( 0, s.length()-1 );
          } else if (c==10) {
            comp.repaint();
            break;
          } else if (c==9) {
            s = completer.complete( s );
          } else {
            s += c;
          }

          inputString = "% "+s+"_";
          showInputString( comp, bigFM );
        } else if (ae.getID() == KeyEvent.KEY_RELEASED) {
          KeyEvent ke = (KeyEvent)ae;
          int kc = ke.getKeyCode();
          boolean doHistory = false;
          if (kc == KeyEvent.VK_DOWN) {
            if (historyCursor==history.size())
              historySave = s;
            historyCursor++;
            if (historyCursor>history.size())
              historyCursor = history.size();
            doHistory = true;
          } else if (kc == KeyEvent.VK_UP) {
            if (historyCursor==history.size())
              historySave = s;
            historyCursor--;
            if (historyCursor<0)
              historyCursor = 0;
            doHistory = true;
          }

          if (doHistory) {
            s = historyCursor == history.size() ?
                historySave : (String)history.elementAt( historyCursor );
            inputString = "% "+s+"_";
            showInputString( comp, bigFM );
          }
        } else if (ae instanceof PaintEvent) {
            inputString = "% "+s+"_";
            showInputString( comp, bigFM );
        } else if (ae instanceof InvocationEvent) {
          savedEvents.addElement( ae );
        } else {
        }
      } catch( InterruptedException ie ) {}
    }

    comp.repaint();

    if (s != null) {
      history.addElement( s );
    }

    while (history.size()>historyMax)
      history.removeElementAt( 0 );

    for (int i=0; i<savedEvents.size(); ++i) {
      AWTEvent ae = (AWTEvent)savedEvents.elementAt( i );
      eq.postEvent( ae );
    }

    return s;
  }

    
  static public int readInt( Component comp, int nullValue ) {
    return readInt( comp, "", nullValue );
  }

  static public int readInt( Component comp, int init, int nullValue ) {
    return readInt( comp, init+"", nullValue );
  }

  static private int readInt( Component comp, String init, int nullValue ) {
    try {
      int ival = Integer.parseInt( readString( comp, init ) );
      return ival;
    } catch( NumberFormatException nfe ) {
      return nullValue;
    }
  }

  static public double readDouble( Component comp ) {
    return readDouble( comp, Double.NaN );
  }

  static public double readDouble( Component comp, double nullValue ) {
    return readDouble( comp, "", nullValue );
  }

  static public double readDouble( Component comp, double init, double nullValue ) {
    return readDouble( comp, init+"", nullValue );
  }

  static public double readDouble( Component comp, String init, double nullValue ) {
    try {
      String s = readString( comp, init );
      if (s==null)
        return nullValue;
      double dval = Double.parseDouble( s );
      return dval;
    } catch( NumberFormatException nfe ) {
      return nullValue;
    }
  }

  static public final int YES=1, NO=2, CANCEL=3;
  static public int ynorc( Component comp, String message ) {
    int val = 0;

    FontMetrics bigFM = comp.getFontMetrics( bigFont );

    EventQueue eq = Toolkit.getDefaultToolkit().getSystemEventQueue();

    inputString = message;
    showInputString( comp, bigFM );

    while (true) {
      try {
        AWTEvent ae = eq.getNextEvent();

        if (ae.getID() == KeyEvent.KEY_TYPED) {
          KeyEvent ke = (KeyEvent)ae;
          char c = ke.getKeyChar();
          if (c=='y' || c=='Y') {
            val = YES;
            break;
          } else if (c==27) {
            val = CANCEL;
            break;
          } else if (c=='n' || c=='N') {
            val = NO;
            break;
          } else {}
        }
      } catch( InterruptedException ie ) {}
    }

    comp.repaint();
    return val;
  }

  static public boolean yorn( Component comp, String message ) {
    return ynorc( comp, message )==YES;
  }
}

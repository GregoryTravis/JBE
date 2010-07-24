// $Id: UndoStack.java,v 1.1 2004/10/26 19:20:23 greg Exp $

import java.util.*;

public class UndoStack
{
  private int maxSize;
  private Vector undos = new Vector();
  private int cursor = 0;

  public UndoStack( int maxSize ) {
    this.maxSize = maxSize;
    //report();
  }

  public void put( byte undo[] ) {
    while (undos.size()>cursor)
      undos.removeElementAt( undos.size()-1 );
    undos.addElement( undo );
    cursor++;

    while (undos.size()>maxSize) {
      undos.removeElementAt( 0 );
      cursor--;
    }

    //report();
  }

  public byte[] undo() {
    if (cursor==0) {
      return null;
    } else {
      byte undo[] = (byte[])undos.elementAt( --cursor );
      //report();
      return undo;
    }
  }

  public byte[] redo() {
    if (cursor>=undos.size()-1) {
      return null;
    } else {
      byte redo[] = (byte[])undos.elementAt( ++cursor );
      //report();
      return redo;
    }
  }

  private void report() {
    System.out.println( "Undo len "+undos.size()+" cur "+cursor );
  }

  public boolean inHistory() {
    return cursor != undos.size();
  }
}

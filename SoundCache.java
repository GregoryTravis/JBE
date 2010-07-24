// $Id: SoundCache.java,v 1.1 2004/10/26 19:20:23 greg Exp $

import java.io.*;
import java.util.*;

public class SoundCache
{
  static private Hashtable sounds = new Hashtable();

  static public Sound load( String filename ) throws IOException {
filename = filename.replace( '\\', '/' );
    return load( new File( filename ) );
  }

  static public Sound load( File file ) throws IOException {
    SoundCacheEntry sce = (SoundCacheEntry)sounds.get( file.getCanonicalPath() );

    if (sce == null) {
      sce = new SoundCacheEntry( new Sound( file ) );
      sounds.put( file.getCanonicalPath(), sce );
    }

    return sce.sound;
  }

  static public void markAllUnused() {
    for (Enumeration e=sounds.elements(); e.hasMoreElements();) {
      SoundCacheEntry sce = (SoundCacheEntry)e.nextElement();
      sce.inuse = false;
    }
  }

  static public void markUsed( Sound sound ) {
    for (Enumeration e=sounds.keys(); e.hasMoreElements();) {
      String filename = (String)e.nextElement();
      SoundCacheEntry sce = (SoundCacheEntry)sounds.get( filename );
      if (sce.sound.equals( sound )) {
        sce.inuse = true;
        return;
      }
    }
  }

  static public void removeUnused() {
    Vector toRemove = new Vector();
    for (Enumeration e=sounds.keys(); e.hasMoreElements();) {
      String filename = (String)e.nextElement();
      SoundCacheEntry sce = (SoundCacheEntry)sounds.get( filename );
      if (!sce.inuse) {
        toRemove.addElement( filename );
      }
    }
    for (Enumeration e=toRemove.elements(); e.hasMoreElements();) {
      String filename = (String)e.nextElement();
      System.out.println( "Collected "+filename );
      sounds.remove( filename );
    }
  }

  static public void ensureWritten( Sound sound ) throws IOException {
    sound.ensureWritten();
    String filename = sound.getSource();
    filename = new File( filename ).getCanonicalPath();
    if (!sounds.containsKey( filename ))
      sounds.put( filename, new SoundCacheEntry( sound ) );
  }
}

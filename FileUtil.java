// $Id: FileUtil.java,v 1.1 2004/10/26 19:20:23 greg Exp $

import java.io.*;
import java.util.*;

public class FileUtil
{
  synchronized static public String getNumberedFile( String category ) {
    final int maxTry = 1024;
    for (int i=0; i<maxTry; ++i) {
      String name = category+i+".wav";
      File file = new File( name );
      if (!file.exists()) {
        return name;
      }
    }
    return null;
  }

  static public File nextFile( File file ) throws IOException {
    return nextFile( file, 1 );
  }

  static public File prevFile( File file ) throws IOException {
    return nextFile( file, -1 );
  }

  static public File nextFile( File file, int increment ) throws IOException {
    File dir = file.getParentFile();

    if (dir==null)
      dir = new File( "." );

    File contents[] = dir.listFiles( new FileFilter() {
      public boolean accept( File file ) {
        return file.getName().toLowerCase().endsWith( ".wav" );
      }
    } );

    Arrays.sort( contents );

    for (int i=0; i<contents.length; ++i) {
      try {
        if (contents[i].getCanonicalPath().equals( file.getCanonicalPath() ) ||
            file==null) {
          int inx = file==null ? 0 : (i+contents.length+increment)%contents.length;
          File next = contents[inx];
          return next;
        }
      } catch( IOException ie ) {
        System.out.println( "Can't get canonical paths." );
      }
    }

    return null;
  }
}

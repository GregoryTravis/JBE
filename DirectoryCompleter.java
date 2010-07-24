// $Id: DirectoryCompleter.java,v 1.1 2004/10/26 19:20:23 greg Exp $

import java.io.*;

public class DirectoryCompleter implements Completer
{
  private File lastDirectory;
  private File workingDir = new File( System.getProperty( "user.dir" ) );

  public DirectoryCompleter() {
    lastDirectory = workingDir;
  }

  public String complete( String incomplete ) {
    try {
      if (incomplete=="")
        return lastDirectory.getPath();
      File ifile = new File( incomplete );

      boolean isAbs = ifile.isAbsolute();

      ifile = ifile.getCanonicalFile();

      File dir = null;
      String stub = null;
      if (ifile.isDirectory() && incomplete.endsWith( "/" )) {
        dir = ifile;
        stub = "";
      } else {
        dir = ifile.getParentFile();
        stub = ifile.getName();
      }

      File files[] = dir.listFiles();

      String gcs = null;

      File match = null;
      int count = 0;
      for (int i=0; i<files.length; ++i) {
        if (files[i].getName().startsWith( stub )) {
          match = files[i];
          System.out.println( "match "+stub+" "+files[i] );
          gcs = greatestCommonSubstring( gcs, files[i].getName() );
          System.out.println( "gcs "+gcs );
          count++;
          if (count>1)
            break;
        }
      }

      File ret = null;
      if (match == null) {
        ret = ifile;
      } else if (count>1) {
        ret = new File( dir+"/"+gcs );
      } else {
        ret = match;
      }

      boolean retIsDir = ret.isDirectory();
      lastDirectory = retIsDir ? ret : ret.getParentFile();

      String rets = ret.getPath();

      if (!isAbs)
        rets = relativizeFilename( rets, workingDir );

      if (retIsDir && !rets.endsWith( File.separator ))
        rets += File.separator;

      rets = rets.replace( File.separator.charAt( 0 ), '/' );

      return rets;
    } catch( IOException ie ) {
      return incomplete;
    }
  }

  static private String greatestCommonSubstring( String a, String b ) {
    if (a==null)
      return b;

    int len = Math.min( a.length(), b.length() );
    for (int i=0; i<len; ++i) {
      if (a.charAt( i ) != b.charAt( i )) {
        return a.substring( 0, i );
      }
    }
    return a;
  }

  static private String canonical( String s ) {
    try {
      return new File( s ).getCanonicalPath();
    } catch( IOException ie ) {
      ie.printStackTrace();
      return s;
    }
  }

  static private boolean equals( String a, String b ) {
    return canonical( a ).equals( canonical( b ) );
  }

  static public String relativizeFilename( String filename,
      File directory ) {
    String dirname = directory.getPath();

    filename = canonical( filename );
    dirname = canonical( dirname );

    int sc=0;
    while (true) {
      if (sc>=filename.length() || sc>=dirname.length() ||
          filename.charAt( sc )!=dirname.charAt( sc )) {
        break;
      }
      sc++;
    }
    while (sc<filename.length() &&
           filename.charAt( sc ) != File.separatorChar) {
      sc--;
    }

    String commonRoot = filename.substring( 0, sc );

    String relative = "";

    // go up to common root
    String du = dirname;
    while (true) {
      if (equals( du, commonRoot ))
        break;
      String parent = new File( du ).getParent();
      du = parent;
      relative += ".."+File.separatorChar;
    }

    if (sc<filename.length())
      relative += filename.substring( sc+1 );

    return relative;
  }

  public void reset() {
  }
}

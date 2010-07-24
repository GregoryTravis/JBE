// $Id: AudOutputStream.java,v 1.1 2004/10/26 19:20:23 greg Exp $

import java.io.*;
import javax.sound.sampled.*;

public class AudOutputStream implements Runnable
{
  private OutputStream outduct;
  private AudioInputStream induct;
  private AudioFormat format;
  private File outfile;
  static private final int bufsize = 65536 * 8;
  private byte buffer[] = new byte[bufsize];
  private int size;

  public AudOutputStream( int size, File outfile ) {
    this.outfile = outfile;
    this.size = size;
  }

  public AudOutputStream( int size, File outfile, AudioFormat format ) {
    this( size, outfile );
    initFormat( format );
  }

  public AudOutputStream( int size, String filename ) {
    this( size, new File( filename ) );
  }

  public synchronized void concatenateFile( String filename ) throws IOException {
    concatenateFile( new File( filename ) );
  }

  public synchronized void concatenateBuffer( byte buffer[] ) throws IOException {
    setupPipe();
    outduct.write( buffer );
    //outduct.flush();
  }

  public synchronized void concatenateFile( File file ) throws IOException {
    concatenateFile( file, -1, -1 );
  }

  public synchronized void concatenateFile( File file, long start, long end )
      throws IOException {
    AudioInputStream ain = null;
    try {
      ain = AudioSystem.getAudioInputStream( file );
      concatenateStream( ain, start, end );
    } catch( UnsupportedAudioFileException uafe ) {
      throw new IOException( uafe.toString() );
    } finally {
      ain.close();
    }
  }

  public synchronized void concatenateStream( AudioInputStream ain ) throws IOException {
    concatenateStream( ain, -1, -1 );
  }

  public synchronized void concatenateStream( AudioInputStream ain,
      long start, long end ) throws IOException {
    initFormat( ain );
    setupPipe();

    int frameSize = ain.getFormat().getFrameSize();

    long lastByte = -1;
    if (end != -1) {
      lastByte = end * frameSize;
      //System.out.println( "Reading to "+lastByte );
    }

    long bytesRead = 0;

    if (start != -1) {
      long skipbytes = start * frameSize;

      long sb = skipbytes;
      while (sb>0) {
        long sk = ain.skip( sb );
        bytesRead += sk;
        //System.out.println( "Skipped "+sk+" bytes" );
        //if (sk != skipbytes) {
        //  System.out.println( "Only skipped "+sk+" of "+sb );
        //}
        sb -= sk;
      }

      if (sb != 0) {
        throw new RuntimeException( "Skipped too far: "+sb );
      }

      //System.out.println( "Skipped "+skipbytes+" total" );
    }

    while (true) {
      if (lastByte != -1 && bytesRead >= lastByte)
        break;

      long toread = buffer.length;
      if (lastByte != -1) {
        toread = lastByte - bytesRead;
        if (toread > buffer.length)
          toread = buffer.length;
      }

      int r = ain.read( buffer, 0, (int)toread );
      if (r==0 || r==-1)
        break;

      bytesRead += r;

      //System.out.println( "Read "+r+" making "+bytesRead );

      outduct.write( buffer, 0, r );

      outduct.flush();
    }
  }

  private void initFormat( AudioFormat format ) {
    this.format = format;
  }

  private void initFormat( AudioInputStream examp ) {
    initFormat( examp.getFormat() );
  }

  private void setupPipe() throws IOException {
    if (outduct!=null) {
      return;
    }

    PipedInputStream pin = new PipedInputStream();
    outduct = new PipedOutputStream( pin );
    induct = new AudioInputStream( pin, format, size );

    Thread thread = new Thread( this, "AOS background thread" );
    thread.setDaemon( true );
    thread.start();
  }

  public void run() {
    try {
      AudioSystem.write( induct, AudioFileFormat.Type.WAVE, outfile );
    } catch( IOException ie ) {
      System.out.println( "Error in AudOutputStream background thread: "+ie );
      ie.printStackTrace();
    }
  }

  private void setFormat( AudioFormat format ) {
    if (this.format != null) {
      throw new IllegalStateException( "Format already set to "+this.format );
    }
    
    this.format = format;
  }

  public void close() throws IOException {
    outduct.flush();
    outduct.close();
  }
}

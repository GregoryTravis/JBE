// $Id: Looper.java,v 1.2 2004/10/26 20:29:04 greg Exp $

import javax.sound.sampled.*;

public class Looper implements Runnable
{
  private SourceDataLine sdl;
  private LooperListener looperListener;
  private byte currentRaw[];
  static private final int innerLoopWriteSize = 512;
  private Queue incoming = new Queue();
  private Object soundLock = new Object();

  static private final double sysLatencyTime = 0.695;
  static private final int sysLatency =
    (int)(sysLatencyTime*Constants.sampRate);

  public Looper() {
    this( null );
  }

  public Looper( LooperListener looperListener ) {
    this.looperListener = looperListener;

    sdl = Util.getOutputLine();

    Thread t = new Thread( this, "looper" );
    t.start();
  }

  // set to null to turn sound off
  public void setSound( Sound sound ) {
    byte raw[] = sound==null ? null : sound.getRawBytes();
    Pair p = new Pair( sound, raw );
    incoming.put( p );
    synchronized( soundLock ) {
      soundLock.notifyAll();
    }
  }

  public void run() {
    try {
      sdl.open( Util.stdFormat );
      sdl.start();
    } catch( LineUnavailableException lue ) {
      lue.printStackTrace();
      System.out.println( sdl );
      throw new RuntimeException( lue.toString() );
    }

    int cursor = 0;

    while (true) {
      // Grab new sounds
      while (incoming.numWaiting()>0) {
        byte[] lcr = currentRaw;

        Pair p = (Pair)incoming.get();
        Sound currentSound = (Sound)p.car;
        currentRaw = (byte[])p.cdr;
        if (currentSound != null && looperListener !=null)
          looperListener.looperListenerAlert( currentSound );

        if (currentRaw != null) {
          int backlog = sdl.getBufferSize()-sdl.available();
          cursor -= backlog;
          cursor -= sysLatency*2; // 16 bit, don'tcha know
          if (cursor < 0) {
            cursor += currentRaw.length;
            if (cursor<0)
              cursor = 0;
          }
          sdl.flush();
        }

        if (currentRaw == null || lcr == null ||
          currentRaw.length != lcr.length ) {
          cursor = 0;
        }
      }

      synchronized( soundLock ) {
        if (currentRaw==null) {
          try {
            soundLock.wait();
            continue;
          } catch( InterruptedException ie ) {}
        }
      }

      int bytesLeft = currentRaw.length-cursor;
      if (bytesLeft<=0) {
        // restart sound
        cursor = 0;
        bytesLeft = currentRaw.length;
      }

      int towrite = innerLoopWriteSize;
      if (towrite > bytesLeft)
        towrite = bytesLeft;

      int r = sdl.write( currentRaw, cursor, towrite );

      if (r==-1)
        throw new RuntimeException( "Can't write to line!" );

      cursor += r;
    }
  }
}

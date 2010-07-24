// $Id: Sound.java,v 1.1 2004/10/26 19:20:23 greg Exp $

import java.io.*;
import javax.sound.sampled.*;

public class Sound
{
  private String source = "";
  private short raw[];
  static final private int sampRate = 44100;
  private boolean written = false;

  private Sound() {
  }

  public Sound( String filename ) throws IOException {
    loadFrom( new File( filename ), false );
  }

  public Sound( File file ) throws IOException {
    loadFrom( file, false );
  }

  public Sound( String filename, long start, long end ) throws IOException {
    loadFrom( new File( filename ), false, start, end );
  }

  public Sound( File file, long start, long end ) throws IOException {
    loadFrom( file, false, start, end );
  }

  static public boolean isStereo( String filename ) throws IOException {
    try {
      File file = new File( filename );
      AudioInputStream ain = AudioSystem.getAudioInputStream( file );
      boolean stereo = ain.getFormat().getChannels()==2;
      ain.close();
      return stereo;
    } catch( UnsupportedAudioFileException uafe ) {
      throw new IOException( uafe.toString() );
    }
  }

  protected void loadFrom( File file, boolean right ) throws IOException {
    loadFrom( file, right, -1, -1 );
  }

  protected void loadFrom( File file, boolean right, long start, long end )
      throws IOException {
//System.out.println( "Loading "+file+" ("+start+" to "+end+")" );
    try {
      if (start == -1 && end == -1) {
        source = file.getPath();
      }

      AudioInputStream ain = AudioSystem.getAudioInputStream( file );

      boolean stereo = ain.getFormat().getChannels()==2;

      if (stereo) {
        //System.out.println( "Loading "+(right?"right":"left")+
        //  " channel of "+file );
      }

      long len=0;
      if (end != -1) {
        len = end - (start==-1 ? 0 : start);
      } else {
        len = ain.getFrameLength();
      }

//      long keepLen = stereo ? len/2 : len;
      int loadLen = (int)(stereo ? len*4 : len*2);
      long keepLen = len;
      //System.out.println( stereo+" "+len+" "+keepLen+" "+loadLen );

      raw = new short[(int)keepLen];
      byte bs[] = new byte[loadLen];

      if (start != -1) {
        long toSeek = start;
        toSeek *= (stereo ? 4 : 2);
        while (toSeek>0) {
          long sk = ain.skip( toSeek );
          toSeek -= sk;
        }
        if (toSeek != 0) {
          throw new RuntimeException( "Skipped too far: "+toSeek );
        }
      }

      int r = ain.read( bs );
      if (r != loadLen)
        throw new RuntimeException( "Read only "+r+" of "+file+" out of "+
          loadLen+" sound bytes" );
      int inx=0;
      if (stereo && right)
        inx += 2;
      for (int i=0; i<keepLen; ++i) {
        byte b0 = bs[inx++];
        byte b1 = bs[inx++];
        short s = (short)(((b1&0xff)<<8) | (b0&0xff));
        raw[i] = s;
        if (stereo)
          inx += 2;
      }

      ain.close();
    } catch( UnsupportedAudioFileException uafe ) {
      throw new IOException( uafe.toString() );
    }

    written = true;
  }

  protected void saveTo( String filename ) throws IOException {
    saveTo( new File( filename ) );
  }

  protected void saveTo( File file ) throws IOException {
    saveTo( file, Util.stdFormat );
  }

  protected void saveTo( String filename, AudioFormat fmt )
      throws IOException {
    saveTo( new File( filename ), fmt );
  }

  protected void saveTo( File file, AudioFormat fmt ) throws IOException {
    ByteArrayInputStream bais = new ByteArrayInputStream( getRawBytes() );
    AudioInputStream ain = new AudioInputStream( bais, fmt,
      length() );
    AudioSystem.write( ain, AudioFileFormat.Type.WAVE, file );
  }

  public void writeTo( OutputStream out ) throws IOException {
    ByteArrayInputStream bais = new ByteArrayInputStream( getRawBytes() );
    AudioInputStream ain = new AudioInputStream( bais, Util.stdFormat,
      length() );
    AudioSystem.write( ain, AudioFileFormat.Type.WAVE, out );
  }

  public void ensureWritten() throws IOException {
    if (written) return;

    if (source.equals( "" )) {
      source = generateFilename();
    }

System.out.println( "writing to "+source );
    saveTo( source );
    written = true;
  }

  static private int filenameSerial=0;
  static synchronized public String generateFilename() {
    while (true) {
      File file = new File( "gen"+filenameSerial+".wav" );
      filenameSerial++;
      if (!file.exists())
        return file.getName();
    }
  }

  public Sound( int len ) {
    raw = new short[len];
  }

  Sound( short raw[] ) {
    this.raw = raw;
  }

  public Sound dup() {
    Sound nu = new Sound( length() );
    System.arraycopy( raw(), 0, nu.raw(), 0, length() );
    return nu;
  }

  public Sound subSound( int start, int end ) {
    if (end <= start)
      return null;

    short nraw[] = new short[end-start];
    System.arraycopy( raw, start, nraw, 0, end-start );
    return new Sound( nraw );
  }

  public Sound concat( Sound sound ) {
    short nraw[] = new short[length()+sound.length()];
    System.arraycopy( raw(), 0, nraw, 0, raw().length );
    System.arraycopy( sound.raw(), 0, nraw, raw().length, sound.length() );
    return new Sound( nraw );
  }

  static public Sound concat( Sound sounds[] ) {
    int totlen=0;
    for (int s=0; s<sounds.length; ++s) {
      totlen += sounds[s].length();
    }
    Sound ns = new Sound( totlen );
    short raw[] = ns.raw();
    int cursor=0;
    for (int s=0; s<sounds.length; ++s) {
      short rraw[] = sounds[s].raw();
      System.arraycopy( rraw, 0, raw, cursor, rraw.length );
      cursor += rraw.length;
    }

    return ns;
  }

  public double timeLength() {
    return (double)length() / (double)sampRate;
  }

  public int length() {
    return raw.length;
  }

  public short[] raw() {
    return raw;
  }

  public boolean getWritten() { return written; }
  public void setWritten( boolean written ) { this.written = written; }

  // should this move the file, if it exists? naah.
  public void setSource( String source ) {
    this.source = source;
    written = false;
  }
  public String getSource() { return source; }

  public byte [] getRawBytes() {
    int len = length();
    byte bs[] = new byte[len*2];
    int inx=0;
    for (int i=0; i<len; ++i) {
      short s = raw[i];
      byte b0 = (byte)(s&0xff);
      byte b1 = (byte)((s>>8)&0xff);
      bs[inx++] = b0;
      bs[inx++] = b1;
    }
    return bs;
  }

  public void play() {
    try {
      byte bs[] = getRawBytes();
      int len = length();

      SourceDataLine sdl = Util.getOutputLine();
      sdl.open( Util.stdFormat );
      sdl.start();
      int binx=0;
      while (binx<len*2) {
	int r = sdl.write( bs, binx, len*2-binx );
	if (r<0) {
	  throw new RuntimeException(
            "Can't write to play sound, wrote "+binx );
	}
	binx += r;
      }
      sdl.drain();
      sdl.close();
    } catch( LineUnavailableException lue ) {
      lue.printStackTrace();
    } catch( Exception e ) {
      e.printStackTrace();
    }
  }

  public void mixOnto( Sound sound ) {
    mixOnto( sound, 0 );
  }

  public void mixOnto( Sound sound, int start ) {
    short sraw[] = sound.raw();
    int len = start+sraw.length < raw.length ?
      sraw.length : raw.length-start;
    for (int i=0; i<len; ++i)
      raw[start+i] += sraw[i];
  }


  public void gain( double d ) {
    for (int i=0; i<raw.length; ++i)
      raw[i] *= d;
  }

  public String toString() {
    return "[sound "+raw.length+"]"+
      (source == null || source.equals( "" ) ? "" : "-"+source);
  }

  public int sampRate() {
    return sampRate;
  }

  void write( DataOutputStream dout ) throws IOException {
    dout.writeUTF( source );
    dout.writeInt( sampRate );
    dout.writeInt( raw.length );
    for (int i=0; i<raw.length; ++i)
      dout.writeShort( raw[i] );
  }

  public double maxVolume() {
    short raw[] = raw();
    int mx = 0;
    for (int i=0; i<raw.length; ++i) {
      int s = raw[i];
      if (s<0) s = -s;
      if (s>mx)
        mx = s;
    }
    return (double)mx/32768;
  }

  static public Sound[] loadStereoFrom( String filename ) throws IOException {
    return loadStereoFrom( new File( filename ) );
  }

  static public Sound[] loadStereoFrom( File file ) throws IOException {
    Sound ss[] = new Sound[2];
    ss[0] = new Sound();
    ss[0].loadFrom( file, false );
    ss[1] = new Sound();
    ss[1].loadFrom( file, true );
    return ss;
  }

  static public byte [] getRawStereoBytes( Sound ss[]) {
    int len = ss[0].length();
    byte bs[] = new byte[len*4];
    short raw0[] = ss[0].raw(), raw1[] = ss[1].raw();
    int inx=0;
    for (int i=0; i<len; ++i) {
      short s0 = raw0[i], s1 = raw1[i];
      byte b0 = (byte)(s0&0xff);
      byte b1 = (byte)((s0>>8)&0xff);
      byte b2 = (byte)(s1&0xff);
      byte b3 = (byte)((s1>>8)&0xff);
      bs[inx++] = b0;
      bs[inx++] = b1;
      bs[inx++] = b2;
      bs[inx++] = b3;
    }
    return bs;
  }

  static public void saveStereoTo( Sound ss[], String filename )
      throws IOException {
    saveStereoTo( ss, new File( filename ) );
  }

  static public void saveStereoTo( Sound ss[], File file )
      throws IOException {
    saveStereoTo( ss, file, Util.stdFormatStereo );
  }

  static public void saveStereoTo( Sound ss[], String filename,
      AudioFormat fmt ) throws IOException {
    saveStereoTo( ss, new File( filename ), fmt );
  }

  static public void saveStereoTo( Sound ss[], File file, AudioFormat fmt )
      throws IOException {
    ByteArrayInputStream bais =
      new ByteArrayInputStream( getRawStereoBytes( ss ) );
    AudioInputStream ain = new AudioInputStream( bais, fmt,
      ss[0].length()*2 );
    AudioSystem.write( ain, AudioFileFormat.Type.WAVE, file );
  }

/*
  static public Sound loadFromPartial( File file, long start, long end )
      throws IOException {
    AudioInputStream ain = AudioSystem.getAudioInputStream( file );

    int frameSize = ain.getFormat().getFrameLength();

    long bytesread = 0;
    long skipbytes = start * frameSize;
    long bytesToRead = (end-start)*frameSize;
    boolean stereo = ain.getFormat().getChannels()==2;
    if (stereo)
      System.out.println( "Loading "+(right?"right":"left")+
        " channel of "+file );

    byte raw[] = new byte[bytesToRead];

    long sb = skipbytes;
    while (sb>0) {
      long sk = ain.skip( sb );
      sb -= sk;
    }

    if (sb != 0) {
      throw new RuntimeException( "Skipped too far: "+sb );
    }

    while (bytesRead < bytesToRead) {
      long toread = buffer.length;
      if (lastByte != -1) {
        toread = lastByte - bytesRead;
        if (toread > buffer.length)
          toread = buffer.length;
      }

      int r = ain.read( raw, bytesRead, lastByte-bytesRead );
      if (r==0 || r==-1)
        break;

      bytesRead += r;
    }

    if (bytesRead != bytesToRead) {
      throw new RuntimeException( "Read "+bytesRead+" instead of "+bytesToRead );
    }
  }
*/

  static public long getFrameLength( File file ) throws IOException {
    try {
      AudioInputStream ain = AudioSystem.getAudioInputStream( file );

      long frameLength = ain.getFrameLength();

      ain.close();

      return frameLength;
    } catch( UnsupportedAudioFileException uafe ) {
      IOException ie = new IOException( uafe.toString() );
      throw ie;
    }
  }
}

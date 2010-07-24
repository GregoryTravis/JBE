// $Id: LongSoundWriter.java,v 1.1 2004/10/26 19:20:23 greg Exp $

import java.io.*;
import javax.sound.sampled.*;

public class LongSoundWriter
{
  private FileOutputStream fout;

  public LongSoundWriter( String filename ) throws IOException {
    fout = new FileOutputStream( filename );
  }

  public void write( Sound sound ) throws IOException {
    sound.writeTo( fout );
  }

  public void close() throws IOException {
    fout.close();
    fout = null;
  }
}

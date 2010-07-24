// $Id: SoundRendererCacheEntry.java,v 1.1 2004/10/26 19:20:23 greg Exp $

public class SoundRendererCacheEntry
{
  public Sound sound;
  private int age;
  static private final int maxAge = 10;

  public SoundRendererCacheEntry( Sound sound ) {
    this.sound = sound;
    age=0;
  }

  public void use() {
    age = 0;
  }

  public boolean age() {
    return ++age>maxAge;
  }
}

// $Id: DB.java,v 1.1 2004/10/26 19:20:23 greg Exp $

import java.io.*;
import java.util.*;

public class DB
{
  static private final int DBTYPE = 1;
  static private final int DBHASH = 2;
  static private final int DBVECTOR = 3;
  static private final int DBNULL = 4;
  static private final int BYTEARRAY = 5;
  static private final int STRING = 6;
  static private final int INT = 7;
  static private final int FLOAT = 8;
  static private final int DOUBLE = 9;
  static private final int BOOLEAN = 10;
  private boolean unset = true;
  private Vector vector;
  private Hashtable hashtable;

  public DB() {
  }

  public DB( String filename ) throws IOException {
    read( filename );
  }

  public DB( File file ) throws IOException {
    read( file );
  }

  public void put( String key, String value ) {
    verifyHash();
    hashtable.put( key, value );
  }

  public void put( String key, int value ) {
    verifyHash();
    hashtable.put( key, value );
  }

  public void put( String key, float value ) {
    verifyHash();
    hashtable.put( key, value );
  }

  public void put( String key, double value ) {
    verifyHash();
    hashtable.put( key, value );
  }

  public void put( String key, boolean value ) {
    verifyHash();
    hashtable.put( key, value );
  }

  public void put( String key, byte ba[] ) {
    verifyHash();
    hashtable.put( key, ba );
  }

  public void put( String key, DB db ) {
    verifyHash();
    hashtable.put( key, db );
  }

  public int getInt( String key ) {
    verifyHash();
    return ((Integer)hashtable.get( key )).intValue();
  }

  public float getFloat( String key ) {
    verifyHash();
    return ((Float)hashtable.get( key )).floatValue();
  }

  public double getDouble( String key ) {
    verifyHash();
    return ((Double)hashtable.get( key )).doubleValue();
  }

  public boolean getBoolean( String key ) {
    verifyHash();
    return ((Boolean)hashtable.get( key )).booleanValue();
  }

  public String getString( String key ) {
    verifyHash();
    return (String)hashtable.get( key );
  }

  public byte [] getByteArray( String key ) {
    verifyHash();
    return (byte[])hashtable.get( key );
  }

  public DB getDB( String key ) {
    verifyHash();
    return (DB)hashtable.get( key );
  }

  public boolean existsInt( String key ) {
    verifyHash();
    Object o = hashtable.get( key );
    return o != null && o instanceof Integer;
  }

  public boolean existsFloat( String key ) {
    verifyHash();
    Object o = hashtable.get( key );
    return o != null && o instanceof Float;
  }

  public boolean existsDouble( String key ) {
    verifyHash();
    Object o = hashtable.get( key );
    return o != null && o instanceof Double;
  }

  public boolean existsBoolean( String key ) {
    verifyHash();
    Object o = hashtable.get( key );
    return o != null && o instanceof Boolean;
  }

  public boolean existsString( String key ) {
    verifyHash();
    Object o = hashtable.get( key );
    return o != null && o instanceof String;
  }

  public boolean existsByteArray( String key ) {
    verifyHash();
    Object o = hashtable.get( key );
    return o != null && o instanceof byte[];
  }

  public boolean existsDB( String key ) {
    verifyHash();
    Object o = hashtable.get( key );
    return o != null && o instanceof DB;
  }

  public void put( int index, String value ) {
    verifyVector();
    ensureRoom( index );
    vector.setElementAt( value, index );
  }

  public void put( int index, int value ) {
    verifyVector();
    ensureRoom( index );
    vector.setElementAt( value, index );
  }

  public void put( int index, float value ) {
    verifyVector();
    ensureRoom( index );
    vector.setElementAt( value, index );
  }

  public void put( int index, double value ) {
    verifyVector();
    ensureRoom( index );
    vector.setElementAt( value, index );
  }

  public void put( int index, boolean value ) {
    verifyVector();
    ensureRoom( index );
    vector.setElementAt( value, index );
  }

  public void put( int index, byte ba[] ) {
    verifyVector();
    ensureRoom( index );
    vector.setElementAt( ba, index );
  }

  public void put( int index, DB db ) {
    verifyVector();
    ensureRoom( index );
    vector.setElementAt( db, index );
  }

  public int getInt( int index ) {
    verifyVector();
    return ((Integer)vector.elementAt( index )).intValue();
  }

  public float getFloat( int index ) {
    verifyVector();
    return ((Float)vector.elementAt( index )).floatValue(); 
  }

  public double getDouble( int index ) {
    verifyVector();
    return ((Double)vector.elementAt( index )).doubleValue(); 
  }

  public boolean getBoolean( int index ) {
    verifyVector();
    return ((Boolean)vector.elementAt( index )).booleanValue(); 
  }

  public String getString( int index ) {
    verifyVector();
    return (String)vector.elementAt( index );
  }

  public byte [] getByteArray( int index ) {
    verifyVector();
    return (byte[])vector.elementAt( index );
  }

  public DB getDB( int index ) {
    verifyVector();
    return (DB)vector.elementAt( index );
  }

  public boolean existsInt( int index ) {
    verifyVector();
    Object o = vector.elementAt( index );
    return o != null && o instanceof Integer;
  }

  public boolean existsFloat( int index ) {
    verifyVector();
    Object o = vector.elementAt( index );
    return o != null && o instanceof Float;
  }

  public boolean existsDouble( int index ) {
    verifyVector();
    Object o = vector.elementAt( index );
    return o != null && o instanceof Double;
  }

  public boolean existsBoolean( int index ) {
    verifyVector();
    Object o = vector.elementAt( index );
    return o != null && o instanceof Boolean;
  }

  public boolean existsString( int index ) {
    verifyVector();
    Object o = vector.elementAt( index );
    return o != null && o instanceof String;
  }

  public boolean existsByteArray( int index ) {
    verifyVector();
    Object o = vector.elementAt( index );
    return o != null && o instanceof byte[];
  }

  public boolean existsDB( int index ) {
    verifyVector();
    Object o = vector.elementAt( index );
    return o != null && o instanceof DB;
  }

  public DB newDB( int index ) {
    verifyVector();
    if (existsDB( index )) {
      throw new RuntimeException( "db already has "+index );
    }
    DB db = new DB();
    put( index, db );
    return db;
  }

  public DB newDB( String name ) {
    verifyHash();
    if (existsDB( name )) {
      throw new RuntimeException( "db already has "+name );
    }
    DB db = new DB();
    put( name, db );
    return db;
  }

  public int size() {
    if (getType()==DBHASH)
      return hashtable.size();
    else if (getType()==DBVECTOR)
      return vector.size();
    else if (getType()==DBNULL)
      return 0;
    else
      throw new RuntimeException( "Shouldn't reach here" );
  }

  private void verifyHash() {
    if (vector!=null)
      throw new RuntimeException( "Not a hash: "+this );
    if (hashtable == null)
      hashtable = new Hashtable();
  }

  private void verifyVector() {
    if (hashtable!=null)
      throw new RuntimeException( "Not a vector: "+this );
    if (vector == null)
      vector = new Vector();
  }

  private void ensureRoom( int index ) {
    if (vector.size() < index+1) {
      vector.setSize( index+1 );
    }
  }

  private int getType() {
    if (hashtable!=null)
      return DBHASH;
    else if (vector!=null)
      return DBVECTOR;
    else
      return DBNULL;
  }

  synchronized public void read( DataInputStream din ) throws IOException {
    int type = din.readInt();
    if (type==DBHASH) {
      verifyHash();
      int size = din.readInt();
      for (int i=0; i<size; ++i) {
        String key = din.readUTF();
        Object value = readValue( din );
        hashtable.put( key, value );
      }
    } else if (type==DBVECTOR) {
      verifyVector();
      int size = din.readInt();
      for (int i=0; i<size; ++i) {
        Object value = readValue( din );
        vector.addElement( value );
      }
    }
  }

  synchronized public void write( DataOutputStream dout ) throws IOException {
    if (getType()==DBHASH) {
      dout.writeInt( DBHASH );
      int size = hashtable.size();
      dout.writeInt( size );
      for (Enumeration e = hashtable.keys(); e.hasMoreElements();) {
        String key = (String)e.nextElement();
        Object value = hashtable.get( key );
        dout.writeUTF( key );
        writeValue( dout, value );
      }
    } else if (getType()==DBVECTOR) {
      dout.writeInt( DBVECTOR );
      int size = vector.size();
      dout.writeInt( size );
      for (Enumeration e = vector.elements(); e.hasMoreElements();) {
        Object value = e.nextElement();
        writeValue( dout, value );
      }
    } else if (getType()==DBNULL) {
      dout.writeInt( DBNULL );
    } else {
      throw new RuntimeException( "Shouldn't reach here" );
    }
  }

  private void writeValue( DataOutputStream dout, Object object ) throws IOException {
    if (object instanceof Integer) {
      dout.writeInt( INT );
      dout.writeInt( ((Integer)object).intValue() );
    } else if (object instanceof Float) {
      dout.writeInt( FLOAT );
      dout.writeFloat( ((Float)object).floatValue() );
    } else if (object instanceof Double) {
      dout.writeInt( DOUBLE );
      dout.writeDouble( ((Double)object).doubleValue() );
    } else if (object instanceof Boolean) {
      dout.writeInt( BOOLEAN );
      dout.writeBoolean( ((Boolean)object).booleanValue() );
    } else if (object instanceof String) {
      dout.writeInt( STRING );
      dout.writeUTF( (String)object );
    } else if (object instanceof byte[]) {
      byte ba[] = (byte[])object;
      dout.writeInt( BYTEARRAY );
      dout.writeInt( ba.length );
      dout.write( ba );
    } else if (object instanceof DB) {
      dout.writeInt( DBTYPE );
      ((DB)object).write( dout );
    } else {
      throw new IOException( "Bad type: "+object+", "+object.getClass() );
    }
  }

  private Object readValue( DataInputStream din ) throws IOException {
    int type = din.readInt();
    switch( type ) {
      case INT:
        int i = din.readInt();
        return i;
        /* break; */
      case FLOAT:
        float f = din.readFloat();
        return f;
        /* break; */
      case DOUBLE:
        double d = din.readDouble();
        return d;
        /* break; */
      case BOOLEAN:
        boolean b = din.readBoolean();
        return b;
        /* break; */
      case STRING:
        return din.readUTF();
        /* break; */
      case BYTEARRAY:
        int size = din.readInt();
        byte ba[] = new byte[size];
        din.readFully( ba );
        return ba;
        /* break; */
      case DBTYPE:
        DB db = new DB();
        db.read( din );
        return db;
        /* break; */
      default:
        throw new IOException( "Bad type: "+type );
        /* break; */
    }
  }

  public void write( OutputStream out ) throws IOException {
    write( new DataOutputStream( out ) );
  }

  public void write( File file ) throws IOException {
    write( new FileOutputStream( file ) );
  }

  public void write( String filename ) throws IOException {
    write( new FileOutputStream( new File( filename ) ) );
  }

  public void read( String filename ) throws IOException {
    read( new File( filename ) );
  }

  public void read( File file ) throws IOException {
    FileInputStream fin = new FileInputStream( file );
    read( fin );
    fin.close();
  }

  public void read( InputStream in ) throws IOException {
    read( new DataInputStream( in ) );
  }

  public byte[] toByteArray() throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    write( baos );
    return baos.toByteArray();
  }

  static public DB fromByteArray( byte raw[] ) throws IOException {
    ByteArrayInputStream bais = new ByteArrayInputStream( raw );
    DB db = new DB();
    db.read( bais );
    return db;
  }

  public void dump() {
    dump( "" );
  }

  private void dumpValue( String prefix, Object value ) {
    if (value instanceof byte[]) {
      byte ba[] = (byte[])value;
      System.out.print( " [" );
      int s = ba.length;
      if (s>3) s=3;
      for (int i=0; i<s; ++i)
        System.out.print( ba[i]+", " );
      System.out.println( "...]" );
    } else if (value instanceof DB) {
      System.out.println( "" );
      ((DB)value).dump( prefix+"  " );
    } else {
      System.out.println( " "+value );
    }
  }

  private void dump( String prefix ) {
    if (getType()==DBHASH) {
      for (Enumeration e = hashtable.keys(); e.hasMoreElements();) {
        String key = (String)e.nextElement();
        Object value = hashtable.get( key );
        System.out.print( prefix+key+" ==>" );
        dumpValue( prefix, value );
      }
    } else if (getType()==DBVECTOR) {
      for (int i=0; i<vector.size(); ++i) {
        Object value = vector.elementAt( i );
        System.out.print( prefix+i+" ==>" );
        dumpValue( prefix, value );
      }
    } else if (getType()==DBNULL) {
      System.out.println( prefix+"()" );
    }
  }
}

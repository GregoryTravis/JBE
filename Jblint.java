// $Id: Jblint.java,v 1.1 2004/10/26 19:20:23 greg Exp $

public class Jblint
{
  static private final int Fo = 512;
  static private final int Nz = 13;
  static private final int WSINCLEN = (2*Nz*Fo+1); // must be odd
  static private double wsinc_[] = new double[WSINCLEN+2];
  //static private float wsinc[];
  //static private float dwsinc_[] = new float[WSINCLEN+2];
  //static private float dwsinc[];

/*
#define HAMMING(t) (0.54 - 0.46 * cos( (t) ))

// plus 2 for padding
fix wsinc_[WSINCLEN+2];
fix *wsinc;
fix dwsinc_[WSINCLEN+2];
fix *dwsinc;
*/

  static {
    System.out.print( "Initting Jblint...." );
    gen_wsinc();
    System.out.println( "done" );
  }

  static private void gen_wsinc() {

    for (int i=0; i<WSINCLEN; ++i) {
      int ii=i-(WSINCLEN/2);
      double arg = ((double)ii) * (Math.PI/Fo);
      double val = (arg==0.0) ? 1.0 : (Math.sin(arg)/arg);
      wsinc_[i+1] = val*(0.54 - 0.46*Math.cos((i*Math.PI)/(Nz*Fo)));
    }

/*
    // padding
         wsinc_[0] = wsinc_[WSINCLEN+1] = 0;

    // deltas
         dwsinc = &dwsinc_[1];
    for (i=0; i<WSINCLEN+2-1; ++i)
      dwsinc_[i] = wsinc_[i+1] - wsinc_[i];
*/
  }

  static public Sound jblint( Sound sound, int newlen ) {
    short raw[] = sound.raw();

    Sound nsound = new Sound( newlen );
    short nraw[] = nsound.raw();

    int len = raw.length;
    int nlen = nraw.length;
   
    boolean shorter = nlen < len;

    // half-width of window is
         double Ww = shorter ? ((((double)Nz)*((double)len))/((double)nlen))
         : (double)Nz;

    // scale factor for sinc
      double ws_scale = shorter ? ((double)nlen/(double)len) : 1.0;

    // output sample at n maps to input at
         double t = 0;
    double dt = (double)len/(double)nlen;

    double c0 = (double)Fo*(Nz/Ww);
    double c1 = Fo*Nz;
    double dwsincarg = (Fo*Nz)/Ww;

    for (int nn=0; nn<nlen; ++nn) {
      // output sample at n maps to input at
           //t = ((double)nn*(double)len)/(double)nlen;

      int startn = (int)Math.ceil( t-Ww );
      int endn = (int)Math.floor( t+Ww );

      double acc = 0;

      if (startn < 0)
        startn = 0;
      if (endn>=len)
        endn = len-1;

      double wsincarg = ((double)startn-t)*c0 + c1;

      for (int n=startn; n<=endn; ++n) {

        if (!(n>=0 && n<len))
          throw new RuntimeException( "Shouldn't reach here" );

        short samp = raw[n];
        int wsincarg_int = (int)wsincarg;
        double wsincarg_frac = wsincarg - wsincarg_int;
        double wsinca = wsinc_[wsincarg_int+1];
        double dws = wsinc_[wsincarg_int+2] - wsinca;
        double ws = wsinca + (wsincarg_frac * dws);
        acc += samp*ws;

        wsincarg += dwsincarg;
      }

      if (!(nn>=0 && nn<nlen))
        throw new RuntimeException( "Shouldn't reach here" );

      nraw[nn] = (short)(acc * ws_scale);

      t += dt;
    }

    return nsound;
  }
}

// $Id: Nblint.c,v 1.1 2004/10/26 19:20:23 greg Exp $

#include <math.h>
#include <malloc.h>
#include <stdio.h>
#include "NBlint.h"
#include "a.h"
#include "fix.h"

static int initted = 0;

void gen_wsinc( void );

#define Fo (512)
#define Nz (13)
// must be odd
#define WSINCLEN (2*Nz*Fo+1)

#define HAMMING(t) (0.54 - 0.46 * cos( (t) ))

// plus 2 for padding
fix wsinc_[WSINCLEN+2];
fix *wsinc;
fix dwsinc_[WSINCLEN+2];
fix *dwsinc;

void init()
{
  if (initted)
    return;

  gen_wsinc();

  initted = 1;
}

void gen_wsinc( void )
{
  int i;

  wsinc = &wsinc_[1];

  for (i=0; i<WSINCLEN; ++i) {
    int ii=i-(WSINCLEN/2);
    double arg = ((double)ii) * (PI/Fo);
    double val = (arg==0.0) ? 1.0 : (sin(arg)/arg);
    wsinc[i] = float_fix(val*HAMMING((i*PI)/(Nz*Fo)));
  }

  // padding
  wsinc_[0] = wsinc_[WSINCLEN+1] = float_fix(0.0);

  // deltas
  dwsinc = &dwsinc_[1];
  for (i=0; i<WSINCLEN+2-1; ++i)
    dwsinc_[i] = fix_sub(wsinc_[i+1],wsinc_[i]);
}

JNIEXPORT void JNICALL Java_Nblint_native_1nblint
  (JNIEnv *env, jclass clas, jshortArray jnraw, jshortArray jraw)
{
  jboolean iscopy = JNI_FALSE;
  jshort *raw, *nraw;
  jsize len, nlen;
  int n, nn;
  double Fc;
  double Ww;
  int shorter;
  double ws_scale;
  fix ws_scale_fx;
  double t, dt;
  double c0;
  fix c1;
  fix dwsincarg;

  init();

  nraw = (*env)->GetShortArrayElements( env, jnraw, &iscopy );
  raw = (*env)->GetShortArrayElements( env, jraw, &iscopy );

  nlen = (*env)->GetArrayLength( env, jnraw );
  len = (*env)->GetArrayLength( env, jraw );

  shorter = nlen < len;

  // half-width of window is
  Ww = shorter ? ((((double)Nz)*((double)len))/((double)nlen))
               : (double)Nz;

  // scale factor for sinc
  ws_scale = shorter ? ((double)nlen/(double)len) : 1.0;
  ws_scale_fx = float_fix(ws_scale);

  // output sample at n maps to input at
  t = 0;
  dt = (double)len/(double)nlen;

  c0 = (double)Fo*(Nz/Ww);
  c1 = int_fix(Fo*Nz);
  dwsincarg = float_fix((Fo*Nz)/Ww);

  for (nn=0; nn<nlen; ++nn) {
    int startn, endn;
    fix acc;
    int n;
    fix wsincarg;

    // output sample at n maps to input at
    //t = ((double)nn*(double)len)/(double)nlen;

    startn = ceil( t-Ww );
    endn = floor( t+Ww );

    acc = 0;

    if (startn < 0)
      startn = 0;
    if (endn>=len)
      endn = len-1;

    wsincarg = float_fix(((double)startn-t)*c0);
    wsincarg = fix_add(wsincarg,c1);

    for (n=startn; n<=endn; ++n) {
      fix samp;
      int wsincarg_int;
      fix wsincarg_frac;
      fix wsinca, dws, ws;

      A(n>=0 && n<len);

      samp = raw[n];
      wsincarg_int = fix_int(wsincarg);
      wsincarg_frac = fix_frac(wsincarg);
      wsinca = wsinc[wsincarg_int];
      dws = fix_sub(wsinc[wsincarg_int+1],wsinca);
      ws = fix_add(wsinca, fix_mul(wsincarg_frac,dws));
      acc = fix_add(acc, fix_mul(samp,ws));

      wsincarg = fix_add(wsincarg,dwsincarg);
    }

    A(nn>=0 && nn<nlen);
    nraw[nn] = fix_mul(acc,ws_scale_fx);

    t += dt;
  }

  (*env)->ReleaseShortArrayElements( env, jnraw, nraw, 0 );
  (*env)->ReleaseShortArrayElements( env, jraw, raw, 0 );

  //printf( "OK\n" );

  fflush( stdout );
}

JNIEXPORT void JNICALL Java_Nblint_native_1nshint
  (JNIEnv *env, jclass claz, jshortArray jnraw, jshortArray jraw)
{
  jboolean iscopy = JNI_FALSE;
  jshort *raw, *nraw;
  jsize len, nlen;
  double t, dt;
  int tp;

  nraw = (*env)->GetShortArrayElements( env, jnraw, &iscopy );
  raw = (*env)->GetShortArrayElements( env, jraw, &iscopy );

  //printf( "got %x %x\n", nraw, raw );

  nlen = (*env)->GetArrayLength( env, jnraw );
  len = (*env)->GetArrayLength( env, jraw );

  t = 0;
  dt = (double)len / (double)nlen;

  for (tp=0; tp<nlen; ++tp) {
    int ti = (int)t;

//    if (ti>=len)
//      break;
A(ti<len && ti>=0);

    nraw[tp] = raw[ti];

    t += dt;
  }

  (*env)->ReleaseShortArrayElements( env, jnraw, nraw, 0 );
  (*env)->ReleaseShortArrayElements( env, jraw, raw, 0 );

  //printf( "OK\n" );

  fflush( stdout );
}

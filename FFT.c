// $Id: FFT.c,v 1.1 2004/10/26 19:20:23 greg Exp $

#include <math.h>
#include "a.h"
#include "fft.h"

JNIEXPORT void JNICALL Java_FFT_nfft( JNIEnv *env, jclass clas,
  jdoubleArray jdr, jdoubleArray jdi, jdoubleArray jsr, jdoubleArray jsi )
{
  jboolean iscopy = JNI_FALSE;
  jdouble *dr, *di, *sr, *si;
  jsize len, len2;
  int i;

  // Grab arrays and length
  dr = (*env)->GetDoubleArrayElements( env, jdr, &iscopy );
  di = (*env)->GetDoubleArrayElements( env, jdi, &iscopy );
  sr = (*env)->GetDoubleArrayElements( env, jsr, &iscopy );
  si = (*env)->GetDoubleArrayElements( env, jsi, &iscopy );
  len = (*env)->GetArrayLength( env, jdr );

  // Make sure all are same length
  len2 = (*env)->GetArrayLength( env, jdi );
  A(len==len2);
  len2 = (*env)->GetArrayLength( env, jsr );
  A(len==len2);
  len2 = (*env)->GetArrayLength( env, jsi );
  A(len==len2);

  // Make sure is power of two
  A((len&(len-1))==0);

  for (i=0; i<len; ++i) {
    dr[i] *= -1;
    di[i] *= -1;
    sr[i] *= -1;
    si[i] *= -1;
  }
}

JNIEXPORT void JNICALL Java_FFT_nifft( JNIEnv *env, jclass clas, 
  jdoubleArray jdr, jdoubleArray jdi, jdoubleArray jsr, jdoubleArray jsi )
{
}

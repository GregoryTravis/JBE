// $Id: fix.h,v 1.1 2004/10/26 19:20:23 greg Exp $

// DIV IS WRONG

#define USE_FIX 0

#define fix_print(f) printf( "%f", fix_float(f) )
#define fix_sq(f) (fix_mul((f),(f)))

#if USE_FIX

typedef int fix;

#define FIX_FRAC (15)
#define FIX_INT (32-(FIX_FRAC))
#define int_fix(i) (((fix)(i))<<FIX_FRAC)
#define float_fix(f) ((fix)((f)*(((long)1)<<FIX_FRAC)))
#define fix_add(a,b) ((a)+(b))
#define fix_sub(a,b) ((a)-(b))
#define fix_float(f) (((float)(f))/(((long)1)<<FIX_FRAC))
#define fix_int(f) ((f)>>FIX_FRAC)
#define fix_ceil(f) fix_floor(((f)+((1<<FIX_FRAC)-1)))
#define fix_floor(f) ((f)&(-1<<FIX_FRAC))
#define fix_frac(f) ((f)&((1<<FIX_FRAC)-1))

#define fix_mul3(prod,m0,m1) asm \
  {\
    asm mov eax, m0\
    asm mov edx, m1\
    asm imul edx\
    asm shr eax, FIX_FRAC\
    asm shl edx, FIX_INT\
    asm or eax, edx\
    asm mov prod, eax\
  }

fix fix_mul( fix a, fix b )
{
  fix prod;
  asm {
    mov eax, a
    mov edx, b
    imul edx
    shr eax, FIX_FRAC
    shl edx, FIX_INT
    or eax, edx
    mov prod, eax
  }
  return prod;
}

#define FIX_ZERO_EPSILON float_fix( 0.0001 )

// THIS IS WRONG
fix fix_div( fix a, fix b )
{
  fix quot;
  if (((b+6)&0xfffffff0)==0) return;
  asm {
    mov eax, a
    mov edx, eax
    sar edx, 16
    shl eax, 16
    mov ecx, b
    idiv ecx
    mov quot, eax
  }
  return quot;
}

fix fix_sqrt( fix f )
{
  return float_fix( sqrt( fix_float(f) ) );
}

fix fix_len2( fix a, fix b )
{
  fix len = fix_sqrt(
    fix_add( fix_sq(a), fix_sq(b) ) );
  return len;
}

fix fix_len3( fix a, fix b, fix c )
{
  fix len = fix_sqrt(
    fix_add( fix_sq(a), fix_add( fix_sq(b), fix_sq(c) ) ) );
  return len;
}

#else

typedef float fix;

#define int_fix(i) ((float)(i))
#define float_fix(f) (f)
#define fix_add(a,b) ((a)+(b))
#define fix_sub(a,b) ((a)-(b))
#define fix_float(f) (f)
#define fix_int(f) ((int)f)
#define fix_mul(a,b) ((a)*(b))
#define fix_div(a,b) ((a)/(b))
#define fix_len3(a,b,c) (sqrt( (a)*(a)+(b)*(b)+(c)*(c) ))
#define fix_ceil(f) (ceil(f))
#define fix_floor(f) (floor(f))
#define fix_frac(f) ((f)-fix_int(f))

#endif

#define decl_fix_wrap(fun)                 \
fix fix_##fun( fix f )                     \
{                                          \
  return float_fix( fun( fix_float( f ) ) ); \
}
decl_fix_wrap(cos);
decl_fix_wrap(sin);

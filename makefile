all: Nblint.dll FFT.dll # fixtest.exe ntest.exe

clean:
	del Nblint.dll fft.dll *.obj ntest.exe

JAVADIR = j:\j2sdk1.4.0
JAVAINCLUDES = -I$(JAVADIR)\include -I$(JAVADIR)\include\win32

DEBUG = 0

!IF $(DEBUG)
CFLAGS = -mn -C -WD -S -3 -a8 -c -g
!ELSE
CFLAGS = -mn -C -WD -S -3 -a8 -c -o
!ENDIF

Nblint.dll: Nblint.obj
	LINK /CO /NOI /DO /DE /XN /NT /ENTRY:__DllMainCRTStartup@12 /VERS:1.0 /BAS:268435456 /A:512 /IMPL:Nblint.LIB @Nblint.LNK;

Nblint.obj: Nblint.c Nblint.h a.h fix.h
	SC $(CFLAGS) $(JAVAINCLUDES)  -oNblint.obj Nblint.c

FFT.dll: FFT.obj
	LINK /CO /NOI /DO /DE /XN /NT /ENTRY:__DllMainCRTStartup@12 /VERS:1.0 /BAS:268435456 /A:512 /IMPL:FFT.LIB @FFT.LNK;

FFT.obj: FFT.c FFT.h a.h
	SC $(CFLAGS) $(JAVAINCLUDES)  -oFFT.obj FFT.c

ntest.obj: ntest.c fix.h
	SC -mn -C -WA -S -3 -a8 -c -g -D_CONSOLE=1  -ontest.obj ntest.c

ntest.exe: ntest.obj fix.h
	LINK /CO /NOI /DE /XN /NT /ENTRY:_mainCRTStartup /VERS:1.0 /BAS:4194304 /A:512 @ntest.LNK;

fixtest.obj: fixtest.c fix.h
	SC -mn -C -WA -S -3 -a8 -c -g -D_CONSOLE=1  -ofixtest.obj fixtest.c

fixtest.exe: fixtest.obj fix.h
	LINK /CO /NOI /DE /XN /NT /ENTRY:_mainCRTStartup /VERS:1.0 /BAS:4194304 /A:512 @fixtest.LNK;

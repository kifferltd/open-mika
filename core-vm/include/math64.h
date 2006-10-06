/*******************************************************************
** m a t h 6 4 . h
** Forth Inspired Command Language - 64 bit math support routines
** Author: John Sadler (john_sadler@alum.mit.edu)
** Created: 25 January 1998
** 
*******************************************************************/
/*
** N O T I C E -- DISCLAIMER OF WARRANTY
** 
** Ficl is freeware. Use it in any way that you like, with
** the understanding that the code is not supported.
** 
** Any third party may reproduce, distribute, or modify the ficl
** software code or any derivative  works thereof without any 
** compensation or license, provided that the author information
** and this disclaimer text are retained in the source code files.
** The ficl software code is provided on an "as is"  basis without
** warranty of any kind, including, without limitation, the implied
** warranties of merchantability and fitness for a particular purpose
** and their equivalents under the laws of any jurisdiction.  
** 
** I am interested in hearing from anyone who uses ficl. If you have
** a problem, a success story, a defect, an enhancement request, or
** if you would like to contribute to the ficl release (yay!), please
** send me email at the address above. 
**
** NOTE: this file depends on sysdep.h for the definition
** of PORTABLE_LONGMULDIV and several abstract types.
**
*/

#if !defined (__MATH64_H__)
#define __MATH64_H__

#ifdef __cplusplus
extern "C" {
#endif

/*
** [CG] make DPINT/DPUNS returns in &parameters
*/
void    m64Abs(DPINT x, DPINT *result);
int     m64IsNegative(DPINT x);
void    m64Mac(DPUNS u, FICL_UNS mul, FICL_UNS add, DPUNS *result);
void    m64MulI(FICL_INT x, FICL_INT y, DPINT *result);
void    m64Negate(DPINT *x);
void    m64FlooredDivI(DPINT num, FICL_INT den, INTQR *result);
void    i64Push(FICL_STACK *pStack, DPINT i64);
void    i64Pop(FICL_STACK *pStack, DPINT *result);
void    u64Push(FICL_STACK *pStack, DPUNS u64);
void    u64Pop(FICL_STACK *pStack, DPUNS *result);
void    m64SymmetricDivI(DPINT num, FICL_INT den, INTQR *result);
w_word  m64UMod(DPUNS *pUD, w_word base);


#if PORTABLE_LONGMULDIV != 0   /* see sysdep.h */
void    m64Add(DPUNS x, DPUNS y, DPUNS *result);
void    m64ASL( DPUNS x , DPUNS *result);
void    m64ASR( DPUNS x , DPUNS *result);
int     m64Compare(DPUNS x, DPUNS y);
void    m64Or( DPUNS x, DPUNS y , DPUNS *result);
void    m64Sub(DPUNS x, DPUNS y, DPUNS *result);
#endif

#define i64Extend(i64) (i64).hi = ((i64).lo < 0) ? -1L : 0 
#define m64CastIU(i64) (*(DPUNS *)(&(i64)))
#define m64CastUI(u64) (*(DPINT *)(&(u64)))
#define m64CastQRIU(iqr) (*(UNSQR *)(&(iqr)))
#define m64CastQRUI(uqr) (*(INTQR *)(&(uqr)))

#define CELL_HI_BIT (1L << (BITS_PER_CELL-1))

#ifdef __cplusplus
}
#endif

#endif


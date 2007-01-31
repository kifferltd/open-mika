###########################################################################
# Copyright  (c) 2001 by Acunia N.V. All rights reserved.                 #
#                                                                         #
# This software is copyrighted by and is the sole property of Acunia N.V. #
# and its licensors, if any. All rights, title, ownership, or other       #
# interests in the software remain the property of Acunia N.V. and its    #
# licensors, if any.                                                      #
#                                                                         #
# This software may only be used in accordance with the corresponding     #
# license agreement. Any unauthorized use, duplication, transmission,     #
#  distribution or disclosure of this software is expressly forbidden.    #
#                                                                         #
# This Copyright notice may not be removed or modified without prior      #
# written consent of Acunia N.V.                                          #
#                                                                         #
# Acunia N.V. reserves the right to modify this software without notice.  #
#                                                                         #
#   Acunia N.V.                                                           #
#   Vanden Tymplestraat 35      info@acunia.com                           #
#   3000 Leuven                 http://www.acunia.com                     #
#   Belgium - EUROPE                                                      #
###########################################################################


#
# $Id: Makefile,v 1.1.1.1 2004/07/12 14:07:45 cvs Exp $
#
# Makefile for the Wonka documentation
#

JUNK=  *~ *.log *.aux *~ *.bbl *.blg *.toc *.lot *.lof *.pdf *.eps *.out *.idx *.jar 

AWK=awk
PDFLATEX=pdflatex
LATEX=latex
DVIPS=dvips
RM=rm -f
LPR=lpr
TGIF=tgif
GV=gv
GS=gs
XDVI=xdvi


#OBJS = dev-guid/Wonka_DEV-GUID.tex \
#       ref-man/Wonka_REF-MAN.tex \
#       whitepaper/Wonka_WHIP.tex \
#       whitepaper-pos/Wonka-POS_WHIP.tex
#
# Internal stuff...
#


all: arch-spec dev-guid ref-man whitepaper whitepaper-pos
	make -C quick-start ;  cp quick-start/Wonka_QUICK.pdf . ; make -C quick-start clean

#	make -C arch-spec ;  cp arch-spec/Wonka_ARCH-SPEC.pdf . ; make -C arch-spec clean
#	make -C dev-guid ;  cp dev-guid/Wonka_DEV-GUID.pdf . ; make -C dev-guid clean
#	make -C features ;  cp features/Wonka_Features.pdf . ; make -C features clean
#	make -C ref-man ;  cp ref-man/Wonka_REF-MAN.pdf . ; make -C ref-man clean
#	make -C whitepaper ;  cp whitepaper/Wonka_WHIP.pdf . ; make -C whitepaper clean
#	make -C whitepaper-pos ;  cp whitepaper-pos/Wonka-POS_WHIP.pdf . ; make  -C whitepaper-pos clean

clean:
	$(RM) -Rf $(JUNK)



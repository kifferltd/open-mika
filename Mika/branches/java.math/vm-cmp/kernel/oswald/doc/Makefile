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
# $Id: Makefile,v 1.1.1.1 2004/07/12 14:07:44 cvs Exp $
#
# Makefile for the Oswald documentation
#

TEX= Oswald_REF-SPEC.tex body.tex atomic.tex booting.tex thread.tex event.tex pcb.tex timer.tex sem.tex mutex.tex mailbox.tex queue.tex signals.tex monitor.tex block.tex monitor.tex map.tex modules.tex
DRAWING=init_stack.obj elf.obj eventlists.obj interrupt_switch.obj symbols.obj thread_switch.obj
JUNK= *~ *.dvi *.log *.aux oswald.ps *.pdf *.eps cvs-info.tex *.out *.lot *.toc *.lof *.tex.bak *.bbl *.blg $(TARGET).pdf
TARGET=Oswald_REF-SPEC

#
# The commands needed in this Makefile
#

AWK=awk
LATEX=latex
DVIPS=dvips
RM=rm -f
LPR=lpr
TGIF=tgif
PDFLATEX=pdflatex
PDFVIEWER=acroread
EPSTOPDF=epstopdf

#
# Internal stuff...
#

EPS=$(DRAWING:.obj=.eps)
PDFGRAPHICS=$(EPS:.eps=.pdf)

all: $(TARGET).pdf

cvs-info.tex: $(TEX)
	$(AWK) -f cvs-tex.awk $(TEX) > cvs-info.tex

#wonka.dvi: $(TEX) cvs-info.tex $(EPS) wonka.log
#       @while awk -f query-rerun.awk wonka.log; \
#         do $(LATEX) wonka.tex; \
#       done; \

$(TARGET).pdf: $(TEX) $(PDFGRAPHICS) $(TARGET).log 
	pdflatex $(TARGET)
	bibtex $(TARGET)
	pdflatex $(TARGET)
	pdflatex $(TARGET)

$(TARGET).log: $(TEX) cvs-info.tex $(EPS)
	pdflatex $(TARGET).tex

#$(TARGET).pdf: $(TARGET).dvi
#	$(DVIPS) -o $(TARGET).ps $(TARGET).dvi

ispell:
	ispell -t -x -C -p dictionary *.tex

clean:
	@$(RM) $(JUNK)
	@find . -name ".#*" | xargs $(RM)

print: $(TARGET).ps
	$(LPR) $(TARGET).ps

view: $(TARGET).pdf
	$(PDFVIEWER) $(TARGET).pdf &
#
# How to go from a .obj file to a .epsi file to .pdf
# (pdf required to include with pdflatex)

$(EPS): %.eps: %.obj
	$(TGIF) -print -eps $<

$(PDFGRAPHICS):$(EPS)
	$(EPSTOPDF) elf.eps
	$(EPSTOPDF) eventlists.eps      
	$(EPSTOPDF) init_stack.eps
	$(EPSTOPDF) interrupt_switch.eps
	$(EPSTOPDF) symbols.eps
	$(EPSTOPDF) thread_switch.eps

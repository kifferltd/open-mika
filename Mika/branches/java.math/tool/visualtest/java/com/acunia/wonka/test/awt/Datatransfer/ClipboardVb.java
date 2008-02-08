/**************************************************************************
* Copyright  (c) 2001 by Acunia N.V. All rights reserved.                 *
*                                                                         *
* This software is copyrighted by and is the sole property of Acunia N.V. *
* and its licensors, if any. All rights, title, ownership, or other       *
* interests in the software remain the property of Acunia N.V. and its    *
* licensors, if any.                                                      *
*                                                                         *
* This software may only be used in accordance with the corresponding     *
* license agreement. Any unauthorized use, duplication, transmission,     *
*  distribution or disclosure of this software is expressly forbidden.    *
*                                                                         *
* This Copyright notice may not be removed or modified without prior      *
* written consent of Acunia N.V.                                          *
*                                                                         *
* Acunia N.V. reserves the right to modify this software without notice.  *
*                                                                         *
*   Acunia N.V.                                                           *
*   Vanden Tymplestraat 35      info@acunia.com                           *
*   3000 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
**************************************************************************/
package com.acunia.wonka.test.awt.Datatransfer;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.awt.datatransfer.*;

public class ClipboardVb extends Panel {
	private Panel knoppenPanel;
	TextArea tekstVeld;
	private Button cut;
	private Button copy;
	private Button paste;
	private Button getName;
	private Button getContents;
	private Button addText;
	private MyButtonListener buttonListener;
	TextArea tekstScherm;

	
	ClipboardVb(TextArea tekst){
		super(new BorderLayout());
		tekstScherm = tekst;
		buttonListener = new MyButtonListener();
		tekstVeld = new TextArea();
		tekstVeld.setEditable(true);
		tekstScherm.setEditable(false);
		System.out.println("tekstVeld.isEditable() = "+tekstVeld.isEditable());
		System.out.println("tekstScherm.isEditable() = "+tekstScherm.isEditable());

		knoppenPanel = new Panel(new GridLayout(2,3));
		
		cut = new Button("cut");
		cut.addActionListener(buttonListener);
		cut.setActionCommand("cut");
		knoppenPanel.add(cut);

		copy = new Button("copy");
		copy.addActionListener(buttonListener);
		copy.setActionCommand("copy");
		knoppenPanel.add(copy);

		paste = new Button("paste");
		paste.addActionListener(buttonListener);
		paste.setActionCommand("paste");
		knoppenPanel.add(paste);

		getName = new Button("getName");
		getName.addActionListener(buttonListener);
		getName.setActionCommand("getName");
		knoppenPanel.add(getName);

		getContents = new Button("getContents");
		getContents.addActionListener(buttonListener);
		getContents.setActionCommand("getContents");
		knoppenPanel.add(getContents);

		addText = new Button("add text");
		addText.addActionListener(buttonListener);
		addText.setActionCommand("addText");
		knoppenPanel.add(addText);

		add("Center",tekstVeld);
		add("South",knoppenPanel);

	}
	public class MyButtonListener implements ActionListener, ClipboardOwner {
		public void actionPerformed(ActionEvent e) {
			try{
				if (e.getActionCommand().equals("cut")) {
					String geselecteerd = tekstVeld.getSelectedText();
					getToolkit().getSystemClipboard().setContents(new StringSelection(geselecteerd),this);
					tekstVeld.replaceRange("",tekstVeld.getSelectionStart(),tekstVeld.getSelectionEnd());
					System.out.println("cut: "+geselecteerd);
					tekstScherm.append('\n'+"cut: "+geselecteerd);
				}
				else if (e.getActionCommand().equals("copy")) {
					String geselecteerd = tekstVeld.getSelectedText();
					getToolkit().getSystemClipboard().setContents(new StringSelection(geselecteerd),this);
					System.out.println("copy: "+geselecteerd);
					tekstScherm.append('\n'+"copy: "+geselecteerd);
				}
				else if (e.getActionCommand().equals("paste")) {
					Transferable tekst = getToolkit().getSystemClipboard().getContents(this);
					try{
						if(tekst!=null && tekst.isDataFlavorSupported(DataFlavor.stringFlavor)){
							System.out.println("tekstVeld.getSelectionStart(): "+tekstVeld.getSelectionStart());
							System.out.println("tekstVeld.getSelectionEnd(): "+tekstVeld.getSelectionEnd());
							tekstVeld.replaceRange((""+tekst.getTransferData(DataFlavor.stringFlavor)),tekstVeld.getSelectionStart(),tekstVeld.getSelectionEnd());
						}
					}catch(Exception exc){System.out.println("exc="+exc.getMessage());}
					System.out.println("paste: "+tekst.getTransferData(DataFlavor.stringFlavor));
					tekstScherm.append('\n'+"paste: "+tekst.getTransferData(DataFlavor.stringFlavor));
				}
				else if (e.getActionCommand().equals("getContents")) {
					System.out.println("getToolkit().getSystemClipboard().getContents() = "+getToolkit().getSystemClipboard().getContents(this));
					tekstScherm.append('\n'+"getToolkit().getSystemClipboard().getContents() = "+getToolkit().getSystemClipboard().getContents(this));
				}
				else if (e.getActionCommand().equals("getName")) {
					System.out.println("getToolkit().getSystemClipboard().getName() = "+getToolkit().getSystemClipboard().getName());
					tekstScherm.append('\n'+"getToolkit().getSystemClipboard().getName() = "+getToolkit().getSystemClipboard().getName());
				}
				else if (e.getActionCommand().equals("addText")) {
					tekstVeld.append("Some text so you`re able to edit");
					System.out.println("addText = Some text so you`re able to edit");
					tekstScherm.append('\n'+"addText = Some text so you`re able to edit");
				}
			}catch(Exception ex){System.out.println("ex="+ex.getMessage());}
		}
		public void lostOwnership(Clipboard clipb, Transferable transf){
			System.out.println("geen eigenaar meer van het clipboard");
			tekstScherm.append('\n'+"geen eigenaar meer van het clipboard");
		}
	}
}

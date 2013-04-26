package br.ufrgs.ppgc.gia.jhekaton.view;

import java.awt.Dimension;

import javax.swing.JLabel;

public class StatusBar extends JLabel {

	private static final long serialVersionUID = -7875199351090907465L;

	StatusBar(String msg) {
		super();
		super.setPreferredSize(new Dimension(100, 16));
		this.setMessage(msg);
	}

	public void setMessage(String messagem) {
		this.setText(" " + messagem);
	}

	
}

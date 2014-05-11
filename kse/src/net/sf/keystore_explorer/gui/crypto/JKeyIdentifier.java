/*
 * Copyright 2004 - 2013 Wayne Grant
 *           2013 - 2014 Kai Kramer
 *
 * This file is part of KeyStore Explorer.
 *
 * KeyStore Explorer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * KeyStore Explorer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with KeyStore Explorer.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.sf.keystore_explorer.gui.crypto;

import static java.awt.Dialog.ModalityType.APPLICATION_MODAL;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.PublicKey;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.sf.keystore_explorer.crypto.CryptoException;
import net.sf.keystore_explorer.gui.CursorUtil;
import net.sf.keystore_explorer.gui.error.DError;
import net.sf.keystore_explorer.utilities.io.HexUtil;

/**
 * Component to edit a key identifier.
 * 
 */
public class JKeyIdentifier extends JPanel {
	private static ResourceBundle res = ResourceBundle.getBundle("net/sf/keystore_explorer/gui/crypto/resources");

	private JTextField jtfKeyIdentifier;
	private JButton jbEditKeyIdentifier;
	private JButton jbClearKeyIdentifier;

	private String title;
	private PublicKey publicKey;
	private byte[] keyIdentifier;

	/**
	 * Construct a JKeyIdentifier.
	 * 
	 * @param title
	 *            Title of edit dialog
	 * @param publicKey
	 *            Public key
	 */
	public JKeyIdentifier(String title, PublicKey publicKey) {
		this(title, publicKey, null);
	}

	/**
	 * Construct a JKeyIdentifier.
	 * 
	 * @param title
	 *            Title of edit dialog
	 * @param publicKey
	 *            Public key
	 * @param keyIdentifier
	 *            Key identifier
	 */
	public JKeyIdentifier(String title, PublicKey publicKey, byte[] keyIdentifier) {
		this.title = title;
		this.publicKey = publicKey;
		this.keyIdentifier = keyIdentifier;
		initComponents();
	}

	private void initComponents() {
		jtfKeyIdentifier = new JTextField(40);
		jtfKeyIdentifier.setEditable(false);

		GridBagConstraints gbc_jtfKeyIdentifier = new GridBagConstraints();
		gbc_jtfKeyIdentifier.gridwidth = 1;
		gbc_jtfKeyIdentifier.gridheight = 1;
		gbc_jtfKeyIdentifier.gridx = 0;
		gbc_jtfKeyIdentifier.gridy = 0;
		gbc_jtfKeyIdentifier.insets = new Insets(0, 0, 0, 5);

		ImageIcon editIcon = new ImageIcon(getClass().getResource(
				res.getString("JKeyIdentifier.jbEditKeyIdentifier.image")));
		jbEditKeyIdentifier = new JButton(editIcon);
		jbEditKeyIdentifier.setToolTipText(res.getString("JKeyIdentifier.jbEditKeyIdentifier.tooltip"));
		jbEditKeyIdentifier.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				try {
					CursorUtil.setCursorBusy(JKeyIdentifier.this);
					editKeyIdentifier();
				} finally {
					CursorUtil.setCursorFree(JKeyIdentifier.this);
				}
			}
		});

		GridBagConstraints gbc_jbEditKeyIdentifier = new GridBagConstraints();
		gbc_jbEditKeyIdentifier.gridwidth = 1;
		gbc_jbEditKeyIdentifier.gridheight = 1;
		gbc_jbEditKeyIdentifier.gridx = 1;
		gbc_jbEditKeyIdentifier.gridy = 0;
		gbc_jbEditKeyIdentifier.insets = new Insets(0, 0, 0, 5);

		ImageIcon clearIcon = new ImageIcon(getClass().getResource(
				res.getString("JKeyIdentifier.jbClearKeyIdentifier.image")));
		jbClearKeyIdentifier = new JButton(clearIcon);
		jbClearKeyIdentifier.setToolTipText(res.getString("JKeyIdentifier.jbClearKeyIdentifier.tooltip"));
		jbClearKeyIdentifier.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				try {
					CursorUtil.setCursorBusy(JKeyIdentifier.this);
					clearKeyIdentifier();
				} finally {
					CursorUtil.setCursorFree(JKeyIdentifier.this);
				}
			}
		});

		GridBagConstraints gbc_jbClearKeyIdentifier = new GridBagConstraints();
		gbc_jbClearKeyIdentifier.gridwidth = 1;
		gbc_jbClearKeyIdentifier.gridheight = 1;
		gbc_jbClearKeyIdentifier.gridx = 2;
		gbc_jbClearKeyIdentifier.gridy = 0;
		gbc_jbClearKeyIdentifier.insets = new Insets(0, 0, 0, 0);

		setLayout(new GridBagLayout());
		add(jtfKeyIdentifier, gbc_jtfKeyIdentifier);
		add(jbEditKeyIdentifier, gbc_jbEditKeyIdentifier);
		add(jbClearKeyIdentifier, gbc_jbClearKeyIdentifier);

		populate();
	}

	/**
	 * Get key identifier.
	 * 
	 * @return Key identifier, or null if none chosen
	 */
	public byte[] getKeyIdentifier() {
		return keyIdentifier;
	}

	/**
	 * Set key identifer.
	 * 
	 * @param keyIdentifier
	 *            Key identifier
	 */
	public void setKeyIdentifier(byte[] keyIdentifier) {
		this.keyIdentifier = keyIdentifier;
		populate();
	}

	/**
	 * Sets whether or not the component is enabled.
	 * 
	 * @param enabled
	 *            True if this component should be enabled, false otherwise
	 */
	public void setEnabled(boolean enabled) {
		jbEditKeyIdentifier.setEnabled(enabled);
		jbClearKeyIdentifier.setEnabled(enabled);
	}

	/**
	 * Set component's tooltip text.
	 * 
	 * @param toolTipText
	 *            Tooltip text
	 */
	public void setToolTipText(String toolTipText) {
		super.setToolTipText(toolTipText);
		jtfKeyIdentifier.setToolTipText(toolTipText);
	}

	private void populate() {
		if (keyIdentifier != null) {
			jtfKeyIdentifier.setText(HexUtil.getHexString(keyIdentifier));
			jbClearKeyIdentifier.setEnabled(true);
		} else {
			jtfKeyIdentifier.setText("");
			jbClearKeyIdentifier.setEnabled(false);
		}

		jtfKeyIdentifier.setCaretPosition(0);
	}

	private void editKeyIdentifier() {
		Container container = getTopLevelAncestor();

		try {
			DKeyIdentifierChooser dKeyIdentifierChooser = null;

			if (container instanceof JDialog) {
				dKeyIdentifierChooser = new DKeyIdentifierChooser((JDialog) container, title, publicKey, keyIdentifier);
				dKeyIdentifierChooser.setLocationRelativeTo(container);
				dKeyIdentifierChooser.setVisible(true);
			} else if (container instanceof JFrame) {
				dKeyIdentifierChooser = new DKeyIdentifierChooser((JFrame) container, title, publicKey, keyIdentifier);
				dKeyIdentifierChooser.setLocationRelativeTo(container);
				dKeyIdentifierChooser.setVisible(true);
			}

			byte[] newKeyIdentifier = dKeyIdentifierChooser.getKeyIdentifier();

			if (newKeyIdentifier == null) {
				return;
			}

			setKeyIdentifier(newKeyIdentifier);
		} catch (CryptoException ex) {
			DError dError = null;

			if (container instanceof JDialog) {
				dError = new DError((JDialog) container, APPLICATION_MODAL, ex);
			} else if (container instanceof JFrame) {
				dError = new DError((JFrame) container, APPLICATION_MODAL, ex);
			}

			dError.setLocationRelativeTo(container);
			dError.setVisible(true);
			return;
		}
	}

	private void clearKeyIdentifier() {
		setKeyIdentifier(null);
	}
}
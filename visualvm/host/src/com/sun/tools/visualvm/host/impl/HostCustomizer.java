/*
 * Copyright 2007-2008 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

package com.sun.tools.visualvm.host.impl;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.profiler.ui.ProfilerDialogs;
import org.openide.DialogDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 *
 * @author Jiri Sedlacek
 */
class HostCustomizer extends JPanel {

  private boolean internalChange = false;

  public static HostProperties defineHost() {
    HostCustomizer hc = getInstance();
    hc.setupDefineHost();
    
    final DialogDescriptor dd = new DialogDescriptor(hc, NbBundle.getMessage(HostCustomizer.class, "Title_Add_Remote_Host"), true, new Object[] {   // NOI18N
      hc.okButton, DialogDescriptor.CANCEL_OPTION }, hc.okButton, 0, null, null);
    final Dialog d = ProfilerDialogs.createDialog(dd);
    d.pack();
    d.setVisible(true);
    
    if (dd.getValue() == hc.okButton) return new HostProperties(hc.getHostName(), hc.getDisplayName());
    else return null;
  }
  
  
  private static HostCustomizer instance;
  
  private HostCustomizer() {
    initComponents();
    update();
  }
  
  private static HostCustomizer getInstance() {
    if (instance == null) instance = new HostCustomizer();
    return instance;
  }
  
  private String getHostName() {
      return hostnameField.getText().trim();
  }
  
  private String getDisplayName() {
      return displaynameField.getText().trim();
  }
  
  private void setupDefineHost() {
    hostnameField.setEnabled(true);
    displaynameCheckbox.setSelected(false);
    displaynameCheckbox.setEnabled(true);
    hostnameField.setText("");
    displaynameField.setText("");
  }
  
  private void update() {
    if (internalChange) return;
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        String hostname = getHostName();
        
        if (!displaynameCheckbox.isSelected()) {
          internalChange = true;
          displaynameField.setText(hostname);
          internalChange = false;
        }
        
        String displayname = getDisplayName();
        displaynameField.setEnabled(displaynameCheckbox.isSelected());
        
        okButton.setEnabled(hostname.length() > 0 && displayname.length() > 0);
      }
    });
  }
  
  private void initComponents() {
    setLayout(new GridBagLayout());
    GridBagConstraints constraints;
    
    // hostnameLabel
    hostnameLabel = new JLabel();
    Mnemonics.setLocalizedText(hostnameLabel, NbBundle.getMessage(HostCustomizer.class, "LBL_Host_name")); // NOI18N
    constraints = new GridBagConstraints();
    constraints.gridx = 0;
    constraints.gridy = 0;
    constraints.gridwidth = 1;
    constraints.fill = GridBagConstraints.NONE;
    constraints.anchor = GridBagConstraints.EAST;
    constraints.insets = new Insets(15, 10, 0, 0);
    add(hostnameLabel, constraints);
    
    // hostnameField
    hostnameField = new JTextField();
    hostnameLabel.setLabelFor(hostnameField);
    hostnameField.setPreferredSize(new Dimension(250, hostnameField.getPreferredSize().height));
    hostnameField.getDocument().addDocumentListener(new DocumentListener() {
      public void insertUpdate(DocumentEvent e)  { update(); }
      public void removeUpdate(DocumentEvent e)  { update(); }
      public void changedUpdate(DocumentEvent e) { update(); }
    });
    constraints = new GridBagConstraints();
    constraints.gridx = 1;
    constraints.gridy = 0;
    constraints.gridwidth = GridBagConstraints.REMAINDER;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.anchor = GridBagConstraints.WEST;
    constraints.insets = new Insets(15, 5, 0, 10);
    add(hostnameField, constraints);
    
    // displaynameCheckbox
    displaynameCheckbox = new JCheckBox();
    Mnemonics.setLocalizedText(displaynameCheckbox, NbBundle.getMessage(HostCustomizer.class, "LBL_Display_name")); // NOI18N
    displaynameCheckbox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) { update(); };
    });
    constraints = new GridBagConstraints();
    constraints.gridx = 0;
    constraints.gridy = 1;
    constraints.gridwidth = 1;
    constraints.fill = GridBagConstraints.NONE;
    constraints.anchor = GridBagConstraints.EAST;
    constraints.insets = new Insets(8, 10, 0, 0);
    add(displaynameCheckbox, constraints);
    
    // displaynameField
    displaynameField = new JTextField();
    displaynameField.setPreferredSize(new Dimension(250, displaynameField.getPreferredSize().height));
    displaynameField.getDocument().addDocumentListener(new DocumentListener() {
      public void insertUpdate(DocumentEvent e)  { update(); }
      public void removeUpdate(DocumentEvent e)  { update(); }
      public void changedUpdate(DocumentEvent e) { update(); }
    });
    constraints = new GridBagConstraints();
    constraints.gridx = 1;
    constraints.gridy = 1;
    constraints.gridwidth = GridBagConstraints.REMAINDER;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.anchor = GridBagConstraints.WEST;
    constraints.insets = new Insets(8, 5, 0, 10);
    add(displaynameField, constraints);
    
    // spacer
    JPanel spacer = new JPanel(new BorderLayout(0, 0));
    constraints = new GridBagConstraints();
    constraints.gridx = 0;
    constraints.gridy = 2;
    constraints.weightx = 1;
    constraints.weighty = 1;
    constraints.gridwidth = GridBagConstraints.REMAINDER;
    constraints.fill = GridBagConstraints.BOTH;
    constraints.anchor = GridBagConstraints.NORTHWEST;
    constraints.insets = new Insets(0, 0, 15, 0);
    add(spacer, constraints);
    
    // okButton
    okButton = new JButton();
    Mnemonics.setLocalizedText(okButton, NbBundle.getMessage(HostCustomizer.class, "LBL_OK")); // NOI18N
    
    // UI tweaks
    displaynameCheckbox.setBorder(hostnameLabel.getBorder());
  }
  
  private JLabel hostnameLabel;
  private JTextField hostnameField;
  private JCheckBox displaynameCheckbox;
  private JTextField displaynameField;
  
  private JButton okButton;
  
}

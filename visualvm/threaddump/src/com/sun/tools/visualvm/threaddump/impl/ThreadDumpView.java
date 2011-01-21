/*
 * Copyright (c) 2007, 2011, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
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
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package com.sun.tools.visualvm.threaddump.impl;

import com.sun.tools.visualvm.core.datasource.descriptor.DataSourceDescriptor;
import com.sun.tools.visualvm.core.datasource.descriptor.DataSourceDescriptorFactory;
import com.sun.tools.visualvm.threaddump.ThreadDump;
import com.sun.tools.visualvm.core.ui.DataSourceView;
import com.sun.tools.visualvm.core.ui.components.DataViewComponent;
import com.sun.tools.visualvm.core.ui.components.ScrollableContainer;
import com.sun.tools.visualvm.uisupport.HTMLTextArea;
import java.awt.BorderLayout;
import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Jiri Sedlacek
 * @author Tomas Hurka
 */
class ThreadDumpView extends DataSourceView {
    private static final Logger LOGGER = Logger.getLogger(ThreadDumpView.class.getName());
    
    public ThreadDumpView(ThreadDump threadDump) {
        this(threadDump, DataSourceDescriptorFactory.getDescriptor(threadDump));
        
    }
    
    private ThreadDumpView(ThreadDump threadDump, DataSourceDescriptor descriptor) {
        super(threadDump, descriptor.getName(), descriptor.getIcon(), 0, true);
    }
        
    protected DataViewComponent createComponent() {
        ThreadDump threadDump = (ThreadDump)getDataSource();
        DataViewComponent dvc = new DataViewComponent(
                new MasterViewSupport(threadDump).getMasterView(),
                new DataViewComponent.MasterViewConfiguration(true));
        
        return dvc;
    }
    
    
    // --- General data --------------------------------------------------------
    
    private static class MasterViewSupport extends JPanel  {
        
        private JLabel progressLabel;
        private JPanel contentsPanel;
        
        public MasterViewSupport(ThreadDump threadDump) {
            initComponents();
            loadThreadDump(threadDump.getFile());
        }
        
        
        public DataViewComponent.MasterView getMasterView() {
            return new DataViewComponent.MasterView(NbBundle.getMessage(ThreadDumpView.class, "LBL_Thread_Dump"), null, new ScrollableContainer(this));  // NOI18N
        }
        
        
        private void initComponents() {
            setLayout(new BorderLayout());
            
            progressLabel = new JLabel(NbBundle.getMessage(ThreadDumpView.class, "MSG_Loading_Thread_Dump"), SwingConstants.CENTER);     // NOI18N
        
            contentsPanel = new JPanel(new BorderLayout());
            contentsPanel.add(progressLabel, BorderLayout.CENTER);
            contentsPanel.setOpaque(false);
            
            add(contentsPanel, BorderLayout.CENTER);
            setOpaque(false);
        }

        private static String htmlize(String value) {
            return value.replace("&", "&amp;").replace("<", "&lt;");     // NOI18N
        }

        private static String transform(String value) {
            StringBuilder sb = new StringBuilder();
            String[] result = value.split("\\n"); // NOI18N
            for (int i = 0; i < result.length; i++) {
                String line = result[i];
                if (!line.isEmpty() && !Character.isWhitespace(line.charAt(0))) {
                    sb.append("<span style=\"color: #0033CC\">"); // NOI18N
                    sb.append(line);
                    sb.append("</span><br>"); // NOI18N
                } else {
                    sb.append(line);
                    sb.append("<br>"); // NOI18N
                }
            }
            return sb.toString();
        }

        private void loadThreadDump(final File file) {
            RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
              InputStream is = null;
              try {
                is = new FileInputStream(file);
                byte[] data = new byte[(int)file.length()];
                try {
                  is.read(data);
                } catch (IOException ex) {
                  LOGGER.throwing(ThreadDumpView.class.getName(), "loadThreadDump", ex);     // NOI18N
                }
                try {
                  HTMLTextArea area = new HTMLTextArea();
                  area.setForeground(new Color(0xcc, 0x33, 0));
                  area.setText("<pre>" + transform(htmlize(new String(data, "UTF-8"))) + "</pre>"); // NOI18N
                  area.setCaretPosition(0);
                  area.setBorder(BorderFactory.createEmptyBorder(14, 8, 14, 8));
                  contentsPanel.remove(progressLabel);
                  contentsPanel.add(area, BorderLayout.CENTER);
                  contentsPanel.revalidate();
                  contentsPanel.repaint();
//                  contentsPanel.doLayout();
                } catch (Exception ex) {
                  LOGGER.throwing(ThreadDumpView.class.getName(), "loadThreadDump", ex);     // NOI18N
                }
              } catch (FileNotFoundException ex) {
                LOGGER.throwing(ThreadDumpView.class.getName(), "loadThreadDump", ex);   // NOI18N
              } finally {
                  if (is != null) {
                      try {
                          is.close();
                      } catch (IOException e) {
                          // ignore
                      }
                  }
              }
           }
          });
        }
        
    }
    
}

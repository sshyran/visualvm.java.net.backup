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

package com.sun.tools.visualvm.application.views.overview;

import com.sun.tools.visualvm.application.Application;
import com.sun.tools.visualvm.application.jvm.Jvm;
import com.sun.tools.visualvm.application.jvm.JvmFactory;
import com.sun.tools.visualvm.core.datasource.DataSource;
import com.sun.tools.visualvm.core.snapshot.Snapshot;
import com.sun.tools.visualvm.core.datasupport.DataChangeEvent;
import com.sun.tools.visualvm.core.datasupport.DataChangeListener;
import com.sun.tools.visualvm.core.snapshot.RegisteredSnapshotCategories;
import com.sun.tools.visualvm.core.snapshot.SnapshotCategory;
import com.sun.tools.visualvm.core.ui.components.DataViewComponent;
import com.sun.tools.visualvm.core.ui.components.NotSupportedDisplayer;
import com.sun.tools.visualvm.core.ui.components.ScrollableContainer;
import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.netbeans.lib.profiler.ui.components.HTMLTextArea;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

/**
 * A public entrypoint to the Overview subtab.
 *
 * @author Jiri Sedlacek
 * @author Tomas Hurka
 */
class OverviewViewSupport {

    // --- General data --------------------------------------------------------
    
    static class MasterViewSupport extends JPanel  {
        private PropertyChangeListener oomeListener;
        
        public MasterViewSupport(ApplicationOverviewModel model) {
            initComponents(model);
        }
        
        
        public DataViewComponent.MasterView getMasterView() {
            return new DataViewComponent.MasterView(NbBundle.getMessage(OverviewViewSupport.class, "LBL_Overview"), null, this);    // NOI18N
        }
        
        
        private void initComponents(final ApplicationOverviewModel model) {
            setLayout(new BorderLayout());
            setOpaque(false);
            
            final HTMLTextArea area = new HTMLTextArea("<nobr>" + getGeneralProperties(model) + "</nobr>"); // NOI18N
            area.setBorder(BorderFactory.createEmptyBorder(14, 8, 14, 8));
            
            DataSource source = model.getSource();
            if (source instanceof Application) {
                oomeListener = new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent evt) {
                        if (Jvm.PROPERTY_DUMP_OOME_ENABLED.equals(evt.getPropertyName())) {
                            int selStart = area.getSelectionStart();
                            int selEnd   = area.getSelectionEnd();
                            area.setText("<nobr>" + getGeneralProperties(model) + "</nobr>");   // NOI18N
                            area.select(selStart, selEnd);
                        }
                    }
                };
                Jvm jvm = JvmFactory.getJVMFor((Application)source);
                jvm.addPropertyChangeListener(WeakListeners.propertyChange(oomeListener,jvm));
            }
            add(area, BorderLayout.CENTER);
        }
        
        private String getGeneralProperties(ApplicationOverviewModel model) {
            StringBuilder data = new StringBuilder();
            
            // Application information
            String PID = NbBundle.getMessage(OverviewViewSupport.class, "LBL_PID"); // NOI18N
            String HOST = NbBundle.getMessage(OverviewViewSupport.class, "LBL_Host");   // NOI18N
            data.append("<b>"+PID+":</b> " + model.getPid() + "<br>");  // NOI18N
            data.append("<b>"+HOST+":</b> " + model.getHostName() + "<br>");    // NOI18N
            
            if (model.basicInfoSupported()) {
                String MAIN_CLASS = NbBundle.getMessage(OverviewViewSupport.class, "LBL_Main_class");   // NOI18N
                String ARGS = NbBundle.getMessage(OverviewViewSupport.class, "LBL_Arguments");  // NOI18N
                String JVM = NbBundle.getMessage(OverviewViewSupport.class, "LBL_JVM"); // NOI18N
                String JAVA_HOME = NbBundle.getMessage(OverviewViewSupport.class, "LBL_Java_Home"); // NOI18N
                String JVM_FLAGS = NbBundle.getMessage(OverviewViewSupport.class, "LBL_JVM_Flags"); // NOI18N
                String HEAP_DUMP_OOME = NbBundle.getMessage(OverviewViewSupport.class, "LBL_Heap_dump_on_OOME");    // NOI18N
                data.append("<b>"+MAIN_CLASS+":</b> " + model.getMainClass() + "<br>"); // NOI18N
                data.append("<b>"+ARGS+":</b> " + model.getMainArgs() + "<br>");    // NOI18N
                
                data.append("<br>");    // NOI18N
                data.append("<b>"+JVM+":</b> " + model.getVmId() + "<br>"); // NOI18N
                data.append("<b>"+JAVA_HOME+":</b> " + model.getJavaHome() + "<br>");   // NOI18N
                data.append("<b>"+JVM_FLAGS+":</b> " + model.getJvmFlags() + "<br><br>");   // NOI18N
                data.append("<b>"+HEAP_DUMP_OOME+":</b> " + model.oomeEnabled() + "<br>");  // NOI18N
            }
            
            return data.toString();
            
        }
        
    }
    
    // --- Snapshots -----------------------------------------------------------
    
    static class SnapshotsViewSupport extends JPanel implements DataChangeListener {
        
        private DataSource dataSource;
        private HTMLTextArea area;
        
        
        public SnapshotsViewSupport(DataSource dataSource) {
            this.dataSource = dataSource;
            initComponents();
            dataSource.getRepository().addDataChangeListener(this, Snapshot.class);
        }
        
        public DataViewComponent.DetailsView getDetailsView() {
            return new DataViewComponent.DetailsView(NbBundle.getMessage(OverviewViewSupport.class, "LBL_Saved_data"), null, 10, this, null);   // NOI18N
        }
        
        private void initComponents() {
            setLayout(new BorderLayout());
            setOpaque(false);
            
            area = new HTMLTextArea();
            updateSavedData();
            area.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            
            add(new ScrollableContainer(area), BorderLayout.CENTER);
        }
        
                public void dataChanged(DataChangeEvent event) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() { updateSavedData(); }
            });
        }
            
        void removed() {
            dataSource.getRepository().removeDataChangeListener(this);
        }
        
        private void updateSavedData() {
            StringBuilder data = new StringBuilder();
            
            List<SnapshotCategory> snapshotCategories = RegisteredSnapshotCategories.sharedInstance().getVisibleCategories();
            for (SnapshotCategory category : snapshotCategories)
                data.append("<b>" + category.getName() + ":</b> " + dataSource.getRepository().getDataSources(category.getType()).size() + "<br>"); // NOI18N
            
            area.setText("<nobr>" + data.toString() + "</nobr>");   // NOI18N
        }
        
    }
    
    
    // --- JVM arguments -------------------------------------------------------
    
    static class JVMArgumentsViewSupport extends JPanel  {
        
        public JVMArgumentsViewSupport(String jvmargs) {
            initComponents(jvmargs);
        }
        
        public DataViewComponent.DetailsView getDetailsView() {
            return new DataViewComponent.DetailsView(NbBundle.getMessage(OverviewViewSupport.class, "LBL_JVM_arguments"), null, 10, this, null);    // NOI18N
        }
        
        private void initComponents(String jvmargs) {
            setLayout(new BorderLayout());
            setOpaque(false);
            
            JComponent contents;
            
            if (jvmargs != null) {
                HTMLTextArea area = new HTMLTextArea("<nobr>" + jvmargs + "</nobr>");   // NOI18N
                area.setCaretPosition(0);
                area.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                contents = area;
            } else {
                contents = new NotSupportedDisplayer(NotSupportedDisplayer.JVM);
            }
            
            add(new ScrollableContainer(contents), BorderLayout.CENTER);
        }
        
                }
        
    
    // --- System properties ---------------------------------------------------
    
    static class SystemPropertiesViewSupport extends JPanel  {
        
        public SystemPropertiesViewSupport(String properties) {
            initComponents(properties);
        }
        
        public DataViewComponent.DetailsView getDetailsView() {
            return new DataViewComponent.DetailsView(NbBundle.getMessage(OverviewViewSupport.class, "LBL_System_properties"), null, 20, this, null);    // NOI18N
        }
        
        private void initComponents(String properties) {
            setLayout(new BorderLayout());
            setOpaque(false);
            
            JComponent contents;
            
            if (properties != null) {
                HTMLTextArea area = new HTMLTextArea("<nobr>" + properties + "</nobr>");    // NOI18N
                area.setCaretPosition(0);
                area.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                contents = area;
            } else {
                contents = new NotSupportedDisplayer(NotSupportedDisplayer.JVM);
            }
            
            add(new ScrollableContainer(contents), BorderLayout.CENTER);
        }
        
            }
        }

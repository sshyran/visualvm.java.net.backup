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

package com.sun.tools.visualvm.application.views.threads;

import com.sun.tools.visualvm.application.Application;
import com.sun.tools.visualvm.core.datasupport.DataRemovedListener;
import com.sun.tools.visualvm.application.jvm.Jvm;
import com.sun.tools.visualvm.application.jvm.JvmFactory;
import com.sun.tools.visualvm.core.datasource.DataSource;
import com.sun.tools.visualvm.core.datasupport.Stateful;
import com.sun.tools.visualvm.core.options.GlobalPreferences;
import com.sun.tools.visualvm.core.ui.DataSourceView;
import com.sun.tools.visualvm.core.ui.components.DataViewComponent;
import com.sun.tools.visualvm.threaddump.ThreadDumpSupport;
import com.sun.tools.visualvm.tools.jmx.JmxModel;
import com.sun.tools.visualvm.tools.jmx.JmxModel.ConnectionState;
import com.sun.tools.visualvm.tools.jmx.JmxModelFactory;
import com.sun.tools.visualvm.tools.jmx.JvmMXBeans;
import com.sun.tools.visualvm.tools.jmx.JvmMXBeansFactory;
import com.sun.tools.visualvm.tools.jmx.MBeanCacheListener;
import com.sun.tools.visualvm.uisupport.HTMLTextArea;
import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.netbeans.lib.profiler.ui.threads.ThreadsDetailsPanel;
import org.netbeans.lib.profiler.ui.threads.ThreadsPanel;
import org.netbeans.lib.profiler.ui.threads.ThreadsTablePanel;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

/**
 *
 * @author Jiri Sedlacek
 */
class ApplicationThreadsView extends DataSourceView implements DataRemovedListener<Application> {

    private static final String IMAGE_PATH = "com/sun/tools/visualvm/application/views/resources/threads.png";  // NOI18N
    private JvmMXBeans mxbeans;
    private VisualVMThreadsDataManager threadsManager;
    private MBeanCacheListener listener;

    private boolean takeThreadDumpSupported;

    ApplicationThreadsView(DataSource dataSource) {
        super(dataSource, NbBundle.getMessage(ApplicationThreadsView.class, "LBL_Threads"), new ImageIcon(ImageUtilities.loadImage(IMAGE_PATH, true)).getImage(), 30, false);   // NOI18N
    }

    @Override
    protected void willBeAdded() {
        DataSource ds = getDataSource();
        if (ds instanceof Application) {
            Application application = (Application)ds;
            Jvm jvm = JvmFactory.getJVMFor(application);
            takeThreadDumpSupported = jvm == null ? false : jvm.isTakeThreadDumpSupported();
            threadsManager = null;
            JmxModel jmxModel = JmxModelFactory.getJmxModelFor(application);
            if (jmxModel != null && jmxModel.getConnectionState() == ConnectionState.CONNECTED) {
                mxbeans = JvmMXBeansFactory.getJvmMXBeans(jmxModel, GlobalPreferences.sharedInstance().getThreadsPoll() * 1000);
                if (mxbeans != null) {
                    threadsManager = new ThreadMXBeanDataManager(mxbeans.getThreadMXBean());
                }
            }
        } else {
            threadsManager = PersistenceSupport.loadDataManager(ds.getStorage());
        }
    }

    @Override
    protected synchronized void removed() {
        cleanup();
    }

    VisualVMThreadsDataManager getDataManager() {
        return threadsManager;
    }

    public synchronized void dataRemoved(Application dataSource) {
        cleanup();
    }
    
    private synchronized void cleanup() {
        if (mxbeans != null) {
            mxbeans.removeMBeanCacheListener(listener);
            mxbeans = null;
        }
    }

    protected DataViewComponent createComponent() {
        DataSource ds = getDataSource();
        final Application application = ds instanceof Application ?
            (Application)ds : null;
        final MasterViewSupport mvs =
                new MasterViewSupport(ds, takeThreadDumpSupported, threadsManager);
        if (mxbeans != null) {
            listener = new MBeanCacheListener() {
                public void flushed() {
                    if (application.getState() != Stateful.STATE_AVAILABLE) {
                        cleanup();
                    } else {
                        ((ThreadMXBeanDataManager)threadsManager).refreshThreadsAsync();
                        mvs.updateThreadsCounts(threadsManager);
                    }
                }
            };
            mxbeans.addMBeanCacheListener(listener);
        }
        if (application != null) application.notifyWhenRemoved(this);

        final DataViewComponent dvc = new DataViewComponent(mvs.getMasterView(), new DataViewComponent.MasterViewConfiguration(false));

        dvc.configureDetailsArea(new DataViewComponent.DetailsAreaConfiguration(NbBundle.getMessage(ApplicationThreadsView.class, "LBL_Threads_visualization"), true), DataViewComponent.TOP_LEFT); // NOI18N

        final DetailsViewSupport detailsViewSupport = new DetailsViewSupport(threadsManager);
        final DataViewComponent.DetailsView detailsView = detailsViewSupport.getDetailsView();
        ThreadsPanel.ThreadsDetailsCallback timelineCallback = new ThreadsPanel.ThreadsDetailsCallback() {
            public void showDetails(final int[] indexes) {
                detailsViewSupport.showDetails(indexes);
                dvc.selectDetailsView(detailsView);
            }
        };
        ThreadsTablePanel.ThreadsDetailsCallback tableCallback = new ThreadsTablePanel.ThreadsDetailsCallback() {
            public void showDetails(final int[] indexes) {
                detailsViewSupport.showDetails(indexes);
                dvc.selectDetailsView(detailsView);
            }
        };

        dvc.addDetailsView(new TimelineViewSupport(threadsManager, timelineCallback).getDetailsView(), DataViewComponent.TOP_LEFT);
        dvc.addDetailsView(new TableViewSupport(threadsManager, tableCallback).getDetailsView(), DataViewComponent.TOP_LEFT);
        dvc.addDetailsView(detailsView, DataViewComponent.TOP_LEFT);

        return dvc;
    }

    // --- General data --------------------------------------------------------

    private static class MasterViewSupport extends JPanel implements DataRemovedListener<Application>, PropertyChangeListener {

        private static RequestProcessor worker = null;

        private Application application;
        private HTMLTextArea area;
        private JButton threadDumpButton;
        private static final String LIVE_THRADS = NbBundle.getMessage(ApplicationThreadsView.class, "LBL_Live_threads");    // NOI18N
        private static final String DAEMON_THREADS = NbBundle.getMessage(ApplicationThreadsView.class, "LBL_Daemon_threads");   // NOI18N

        MasterViewSupport(DataSource dataSource, boolean takeThreadDumpSupported,
                          VisualVMThreadsDataManager threadsManager) {
            if (dataSource instanceof Application) application = (Application)dataSource;
            initComponents(takeThreadDumpSupported);
            updateThreadsCounts(threadsManager);
        }

        DataViewComponent.MasterView getMasterView() {
            return new DataViewComponent.MasterView(NbBundle.getMessage(ApplicationThreadsView.class, "LBL_Threads"), null, this);  // NOI18N
        }

        public void dataRemoved(Application dataSource) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    threadDumpButton.setEnabled(false);
                }
            });
        }

        public void propertyChange(PropertyChangeEvent evt) {
            dataRemoved(application);
        }

        private void initComponents(boolean takeThreadDumpSupported) {
            setLayout(new BorderLayout());
            setOpaque(false);

            area = new HTMLTextArea();
            area.setBorder(BorderFactory.createEmptyBorder(14, 8, 14, 8));

            add(area, BorderLayout.CENTER);

            threadDumpButton = new JButton(new AbstractAction(NbBundle.getMessage(ApplicationThreadsView.class, "LBL_Thread_Dump")) {   // NOI18N
                public void actionPerformed(ActionEvent e) {
                    ThreadDumpSupport.getInstance().takeThreadDump(application, (e.getModifiers() &
                            Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) == 0);
                }
            });
            threadDumpButton.setEnabled(takeThreadDumpSupported);

            JPanel buttonsArea = new JPanel(new BorderLayout());
            buttonsArea.setOpaque(false);
            JPanel buttonsContainer = new JPanel(new BorderLayout(3, 0));
            buttonsContainer.setBackground(area.getBackground());
            buttonsContainer.setBorder(BorderFactory.createEmptyBorder(14, 8, 14, 8));
            buttonsContainer.add(threadDumpButton, BorderLayout.EAST);
            buttonsArea.add(buttonsContainer, BorderLayout.NORTH);

            add(buttonsArea, BorderLayout.AFTER_LINE_ENDS);

            if (application != null) {
                application.notifyWhenRemoved(this);
                application.addPropertyChangeListener(Stateful.PROPERTY_STATE,
                        WeakListeners.propertyChange(this, application));
            }
        }

        void updateThreadsCounts(final VisualVMThreadsDataManager threadsManager) {

            final int[] threads = new int[2];

            getWorker().post(new Runnable() {
                public void run() {
                    try {
                        threads[0] = threadsManager.getThreadCount();
                        threads[1] = threadsManager.getDaemonThreadCount();
                    } catch (Exception ex) {
                        threads[0] = 0;
                        threads[1] = 0;
                    }
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            updateThreadsCounts(threads[0], threads[1]);
                        }
                    });
                }
            });
        }

        private void updateThreadsCounts(int liveThreads, int daemonThreads) {
            StringBuilder data = new StringBuilder();

            data.append("<b>" + LIVE_THRADS + ":</b> " + liveThreads + "<br>");  // NOI18N
            data.append("<b>" + DAEMON_THREADS + ":</b> " + daemonThreads + "<br>");   // NOI18N

            int selStart = area.getSelectionStart();
            int selEnd = area.getSelectionEnd();
            area.setText(data.toString());
            area.select(selStart, selEnd);
        }

        private static synchronized RequestProcessor getWorker() {
            if (worker == null) worker = new RequestProcessor("ThreadsWorker", 1); // NOI18N
            return worker;
        }

    }

    // --- Timeline ------------------------------------------------------------

    private static class TimelineViewSupport extends JPanel {

        TimelineViewSupport(VisualVMThreadsDataManager threadsManager, ThreadsPanel.ThreadsDetailsCallback callback) {
            initComponents(threadsManager, callback);
        }

        DataViewComponent.DetailsView getDetailsView() {
            return new DataViewComponent.DetailsView(NbBundle.getMessage(ApplicationThreadsView.class, "LBL_Timeline"), null, 10, this, null);  // NOI18N
        }

        private void initComponents(VisualVMThreadsDataManager threadsManager, ThreadsPanel.ThreadsDetailsCallback callback) {
            setLayout(new BorderLayout());
            setOpaque(false);

            ThreadsPanel threadsPanel = new ThreadsPanel(threadsManager, callback, true);
            threadsPanel.threadsMonitoringEnabled();

            add(threadsPanel, BorderLayout.CENTER);
        }
    }

    // --- Timeline ------------------------------------------------------------

    private static class TableViewSupport extends JPanel {

        TableViewSupport(VisualVMThreadsDataManager threadsManager, ThreadsTablePanel.ThreadsDetailsCallback callback) {
            initComponents(threadsManager, callback);
        }

        DataViewComponent.DetailsView getDetailsView() {
            return new DataViewComponent.DetailsView(NbBundle.getMessage(ApplicationThreadsView.class, "LBL_Table"), null, 20, this, null);  // NOI18N
        }

        private void initComponents(VisualVMThreadsDataManager threadsManager, ThreadsTablePanel.ThreadsDetailsCallback callback) {
            setLayout(new BorderLayout());
            setOpaque(false);

            ThreadsTablePanel threadsPanel = new ThreadsTablePanel(threadsManager, callback, true);
            threadsPanel.setOpaque(false);

            JComponent toolbar = (JComponent)threadsPanel.getComponent(0);
            toolbar.setOpaque(false);
            toolbar.setBorder(BorderFactory.createEmptyBorder(9, 5, 5, 5));
            
            JComponent tablePanel = (JComponent)threadsPanel.getComponent(1);
            tablePanel.setOpaque(false);

            add(threadsPanel, BorderLayout.CENTER);
        }
    }

    // --- Details -------------------------------------------------------------

    private static class DetailsViewSupport extends JPanel {

        private ThreadsDetailsPanel threadsDetailsPanel;

        DetailsViewSupport(VisualVMThreadsDataManager threadsManager) {
            initComponents(threadsManager);
        }

        DataViewComponent.DetailsView getDetailsView() {
            return new DataViewComponent.DetailsView(NbBundle.getMessage(ApplicationThreadsView.class, "LBL_Details"), null, 30, this, null);   // NOI18N
        }

        void showDetails(int[] indexes) {
            threadsDetailsPanel.showDetails(indexes);
        }

        private void initComponents(VisualVMThreadsDataManager threadsManager) {
            setLayout(new BorderLayout());
            setOpaque(false);
            
            threadsDetailsPanel = new ThreadsDetailsPanel(threadsManager, true);
            add(threadsDetailsPanel, BorderLayout.CENTER);
        }
    }
}

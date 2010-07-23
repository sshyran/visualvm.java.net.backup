/*
 *  Copyright 2007-2010 Sun Microsystems, Inc.  All Rights Reserved.
 *  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 * 
 *  This code is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License version 2 only, as
 *  published by the Free Software Foundation.  Sun designates this
 *  particular file as subject to the "Classpath" exception as provided
 *  by Sun in the LICENSE file that accompanied this code.
 * 
 *  This code is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 *  version 2 for more details (a copy is included in the LICENSE file that
 *  accompanied this code).
 * 
 *  You should have received a copy of the GNU General Public License version
 *  2 along with this work; if not, write to the Free Software Foundation,
 *  Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 *  Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 *  CA 95054 USA or visit www.sun.com if you need additional information or
 *  have any questions.
 */

package com.sun.tools.visualvm.profiling.snapshot;

import com.sun.tools.visualvm.core.snapshot.SnapshotCategory;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.profiler.LoadedSnapshot;
import org.netbeans.modules.profiler.NetBeansProfiler;
import org.netbeans.modules.profiler.ResultsManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Jiri Sedlacek
 */
final class ProfilerSnapshotCategory extends SnapshotCategory<ProfilerSnapshot> {
    private static final Logger LOGGER =
            Logger.getLogger(ProfilerSnapshotCategory.class.getName());
    
    private static final String NAME = NbBundle.getMessage(
            ProfilerSnapshotCategory.class, "MSG_Profiler_Snapshots");   // NOI18N
    private static final String PREFIX = "snapshot";    // NOI18N
    private static final String NPS_SUFFIX = ".nps";    // NOI18N
    private static final String NPSS_SUFFIX = ".npss";    // NOI18N
    
    public ProfilerSnapshotCategory() {
        super(NAME, ProfilerSnapshot.class, PREFIX, NPS_SUFFIX, 30);
    }
    
    public boolean supportsOpenSnapshot() {
        return true;
    }
    
    protected boolean isSnapshot(File file) {
        if (super.isSnapshot(file)) {
            return true;
        }
        return file != null && file.getName().endsWith(NPSS_SUFFIX);
    }

    public void openSnapshot(final File file) {
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                ProgressHandle pHandle = null;
                try {
                    pHandle = ProgressHandleFactory.createHandle(
                            NbBundle.getMessage(ProfilerSnapshotCategory.class,
                                                "MSG_Opening_Profiler_Snapshot")); // NOI18N
                    pHandle.setInitialDelay(0);
                    pHandle.start();
                    try {
                        FileObject fileObject = FileUtil.toFileObject(file);
                        final LoadedSnapshot loadedSnapshot =
                                ResultsManager.getDefault().loadSnapshot(fileObject);
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                ResultsManager.getDefault().openSnapshot(loadedSnapshot);
                            }
                        });
                    } catch (Exception e) {
                        LOGGER.log(Level.INFO, "Error loading profiler snapshot", e); // NOI18N
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                NetBeansProfiler.getDefaultNB().displayError(
                                        NbBundle.getMessage(ProfilerSnapshotCategory.class,
                                                            "MSG_Opening_snapshot_failed")); // NOI18N
                            }   
                        });
                    }
                } finally {
                    final ProgressHandle pHandleF = pHandle;
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() { if (pHandleF != null) pHandleF.finish(); }
                    });
                }
            }
        });
    }
    
    public FileFilter getFileFilter() {
        return new FileFilter() {
            public boolean accept(File f) {
                return f.isDirectory() || isSnapshot(f);
            }
            public String getDescription() {
                String suff = getSuffix();
                return getName() + (suff != null ? " (*" + suff +", *" + NPSS_SUFFIX + ")" : "");    // NOI18N
            }
        };
    }    

}

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

package com.sun.tools.visualvm.coredump;

import com.sun.tools.visualvm.coredump.impl.CoreDumpCategory;
import com.sun.tools.visualvm.core.datasource.Storage;
import com.sun.tools.visualvm.core.datasource.descriptor.DataSourceDescriptorFactory;
import com.sun.tools.visualvm.core.datasupport.Utils;
import com.sun.tools.visualvm.core.snapshot.RegisteredSnapshotCategories;
import com.sun.tools.visualvm.core.snapshot.SnapshotCategory;
import com.sun.tools.visualvm.core.ui.DataSourceViewsManager;
import com.sun.tools.visualvm.core.ui.PluggableDataSourceViewProvider;
import com.sun.tools.visualvm.coredump.impl.CoreDumpDescriptorProvider;
import com.sun.tools.visualvm.coredump.impl.CoreDumpOverviewViewProvider;
import com.sun.tools.visualvm.coredump.impl.CoreDumpProvider;
import java.io.File;
import org.openide.util.Utilities;

/**
 *
 * @author Tomas Hurka
 */
public final class CoreDumpSupport {
    
    private static final String COREDUMPS_STORAGE_DIRNAME = "coredumps";    // NOI18N
    
    private static final Object coredumpsStorageDirectoryLock = new Object();
    // @GuardedBy coredumpsStorageDirectoryLock
    private static File coredumpsStorageDirectory;
    private static final Object coredumpsStorageDirectoryStringLock = new Object();
    // @GuardedBy coredumpsStorageDirectoryStringLock
    private static String coredumpsStorageDirectoryString;
    
    private static CoreDumpOverviewViewProvider viewProvider = new CoreDumpOverviewViewProvider();
    private static CoreDumpCategory category = new CoreDumpCategory();
    private static final Object currentJDKHomeLock = new Object();
    // @GuardedBy currentJDKHomeLock
    private static String currentJDKHome;
    
    
    public static PluggableDataSourceViewProvider<CoreDump> getOverviewView() {
        return viewProvider;
    } 
    
    public static SnapshotCategory getCategory() {
        return category;
    }
    
    // TODO: should be moved to some public Utils class
    public static String getCurrentJDKHome() {
        synchronized(currentJDKHomeLock) {
            if (currentJDKHome == null) {
                currentJDKHome = System.getProperty("java.home");   // NOI18N
                String jreSuffix = File.separator + "jre";  // NOI18N
                if (currentJDKHome.endsWith(jreSuffix)) currentJDKHome = currentJDKHome.substring(0, currentJDKHome.length() - jreSuffix.length());
            }
            return currentJDKHome;
        }
    }
    
    static String getStorageDirectoryString() {
        synchronized(coredumpsStorageDirectoryStringLock) {
            if (coredumpsStorageDirectoryString == null)
                coredumpsStorageDirectoryString = Storage.getPersistentStorageDirectoryString() + File.separator + COREDUMPS_STORAGE_DIRNAME;
            return coredumpsStorageDirectoryString;
        }
    }
    
    public static File getStorageDirectory() {
        synchronized(coredumpsStorageDirectoryLock) {
            if (coredumpsStorageDirectory == null) {
                String snapshotsStorageString = getStorageDirectoryString();
                coredumpsStorageDirectory = new File(snapshotsStorageString);
                if (coredumpsStorageDirectory.exists() && coredumpsStorageDirectory.isFile())
                    throw new IllegalStateException("Cannot create coredumps storage directory " + snapshotsStorageString + ", file in the way");   // NOI18N
                if (coredumpsStorageDirectory.exists() && (!coredumpsStorageDirectory.canRead() || !coredumpsStorageDirectory.canWrite()))
                    throw new IllegalStateException("Cannot access coredumps storage directory " + snapshotsStorageString + ", read&write permission required");    // NOI18N
                if (!Utils.prepareDirectory(coredumpsStorageDirectory))
                    throw new IllegalStateException("Cannot create coredumps storage directory " + snapshotsStorageString); // NOI18N
            }
            return coredumpsStorageDirectory;
        }
    }
    
    public static boolean storageDirectoryExists() {
        return new File(getStorageDirectoryString()).isDirectory();
    }

    
    public static void register() {
        if (Utilities.isWindows()) return;
        
        DataSourceDescriptorFactory.getDefault().registerProvider(new CoreDumpDescriptorProvider());
        CoreDumpsContainer.sharedInstance();
        CoreDumpProvider.register();
        RegisteredSnapshotCategories.sharedInstance().registerCategory(category);
        DataSourceViewsManager.sharedInstance().addViewProvider(viewProvider, CoreDump.class);
    }

}

/*
 *  Copyright 2007-2008 Sun Microsystems, Inc.  All Rights Reserved.
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

package com.sun.tools.visualvm.jmx;

import com.sun.tools.visualvm.application.Application;
import com.sun.tools.visualvm.core.datasource.Storage;
import com.sun.tools.visualvm.core.datasupport.Utils;
import com.sun.tools.visualvm.jmx.impl.JmxApplicationProvider;
import java.io.File;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.profiler.NetBeansProfiler;
import org.openide.util.NbBundle;

/**
 * Support for creating JMX applications in VisualVM.
 *
 * @since 1.1
 * @author Jiri Sedlacek
 */
public final class JmxApplicationsSupport {
    
    private static final String STORAGE_DIRNAME = "jmxapplications";    // NOI18N
    
    private static final Object storageDirectoryLock = new Object();
    // @GuardedBy storageDirectoryLock
    private static File storageDirectory;
    private static final Object storageDirectoryStringLock = new Object();
    // @GuardedBy storageDirectoryStringLock
    private static String storageDirectoryString;

    private static JmxApplicationsSupport instance;

    private JmxApplicationProvider applicationProvider = new JmxApplicationProvider();


    /**
     * Returns singleton instance of JmxApplicationsSupport.
     *
     * @return singleton instance of JmxApplicationsSupport.
     */
    public static synchronized JmxApplicationsSupport getInstance() {
        if (instance == null) instance = new JmxApplicationsSupport();
        return instance;
    }


    /**
     * Creates new Application defined by JMX connection and adds it to the
     * Applications tree. The application won't be restored on another VisualVM
     * sessions. Throws a JmxApplicationException if the application cannot be created.
     *
     * Note that even though the created application won't be restored for another
     * VisualVM sessions, the host created for this application will be restored.
     *
     * @param connectionString definition of the connection, for example hostname:port
     * @param displayName display name for the application, may be null
     * @param username username for the connection, may be null
     * @param password password for the connection, may be null
     * @return created JMX application
     * @throws JmxApplicationException if creating the application failed
     */
    public Application createJmxApplication(String connectionString, String displayName,
                                            String username, String password) throws JmxApplicationException {

        return createJmxApplication(connectionString, displayName, username,
                                    password, false, false);
    }

    /**
     * Creates new Application defined by JMX connection and adds it to the
     * Applications tree. Throws a JmxApplicationException if the application
     * cannot be created.
     *
     * Note that even if the created application isn't persistent for another
     * VisualVM sessions, the host created for this application will be restored.
     *
     * @param connectionString definition of the connection, for example hostname:port
     * @param displayName display name for the application, may be null
     * @param username username for the connection, may be null
     * @param password password for the connection, may be null
     * @param saveCredentials if persistent, controls whether the username and password should be persisted for another VisualVM sessions
     * @param persistent controls whether the application definition will be persisted for another VisualVM sessions
     * @return created JMX application
     * @throws JmxApplicationException if creating the application failed
     */
    public Application createJmxApplication(String connectionString,
                                            String displayName, String username,
                                            String password, boolean saveCredentials,
                                            boolean persistent) throws JmxApplicationException {

        if (username == null) username = ""; // NOI18N
        if (password == null) password = ""; // NOI18N
        if (displayName == null)
            displayName = (username.isEmpty() ? "" : username + "@") + connectionString; // NOI18N

        EnvironmentProvider epr = JmxEnvironmentSupport.getInstance().
                createCredentialsProvider(username, password.toCharArray(), saveCredentials);
        return this.createJmxApplicationImpl(connectionString, displayName, epr, persistent);
    }

    /**
     * Creates new Application defined by JMX connection and adds it to the
     * Applications tree. Throws a JmxApplicationException if the application
     * cannot be created.
     *
     * @param connectionString definition of the connection, for example hostname:port
     * @param displayName display name for the application, may be null
     * @param provider JMX EnvironmentProvider for the Application
     * @param persistent controls whether the application definition will be persisted for another VisualVM sessions
     * @return created JMX application
     * @throws JmxApplicationException if creating the application failed
     *
     * @since 1.2
     */
    public Application createJmxApplication(String connectionString,
                                            String displayName,
                                            EnvironmentProvider provider,
                                            boolean persistent) throws JmxApplicationException {

        return this.createJmxApplicationImpl(connectionString, displayName, provider, persistent);
    }

    /**
     * Creates new Application defined by JMX connection and adds it to the
     * Applications tree. The application won't be restored on another VisualVM
     * sessions. Displays progress during application creation and opens an error
     * dialog if creating the application failed. Throws a JmxApplicationException
     * if the application cannot be created.
     *
     * Note that even though the created application won't be restored for another
     * VisualVM sessions, the host created for this application will be restored.
     *
     * @param connectionString definition of the connection, for example hostname:port
     * @param displayName display name for the application, may be null
     * @param username username for the connection, may be null
     * @param password password for the connection, may be null
     * @return created JMX application or null if creating the application failed
     * @throws JmxApplicationException if creating the application failed
     */
    public Application createJmxApplicationInteractive(String connectionString, String displayName,
                                            String username, String password) {

        return createJmxApplicationInteractive(connectionString, displayName, username,
                                    password, false, false);
    }

    /**
     * Creates new Application defined by JMX connection and adds it to the
     * Applications tree. Displays progress during application creation and
     * opens an error dialog if creating the application failed.
     *
     * Note that even if the created application isn't persistent for another
     * VisualVM sessions, the host created for this application will be restored.
     *
     * @param connectionString definition of the connection, for example hostname:port
     * @param displayName display name for the application, may be null
     * @param username username for the connection, may be null
     * @param password password for the connection, may be null
     * @param saveCredentials if persistent, controls whether the username and password should be persisted for another VisualVM sessions
     * @param persistent controls whether the application definition will be persisted for another VisualVM sessions
     * @return created JMX application or null if creating the application failed
     */
    public Application createJmxApplicationInteractive(String connectionString,
                                            String displayName, String username,
                                            String password, boolean saveCredentials,
                                            boolean persistent) {

        if (username == null) username = ""; // NOI18N
        if (password == null) password = ""; // NOI18N
        if (displayName == null)
            displayName = (username.isEmpty() ? "" : username + "@") + connectionString; // NOI18N
        
        final ProgressHandle[] pHandle = new ProgressHandle[1];
        try {
            final String displayNameF = displayName;
            SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        pHandle[0] = ProgressHandleFactory.createHandle(
                                NbBundle.getMessage(JmxApplicationsSupport.class,
                                                    "LBL_Adding", displayNameF)); // NOI18N
                        pHandle[0].setInitialDelay(0);
                        pHandle[0].start();
                    }
                });
            EnvironmentProvider epr = JmxEnvironmentSupport.getInstance().
                createCredentialsProvider(username, password.toCharArray(), saveCredentials);
            return createJmxApplicationImpl(connectionString, displayName,
                                            epr, persistent);
        } catch (JmxApplicationException e) {
            NetBeansProfiler.getDefaultNB().displayError(e.getMessage());
        } finally {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if (pHandle[0] != null) {
                        pHandle[0].finish();
                    }
                }
            });
        }
        return null;
    }

    /**
     * Creates new Application defined by JMX connection and adds it to the
     * Applications tree. Displays progress during application creation and
     * opens an error dialog if creating the application failed.
     *
     * Note that even if the created application isn't persistent for another
     * VisualVM sessions, the host created for this application will be restored.
     *
     * @param connectionString definition of the connection, for example hostname:port
     * @param displayName display name for the application, may be null
     * @param provider JMX EnvironmentProvider for the Application
     * @param persistent controls whether the application definition will be persisted for another VisualVM sessions
     * @return created JMX application or null if creating the application failed
     *
     * @since 1.2
     */
    public Application createJmxApplicationInteractive(String connectionString,
                                            String displayName,
                                            EnvironmentProvider provider,
                                            boolean persistent) {

        final ProgressHandle[] pHandle = new ProgressHandle[1];
        try {
            final String displayNameF = displayName;
            SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        pHandle[0] = ProgressHandleFactory.createHandle(
                                NbBundle.getMessage(JmxApplicationsSupport.class,
                                                    "LBL_Adding", displayNameF)); // NOI18N
                        pHandle[0].setInitialDelay(0);
                        pHandle[0].start();
                    }
                });
            return createJmxApplicationImpl(connectionString, displayName,
                                            provider, persistent);
        } catch (JmxApplicationException e) {
            NetBeansProfiler.getDefaultNB().displayError(e.getMessage());
        } finally {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if (pHandle[0] != null) {
                        pHandle[0].finish();
                    }
                }
            });
        }
        return null;
    }

    private Application createJmxApplicationImpl(String connectionString,
                                            String displayName,
                                            EnvironmentProvider provider,
                                            boolean persistent)
                                            throws JmxApplicationException {

        return applicationProvider.createJmxApplication(connectionString,
                                            displayName, provider, persistent);
    }
    
    
    static String getStorageDirectoryString() {
        synchronized(storageDirectoryStringLock) {
            if (storageDirectoryString == null)
                storageDirectoryString = Storage.getPersistentStorageDirectoryString() + File.separator + STORAGE_DIRNAME;
            return storageDirectoryString;
        }
    }

    /**
     * Returns storage directory for defined JMX applications.
     *
     * @return storage directory for defined JMX applications.
     */
    public static File getStorageDirectory() {
        synchronized(storageDirectoryLock) {
            if (storageDirectory == null) {
                String storageString = getStorageDirectoryString();
                storageDirectory = new File(storageString);
                if (storageDirectory.exists() && storageDirectory.isFile())
                    throw new IllegalStateException("Cannot create hosts storage directory " + storageString + ", file in the way");    // NOI18N
                if (storageDirectory.exists() && (!storageDirectory.canRead() || !storageDirectory.canWrite())) 
                    throw new IllegalStateException("Cannot access hosts storage directory " + storageString + ", read&write permission required"); // NOI18N
                if (!Utils.prepareDirectory(storageDirectory))
                    throw new IllegalStateException("Cannot create hosts storage directory " + storageString);  // NOI18N
            }
            return storageDirectory;
        }
    }

    /**
     * Returns true if the storage directory for defined JMX applications already exists, false otherwise.
     *
     * @return true if the storage directory for defined JMX applications already exists, false otherwise.
     */
    public static boolean storageDirectoryExists() {
        return new File(getStorageDirectoryString()).isDirectory();
    }


    private JmxApplicationsSupport() {
        applicationProvider.initialize();
    }
}

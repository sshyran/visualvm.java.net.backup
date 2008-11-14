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

package com.sun.tools.visualvm.tools.jmx;

import com.sun.tools.visualvm.core.datasupport.AsyncPropertyChangeSupport;
import com.sun.tools.visualvm.core.model.Model;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.management.MBeanServerConnection;
import javax.management.remote.JMXServiceURL;

/**
 * <p>This class encapsulates the JMX functionality of the target Java application.</p>
 *
 * <p>Call {@link JmxModelFactory#getJmxModelFor()} to get an instance of the
 * {@link JmxModel} class.</p>
 *
 * <p>Usually this class will be used as follows:</p>
 *
 * <pre>
 * JmxModel jmx = JmxModelFactory.getJmxModelFor(application);
 * if (jmx == null || jmx.getConnectionState() != JmxModel.ConnectionState.CONNECTED) {
 *     // JMX connection not available...
 * } else {
 *     MBeanServerConnection mbsc = jmx.getMBeanServerConnection();
 *     if (mbsc != null) {
 *         // Invoke JMX operations...
 *     }
 * }
 * </pre>
 *
 * <p>Any of the {@link CachedMBeanServerConnectionFactory CachedMBeanServerConnectionFactory.getCachedMBeanServerConnection}
 * methods can be used to work with a {@link CachedMBeanServerConnection} instead of a plain {@link MBeanServerConnection}.</p>
 *
 * <p>In case the JMX connection is not established yet, you could register
 * a listener on the {@code JmxModel} for ConnectionState property changes.
 * The JmxModel notifies any PropertyChangeListeners about the ConnectionState
 * property change to CONNECTED and DISCONNECTED. The JmxModel instance will
 * be the source for any generated events.</p>
 *
 * <p>Polling for the ConnectionState is also possible by calling
 * {@link JmxModel#getConnectionState()}.</p>
 *
 * @author Luis-Miguel Alventosa
 */
public abstract class JmxModel extends Model {

    protected PropertyChangeSupport propertyChangeSupport =
            new AsyncPropertyChangeSupport(this);
    /**
     * The {@link ConnectionState ConnectionState} bound property name.
     */
    public static final String CONNECTION_STATE_PROPERTY = "connectionState"; // NOI18N

    /**
     * Values for the {@linkplain #CONNECTION_STATE_PROPERTY
     * <i>ConnectionState</i>} bound property.
     */
    public enum ConnectionState {
        /**
         * The connection has been successfully established.
         */
        CONNECTED,
        /**
         * No connection present.
         */
        DISCONNECTED,
        /**
         * The connection is being attempted.
         */
        CONNECTING
    }

    /**
     * Add a {@link java.beans.PropertyChangeListener PropertyChangeListener}
     * to the listener list.
     * The listener is registered for all properties.
     * The same listener object may be added more than once, and will be called
     * as many times as it is added.
     * If {@code listener} is {@code null}, no exception is thrown and
     * no action is taken.
     *
     * @param listener the {@code PropertyChangeListener} to be added.
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Removes a {@link java.beans.PropertyChangeListener PropertyChangeListener}
     * from the listener list. This
     * removes a {@code PropertyChangeListener} that was registered for all
     * properties. If {@code listener} was added more than once to the same
     * event source, it will be notified one less time after being removed. If
     * {@code listener} is {@code null}, or was never added, no exception is
     * thrown and no action is taken.
     *
     * @param listener the {@code PropertyChangeListener} to be removed.
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    /**
     * Returns the current connection state.
     *
     * @return the current connection state.
     */
    public abstract ConnectionState getConnectionState();

    /**
     * Returns the {@link MBeanServerConnection MBeanServerConnection} for the
     * connection to an application. The returned {@code MBeanServerConnection}
     * object becomes invalid when the connection state is changed to the
     * {@link ConnectionState#DISCONNECTED DISCONNECTED} state.
     *
     * @return the {@code MBeanServerConnection} for the
     * connection to an application. It returns {@code null}
     * if the JMX connection couldn't be established.
     */
    public abstract MBeanServerConnection getMBeanServerConnection();

    /**
     * Returns the {@link JMXServiceURL} associated to this (@code JmxModel}.
     *
     * @return the {@link JMXServiceURL} associated to this (@code JmxModel}.
     */
    public abstract JMXServiceURL getJMXServiceURL();
}

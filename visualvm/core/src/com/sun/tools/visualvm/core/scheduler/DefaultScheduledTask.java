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
package com.sun.tools.visualvm.core.scheduler;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


/**
 * A default implementation of the <code>ScheduledTask</code>
 * @author Jaroslav Bachorik
 */
class DefaultScheduledTask implements ScheduledTask, SchedulerTask {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private final ReadWriteLock intervalLock = new ReentrantReadWriteLock();

    // @GuardedBy intervalLock
    private Quantum interval;
    private SchedulerTask delegateTask;
    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public DefaultScheduledTask(Quantum interval, SchedulerTask task) {
        delegateTask = task;
        setInterval(interval);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------
    /**
     * @see com.sun.tools.visualvm.core.scheduler.ScheduledTask#setInterval(Quantum)
     */
    public void setInterval(Quantum interval) {
        intervalLock.writeLock().lock();

        Quantum oldInterval = interval;

        try {
            oldInterval = this.interval;
            this.interval = interval;
        } finally {
            intervalLock.writeLock().unlock();
        }

        pcs.firePropertyChange(INTERVAL_PROPERTY, oldInterval, interval);
    }

    /**
     * @see com.sun.tools.visualvm.core.scheduler.ScheduledTask#getInterval()
     */
    public Quantum getInterval() {
        intervalLock.readLock().lock();

        try {
            return interval;
        } finally {
            intervalLock.readLock().unlock();
        }
    }

    public synchronized void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(propertyName, listener);
    }

    public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public synchronized boolean hasListeners(String propertyName) {
        return pcs.hasListeners(propertyName);
    }

    public synchronized void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(propertyName, listener);
    }

    public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    /**
     * @see com.sun.tools.visualvm.core.scheduler.ScheduledTask#suspend()
     */
    public void suspend() {
        setInterval(Quantum.SUSPENDED);
    }
    
    /**
     * @see com.sun.tools.visualvm.core.scheduler.SchedulerTask#onSchedule(long)
     */
    public void onSchedule(long timeStamp) {
        delegateTask.onSchedule(timeStamp);
    }
}

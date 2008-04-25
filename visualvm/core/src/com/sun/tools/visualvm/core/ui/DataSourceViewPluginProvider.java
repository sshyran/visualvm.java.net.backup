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

package com.sun.tools.visualvm.core.ui;

import com.sun.tools.visualvm.core.datasource.DataSource;
import com.sun.tools.visualvm.core.snapshot.Snapshot;
import java.util.HashMap;
import java.util.Map;

/**
 * Definition of a plugin which can modify pluggable DataSourceView.
 *
 * @author Jiri Sedlacek
 */
public abstract class DataSourceViewPluginProvider<X extends DataSource> {
    
    private final Map<X, DataSourceViewPlugin> pluginsCache = new HashMap();
    
    
    protected abstract boolean supportsPluginFor(X dataSource);

    protected abstract DataSourceViewPlugin createPlugin(X dataSource);
    
    protected boolean supportsSavePluginFor(X dataSource, Class<? extends Snapshot> snapshotClass) { return false; };
    
    protected void savePlugin(X dataSource, Snapshot snapshot) {};
    
    
    protected final DataSourceViewPlugin getCachedPlugin(X dataSource) {
        synchronized(pluginsCache) {
            return pluginsCache.get(dataSource);
        }
    }
    
    
    void pluginWillBeAdded(DataSourceViewPlugin plugin) {
    }
    
    void pluginAdded(DataSourceViewPlugin plugin) {
    }
    
    void pluginRemoved(DataSourceViewPlugin plugin) {
        synchronized(pluginsCache) {
            pluginsCache.remove((X)plugin.getDataSource());
        }
    }
    
    protected final DataSourceViewPlugin getPlugin(X dataSource) {
        synchronized(pluginsCache) {
            DataSourceViewPlugin plugin = getCachedPlugin(dataSource);
            if (plugin == null) {
                plugin = createPlugin(dataSource);
                if (plugin == null) throw new NullPointerException("DataSourceViewPluginProvider provides null plugin: " + this);   // NOI18N
                plugin.setController(this);
                pluginsCache.put(dataSource, plugin);
            }
            return plugin;
        }
    }

}

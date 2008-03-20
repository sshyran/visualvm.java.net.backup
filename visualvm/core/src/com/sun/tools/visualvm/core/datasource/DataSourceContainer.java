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

package com.sun.tools.visualvm.core.datasource;

import java.util.Set;

/**
 * Default implementation of DataSourceContainer.
 * This class implements all neccessary methods to act as a repository of a DataSource.
 *
 * @author Jiri Sedlacek
 */
public final class DataSourceContainer extends DataSourceProvider {
    
    private final DataSource owner;
    
    
    /**
     * Default implementation of DataSourceContainer.
     * DataSoures can benefit from using this class which implements
     * managing created DataSource instances and firing the events to listeners
     * as their repositories.
     * 
     * @param owner
     */
    DataSourceContainer(DataSource owner) {
        this.owner = owner;
    }
    

    public void addDataSource(DataSource added) {
        registerDataSource(added);
    }

    public void addDataSources(Set<? extends DataSource> added) {
        super.registerDataSources(added);
    }

    public void removeDataSource(DataSource removed) {
        unregisterDataSource(removed);
    }

    public void removeDataSources(Set<? extends DataSource> removed) {
        super.unregisterDataSources(removed);
    }
    
    public void updateDataSources(Set<? extends DataSource> added, Set<? extends DataSource> removed) {
        super.changeDataSources(added, removed);
    }
    
    
    protected void registerDataSourcesImpl(Set<? extends DataSource> added) {
        for (DataSource dataSource : added) dataSource.addImpl(owner);
        super.registerDataSourcesImpl(added);
    }
    
    protected void unregisterDataSourcesImpl(Set<? extends DataSource> removed) {
        for (DataSource dataSource : removed) {
            DataSourceContainer dataSourceRepository = dataSource.getRepository();
            dataSourceRepository.unregisterDataSourcesImpl(dataSourceRepository.getDataSources());
            dataSource.removeImpl();
        }
        super.unregisterDataSourcesImpl(removed);
    }

}

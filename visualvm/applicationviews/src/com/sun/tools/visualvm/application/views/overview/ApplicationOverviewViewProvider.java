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
import com.sun.tools.visualvm.application.ApplicationSnapshot;
import com.sun.tools.visualvm.core.snapshot.Snapshot;
import com.sun.tools.visualvm.core.ui.DataSourceView;
import com.sun.tools.visualvm.core.ui.PluggableDataSourceViewProvider;
import java.util.Set;

/**
 *
 * @author Jiri Sedlacek
 */
public class ApplicationOverviewViewProvider extends PluggableDataSourceViewProvider<Application> {
    
    protected boolean supportsViewFor(Application application) {
        return true;
    }

    protected DataSourceView createView(Application application) {
        return new ApplicationOverviewView(application, ApplicationOverviewModel.create(application));
    }
    
    public Set<Integer> getPluggableLocations(DataSourceView view) {
        return ALL_LOCATIONS;
    }

    protected boolean supportsSaveViewFor(Application application, Class<? extends Snapshot> snapshotClass) {
        return ApplicationSnapshot.class.isAssignableFrom(snapshotClass);
    }
    
    protected void saveView(Application application, Snapshot snapshot) {
        ApplicationOverviewView view = (ApplicationOverviewView)getCachedView(application);
        if (view != null) view.getModel().save(snapshot);
        else ApplicationOverviewModel.create(application).save(snapshot);
    }

}

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

package com.sun.tools.visualvm.host;

import com.sun.tools.visualvm.core.datasource.descriptor.DataSourceDescriptor;
import java.awt.Image;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 * DataSourceDescriptor for Host.LOCALHOST.
 * 
 * @author Jiri Sedlacek
 */
public class LocalHostDescriptor extends DataSourceDescriptor {

    private static final Image NODE_ICON = ImageUtilities.loadImage("com/sun/tools/visualvm/host/resources/localHost.png", true);    // NOI18N

    /**
     * Creates new instance of LocalHostDescriptor.
     */
    public LocalHostDescriptor() {
        super(Host.LOCALHOST, NbBundle.getMessage(LocalHostDescriptor.class, "LBL_Local"),
              NbBundle.getMessage(LocalHostDescriptor.class, "DESCR_Local"), NODE_ICON, 0,
              EXPAND_ON_FIRST_CHILD);  // NOI18N
    }

    public boolean providesProperties() {
        return true;
    }
}

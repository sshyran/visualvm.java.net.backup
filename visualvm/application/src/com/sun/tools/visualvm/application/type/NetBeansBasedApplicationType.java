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

package com.sun.tools.visualvm.application.type;

import com.sun.tools.visualvm.application.jvm.Jvm;
import com.sun.tools.visualvm.application.Application;
import java.awt.Image;
import java.util.Set;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;


/**
 * This {@link ApplicationType} represents application based on
 * NetBeans Platform.
 * @author Tomas Hurka
 */
public class NetBeansBasedApplicationType extends ApplicationType {
    Application application;
    String name;
    String branding;
    Set<String> clusters;
    
    NetBeansBasedApplicationType(Application app,Jvm jvm,Set<String> cls, String br) {
        application = app;
        clusters = cls;
        branding = br;
    }
    
    /**
     * Returns set of BetBeans' clusters.
     *
     */
    public Set<String> getClusters() {
        return clusters;
    }
    
    /**
     * {@inheritDoc}
     */
    public String getName() {
        return NbBundle.getMessage(NetBeansBasedApplicationType.class, "LBL_NbPlatformApplication"); // NOI18N
    }
    
    /**
     * {@inheritDoc}
     */
    public String getVersion() {
        return NbBundle.getMessage(NetBeansBasedApplicationType.class, "LBL_Unknown");  // NOI18N
    }
    
    /**
     * {@inheritDoc}
     */
    public String getDescription() {
        return NbBundle.getMessage(NetBeansBasedApplicationType.class, "DESCR_NetBeansBasedApplicationType"); // NOI18N
    }
    
    /**
     * {@inheritDoc}
     */
    public Image getIcon() {
        String iconPath = "com/sun/tools/visualvm/application/type/resources/NetBeansPlatform.png";   // NOI18N
        return ImageUtilities.loadImage(iconPath, true);
    }
}

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

import com.sun.tools.visualvm.application.Application;
import com.sun.tools.visualvm.core.model.AbstractModelProvider;
import com.sun.tools.visualvm.application.jvm.Jvm;
import com.sun.tools.visualvm.application.jvm.JvmFactory;

/**
 * Factory which recognizes Java-Plugin application 
 * @author Luis-Miguel Alventosa
 */
public class JavaPluginApplicationTypeFactory
        extends AbstractModelProvider<ApplicationType, Application> {

    private static final String JAVA_PLUGIN = "-Djavaplugin.version=";  // NOI18N

    /**
     * Detects Java-Plugin application.
     * @return {@link JavaPluginApplicationType} instance or <code>null</code>
     * if application is not Java-Plugin application
     * @param application Application
     */
    @Override
    public ApplicationType createModelFor(Application application) {
        Jvm jvm = JvmFactory.getJVMFor(application);
        if (jvm.isBasicInfoSupported()) {
            String args = jvm.getJvmArgs();
            int plugin_index = args.indexOf(JAVA_PLUGIN);
            if (plugin_index != -1) {
                String version;
                int version_index = plugin_index + JAVA_PLUGIN.length();
                int space_index = args.indexOf(' ', version_index);
                if (space_index != -1) {
                    version = args.substring(version_index, space_index);
                } else {
                    version = args.substring(version_index);
                }
                return new JavaPluginApplicationType(version);
            }
        }
        return null;
    }
}

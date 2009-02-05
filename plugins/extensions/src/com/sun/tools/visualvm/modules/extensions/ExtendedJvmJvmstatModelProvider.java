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

package com.sun.tools.visualvm.modules.extensions;

import com.sun.tools.visualvm.core.model.AbstractModelProvider;
import com.sun.tools.visualvm.application.Application;
import com.sun.tools.visualvm.tools.jvmstat.JvmstatModel;
import com.sun.tools.visualvm.tools.jvmstat.JvmstatModelFactory;
import com.sun.tools.visualvm.tools.jvmstat.JvmJvmstatModel;

/**
 * Support additional JVMs.
 *
 * @author Luis-Miguel Alventosa
 * @author Tomas Hurka
 */
public class ExtendedJvmJvmstatModelProvider extends AbstractModelProvider<JvmJvmstatModel, Application> {

    public JvmJvmstatModel createModelFor(Application app) {
        JvmstatModel jvmstat = JvmstatModelFactory.getJvmstatFor(app);
        if (jvmstat != null) {
            String vmVersion = jvmstat.findByName("java.property.java.vm.version"); // NOI18N
            if (vmVersion != null) {
                JvmJvmstatModel model = null;
                
                // Hotspot Express
                if (vmVersion.startsWith("10.")) model = new ExtendedJvmJvmstatModel(app,jvmstat); // NOI18N
                else if (vmVersion.startsWith("11.")) model = new ExtendedJvmJvmstatModel(app,jvmstat); // NOI18N
                else if (vmVersion.startsWith("12.")) model = new ExtendedJvmJvmstatModel(app,jvmstat); // NOI18N
                else if (vmVersion.startsWith("13.")) model = new ExtendedJvmJvmstatModel(app,jvmstat); // NOI18N
                else if (vmVersion.startsWith("14.")) model = new ExtendedJvmJvmstatModel(app,jvmstat); // NOI18N
                
                return model;
            }
        }
        return null;
    }

    public int priority() {
        return 3;
    }

}

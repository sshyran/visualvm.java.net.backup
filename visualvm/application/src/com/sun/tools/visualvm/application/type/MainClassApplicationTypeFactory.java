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

import com.sun.tools.visualvm.core.model.AbstractModelProvider;
import com.sun.tools.visualvm.application.jvm.Jvm;
import com.sun.tools.visualvm.application.jvm.JvmFactory;
import com.sun.tools.visualvm.application.Application;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Tomas Hurka
 */
public class MainClassApplicationTypeFactory extends AbstractModelProvider<ApplicationType,Application> {
    private static final int CLASS_NAME = 0;
    private static final int NAME = 1;
    private static final int ICON_PATH = 2;

    private String[][] appmatrix = {
        // build tools
        {"org.apache.tools.ant.launch.Launcher","Ant","com/sun/tools/visualvm/application/resources/application.png"},  // NOI18N

        // Application servers
        {"com.sun.enterprise.server.PELaunch","GlassFish","com/sun/tools/visualvm/application/type/resources/GlassFish.png"},   // NOI18N
        {"com.sun.enterprise.ee.nodeagent.NodeAgentMain", "GlassFish Node", "com/sun/tools/visualvm/application/type/resources/GlassFish.png"}, // NOI18N
        {"org.apache.catalina.startup.Bootstrap","Tomcat","com/sun/tools/visualvm/application/type/resources/Tomcat.png"},  // NOI18N
        {"org.jboss.Main","JBoss","com/sun/tools/visualvm/application/resources/application.png"},  // NOI18N
        
        // JDK tools
        {"sun.tools.jconsole.JConsole","JConsole","com/sun/tools/visualvm/application/resources/application.png"},  // NOI18N
        {"sun.tools.jps.Jps","Jps","com/sun/tools/visualvm/application/resources/application.png"}, // NOI18N
        {"sun.tools.jstat.Jstat","Jstat","com/sun/tools/visualvm/application/resources/application.png"},   // NOI18N
        {"sun.tools.jstatd.Jstatd","Jstatd","com/sun/tools/visualvm/application/resources/application.png"},    // NOI18N
        {"sun.jvm.hotspot.tools.JStack","JStack","com/sun/tools/visualvm/application/resources/application.png"},   // NOI18N
        {"sun.tools.jstack.JStack","JStack","com/sun/tools/visualvm/application/resources/application.png"},    // NOI18N
        {"sun.jvm.hotspot.tools.JMap","JMap","com/sun/tools/visualvm/application/resources/application.png"},   // NOI18N
        {"sun.tools.jmap.JMap","JMap","com/sun/tools/visualvm/application/resources/application.png"},  // NOI18N
        {"com.sun.tools.hat.Main","JHat","com/sun/tools/visualvm/application/resources/application.png"},   // NOI18N
        {"sun.tools.jinfo.JInfo","JInfo","com/sun/tools/visualvm/application/resources/application.png"},   // NOI18N
        {"sun.jvm.hotspot.jdi.SADebugServer","jsadebugd","com/sun/tools/visualvm/application/resources/application.png"},   // NOI18N
        
        // JDK utilitites
        {"sun.tools.jar.Main","Jar","com/sun/tools/visualvm/application/resources/application.png"},    // NOI18N
        {"com.sun.java.util.jar.pack.Driver","pack200","com/sun/tools/visualvm/application/resources/application.png"}, // NOI18N
        {"com.sun.tools.javadoc.Main","JavaDoc","com/sun/tools/visualvm/application/resources/application.png"},    // NOI18N
        {"com.sun.tools.javac.Main","Javac","com/sun/tools/visualvm/application/resources/application.png"},    // NOI18N
        {"com.sun.tools.javah.Main","Javah","com/sun/tools/visualvm/application/resources/application.png"},    // NOI18N
        {"sun.tools.javap.Main","Javap","com/sun/tools/visualvm/application/resources/application.png"},    // NOI18N
        {"sun.security.tools.JarSigner","JarSigner","com/sun/tools/visualvm/application/resources/application.png"},    // NOI18N
        {"com.sun.tools.apt.Main","APT","com/sun/tools/visualvm/application/resources/application.png"},    // NOI18N
        
        // Java DB
        {"org.apache.derby.drda.NetworkServerControl", "JavaDB", "com/sun/tools/visualvm/application/type/resources/JavaDB.png"},   // NOI18N
    };
    
    Map<String,String[]> map;
    
    protected MainClassApplicationTypeFactory() {
        map = new HashMap();
        for (int i = 0; i < appmatrix.length; i++) {
            String[] appDesc = appmatrix[i];
            
            map.put(appDesc[CLASS_NAME],appDesc);
        }
    }
    
    public ApplicationType createModelFor(Application appl) {
        Jvm jvm = JvmFactory.getJVMFor(appl);
            
        if (jvm.isBasicInfoSupported()) {
            String mainClass = jvm.getMainClass();
            if (mainClass != null) {
                return createApplicationTypeFor(appl,jvm,mainClass);
            }
        }
        return null;
    }
    
    public ApplicationType createApplicationTypeFor(Application app, Jvm jvm, String mainClass) {
        String[] appDesc = map.get(mainClass);
        if (appDesc != null) {
            return new MainClassApplicationType(app,appDesc[NAME],appDesc[ICON_PATH]);
        }
        return null;
    }
}

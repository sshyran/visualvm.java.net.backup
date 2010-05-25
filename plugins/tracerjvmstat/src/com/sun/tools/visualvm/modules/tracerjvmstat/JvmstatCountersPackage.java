/*
 *  Copyright 2007-2010 Sun Microsystems, Inc.  All Rights Reserved.
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

package com.sun.tools.visualvm.modules.tracerjvmstat;

import com.sun.management.UnixOperatingSystemMXBean;
import com.sun.tools.visualvm.application.Application;
import com.sun.tools.visualvm.modules.tracer.TracerPackage;
import com.sun.tools.visualvm.modules.tracer.TracerProbe;
import com.sun.tools.visualvm.modules.tracer.TracerProbeDescriptor;
import com.sun.tools.visualvm.modules.tracer.TracerProgressObject;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.openide.util.ImageUtilities;
import sun.jvmstat.monitor.Monitor;
import sun.jvmstat.monitor.MonitoredHost;
import sun.jvmstat.monitor.MonitoredVm;
import sun.jvmstat.monitor.MonitorException;
import sun.management.counter.Units;
import sun.management.counter.Variability;
import sun.jvmstat.monitor.VmIdentifier;

/**
 *
 * @author Tomas Hurka
 */
class JvmstatCountersPackage extends TracerPackage.SessionAware<Application> {
    
    private static final Logger LOGGER = Logger.getLogger(JvmstatCountersPackage.class.getName());
    static final Icon ICON = new ImageIcon(ImageUtilities.loadImage(
            "com/sun/tools/visualvm/modules/tracer/monitor/resources/monitor.png", true)); // NOI18N
    private static final String NAME = "jvmstat counters metrics";
    private static final String DESCR = "Provides metric for jvmstat counters.";
    private static final int POSITION = 70;
    
    private Map<TracerProbeDescriptor,TracerProbe<Application>> probes;
    private JvmstatCountersPackages master;
    
    JvmstatCountersPackage(JvmstatCountersPackages m, String name,int pos) {
        super(name+" "+NAME, DESCR, ICON, POSITION+pos);
        probes = new HashMap();
        master = m;
    }
    
    public TracerProbeDescriptor[] getProbeDescriptors() {
        return probes.keySet().toArray(new TracerProbeDescriptor[probes.size()]);
    }
    
    public TracerProbe<Application> getProbe(TracerProbeDescriptor descriptor) {
        return probes.get(descriptor);
    }

    void addProbe(Monitor monitor, int pos, String probeName) {
        String descName = "Jvmstat counter "+monitor.getName()+" Units: "+monitor.getUnits();
        TracerProbeDescriptor desc = new TracerProbeDescriptor(probeName,descName,ICON,pos,true);
        probes.put(desc,new JvmstatCounterProbe(probeName, descName, monitor));
    }

    protected TracerProgressObject sessionInitializing(TracerProbe<Application>[] probes, Application dataSource, int refresh) {
        master.setInterval(refresh*1000);
        return null;
    }

    protected void refreshRateChanged(TracerProbe<Application>[] probes, Application dataSource, int refresh) {
        master.setInterval(refresh*1000);
    }

    protected void sessionStopping(TracerProbe<Application>[] probes, Application dataSource) {
    }


}

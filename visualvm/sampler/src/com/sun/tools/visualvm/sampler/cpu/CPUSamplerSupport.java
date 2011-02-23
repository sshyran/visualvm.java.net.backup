/*
 * Copyright (c) 2007, 2011, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 * 
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
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
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package com.sun.tools.visualvm.sampler.cpu;

import com.sun.tools.visualvm.core.options.GlobalPreferences;
import com.sun.tools.visualvm.core.ui.components.DataViewComponent;
import com.sun.tools.visualvm.sampler.AbstractSamplerSupport;
import java.lang.management.ThreadInfo;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.SwingUtilities;
import org.netbeans.lib.profiler.common.ProfilingSettings;
import org.netbeans.lib.profiler.common.filters.SimpleFilter;
import org.netbeans.lib.profiler.global.InstrumentationFilter;
import org.netbeans.lib.profiler.results.cpu.StackTraceSnapshotBuilder;
import org.openide.util.NbBundle;

/**
 *
 * @author Jiri Sedlacek
 * @author Tomas Hurka
 */
public abstract class CPUSamplerSupport extends AbstractSamplerSupport {

    private final ThreadInfoProvider threadInfoProvider;
    private final SnapshotDumper snapshotDumper;
    private final ThreadDumper threadDumper;

    private Timer timer;
    private TimerTask samplerTask;
    private final Refresher refresher;
    private int refreshRate;

    private StackTraceSnapshotBuilder builder;

    private volatile boolean sampleRunning;
    private final Object updateLock = new Object();
    private long currentLiveUpdate;
    private long lastLiveUpdate;

    private CPUView cpuView;
    private DataViewComponent.DetailsView[] detailsViews;


    public CPUSamplerSupport(ThreadInfoProvider tip, SnapshotDumper snapshotDumper, ThreadDumper threadDumper) {
        threadInfoProvider = tip;
        this.snapshotDumper = snapshotDumper;
        this.threadDumper = threadDumper;

        refreshRate = GlobalPreferences.sharedInstance().getMonitoredDataPoll() * 1000;

        refresher = new Refresher() {
            public void setRefreshRate(int rr) {
                CPUSamplerSupport.this.refreshRate = rr;
            }
            public int getRefreshRate() {
                return CPUSamplerSupport.this.refreshRate;
            }
            protected boolean checkRefresh() {
                return samplerTask != null && cpuView.isShowing();
            }
            protected void doRefresh() {
                doRefreshImpl();
            }
        };
    }


    public DataViewComponent.DetailsView[] getDetailsView() {
        if (detailsViews == null) {
            cpuView = new CPUView(refresher, snapshotDumper, threadDumper);
                        detailsViews = new DataViewComponent.DetailsView[] {
                new DataViewComponent.DetailsView(NbBundle.getMessage(
                CPUSamplerSupport.class, "LBL_Cpu_samples"), null, 10, cpuView, null) }; // NOI18N
        }

        cpuView.initSession();
        return detailsViews.clone();
    }

    public boolean startSampling(ProfilingSettings settings, int samplingRate, int refreshRate) {
        InstrumentationFilter filter = new InstrumentationFilter();
        SimpleFilter sf = (SimpleFilter)settings.getSelectedInstrumentationFilter();
        filter.setFilterStrings(sf.getFilterValue());
        filter.setFilterType(convertFilterType(sf.getFilterType()));
        builder = new StackTraceSnapshotBuilder(1, filter);
        snapshotDumper.builder = builder;
        
        refresher.setRefreshRate(refreshRate);

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                cpuView.setResultsPanel(new SampledLivePanel(builder));
            }
        });

        if (timer == null) timer = getTimer();
        samplerTask = new SamplerTask(builder);

        try {
            timer.scheduleAtFixedRate(samplerTask, 0, samplingRate);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public synchronized void stopSampling() {
        if (samplerTask != null) {
            samplerTask.cancel();
            samplerTask = null;
        }
    }

    public synchronized void terminate() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (cpuView != null) cpuView.terminate();
    }


    private void doRefreshImpl() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (samplerTask == null) return;
                if (!sampleRunning) {
                    synchronized (updateLock) {
                        lastLiveUpdate = currentLiveUpdate;
                        cpuView.refresh();
                    }
                } else {
                    SwingUtilities.invokeLater(this);
                }
            }
        });
    }

    private int convertFilterType(int simpleFilterrType) {
        if (simpleFilterrType == SimpleFilter.SIMPLE_FILTER_NONE) {
            return InstrumentationFilter.INSTR_FILTER_NONE;
        }
        if (simpleFilterrType == SimpleFilter.SIMPLE_FILTER_EXCLUSIVE) {
            return InstrumentationFilter.INSTR_FILTER_EXCLUSIVE;
        }
        if (simpleFilterrType == SimpleFilter.SIMPLE_FILTER_INCLUSIVE) {
            return InstrumentationFilter.INSTR_FILTER_INCLUSIVE;
        }
        throw new IllegalArgumentException("type "+simpleFilterrType); // NOI18N
    }


    private class SamplerTask extends TimerTask {

        private final StackTraceSnapshotBuilder builder;
        private final Set samplingThreads = new HashSet();

        public SamplerTask(StackTraceSnapshotBuilder builder) {
            this.builder = builder;
        }

        public void run() {
            if (sampleRunning) return;
            synchronized (updateLock) {
                sampleRunning = true;
                try {
                    ThreadInfo[] infos = threadInfoProvider.dumpAllThreads();
                    long timestamp = System.nanoTime();
                    String samplingThreadName = findSamplingThread(infos);
                    if (samplingThreadName != null) {
                        if (samplingThreads.add(samplingThreadName)) {
//                                System.out.println("New ignored thread: "+samplingThreadName);
                            builder.setIgnoredThreads(samplingThreads);
                        }
                    }
                    builder.addStacktrace(infos, timestamp);

                    currentLiveUpdate = timestamp / 1000000;
                    if (currentLiveUpdate - lastLiveUpdate >= refreshRate)
                        refresher.refresh();

                } catch (Throwable ex) {
                    terminate();
                } finally {
                    sampleRunning = false;
                }
            }
        }

        private String findSamplingThread(ThreadInfo[] infos) {
//                for (ThreadInfo info : infos) {
//                    if (info.getThreadState() == Thread.State.RUNNABLE) {
//                        StackTraceElement[] stack = info.getStackTrace();
//
//                        if (stack.length > 0) {
//                            StackTraceElement topStack = stack[0];
//
//                            if (!topStack.isNativeMethod()) {
//                                continue;
//                            }
//                            if (!"sun.management.ThreadImpl".equals(topStack.getClassName())) {  // NOI18N
//                                continue;
//                            }
//                            if ("getThreadInfo0".equals(topStack.getMethodName())) {
//                                return info.getThreadName();
//                            }
//                        }
//                    }
//                }
            return null;
        }
    }
    
    public static abstract class ThreadDumper {
        public abstract void takeThreadDump(boolean openView);
    }

    public static abstract class SnapshotDumper {
        protected StackTraceSnapshotBuilder builder;
        public abstract void takeSnapshot(boolean openView);
    }

}

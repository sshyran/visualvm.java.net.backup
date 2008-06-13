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

package com.sun.tools.visualvm.modules.mbeans;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.Timer;

@SuppressWarnings("serial")
class XPlottingViewer extends PlotterPanel implements ActionListener {

    private final static Logger LOGGER = Logger.getLogger(XPlottingViewer.class.getName());

    // TODO: Make number of decimal places customizable
    private static final int PLOTTER_DECIMALS = 4;

    private JButton plotButton;
    // The plotter cache holds Plotter instances for the various attributes
    private static HashMap<String, XPlottingViewer> plotterCache =
            new HashMap<String, XPlottingViewer>();
    private static HashMap<String, Timer> timerCache =
            new HashMap<String, Timer>();
    private MBeansTab tab;
    private XMBean mbean;
    private String attributeName;
    private String key;
    private JTable table;

    private XPlottingViewer(String key,
                            XMBean mbean,
                            String attributeName,
                            Object value,
                            JTable table,
                            MBeansTab tab) {
        super(null);
        this.tab = tab;
        this.key = key;
        this.mbean = mbean;
        this.table = table;
        this.attributeName = attributeName;        
        setupDisplay(createPlotter(mbean, attributeName, key, table));
    }

    static void dispose(MBeansTab tab) {
        Iterator it = plotterCache.keySet().iterator();
        while(it.hasNext()) {
            String key = (String) it.next();
            if(key.startsWith(String.valueOf(tab.hashCode()))) {
                it.remove();
            }
        }
        //plotterCache.clear();
        it = timerCache.keySet().iterator();
        while(it.hasNext()) {
            String key = (String) it.next();
            if(key.startsWith(String.valueOf(tab.hashCode()))) {
                Timer t = timerCache.get(key);
                t.stop();
                it.remove();
            }
        }
    }

    public static boolean isViewableValue(Object value) {
        return (value instanceof Number);
    }

    // Fired by dbl click
    public static Component loadPlotting(
            XMBean mbean, String attributeName, Object value, JTable table, MBeansTab tab) {
        Component comp = null;
        if (isViewableValue(value)) {
            String key = String.valueOf(tab.hashCode()) + " " + // NOI18N
                    String.valueOf(mbean.hashCode()) + " " + // NOI18N
                    mbean.getObjectName().getCanonicalName() + attributeName;
            XPlottingViewer p = plotterCache.get(key);
            if (p == null) {
                p = new XPlottingViewer(key, mbean, attributeName, value, table, tab);
                plotterCache.put(key, p);
            }
            comp = p;
        }
        return comp;
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        plotterCache.remove(key);
        Timer t = timerCache.remove(key);
        t.stop();
        ((XMBeanAttributes) table).collapse(attributeName, this);
    }

    // Create plotter instance
    public Plotter createPlotter(final XMBean xmbean,
                                 final String attributeName,
                                 String key,
                                 JTable table) {
        final Plotter p = new XPlotter(table, Plotter.Unit.NONE) {
            Dimension prefSize = new Dimension(400, 170);
            @Override
            public Dimension getPreferredSize() {
                return prefSize;
            }
            @Override
            public Dimension getMinimumSize() {
                return prefSize;
            }
        };

        p.createSequence(attributeName, attributeName, null, true);

        Timer timer = new Timer(tab.getUpdateInterval(), new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                intervalElapsed(p);
            }
        });
        timer.setCoalesce(true);
        timer.setInitialDelay(0);
        timer.start();
        timerCache.put(key, timer);
        return p;
    }

    void intervalElapsed(final Plotter p) {
        tab.getRequestProcessor().post(new Runnable() {
            public void run() {
                try {
                    Number n = (Number) mbean.getCachedMBeanServerConnection().getAttribute(
                            mbean.getObjectName(), attributeName);
                    long v;
                    if (n instanceof Float || n instanceof Double) {
                        p.setDecimals(PLOTTER_DECIMALS);
                        double d = (n instanceof Float) ? (Float) n : (Double) n;
                        v = Math.round(d * Math.pow(10.0, PLOTTER_DECIMALS));
                    } else {
                        v = n.longValue();
                    }
                    p.addValues(System.currentTimeMillis(), v);
                } catch (Exception e) {
                    LOGGER.throwing(XPlottingViewer.class.getName(), "intervalElapsed", e); // NOI18N
                }
            }
        });
    }

    // Create Plotter display
    private void setupDisplay(Plotter p) {
        //setLayout(new GridLayout(2,0));
        GridBagLayout gbl = new GridBagLayout();
        setLayout(gbl);
        plotButton = new JButton(Resources.getText("LBL_DiscardChart")); // NOI18N
        plotButton.addActionListener(this);
        plotButton.setEnabled(true);

        // Add the display to the top four cells
        GridBagConstraints buttonConstraints = new GridBagConstraints();
        buttonConstraints.gridx = 0;
        buttonConstraints.gridy = 0;
        buttonConstraints.fill = GridBagConstraints.VERTICAL;
        buttonConstraints.anchor = GridBagConstraints.CENTER;
        gbl.setConstraints(plotButton, buttonConstraints);
        add(plotButton);

        GridBagConstraints plotterConstraints = new GridBagConstraints();
        plotterConstraints.gridx = 0;
        plotterConstraints.gridy = 1;
        plotterConstraints.weightx = 1;
        plotterConstraints.fill = GridBagConstraints.VERTICAL;
        gbl.setConstraints(p, plotterConstraints);

        setPlotter(p);
        repaint();
    }
}

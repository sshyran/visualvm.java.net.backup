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

package com.sun.tools.visualvm.core.explorer;

import com.sun.tools.visualvm.core.datasource.DataSource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

/**
 * Class for accessing the explorer tree.
 *
 * @author Jiri Sedlacek
 */
public class ExplorerSupport {

    private static ExplorerSupport sharedInstance;

    private JTree mainTree;
    
    private Set<ExplorerSelectionListener> selectionListeners = Collections.synchronizedSet(new HashSet());
    private Set<ExplorerExpansionListener> expansionListeners = Collections.synchronizedSet(new HashSet());


    /**
     * Returns singleton instance of ExplorerSupport.
     * 
     * @return singleton instance of ExplorerSupport.
     */
    public static synchronized ExplorerSupport sharedInstance() {
        if (sharedInstance == null) sharedInstance = new ExplorerSupport();
        return sharedInstance;
    }

    
    /**
     * Returns current DataSource position within its owner DataSource in explorer tree or -1 if the position cannot be determined.
     * 
     * @param dataSource DataSource for which to get the position.
     * @return current DataSource position within its owner DataSource in explorer tree or -1 if the position cannot be determined.
     */
    public int getDataSourcePosition(DataSource dataSource) {
        ExplorerNode node = getNode(dataSource);
        if (node == null) return -1;
        ExplorerNode parentNode = (ExplorerNode)node.getParent();
        if (parentNode == null) return -1;
        return parentNode.getIndex(node);
    }

    /**
     * Selects DataSource in explorer tree.
     * 
     * @param dataSource DataSource to be selected.
     */
    public void selectDataSource(final DataSource dataSource) {
        if (dataSource == null) return;
        selectDataSources(Collections.singleton(dataSource));
    }
    
    public void selectDataSources(final Set<DataSource> dataSources) {
        if (dataSources.isEmpty()) return;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                List<TreePath> selectedPaths = new ArrayList();
                for (DataSource dataSource : dataSources) {
                    ExplorerNode node = getNode(dataSource);
                    if (node != null) selectedPaths.add(getPath(node));
                }
                mainTree.setSelectionPaths(selectedPaths.isEmpty() ? null : selectedPaths.toArray(new TreePath[selectedPaths.size()]));
            } 
        });
    }
    
    /**
     * Clears selection of explorer tree.
     */
    public void clearSelection() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() { mainTree.clearSelection(); } 
        });
    }
    
    /**
     * Returns DataSource selected in explorer tree or null if no DataSource is selected.
     * 
     * @return DataSource selected in explorer tree or null if no DataSource is selected.
     */
    public DataSource getSelectedDataSource() {
        Set<DataSource> selectedDataSources = getSelectedDataSources();
        return selectedDataSources.size() == 1 ? selectedDataSources.iterator().next() : null;
    }
    
    public Set<DataSource> getSelectedDataSources() {
        TreePath[] selectedPaths = mainTree.getSelectionPaths();
        if (selectedPaths == null) return Collections.EMPTY_SET;
        
        Set<DataSource> selectedDataSources = new HashSet();
        for (TreePath treePath : selectedPaths) {
            DataSource dataSource = getDataSource(treePath);
            if (dataSource != null) selectedDataSources.add(dataSource);
        }
        return selectedDataSources;
    }
    
    /**
     * Adds a listener to receive notifications about explorer tree selection change.
     * 
     * @param listener listener to add.
     */
    public void addSelectionListener(ExplorerSelectionListener listener) {
        selectionListeners.add(listener);
    }
    
    /**
     * Removes explorer tree selection listener.
     * @param listener listener to remove.
     */
    public void removeSelectionListener(ExplorerSelectionListener listener) {
        selectionListeners.remove(listener);
    }
    
    /**
     * Expands DataSource if displayed and collapsed in explorer tree.
     * 
     * @param dataSource DataSource to expand.
     */
    public void expandDataSource(DataSource dataSource) {
        expandNode(getNode(dataSource));
    }
    
    void expandNode(final ExplorerNode node) {
        if (node == null) return;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() { mainTree.expandPath(getPath(node)); } 
        });
    }
    
    /**
     * Collapses DataSource if displayed and expanded in explorer tree.
     * 
     * @param dataSource DataSource to collapse.
     */
    public void collapseDataSource(DataSource dataSource) {
        collapseNode(getNode(dataSource));
    }
    
    void collapseNode(final ExplorerNode node) {
        if (node == null) return;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() { mainTree.collapsePath(getPath(node)); } 
        });
    }
    
    public void addExpansionListener(ExplorerExpansionListener listener) {
        expansionListeners.add(listener);
    }
    
    public void removeExpansionListener(ExplorerExpansionListener listener) {
        expansionListeners.remove(listener);
    }
    
    
    DataSource getDataSource(TreePath path) {
        if (path == null) return null;
        ExplorerNode node = (ExplorerNode)path.getLastPathComponent();
        return node.getUserObject();
    }
    
    ExplorerNode getNode(DataSource dataSource) {
        return ExplorerModelBuilder.getInstance().getNodeFor(dataSource);
    }
    
    TreePath getPath(ExplorerNode node) {
        return new TreePath(node.getPath());
    }
            
    
    private ExplorerSupport() {
        mainTree = ExplorerComponent.instance().getTree();
        mainTree.addTreeSelectionListener(new ExplorerTreeSelectionListener());
        mainTree.addTreeExpansionListener(new ExplorerTreeExpansionListener());
        OpenDataSourceSupport.getInstance().initialize();
    }
    
    
    private class ExplorerTreeSelectionListener implements TreeSelectionListener {

        public void valueChanged(TreeSelectionEvent e) {
            Set<DataSource> selectedDataSources = getSelectedDataSources();
            Set<ExplorerSelectionListener> listeners = new HashSet(selectionListeners);
            for (ExplorerSelectionListener listener : listeners) listener.selectionChanged(selectedDataSources);
        }
        
    }
    
    private class ExplorerTreeExpansionListener implements TreeExpansionListener {

        public void treeExpanded(TreeExpansionEvent event) {
            DataSource expandedDataSource = getDataSource(event.getPath());
            if (expandedDataSource != null) {
                Set<ExplorerExpansionListener> listeners = new HashSet(expansionListeners);
                for (ExplorerExpansionListener listener : listeners) listener.dataSourceExpanded(expandedDataSource);
            }
        }

        public void treeCollapsed(TreeExpansionEvent event) {
            DataSource collapsedDataSource = getDataSource(event.getPath());
            if (collapsedDataSource != null) {
                Set<ExplorerExpansionListener> listeners = new HashSet(expansionListeners);
                for (ExplorerExpansionListener listener : listeners) listener.dataSourceCollapsed(collapsedDataSource);
            }
        }
        
    }

}

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
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Factory which recognizes NetBeans IDE, NetBeans Platform based
 * application and VisualVM itself 
 * @author Tomas Hurka
 */
public class NetBeansApplicationTypeFactory extends MainClassApplicationTypeFactory {

  private static final String NETBEANS_DIRS = "-Dnetbeans.dirs="; // NOI18N
  private static final String NB_PLATFORM_HOME = "-Dnetbeans.home="; // NOI18N
  private static final String BRANDING_ID = "--branding "; // NOI18N
  private static final String VISUALVM_ID = "visualvm"; // NOI18N
  private static final String MAIN_CLASS = "org.netbeans.Main"; // NOI18N
  private static final Pattern nbcluster_pattern = Pattern.compile("nb[0-9]+\\.[0-9]+");    // NOI18N

  private boolean isNetBeans(Jvm jvm, String mainClass) {
      if (MAIN_CLASS.equals(mainClass)) {
          return true;
      }
      if (mainClass == null || mainClass.length() == 0) {    // there is no main class - detect new NB 7.0 windows launcher
          String args = jvm.getJvmArgs();
          if (args != null && args.contains(NB_PLATFORM_HOME)) {
              return true;
          }
      }
      return false;
  }
  
  protected Set<String> computeClusters(Jvm jvm) {
    String args = jvm.getJvmArgs();
    int clusterIndex = -1;
    if (args != null) {
        clusterIndex = args.indexOf(NETBEANS_DIRS);
    }
    String pathSeparator = jvm.getJavaHome().contains("\\")?";":":";    // NOI18N
    String separator = pathSeparator.equals(":")?"/":"\\";      // NOI18N
    Set<String> clusters = new HashSet();
    
    if (clusterIndex > -1) {
      String clustersString=args.substring(clusterIndex);
      int endIndex = clustersString.indexOf(" -");  // NOI18N
      Scanner clusterScanner;   
      if (endIndex > -1) {
        clustersString = clustersString.substring(0,endIndex);
      }
      clusterScanner = new Scanner(clustersString).useDelimiter(pathSeparator);
      while (clusterScanner.hasNext()) {
        String clusterPath = clusterScanner.next();
        int pathIndex = clusterPath.lastIndexOf(separator);
        if (pathIndex > -1) {
          clusters.add(clusterPath.substring(pathIndex+1));
        }
      }
    }
    return Collections.unmodifiableSet(clusters);
  }
  
  protected String getBranding(Jvm jvm) {
    String args = jvm.getMainArgs();
    if (args != null) {
      int brandingOffset = args.indexOf(BRANDING_ID);
      
      if (brandingOffset > -1) {
        Scanner sc = new Scanner(args.substring(brandingOffset));
        sc.next(); // skip --branding
        if (sc.hasNext()) {
          return sc.next();
        }
      }
    }
    return null;
  }

  /**
   * Detects NetBeans IDE, NetBeans Platform based
   * application and VisualVM itself. It returns 
   * {@link VisualVMApplicationType} for VisualVM,
   * {@link NetBeansApplicationType} for NetBeans 4.0 and newer and
   * {@link NetBeans3xApplicationType} for NetBeans 3.x
   * 
   * @return {@link ApplicationType} subclass or <code>null</code> if
   * this application is not NetBeans 
   */ 
  public ApplicationType createApplicationTypeFor(Application app, Jvm jvm, String mainClass) {
    if (isNetBeans(jvm,mainClass)) {
      String branding = getBranding(jvm);
      if (VISUALVM_ID.equals(branding)) {
        return new VisualVMApplicationType(app);
      }
      Set<String> clusters = computeClusters(jvm);
      Iterator<String> clIt = clusters.iterator();
      
      while(clIt.hasNext()) {
        String cluster = clIt.next();
        if (nbcluster_pattern.matcher(cluster).matches()) {
          return new NetBeansApplicationType(app,jvm,clusters);
        }
      }
      if (clusters.isEmpty() && branding == null) {
        return new NetBeans3xApplicationType(app,jvm);
      }
      return new NetBeansBasedApplicationType(app,jvm,clusters,branding);
    }
    return null;
  }

}

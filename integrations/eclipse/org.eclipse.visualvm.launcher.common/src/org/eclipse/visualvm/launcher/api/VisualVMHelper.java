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
package org.eclipse.visualvm.launcher.api;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.visualvm.launcher.Activator;
import org.eclipse.visualvm.launcher.preferences.PreferenceConstants;

public final class VisualVMHelper {
	public static long getNextID() {
		return System.nanoTime();
	}
	
	public static String[] getJvmArgs(long id) {
		return new String[]{"-Dvisualvm.id=" + id}; 
	}
	
	public static void openInVisualVM(long id) throws IOException {
		String jv = getJavaVersion();
		if (jv == null || !jv.startsWith("1.6")) {
			try {
				final Display d = Display.getDefault();
				d.asyncExec(new Runnable() {
					public void run() {
						Shell s = new Shell(d);
						MessageDialog.openError(s, "VisualVM requires JDK1.6+ to run", "You are trying to launch VisualVM using an unsupported JDK.\n\nUse 'Window\\Preferences\\Run/Debug\\Launching\\VisualVM Configuration' to set the VisualVM JDK_HOME.");						
					}
				});
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			return;
		}
		
		Runtime.getRuntime().exec(
			new String[] { 
					Activator.getDefault().getPreferenceStore().getString(PreferenceConstants.P_PATH),
					"--jdkhome",
					Activator.getDefault().getPreferenceStore().getString(PreferenceConstants.P_JAVAHOME),
					"--openid",
					String.valueOf(id) 
		});
	}
	
	private static String getJavaVersion() {
		try {
			String javaCmd = Activator.getDefault().getPreferenceStore().getString(PreferenceConstants.P_JAVAHOME) + File.separator + "bin" + File.separator + "java";
			Process prc = Runtime.getRuntime().exec(
				new String[] {
						javaCmd,
						"-version"
				}
			);
			
			String version = getJavaVersion(prc.getErrorStream());
			if (version == null) {
				version = getJavaVersion(prc.getInputStream());
			}
			return version;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private static String getJavaVersion(InputStream is) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		try {
			String line;
			while ((line = br.readLine()) != null) {
				if (line.startsWith("java version")) {
					int start = line.indexOf("\"");
					int end = line.lastIndexOf("\"");
					if (start > -1 && end > -1) {
						return line.substring(start + 1, end);
					}
				}
			}
		} finally {
			br.close();
		}
		return null;
	}
}

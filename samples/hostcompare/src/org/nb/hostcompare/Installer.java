/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nb.hostcompare;

import org.openide.modules.ModuleInstall;

/**
 * Manages a module's lifecycle. Remember that an installer is optional and
 * often not needed at all.
 */
public class Installer extends ModuleInstall {

    @Override
    public void restored() {
        HostViewProvider.initialize();
    }

    @Override
    public void uninstalled() {
        HostViewProvider.unregister();
    }
}

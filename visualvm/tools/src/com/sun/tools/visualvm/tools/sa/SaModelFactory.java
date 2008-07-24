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

package com.sun.tools.visualvm.tools.sa;

import com.sun.tools.visualvm.core.datasource.DataSource;
import com.sun.tools.visualvm.core.model.ModelFactory;

/**
 * The SaModelFactory class is a factory class for getting the
 * {@link SaModel} representation for the {@link Application}.
 * 
 * @author Tomas Hurka
 */
public final class SaModelFactory extends ModelFactory<SaModel, DataSource> {

    private static SaModelFactory saAgentFactory;

    private SaModelFactory() {
    }

    /**
     * Getter for the default version of the SaModelFactory.
     * @return instance of {@link SaModelFactory}.
     */
    public static synchronized SaModelFactory getDefault() {
        if (saAgentFactory == null) {
            saAgentFactory = new SaModelFactory();
        }
        return saAgentFactory;
    }
    
    /**
     * Factory method for obtaining {@link SaModel} for {@link Application}.
     * Note that there is only one instance of {@link SaModel} for a concrete
     * application.This {@link SaModel} instance is cached. This method can 
     * return <CODE>null</CODE> if there is no AttachModel available
     * @param app application
     * @return {@link SaModel} instance or <CODE>null</CODE> if there is no
     * {@link SaModel}
     */
    public static SaModel getSAAgentFor(DataSource app) {
        return getDefault().getModel(app);
    }
    
}

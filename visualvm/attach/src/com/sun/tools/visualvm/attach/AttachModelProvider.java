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

package com.sun.tools.visualvm.attach;

import com.sun.tools.visualvm.application.Application;
import com.sun.tools.visualvm.core.model.AbstractModelProvider;
import com.sun.tools.visualvm.host.Host;
import com.sun.tools.visualvm.tools.attach.AttachModel;
import com.sun.tools.visualvm.tools.jvmstat.JvmJvmstatModel;
import com.sun.tools.visualvm.tools.jvmstat.JvmJvmstatModelFactory;
import org.openide.util.Utilities;

/**
 *
 * @author Tomas Hurka
 */
public final class AttachModelProvider extends AbstractModelProvider<AttachModel, Application>  {
    
    private static final String HOST_ID_KEY = "jvmstat.hostid";
    
    AttachModelProvider() {
    }
    
    public AttachModel createModelFor(Application app) {
        if (Host.LOCALHOST.equals(app.getHost())) {
            JvmJvmstatModel jvmstat = JvmJvmstatModelFactory.getJvmstatModelFor(app);
            
            if (jvmstat != null && jvmstat.isAttachable()) {
                if (Utilities.isWindows()) {
                    // on Windows Attach API can only attach to the process of the same
                    // architecture ( 32bit / 64bit )
                    Boolean this64bitArch = is64BitArchitecture();
                    Boolean app64bitArch = is64BitArchitecture(jvmstat);
                    if (this64bitArch != null && app64bitArch != null) {
                        if (!this64bitArch.equals(app64bitArch)) {
                            return null;
                        }
                    }
                }
                // check that application is runnung under the same users as VisualVM
                String currentAppId = Application.CURRENT_APPLICATION.getStorage().getCustomProperty(HOST_ID_KEY);
                String appId = app.getStorage().getCustomProperty(HOST_ID_KEY);
                if (!currentAppId.equals(appId)) {
                    return null;
                }
                return new AttachModelImpl(app);
            }
        }
        return null;
    }
    
    private static Boolean is64BitArchitecture(JvmJvmstatModel jvmstat) {
        String name = jvmstat.getVmName();
        if (name != null) {
            return name.toLowerCase().contains("64-bit");   // NOI18N
        }
        return null;
    }
    
    private static Boolean is64BitArchitecture() {
        String thisArch = System.getProperty("sun.arch.data.model");    // NOI18N
        if (thisArch != null) {
            return Boolean.valueOf("64".equals(thisArch));  // NOI18N
        }
        return null;
    }
}

/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.as.embedded;

import java.util.Map;
import java.util.Properties;
import org.wildfly.security.manager.ClearPropertyAction;
import org.wildfly.security.manager.GetContextClassLoaderAction;
import org.wildfly.security.manager.GetEnvironmentAction;
import org.wildfly.security.manager.GetSystemPropertiesAction;
import org.wildfly.security.manager.ReadPropertyAction;
import org.wildfly.security.manager.SetContextClassLoaderAction;
import org.wildfly.security.manager.WildFlySecurityManager;
import org.wildfly.security.manager.WritePropertyAction;

import static java.lang.System.clearProperty;
import static java.lang.System.getProperties;
import static java.lang.System.getProperty;
import static java.lang.System.getenv;
import static java.lang.System.setProperty;
import static java.lang.Thread.currentThread;
import static java.security.AccessController.doPrivileged;

/**
 * Security actions to access system environment information.  No methods in
 * this class are to be made public under any circumstances!
 *
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
class SecurityActions {

    private SecurityActions() {
    }

    static String getSystemProperty(final String key) {
        return ! WildFlySecurityManager.isChecking() ? getProperty(key) : doPrivileged(new ReadPropertyAction(key));
    }

    static void setSystemProperty(final String key, final String value) {
        if (! WildFlySecurityManager.isChecking()) {
            setProperty(key, value);
        } else {
            doPrivileged(new WritePropertyAction(key, value));
        }
    }

    static void clearSystemProperty(final String key) {
        if (! WildFlySecurityManager.isChecking()) {
            clearProperty(key);
        } else {
            doPrivileged(new ClearPropertyAction(key));
        }
    }

    public static Properties getSystemProperties() {
        return ! WildFlySecurityManager.isChecking() ? getProperties() : doPrivileged(GetSystemPropertiesAction.getInstance());
    }

    public static Map<String, String> getSystemEnvironment() {
        return ! WildFlySecurityManager.isChecking() ? getenv() : doPrivileged(GetEnvironmentAction.getInstance());
    }

    static ClassLoader getContextClassLoader() {
        return ! WildFlySecurityManager.isChecking() ? currentThread().getContextClassLoader() : doPrivileged(GetContextClassLoaderAction.getInstance());
    }

    static void setContextClassLoader(final ClassLoader tccl) {
        if (! WildFlySecurityManager.isChecking()) {
            currentThread().setContextClassLoader(tccl);
        } else {
            doPrivileged(new SetContextClassLoaderAction(tccl));
        }
    }
}

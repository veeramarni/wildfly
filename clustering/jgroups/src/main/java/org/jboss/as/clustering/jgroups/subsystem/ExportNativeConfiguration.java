/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
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

package org.jboss.as.clustering.jgroups.subsystem;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.jboss.as.clustering.jgroups.ChannelFactory;
import org.jboss.as.clustering.msc.ServiceContainerHelper;
import org.jboss.as.controller.AbstractRuntimeOnlyHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.PathAddress;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceRegistry;
import org.jgroups.Channel;
import org.jgroups.stack.Protocol;
import org.jgroups.stack.ProtocolStack;

/**
 * Implements /subsystem=jgroups/stack=X/export-native-configuration() operation.
 *
 * @author Richard Achmatowicz (c) 2011 Red Hat Inc.
 */
public class ExportNativeConfiguration extends AbstractRuntimeOnlyHandler {

    @Override
    protected void executeRuntimeStep(OperationContext context, ModelNode operation) throws OperationFailedException {

        PathAddress stackAddress = PathAddress.pathAddress(operation.get(OP_ADDR));
        String stackName = stackAddress.getLastElement().getValue();

        ServiceRegistry registry = context.getServiceRegistry(false);
        ServiceName serviceName = ChannelFactoryService.getServiceName(stackName);
        try {
            ServiceController<?> controller = registry.getRequiredService(serviceName);
            controller.setMode(ServiceController.Mode.ACTIVE);
            try {
                ChannelFactory factory = ServiceContainerHelper.getValue(controller, ChannelFactory.class);
                // Create a temporary channel, but don't connect it
                Channel channel = factory.createChannel(UUID.randomUUID().toString());
                try {
                    // ProtocolStack.printProtocolSpecAsXML() is very hacky and only works on an uninitialized stack
                    List<Protocol> protocols = channel.getProtocolStack().getProtocols();
                    Collections.reverse(protocols);
                    ProtocolStack stack = new ProtocolStack();
                    stack.addProtocols(protocols);
                    context.getResult().set(stack.printProtocolSpecAsXML());
                    context.completeStep(OperationContext.RollbackHandler.NOOP_ROLLBACK_HANDLER);
                } finally {
                    channel.close();
                }
            } finally {
                controller.setMode(ServiceController.Mode.ON_DEMAND);
            }
        } catch (Exception e) {
            throw new OperationFailedException(e.getLocalizedMessage(), e);
        }
    }
}

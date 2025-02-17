/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.generic.annotation;

import org.eclipse.sensinact.gateway.generic.Task;
import org.eclipse.sensinact.gateway.util.UriUtils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
@Inherited
@Target(value = {ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface TaskCommand {
    enum SynchronizationPolicy {
        SYNCHRONOUS, ASYNCHRONOUS;
    }

    public static final String ROOT = UriUtils.ROOT;

    /**
     * {@link Task.CommandType} of the executed {@link Task} to
     * which to map the annotated method
     *
     * @return {@link Task.CommandType} of the executed {@link Task}
     */
    public Task.CommandType method();

    /**
     * the string uri of the {@link SnaObject} which has
     * created the executed {@link Task} to which to map the
     * annotated method
     *
     * @return the string uri of the requirer {@link SnaObject}
     */
    public String target() default ROOT;

    /**
     * the {@link SynchronizationPolicy} which applies for the method
     * execution : if defined as {SynchronizationPolicy.SYNCHRONOUS}
     * the result of the method is set as the result of the executed
     * mapped {@link Task}
     *
     * @return the {@link SynchronizationPolicy} which applies for the method
     */
    public SynchronizationPolicy synchronization() default SynchronizationPolicy.SYNCHRONOUS;

}

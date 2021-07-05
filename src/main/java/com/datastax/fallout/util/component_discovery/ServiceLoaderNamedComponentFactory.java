/*
 * Copyright 2021 DataStax, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.datastax.fallout.util.component_discovery;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.ServiceLoader;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.fallout.ops.PropertyBasedComponent;

public class ServiceLoaderNamedComponentFactory<Component extends PropertyBasedComponent>
    implements NamedComponentFactory<Component>
{
    private static final Logger log = LoggerFactory.getLogger(ServiceLoaderNamedComponentFactory.class);
    private final List<Component> loadedComponents;

    public ServiceLoaderNamedComponentFactory(Class<Component> clazz)
    {
        loadedComponents = loadComponents(clazz);
    }

    public static <Component extends PropertyBasedComponent> List<Component>
        loadComponents(Class<Component> componentClass)
    {
        try
        {
            ServiceLoader<Component> loadedComponents = ServiceLoader.load(componentClass);
            return Lists.newArrayList(loadedComponents);
        }
        catch (Throwable t)
        {
            log.error("Failed to loadComponents for " + componentClass, t);
            throw t;
        }
    }

    @SuppressWarnings("unchecked")
    private Component createNewInstance(Component componentInstance)
        throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException
    {
        return (Component) componentInstance.getClass().getDeclaredConstructor().newInstance();
    }

    @Override
    public Component createComponent(String name)
    {
        for (Component componentInstance : loadedComponents)
        {
            try
            {
                if (componentInstance.name().equalsIgnoreCase(name))
                {
                    return createNewInstance(componentInstance);
                }
            }
            catch (InstantiationException | IllegalAccessException | NoSuchMethodException |
                InvocationTargetException e)
            {
                throw new RuntimeException("Error creating instance", e);
            }
        }
        return null;
    }
}

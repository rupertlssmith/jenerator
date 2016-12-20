/*
 * Copyright The Sett Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.thesett.catalogue.generator;

import java.util.Set;

import com.thesett.aima.state.ComponentType;
import com.thesett.catalogue.model.ViewType;

/**
 * ViewTypeDecorator is a {@link TypeDecorator} for {@link ViewType}s. It automatically decorates the types of any
 * fields of the view that are accessed through it, and provides all of the underlying properties of the decorated view.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Decorate a component type, and the types of all of its fields.
 *     <td> {@link TypeDecoratorFactory}, {@link com.thesett.aima.state.ComponentType}.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class ViewTypeDecorator extends ComponentTypeDecorator implements ViewType
{
    /**
     * Creates a type decorator for an view type, that returns decorated types for all reachable child types on all of
     * the fields of the view.
     *
     * @param type The view type to decorate.
     */
    public ViewTypeDecorator(ViewType type)
    {
        super(type);
    }

    /** {@inheritDoc} */
    public Set<ComponentType> getDescendants()
    {
        return ((ViewType) type).getDescendants();
    }

    /** {@inheritDoc} */
    public void setDescendants(Set<ComponentType> descendants)
    {
        ((ViewType) type).setDescendants(descendants);
    }
}

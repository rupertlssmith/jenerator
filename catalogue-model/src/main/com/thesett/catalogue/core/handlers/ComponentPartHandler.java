/*
 * Copyright The Sett Ltd, 2005 to 2014.
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
package com.thesett.catalogue.core.handlers;

import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.xml.bind.JAXBElement;

import com.thesett.aima.logic.fol.Functor;
import com.thesett.aima.logic.fol.VariableAndFunctorInterner;
import com.thesett.catalogue.setup.BagType;
import com.thesett.catalogue.setup.BooleanType;
import com.thesett.catalogue.setup.CollectionType;
import com.thesett.catalogue.setup.ComponentPartType;
import com.thesett.catalogue.setup.ComponentType;
import com.thesett.catalogue.setup.DateType;
import com.thesett.catalogue.setup.EnumerationType;
import com.thesett.catalogue.setup.ExtendComponentType;
import com.thesett.catalogue.setup.FieldDeclrType;
import com.thesett.catalogue.setup.HierarchyType;
import com.thesett.catalogue.setup.IntegerType;
import com.thesett.catalogue.setup.ListType;
import com.thesett.catalogue.setup.MapType;
import com.thesett.catalogue.setup.RealType;
import com.thesett.catalogue.setup.SetType;
import com.thesett.catalogue.setup.StringType;
import com.thesett.catalogue.setup.TimeType;
import com.thesett.catalogue.setup.UniqueType;

/**
 * ComponentPartHandler tranforms the 'componentPart' fields of a component type declaration into a fields/1 functor
 * with a list of component_ref, property, unqiue, extend, or collection functors as its argument. These listed
 * arguments form a set of elements that make up the component, and the component defines a set which is the
 * cross-product of them. Some of the argument elements, collections, extends or unique declrations recursively wrap
 * other components, so the handle method is recursively applied to their arguments, to build up the component.
 *
 * <pre><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Transform the elements that make up a component into a list.
 *     <td> {@link ComponentPartType}, {@link ComponentType}, {@link FieldDeclrType}, {@link UniqueType},
 *          {@link ExtendComponentType}, {@link CollectionType}.
 * <tr><td> Transform component basic field declaration types into atoms.
 *     <td> {@link com.thesett.aima.logic.fol.Functor}, {@link VariableAndFunctorInterner}.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class ComponentPartHandler implements FieldHandler
{
    /** Holds the interner to use to transform the basic field types into atoms. */
    private VariableAndFunctorInterner interner;

    /**
     * Creates a component field handler. The specified interner is required to transform basic field types into atoms.
     *
     * @param interner The interner to intern functor names with.
     */
    public ComponentPartHandler(VariableAndFunctorInterner interner)
    {
        this.interner = interner;
    }

    /**
     * {@inheritDoc}
     *
     * <p/>This transform turns the elements that make up a component into a list. If any of the elements are themselves
     * components, the transformation is recursively applied to them. This transformation expects the value of the field
     * to be a list of {@link ComponentPartType}s.
     */
    public String handleField(String property, Object value, boolean more)
    {
        if ("componentPart".equals(property))
        {
            // Cast the field value to a list of component parts.
            List<JAXBElement<? extends ComponentPartType>> componentParts =
                (List<JAXBElement<? extends ComponentPartType>>) value;

            String result = "fields([";

            for (Iterator<JAXBElement<? extends ComponentPartType>> i = componentParts.iterator(); i.hasNext();)
            {
                ComponentPartType componentPart = i.next().getValue();

                if (componentPart instanceof ComponentType)
                {
                    ComponentType component = (ComponentType) componentPart;
                    result += "component_ref(" + component.getName() + ", " + component.getType() + ")";
                }
                else if (componentPart instanceof FieldDeclrType)
                {
                    FieldDeclrType field = (FieldDeclrType) componentPart;
                    String fieldType =
                        (field.getType() == null) ? interner.getFunctorName(fieldDeclrToAtom(field)) : field.getType();

                    result += "property(" + field.getName() + ", " + fieldType + ")";
                }
                else if (componentPart instanceof UniqueType)
                {
                    UniqueType unique = (UniqueType) componentPart;

                    boolean isKey = (unique.isNaturalKey() == null) ? false : unique.isNaturalKey();
                    unique.getFieldDeclr();

                    result +=
                        "unique(" + (isKey ? "key, " : "not_key, ") +
                        handleField("componentPart", unique.getFieldDeclr(), false) + ")";
                }
                else if (componentPart instanceof ExtendComponentType)
                {
                    ExtendComponentType extend = (ExtendComponentType) componentPart;

                    String name = extend.getName();
                    String extendRef = "";

                    int closureCount = 0;

                    for (StringTokenizer nameTokenizer = new StringTokenizer(name, ".");
                            nameTokenizer.hasMoreElements();)
                    {
                        String nextToken = nameTokenizer.nextToken();

                        if (nameTokenizer.hasMoreElements())
                        {
                            extendRef += "extend_ref(" + nextToken + ", ";
                            closureCount++;
                        }
                        else
                        {
                            extendRef += nextToken;
                        }
                    }

                    for (int j = 0; j < closureCount; j++)
                    {
                        extendRef += ")";
                    }

                    result +=
                        "extend(" + extendRef + ", " + handleField("componentPart", extend.getFieldDeclr(), false) +
                        ")";
                }
                else if (componentPart instanceof CollectionType)
                {
                    CollectionType collection = (CollectionType) componentPart;

                    String collectionKind = null;

                    if (collection instanceof SetType)
                    {
                        collectionKind = "set";
                    }
                    else if (collection instanceof ListType)
                    {
                        collectionKind = "list";
                    }
                    else if (collection instanceof BagType)
                    {
                        collectionKind = "bag";
                    }
                    else if (collection instanceof MapType)
                    {
                        MapType mapCollection = (MapType) collection;

                        FieldDeclrType keyField = mapCollection.getKey().getFieldDeclr().getValue();
                        String keyName = keyField.getName();
                        String keyType =
                            (keyField.getType() == null) ? interner.getFunctorName(fieldDeclrToAtom(keyField))
                                                         : keyField.getType();

                        collectionKind = "map(" + keyType + ", " + keyName + ")";
                    }

                    collection.getName();
                    collection.getFieldDeclrRoot();

                    String parent = (collection.getParent() == null) ? null : collection.getParent().getName();

                    result +=
                        "collection(" + collectionKind + ", " + collection.getName() + ", " +
                        ((parent == null) ? "no_parent, " : ("parent(" + parent + "), ")) +
                        handleField("componentPart", collection.getFieldDeclrRoot(), false) + ")";
                }
                else
                {
                    result += "other";
                }

                if (i.hasNext())
                {
                    result += ", ";
                }
            }

            result += "])" + (more ? ", " : "");

            return result;
        }

        return null;
    }

    /**
     * Provides the prolog atom name for the specified field declaration.
     *
     * @param  field An instance of the field declaraion to get the atom for.
     *
     * @return The atom name of the field declarations type in prolog.
     */
    private Functor fieldDeclrToAtom(FieldDeclrType field)
    {
        String fieldType = null;

        if (field instanceof DateType)
        {
            fieldType = "date";
        }
        else if (field instanceof EnumerationType)
        {
            fieldType = "enumeration";
        }
        else if (field instanceof HierarchyType)
        {
            fieldType = "hierarchy";
        }
        else if (field instanceof BooleanType)
        {
            fieldType = "boolean";
        }
        else if (field instanceof IntegerType)
        {
            fieldType = "integer";
        }
        else if (field instanceof RealType)
        {
            fieldType = "real";
        }
        else if (field instanceof StringType)
        {
            fieldType = "string";
        }
        else if (field instanceof TimeType)
        {
            fieldType = "time";
        }

        int id = interner.internFunctorName(fieldType, 0);

        return new Functor(id, null);
    }
}

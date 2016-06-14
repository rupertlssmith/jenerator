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
package com.thesett.catalogue.core;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/** Defines the possible formats for holding data in a document model. */
public enum DocModelFormat
{
    /** Persist document models as json. */
    Json("json"),

    /** Persist document models as XML. */
    Xml("xml"),

    /** Persist document models in Java Serializer default binary format. */
    JavaSerializer("java-serializer");

    /** Holds a mapping of the enum values by name. */
    private static final Map<String, DocModelFormat> nameToValue = new HashMap<String, DocModelFormat>();

    static
    {
        for (DocModelFormat format : EnumSet.allOf(DocModelFormat.class))
        {
            nameToValue.put(format.name, format);
        }
    }

    /** The name of the format in the model. */
    private final String name;

    /**
     * Creates a named format.
     *
     * @param name The name of the format in the model.
     */
    DocModelFormat(String name)
    {
        this.name = name;
    }

    /**
     * Looks up a format enum by its name in the model.
     *
     * @param  name The name of the format in the model.
     *
     * @return The corresponding enum value.
     */
    public static DocModelFormat fromName(String name)
    {
        return nameToValue.get(name);
    }
}

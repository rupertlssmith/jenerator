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

import java.io.Writer;

import com.thesett.aima.logic.fol.Clause;
import com.thesett.aima.logic.fol.Functor;
import com.thesett.aima.logic.fol.interpreter.ResolutionEngine;
import com.thesett.aima.logic.fol.prolog.PrologCompiledClause;
import com.thesett.catalogue.setup.ComponentDefType;
import com.thesett.catalogue.setup.DateRangeType;
import com.thesett.catalogue.setup.DecimalType;
import com.thesett.catalogue.setup.DimensionDefType;
import com.thesett.catalogue.setup.EntityDefType;
import com.thesett.catalogue.setup.EnumerationDefType;
import com.thesett.catalogue.setup.FactDefType;
import com.thesett.catalogue.setup.HierarchyDefType;
import com.thesett.catalogue.setup.IntegerRangeType;
import com.thesett.catalogue.setup.RealRangeType;
import com.thesett.catalogue.setup.StringPatternType;
import com.thesett.catalogue.setup.TimeRangeType;
import com.thesett.catalogue.setup.TypeDefType;
import com.thesett.catalogue.setup.ViewDefType;

/**
 * BaseTermBuilder provides some base methods for implementing term builders.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Map types to atoms representing them. </td></tr>
 * </table></pre>
 */
public abstract class BaseTermBuilder implements ModelTermBuilder
{
    /** Holds the prolog engine to process the type checking rules with. */
    protected final ResolutionEngine<Clause, PrologCompiledClause, PrologCompiledClause> engine;

    /** Holds an optional writer to write the raw model out to. */
    protected final Writer modelWriter;

    /**
     * Creates a model term builder.
     *
     * @param engine      The Prolog engine to generate the terms for.
     * @param modelWriter An optional writer to output a copy of the terms to, may be <tt>null</tt>.
     */
    public BaseTermBuilder(ResolutionEngine<Clause, PrologCompiledClause, PrologCompiledClause> engine,
        Writer modelWriter)
    {
        this.engine = engine;
        this.modelWriter = modelWriter;
    }

    /**
     * Provides the prolog atom name for the specified type class.
     *
     * @param  type An instance of the type class to get the atom for.
     *
     * @return The atom name of the types representation in prolog.
     */
    protected Functor typeClassToAtom(TypeDefType type)
    {
        String kind = null;

        if (type instanceof DecimalType)
        {
            kind = "decimal_type";
        }

        if (type instanceof IntegerRangeType)
        {
            kind = "integer_range";
        }
        else if (type instanceof RealRangeType)
        {
            kind = "real_range";
        }
        else if (type instanceof StringPatternType)
        {
            kind = "string_pattern";
        }
        else if (type instanceof DateRangeType)
        {
            kind = "date_range";
        }
        else if (type instanceof TimeRangeType)
        {
            kind = "time_range";
        }
        else if (type instanceof EnumerationDefType)
        {
            kind = "enumeration_type";
        }
        else if (type instanceof HierarchyDefType)
        {
            kind = "hierarchy_type";
        }
        else if (type instanceof DimensionDefType)
        {
            kind = "dimension_type";
        }
        else if (type instanceof EntityDefType)
        {
            kind = "entity_type";
        }
        else if (type instanceof FactDefType)
        {
            kind = "fact_type";
        }
        else if (type instanceof ViewDefType)
        {
            kind = "view_type";
        }
        else if (type instanceof ComponentDefType)
        {
            kind = "component_type";
        }

        int id = engine.internFunctorName(kind, 0);

        return new Functor(id, null);
    }
}

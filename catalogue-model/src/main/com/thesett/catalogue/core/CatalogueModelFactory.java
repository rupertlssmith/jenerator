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

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.thesett.aima.attribute.impl.BigDecimalTypeImpl;
import com.thesett.aima.attribute.impl.DoubleRangeType;
import com.thesett.aima.attribute.impl.EnumeratedStringAttribute;
import com.thesett.aima.attribute.impl.FloatRangeType;
import com.thesett.aima.attribute.impl.HierarchyAttribute;
import com.thesett.aima.attribute.impl.HierarchyAttributeFactory;
import com.thesett.aima.attribute.impl.IntRangeType;
import com.thesett.aima.attribute.time.DateOnly;
import com.thesett.aima.attribute.time.TimeOnly;
import com.thesett.aima.logic.fol.Clause;
import com.thesett.aima.logic.fol.Functor;
import com.thesett.aima.logic.fol.NumericType;
import com.thesett.aima.logic.fol.Parser;
import com.thesett.aima.logic.fol.RecursiveList;
import com.thesett.aima.logic.fol.Sentence;
import com.thesett.aima.logic.fol.StringLiteral;
import com.thesett.aima.logic.fol.Term;
import com.thesett.aima.logic.fol.Variable;
import com.thesett.aima.logic.fol.interpreter.ResolutionEngine;
import com.thesett.aima.logic.fol.isoprologparser.Token;
import com.thesett.aima.logic.fol.isoprologparser.TokenSource;
import com.thesett.aima.logic.fol.prolog.PrologCompiledClause;
import com.thesett.aima.search.GoalState;
import com.thesett.aima.search.Operator;
import com.thesett.aima.search.QueueBasedSearchMethod;
import com.thesett.aima.search.Traversable;
import com.thesett.aima.search.TraversableState;
import com.thesett.aima.search.util.OperatorImpl;
import com.thesett.aima.search.util.Searches;
import com.thesett.aima.search.util.uninformed.DepthFirstSearch;
import com.thesett.aima.state.*;
import com.thesett.aima.state.impl.JavaType;
import com.thesett.aima.state.impl.WrappedBeanState;
import com.thesett.catalogue.core.handlers.ComponentPartHandler;
import com.thesett.catalogue.core.handlers.DefaultFieldHandler;
import com.thesett.catalogue.core.handlers.EnumLabelFieldHandler;
import com.thesett.catalogue.core.handlers.ExternalIdHandler;
import com.thesett.catalogue.core.handlers.FieldHandler;
import com.thesett.catalogue.core.handlers.HierarchyLabelFieldHandler;
import com.thesett.catalogue.core.handlers.InQuotesFieldHandler;
import com.thesett.catalogue.core.handlers.ViewHandler;
import com.thesett.catalogue.interfaces.CollectionType;
import com.thesett.catalogue.setup.CatalogueDefinition;
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
import com.thesett.catalogue.setup.SetupModelHelper;
import com.thesett.catalogue.setup.StringPatternType;
import com.thesett.catalogue.setup.TimeRangeType;
import com.thesett.catalogue.setup.TypeDefType;
import com.thesett.catalogue.setup.ViewDefType;
import com.thesett.common.parsing.SourceCodeException;
import com.thesett.common.util.EmptyIterator;
import com.thesett.common.util.StringUtils;
import com.thesett.common.util.maps.HashArray;

/**
 * CatalogueModelFactory provides queries to type check the catalogue model, which requires greater sophistication than
 * can be provided by XML schema validation of the model in its raw XML form. The type checking of the catalogue model
 * to a normal form ensures that where it is possible to define things with identical semantics in multiple ways using
 * the raw syntax of the model, syntactical differences are reduced to semantic equivalences where possible.
 *
 * <p/>The input to the catalogue model checker is a raw {@link CatalogueDefinition}. The output is a model, containing
 * the types from the raw model that type check and have been reduced to their canonical form.
 *
 * <p/>The factory outputs the results of type checking and reduction to normal as a {@link CatalogueModel}.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Reduce the raw catalogue model to its canonical form. <td> {@link CatalogueDefinition}
 * <tr><td> Type check the catalogue model. <td> {@link CatalogueDefinition}
 * <tr><td> Produce a CatalogueModel containing the results. <td> {@link CatalogueModel}.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class CatalogueModelFactory
{
    /** Used for debugging purposes. */
    public static final Logger log = Logger.getLogger(CatalogueModelFactory.class);

    /** Holds the resource name of the catalogue modelling rules. */
    static final String MODEL_RULES_RESOURCE = "model_rules.pl";

    /** Holds the prolog engine to process the type checking rules with. */
    private final ResolutionEngine<Clause, PrologCompiledClause, PrologCompiledClause> engine;

    /** Holds a mapping from basic java type names in the model, to Java basic types. */
    private Map<String, Type> basicTypeNameToJavaTypeMap =
        new HashMap<String, Type>()
        {
            {
                put("boolean", JavaType.BOOLEAN_TYPE);
                put("integer", JavaType.INTEGER_TYPE);
                put("real", JavaType.FLOAT_TYPE);
                put("string", JavaType.STRING_TYPE);
                put("date", new JavaType(DateOnly.class));
                put("time", new JavaType(TimeOnly.class));
            }
        };

    /** Holds a mapping from names of kinds of collections to kinds of collections. */
    private Map<String, CollectionType.CollectionKind> nameToCollectionKindMap =
        new HashMap<String, CollectionType.CollectionKind>()
        {
            {
                put("set", CollectionType.CollectionKind.Set);
                put("list", CollectionType.CollectionKind.List);
                put("bag", CollectionType.CollectionKind.Bag);
                put("map", CollectionType.CollectionKind.Map);
            }
        };

    /** Holds the raw catalogue definition to built the raw model from. */
    protected CatalogueDefinition catalogueDef;

    /** Holds an optional writer to write the raw model out to. */
    private Writer modelWriter;

    /**
     * Creates a catalogue model factory using the specified logic engine on the specifed catalogue definition.
     *
     * @param engine         The first order logic resolution engine used to type check the model.
     * @param catalogueDef   The raw catalogue model to create the logical model from.
     * @param rawModelWriter An optional writer to write out the raw, logic type model to. May be <tt>null</tt>.
     */
    public CatalogueModelFactory(ResolutionEngine<Clause, PrologCompiledClause, PrologCompiledClause> engine,
        CatalogueDefinition catalogueDef, Writer rawModelWriter)
    {
        this.engine = engine;
        this.catalogueDef = catalogueDef;
        this.modelWriter = rawModelWriter;
    }

    /**
     * Loads and compiles the model type checking rules, applies them to a catalogue definition, and outputs the
     * normalized catalogue types as a {@link CatalogueModel}.
     *
     * <p/>Converts the catalogue model from its XML form into first order logic. This naturally specifies all of the
     * types defined in the model as sets, in the case of components, sets made up of the cross product of more
     * primitive types as sets.
     *
     * <p/>Applies the type checking rules to the model, to reduce it to its normal form, and check for typing errors in
     * the model.
     *
     * @return An instantiated catalogue model.
     *
     * @throws SourceCodeException If any compilation or runtime errors are encountered in the type checking rules, or
     *                             the first order logic model.
     */
    public CatalogueModel initializeModel() throws SourceCodeException
    {
        log.debug("public void initializeModel(): called");

        // Add the type checking rules to the knowledge base.
        engine.consultInputStream(CatalogueModelFactory.class.getClassLoader().getResourceAsStream(
                MODEL_RULES_RESOURCE));

        // Extract all raw type definitions from the model and first order logic clauses for them.
        List<Sentence<Clause>> clauses = new ArrayList<Sentence<Clause>>();

        convertTypeToTerm(catalogueDef, engine, clauses, DecimalType.class,
            new String[] { "precision", "scale", "rounding", "from", "to" });
        convertTypeToTerm(catalogueDef, engine, clauses, IntegerRangeType.class, new String[] { "from", "to" });
        convertTypeToTerm(catalogueDef, engine, clauses, StringPatternType.class, new String[] { "regexp" },
            new InQuotesFieldHandler(new String[] { "regexp" }));
        convertTypeToTerm(catalogueDef, engine, clauses, DateRangeType.class, new String[] { "from", "to" },
            new InQuotesFieldHandler(new String[] { "from", "to" }));
        convertTypeToTerm(catalogueDef, engine, clauses, TimeRangeType.class, new String[] { "from", "to", "step" },
            new InQuotesFieldHandler(new String[] { "from", "to", "step" }));
        convertTypeToTerm(catalogueDef, engine, clauses, EnumerationDefType.class, new String[] { "label" },
            new EnumLabelFieldHandler());
        convertTypeToTerm(catalogueDef, engine, clauses, HierarchyDefType.class,
            new String[] { "finalized", "level", "hierarchyLabel" }, new HierarchyLabelFieldHandler());
        convertTypeToTerm(catalogueDef, engine, clauses, ComponentDefType.class,
            new String[] { "componentPart", "view", "externalId" }, new ComponentPartHandler(engine), new ViewHandler(),
            new ExternalIdHandler());

        // Add all the clauses to the knowledge base.
        for (Sentence<Clause> sentence : clauses)
        {
            engine.compile(sentence);
        }

        // Initialize top-level meta information about this catalogue.
        String packageName = SetupModelHelper.getPackageName(catalogueDef);

        // Used to build a mapping of all top-level types in the model by name.
        Map<String, Type> catalogueTypes = new HashMap<String, Type>();

        // Query the model for all component types and cache the result in a map.
        initializeAllTypes(catalogueTypes, packageName);

        return new CatalogueModel(packageName, catalogueTypes);
    }

    /**
     * Provides an iterator over all raw, non-normalized types in the model.
     *
     * @return An iterable over the variable bindings resulting from the query.
     */
    public Iterable<Map<String, Variable>> getRawTypes()
    {
        String queryString = "?-type_instance(MN, MT, RP).";

        return runQuery(queryString);
    }

    /**
     * Provides an iterator over all normalized types in the model.
     *
     * @return An iterable over the variable bindings resulting from the query.
     */
    public Iterable<Map<String, Variable>> getNormalizedTypes()
    {
        String queryString = "?-normal_type(MT, MN, JT, MP).";

        return runQuery(queryString);
    }

    /**
     * Provides an iterator over all types in the model that type check.
     *
     * @return An iterable over the variable bindings resulting from the query.
     */
    public Iterable<Map<String, Variable>> getCheckedTypes()
    {
        String queryString = "?-type_check(MT, MN, JT, MP).";

        return runQuery(queryString);
    }

    /**
     * Provides an iterator over all types in the model that fail to type check.
     *
     * @return An iterable over the variable bindings resulting from the query.
     */
    public Iterable<Map<String, Variable>> getFailedCheckTypes()
    {
        String queryString = "?-normal_type(MT, MN, JT, MP), not(type_check(MT, MN, JT, MP)).";

        return runQuery(queryString);
    }

    /**
     * Provides the resolution engine that contains the catalogue model in its domain.
     *
     * @return The catlaogue resolution engine.
     */
    public ResolutionEngine<Clause, PrologCompiledClause, PrologCompiledClause> getEngine()
    {
        return engine;
    }

    /**
     * Extracts the fields and their types for a named component type in the catalogue model.
     *
     * @param  catalogueTypes The map to build up the catalogue types in.
     * @param  name           The name of the component type to get the fields of.
     *
     * @return The fields and types of a named component type.
     */
    private Map<String, Type> getComponentFields(Map<String, Type> catalogueTypes, String name)
    {
        String queryString =
            "?-product_type(_PT), normal_type(_PT, " + name +
            ", class, _MP), member(fields(_FS), _MP), member(F, _FS).";
        Iterable<Map<String, Variable>> fieldBindingsIterable = runQuery(queryString);

        final Map<String, Type> results = new LinkedHashMap<String, Type>();

        for (Map<String, Variable> variables : fieldBindingsIterable)
        {
            Variable var = variables.get("F");
            Functor fieldFunctor = (Functor) var.getValue();

            String fieldKind = engine.getFunctorName(fieldFunctor);

            if ("property".equals(fieldKind))
            {
                String fieldName = engine.getFunctorName((Functor) fieldFunctor.getArgument(0));
                String fieldTypeName = engine.getFunctorName((Functor) fieldFunctor.getArgument(1));

                // Check if the type of the field is recognized as a basic type.
                if (basicTypeNameToJavaTypeMap.containsKey(fieldTypeName))
                {
                    Type fieldType = basicTypeNameToJavaTypeMap.get(fieldTypeName);
                    results.put(fieldName, fieldType);
                }

                // Check if the type of the field is recognized as a user defined top-level type.
                else if (catalogueTypes.containsKey(fieldTypeName))
                {
                    Type fieldType = catalogueTypes.get(fieldTypeName);
                    results.put(fieldName, fieldType);
                }
                else
                {
                    log.warn("Field " + fieldName + " of type " + fieldTypeName +
                        " not recognized as basic or user defined.");
                }
            }
            else if ("component_ref".equals(fieldKind))
            {
                String fieldName = engine.getFunctorName((Functor) fieldFunctor.getArgument(0));
                String fieldTypeName = engine.getFunctorName((Functor) fieldFunctor.getArgument(1));

                // Check if the type of the field is recognized as a user defined top-level type.
                if (catalogueTypes.containsKey(fieldTypeName))
                {
                    Type fieldType = catalogueTypes.get(fieldTypeName);
                    results.put(fieldName, fieldType);
                }

                // Otherwise, the type is assumed to refer to a yet to be processed user type.
                else
                {
                    Type fieldType = new PendingComponentRefType(fieldTypeName);
                    results.put(fieldName, fieldType);
                }
            }
            else if ("collection".equals(fieldKind))
            {
                Functor collectionKindFunctor = (Functor) fieldFunctor.getArgument(0).getValue();
                String collectionKindName = engine.getFunctorName(collectionKindFunctor);
                String fieldName = engine.getFunctorName((Functor) fieldFunctor.getArgument(1).getValue());
                String fieldTypeName =
                    engine.getFunctorName((Functor) ((Functor) fieldFunctor.getArgument(2).getValue()).getArgument(1)
                        .getValue());

                log.debug("fieldName = " + fieldName);
                log.debug("fieldTypeName = " + fieldTypeName);
                log.debug("collectionKindName = " + collectionKindName);

                CollectionType.CollectionKind collectionKind = nameToCollectionKindMap.get(collectionKindName);

                // Work out the type of elements in the collection.
                Type elementType = resolveTypeName(catalogueTypes, fieldTypeName);

                // If the collection is a map, work out the type of keys in the map and create a map type for the field.
                if (CollectionType.CollectionKind.Map.equals(collectionKind))
                {
                    String keyTypeName =
                        engine.getFunctorName((Functor) collectionKindFunctor.getArgument(0).getValue());

                    Type keyType = resolveTypeName(catalogueTypes, keyTypeName);

                    results.put(fieldName, new MapTypeImpl(keyType, elementType, ArrayList.class));
                }

                // Otherwise the field is a non-map collection type, so create a collection type for the field.
                else
                {
                    results.put(fieldName, new CollectionTypeImpl(elementType, ArrayList.class, collectionKind));
                }
            }
        }

        return results;
    }

    /**
     * Extracts the natural key fields for a named component type in the catalogue model. Not all components have
     * natural key fields, in which case the resulting set of fields will be empty.
     *
     *
     * @param  name The name of the component type to get the natural key fields of.
     *
     * @return The natural key fields of a named component type.
     */
    private Set<String> getNaturalKeyFields(String name)
    {
        String queryString =
            "?-product_type(_PT), normal_type(_PT, " + name +
            ", class, _MP), member(unique_fields(key, _FS), _MP), member(F, _FS).";
        Iterable<Map<String, Variable>> uniqueFieldsIterable = runQuery(queryString);

        final Set<String> results = new LinkedHashSet<String>();

        for (Map<String, Variable> variables : uniqueFieldsIterable)
        {
            Variable var = variables.get("F");
            Functor fieldFunctor = (Functor) var.getValue();

            String fieldName = engine.getFunctorName(fieldFunctor);

            results.add(fieldName);
        }

        return results;
    }

    /**
     * Extracts the ancestors types for a named component type in the catalogue model.
     *
     * @param  catalogueTypes The map to build up the catalogue types in.
     * @param  name           The name of the component type to get the ancestors of.
     *
     * @return The ancestors of a named component type.
     */
    private Set<ComponentType> getComponentAncestors(Map<String, Type> catalogueTypes, String name)
    {
        String queryString =
            "?-product_type(_PT), normal_type(_PT, " + name + ", class, _MP), member(views(_VS), _MP), member(V, _VS).";
        Iterable<Map<String, Variable>> viewBindingsIterable = runQuery(queryString);

        final Set<ComponentType> results = new HashSet<ComponentType>();

        for (Map<String, Variable> variables : viewBindingsIterable)
        {
            Variable var = variables.get("V");
            Functor viewFunctor = (Functor) var.getValue();

            String viewTypeName = engine.getFunctorName(viewFunctor);

            // Check if the type of the field is recognized as a user defined top-level type.
            if (catalogueTypes.containsKey(viewTypeName))
            {
                Type viewType = catalogueTypes.get(viewTypeName);
                results.add((ComponentType) viewType);
            }
            else // Otherwise, the type is assumed to refer to a yet to be processed user type.
            {
                ComponentType viewType = new PendingComponentType(viewTypeName);
                results.add(viewType);
            }
        }

        return results;
    }

    /**
     * Checks if a specified entity type supports external ids.
     *
     * @param  name The name of the entity type to check.
     *
     * @return <tt>true</tt> if the entity supports external ids.
     */
    private boolean supportsExternalId(String name)
    {
        String queryString =
            "?-product_type(_PT), normal_type(_PT, " + name + ", class, _MP), member(externalid, _MP).";

        engine.setTokenSource(TokenSource.getTokenSourceForString(queryString));

        try
        {
            engine.compile(engine.parse());
        }
        catch (SourceCodeException e)
        {
            // If the query fails to parse or link, then this is a non-recoverable bug, so is reported as a runtime
            // exception.
            throw new RuntimeException("The query, " + queryString + ", failed to compile.", e);
        }

        return engine.resolve() != null;
    }

    /**
     * Resolves the name of a type onto either a basic type, a user defined type in the catalogue that has already been
     * encountered, or a pending component type, if the type name is not recognized and therefore it is assumed that its
     * definition has not yet been encountered.
     *
     * @param  catalogueTypes The map to build up the catalogue types in.
     * @param  fieldTypeName  The name of the type to resolve.
     *
     * @return The resolved, possibly pending, type.
     */
    private Type resolveTypeName(Map<String, Type> catalogueTypes, String fieldTypeName)
    {
        Type elementType; // Check if the type of the field is recognized as a built in type.

        if (basicTypeNameToJavaTypeMap.containsKey(fieldTypeName))
        {
            elementType = basicTypeNameToJavaTypeMap.get(fieldTypeName);
        }

        // Check if the type of the field is recognized as a user defined top-level type.
        else if (catalogueTypes.containsKey(fieldTypeName))
        {
            elementType = catalogueTypes.get(fieldTypeName);
        }

        // Otherwise, the type is assumed to refer to a yet to be processed user type.
        else
        {
            elementType = new PendingComponentRefType(fieldTypeName);
        }

        return elementType;
    }

    /**
     * Extracts all types from the type checked model and caches them in the 'catalogueTypes' parameter by name.
     *
     * @param catalogueTypes The map to build up the catalogue types in.
     * @param packageName    The name of the java package for the catalogue.
     */
    private void initializeAllTypes(Map<String, Type> catalogueTypes, String packageName)
    {
        log.debug("private void initializeAllTypes(): called");

        // Initialize all the restricted types.
        initializeAllDecimalTypes(catalogueTypes);
        initializeAllRestrictedIntTypes(catalogueTypes);
        initializeAllRestrictedRealTypes(catalogueTypes);
        initializeAllRestrictedStringTypes(catalogueTypes);
        initializeAllRestrictedTimeTypes(catalogueTypes);
        initializeAllRestrictedDateTypes(catalogueTypes);

        // Initialize the hierarchy and enum types.
        initializeAllEnumTypes(catalogueTypes);
        initializeAllHierarchyTypes(catalogueTypes);

        // Initialize all the component types.
        initializeAllComponentTypes(catalogueTypes, packageName);
    }

    /**
     * Extracts all decimal types from the type checked model and caches them in the 'catalogueTypes' parameter by name.
     *
     * @param catalogueTypes The map to build up the catalogue types in.
     */
    private void initializeAllDecimalTypes(Map<String, Type> catalogueTypes)
    {
        String queryString =
            "?-normal_type(decimal, MN, JT, _MP), member(precision(Precision), _MP), member(scale(Scale), _MP).";
        Iterable<Map<String, Variable>> bindingsIterable = runQuery(queryString);

        for (Map<String, Variable> bindings : bindingsIterable)
        {
            String typeName = engine.getFunctorName((Functor) bindings.get("MN").getValue());
            String javaTypeName = engine.getFunctorName((Functor) bindings.get("JT").getValue());
            NumericType precisionTerm = (NumericType) bindings.get("Precision").getValue();
            NumericType scaleTerm = (NumericType) bindings.get("Scale").getValue();

            if ("bigdecimal".equals(javaTypeName))
            {
                catalogueTypes.put(typeName,
                    BigDecimalTypeImpl.createInstance(typeName, precisionTerm.intValue(), scaleTerm.intValue()));
            }
        }
    }

    /**
     * Extracts all restricted int types from the type checked model and caches them in 'catalogueTypes' parameter by
     * name.
     *
     * @param catalogueTypes The map to build up the catalogue types in.
     */
    private void initializeAllRestrictedIntTypes(Map<String, Type> catalogueTypes)
    {
        String queryString = "?-normal_type(integer_range, MN, JT, _MP), member(from(From), _MP), member(to(To), _MP).";
        Iterable<Map<String, Variable>> bindingsIterable = runQuery(queryString);

        for (Map<String, Variable> bindings : bindingsIterable)
        {
            String typeName = engine.getFunctorName((Functor) bindings.get("MN").getValue());
            String javaTypeName = engine.getFunctorName((Functor) bindings.get("JT").getValue());
            NumericType fromTerm = (NumericType) bindings.get("From").getValue();
            NumericType toTerm = (NumericType) bindings.get("To").getValue();

            if ("int".equals(javaTypeName))
            {
                catalogueTypes.put(typeName,
                    IntRangeType.createInstance(typeName, fromTerm.intValue(), toTerm.intValue()));
            }
            else if ("long".equals(javaTypeName))
            {
            }
        }
    }

    /**
     * Extracts all restricted real types from the type checked model and caches them in the 'catalogueTypes' parameter
     * by name.
     *
     * @param catalogueTypes The map to build up the catalogue types in.
     */
    private void initializeAllRestrictedRealTypes(Map<String, Type> catalogueTypes)
    {
        String queryString = "?-normal_type(real_range, MN, JT, _MP), member(from(From), _MP), member(to(To), _MP).";
        Iterable<Map<String, Variable>> bindingsIterable = runQuery(queryString);

        for (Map<String, Variable> bindings : bindingsIterable)
        {
            String typeName = engine.getFunctorName((Functor) bindings.get("MN").getValue());
            String javaTypeName = engine.getFunctorName((Functor) bindings.get("JT").getValue());
            NumericType fromTerm = (NumericType) bindings.get("From").getValue();
            NumericType toTerm = (NumericType) bindings.get("To").getValue();

            if ("float".equals(javaTypeName))
            {
                catalogueTypes.put(typeName,
                    FloatRangeType.createInstance(typeName, fromTerm.floatValue(), toTerm.floatValue()));
            }
            else if ("double".equals(javaTypeName))
            {
                catalogueTypes.put(typeName,
                    DoubleRangeType.createInstance(typeName, fromTerm.doubleValue(), toTerm.doubleValue()));
            }
        }
    }

    /**
     * Extracts all restricted string types from the type checked model and caches them in the 'catalogueTypes'
     * parameter by name.
     *
     * @param catalogueTypes The map to build up the catalogue types in.
     */
    private void initializeAllRestrictedStringTypes(Map<String, Type> catalogueTypes)
    {
        String queryString =
            "?-normal_type(string_pattern, MN, string, _MP), member(length(Length), _MP), member(regexp(Regexp), _MP).";
        Iterable<Map<String, Variable>> bindingsIterable = runQuery(queryString);

        for (Map<String, Variable> bindings : bindingsIterable)
        {
            String typeName = engine.getFunctorName((Functor) bindings.get("MN").getValue());

            Term lengthTerm = bindings.get("Length").getValue();
            Term regexpTerm = bindings.get("Regexp").getValue();

            // Extract the length parameter if one is set.
            int length = 0;

            if (lengthTerm.isNumber())
            {
                length = ((NumericType) lengthTerm).intValue();
            }

            // Extract the pattern parameter if one is set.
            String pattern = null;

            if (regexpTerm instanceof StringLiteral)
            {
                pattern = regexpTerm.toString();
            }

            // Create the named string pattern type.
            catalogueTypes.put(typeName,
                com.thesett.aima.attribute.impl.StringPatternType.createInstance(typeName, length, pattern));
        }
    }

    /**
     * Extracts all restricted time types from the type checked model and caches them in the 'catalogueTypes' parameter
     * by name.
     *
     * @param catalogueTypes The map to build up the catalogue types in.
     */
    private void initializeAllRestrictedTimeTypes(Map<String, Type> catalogueTypes)
    {
        String queryString =
            "?-normal_type(time_range, MN, time, _MP), " +
            "member(from(From), _MP), member(to(To), _MP), member(step(Step), _MP).";
        Iterable<Map<String, Variable>> bindingsIterable = runQuery(queryString);

        for (Map<String, Variable> bindings : bindingsIterable)
        {
            String typeName = engine.getFunctorName((Functor) bindings.get("MN").getValue());

            Term fromTerm = bindings.get("From").getValue();
            Term toTerm = bindings.get("To").getValue();
            Term stepTerm = bindings.get("Step").getValue();

            TimeOnly from = null;
            TimeOnly to = null;

            if (fromTerm instanceof StringLiteral)
            {
                String value = ((StringLiteral) fromTerm).stringValue();
                from = TimeOnly.parseTime(value);
            }

            if (toTerm instanceof StringLiteral)
            {
                String value = ((StringLiteral) toTerm).stringValue();
                to = TimeOnly.parseTime(value);
            }

            // Create the named restricted time range type.
            catalogueTypes.put(typeName,
                com.thesett.aima.attribute.impl.TimeRangeType.createInstance(typeName, from, to));
        }
    }

    /**
     * Extracts all restricted date types from the type checked model and caches them in the 'catalogueTypes' parameter
     * by name.
     *
     * @param catalogueTypes The map to build up the catalogue types in.
     */
    private void initializeAllRestrictedDateTypes(Map<String, Type> catalogueTypes)
    {
        String queryString =
            "?-normal_type(date_range, MN, date, _MP), " + "member(from(From), _MP), member(to(To), _MP).";
        Iterable<Map<String, Variable>> bindingsIterable = runQuery(queryString);

        for (Map<String, Variable> bindings : bindingsIterable)
        {
            String typeName = engine.getFunctorName((Functor) bindings.get("MN").getValue());

            Term fromTerm = bindings.get("From").getValue();
            Term toTerm = bindings.get("To").getValue();

            DateOnly from = null;
            DateOnly to = null;

            if (fromTerm instanceof StringLiteral)
            {
                String value = ((StringLiteral) fromTerm).stringValue();

                if (!"null".equals(value))
                {
                    from = DateOnly.parseDate(value);
                }
            }

            if (toTerm instanceof StringLiteral)
            {
                String value = ((StringLiteral) toTerm).stringValue();

                if (!"null".equals(value))
                {
                    to = DateOnly.parseDate(value);
                }
            }

            // Create the named restricted time range type.
            catalogueTypes.put(typeName,
                com.thesett.aima.attribute.impl.DateRangeType.createInstance(typeName, from, to));
        }
    }

    /**
     * Extracts all enum types from the type checked model and caches them in the 'catalogueTypes' parameter by name.
     *
     * @param catalogueTypes The map to build up the catalogue types in.
     */
    private void initializeAllEnumTypes(Map<String, Type> catalogueTypes)
    {
        String queryString = "?-normal_type(enumeration_type, MN, enumeration, _MP), member(labels(L), _MP).";
        Iterable<Map<String, Variable>> bindingsIterable = runQuery(queryString);

        for (Map<String, Variable> enumBindings : bindingsIterable)
        {
            Variable nameVar = enumBindings.get("MN");
            String typeName = engine.getFunctorName((Functor) nameVar.getValue());

            Variable labelsVar = enumBindings.get("L");
            RecursiveList labelsList = (RecursiveList) labelsVar.getValue();

            List<String> labelNames = new LinkedList<String>();

            for (Term levelTerm : labelsList)
            {
                labelNames.add(engine.getFunctorName((Functor) levelTerm));
            }

            EnumeratedStringAttribute.EnumeratedStringAttributeFactory factory =
                EnumeratedStringAttribute.getFactoryForClass(typeName);

            for (String label : labelNames)
            {
                factory.createStringAttribute(label);
            }

            catalogueTypes.put(typeName, factory.getType());
        }
    }

    /**
     * Extracts all hierarchy types from the type checked model and caches them in the 'catalogueTypes' parameter by
     * name.
     *
     * @param catalogueTypes The map to build up the catalogue types in.
     */
    private void initializeAllHierarchyTypes(Map<String, Type> catalogueTypes)
    {
        String queryString = "?-normal_type(hierarchy_type, MN, hierarchy, _MP), member(levels(Lev), _MP).";
        Iterable<Map<String, Variable>> hierarchyBingingsIterable = runQuery(queryString);

        // Used to keep the names of hierarchies to initialize.
        List<String> hierarchyNames = new LinkedList<String>();

        for (Map<String, Variable> hierarchyBindings : hierarchyBingingsIterable)
        {
            Variable nameVar = hierarchyBindings.get("MN");
            String typeName = engine.getFunctorName((Functor) nameVar.getValue());
            hierarchyNames.add(typeName);

            Variable levelsVar = hierarchyBindings.get("Lev");
            RecursiveList levelsList = (RecursiveList) levelsVar.getValue();

            List<String> levelNames = new LinkedList<String>();

            for (Term levelTerm : levelsList)
            {
                levelNames.add(engine.getFunctorName((Functor) levelTerm));
            }

            HierarchyAttributeFactory factory = HierarchyAttribute.getFactoryForClass(typeName);
            factory.setLevelNames(levelNames.toArray(new String[levelNames.size()]));

            catalogueTypes.put(typeName, factory.getType());
        }

        // For each hierarchy type, check if it has some initializing labels defined, in which case initialize these
        // into its type.
        for (String hierarchyName : hierarchyNames)
        {
            HierarchyAttributeFactory factory = HierarchyAttribute.getFactoryForClass(hierarchyName);

            queryString =
                "?-normal_type(hierarchy_type, " + hierarchyName + ", hierarchy, _MP), member(labels(Lab), _MP).";
            hierarchyBingingsIterable = runQuery(queryString);

            for (Map<String, Variable> labelBindings : hierarchyBingingsIterable)
            {
                Functor labels = (Functor) labelBindings.get("Lab").getValue();

                // Create an initial traversable state space out of the top-level label.
                LabelState startState = new LabelState(labels);

                // Create a depth first search over the label space, and extract all label paths from it.
                QueueBasedSearchMethod<LabelState, LabelState> labelSearch =
                    new DepthFirstSearch<LabelState, LabelState>();
                labelSearch.reset();
                labelSearch.addStartState(startState);

                Iterator<LabelState> leafLabelIterator = Searches.allSolutions(labelSearch);

                while (leafLabelIterator.hasNext())
                {
                    LabelState leafLabel = leafLabelIterator.next();

                    factory.createHierarchyAttribute(leafLabel.getLabelPath());

                    log.debug("Created label on hierarchy, " + hierarchyName + ", with path " +
                        Arrays.toString(leafLabel.getLabelPath()) + ".");
                }
            }
        }

        // For each finalized hierarchy type, close its definition set by finalizing it.
        queryString = "?-normal_type(hierarchy_type, MN, hierarchy, _MP), member(finalized, _MP).";
        hierarchyBingingsIterable = runQuery(queryString);

        for (Map<String, Variable> finalizedBindings : hierarchyBingingsIterable)
        {
            Variable nameVar = finalizedBindings.get("MN");
            String typeName = engine.getFunctorName((Functor) nameVar.getValue());

            HierarchyAttributeFactory factory = HierarchyAttribute.getFactoryForClass(typeName);

            factory.finalizeAttribute();

            log.debug("Finalized hierarchy " + typeName);
        }
    }

    /**
     * Extracts all component types from the type checked model and caches them in the 'catalogueTypes' parameter by
     * name.
     *
     * @param catalogueTypes The map to build up the catalogue types in.
     * @param packageName    The name of the java package for the catalogue.
     */
    private void initializeAllComponentTypes(Map<String, Type> catalogueTypes, String packageName)
    {
        String queryString = "?-product_type(PT), normal_type(PT, MN, class, _MP).";
        Iterable<Map<String, Variable>> componentBindingsIterable = runQuery(queryString);

        Map<String, Collection<String>> componentNamesByType = new HashMap<String, Collection<String>>();

        for (Map<String, Variable> variables : componentBindingsIterable)
        {
            Variable nameVar = variables.get("MN");
            Functor typeNameAtom = (Functor) nameVar.getValue();

            Variable typeVar = variables.get("PT");
            String typeName = engine.getFunctorName((Functor) typeVar.getValue());

            String componentName = engine.getFunctorName(typeNameAtom);

            Collection<String> componentNames = componentNamesByType.get(typeName);

            if (componentNames == null)
            {
                componentNames = new LinkedList<String>();
                componentNamesByType.put(typeName, componentNames);
            }

            componentNames.add(componentName);

            log.debug("Found " + typeName + ": " + componentName);
        }

        for (Map.Entry<String, Collection<String>> componentNamesEntry : componentNamesByType.entrySet())
        {
            String componentType = componentNamesEntry.getKey();

            for (String componentName : componentNamesEntry.getValue())
            {
                Map<String, Type> componentFields = getComponentFields(catalogueTypes, componentName);
                Set<String> naturalKeyFields = getNaturalKeyFields(componentName);
                Set<ComponentType> ancestors = getComponentAncestors(catalogueTypes, componentName);

                if ("component_type".equals(componentType))
                {
                    catalogueTypes.put(componentName,
                        new ComponentTypeImpl(componentName, componentFields, naturalKeyFields,
                            packageName + "." + StringUtils.toCamelCaseUpper(componentName), ancestors));
                }
                else if ("view_type".equals(componentType))
                {
                    catalogueTypes.put(componentName,
                        new ViewTypeImpl(componentName, componentFields, naturalKeyFields,
                            packageName + "." + StringUtils.toCamelCaseUpper(componentName) + "Impl", ancestors));
                }
                else if ("entity_type".equals(componentType))
                {
                    EntityTypeImpl entityType =
                        new EntityTypeImpl(componentName, componentFields, naturalKeyFields,
                            packageName + "." + StringUtils.toCamelCaseUpper(componentName), ancestors);

                    if (supportsExternalId(componentName))
                    {
                        entityType.setExternalIdFlag(true);
                    }

                    catalogueTypes.put(componentName, entityType);
                }
                else if ("dimension_type".equals(componentType))
                {
                    DimensionTypeImpl dimensionType =
                        new DimensionTypeImpl(componentName, componentFields, naturalKeyFields,
                            packageName + "." + StringUtils.toCamelCaseUpper(componentName), ancestors);

                    if (supportsExternalId(componentName))
                    {
                        dimensionType.setExternalIdFlag(true);
                    }

                    catalogueTypes.put(componentName, dimensionType);
                }
                else if ("fact_type".equals(componentType))
                {
                    catalogueTypes.put(componentName,
                        new FactTypeImpl(componentName, componentFields,
                            packageName + "." + StringUtils.toCamelCaseUpper(componentName), ancestors));
                }
            }
        }

        // Go through all component types in the catalogue, looking for fields that contain pending forward references
        // to other components, and views that are forward references to other components, and now that all components
        // have been added to the catalogue, resolve these pending references.
        for (Type type : catalogueTypes.values())
        {
            if (type instanceof ComponentType)
            {
                ComponentType componentType = (ComponentType) type;

                // Replace all field forward references.
                for (String fieldName : componentType.getAllPropertyNames())
                {
                    Type fieldType = componentType.getPropertyType(fieldName);

                    if (fieldType instanceof PendingComponentRefType)
                    {
                        Type replacementType = catalogueTypes.get(fieldType.getName());
                        componentType.setPropertyType(fieldName, replacementType);

                        if (replacementType == null)
                        {
                            log.warn("Replaced pending reference to '" + fieldType.getName() + "' with " +
                                replacementType);
                        }
                    }
                    else if (fieldType instanceof CollectionType)
                    {
                        CollectionType collectionType = (CollectionType) fieldType;
                        Type elementType = collectionType.getElementType();

                        if (elementType instanceof PendingComponentRefType)
                        {
                            PendingComponentRefType pendingType = (PendingComponentRefType) elementType;

                            Type replacementType = catalogueTypes.get(pendingType.getName());
                            collectionType.setElementType(replacementType);

                            if (replacementType == null)
                            {
                                log.warn("Replaced pending reference on collection to '" + pendingType.getName() +
                                    "' with " + replacementType);
                            }
                        }
                    }
                }

                // Replace all view forward references.
                Set<ComponentType> replacementAncestors = new HashSet<ComponentType>();

                for (ComponentType viewType : componentType.getImmediateAncestors())
                {
                    if (viewType instanceof PendingComponentType)
                    {
                        viewType = (ComponentType) catalogueTypes.get(viewType.getName());
                    }

                    replacementAncestors.add(viewType);
                }

                componentType.setImmediateAncestors(replacementAncestors);
            }
        }
    }

    /**
     * Runs a query against the first order logic model. The variable bindings resulting from the query are presented in
     * a map from strings (non-interned) to variables.
     *
     * @param  queryString The query as a string.
     *
     * @return An iterable over solutions as a map of variable names to variables.
     */
    private Iterable<Map<String, Variable>> runQuery(String queryString)
    {
        engine.setTokenSource(TokenSource.getTokenSourceForString(queryString));

        try
        {
            engine.compile(engine.parse());
        }
        catch (SourceCodeException e)
        {
            // If the query fails to parse or link, then this is a non-recoverable bug, so is reported as a runtime
            // exception.
            throw new RuntimeException("The query, " + queryString + ", failed to compile.", e);
        }

        return engine.expandResultSetToMap(engine.iterator());
    }

    /**
     * Converts types extracted from the XML catalogue model into first order logic clauses that encapsulate all the
     * parameters that make up the type. For each type definiction in the catalogue model, the fields are extracted and
     * passed through the custom handlers in turn, until one of them applies a transformation to the field. If none of
     * the custom handlers applies a transformation, then the {@link DefaultFieldHandler} is used to transform the field
     * into a name(value) functor.
     *
     * @param <T>            The type of types to extract.
     * @param catalogueDef   The catalogue definition to extract types from.
     * @param parser         The parser to parser terms with.
     * @param clauses        The list of clauses to accumulate parsed clauses in.
     * @param typeClass      The class of types to extract.
     * @param properties     The properties to extract from the type bean.
     * @param customHandlers A chain of custom handlers to apply to fields of type definitions, where the general
     */
    private <T extends TypeDefType> void convertTypeToTerm(CatalogueDefinition catalogueDef,
        Parser<Clause, Token> parser, List<Sentence<Clause>> clauses, Class<T> typeClass, String[] properties,
        FieldHandler... customHandlers)
    {
        // Create an instance of the default field handler.
        FieldHandler defaultHandler = new DefaultFieldHandler();

        // Loop over all instances of the specified type class found in the model.
        for (TypeDefType type : SetupModelHelper.getAllTypeDefsOfType(typeClass, catalogueDef))
        {
            // Get the name of the type class.
            String kind = engine.getFunctorName(typeClassToAtom(type));

            // If the type is not known then ignore it and move on to the next one.
            if (kind == null)
            {
                continue;
            }

            // Wrap the type as a bean state so that its properties can be extracted by name.
            WrappedBeanState typeBean = new WrappedBeanState(type);

            // Get the name of the type.
            String name = (String) typeBean.getProperty("name");

            // Build the first part of the type instance clause.
            String termText = "type_instance(" + name + ", " + kind + ", [";

            // Extract all the specified properties from the type bean, filtering out any nulls.
            Map<String, Object> nonNullProperties = new HashArray<String, Object>();

            for (String property : properties)
            {
                if (typeBean.hasProperty(property))
                {
                    Object value = typeBean.getProperty(property);

                    if (value != null)
                    {
                        nonNullProperties.put(property, value);
                    }
                }
            }

            // Build the rest of the type instance clause from the non-null properties.
            for (Iterator<Map.Entry<String, Object>> i = nonNullProperties.entrySet().iterator(); i.hasNext();)
            {
                Map.Entry<String, Object> entry = i.next();
                String property = entry.getKey();
                Object value = entry.getValue();

                // Chain the raw property value down to the custom field handler if one is defined.
                boolean handled = false;

                for (FieldHandler handler : customHandlers)
                {
                    String result = handler.handleField(property, value, i.hasNext());

                    if (result != null)
                    {
                        handled = true;
                        termText += result;

                        // Break out once one handler has responded.
                        break;
                    }
                }

                // If the custom handler did not handle the field then use the default.
                if (!handled)
                {
                    termText += defaultHandler.handleField(property, value, i.hasNext());
                }
            }

            termText += "]).";
            log.debug(termText);

            // CC the raw model text to the specified raw model writer, only if one was set.
            if (modelWriter != null)
            {
                try
                {
                    modelWriter.write(termText + "\n");
                    modelWriter.flush();
                }
                catch (IOException e)
                {
                    throw new RuntimeException("Error whilst writing out the raw model.", e);
                }
            }

            // Parse the instance clause into a prolog clause and add it to the list of clauses.
            parser.setTokenSource(TokenSource.getTokenSourceForString(termText));

            try
            {
                Sentence<Clause> sentence = parser.parse();
                clauses.add(sentence);
            }
            catch (SourceCodeException e)
            {
                throw new RuntimeException("Badly formed typedef conversion to logical term.", e);
            }
        }
    }

    /**
     * Provides the prolog atom name for the specified type class.
     *
     * @param  type An instance of the type class to get the atom for.
     *
     * @return The atom name of the types representation in prolog.
     */
    private Functor typeClassToAtom(TypeDefType type)
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

    /**
     * LabelState provides exposes recursively defined hierarchy labels as traversable state spaces. By searching down
     * to every leaf in the state space, all possible label paths can be found for a hierarchy.
     *
     * <p/>A label term is a recursive functor, with name 'label', the value of the label at its level of definition the
     * first argument of the functor, and subsequest arguments, child labels below the current one. LabelState
     * implements {@link GoalState} with all leaf labelling as goal by default.
     */
    private class LabelState extends TraversableState<LabelState> implements GoalState
    {
        /** Holds the label term for this state. */
        private Functor labelTerm;

        /** Holds the label path to this state. */
        private String[] labelPath;

        /** Set to indicate whether this state corresponds to a leaf labelling. */
        protected boolean isLeaf;

        /**
         * Creates a traversable state for the specified label term.
         *
         * @param labelTerm The label term to create a traversable state for.
         */
        public LabelState(Functor labelTerm)
        {
            this.labelTerm = labelTerm;
            labelPath = new String[] { ((StringLiteral) labelTerm.getArgument(0)).stringValue() };
            isLeaf = labelTerm.getArity() <= 1;
        }

        /**
         * Creates a traversable state for the specified label term.
         *
         * @param labelTerm The label term to create a traversable state for.
         * @param labelPath The label path of the parent state, that this state extends with a child label.
         */
        private LabelState(Functor labelTerm, String[] labelPath)
        {
            this.labelTerm = labelTerm;
            this.labelPath = Arrays.copyOf(labelPath, labelPath.length + 1);
            this.getLabelPath()[labelPath.length] = ((StringLiteral) labelTerm.getArgument(0)).stringValue();
            isLeaf = labelTerm.getArity() <= 1;
        }

        /**
         * {@inheritDoc}
         *
         * <p/>This is a goal state if it is a leaf label.
         */
        public boolean isGoal()
        {
            return isLeaf;
        }

        /** {@inheritDoc} */
        public Traversable<LabelState> getChildStateForOperator(Operator<LabelState> op)
        {
            return op.getOp();
        }

        /** {@inheritDoc} */
        public float costOf(Operator op)
        {
            return 0;
        }

        /**
         * {@inheritDoc}
         *
         * <p/>Checks if the label term has more than one argument, and for each child label argument creates an
         * operator leadning to a new state for that child label.
         */
        public Iterator<Operator<LabelState>> validOperators(boolean reverse)
        {
            // Check if the label has any child labels.
            if (!isLeaf)
            {
                List<Operator<LabelState>> childLabels = new LinkedList<Operator<LabelState>>();

                for (int i = 1; i < labelTerm.getArity(); i++)
                {
                    childLabels.add(new OperatorImpl<LabelState>(
                            new LabelState((Functor) labelTerm.getArgument(i), labelPath)));
                }

                return childLabels.iterator();
            }
            else
            {
                return new EmptyIterator<Operator<LabelState>>();
            }
        }

        /**
         * Provides the label path to this state.
         *
         * @return The label path to this state.
         */
        public String[] getLabelPath()
        {
            return labelPath;
        }
    }

    /**
     * PendingComponentRefType is a type temporarily standing in for a reference from one component type to another,
     * that needs to become resolved onto the actual type once the type of the refered to component becomes fully known.
     *
     * <p/>Components may contain references to other components, or collections of components, and these references may
     * be circular or forward references in the catalogue model. When a component reference is encountered a pending
     * reference is first created for it, then once all component types have been extracted from the model, the pending
     * references can be fully resolved.
     *
     * <pre><p/><table id="crc"><caption>CRC Card</caption>
     * <tr><th>Responsibilities <th>Collaborations
     * <tr><td>Stand in as a reference to a component type that has not been extracted from the model yet.
     * </table></pre>
     */
    private class PendingComponentRefType extends BaseType implements Type
    {
        /** Holds the name of the refered to but as yet unresolved component. */
        private String name;

        /**
         * Creates a pending resolution reference to the named component.
         *
         * @param name The name of the component type that this is a pending reference to.
         */
        private PendingComponentRefType(String name)
        {
            this.name = name;
        }

        /** {@inheritDoc} */
        public Object getDefaultInstance()
        {
            return null;
        }

        /** {@inheritDoc} */
        public String getName()
        {
            return name;
        }

        /** {@inheritDoc} */
        public Class getBaseClass()
        {
            return null;
        }

        /** {@inheritDoc} */
        public String getBaseClassName()
        {
            return null;
        }

        /** {@inheritDoc} */
        public int getNumPossibleValues()
        {
            return -1;
        }

        /** {@inheritDoc} */
        public Set getAllPossibleValuesSet() throws InfiniteValuesException
        {
            throw new UnsupportedOperationException(
                "'getAllPossibleValuesSet' is not supported on PendingComponentRefType.");
        }

        /** {@inheritDoc} */
        public Iterator getAllPossibleValuesIterator() throws InfiniteValuesException
        {
            throw new UnsupportedOperationException(
                "'getAllPossibleValuesIterator' is not supported on PendingComponentRefType.");
        }
    }

    /**
     * PendingComponentType is a type temporarily standing in for component type that needs to become resolved onto the
     * actual type once the type of the refered to component becomes fully known.
     *
     * <p/>Components may contain references to other components, or collections of components, and these references may
     * be circular or forward references in the catalogue model. When a component reference is encountered a pending
     * reference is first created for it, then once all component types have been extracted from the model, the pending
     * references can be fully resolved.
     *
     * <pre><p/><table id="crc"><caption>CRC Card</caption>
     * <tr><th>Responsibilities <th>Collaborations
     * <tr><td>Stand in as a reference to a component type that has not been extracted from the model yet.
     * </table></pre>
     */
    private class PendingComponentType extends PendingComponentRefType implements ComponentType
    {
        /**
         * Creates a pending resolution reference to the named component.
         *
         * @param name The name of the component type that this is a pending reference to.
         */
        private PendingComponentType(String name)
        {
            super(name);
        }

        /** {@inheritDoc} */
        public Map<String, Type> getAllPropertyTypes()
        {
            return null;
        }

        /** {@inheritDoc} */
        public State getInstance()
        {
            return null;
        }

        /** {@inheritDoc} */
        public Type getPropertyType(String name)
        {
            return null;
        }

        /** {@inheritDoc} */
        public void setPropertyType(String name, Type type)
        {
        }

        /** {@inheritDoc} */
        public Set<String> getAllPropertyNames()
        {
            return null;
        }

        /** {@inheritDoc} */
        public Set<String> getNaturalKeyFieldNames() {
            return null;
        }

        /** {@inheritDoc} */
        public Set<ComponentType> getImmediateAncestors()
        {
            return null;
        }

        /** {@inheritDoc} */
        public void setImmediateAncestors(Set<ComponentType> immediateAncestors)
        {
        }
    }
}

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

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import com.thesett.aima.logic.fol.IntLiteral;
import com.thesett.aima.logic.fol.NumericType;
import com.thesett.aima.logic.fol.RecursiveList;
import com.thesett.aima.logic.fol.Sentence;
import com.thesett.aima.logic.fol.StringLiteral;
import com.thesett.aima.logic.fol.Term;
import com.thesett.aima.logic.fol.Variable;
import com.thesett.aima.logic.fol.interpreter.ResolutionEngine;
import com.thesett.aima.logic.fol.isoprologparser.TokenSource;
import com.thesett.aima.logic.fol.prolog.PrologCompiledClause;
import com.thesett.aima.search.GoalState;
import com.thesett.aima.search.Operator;
import com.thesett.aima.search.SearchMethod;
import com.thesett.aima.search.Traversable;
import com.thesett.aima.search.TraversableState;
import com.thesett.aima.search.util.OperatorImpl;
import com.thesett.aima.search.util.Searches;
import com.thesett.aima.search.util.uninformed.DepthFirstSearch;
import com.thesett.aima.state.*;
import com.thesett.aima.state.impl.JavaType;
import com.thesett.catalogue.core.flathandlers.FlatEnumLabelFieldHandler;
import com.thesett.catalogue.core.flathandlers.FlatExternalIdHandler;
import com.thesett.catalogue.core.flathandlers.FlatHierarchyLabelFieldHandler;
import com.thesett.catalogue.core.flathandlers.FlatInQuotesFieldHandler;
import com.thesett.catalogue.core.flathandlers.FlatViewHandler;
import com.thesett.catalogue.core.handlers.ComponentPartHandler;
import com.thesett.catalogue.core.handlers.DocRootHandler;
import com.thesett.catalogue.core.handlers.EnumLabelFieldHandler;
import com.thesett.catalogue.core.handlers.ExternalIdHandler;
import com.thesett.catalogue.core.handlers.HierarchyLabelFieldHandler;
import com.thesett.catalogue.core.handlers.InQuotesFieldHandler;
import com.thesett.catalogue.core.handlers.ViewHandler;
import com.thesett.catalogue.model.CollectionType;
import com.thesett.catalogue.model.EntityType;
import com.thesett.catalogue.model.impl.CatalogueModel;
import com.thesett.catalogue.model.impl.CollectionTypeImpl;
import com.thesett.catalogue.model.impl.ComponentTypeImpl;
import com.thesett.catalogue.model.impl.DimensionTypeImpl;
import com.thesett.catalogue.model.impl.EntityTypeImpl;
import com.thesett.catalogue.model.impl.FactTypeImpl;
import com.thesett.catalogue.model.impl.MapTypeImpl;
import com.thesett.catalogue.model.impl.Relationship;
import static com.thesett.catalogue.model.impl.Relationship.Arity.Many;
import static com.thesett.catalogue.model.impl.Relationship.Arity.One;
import com.thesett.catalogue.model.impl.ViewTypeImpl;
import com.thesett.catalogue.setup.CatalogueDefinition;
import com.thesett.catalogue.setup.ComponentDefType;
import com.thesett.catalogue.setup.DateRangeType;
import com.thesett.catalogue.setup.DecimalType;
import com.thesett.catalogue.setup.EnumerationDefType;
import com.thesett.catalogue.setup.HierarchyDefType;
import com.thesett.catalogue.setup.IntegerRangeType;
import com.thesett.catalogue.setup.SetupModelHelper;
import com.thesett.catalogue.setup.StringPatternType;
import com.thesett.catalogue.setup.TimeRangeType;
import com.thesett.common.parsing.SourceCodeException;
import com.thesett.common.util.EmptyIterator;
import com.thesett.common.util.StringUtils;

import org.apache.log4j.Logger;

/**
 * CatalogueModelFactory provides queries to type check the catalogue model, which requires greater sophistication than
 * can be provided by XML schema validation of the model in its raw XML form. The type checking of the catalogue model
 * to a normal form ensures that where it is possible to define things with identical semantics in multiple ways using
 * the raw syntax of the model, syntactical differences are reduced to semantic equivalences where possible.
 *
 * <p/>The input to the catalogue model checker is a raw {@link CatalogueDefinition}. The output is a model, containing
 * the types from the raw model that type check and have been reduced to their canonical form.
 *
 * <p/>The factory outputs the results of type checking and reduction to normal as a
 * {@link com.thesett.catalogue.model.impl.CatalogueModel}.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Reduce the raw catalogue model to its canonical form. <td> {@link CatalogueDefinition}
 * <tr><td> Type check the catalogue model. <td> {@link CatalogueDefinition}
 * <tr><td> Produce a CatalogueModel containing the results. <td> {@link com.thesett.catalogue.model.impl.CatalogueModel}.
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
        new LinkedHashMap<String, Type>()
        {
            {
                put("boolean", JavaType.BOOLEAN_TYPE);
                put("integer", JavaType.INTEGER_TYPE);
                put("real", JavaType.FLOAT_TYPE);
                put("string", JavaType.STRING_TYPE);
                put("date", new JavaType(DateOnly.class));
                put("time", new JavaType(TimeOnly.class));
                put("timestamp", new JavaType(Date.class));
            }
        };

    /** Holds a mapping from names of kinds of collections to kinds of collections. */
    private Map<String, CollectionType.CollectionKind> nameToCollectionKindMap =
        new LinkedHashMap<String, CollectionType.CollectionKind>()
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
     * normalized catalogue types as a {@link com.thesett.catalogue.model.impl.CatalogueModel}.
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

        convertTypesToTerms(clauses);

        //convertTypesToFlatTerms(new ArrayList<Sentence<Clause>>());

        // Add all the clauses to the knowledge base.
        for (Sentence<Clause> sentence : clauses)
        {
            engine.compile(sentence);
        }

        // Initialize top-level meta information about this catalogue.
        String packageName = SetupModelHelper.getPackageName(catalogueDef);

        // Used to build a mapping of all top-level types in the model by name.
        Map<String, Type> catalogueTypes = new LinkedHashMap<String, Type>();

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
     * Applies a model term builder to the raw catalogue definition to transform it into a list of first order logic
     * terms.
     *
     * @param clauses A list of clauses to place the resulting facts in.
     */
    private void convertTypesToTerms(List<Sentence<Clause>> clauses)
    {
        ModelTermBuilder builder = new ListStyleTermBuilder(engine, modelWriter);

        builder.convertTypeToTerm(catalogueDef, engine, clauses, DecimalType.class,
            new String[] { "precision", "scale", "rounding", "from", "to" });
        builder.convertTypeToTerm(catalogueDef, engine, clauses, IntegerRangeType.class, new String[] { "from", "to" });
        builder.convertTypeToTerm(catalogueDef, engine, clauses, StringPatternType.class, new String[] { "regexp" },
            new InQuotesFieldHandler(new String[] { "regexp" }));
        builder.convertTypeToTerm(catalogueDef, engine, clauses, DateRangeType.class, new String[] { "from", "to" },
            new InQuotesFieldHandler(new String[] { "from", "to" }));
        builder.convertTypeToTerm(catalogueDef, engine, clauses, TimeRangeType.class,
            new String[] { "from", "to", "step" }, new InQuotesFieldHandler(new String[] { "from", "to", "step" }));
        builder.convertTypeToTerm(catalogueDef, engine, clauses, EnumerationDefType.class, new String[] { "label" },
            new EnumLabelFieldHandler());
        builder.convertTypeToTerm(catalogueDef, engine, clauses, HierarchyDefType.class,
            new String[] { "finalized", "level", "hierarchyLabel" }, new HierarchyLabelFieldHandler());
        builder.convertTypeToTerm(catalogueDef, engine, clauses, ComponentDefType.class,
            new String[] { "componentPart", "view", "externalId", "root" }, new ComponentPartHandler(engine),
            new ViewHandler(), new ExternalIdHandler(), new DocRootHandler());
    }

    /**
     * Applies a model term builder to the raw catalogue definition to transform it into a list of first order logic
     * terms.
     *
     * @param clauses A list of clauses to place the resulting facts in.
     */
    private void convertTypesToFlatTerms(List<Sentence<Clause>> clauses)
    {
        ModelTermBuilder builder = new FlatStyleTermBuilder(engine, new OutputStreamWriter(System.out));

        builder.convertTypeToTerm(catalogueDef, engine, clauses, DecimalType.class,
            new String[] { "precision", "scale", "rounding", "from", "to" });
        builder.convertTypeToTerm(catalogueDef, engine, clauses, IntegerRangeType.class, new String[] { "from", "to" });
        builder.convertTypeToTerm(catalogueDef, engine, clauses, StringPatternType.class, new String[] { "regexp" },
            new FlatInQuotesFieldHandler(new String[] { "regexp" }));
        builder.convertTypeToTerm(catalogueDef, engine, clauses, DateRangeType.class, new String[] { "from", "to" },
            new FlatInQuotesFieldHandler(new String[] { "from", "to" }));
        builder.convertTypeToTerm(catalogueDef, engine, clauses, TimeRangeType.class,
            new String[] { "from", "to", "step" }, new FlatInQuotesFieldHandler(new String[] { "from", "to", "step" }));
        builder.convertTypeToTerm(catalogueDef, engine, clauses, EnumerationDefType.class, new String[] { "label" },
            new FlatEnumLabelFieldHandler());
        builder.convertTypeToTerm(catalogueDef, engine, clauses, HierarchyDefType.class,
            new String[] { "finalized", "level", "hierarchyLabel" }, new FlatHierarchyLabelFieldHandler());
        builder.convertTypeToTerm(catalogueDef, engine, clauses, ComponentDefType.class,
            new String[] { "componentPart", "view", "externalId" }, new ComponentPartHandler(engine),
            new FlatViewHandler(), new FlatExternalIdHandler());
    }

    /**
     * Extracts the fields and their types for a named component type in the catalogue model.
     *
     * @param  catalogueTypes The map to build up the catalogue types in.
     * @param  name           The name of the component type to get the fields of.
     *
     * @return The fields and types of a named component type.
     */
    private Map<String, FieldProperties> getComponentFields(Map<String, Type> catalogueTypes, String name)
    {
        String queryString =
            "?-product_type(_PT), normal_type(_PT, " + name +
            ", class, _MP), member(fields(_FS), _MP), member(F, _FS).";
        Iterable<Map<String, Variable>> fieldBindingsIterable = runQuery(queryString);

        Map<String, FieldProperties> results = new LinkedHashMap<String, FieldProperties>();

        for (Map<String, Variable> variables : fieldBindingsIterable)
        {
            Variable var = variables.get("F");
            Functor fieldFunctor = (Functor) var.getValue();

            String fieldKind = engine.getFunctorName(fieldFunctor);

            if ("property".equals(fieldKind))
            {
                String fieldName = engine.getFunctorName((Functor) fieldFunctor.getArgument(0));
                String fieldTypeName = engine.getFunctorName((Functor) fieldFunctor.getArgument(1));
                Term presentAsTerm = fieldFunctor.getArgument(2).getValue();

                String presentAsName = null;

                if (presentAsTerm instanceof StringLiteral)
                {
                    String presentAs = ((StringLiteral) presentAsTerm).stringValue();

                    if ((presentAs != null) && !presentAs.equals(fieldName))
                    {
                        presentAsName = presentAs;
                    }
                }

                // Check if the type of the field is recognized as a basic type.
                if (basicTypeNameToJavaTypeMap.containsKey(fieldTypeName))
                {
                    Type fieldType = basicTypeNameToJavaTypeMap.get(fieldTypeName);
                    results.put(fieldName, new FieldProperties(fieldType, presentAsName));
                }

                // Check if the type of the field is recognized as a user defined top-level type.
                else if (catalogueTypes.containsKey(fieldTypeName))
                {
                    Type fieldType = catalogueTypes.get(fieldTypeName);
                    results.put(fieldName, new FieldProperties(fieldType, presentAsName));
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
                    results.put(fieldName, new FieldProperties(fieldType, null));
                }

                // Otherwise, the type is assumed to refer to a yet to be processed user type.
                else
                {
                    Type fieldType = new PendingComponentRefType(fieldTypeName);
                    results.put(fieldName, new FieldProperties(fieldType, null));
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

                    MapTypeImpl fieldType = new MapTypeImpl(keyType, elementType, ArrayList.class);
                    results.put(fieldName, new FieldProperties(fieldType, null));
                }

                // Otherwise the field is a non-map collection type, so create a collection type for the field.
                else
                {
                    CollectionTypeImpl fieldType = new CollectionTypeImpl(elementType, ArrayList.class, collectionKind);
                    results.put(fieldName, new FieldProperties(fieldType, null));
                }
            }
        }

        return results;
    }

    /**
     * Extracts the natural key fields for a named component type in the catalogue model. Not all components have
     * natural key fields, in which case the resulting set of fields will be empty.
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

        Set<String> results = new LinkedHashSet<String>();

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

        Set<ComponentType> results = new LinkedHashSet<ComponentType>();

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
     * Queries the relationships between components, to find which components have no incoming relationship links by
     * composition, and are therefore not wholly owned by other components and can be considered to be top-level.
     *
     * @param catalogueTypes The types in the catalogue to extract the top-level feature of.
     */
    private void initializeAllTopLevel(Map<String, Type> catalogueTypes)
    {
        // Query for all top-level entities.
        String queryString = "?-top_level_entity(Name).";
        Iterable<Map<String, Variable>> fieldBindingsIterable = runQuery(queryString);

        for (Map<String, Variable> variables : fieldBindingsIterable)
        {
            String componentName = engine.getFunctorName((Functor) variables.get("Name").getValue());

            // Check if the top-level entity was in the list of types to extract.
            Type type = catalogueTypes.get(componentName);

            if (type instanceof EntityType)
            {
                ComponentType entityType = (EntityType) type;
                State metaModel = entityType.getMetaModel();
                metaModel.setProperty("topLevel", true);
            }
        }
    }

    /**
     * Queries the relationships between components, to discover what the nature of those relationship is.
     *
     * @param catalogueTypes The types in the catalogue to extract the relationships of.
     */
    private void initializeAllRelationships(Map<String, Type> catalogueTypes)
    {
        String queryString = "?-related(From, To, Direction, Component, OtherComponent, Field, TargetField, Owner).";
        Iterable<Map<String, Variable>> fieldBindingsIterable = runQuery(queryString);

        for (Map<String, Variable> variables : fieldBindingsIterable)
        {
            String componentName = engine.getFunctorName((Functor) variables.get("Component").getValue());
            String fieldName = engine.getFunctorName((Functor) variables.get("Field").getValue());
            String arityFrom = engine.getFunctorName((Functor) variables.get("From").getValue());
            String arityTo = engine.getFunctorName((Functor) variables.get("To").getValue());
            Boolean biDirectional = engine.getFunctorName((Functor) variables.get("Direction").getValue()).equals("bi");
            String target = engine.getFunctorName((Functor) variables.get("OtherComponent").getValue());

            Term ownerTerm = variables.get("Owner").getValue();
            boolean owner = false;

            if (ownerTerm.isFunctor())
            {
                owner = engine.getFunctorName((Functor) ownerTerm).equals("true");
            }

            Term targetFieldTerm = variables.get("TargetField").getValue();
            String targetFieldName = null;

            if (targetFieldTerm.isFunctor())
            {
                targetFieldName = engine.getFunctorName((Functor) targetFieldTerm);
            }

            boolean alphaOrder = componentName.compareTo(target) < 0;
            Relationship.Arity from = "one".equals(arityFrom) ? One : Many;
            Relationship.Arity to = "one".equals(arityTo) ? One : Many;

            // Decide which end of the relationship is the owner. This is the end that holds the foreign key, except
            // when a relationship is uni-directional, in which case it is the end that holds the object reference.
            // Many to many relationships do not have an owner since they are symmetric.
            // Also decide how to name the relationship. In the case of one-to-one relationships and uni-directional
            // relationships put the name of the owning end first. In many-to-many relationships the choice is
            // arbitrary, so alphabetical ordering is used. For other relationships put the singular end first, since
            // it is more natural to think of an object holding a collection of some other object as the parent of
            // the relationship.
            // This feature extraction should really be handled in the Prolog code.
            boolean goesFirst = false;

            if (!biDirectional)
            {
                owner = true;
                goesFirst = true;
            }
            else if (One.equals(from) && One.equals(to))
            {
                goesFirst = owner;
            }
            else if (One.equals(from) && Many.equals(to))
            {
                owner = false;
                goesFirst = true;
            }
            else if (Many.equals(from) && One.equals(to))
            {
                owner = true;
                goesFirst = false;
            }
            else if (Many.equals(from) && Many.equals(to))
            {
                owner = false;
                goesFirst = alphaOrder;
            }

            String firstComponent = (goesFirst) ? componentName : target;
            String secondComponent = (goesFirst) ? target : componentName;
            String relationName = firstComponent + "_" + secondComponent;

            Relationship relationship =
                new Relationship(target, targetFieldName, biDirectional, from, to, owner, relationName);

            Type type = catalogueTypes.get(componentName);

            if (type instanceof EntityType)
            {
                EntityType entityType = (EntityType) type;

                Map<String, Relationship> relationships = entityType.getRelationships();
                relationships.put(fieldName, relationship);
            }
        }
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
            throw new IllegalStateException("The query, " + queryString + ", failed to compile.", e);
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
            "?-normal_type(decimal, MN, JT, _MP), member(precision(Precision), _MP), member(scale(Scale), _MP), member(from(From), _MP), member(to(To), _MP).";
        Iterable<Map<String, Variable>> bindingsIterable = runQuery(queryString);

        for (Map<String, Variable> bindings : bindingsIterable)
        {
            String typeName = engine.getFunctorName((Functor) bindings.get("MN").getValue());
            String javaTypeName = engine.getFunctorName((Functor) bindings.get("JT").getValue());
            NumericType precisionTerm = (NumericType) bindings.get("Precision").getValue();
            NumericType scaleTerm = (NumericType) bindings.get("Scale").getValue();
            Term fromTerm = bindings.get("From").getValue();
            Term toTerm = bindings.get("To").getValue();

            String from = null;

            if (fromTerm instanceof StringLiteral)
            {
                from = ((StringLiteral) fromTerm).stringValue();
            }
            else if (fromTerm instanceof IntLiteral)
            {
                from = Integer.toString(((IntLiteral) fromTerm).intValue());
            }

            if ("unbounded".equals(from))
            {
                from = null;
            }

            String to = null;

            if (toTerm instanceof StringLiteral)
            {
                to = ((StringLiteral) toTerm).stringValue();
            }
            else if (toTerm instanceof IntLiteral)
            {
                to = Integer.toString(((IntLiteral) toTerm).intValue());
            }

            if ("unbounded".equals(to))
            {
                to = null;
            }

            if ("bigdecimal".equals(javaTypeName))
            {
                catalogueTypes.put(typeName,
                    BigDecimalTypeImpl.createInstance(typeName, precisionTerm.intValue(), scaleTerm.intValue(), from,
                        to));
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
                pattern = ((StringLiteral) regexpTerm).stringValue();
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
            Iterable<Term> labelsList = (RecursiveList) labelsVar.getValue();

            Collection<String> labelNames = new LinkedList<String>();

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
        Collection<String> hierarchyNames = new LinkedList<String>();

        for (Map<String, Variable> hierarchyBindings : hierarchyBingingsIterable)
        {
            Variable nameVar = hierarchyBindings.get("MN");
            String typeName = engine.getFunctorName((Functor) nameVar.getValue());
            hierarchyNames.add(typeName);

            Variable levelsVar = hierarchyBindings.get("Lev");
            Iterable<Term> levelsList = (RecursiveList) levelsVar.getValue();

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
                SearchMethod labelSearch = new DepthFirstSearch<LabelState, LabelState>();
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

        Map<String, Collection<String>> componentNamesByType = new LinkedHashMap<String, Collection<String>>();

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
                Map<String, FieldProperties> fieldProperties = getComponentFields(catalogueTypes, componentName);

                Map<String, Type> componentFields = new LinkedHashMap<String, Type>();
                Map<String, String> presentAsAliases = new HashMap<String, String>();

                for (Map.Entry<String, FieldProperties> entry : fieldProperties.entrySet())
                {
                    componentFields.put(entry.getKey(), entry.getValue().type);

                    String presentAsName = entry.getValue().presentAsName;

                    if (presentAsName != null)
                    {
                        presentAsAliases.put(entry.getKey(), presentAsName);
                    }
                }

                Set<String> naturalKeyFields = getNaturalKeyFields(componentName);
                Set<ComponentType> ancestors = getComponentAncestors(catalogueTypes, componentName);

                if ("component_type".equals(componentType))
                {
                    catalogueTypes.put(componentName,
                        new ComponentTypeImpl(componentFields, presentAsAliases, naturalKeyFields, componentName,
                            packageName + "." + StringUtils.toCamelCaseUpper(componentName), ancestors));
                }
                else if ("view_type".equals(componentType))
                {
                    catalogueTypes.put(componentName,
                        new ViewTypeImpl(componentName, componentFields, presentAsAliases, naturalKeyFields,
                            packageName + "." + StringUtils.toCamelCaseUpper(componentName) + "Impl", ancestors));
                }
                else if ("entity_type".equals(componentType))
                {
                    EntityTypeImpl entityType =
                        new EntityTypeImpl(componentName, componentFields, presentAsAliases, naturalKeyFields,
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
                        new DimensionTypeImpl(componentName, componentFields, presentAsAliases, naturalKeyFields,
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
                        new FactTypeImpl(componentName, componentFields, presentAsAliases,
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
                            Type pendingType = (PendingComponentRefType) elementType;

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
                Set<ComponentType> replacementAncestors = new LinkedHashSet<ComponentType>();

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

        // Examine all the component relationships.
        initializeAllRelationships(catalogueTypes);
        //initializeAllTopLevel(catalogueTypes);
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
            throw new IllegalStateException("The query, " + queryString + ", failed to compile.", e);
        }

        return engine.expandResultSetToMap(engine.iterator());
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
        public Map<String, String> getPropertyPresentAsAliases()
        {
            return null;
        }

        /** {@inheritDoc} */
        public String getPropertyPresentAsAlias(String name)
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
        public Set<String> getNaturalKeyFieldNames()
        {
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

        /** {@inheritDoc} */
        public State getMetaModel()
        {
            return null;
        }
    }

    /**
     * Used to capture the properties of a field of a component.
     */
    private static class FieldProperties
    {
        public Type type;
        public String presentAsName;

        /** Used to indicate that a field is a component serialized into a document model. */
        public boolean isDocModelComponent;

        /** Used to indicate the document model format to use when serializing the field. */
        public DocModelFormat docModelFormat;

        private FieldProperties(Type type, String presentAsName)
        {
            this.type = type;
            this.presentAsName = presentAsName;
        }
    }
}

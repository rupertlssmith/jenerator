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
package com.thesett.catalogue.setup;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBElement;

import com.thesett.aima.search.GoalState;
import com.thesett.aima.search.Operator;
import com.thesett.aima.search.QueueBasedSearchMethod;
import com.thesett.aima.search.SearchNode;
import com.thesett.aima.search.SearchNotExhaustiveException;
import com.thesett.aima.search.TraversableState;
import com.thesett.aima.search.util.OperatorImpl;
import com.thesett.aima.search.util.uninformed.DepthFirstSearch;

/**
 * SetupModelHelper contains a library of reusable methods that provide assistance when working with the catalogue
 * knowledge level model.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class SetupModelHelper
{
    /**
     * Extracts the namespace of the catalogue.
     *
     * @param  definition The catalogue raw definition.
     *
     * @return The namespace of the catalogue.
     */
    public static String getPackageName(CatalogueDefinition definition)
    {
        return definition.getPackage().getName();
    }

    /**
     * Extracts all the top level type definitions from a catalogue definition matching a specified class.
     *
     * @param  <T>          The type of the type definitions to extract.
     * @param  typeDefClass The class of the type definitions to extract.
     * @param  definition   The catalogue definition.
     *
     * @return All type defs.
     */
    public static <T> List<T> getAllTypeDefsOfType(Class<T> typeDefClass, CatalogueDefinition definition)
    {
        List<T> result = new ArrayList<T>();

        // Extract all the type definitions.
        for (JAXBElement<? extends TypeDefType> element : definition.getTypeDef())
        {
            TypeDefType typeDef = element.getValue();

            if (typeDefClass.isInstance(typeDef))
            {
                result.add(typeDefClass.cast(typeDef));
            }
        }

        return result;
    }

    /**
     * Extracts all the typedefs from a catalogue definition.
     *
     * @param  definition The catalogue definition.
     *
     * @return All typedefs.
     */
    public static List<TypeDefType> getAllTypeDefs(CatalogueDefinition definition)
    {
        return getAllTypeDefsOfType(TypeDefType.class, definition);
    }

    /**
     * Extracts all the componentdefs from a catalogue definition.
     *
     * @param  definition The catalogue definition.
     *
     * @return All componentdefs.
     */
    public static List<ComponentDefType> getAllComponentDefs(CatalogueDefinition definition)
    {
        return getAllTypeDefsOfType(ComponentDefType.class, definition);
    }

    /**
     * Extracts all the dimensionDefs from a catalogue definition.
     *
     * @param  definition The catalogue definition.
     *
     * @return All dimensionDefs.
     */
    public static List<DimensionDefType> getAllDimensionDefs(CatalogueDefinition definition)
    {
        return getAllTypeDefsOfType(DimensionDefType.class, definition);
    }

    /**
     * Extracts all the entityDefs from a catalogue definition.
     *
     * @param  definition The catalogue definition.
     *
     * @return All entityDefs.
     */
    public static List<EntityDefType> getAllEntityDefs(CatalogueDefinition definition)
    {
        return getAllTypeDefsOfType(EntityDefType.class, definition);
    }

    /**
     * Extracts all the factDefs from a catalogue definition.
     *
     * @param  definition The catalogue definition.
     *
     * @return All factDefs.
     */
    public static List<FactDefType> getAllFactDefs(CatalogueDefinition definition)
    {
        return getAllTypeDefsOfType(FactDefType.class, definition);
    }

    /**
     * Extracts all the viewDefs from a catalogue definition.
     *
     * @param  definition The catalogue definition.
     *
     * @return All viewDefs.
     */
    public static List<ViewDefType> getAllViewDefs(CatalogueDefinition definition)
    {
        return getAllTypeDefsOfType(ViewDefType.class, definition);
    }

    /**
     * Extracts all the integerRanges from a catalogue definition.
     *
     * @param  definition The catalogue definition.
     *
     * @return All integerRanges.
     */
    public static List<IntegerRangeType> getAllIntegerRanges(CatalogueDefinition definition)
    {
        return getAllTypeDefsOfType(IntegerRangeType.class, definition);
    }

    /**
     * Extracts all the RealRanges from a catalogue definition.
     *
     * @param  definition The catalogue definition.
     *
     * @return All RealRanges.
     */
    public static List<RealRangeType> getAllRealRanges(CatalogueDefinition definition)
    {
        return getAllTypeDefsOfType(RealRangeType.class, definition);
    }

    /**
     * Extracts all the stringPatterns from a catalogue definition.
     *
     * @param  definition The catalogue definition.
     *
     * @return All stringPatterns.
     */
    public static List<StringPatternType> getAllStringPatterns(CatalogueDefinition definition)
    {
        return getAllTypeDefsOfType(StringPatternType.class, definition);
    }

    /**
     * Extracts all the dateRanges from a catalogue definition.
     *
     * @param  definition The catalogue definition.
     *
     * @return All dateRanges.
     */
    public static List<DateRangeType> getAllDateRanges(CatalogueDefinition definition)
    {
        return getAllTypeDefsOfType(DateRangeType.class, definition);
    }

    /**
     * Extracts all the timeRanges from a catalogue definition.
     *
     * @param  definition The catalogue definition.
     *
     * @return All timeRanges.
     */
    public static List<TimeRangeType> getAllTimeRanges(CatalogueDefinition definition)
    {
        return getAllTypeDefsOfType(TimeRangeType.class, definition);
    }

    /**
     * Extracts all the enumerationDefs from a catalogue definition.
     *
     * @param  definition The catalogue definition.
     *
     * @return All enumerationDefs.
     */
    public static List<EnumerationDefType> getAllEnumerationDefs(CatalogueDefinition definition)
    {
        return getAllTypeDefsOfType(EnumerationDefType.class, definition);
    }

    /**
     * Extracts all the hierarchyDefs from a catalogue definition.
     *
     * @param  definition The catalogue definition.
     *
     * @return All hierarchyDefs.
     */
    public static List<HierarchyDefType> getAllHierarchyDefs(CatalogueDefinition definition)
    {
        return getAllTypeDefsOfType(HierarchyDefType.class, definition);
    }

    /**
     * Gets a named type def from the model, or null if none matching the name is found.
     *
     * @param  <T>          The type of the type def to fetch.
     * @param  typeDefClass The class of the type def to fetch.
     * @param  definition   The catalogue model.
     * @param  name         The type def name to get.
     *
     * @return A named type def from the model, or null if none matching the name is found.
     */
    public static <T> T getTypeDefOfTypeByName(Class<T> typeDefClass, CatalogueDefinition definition, String name)
    {
        // Scan for the named dimension definition.
        for (JAXBElement<? extends TypeDefType> element : definition.getTypeDef())
        {
            TypeDefType typeDef = element.getValue();

            if (typeDef.getName().equals(name) && typeDefClass.isInstance(typeDef))
            {
                return typeDefClass.cast(typeDef);
            }
        }

        return null;
    }

    /**
     * Gets a named typedef from the model, or null if none matching the name is found.
     *
     * @param  definition The catalogue model.
     * @param  name       The typedef name to get.
     *
     * @return A named typedef from the model, or null if none matching the name is found.
     */
    public static TypeDefType getTypeDefByName(CatalogueDefinition definition, String name)
    {
        return getTypeDefOfTypeByName(TypeDefType.class, definition, name);
    }

    /**
     * Gets a named componentDef from the model, or null if none matching the name is found.
     *
     * @param  definition The catalogue model.
     * @param  name       The componentDef name to get.
     *
     * @return A named componentDef from the model, or null if none matching the name is found.
     */
    public static ComponentDefType getComponentDefByName(CatalogueDefinition definition, String name)
    {
        return getTypeDefOfTypeByName(ComponentDefType.class, definition, name);
    }

    /**
     * Gets a named dimensionDef from the model, or null if none matching the name is found.
     *
     * @param  definition The catalogue model.
     * @param  name       The dimensionDef name to get.
     *
     * @return A named dimensionDef from the model, or null if none matching the name is found.
     */
    public static DimensionDefType getDimensionDefByName(CatalogueDefinition definition, String name)
    {
        return getTypeDefOfTypeByName(DimensionDefType.class, definition, name);
    }

    /**
     * Gets a named entityDef from the model, or null if none matching the name is found.
     *
     * @param  definition The catalogue model.
     * @param  name       The entityDef name to get.
     *
     * @return A named entityDef from the model, or null if none matching the name is found.
     */
    public static EntityDefType getEntityDefByName(CatalogueDefinition definition, String name)
    {
        return getTypeDefOfTypeByName(EntityDefType.class, definition, name);
    }

    /**
     * Gets a named factDef from the model, or null if none matching the name is found.
     *
     * @param  definition The catalogue model.
     * @param  name       The factDef name to get.
     *
     * @return A named factDef from the model, or null if none matching the name is found.
     */
    public static FactDefType getFactDefByName(CatalogueDefinition definition, String name)
    {
        return getTypeDefOfTypeByName(FactDefType.class, definition, name);
    }

    /**
     * Gets a named viewDef from the model, or null if none matching the name is found.
     *
     * @param  definition The catalogue model.
     * @param  name       The viewDef name to get.
     *
     * @return A named viewDef from the model, or null if none matching the name is found.
     */
    public static ViewDefType getViewDefByName(CatalogueDefinition definition, String name)
    {
        return getTypeDefOfTypeByName(ViewDefType.class, definition, name);
    }

    /**
     * Gets a named integerRange from the model, or null if none matching the name is found.
     *
     * @param  definition The catalogue model.
     * @param  name       The integerRange name to get.
     *
     * @return A named integerRange from the model, or null if none matching the name is found.
     */
    public static IntegerRangeType getIntegerRangeByName(CatalogueDefinition definition, String name)
    {
        return getTypeDefOfTypeByName(IntegerRangeType.class, definition, name);
    }

    /**
     * Gets a named realRange from the model, or null if none matching the name is found.
     *
     * @param  definition The catalogue model.
     * @param  name       The realRange name to get.
     *
     * @return A named realRange from the model, or null if none matching the name is found.
     */
    public static RealRangeType getRealRangeByName(CatalogueDefinition definition, String name)
    {
        return getTypeDefOfTypeByName(RealRangeType.class, definition, name);
    }

    /**
     * Gets a named stringPattern from the model, or null if none matching the name is found.
     *
     * @param  definition The catalogue model.
     * @param  name       The stringPattern name to get.
     *
     * @return A named stringPattern from the model, or null if none matching the name is found.
     */
    public static StringPatternType getStringPatternByName(CatalogueDefinition definition, String name)
    {
        return getTypeDefOfTypeByName(StringPatternType.class, definition, name);
    }

    /**
     * Gets a named dateRange from the model, or null if none matching the name is found.
     *
     * @param  definition The catalogue model.
     * @param  name       The dateRange name to get.
     *
     * @return A named dateRange from the model, or null if none matching the name is found.
     */
    public static DateRangeType getDateRangeByName(CatalogueDefinition definition, String name)
    {
        return getTypeDefOfTypeByName(DateRangeType.class, definition, name);
    }

    /**
     * Gets a named timeRange from the model, or null if none matching the name is found.
     *
     * @param  definition The catalogue model.
     * @param  name       The timeRange name to get.
     *
     * @return A named timeRange from the model, or null if none matching the name is found.
     */
    public static TimeRangeType getTimeRangeByName(CatalogueDefinition definition, String name)
    {
        return getTypeDefOfTypeByName(TimeRangeType.class, definition, name);
    }

    /**
     * Gets a named enumerationDef from the model, or null if none matching the name is found.
     *
     * @param  definition The catalogue model.
     * @param  name       The enumerationDef name to get.
     *
     * @return A named enumerationDef from the model, or null if none matching the name is found.
     */
    public static EnumerationDefType getEnumerationDefByName(CatalogueDefinition definition, String name)
    {
        return getTypeDefOfTypeByName(EnumerationDefType.class, definition, name);
    }

    /**
     * Gets a named hierarchyDef from the model, or null if none matching the name is found.
     *
     * @param  definition The catalogue model.
     * @param  name       The hierarchyDef name to get.
     *
     * @return A named hierarchyDef from the model, or null if none matching the name is found.
     */
    public static HierarchyDefType getHierarchyDefByName(CatalogueDefinition definition, String name)
    {
        return getTypeDefOfTypeByName(HierarchyDefType.class, definition, name);
    }

    /**
     * Extracts all the field declarations from a dimension definition that are of a specific type.
     *
     * @param  <T>            The type of the component part to extract.
     * @param  attributeClass The class of the type of the component part to extract.
     * @param  componentDef   The component definition.
     *
     * @return A list of all int attribute declarations in the dimension definition.
     */
    public static <T> List<T> getAllAttributesOfType(Class<T> attributeClass, ComponentDefType componentDef)
    {
        List<T> result = new ArrayList<T>();

        for (JAXBElement<? extends ComponentPartType> element : componentDef.getComponentPart())
        {
            ComponentPartType part = element.getValue();

            if (attributeClass.isInstance(part))
            {
                result.add(attributeClass.cast(part));
            }
        }

        return result;
    }

    /**
     * Extracts all the integer field declarations from a dimension definition.
     *
     * @param  componentDef The dimension definition.
     *
     * @return A list of all integer attribute declarations in the dimension definition.
     */
    public static List<BooleanType> getAllBooleanAttributes(ComponentDefType componentDef)
    {
        return getAllAttributesOfType(BooleanType.class, componentDef);
    }

    /**
     * Extracts all the integer field declarations from a dimension definition.
     *
     * @param  componentDef The dimension definition.
     *
     * @return A list of all integer attribute declarations in the dimension definition.
     */
    public static List<IntegerType> getAllIntegerAttributes(ComponentDefType componentDef)
    {
        return getAllAttributesOfType(IntegerType.class, componentDef);
    }

    /**
     * Extracts all the real field declarations from a dimension definition.
     *
     * @param  componentDef The dimension definition.
     *
     * @return A list of all real attribute declarations in the dimension definition.
     */
    public static List<RealType> getAllRealAttributes(ComponentDefType componentDef)
    {
        return getAllAttributesOfType(RealType.class, componentDef);
    }

    /**
     * Extracts all the string field declarations from a dimension definition.
     *
     * @param  componentDef The dimension definition.
     *
     * @return A list of all string attribute declarations in the dimension definition.
     */
    public static List<StringType> getAllStringAttributes(ComponentDefType componentDef)
    {
        return getAllAttributesOfType(StringType.class, componentDef);
    }

    /**
     * Extracts all the date field declarations from a dimension definition.
     *
     * @param  componentDef The dimension definition.
     *
     * @return A list of all date attribute declarations in the dimension definition.
     */
    public static List<DateType> getAllDateAttributes(ComponentDefType componentDef)
    {
        return getAllAttributesOfType(DateType.class, componentDef);
    }

    /**
     * Extracts all the time field declarations from a dimension definition.
     *
     * @param  componentDef The dimension definition.
     *
     * @return A list of all time attribute declarations in the dimension definition.
     */
    public static List<TimeType> getAllTimeAttributes(ComponentDefType componentDef)
    {
        return getAllAttributesOfType(TimeType.class, componentDef);
    }

    /**
     * Extracts all the enumeration field declarations from a dimension definition.
     *
     * @param  componentDef The dimension definition.
     *
     * @return A list of all enumeration attribute declarations in the dimension definition.
     */
    public static List<EnumerationType> getAllEnumerationAttributes(ComponentDefType componentDef)
    {
        return getAllAttributesOfType(EnumerationType.class, componentDef);
    }

    /**
     * Extracts all the hierarchy field declarations from a dimension definition.
     *
     * @param  componentDef The dimension definition.
     *
     * @return A list of all hierarchy attribute declarations in the dimension definition.
     */
    public static List<HierarchyType> getAllHierarchyAttributes(ComponentDefType componentDef)
    {
        return getAllAttributesOfType(HierarchyType.class, componentDef);
    }

    /**
     * Finds a int attribute by name from a dimension definition.
     *
     * @param  <T>            The type of the attribute to find.
     * @param  attributeClass The class of the attribute to find.
     * @param  componentDef   The dimension definition.
     * @param  name           The name of the intattribute attribute to find.
     *
     * @return The matching intattribute or null if none is found.
     */
    public static <T> T getAttributeOfTypeByName(Class<T> attributeClass, ComponentDefType componentDef, String name)
    {
        for (JAXBElement<? extends ComponentPartType> element : componentDef.getComponentPart())
        {
            ComponentPartType part = element.getValue();

            if (attributeClass.isInstance(part))
            {
                // Only collection types and field decl types have named so the component needs to be one of them.
                if (part instanceof CollectionType)
                {
                    CollectionType collection = (CollectionType) part;

                    if (collection.getName().equals(name))
                    {
                        return attributeClass.cast(part);
                    }
                }
                else if (part instanceof FieldDeclrType)
                {
                    FieldDeclrType fieldDeclr = (FieldDeclrType) part;

                    if (fieldDeclr.getName().equals(name))
                    {
                        return attributeClass.cast(part);
                    }
                }
            }
        }

        return null;
    }

    /**
     * Finds a boolean attribute by name from a dimension definition.
     *
     * @param  componentDef The dimension definition.
     * @param  name         The name of the boolean attribute to find.
     *
     * @return The matching boolean attribute or null if none is found.
     */
    public static BooleanType getBooleanAttributeByName(ComponentDefType componentDef, String name)
    {
        return getAttributeOfTypeByName(BooleanType.class, componentDef, name);
    }

    /**
     * Finds a integer attribute by name from a dimension definition.
     *
     * @param  componentDef The dimension definition.
     * @param  name         The name of the integer attribute to find.
     *
     * @return The matching integer attribute or null if none is found.
     */
    public static IntegerType getIntegerAttributeByName(ComponentDefType componentDef, String name)
    {
        return getAttributeOfTypeByName(IntegerType.class, componentDef, name);
    }

    /**
     * Finds a real attribute by name from a dimension definition.
     *
     * @param  componentDef The dimension definition.
     * @param  name         The name of the real attribute to find.
     *
     * @return The matching real attribute or null if none is found.
     */
    public static RealType getRealAttributeByName(ComponentDefType componentDef, String name)
    {
        return getAttributeOfTypeByName(RealType.class, componentDef, name);
    }

    /**
     * Finds a string attribute by name from a dimension definition.
     *
     * @param  componentDef The dimension definition.
     * @param  name         The name of the string attribute to find.
     *
     * @return The matching string attribute or null if none is found.
     */
    public static StringType getStringAttributeByName(ComponentDefType componentDef, String name)
    {
        return getAttributeOfTypeByName(StringType.class, componentDef, name);
    }

    /**
     * Finds a date attribute by name from a dimension definition.
     *
     * @param  componentDef The dimension definition.
     * @param  name         The name of the date attribute to find.
     *
     * @return The matching date attribute or null if none is found.
     */
    public static DateType getDateAttributeByName(ComponentDefType componentDef, String name)
    {
        return getAttributeOfTypeByName(DateType.class, componentDef, name);
    }

    /**
     * Finds a time attribute by name from a dimension definition.
     *
     * @param  componentDef The dimension definition.
     * @param  name         The name of the time attribute to find.
     *
     * @return The matching time attribute or null if none is found.
     */
    public static TimeType getTimeAttributeByName(ComponentDefType componentDef, String name)
    {
        return getAttributeOfTypeByName(TimeType.class, componentDef, name);
    }

    /**
     * Finds a enumeration attribute by name from a dimension definition.
     *
     * @param  componentDef The dimension definition.
     * @param  name         The name of the enumeration attribute to find.
     *
     * @return The matching enumeration attribute or null if none is found.
     */
    public static EnumerationType getEnumerationAttributeByName(ComponentDefType componentDef, String name)
    {
        return getAttributeOfTypeByName(EnumerationType.class, componentDef, name);
    }

    /**
     * Finds a hierarchy attribute by name from a dimension definition.
     *
     * @param  componentDef The dimension definition.
     * @param  name         The name of the hierarchy attribute to find.
     *
     * @return The matching hierarchy attribute or null if none is found.
     */
    public static HierarchyType getHierarchyAttributeByName(ComponentDefType componentDef, String name)
    {
        return getAttributeOfTypeByName(HierarchyType.class, componentDef, name);
    }

    /**
     * Extracts all the level names from a hierarchy type definition.
     *
     * @param  hierarchyDef A hierarchy type definition.
     *
     * @return A list of the level names.
     */
    public static List<String> extractLevelNames(HierarchyDefType hierarchyDef)
    {
        // Extract all level names for the hierarchy.
        List<String> levels = new ArrayList<String>();
        Level level = hierarchyDef.getLevel();

        while (level != null)
        {
            levels.add(level.getName());
            level = level.getLevel();
        }

        return levels;
    }

    /**
     * Extract all the allowable label paths from a hierarchy type definition.
     *
     * @param  hierarchyDef The hierarchy type definition.
     *
     * @return All the allowable label paths from the hierarchy type definition.
     */
    public static Set<String[]> extractLabelValues(HierarchyDefType hierarchyDef)
    {
        // Extract all the possible hierarchy label values by walking over the hierarchy labels, depth first,
        // taking as goal states either leaf labels or those explicitly marked as allowable.
        Set<String[]> allLabelPaths = new LinkedHashSet<String[]>();

        // Return the empty set if no labels have been specified.
        if (hierarchyDef.getHierarchyLabel() == null)
        {
            return allLabelPaths;
        }

        // Create the search starting point.
        HierarchyLabelType label = hierarchyDef.getHierarchyLabel();
        List<String> initialPath = new ArrayList<String>();
        initialPath.add(label.getName());

        // Create a depth first search method.
        QueueBasedSearchMethod<String, HierarchyLabelState> search =
            new DepthFirstSearch<String, HierarchyLabelState>();
        search.reset();
        search.addStartState(new HierarchyLabelState(label, initialPath));

        SearchNode<String, HierarchyLabelState> goal;

        try
        {
            do
            {
                goal = search.findGoalPath();

                // Check if a goal state was found.
                if (goal != null)
                {
                    HierarchyLabelState goalLabel = goal.getState();
                    List<String> goalPath = goalLabel.getPath();

                    allLabelPaths.add(goalPath.toArray(new String[goalPath.size()]));
                }
            }
            while (goal != null);
        }
        catch (SearchNotExhaustiveException e)
        {
            // There should not be any search failure exceptions, rethrow as runtimes if there are.
            throw new RuntimeException(e);
        }

        return allLabelPaths;
    }

    /**
     * Models the state of a search over a tree of hierarchy label elements for allowable hierarchy attributes. Each
     * state consists of a hierarchy label within a tree, and a path from the root to that label that contains all the
     * labels encountered on the way. Goal states are labels that are leafs (should be allowable). Possible transtions
     * to new states exist for each child label of a label.
     *
     * @todo Change the goal state so that all allowable labels are goal states, not just the leafs.
     */
    public static class HierarchyLabelState extends TraversableState<String> implements GoalState
    {
        /** Holds the underlying label element for this point in the search. */
        private HierarchyLabelType label;

        /** Holds the label name path to this point in the search. */
        private List<String> path;

        /**
         * Creates a search state along a label path.
         *
         * @param label The hierarchy label element at this state.
         * @param path  The label path to reach this state.
         */
        public HierarchyLabelState(HierarchyLabelType label, List<String> path)
        {
            // log.debug("public HierarchyLabelState(HierarchyLabel label, List<String> path): called");

            this.label = label;
            this.path = path;

            // log.debug(this);
        }

        /**
         * A state is a goal state if its hierarchy label is explicitly marked allowable, or is a leaf.
         *
         * @return <tt>true</tt> if the hierarchy label is a leaf or allowable.
         */
        public boolean isGoal()
        {
            // log.debug("public boolean isGoal(): called");
            // log.debug(this);

            // Test for leafs (allowable attribute not supported yet).
            boolean isGoal = label.getHierarchyLabel().isEmpty();

            // .debug("isGoal = " + isGoal);

            return isGoal;
        }

        /**
         * Returns the path to this state.
         *
         * @return The path to this state.
         */
        public List<String> getPath()
        {
            return path;
        }

        /**
         * Gets the names of all the child hierarchy label elements of this one.
         *
         * @param  reverse When set, indicates that the successors should be presented in reverse order. This is only
         *                 necessary if the traversal cares about the ordering of the successor states, and is used to
         *                 generate intuitive, left-to-right goal checking in depth first search based searches.
         *
         * @return The names of all the child hierarchy label elements of this one.
         */
        public Iterator<Operator<String>> validOperators(boolean reverse)
        {
            // log.debug("public Iterator<String> validOperators(): called");
            // log.debug(this);

            List<Operator<String>> labels = new ArrayList<Operator<String>>();

            for (HierarchyLabelType nextLabel : label.getHierarchyLabel())
            {
                labels.add(new OperatorImpl<String>(nextLabel.getName()));
            }

            // log.debug("validOperators() = " + labels);

            return labels.iterator();
        }

        /**
         * Returns the search state associated with the named hierarchy label element that is a child of this one.
         *
         * @param  operator The child hierarchy label element to get the search state for.
         *
         * @return The search state associated with the named hierarchy label element that is a child of this one.
         */
        public HierarchyLabelState getChildStateForOperator(Operator operator)
        {
            // log.debug("public Traversable getChildStateForOperator(String operator): called");
            // log.debug("operator = " + operator);
            // log.debug(this);

            for (HierarchyLabelType nextLabel : label.getHierarchyLabel())
            {
                if (operator.getOp().equals(nextLabel.getName()))
                {
                    List<String> newPath = new ArrayList<String>(path);
                    newPath.add(nextLabel.getName());

                    // log.debug("Generated state: label = " + nextLabel.getName() + ", path = " + newPath);

                    return new HierarchyLabelState(nextLabel, newPath);
                }
            }

            // Should never get here as only valid operator names are supplied by the validOperators method.
            return null;
        }

        /**
         * Returns the cost of the operation. Always 1.0 as not important to this search anyway.
         *
         * @param  operator The operator to get the cost for.
         *
         * @return 1.0.
         */
        public float costOf(Operator operator)
        {
            return 1.0f;
        }

        /**
         * Returns this state as a string for debugging purposes.
         *
         * @return This state as a string for debugging purposes.
         */
        public String toString()
        {
            return "label: " + label.getName() + ", path: " + path;
        }
    }
}

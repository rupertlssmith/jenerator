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
package com.thesett.catalogue.core.flathandlers;

import java.util.LinkedList;
import java.util.List;

import com.thesett.catalogue.core.FieldHandler;
import com.thesett.catalogue.setup.HierarchyLabelType;
import com.thesett.catalogue.setup.Level;

/**
 * HierarchyLabelFieldHandler transforms 'hierarchyLabel' fields into a labels/1 functors with a list of one hierarchy
 * label as its argument.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Transform a hiearchy label into a one element list. <td> {@link com.thesett.catalogue.setup.HierarchyLabelType}.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class FlatHierarchyLabelFieldHandler implements FieldHandler
{
    /**
     * {@inheritDoc}
     *
     * <p/>This transformation expects a {@link com.thesett.catalogue.setup.HierarchyLabelType} as the fields value and
     * transforms it into a recursive list of one element. This transformation only applies to 'hierarchyLabel' fields.
     */
    public String handleField(String property, Object value, boolean more)
    {
        if ("hierarchyLabel".equals(property))
        {
            // Convert the property value to a list of labels.
            HierarchyLabelType label = (HierarchyLabelType) value;

            List<HierarchyLabelType> labels = new LinkedList<HierarchyLabelType>();
            labels.add(label);

            StringBuffer labelsBuffer = new StringBuffer();

            decomposeLabels(labels, labelsBuffer);

            return "labels(" + labelsBuffer + ")" + (more ? ", " : "");

        }
        else if ("level".equals(property))
        {
            Level level = (Level) value;

            StringBuffer levelsBuffer = new StringBuffer();

            decomposeLevels(level, levelsBuffer);

            return "levels([" + levelsBuffer + "])" + (more ? ", " : "");
        }
        else if ("finalized".equals(property))
        {
            return "finalized" + (more ? ", " : "");
        }

        return null;
    }

    /**
     * Recursively walks down the lists of nested hierarchy labels, transforming them into a nested functor.
     *
     * @param labels The labels to transform into a functor.
     * @param result A string describing the recursive functor made up of the labels.
     */
    private void decomposeLabels(List<HierarchyLabelType> labels, StringBuffer result)
    {
        for (int i = 0; i < labels.size(); i++)
        {
            HierarchyLabelType label = labels.get(i);
            List<HierarchyLabelType> nextLabels = label.getHierarchyLabel();

            boolean moreLabels = i < (labels.size() - 1);
            boolean moreChildLabels = (nextLabels != null) && !nextLabels.isEmpty();

            result.append("label(\"").append(label.getName()).append("\"").append(moreChildLabels ? ", " : ")");

            if (moreChildLabels)
            {
                StringBuffer subLabels = new StringBuffer();

                decomposeLabels(nextLabels, subLabels);

                result.append(subLabels).append(")");
            }

            result.append(moreLabels ? ", " : "");
        }
    }

    /**
     * Recursively walks down a list of hierarchy levels, transforming them into a list.
     *
     * @param level  The level to walk down.
     * @param result A string describing the levels as a list of levels from top to bottom.
     */
    private void decomposeLevels(Level level, StringBuffer result)
    {
        Level nextLevel = level.getLevel();

        boolean moreLevels = nextLevel != null;

        result.append(level.getName()).append(moreLevels ? ", " : "");

        if (moreLevels)
        {
            decomposeLevels(nextLevel, result);
        }
    }
}

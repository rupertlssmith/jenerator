/*
 * © Copyright Rupert Smith, 2005 to 2013.
 *
 * ALL RIGHTS RESERVED. Any unauthorized reproduction or use of this
 * material is prohibited. No part of this work may be reproduced or
 * transmitted in any form or by any means, electronic or mechanical,
 * including photocopying, recording, or by any information storage
 * and retrieval system without express written permission from the
 * author.
 */
package com.thesett.catalogue.core.handlers;

import java.util.LinkedList;
import java.util.List;

import com.thesett.catalogue.setup.HierarchyLabelType;
import com.thesett.catalogue.setup.Level;

/**
 * HierarchyLabelFieldHandler transforms 'hierarchyLabel' fields into a labels/1 functors with a list of one
 * hierarchy label as its argument.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Transform a hiearchy label into a one element list. <td> {@link HierarchyLabelType}.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class HierarchyLabelFieldHandler implements FieldHandler
{
    /**
     * {@inheritDoc}
     *
     * <p/>This transformation expects a {@link HierarchyLabelType} as the fields value and transforms it into a
     * recursive list of one element. This transformation only applies to 'hierarchyLabel' fields.
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
     *
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
     *
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

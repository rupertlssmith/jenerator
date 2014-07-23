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
package com.thesett.catalogue.interfaces;

import java.io.Serializable;

/**
 * A DimensionType is a {@link com.thesett.aima.state.Type} that is an {@link EntityType} that, in addition to
 * the storage layout of an entity type, has a special denormalized storage layout of that sort that is used to build
 * 'star schemas' for data warehousing.
 *
 * <p/>A dimension is an 'axis of investigation' relating to a factual entry in a data warehouse. For example, if the
 * fact is a record of a completed purchase, a dimension may represent the time, date, place of purchase, item purchased,
 * customer demographic and so on relating to the purchase. Dimensions are used to query and group facts in order to
 * provide statistical answers to questions such as who?, when?, what?, where? and ultimately why? around the related
 * fact.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Provide the types and names of fields that make up an 'axis of investigation'.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public interface DimensionType extends EntityType, Serializable
{
    /** Enumeration to specify the dimension type, dimension or conformed dimension. */
    enum ConformedType implements Serializable
    {
        /** Used to indicate an ordinary dimension. */
        Dimension,

        /** Used to indicate a conformed dimension. */
        Conformed
    }
}

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

/**
 * Specifies a visitor for collection types.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Visit a collection type.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public interface CollectionTypeVisitor
{
    /**
     * Visits a collection type.
     *
     * @param <E>  The type of the elements of the collection.
     * @param type The collection type to visit.
     */
    public <E> void visit(CollectionType<E> type);
}

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
package com.thesett.index;

/**
 * IndexStore provides a mapping from index names to index implementations.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Map names to indexes.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public interface IndexStore
{
    /**
     * Retrieves a handle to the named index. If an index with this name does not already exist then a new one is
     * created.
     *
     * @param indexName The name of the index to retrieve.
     *
     * @return The named index.
     */
    TransactionalIndex getNamedIndex(String indexName);

    /**
     * Retrieves the named indexes setup instance.
     *
     * @param indexName The name of the index to get the setup instance for.
     *
     * @return The indexes setup instance.
     */
    IndexSetup getNamedIndexSetup(String indexName);
}

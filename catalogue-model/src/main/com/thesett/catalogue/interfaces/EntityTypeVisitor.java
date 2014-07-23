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
 * Specifies a visitor for entity types.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Visit an entity type.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public interface EntityTypeVisitor
{
    /**
     * Visits an entity type.
     *
     * @param type The entity type to visit.
     */
    public void visit(EntityType type);
}

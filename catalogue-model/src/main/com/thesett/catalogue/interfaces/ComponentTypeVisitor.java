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

import com.thesett.aima.state.ComponentType;

/**
 * Specifies a visitor for component types.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Visit a component type.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public interface ComponentTypeVisitor
{
    /**
     * Visits a component type.
     *
     * @param type The component type to visit.
     */
    public void visit(ComponentType type);
}

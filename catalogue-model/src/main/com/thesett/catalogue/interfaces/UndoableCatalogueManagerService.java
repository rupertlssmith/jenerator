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

import com.thesett.common.util.UndoStack;

/**
 * UndoableCatalogueManagerService defines a {@link CatalogueManagerService} with undoable operations implemented
 * as an {@link UndoStack}. This allows a save point to be created, changes made to a catalogue, then undone. This
 * is usefull when writing tests against a catalogue, as the catalogue state can automatically be restored at the end
 * of every test.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Restore catalogue state to a save point.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public interface UndoableCatalogueManagerService extends CatalogueManagerService, UndoStack
{
}

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
package com.thesett.catalogue.interfaces;

import com.thesett.common.util.UndoStack;

/**
 * UndoableCatalogueManagerService defines a {@link CatalogueManagerService} with undoable operations implemented as an
 * {@link UndoStack}. This allows a save point to be created, changes made to a catalogue, then undone. This is usefull
 * when writing tests against a catalogue, as the catalogue state can automatically be restored at the end of every
 * test.
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

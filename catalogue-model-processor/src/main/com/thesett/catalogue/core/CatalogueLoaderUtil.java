/*
 * Copyright The Sett Ltd.
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
package com.thesett.catalogue.core;

import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import com.thesett.aima.logic.fol.Clause;
import com.thesett.aima.logic.fol.interpreter.ResolutionEngine;
import com.thesett.aima.logic.fol.prolog.PrologCompiledClause;
import com.thesett.aima.logic.fol.prolog.PrologEngine;
import com.thesett.catalogue.model.Catalogue;
import com.thesett.catalogue.setup.CatalogueDefinition;
import com.thesett.common.parsing.SourceCodeException;

/**
 * CatalogueLoaderUtil provides a convenient way of loading a model from a resource name.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Load a model from a resource name. </td></tr>
 * </table></pre>
 */
public class CatalogueLoaderUtil
{
    public static Catalogue loadModel(String modelResource) throws JAXBException, SourceCodeException {
        // Open the specified resource and un-marshal the catalogue model from it.
        JAXBContext jc = JAXBContext.newInstance("com.thesett.catalogue.setup");
        Unmarshaller u = jc.createUnmarshaller();

        InputStream resource = CatalogueLoaderUtil.class.getClassLoader().getResourceAsStream(modelResource);

        if (resource == null)
        {
            throw new IllegalStateException("The resource 'modelResource' could not be found on the classpath.");
        }

        CatalogueDefinition catalogueDefinition = (CatalogueDefinition) u.unmarshal(resource);

        // Create a first order logic resolution engine to perform the type checking with.
        ResolutionEngine<Clause, PrologCompiledClause, PrologCompiledClause> engine = new PrologEngine();
        engine.reset();

        // Create the catalogue logical model from the raw model and run its type checker.
        CatalogueModelFactory modelFactory = new CatalogueModelFactory(engine, catalogueDefinition, null);

        return modelFactory.initializeModel();
    }
}

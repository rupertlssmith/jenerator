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
package com.thesett.catalogue;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import com.thesett.aima.logic.fol.Clause;
import com.thesett.aima.logic.fol.interpreter.ResolutionEngine;
import com.thesett.aima.logic.fol.prolog.PrologCompiledClause;
import com.thesett.aima.logic.fol.prolog.PrologEngine;
import com.thesett.catalogue.core.CatalogueModelFactory;
import com.thesett.catalogue.setup.CatalogueDefinition;

public class Main
{
    private static final String modelResource = "pp-model.xml";

    private static CatalogueModelFactory modelFactory;

    public static void main(String[] args) throws Exception
    {
        JAXBContext jc = JAXBContext.newInstance("com.thesett.catalogue.setup");
        Unmarshaller u = jc.createUnmarshaller();

        CatalogueDefinition catalogueDefinition =
            (CatalogueDefinition) u.unmarshal(Main.class.getClassLoader().getResourceAsStream(modelResource));

        // Create the catalogue logical model from the raw model and run its type checker.
        ResolutionEngine<Clause, PrologCompiledClause, PrologCompiledClause> engine = new PrologEngine();
        engine.reset();

        modelFactory = new CatalogueModelFactory(engine, catalogueDefinition, null);

        for (int i = 0; i < 1000; i++)
        {
            long startMillis = System.currentTimeMillis();

            engine.reset();
            modelFactory.initializeModel();

            long endMillis = System.currentTimeMillis();
            long durationMillis = endMillis - startMillis;

            System.out.println("Processed Model, run " + i + " took " + durationMillis + " ms.");
        }
    }
}

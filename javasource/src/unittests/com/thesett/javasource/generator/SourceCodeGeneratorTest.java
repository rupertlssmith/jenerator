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
package com.thesett.javasource.generator;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.NDC;

/**
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class SourceCodeGeneratorTest extends TestCase
{
    /** Used for logging. */
    private static final Logger log = Logger.getLogger(SourceCodeGeneratorTest.class);

    public SourceCodeGeneratorTest(String name)
    {
        super(name);
    }

    public void setUp()
    {
        NDC.push(getName());
    }

    public void testSimpleExample() throws Exception
    {
        SourceCodeGenerator.main(
            new String[]
            {
                "-modelfile", "src/resources/model.xml", "-templatefile", "src/resources/test.xml", "-dir",
                "target/example", "-package", "com.thesett.javasource.example.test"
            });
    }

    protected void tearDown() throws Exception
    {
        NDC.pop();
    }
}

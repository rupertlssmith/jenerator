/* Copyright Rupert Smith, 2005 to 2008, all rights reserved. */
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

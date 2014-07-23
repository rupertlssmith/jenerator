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
package com.thesett.javasource.generator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * SourceCodeWriter is a writer that writes to source files in a package structure that grows from a root directory.
 * As different packages and compilation unit file names are written to, they are automatically created, but most of
 * the calling code can just work with this as any other writer.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Provide creation of directories and files to write Java source code to.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class SourceCodeWriter extends Writer
{
    /** The underlying writer that is set up to write to the source files. */
    private Writer w;

    /** The root directory for the source writer. */
    private String rootDir;

    /**
     * Creates a source code writer taking the specified directory as the root to write packages to, under
     * sub-directories of it.
     *
     * @param dirName The name of the directory root for the package tree.
     */
    public SourceCodeWriter(String dirName)
    {
        // Keep the root directory to write to.
        this.rootDir = dirName;
    }

    /**
     * Write a portion of an array of characters. Delegates to the underlying writer.
     *
     * @param cbuf Array of characters.
     * @param off  Offset from which to start writing characters.
     * @param len  Number of characters to write.
     *
     * @throws IOException If the underlying writer causes an i/o exception.
     */
    public void write(char[] cbuf, int off, int len) throws IOException
    {
        if (w != null)
        {
            w.write(cbuf, off, len);
        }
    }

    /**
     * Flush the stream. Delegates to the underlying writer.
     *
     * @throws IOException If the underlying writer causes an i/o exception.
     */
    public void flush() throws IOException
    {
        if (w != null)
        {
            w.flush();
        }
    }

    /**
     * Close the stream, flushing it first. Delegates to the underlying writer.
     *
     * @throws IOException If the underlying writer causes an i/o exception.
     */
    public void close() throws IOException
    {
        if (w != null)
        {
            w.close();
        }
    }

    /**
     * Sets up the package and output file as the current compilation unit to write to. The necessary directories
     * and files will be created and set up as the current location where the output of this writer is directed.
     * Any previous compilation unit currently open will be flushed and closed.
     *
     * @param packageName The package name to write to.
     * @param fileName    The file name to write to.
     *
     * @throws IOException If any i/o exceptions occur on the underlying writers or whilst performing directory
     *                     or file operations on the file system.
     */
    public void setCompilationUnit(String packageName, String fileName) throws IOException
    {
        // Flush and close and current writer.
        if (w != null)
        {
            w.flush();
            w.close();
        }

        // Ensure the directory for the compilation unit exists.
        createPackageDir(packageName);

        // Create and open the file to write to.
        File compUnit = new File(rootDir + "/" + packageToDir(packageName) + "/" + fileName);

        // Establish the new writer as the output location.
        w = new FileWriter(compUnit);
    }

    /**
     * Converts a package name to a relative direcory name by converting all '.' characters to '/' characters.
     *
     * @param packageName The package name.
     *
     * @return The relative directory for the package name.
     */
    private String packageToDir(String packageName)
    {
        return packageName.replace('.', '/');
    }

    /**
     * Creates a directory for a named package, relative to the root directory of this source writer.
     *
     * @param packageName The package name.
     */
    private void createPackageDir(String packageName)
    {
        // Convert the package to a directory name and append it to the root directory.
        String dirName = rootDir + File.separator + packageToDir(packageName);

        File dir = new File(dirName);
        dir.mkdirs();
    }
}

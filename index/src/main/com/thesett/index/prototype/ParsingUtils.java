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
package com.thesett.index.prototype;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * Contains commonly re-used test parsing functions.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Transform a text into a set-of-words model, dropping any specified stop words.
 * <tr><td> Transform a text into a bag-of-words model, dropping any specified stop words. Still to be implemented.
 * </table></pre>
 *
 * @todo Make this class more flexible by requiring it to be instantiated with specification of how to treat punctuation
 *       and whitespace and so on. Could also make stemming or other transformations configurable here too.
 *
 * @author Rupert Smith
 */
public class ParsingUtils
{
    /** Used for logging. */
    private static final Logger log = Logger.getLogger(ProtoIndex.class);

    /**
     * Parses a text into a set-of-words model.
     *
     * @param text      The text to reduce to a set of words.
     * @param stopWords The stop words to ignore in the text.
     *
     * @return The text as a set of words.
     */
    public static Set<String> toSetOfWords(String text, Set<String> stopWords)
    {
        log.debug("private Set<String> toSetOfWords(String text): called");

        // Build the tokenizer to extract the data into words, dropping all punctuation and splitting on whitespace.
        StreamTokenizer tokenizer = new StreamTokenizer(new StringReader(text));

        tokenizer.lowerCaseMode(true);
        tokenizer.wordChars('a', 'z');
        tokenizer.wordChars('0', '9');
        tokenizer.whitespaceChars(' ', ' ');
        tokenizer.whitespaceChars('\n', '\n');
        tokenizer.whitespaceChars('\t', '\t');
        tokenizer.whitespaceChars('.', '.');
        tokenizer.whitespaceChars(',', ',');
        tokenizer.whitespaceChars(';', ';');
        tokenizer.whitespaceChars('/', '/');

        // Remove any duplicate terms from the data to arrive at the set of terms in the record (reduce the bag-of-words
        // model to a set-of-words model for this prototype implementation - only interested in recording whether or
        // not a record contains a term, not how many times).
        Set<String> setOfWords = new HashSet<String>();

        // This is usd to hold the previous token between two loops of the tokenization algorithm. Tokens are joined
        // onto previous tokens where non-word punctuation characters in the middle of a word are encountered.
        String previousToken = "";

        // Used to indicate when a previous token should be carried forward and joined onto the next token.
        boolean carryForward = false;

        while (true)
        {
            int tokenType;

            try
            {
                tokenType = tokenizer.nextToken();
            }
            catch (IOException e)
            {
                // Exception noted and ignored.
                e = null;

                break;
            }

            String token = tokenizer.sval;
            log.debug("token = " + token);

            // If an non-word character is read then carry the previous token forward accross succesive non-word
            // characters until another word is encountered and concatenate the two together. Punctuation in the
            // middle of words is effectively removed by doing this.
            if ((tokenType != StreamTokenizer.TT_WORD) && (tokenType != StreamTokenizer.TT_EOF))
            {
                // Keep the existing previous token.

                // Set the carry forward flag to indicate that a carry forward is in progress.
                carryForward = true;
            }
            else if (tokenType == StreamTokenizer.TT_WORD)
            {
                // A word was found.
                // Check if a carry forward is in progress and concatenate this word onto the previous one if so.
                if (carryForward)
                {
                    token = previousToken + token;

                    // Make sure the carry forward flag is clear.
                    carryForward = false;

                    // Set the previous token for the next round.
                    previousToken = token;
                }

                // Filter the stop words out of the bag of words model as the set is built.
                if (!stopWords.contains(token))
                {
                    // Place the word in the set of words. This has the effect of removing duplicates.
                    setOfWords.add(token);
                }
            }
            else
            {
                // The end of the text to tokenize was reached.
                break;
            }
        }

        log.debug("setOfWords = " + setOfWords);

        return setOfWords;
    }
}

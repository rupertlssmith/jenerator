/* Copyright Rupert Smith, 2005 to 2008, all rights reserved. */
package com.thesett.common.config;

/**
 * TestConfigBean is a dummy config bean that exists for the purpose of testing the config bean configurator and has been
 * suitably instrumented for this purpose.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Report succesful configuration.
 * <tr><td> Throw config exceptions on demand.
 * <tr><td> Track the most recent value of the force flag.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class TestConfigBean implements ConfigBean
{
    /** Holds the value of the force flag on the most recent call to the {@link #doConfigure method}. */
    public static boolean mostRecentForceFlag = false;

    /** A flag that indicates whether or not config exceptions should be thrown. */
    public static boolean throwConfigExceptions = false;

    /** Holds the configuration state of this bean. */
    public static boolean isConfigured = false;

    /** Holds a reference to the most recent instance of this bean. */
    public static TestConfigBean mostRecentInstance;

    /* Holds the yy value of the dummy yy property setter. */
    //public yy testYy;

    /* A dummy yy property setter. */
    //public void setTestYy(yy testYy) { this.testYy = testYy; }

    /** Holds the int value of the dummy int property setter. */
    public int testInt;

    /** Holds the char value of the dummy char property setter. */
    public char testChar;

    /** Holds the byte value of the dummy byte property setter. */
    public byte testByte;

    /** Holds the long value of the dummy long property setter. */
    public long testLong;

    /** Holds the boolean value of the dummy boolean property setter. */
    public boolean testBoolean;

    /** Holds the float value of the dummy float property setter. */
    public float testFloat;

    /** Holds the double value of the dummy double property setter. */
    public double testDouble;

    /** Holds the String value of the dummy String property setter. */
    public String testString;

    /** Default constructor. Records this as the most recent instance. */
    public TestConfigBean()
    {
        mostRecentInstance = this;
    }

    /**
     * Tells all config beans of this class to throw test config exceptions whenever the {@link #doConfigure} method
     * is called.
     *
     * @param throwFlag Set to true to cause exceptions to be thrown, false to turn them off.
     */
    public void setThrowFlag(boolean throwFlag)
    {
        throwConfigExceptions = throwFlag;
    }

    /**
     * Tells the bean to perform whatever configuration it is intended to do.
     *
     * @param force Setting this to true tells the config bean to re-run its configuration action even if it has
     *              already been run.
     * @param configBeanContext A reference to the configurator that is managing the whole configuration process.
     *
     * @throws ConfigException If some error occurs that means that the configuration cannot be succesfully completed.
     */
    public void doConfigure(boolean force, ConfigBeanContext configBeanContext) throws ConfigException
    {
        // Keep the value of the most recent force flag.
        mostRecentForceFlag = force;

        // Check if a configuration exception should be thrown.
        if (throwConfigExceptions)
        {
            throw new ConfigException("TestConfigBean test Exception.", null, null, null);
        }

        // Set this beans state to configured.
        isConfigured = true;
    }

    /**
     * Checks whether or not the config bean has been succesfully run and is in a configured state.
     *
     * @return True if the config bean has run its configuration succesfully.
     */
    public boolean getConfigured()
    {
        return isConfigured;
    }

    /** A dummy int property setter. */
    public void setTestInt(int testInt)
    {
        this.testInt = testInt;
    }

    /** A dummy char property setter. */
    public void setTestChar(char testChar)
    {
        this.testChar = testChar;
    }

    /** A dummy byte property setter. */
    public void setTestByte(byte testByte)
    {
        this.testByte = testByte;
    }

    /** A dummy long property setter. */
    public void setTestLong(long testLong)
    {
        this.testLong = testLong;
    }

    /** A dummy boolean property setter. */
    public void setTestBoolean(boolean testBoolean)
    {
        this.testBoolean = testBoolean;
    }

    /** A dummy float property setter. */
    public void setTestFloat(float testFloat)
    {
        this.testFloat = testFloat;
    }

    /** A dummy double property setter. */
    public void setTestDouble(double testDouble)
    {
        this.testDouble = testDouble;
    }

    /** A dummy String property setter. */
    public void setTestString(String testString)
    {
        this.testString = testString;
    }
}

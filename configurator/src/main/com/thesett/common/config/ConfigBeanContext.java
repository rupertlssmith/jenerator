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
package com.thesett.common.config;

/**
 * ConfigBeanContext provides a context in which beans that have been created and configured by a configuration system
 * may be located by other beans requiring their services. This interface is effectively a service locator.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities
 * <tr><td> Locate managed configuration beans.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public interface ConfigBeanContext
{
    /**
     * Gets successfully configured beans from the cache of configured beans.
     *
     * @param name The class name of the bean to fetch.
     *
     * @return The matching configured bean, or null if none with a matching name can be found.
     *
     * @throws ConfigException If there is an error whilst looking up the bean, or if the bean is not configured.
     */
    ConfigBean getConfiguredBean(String name) throws ConfigException;
}

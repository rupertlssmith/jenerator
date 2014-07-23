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

import com.thesett.aima.state.State;

/**
 * A ComponentInstance is an instance of a {@link com.thesett.aima.state.ComponentType} type.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities
 * <tr><td> Provide the types of fields that make up a component.
 * <tr><td> Provide an internal id to identify the component instance uniquely by.
 * <tr><td> Provide a long lived external id to identify the component instance uniquely by.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public interface ComponentInstance extends State
{
}

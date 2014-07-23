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

import java.io.Serializable;

import com.thesett.aima.state.ComponentType;

/**
 * ViewType is a {@link com.thesett.aima.state.Type} which is a {@link ComponentType} defining a set of named and
 * typed fields. A ViewType may provide a sub-set of the fields that a ComponentType exposes, and a ComponentType may
 * be able to be presented as a ViewType which is a sub-set of its fields.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Provide a subset of the types and names of fields that make up a component.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public interface ViewType extends ComponentType, Serializable
{
}

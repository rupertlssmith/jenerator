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

/**
 * A ViewInstance is an instance of a {@link ViewType}. It is a {@link com.thesett.aima.state.State} that contains
 * the fields of name and type matching some {@link ViewType}.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Provide a subset of the types of fields that make up a component.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public interface ViewInstance extends ComponentInstance
{
}

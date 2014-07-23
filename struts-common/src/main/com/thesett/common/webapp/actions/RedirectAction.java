/* Copyright Rupert Smith, 2005 to 2008, all rights reserved. */
package com.thesett.common.webapp.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;

/**
 * RedirectAction is a slightly alternative version of j_security_check that can be used to perform a login
 * without trying to access a protected resource. It is intended for use on web sites where users do not have to login
 * to use many of the sites features but can optionally log in for greater privileges/utility. The normal pattern of
 * logging in and protecting web resources works as follows:
 *
 * <p> User tries to access protected resource -> container redirects to login page -> container redirects to resource.
 *
 * <p> If the log in is optional it can occur at any time and the resource being accessed could be any page and not just
 * a protected one. This works as follows:
 *
 * <p> User is on unprotected page and enters log in details and then submits to this action -> container redirects
 * to login page, which can tell from its parameters that the log in details have been entered and automatically
 * submits to j_security check -> container authenticates and redirects to this action -> this action
 * redirects to any desired page passed in the original parameters to this action.
 *
 * <p><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Bounce back to the calling location after a j_security_check log in.
 * </table>
 *
 * @todo A better name for this may be BounceBackAction. Its purpose is to be a protected resource which can bounce back
 * to the calling location (or another location) when doing a log in at an arbitrary time. For example when a log in (or
 * register)/logged in box should be displayed on every page and users can opt to log in whenever they like to gain
 * greater access priveleges. Normal servlet log in only kicks in when a protected resource is explicitly accessed and
 * the log in page cannot be navigated to directly or else j_security_check won't know where to redirect to.
 *
 * @struts.action name="redirectform"
 *                path="/protected/loginredirect"
 *                scope="request"
 *                validate="false"
 *
 * @author Rupert Smith
 */
public class RedirectAction extends BaseAction
{
    /** Used for logging. */
    private static final Logger log = Logger.getLogger(RedirectAction.class);

    /** Constant defining the name of the request parameter that holds the redirect location. */
    private static final String REDIRECT = "redirect";

    /**
     * Handles an HTTP request sent to this action by struts. This simply forwards to the location specified
     * by the "redirect" request parameter.
     *
     * @param mapping  The Struts action mapping.
     * @param form     The Struts form associated with this action.
     * @param request  The HTTP request.
     * @param response The HTTP response.
     * @param errors   The Struts action errors object to place any errors in.
     *
     * @return A forward to the "redirect" location.
     */
    public ActionForward executeWithErrorHandling(ActionMapping mapping, ActionForm form, HttpServletRequest request,
        HttpServletResponse response, ActionErrors errors)
    {
        log.debug("public ActionForward performWithErrorHandling(ActionMapping mapping, ActionForm form," +
            "HttpServletRequest request, HttpServletResponse response, " + "ActionErrors errors): called");

        HttpSession session = request.getSession();
        DynaActionForm dynaForm = (DynaActionForm) form;

        // Redirect to the specified location
        String redirect = (String) dynaForm.get(REDIRECT);

        log.debug("redirect = " + redirect);

        return new ActionForward(redirect, true);
    }
}

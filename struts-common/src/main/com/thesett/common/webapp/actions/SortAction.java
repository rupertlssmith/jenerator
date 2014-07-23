/* Copyright Rupert Smith, 2005 to 2008, all rights reserved. */
package com.thesett.common.webapp.actions;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.thesett.common.webapp.beans.SortStateBean;
import com.thesett.common.webapp.forms.SortForm;

/**
 * SortAction applies a sorting to a list stored in a web variable scope and replaces the variable with the newly
 * sorted list. The list to be sorted is passed as a parameter as is the name of the bean which holds an implementation
 * of the interface {@link java.util.Comparator}. The sort is performed by the {@link java.util.Collections#sort}
 * method. Once the sort has been done control is returned to the "success" page which should be configured in the
 * struts-config.xml for every page that needs to do sorting to point back to that same page (unless of course you
 * really want to navigate to a new page after the sort). The idea is that the page will then be redrawn with some
 * of its elements sorted but without making any other changes to the data that the page is built from. This allows
 * sorting to be applied to user interface elements independantly of business logic and provides a re-usable piece
 * of user interface logic that will find uses in many user intefaces.
 *
 * <p/>See {@link com.thesett.common.webapp.forms.SortForm} for information about the parameters that are passed
 * to this action.
 *
 * <p/>If its reverse sorted, unsorted or not sorted by the current sort property then forward sort it. If its already
 * forward sorted then reverse sort it.
 *
 * <p><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Sort a list <td> {@link java.util.Collections#sort}
 * <tr><td> Return control to calling page.
 * </table>
 *
 * @author Rupert Smith
 */
public class SortAction extends BaseAction
{
    /** Used for logging. */
    private static final Logger log = Logger.getLogger(SortAction.class);

    /**
     * Processes the action.
     *
     * @param mapping  The ActionMapping used to select this instance
     * @param form     The optional ActionForm bean for this request (if any)
     * @param request  The HTTP request.
     * @param response The HTTP response.
     *
     * @return A struts forward to the "success" location.
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet exception occurs
     */
    public ActionForward perform(ActionMapping mapping, ActionForm form, HttpServletRequest request,
        HttpServletResponse response) throws IOException, ServletException
    {
        log.debug("perform: called");

        // Reference the SortForm as a SortForm rather than the generic ActionForm
        SortForm sortForm = (SortForm) form;

        // Get a reference to the session scope
        HttpSession session = request.getSession();

        // Get a reference to the application scope
        ServletContext application = session.getServletContext();

        log.debug("variables in the servlet context: ");

        for (Enumeration e = application.getAttributeNames(); e.hasMoreElements();)
        {
            log.debug(e.nextElement());
        }

        // Get a reference to the list to be sorted
        List list = (List) session.getAttribute(sortForm.getList());

        // Get a reference to the comparator from the application scope to use to perform the sort
        Comparator comparator = (Comparator) application.getAttribute(sortForm.getComparator());

        log.debug("comparator = " + comparator);

        // Get a reference to the current sort state (if there is one)
        SortStateBean sortStateBean = (SortStateBean) session.getAttribute(sortForm.getSortState());

        // Check if there is no sort state bean and create one if so
        if (sortStateBean == null)
        {
            log.debug("There is no sort state bean");

            sortStateBean = new SortStateBean();
        }

        // Determine whether a forward or reverse sort is to be done
        // If its reverse sorted, unsorted or not sorted by the current sort property then forward sort it
        if (!sortStateBean.getState().equals(SortStateBean.FORWARD) ||
                !sortStateBean.getSortProperty().equals(sortForm.getSortStateProperty()))
        {
            // Sort the list
            Collections.sort(list, comparator);

            // Update the current sort state
            sortStateBean.setState(SortStateBean.FORWARD);
        }

        // If its already forward sorted then reverse sort it
        else
        {
            // Sort the list
            Collections.sort(list, comparator);

            // Reverse the list
            Collections.reverse(list);

            // Update the current sort state
            sortStateBean.setState(SortStateBean.REVERSE);
        }

        // Store the sorted list in the variable from which the original list was taken
        session.setAttribute(sortForm.getList(), list);

        // Store the new sort state, setting the property that has been sorted by in the sort state
        sortStateBean.setSortProperty(sortForm.getSortStateProperty());
        session.setAttribute(sortForm.getSortState(), sortStateBean);

        // Forward to the success page
        return (mapping.findForward("success"));
    }
}

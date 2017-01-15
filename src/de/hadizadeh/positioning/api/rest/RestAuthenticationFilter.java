package de.hadizadeh.positioning.api.rest;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Handles the local authentication check
 */
public class RestAuthenticationFilter implements javax.servlet.Filter {
    /**
     * authentication header
     */
    public static final String AUTHENTICATION_HEADER = "Authorization";

    /**
     * Checkes the local authentication
     *
     * @param request  request
     * @param response response
     * @param filter   filter to hadle the authentication
     * @throws IOException      if the check could not be done
     * @throws ServletException if the servlet crashes while checking the authentication
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain filter) throws IOException, ServletException {
        if (request instanceof HttpServletRequest) {
            HttpServletRequest httpServletRequest = (HttpServletRequest) request;

            String authCredentials = httpServletRequest.getHeader(AUTHENTICATION_HEADER);
            AuthenticationService authenticationService = new AuthenticationService();
            try {
                boolean authenticationStatus = authenticationService.authenticate(authCredentials, httpServletRequest.getMethod());

                if (authenticationStatus) {
                    filter.doFilter(request, response);
                } else {
                    if (response instanceof HttpServletResponse) {
                        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
                        httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        httpServletResponse.getWriter().flush();
                    }
                }
            } catch (Exception exception) {
                HttpServletResponse httpServletResponse = (HttpServletResponse) response;
                httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
//                    httpServletResponse.getWriter().write((String)exception.getResponse().getEntity());
                httpServletResponse.getWriter().flush();
            }
        }
    }

    /**
     * Destroys the authentication
     */
    @Override
    public void destroy() {
    }

    /**
     * Initializes the authentication
     *
     * @param arg0 filter config
     * @throws ServletException if the servlet crashes
     */
    @Override
    public void init(FilterConfig arg0) throws ServletException {
    }
}

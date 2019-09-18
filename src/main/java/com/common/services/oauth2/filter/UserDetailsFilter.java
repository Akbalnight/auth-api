package com.common.services.oauth2.filter;

import com.common.services.oauth2.user.User;
import com.common.services.oauth2.user.UserDetails;
import org.springframework.security.core.AuthenticationException;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Description: Фильтр добавляет заголовки в запрос с данными пользователя
 * @author AsMatveev
 */
public class UserDetailsFilter
        implements Filter
{
    private static final String USER_ID = "userId";
    private static final String USER_NAME = "userName";
    private static final String USER_ROLES = "userRoles";
    private static final String UNDEFINED = "undefined";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException
    {
        RequestWrapper requestWrapper = new RequestWrapper((HttpServletRequest) request);
        try
        {
            User user = UserDetails.getUser();
            requestWrapper.putHeader(USER_ID, user.getId().toString());
            requestWrapper.putHeader(USER_NAME, user.getName());
            requestWrapper.putHeader(USER_ROLES, user.getRoles().toString());
        }
        catch (AuthenticationException ex)
        {
            requestWrapper.putHeader(USER_ID, UNDEFINED);
            requestWrapper.putHeader(USER_NAME, UNDEFINED);
            requestWrapper.putHeader(USER_ROLES, UNDEFINED);
        }
        chain.doFilter(requestWrapper, response);
    }

    @Override
    public void init(FilterConfig filterConfig)
            throws ServletException
    {
    }

    @Override
    public void destroy()
    {
    }
}

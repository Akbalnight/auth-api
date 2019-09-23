package com.common.services.oauth2.filter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.*;

/**
 * Description: Обертка запроса для добавления заголовков
 * @author AsMatveev
 */
public class RequestWrapper
        extends HttpServletRequestWrapper
{
    private final Map<String, String> headers;

    /**
     * Constructs a request object wrapping the given request.
     * @param request The request to wrap
     * @throws IllegalArgumentException if the request is null
     */
    public RequestWrapper(HttpServletRequest request)
    {
        super(request);
        this.headers = new HashMap<>();
    }

    public void putHeader(String name, String value)
    {
        this.headers.put(name, value);
    }

    @Override
    public String getHeader(String name)
    {
        String headerValue = headers.get(name);

        if (headerValue != null)
        {
            return headerValue;
        }
        return ((HttpServletRequest) getRequest()).getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaderNames()
    {
        Set<String> set = new HashSet<>(headers.keySet());

        Enumeration<String> e = ((HttpServletRequest) getRequest()).getHeaderNames();
        while (e.hasMoreElements())
        {
            String n = e.nextElement();
            set.add(n);
        }
        return Collections.enumeration(set);
    }
}

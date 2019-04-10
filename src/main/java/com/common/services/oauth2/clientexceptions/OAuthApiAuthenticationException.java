package com.common.services.oauth2.clientexceptions;

import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.common.exceptions.ClientAuthenticationException;

/**
 * OAuthApiAuthenticationException.java
 * Date: 9 апр. 2019 г.
 * Users: vmeshkov
 * Description: TODO
 */
@SuppressWarnings("serial")
public class OAuthApiAuthenticationException
    extends ClientAuthenticationException
{
    private HttpStatus status = HttpStatus.UNAUTHORIZED;
    public OAuthApiAuthenticationException(String msg, Throwable t)
    {
        super(msg, t);
    }
    
    public OAuthApiAuthenticationException(HttpStatus status, String message,
        Throwable e)
    {
        super(message, e);
        this.status = status;
    }

    @Override
    public int getHttpErrorCode()
    {
        return status.value();
    }

    @Override
    public String getOAuth2ErrorCode()
    {
        return status.getReasonPhrase();
    }

}

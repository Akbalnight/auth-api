package com.common.services.oauth2.user;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;

import java.util.List;

/**
 * Description: Данные аутентификации пользователя.
 * Данные пользователя формируются при проверке токена доступа (access_token) на сервере авторизации.
 * @author AsMatveev
 */
@SuppressWarnings("serial")
public class OAuth2User
        extends UsernamePasswordAuthenticationToken
        implements User
{
    private String username;
    private Integer id;
    private List<String> roles;

    public OAuth2User(Integer id, String username, List<String> roles)
    {
        super(username, null, AuthorityUtils.createAuthorityList((roles).toArray(new String[0])));
        this.id = id;
        this.username = username;
        this.roles = roles;
    }

    @Override
    public Integer getId()
    {
        return id;
    }

    @Override
    public String getName()
    {
        return username;
    }

    @Override
    public List<String> getRoles()
    {
        return roles;
    }
}
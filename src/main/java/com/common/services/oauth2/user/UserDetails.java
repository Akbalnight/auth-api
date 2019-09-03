package com.common.services.oauth2.user;

import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;


/**
 * Description: Класс для получения информации о текущем пользователе
 * @author AsMatveev
 */
public class UserDetails
{
    /**
     * Возаращает данные аутентификации текущего пользователя
     * @return Возвращает данные пользователя
     * @throws AuthenticationException Исключение если данные аутентификации не были найдены
     */
    public static User getUser()
            throws AuthenticationException
    {
        Object authentication = SecurityContextHolder.getContext()
                                                     .getAuthentication();
        if (authentication instanceof OAuth2Authentication)
        {
            return (User) ((OAuth2Authentication) authentication).getUserAuthentication();
        }
        else
        {
            throw new AuthenticationServiceException("Ошибка получения данных аутентификации пользователя");
        }
    }
}

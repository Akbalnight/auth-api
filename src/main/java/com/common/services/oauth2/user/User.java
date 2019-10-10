package com.common.services.oauth2.user;

import java.util.List;

/**
 * Description: Интерфейс для получения информации о текущем пользователе
 * @author AsMatveev
 */
public interface User
{
    /**
     * Возвращает логин пользователя
     */
    String getName();

    /**
     * Возвращает id пользователя
     */
    Integer getId();

    /**
     * Возвращает список ролей пользователя
     */
    List<String> getRoles();
}

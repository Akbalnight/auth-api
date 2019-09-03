package com.common.services.oauth2.setup;

import com.common.services.oauth2.user.OAuth2User;
import com.common.services.oauth2.clientexceptions.OAuthApiAuthenticationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.security.oauth2.provider.token.UserAuthenticationConverter;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OAuthApiRemoteTokenServices.java
 * Date: 8 апр. 2019 г.
 * Users: vmeshkov
 * Description: Определим свой токенсервис, чтобы перегрузить resttemplate
 */
public class OAuthApiRemoteTokenServices
        implements ResourceServerTokenServices
{
    /**
     * Для выполнения запросов-проврки тоена
     */
    private RestOperations restTemplate;
    /**
     * URL для проверки токена
     */
    private String checkTokenEndpointUrl;

    private DefaultAccessTokenConverter tokenConverter = new DefaultAccessTokenConverter();

    public OAuthApiRemoteTokenServices()
    {
        restTemplate = new RestTemplate();
        tokenConverter.setUserTokenConverter(new UserAuthenticationConverter()
        {
            @Override
            public Authentication extractAuthentication(Map<String, ?> map)
            {
                return new OAuth2User((Integer) map.get("id"), (String) map.get("username"), (List<String>) map.get("roles"));
            }

            @Override
            public Map<String, ?> convertUserAuthentication(Authentication userAuthentication)
            {
                return null;
            }
        });
    }

    public void setCheckTokenEndpointUrl(String checkTokenEndpointUrl)
    {
        this.checkTokenEndpointUrl = checkTokenEndpointUrl;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public OAuth2Authentication loadAuthentication(String accessToken)
            throws AuthenticationException, InvalidTokenException
    {
        HttpEntity<String> httpEntity = new HttpEntity<>(getBody(), getHeaders(accessToken));
        ResponseEntity<Map> response = null;
        try
        {
            response = restTemplate.exchange(checkTokenEndpointUrl, HttpMethod.POST, httpEntity, Map.class);
        }
        catch (HttpStatusCodeException e)
        {
            String message = e.getStatusCode().name();
            try
            {
                JsonNode data = new ObjectMapper().readTree(e.getResponseBodyAsByteArray());
                message = data.get("error_description").asText();
            }
            catch (IOException | NullPointerException ignore)
            {
            }
            throw new OAuthApiAuthenticationException(e.getStatusCode(), message, e);
        }
        catch (ResourceAccessException connectionException)
        {
            throw new OAuthApiAuthenticationException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Ошибка подключения к серверу авторизации.", connectionException);
        }
        return tokenConverter.extractAuthentication(response.getBody());
    }

    /**
     * Формирует заголовки запроса для проверки авторизации пользователя
     */
    private HttpHeaders getHeaders(String accessToken)
    {
        HttpHeaders headers = new HttpHeaders();
        if (accessToken != null)
        {
            headers.add("Authorization", "Bearer " + accessToken);
        }
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    /**
     * Формирует тело запроса для проверки авторизации пользователя
     */
    private String getBody()
    {
        String body = null;
        Map<String, String> map = new HashMap<>();
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        HttpServletRequest httpRequest = null;
        if (requestAttributes instanceof ServletRequestAttributes)
        {
            httpRequest = ((ServletRequestAttributes) requestAttributes).getRequest();
        }
        if (httpRequest != null)
        {
            // Запишем метод и путь исходного запроса в json запроса
            map.put("method", httpRequest.getMethod());
            map.put("path", httpRequest.getRequestURI());
        }
        try
        {
            body = new ObjectMapper().writeValueAsString(map);
        }
        catch (JsonProcessingException ignore)
        {
        }
        return body;
    }

    @Override
    public OAuth2AccessToken readAccessToken(String accessToken)
    {
        throw new UnsupportedOperationException("Not supported: read access token");
    }
}

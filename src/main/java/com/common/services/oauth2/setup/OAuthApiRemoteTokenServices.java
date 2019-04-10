package com.common.services.oauth2.setup;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.security.oauth2.provider.token.UserAuthenticationConverter;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.common.services.oauth2.clientexceptions.OAuthApiAuthenticationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * OAuthApiRemoteTokenServices.java
 * Date: 8 апр. 2019 г.
 * Users: vmeshkov
 * Description: Определим свой токенсервис, чтобы перегрузить resttemplate
 */
public class OAuthApiRemoteTokenServices implements ResourceServerTokenServices
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
                @SuppressWarnings("unchecked")
                Collection<? extends GrantedAuthority> authorities =
                    AuthorityUtils.createAuthorityList(
                        ((Collection<String>)map.get("roles")).toArray(new String[0]));
                return new UsernamePasswordAuthenticationToken((Integer)map.get("id"), "N/A", authorities);
            }
            
            @Override
            public Map<String, ?> convertUserAuthentication(
                Authentication userAuthentication)
            {
                return null;
            }
        });
    }

    public void setCheckTokenEndpointUrl(String checkTokenEndpointUrl)
    {
        this.checkTokenEndpointUrl = checkTokenEndpointUrl;
    }

    @SuppressWarnings({
        "unchecked", "rawtypes"
    })
    @Override
    public OAuth2Authentication loadAuthentication(String accessToken)
        throws AuthenticationException, InvalidTokenException
    {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        HttpServletRequest httpRequest = null;
        if (requestAttributes instanceof ServletRequestAttributes)
        {
            httpRequest = ((ServletRequestAttributes)requestAttributes).getRequest();
        }

        HttpHeaders headers = new HttpHeaders();
        Map<String, String> map = new HashMap<>();
        if (httpRequest != null)
        {
            // Запишем метод и путь исходного запроса в json запроса
            map.put("method", httpRequest.getMethod());
            map.put("path", httpRequest.getServletPath());            
        }
        if (accessToken != null)
        {
            headers.add("Authorization", "Bearer " + accessToken);
        }
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> httpEntity = null;
        String body = null;
        try
        {
            body = new ObjectMapper().writeValueAsString(map);
        }
        catch (JsonProcessingException e)
        {
        }

        httpEntity = new HttpEntity<String>(body, headers);
        ResponseEntity<Map> response = null;
        try
        {
            response = restTemplate.exchange(
                checkTokenEndpointUrl, HttpMethod.POST, httpEntity, Map.class);
        }
        catch (HttpStatusCodeException e)
        {
            String message = e.getStatusCode().name();
            try
            {
                JsonNode data =
                    new ObjectMapper().readTree(e.getResponseBodyAsByteArray());
                message = data.get("error_description").asText();
            }
            catch (IOException | NullPointerException e1)
            {
            }
            
            throw new OAuthApiAuthenticationException(e.getStatusCode(), message, e);
        }
        return tokenConverter.extractAuthentication(response.getBody());
    }

    @Override
    public OAuth2AccessToken readAccessToken(String accessToken)
    {
        throw new UnsupportedOperationException("Not supported: read access token");
    }
}

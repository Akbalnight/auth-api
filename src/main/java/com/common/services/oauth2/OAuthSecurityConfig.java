package com.common.services.oauth2;

import com.common.services.oauth2.setup.OAuthApiRemoteTokenServices;
import com.common.services.oauth2.filter.UserDetailsFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

/**
 * OAuthSecurityConfig.java
 * Date: 19 мар. 2019 г.
 * Users: vmeshkov
 * Description: Конфигурация для OAuth2
 */
@Configuration
@EnableResourceServer
public class OAuthSecurityConfig  extends ResourceServerConfigurerAdapter
{
    /**
     * URL cервера авторизации
     */
    @Value("${oauth2.check_token.url}")
    private String remoteServer;

    @Override
    public  void configure(HttpSecurity http) throws Exception
    {
        http.authorizeRequests()
            .antMatchers("/**/springfox-swagger-ui/**", "/swagger-ui.html", "/swagger-resources/**", "/v2/api-docs/**")
            .permitAll()
            .antMatchers(HttpMethod.GET, "/version")
            .permitAll()
            .anyRequest().authenticated();
        http.addFilterAfter(new UserDetailsFilter(), BasicAuthenticationFilter.class);
    }

    @Primary
    @Bean
    public ResourceServerTokenServices tokenServices()
    {
        final OAuthApiRemoteTokenServices tokenService = new OAuthApiRemoteTokenServices();
        tokenService.setCheckTokenEndpointUrl(remoteServer);
        return tokenService;
    }
}

/*
 *  Copyright (C) 2015-2018 EPAM Systems
 *  
 *  This file is part of Indigo ELN.
 *
 *  Indigo ELN is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Indigo ELN is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Indigo ELN.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.indigoeln.config.security;

import com.epam.indigoeln.Application;
import com.epam.indigoeln.core.security.*;
import com.epam.indigoeln.web.rest.filter.CsrfCookieFilter;
import com.epam.indigoeln.web.rest.filter.SessionExpirationFilter;
import com.epam.indigoeln.web.rest.util.HeaderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.data.repository.query.SecurityEvaluationContextExtension;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.session.ConcurrentSessionFilter;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.epam.indigoeln.core.security.Authority.*;
import static com.epam.indigoeln.core.security.CookieConstants.CSRF_TOKEN_HEADER;
import static com.epam.indigoeln.core.util.AuthoritiesUtil.*;


@Configuration
@EnableScheduling
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class WebSecurityConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebSecurityConfig.class);

    @Autowired
    private Environment environment;

    @Autowired
    private IntSecurityProperties securityProperties;

    @Autowired
    private AjaxAuthenticationSuccessHandler ajaxAuthenticationSuccessHandler;

    @Autowired
    private AjaxAuthenticationFailureHandler ajaxAuthenticationFailureHandler;

    @Autowired
    private AjaxLogoutSuccessHandler ajaxLogoutSuccessHandler;

    @Autowired
    private Http401UnauthorizedEntryPoint authenticationEntryPoint;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private RememberMeServices rememberMeServices;

    @Value("${cors.origin}")
    private String corsOrigin;


    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth, PasswordEncoder passwordEncoder) throws Exception {
        auth
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder);
    }

    @Bean
    @Profile(Application.Profile.CORS)
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowCredentials(true)
                        .allowedOrigins(corsOrigin)
                        .allowedMethods("*")
                        .allowedHeaders("*")
                        .exposedHeaders(
                                HeaderUtil.SUCCESS_ALERT,
                                HeaderUtil.TOTAL_COUNT,
                                HttpHeaders.LINK,
                                HttpHeaders.CONTENT_TYPE,
                                HttpHeaders.COOKIE,
                                HttpHeaders.SET_COOKIE,
                                HttpHeaders.CONTENT_DISPOSITION
                        );
                LOGGER.warn("Enabled CORS mappings for origin '" + corsOrigin + "'");
            }
        };
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
                .requestMatchers("/scripts/**")
                .requestMatchers("/bower_components/**")
                .requestMatchers("/i18n/**")
                .requestMatchers("/assets/**")
                .requestMatchers("/test/**");
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .sessionManagement() // add session management
                .maximumSessions(-1) // set unlimited number of sessions per User // TODO think about this
                .sessionRegistry(sessionRegistry());

        if (Arrays.asList(environment.getActiveProfiles()).contains(Application.Profile.CORS)) {
            http.cors();
        }

        http.addFilterBefore(sessionExpirationFilter(), ConcurrentSessionFilter.class);
//        http
//                .csrf(csrf -> csrf
//                        .csrfTokenRepository(cookieCsrfTokenRepository())
//                        .ignoringRequestMatchers("/api/authentication", "/api/logout", "/websocket/**") // For solving a problem with login after logout
//                );
//        http
//                .addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class);
        http.csrf(AbstractHttpConfigurer::disable);
        http
                .exceptionHandling()
                .accessDeniedHandler(new CustomAccessDeniedHandler())
                .authenticationEntryPoint(authenticationEntryPoint)
                .and()
                .rememberMe()
                .rememberMeServices(rememberMeServices)
                .rememberMeParameter("remember-me")
                .key(securityProperties.getRemembermeKey())
                .and()
                .formLogin()
                .loginProcessingUrl("/api/authentication")
                .successHandler(ajaxAuthenticationSuccessHandler)
                .failureHandler(ajaxAuthenticationFailureHandler)
                .usernameParameter("j_username")
                .passwordParameter("j_password")
                .permitAll()
                .and()
                .logout()
                .logoutUrl("/api/logout")
                .logoutSuccessHandler(ajaxLogoutSuccessHandler)
                .deleteCookies(CookieConstants.JSESSIONID, CookieConstants.CSRF_TOKEN)
                .permitAll()
                .and()
                .headers()
                .frameOptions()
                .disable()
                .and()
                .authorizeRequests()
                // account resource
                .requestMatchers(HttpMethod.GET, "/api/accounts/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/accounts/account/roles").hasAuthority(ROLE_EDITOR.name())
                // experiment_file resource
                .requestMatchers(HttpMethod.GET, "/api/experiment_files").hasAnyAuthority(EXPERIMENT_READERS)
                .requestMatchers(HttpMethod.GET, "/api/experiment_files/**").hasAnyAuthority(EXPERIMENT_READERS)
                .requestMatchers(HttpMethod.POST, "/api/experiment_files").hasAnyAuthority(EXPERIMENT_CREATORS)
                .requestMatchers(HttpMethod.DELETE, "/api/experiment_files/**")
                .hasAnyAuthority(EXPERIMENT_CREATORS)
                // experiment resource
                //this request secured with method security
                .requestMatchers(HttpMethod.GET, "/api/projects/*/notebooks/*/experiments")
                .authenticated()
                .requestMatchers(HttpMethod.GET, "/api/projects/*/notebooks/*/experiments/all")
                .hasAuthority(CONTENT_EDITOR.name())
                .requestMatchers(HttpMethod.GET, "/api/projects/*/notebooks/*/experiments/**")
                .hasAnyAuthority(EXPERIMENT_READERS)
                .requestMatchers(HttpMethod.POST, "/api/projects/*/notebooks/*/experiments")
                .hasAnyAuthority(EXPERIMENT_CREATORS)
                .requestMatchers(HttpMethod.POST, "/api/projects/*/notebooks/*/experiments/**")
                .hasAnyAuthority(EXPERIMENT_CREATORS)
                .requestMatchers(HttpMethod.PUT, "/api/projects/*/notebooks/*/experiments")
                .hasAnyAuthority(EXPERIMENT_CREATORS)
                .requestMatchers(HttpMethod.PUT, "/api/projects/*/notebooks/*/experiments/**")
                .hasAnyAuthority(EXPERIMENT_CREATORS)
                .requestMatchers(HttpMethod.DELETE, "/api/projects/*/notebooks/*/experiments/**")
                .hasAnyAuthority(EXPERIMENT_REMOVERS)
                // notebook resource
                //this request secured with method security
                .requestMatchers(HttpMethod.GET, "/api/projects/*/notebooks").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/projects/*/notebooks/all")
                .hasAuthority(CONTENT_EDITOR.name())
                .requestMatchers(HttpMethod.GET, "/api/projects/*/notebooks/**").hasAnyAuthority(NOTEBOOK_READERS)
                .requestMatchers(HttpMethod.POST, "/api/projects/*/notebooks").hasAnyAuthority(NOTEBOOK_CREATORS)
                .requestMatchers(HttpMethod.PUT, "/api/projects/*/notebooks").hasAnyAuthority(NOTEBOOK_CREATORS)
                .requestMatchers(HttpMethod.DELETE, "/api/projects/*/notebooks/**")
                .hasAnyAuthority(NOTEBOOK_REMOVERS)
                // project_file resource
                .requestMatchers(HttpMethod.GET, "/api/project_files").hasAnyAuthority(PROJECT_READERS)
                .requestMatchers(HttpMethod.GET, "/api/project_files/**").hasAnyAuthority(PROJECT_READERS)
                .requestMatchers(HttpMethod.POST, "/api/project_files").hasAnyAuthority(PROJECT_CREATORS)
                .requestMatchers(HttpMethod.DELETE, "/api/project_files/**").hasAnyAuthority(PROJECT_CREATORS)
                // project resource
                .requestMatchers(HttpMethod.GET, "/api/projects").hasAnyAuthority(PROJECT_READERS)
                .requestMatchers(HttpMethod.GET, "/api/projects/all").hasAuthority(CONTENT_EDITOR.name())
                .requestMatchers(HttpMethod.GET, "/api/projects/**").hasAnyAuthority(PROJECT_READERS)
                .requestMatchers(HttpMethod.POST, "/api/projects").hasAnyAuthority(PROJECT_CREATORS)
                .requestMatchers(HttpMethod.PUT, "/api/projects").hasAnyAuthority(PROJECT_CREATORS)
                .requestMatchers(HttpMethod.DELETE, "/api/projects/**").hasAnyAuthority(PROJECT_REMOVERS)
                // role resource
                .requestMatchers(HttpMethod.GET, "/api/roles").hasAnyAuthority(ROLE_READERS)
                .requestMatchers(HttpMethod.GET, "/api/roles/**").hasAuthority(ROLE_EDITOR.name())
                .requestMatchers(HttpMethod.POST, "/api/roles").hasAuthority(ROLE_EDITOR.name())
                .requestMatchers(HttpMethod.PUT, "/api/roles").hasAuthority(ROLE_EDITOR.name())
                .requestMatchers(HttpMethod.DELETE, "/api/roles/**").hasAuthority(ROLE_EDITOR.name())
                // template resource
                .requestMatchers(HttpMethod.GET, "/api/templates").hasAnyAuthority(TEMPLATE_READERS)
                .requestMatchers(HttpMethod.GET, "/api/templates/**").hasAuthority(TEMPLATE_EDITOR.name())
                .requestMatchers(HttpMethod.POST, "/api/templates").hasAuthority(TEMPLATE_EDITOR.name())
                .requestMatchers(HttpMethod.PUT, "/api/templates").hasAuthority(TEMPLATE_EDITOR.name())
                .requestMatchers(HttpMethod.DELETE, "/api/templates/**").hasAuthority(TEMPLATE_EDITOR.name())
                // user resource
                .requestMatchers(HttpMethod.GET, "/api/users").hasAnyAuthority(USER_READERS)
                .requestMatchers(HttpMethod.GET, "/api/users/permission-management").hasAnyAuthority(USER_READERS)
                .requestMatchers(HttpMethod.GET, "/api/users/**").hasAuthority(USER_EDITOR.name())
                .requestMatchers(HttpMethod.POST, "/api/users").hasAuthority(USER_EDITOR.name())
                .requestMatchers(HttpMethod.PUT, "/api/users").hasAuthority(USER_EDITOR.name())
                .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasAuthority(USER_EDITOR.name())
                // dictionary resource
                .requestMatchers(HttpMethod.GET, "/api/dictionaries").hasAnyAuthority(DICTIONARY_READERS)
                .requestMatchers(HttpMethod.GET, "/api/dictionaries/**").hasAnyAuthority(DICTIONARY_READERS)
                .requestMatchers(HttpMethod.POST, "/api/dictionaries").hasAuthority(DICTIONARY_EDITOR.name())
                .requestMatchers(HttpMethod.PUT, "/api/dictionaries").hasAuthority(DICTIONARY_EDITOR.name())
                .requestMatchers(HttpMethod.DELETE, "/api/dictionaries/**").hasAuthority(DICTIONARY_EDITOR.name())
                //search resource
                .requestMatchers(HttpMethod.POST, "/api/search").hasAuthority(GLOBAL_SEARCH.name())
                .requestMatchers(HttpMethod.GET, "/api/search/experiments").hasAnyAuthority(EXPERIMENT_READERS)
                // spring boot endpoints
                // https://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-endpoints.html
                .requestMatchers("/health/**").anonymous()
                .requestMatchers("/trace/**").anonymous()
                .requestMatchers("/dump/**").anonymous()
                .requestMatchers("/shutdown/**").anonymous()
                .requestMatchers("/beans/**").anonymous()
                .requestMatchers("/configprops/**").anonymous()
                .requestMatchers("/info/**").anonymous()
                .requestMatchers("/autoconfig/**").anonymous()
                .requestMatchers("/env/**").anonymous()
                .requestMatchers("/trace/**").anonymous()
                .requestMatchers("/mappings/**").anonymous()

                // others
                .requestMatchers("/api/bingodb/**").authenticated()
                .requestMatchers("/api/calculations/**").authenticated()
                .requestMatchers("/api/renderer/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/signature/document").hasAnyAuthority(EXPERIMENT_CREATORS)
                .requestMatchers("/api/signature/**").authenticated()
                .requestMatchers("/api/user_reagents/**").authenticated()
                .requestMatchers("/protected/**").authenticated()

                //restrictions for swagger
                .requestMatchers("/swagger-ui.html").authenticated()
                .requestMatchers("/v2/api-docs").authenticated()

                .requestMatchers("/websocket/**").authenticated()

                //print
                .requestMatchers(HttpMethod.GET, "/api/print/project/*").hasAnyAuthority(PROJECT_READERS)
                .requestMatchers(HttpMethod.GET, "/api/print/project/*/notebook/*")
                .hasAnyAuthority(NOTEBOOK_READERS)
                .requestMatchers(HttpMethod.GET, "/api/print/project/*/notebook/*/experiment/*")
                .hasAnyAuthority(EXPERIMENT_READERS);

        return http.build();
    }

    @Bean
    public SecurityEvaluationContextExtension securityEvaluationContextExtension() {
        return new SecurityEvaluationContextExtension();
    }

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    public SessionExpirationFilter sessionExpirationFilter() {
        return new SessionExpirationFilter(sessionRegistry());
    }

    private CookieCsrfTokenRepository cookieCsrfTokenRepository() {
        CookieCsrfTokenRepository cookieCsrfTokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        cookieCsrfTokenRepository.setCookieName(CookieConstants.CSRF_TOKEN);
        cookieCsrfTokenRepository.setHeaderName(CSRF_TOKEN_HEADER);
        return cookieCsrfTokenRepository;
    }

    @Bean
    public ServletListenerRegistrationBean<HttpSessionEventPublisher> httpSessionEventPublisher() {
        return new ServletListenerRegistrationBean<>(new HttpSessionEventPublisher());
    }
}

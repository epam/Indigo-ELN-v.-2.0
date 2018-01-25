package com.epam.indigoeln.config.security;

import com.epam.indigoeln.Application;
import com.epam.indigoeln.core.security.*;
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
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.data.repository.query.SecurityEvaluationContextExtension;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.session.ConcurrentSessionFilter;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

import static com.epam.indigoeln.core.security.Authority.*;
import static com.epam.indigoeln.core.security.CookieConstants.CSRF_TOKEN_HEADER;
import static com.epam.indigoeln.core.util.AuthoritiesUtil.*;


@Configuration
@EnableScheduling
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

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


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder());
    }

    @Bean
    @Profile(Application.Profile.CORS)
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowCredentials(true);
        configuration.setAllowedOrigins(Collections.singletonList(corsOrigin));
        configuration.setAllowedMethods(Collections.singletonList("*"));
        configuration.setAllowedHeaders(Collections.singletonList("*"));
        configuration.setExposedHeaders(Arrays.asList(
                HeaderUtil.SUCCESS_ALERT,
                HeaderUtil.TOTAL_COUNT,
                HttpHeaders.LINK,
                HttpHeaders.CONTENT_TYPE,
                HttpHeaders.COOKIE,
                HttpHeaders.SET_COOKIE,
                HttpHeaders.CONTENT_DISPOSITION));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        LOGGER.warn("Enabled CORS mappings for origin '" + corsOrigin + "'");

        return source;
    }

    @Override
    public void configure(WebSecurity web) {
        web.ignoring()
                .antMatchers("/scripts/**/*.{js,html}")
                .antMatchers("/bower_components/**")
                .antMatchers("/i18n/**")
                .antMatchers("/assets/**")
                .antMatchers("/test/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .sessionManagement() // add session management
                .maximumSessions(-1) // set unlimited number of sessions per User // TODO think about this
                .sessionRegistry(sessionRegistry());

        if (Arrays.asList(environment.getActiveProfiles()).contains(Application.Profile.CORS)) {
            http.cors();
        }

        http
                .csrf()
                .csrfTokenRepository(cookieCsrfTokenRepository())
                .ignoringAntMatchers("/api/authentication", "/api/logout", "/websocket/**") // For solving a
                // problem with login after logout
                .and()
                .addFilterBefore(sessionExpirationFilter(), ConcurrentSessionFilter.class)
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
                .antMatchers(HttpMethod.GET, "/api/accounts/**").authenticated()
                .antMatchers(HttpMethod.GET, "/api/accounts/account/roles").hasAuthority(ROLE_EDITOR.name())
                // experiment_file resource
                .antMatchers(HttpMethod.GET, "/api/experiment_files").hasAnyAuthority(EXPERIMENT_READERS)
                .antMatchers(HttpMethod.GET, "/api/experiment_files/**").hasAnyAuthority(EXPERIMENT_READERS)
                .antMatchers(HttpMethod.POST, "/api/experiment_files").hasAnyAuthority(EXPERIMENT_CREATORS)
                .antMatchers(HttpMethod.DELETE, "/api/experiment_files/**")
                .hasAnyAuthority(EXPERIMENT_CREATORS)
                // experiment resource
                //this request secured with method security
                .antMatchers(HttpMethod.GET, "/api/projects/*/notebooks/*/experiments")
                .authenticated()
                .antMatchers(HttpMethod.GET, "/api/projects/*/notebooks/*/experiments/all")
                .hasAuthority(CONTENT_EDITOR.name())
                .antMatchers(HttpMethod.GET, "/api/projects/*/notebooks/*/experiments/**")
                .hasAnyAuthority(EXPERIMENT_READERS)
                .antMatchers(HttpMethod.POST, "/api/projects/*/notebooks/*/experiments")
                .hasAnyAuthority(EXPERIMENT_CREATORS)
                .antMatchers(HttpMethod.POST, "/api/projects/*/notebooks/*/experiments/**")
                .hasAnyAuthority(EXPERIMENT_CREATORS)
                .antMatchers(HttpMethod.PUT, "/api/projects/*/notebooks/*/experiments")
                .hasAnyAuthority(EXPERIMENT_CREATORS)
                .antMatchers(HttpMethod.PUT, "/api/projects/*/notebooks/*/experiments/**")
                .hasAnyAuthority(EXPERIMENT_CREATORS)
                .antMatchers(HttpMethod.DELETE, "/api/projects/*/notebooks/*/experiments/**")
                .hasAnyAuthority(EXPERIMENT_REMOVERS)
                // notebook resource
                //this request secured with method security
                .antMatchers(HttpMethod.GET, "/api/projects/*/notebooks").authenticated()
                .antMatchers(HttpMethod.GET, "/api/projects/*/notebooks/all")
                .hasAuthority(CONTENT_EDITOR.name())
                .antMatchers(HttpMethod.GET, "/api/projects/*/notebooks/**").hasAnyAuthority(NOTEBOOK_READERS)
                .antMatchers(HttpMethod.POST, "/api/projects/*/notebooks").hasAnyAuthority(NOTEBOOK_CREATORS)
                .antMatchers(HttpMethod.PUT, "/api/projects/*/notebooks").hasAnyAuthority(NOTEBOOK_CREATORS)
                .antMatchers(HttpMethod.DELETE, "/api/projects/*/notebooks/**")
                .hasAnyAuthority(NOTEBOOK_REMOVERS)
                // project_file resource
                .antMatchers(HttpMethod.GET, "/api/project_files").hasAnyAuthority(PROJECT_READERS)
                .antMatchers(HttpMethod.GET, "/api/project_files/**").hasAnyAuthority(PROJECT_READERS)
                .antMatchers(HttpMethod.POST, "/api/project_files").hasAnyAuthority(PROJECT_CREATORS)
                .antMatchers(HttpMethod.DELETE, "/api/project_files/**").hasAnyAuthority(PROJECT_CREATORS)
                // project resource
                .antMatchers(HttpMethod.GET, "/api/projects").hasAnyAuthority(PROJECT_READERS)
                .antMatchers(HttpMethod.GET, "/api/projects/all").hasAuthority(CONTENT_EDITOR.name())
                .antMatchers(HttpMethod.GET, "/api/projects/**").hasAnyAuthority(PROJECT_READERS)
                .antMatchers(HttpMethod.POST, "/api/projects").hasAnyAuthority(PROJECT_CREATORS)
                .antMatchers(HttpMethod.PUT, "/api/projects").hasAnyAuthority(PROJECT_CREATORS)
                .antMatchers(HttpMethod.DELETE, "/api/projects/**").hasAnyAuthority(PROJECT_REMOVERS)
                // role resource
                .antMatchers(HttpMethod.GET, "/api/roles").hasAnyAuthority(ROLE_READERS)
                .antMatchers(HttpMethod.GET, "/api/roles/**").hasAuthority(ROLE_EDITOR.name())
                .antMatchers(HttpMethod.POST, "/api/roles").hasAuthority(ROLE_EDITOR.name())
                .antMatchers(HttpMethod.PUT, "/api/roles").hasAuthority(ROLE_EDITOR.name())
                .antMatchers(HttpMethod.DELETE, "/api/roles/**").hasAuthority(ROLE_EDITOR.name())
                // template resource
                .antMatchers(HttpMethod.GET, "/api/templates").hasAnyAuthority(TEMPLATE_READERS)
                .antMatchers(HttpMethod.GET, "/api/templates/**").hasAuthority(TEMPLATE_EDITOR.name())
                .antMatchers(HttpMethod.POST, "/api/templates").hasAuthority(TEMPLATE_EDITOR.name())
                .antMatchers(HttpMethod.PUT, "/api/templates").hasAuthority(TEMPLATE_EDITOR.name())
                .antMatchers(HttpMethod.DELETE, "/api/templates/**").hasAuthority(TEMPLATE_EDITOR.name())
                // user resource
                .antMatchers(HttpMethod.GET, "/api/users").hasAnyAuthority(USER_READERS)
                .antMatchers(HttpMethod.GET, "/api/users/permission-management").hasAnyAuthority(USER_READERS)
                .antMatchers(HttpMethod.GET, "/api/users/**").hasAuthority(USER_EDITOR.name())
                .antMatchers(HttpMethod.POST, "/api/users").hasAuthority(USER_EDITOR.name())
                .antMatchers(HttpMethod.PUT, "/api/users").hasAuthority(USER_EDITOR.name())
                .antMatchers(HttpMethod.DELETE, "/api/users/**").hasAuthority(USER_EDITOR.name())
                // dictionary resource
                .antMatchers(HttpMethod.GET, "/api/dictionaries").hasAnyAuthority(DICTIONARY_READERS)
                .antMatchers(HttpMethod.GET, "/api/dictionaries/**").hasAnyAuthority(DICTIONARY_READERS)
                .antMatchers(HttpMethod.POST, "/api/dictionaries").hasAuthority(DICTIONARY_EDITOR.name())
                .antMatchers(HttpMethod.PUT, "/api/dictionaries").hasAuthority(DICTIONARY_EDITOR.name())
                .antMatchers(HttpMethod.DELETE, "/api/dictionaries/**").hasAuthority(DICTIONARY_EDITOR.name())
                //search resource
                .antMatchers(HttpMethod.POST, "/api/search").hasAuthority(GLOBAL_SEARCH.name())
                .antMatchers(HttpMethod.GET, "/api/search/experiments").hasAnyAuthority(EXPERIMENT_READERS)
                // spring boot endpoints
                // https://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-endpoints.html
                .antMatchers("/health/**").authenticated()
                .antMatchers("/trace/**").authenticated()
                .antMatchers("/dump/**").authenticated()
                .antMatchers("/shutdown/**").authenticated()
                .antMatchers("/beans/**").authenticated()
                .antMatchers("/configprops/**").authenticated()
                .antMatchers("/info/**").authenticated()
                .antMatchers("/autoconfig/**").authenticated()
                .antMatchers("/env/**").authenticated()
                .antMatchers("/trace/**").authenticated()
                .antMatchers("/mappings/**").authenticated()

                // others
                .antMatchers("/api/bingodb/**").authenticated()
                .antMatchers("/api/calculations/**").authenticated()
                .antMatchers("/api/renderer/**").authenticated()
                .antMatchers(HttpMethod.POST, "/api/signature/document").hasAnyAuthority(EXPERIMENT_CREATORS)
                .antMatchers("/api/signature/**").authenticated()
                .antMatchers("/api/user_reagents/**").authenticated()
                .antMatchers("/protected/**").authenticated()

                //restrictions for swagger
                .antMatchers("/swagger-ui.html").authenticated()
                .antMatchers("/v2/api-docs").authenticated()

                .antMatchers("/websocket/**").authenticated()

                //print
                .antMatchers(HttpMethod.GET, "/api/print/project/*").hasAnyAuthority(PROJECT_READERS)
                .antMatchers(HttpMethod.GET, "/api/print/project/*/notebook/*")
                .hasAnyAuthority(NOTEBOOK_READERS)
                .antMatchers(HttpMethod.GET, "/api/print/project/*/notebook/*/experiment/*")
                .hasAnyAuthority(EXPERIMENT_READERS);
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

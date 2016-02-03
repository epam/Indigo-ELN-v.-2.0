package com.epam.indigoeln.config.security;

import com.epam.indigoeln.core.security.*;
import com.epam.indigoeln.web.rest.filter.CsrfCookieGeneratorFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.data.repository.query.SecurityEvaluationContextExtension;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.csrf.CsrfFilter;

import static com.epam.indigoeln.core.security.Authority.*;


@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private Environment env;

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

    @Override
    public void configure(WebSecurity web) throws Exception {
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
            .csrf()
                .ignoringAntMatchers("/api/authentication") // For solving a problem with login after logout
            .and()
                .addFilterAfter(new CsrfCookieGeneratorFilter(), CsrfFilter.class)
                .exceptionHandling()
                    .accessDeniedHandler(new CustomAccessDeniedHandler())
                    .authenticationEntryPoint(authenticationEntryPoint)
            .and()
                .rememberMe()
                    .rememberMeServices(rememberMeServices)
                    .rememberMeParameter("remember-me")
                    .key(env.getProperty("indigoeln.security.rememberme.key"))
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
                    .antMatchers("/api/**").authenticated()
                    // bingoDbIntegration resource
                    //TODO set up
                    // calculation resource
                    //TODO set up
                    // component resource
                    .antMatchers(HttpMethod.GET, "/api/experiments/*/components").hasAnyAuthority(COMPONENT_READERS)
                    .antMatchers(HttpMethod.GET, "/api/experiments/*/components/*").hasAnyAuthority(COMPONENT_READERS)
                    .antMatchers(HttpMethod.POST, "/api/experiments/*/components").hasAnyAuthority(COMPONENT_EDITORS)
                    .antMatchers(HttpMethod.PUT, "/api/experiments/*/components").hasAnyAuthority(COMPONENT_EDITORS)
                    .antMatchers(HttpMethod.DELETE, "/api/experiments/*/components/*").hasAnyAuthority(COMPONENT_EDITORS)
                    // experiment_file resource
                    .antMatchers(HttpMethod.GET, "/api/experiment_files").hasAnyAuthority(EXPERIMENT_READERS)
                    .antMatchers(HttpMethod.GET, "/api/experiment_files/*").hasAnyAuthority(EXPERIMENT_READERS)
                    .antMatchers(HttpMethod.POST, "/api/experiment_files").hasAnyAuthority(EXPERIMENT_CREATORS)
                    .antMatchers(HttpMethod.DELETE, "/api/experiment_files/*").hasAnyAuthority(EXPERIMENT_CREATORS)
                    // experiment resource
                    .antMatchers(HttpMethod.GET, "/api/experiments").hasAnyAuthority(EXPERIMENT_READERS)
                    .antMatchers(HttpMethod.GET, "/api/experiments/*").hasAnyAuthority(EXPERIMENT_READERS)
                    .antMatchers(HttpMethod.POST, "/api/experiments").hasAnyAuthority(EXPERIMENT_CREATORS)
                    .antMatchers(HttpMethod.PUT, "/api/experiments").hasAnyAuthority(EXPERIMENT_CREATORS)
                    .antMatchers(HttpMethod.DELETE, "/api/experiments/*").hasAnyAuthority(EXPERIMENT_REMOVERS)
                    .antMatchers(HttpMethod.GET, "/api/experiments/tables").permitAll() // TODO Need to configure
                    // notebook resource
                    .antMatchers(HttpMethod.GET, "/api/notebooks").hasAnyAuthority(NOTEBOOK_READERS)
                    .antMatchers(HttpMethod.GET, "/api/notebooks/*").hasAnyAuthority(NOTEBOOK_READERS)
                    .antMatchers(HttpMethod.POST, "/api/notebooks").hasAnyAuthority(NOTEBOOK_CREATORS)
                    .antMatchers(HttpMethod.PUT, "/api/notebooks").hasAnyAuthority(NOTEBOOK_CREATORS)
                    .antMatchers(HttpMethod.DELETE, "/api/notebooks/*").hasAnyAuthority(NOTEBOOK_REMOVERS)
                    // project_file resource
                    .antMatchers(HttpMethod.GET, "/api/project_files").hasAnyAuthority(PROJECT_READERS)
                    .antMatchers(HttpMethod.GET, "/api/project_files/*").hasAnyAuthority(PROJECT_READERS)
                    .antMatchers(HttpMethod.POST, "/api/project_files").hasAnyAuthority(PROJECT_CREATORS)
                    .antMatchers(HttpMethod.DELETE, "/api/project_files/*").hasAnyAuthority(PROJECT_CREATORS)
                    // project resource
                    .antMatchers(HttpMethod.GET, "/api/projects").hasAnyAuthority(PROJECT_READERS)
                    .antMatchers(HttpMethod.GET, "/api/projects/*").hasAnyAuthority(PROJECT_READERS)
                    .antMatchers(HttpMethod.POST, "/api/projects").hasAnyAuthority(PROJECT_CREATORS)
                    .antMatchers(HttpMethod.PUT, "/api/projects").hasAnyAuthority(PROJECT_CREATORS)
                    .antMatchers(HttpMethod.DELETE, "/api/projects/*").hasAnyAuthority(PROJECT_REMOVERS)
                    // template resource
                    .antMatchers(HttpMethod.GET, "/api/templates").hasAnyAuthority(TEMPLATE_READERS)
                    .antMatchers(HttpMethod.GET, "/api/templates/*").hasAuthority(TEMPLATE_EDITOR.name())
                    .antMatchers(HttpMethod.POST, "/api/templates").hasAuthority(TEMPLATE_EDITOR.name())
                    .antMatchers(HttpMethod.PUT, "/api/templates").hasAuthority(TEMPLATE_EDITOR.name())
                    .antMatchers(HttpMethod.DELETE, "/api/templates/*").hasAuthority(TEMPLATE_EDITOR.name())
                    // user resource
                    .antMatchers(HttpMethod.GET, "/api/users").hasAnyAuthority(USER_READERS)
                    .antMatchers(HttpMethod.GET, "/api/users/*").hasAuthority(USER_EDITOR.name())
                    .antMatchers(HttpMethod.POST, "/api/users").hasAuthority(USER_EDITOR.name())
                    .antMatchers(HttpMethod.PUT, "/api/users").hasAuthority(USER_EDITOR.name())
                    .antMatchers(HttpMethod.DELETE, "/api/users/*").hasAuthority(USER_EDITOR.name())
                    // others
                    .antMatchers("/health/**").authenticated() // TODO Which Authority do need to use?
                    .antMatchers("/trace/**").authenticated() // TODO Which Authority do need to use?
                    .antMatchers("/dump/**").authenticated() // TODO Which Authority do need to use?
                    .antMatchers("/shutdown/**").authenticated() // TODO Which Authority do need to use?
                    .antMatchers("/beans/**").authenticated() // TODO Which Authority do need to use?
                    .antMatchers("/configprops/**").authenticated() // TODO Which Authority do need to use?
                    .antMatchers("/info/**").authenticated() // TODO Which Authority do need to use?
                    .antMatchers("/autoconfig/**").authenticated() // TODO Which Authority do need to use?
                    .antMatchers("/env/**").authenticated() // TODO Which Authority do need to use?
                    .antMatchers("/trace/**").authenticated() // TODO Which Authority do need to use?
                    .antMatchers("/mappings/**").authenticated() // TODO Which Authority do need to use?
                    .antMatchers("/protected/**").authenticated();
    }

    @Bean
    public SecurityEvaluationContextExtension securityEvaluationContextExtension() {
        return new SecurityEvaluationContextExtension();
    }
}
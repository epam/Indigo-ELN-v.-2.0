package com.epam.indigo.bingodb.config;

import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;

@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http
            .csrf().disable()
            .addFilterBefore(new CORSFilter(), ChannelProcessingFilter.class)
            .authorizeRequests().antMatchers(HttpMethod.OPTIONS, "**").permitAll();
    }
}
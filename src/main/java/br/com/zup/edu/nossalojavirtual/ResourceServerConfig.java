package br.com.zup.edu.nossalojavirtual;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class ResourceServerConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .cors()
                .and()
                    .csrf().disable()
                    .httpBasic().disable()
                    .rememberMe().disable()
                    .formLogin().disable()
                    .logout().disable()
                    .requestCache().disable()
                    .headers().frameOptions().deny()
                .and()
                    .sessionManagement()
                        .sessionCreationPolicy(STATELESS)
                .and()
                        .authorizeRequests()
                        .antMatchers(HttpMethod.POST, "/api/categories").hasAuthority("SCOPE_categories:write")
                        .antMatchers(HttpMethod.POST, "/api/users").hasAuthority("SCOPE_users:write")
                        .antMatchers(HttpMethod.POST, "/api/products").hasAuthority("SCOPE_product:write")
                        .antMatchers(HttpMethod.POST, "/api/opinions").hasAuthority("SCOPE_opinion:write")
                        .antMatchers(HttpMethod.POST, "/api/products/**/questions").hasAuthority("SCOPE_questions:write")
                        .antMatchers(HttpMethod.POST, "/api/purchase").hasAuthority("SCOPE_purchase:write")
                        .antMatchers(HttpMethod.GET, "/api/products/**").hasAuthority("SCOPE_product:read")
                        .antMatchers(HttpMethod.GET, "/actuator/").hasAuthority("SCOPE_actuator:read")
                        .antMatchers("/h2-console/**").permitAll()
                    .anyRequest()
                        .authenticated()
                    .and()
                        .oauth2ResourceServer()
                            .jwt(jwt -> jwt.jwkSetUri("http://localhost:18080/realms/nosa-loja-virtual/protocol/openid-connect/certs"));


        http.headers().frameOptions().sameOrigin();
    }
}

package vn.iotstar.configs;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.web.cors.*;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.BadCredentialsException;
import java.util.List;
import vn.iotstar.filter.JwtAuthenticationFilter;
@Configuration
public class SecurityConfiguration {
 @Bean public SecurityFilterChain securityFilterChain(HttpSecurity http,JwtAuthenticationFilter filter,DaoAuthenticationProvider provider,
  @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) throws Exception {
  return http.csrf(csrf->csrf.disable()).cors(cors->{})
   .authorizeHttpRequests(auth->auth.requestMatchers("/","/auth/signup","/auth/login","/login","/user/profile","/js/**","/css/**","/images/**","/error").permitAll().anyRequest().authenticated())
   .sessionManagement(session->session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
   .requestCache(cache->cache.disable())
   .authenticationProvider(provider)
   .exceptionHandling(ex->ex.authenticationEntryPoint((req,res,e)->resolver.resolveException(req,res,null,new BadCredentialsException("Vui lòng gửi Bearer token.")))
    .accessDeniedHandler((req,res,e)->resolver.resolveException(req,res,null,e)))
   .addFilterBefore(filter,UsernamePasswordAuthenticationFilter.class).build();
 }
 @Bean public FilterRegistrationBean<JwtAuthenticationFilter> filterRegistration(JwtAuthenticationFilter filter){
  var registration=new FilterRegistrationBean<>(filter);registration.setEnabled(false);return registration;
 }
 @Bean public CorsConfigurationSource corsConfigurationSource(){
  var config=new CorsConfiguration();config.setAllowedOrigins(List.of("http://localhost:8005","http://127.0.0.1:8005"));
  config.setAllowedMethods(List.of("GET","POST","OPTIONS"));config.setAllowedHeaders(List.of("Authorization","Content-Type","Cache-Control"));
  var source=new UrlBasedCorsConfigurationSource();source.registerCorsConfiguration("/**",config);return source;
 }
}


package vn.iotstar.filter;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.security.authentication.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.beans.factory.annotation.Qualifier;
import vn.iotstar.services.JwtService;
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
 private final JwtService jwtService;
 private final UserDetailsService userDetailsService;
 private final HandlerExceptionResolver resolver;
 public JwtAuthenticationFilter(JwtService jwtService,UserDetailsService service,
  @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver){this.jwtService=jwtService;this.userDetailsService=service;this.resolver=resolver;}
 @Override protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain chain) throws ServletException,IOException {
  String header=request.getHeader("Authorization");
  if(header==null){chain.doFilter(request,response);return;}
  try {
   if(!header.startsWith("Bearer ") || header.substring(7).isBlank()) throw new BadCredentialsException("Authorization cần Bearer token.");
   String email=jwtService.extractUsername(header.substring(7));
   UserDetails user=userDetailsService.loadUserByUsername(email);
   if(!user.isEnabled() || !user.isAccountNonLocked()) throw new LockedException("Tài khoản bị khóa.");
   var authentication=new UsernamePasswordAuthenticationToken(user,null,user.getAuthorities());
   authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
   SecurityContextHolder.getContext().setAuthentication(authentication);
  } catch(org.springframework.security.core.AuthenticationException e){
   SecurityContextHolder.clearContext(); resolver.resolveException(request,response,null,e); return;
  }
  chain.doFilter(request,response);
 }
}


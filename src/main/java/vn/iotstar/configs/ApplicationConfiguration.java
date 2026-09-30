package vn.iotstar.configs;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.*;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.iotstar.repository.UserRepository;
@Configuration @RequiredArgsConstructor
public class ApplicationConfiguration {
 private final UserRepository repository;
 @Bean public UserDetailsService userDetailsService(){return email->repository.findByEmail(email).orElseThrow(()->new UsernameNotFoundException("Không tìm thấy tài khoản."));}
 @Bean public PasswordEncoder passwordEncoder(){return new BCryptPasswordEncoder();}
 @Bean public DaoAuthenticationProvider authenticationProvider(UserDetailsService service,PasswordEncoder encoder){
  DaoAuthenticationProvider provider=new DaoAuthenticationProvider(service); provider.setPasswordEncoder(encoder); return provider;
 }
 @Bean public AuthenticationManager authenticationManager(DaoAuthenticationProvider provider){return new ProviderManager(provider);}
}


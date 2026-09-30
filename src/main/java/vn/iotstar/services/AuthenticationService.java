package vn.iotstar.services;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import vn.iotstar.entity.User;
import vn.iotstar.models.*;
import vn.iotstar.repository.UserRepository;
@Service @RequiredArgsConstructor
public class AuthenticationService {
 private final UserRepository repository;
 private final PasswordEncoder encoder;
 private final AuthenticationManager manager;
 @Transactional public User signup(RegisterUserModel input){
  String email=normalize(input.email());
  if(repository.existsByEmail(email)) throw new ResponseStatusException(HttpStatus.CONFLICT,"Email đã được đăng ký.");
  if(input.password().getBytes(java.nio.charset.StandardCharsets.UTF_8).length>72)
   throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Mật khẩu không được vượt quá 72 byte.");
  User user=new User(); user.setEmail(email); user.setFullName(input.fullName().trim());
  user.setPassword(encoder.encode(input.password())); return repository.saveAndFlush(user);
 }
 public User authenticate(LoginUserModel input){
  return (User)manager.authenticate(new UsernamePasswordAuthenticationToken(normalize(input.email()),input.password())).getPrincipal();
 }
 private String normalize(String email){return email.trim().toLowerCase(Locale.ROOT);}
}


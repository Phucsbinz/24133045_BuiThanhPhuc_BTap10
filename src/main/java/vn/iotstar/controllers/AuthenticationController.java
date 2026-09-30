package vn.iotstar.controllers;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import vn.iotstar.models.*;
import vn.iotstar.services.*;
@RestController @RequestMapping("/auth") @RequiredArgsConstructor
public class AuthenticationController {
 private final AuthenticationService authenticationService;
 private final JwtService jwtService;
 @PostMapping("/signup") public UserResponse register(@Valid @RequestBody RegisterUserModel input){return UserResponse.from(authenticationService.signup(input));}
 @PostMapping("/login") public LoginResponse authenticate(@Valid @RequestBody LoginUserModel input){
  var user=authenticationService.authenticate(input);return new LoginResponse(jwtService.generateToken(user),jwtService.getExpirationTime());
 }
}


package vn.iotstar.controllers;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import java.util.List;
import vn.iotstar.entity.User;
import vn.iotstar.models.UserResponse;
import vn.iotstar.services.UserService;
@RestController @RequestMapping("/users") @RequiredArgsConstructor
public class UserController {
 private final UserService userService;
 @GetMapping("/me") public UserResponse authenticatedUser(@AuthenticationPrincipal User user){return UserResponse.from(user);}
 @GetMapping({"","/"}) public List<UserResponse> allUsers(){return userService.allUsers();}
}


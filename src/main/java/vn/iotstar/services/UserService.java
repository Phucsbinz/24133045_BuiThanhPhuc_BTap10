package vn.iotstar.services;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.iotstar.models.UserResponse;
import vn.iotstar.repository.UserRepository;
@Service @RequiredArgsConstructor
public class UserService {
 private final UserRepository repository;
 public List<UserResponse> allUsers(){return repository.findAll().stream().map(UserResponse::from).toList();}
}


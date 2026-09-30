package vn.iotstar.models;
import java.time.Instant;
import vn.iotstar.entity.User;
public record UserResponse(Integer id,String fullName,String email,String images,Instant createdAt,Instant updatedAt) {
 public static UserResponse from(User u){return new UserResponse(u.getId(),u.getFullName(),u.getEmail(),u.getImages(),u.getCreatedAt(),u.getUpdatedAt());}
}


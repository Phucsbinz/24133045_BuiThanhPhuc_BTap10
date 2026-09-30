package vn.iotstar.entity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
@Entity
@Table(name="users")
@Getter @Setter
public class User implements UserDetails {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Integer id;
 @Column(name="full_name",nullable=false,length=100) private String fullName;
 @Column(nullable=false,unique=true,length=100) private String email;
 @Column(nullable=false,length=255) private String password;
 @Column(nullable=false,length=500) private String images="/images/avatar.svg";
 @Column(nullable=false) private boolean enabled=true;
 @CreationTimestamp @Column(name="created_at",nullable=false,updatable=false) private Instant createdAt;
 @UpdateTimestamp @Column(name="updated_at",nullable=false) private Instant updatedAt;
 @Override public String getUsername(){return email;}
 @Override public Collection<? extends GrantedAuthority> getAuthorities(){return List.of();}
 @Override public boolean isAccountNonExpired(){return true;}
 @Override public boolean isAccountNonLocked(){return enabled;}
 @Override public boolean isCredentialsNonExpired(){return true;}
 @Override public boolean isEnabled(){return enabled;}
}


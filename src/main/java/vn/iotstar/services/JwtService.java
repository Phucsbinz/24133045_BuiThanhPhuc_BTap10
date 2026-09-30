package vn.iotstar.services;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.*;
import com.nimbusds.jwt.*;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
@Service
public class JwtService {
 private final byte[] key;
 private final long expiration;
 public JwtService(@Value("${security.jwt.secret-key}") String secret,@Value("${security.jwt.expiration-time}") long expiration){
  this.key=Base64.getDecoder().decode(secret); this.expiration=expiration;
  if(key.length<32 || expiration<=0) throw new IllegalArgumentException("JWT cần khóa ít nhất 32 byte và thời hạn dương.");
 }
 public long getExpirationTime(){return expiration;}
 public String generateToken(UserDetails user){
  try {
   Instant now=Instant.now();
   JWTClaimsSet claims=new JWTClaimsSet.Builder().subject(user.getUsername()).issueTime(Date.from(now))
    .expirationTime(Date.from(now.plusMillis(expiration))).build();
   SignedJWT jwt=new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.HS256).type(JOSEObjectType.JWT).build(),claims);
   jwt.sign(new MACSigner(key)); return jwt.serialize();
  } catch(JOSEException e){throw new IllegalStateException("Không thể tạo JWT.",e);}
 }
 // Nimbus verify chỉ kiểm tra chữ ký; phải kiểm tra claims trước khi dùng subject.
 public String extractUsername(String token){
  try {
   SignedJWT jwt=SignedJWT.parse(token);
   if(!JWSAlgorithm.HS256.equals(jwt.getHeader().getAlgorithm()) || !jwt.verify(new MACVerifier(key)))
    throw new BadCredentialsException("Chữ ký JWT không hợp lệ.");
   JWTClaimsSet claims=jwt.getJWTClaimsSet();
   Date now=new Date();
   if(claims.getExpirationTime()==null || !claims.getExpirationTime().after(now))
    throw new BadCredentialsException("JWT đã hết hạn hoặc thiếu thời hạn.");
   if(claims.getNotBeforeTime()!=null && claims.getNotBeforeTime().after(now))
    throw new BadCredentialsException("JWT chưa có hiệu lực.");
   if(claims.getSubject()==null || claims.getSubject().isBlank()) throw new BadCredentialsException("JWT thiếu subject.");
   return claims.getSubject();
  } catch(java.text.ParseException | JOSEException e){throw new BadCredentialsException("JWT không hợp lệ.",e);}
 }
 public boolean isTokenValid(String token,UserDetails user){return extractUsername(token).equals(user.getUsername());}
}


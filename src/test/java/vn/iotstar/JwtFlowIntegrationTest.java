package vn.iotstar;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.*;
import java.util.*;
import java.time.Instant;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.security.web.FilterChainProxy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.iotstar.repository.UserRepository;
@SpringBootTest @ActiveProfiles("test")
class JwtFlowIntegrationTest {
 @Autowired WebApplicationContext context;
 @Autowired FilterChainProxy security;
 @Autowired UserRepository repository;
 @Autowired PasswordEncoder encoder;
 MockMvc mvc;
 @BeforeEach void setup(){mvc=MockMvcBuilders.webAppContextSetup(context).addFilters(security).build();repository.deleteAll();}
 private String signup() throws Exception {
  return mvc.perform(post("/auth/signup").contentType(MediaType.APPLICATION_JSON)
   .content("{\"email\":\"phuc@example.com\",\"password\":\"123456\",\"fullName\":\"Bùi Thanh Phúc\"}"))
   .andExpect(status().isOk()).andExpect(jsonPath("$.password").doesNotExist()).andExpect(jsonPath("$.images").value("/images/avatar.svg")).andReturn().getResponse().getContentAsString();
 }
 private String login() throws Exception {
  String json=mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"phuc@example.com\",\"password\":\"123456\"}"))
   .andExpect(status().isOk()).andExpect(jsonPath("$.expiresIn").value(3600000)).andReturn().getResponse().getContentAsString();
  return (String)com.nimbusds.jose.util.JSONObjectUtils.parse(json).get("token");
 }
 private String token(String subject,Date exp,JWSAlgorithm alg,byte[] key) throws Exception {
  var claims=new JWTClaimsSet.Builder().subject(subject).issueTime(new Date());
  if(exp!=null) claims.expirationTime(exp);
  var jwt=new SignedJWT(new JWSHeader(alg),claims.build());jwt.sign(new MACSigner(key));return jwt.serialize();
 }
 @Test void fullFlowAndNoSession() throws Exception {
  signup(); var user=repository.findByEmail("phuc@example.com").orElseThrow();
  assertTrue(encoder.matches("123456",user.getPassword()));assertNotEquals("123456",user.getPassword());
  String token=login(); var jwt=SignedJWT.parse(token);
  assertEquals(JWSAlgorithm.HS256,jwt.getHeader().getAlgorithm());
  assertEquals(3600000,jwt.getJWTClaimsSet().getExpirationTime().getTime()-jwt.getJWTClaimsSet().getIssueTime().getTime());
  mvc.perform(get("/users/me").header("Authorization","Bearer "+token)).andExpect(status().isOk()).andExpect(jsonPath("$.fullName").value("Bùi Thanh Phúc"))
   .andExpect(jsonPath("$.password").doesNotExist()).andExpect(cookie().doesNotExist("JSESSIONID"));
  for(String path:List.of("/users","/users/")) mvc.perform(get(path).header("Authorization","Bearer "+token)).andExpect(status().isOk()).andExpect(jsonPath("$[0].email").value("phuc@example.com"));
  mvc.perform(get("/users/me")).andExpect(status().isUnauthorized());
 }
 @Test void duplicateAndValidation() throws Exception {
  signup();
  mvc.perform(post("/auth/signup").contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"phuc@example.com\",\"password\":\"123456\",\"fullName\":\"Phuc\"}")).andExpect(status().isConflict());
  mvc.perform(post("/auth/signup").contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"bad\",\"password\":\"1\",\"fullName\":\"\"}")).andExpect(status().isBadRequest());
  mvc.perform(post("/auth/signup").contentType(MediaType.APPLICATION_JSON).content("{")).andExpect(status().isBadRequest());
 }
 @Test void invalidLoginAndDisabledAccount() throws Exception {
  signup();
  mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"phuc@example.com\",\"password\":\"wrong\"}")).andExpect(status().isUnauthorized());
  String token=login(); var user=repository.findByEmail("phuc@example.com").orElseThrow();user.setEnabled(false);repository.saveAndFlush(user);
  mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"phuc@example.com\",\"password\":\"123456\"}")).andExpect(status().isForbidden());
  mvc.perform(get("/users/me").header("Authorization","Bearer "+token)).andExpect(status().isForbidden());
 }
 @Test void invalidTokensAre401() throws Exception {
  signup(); byte[] key=new byte[32];byte[] wrong=new byte[32];Arrays.fill(wrong,(byte)1);
  for(String invalid:List.of("nonsense", token("phuc@example.com",Date.from(Instant.now().minusSeconds(5)),JWSAlgorithm.HS256,key),
   token("phuc@example.com",null,JWSAlgorithm.HS256,key),
   token("unknown@example.com",Date.from(Instant.now().plusSeconds(60)),JWSAlgorithm.HS256,key),
   token("phuc@example.com",Date.from(Instant.now().plusSeconds(60)),JWSAlgorithm.HS256,wrong),
   token("phuc@example.com",Date.from(Instant.now().plusSeconds(60)),JWSAlgorithm.HS512,new byte[64]),
   token("",Date.from(Instant.now().plusSeconds(60)),JWSAlgorithm.HS256,key))){
    mvc.perform(get("/users/me").header("Authorization","Bearer "+invalid)).andExpect(status().isUnauthorized()).andExpect(jsonPath("$.status").value(401));
   }
  mvc.perform(get("/users/me").header("Authorization","Basic abc")).andExpect(status().isUnauthorized());
 }
 @Test void publicViewsAndAssets() throws Exception {
  for(String path:List.of("/login","/user/profile","/js/mainjs.js","/images/avatar.svg")) mvc.perform(get(path)).andExpect(status().isOk());
  mvc.perform(get("/")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/login"));
 }
}


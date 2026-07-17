package in.lakshay.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import in.lakshay.entity.User;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Base64; // needed for secret decoding

@Component // makes this a spring bean
@Slf4j // adds logging support
public class JwtUtil {
    // jwt stuff from application.properties
    @Value("${jwt.secret}")
    private String secret; // base64 encoded secret key

    @Value("${jwt.expiration}")
    private Long expiration; // how long tokens last in ms

    private SecretKey key; // the hmac key we use for signing

    // runs after construction but before bean is used
    @PostConstruct
    public void init() {
        // init the key once and reuse - more efficient this way
        byte[] keyBytes = Base64.getDecoder().decode(secret);
        key = Keys.hmacShaKeyFor(keyBytes); // make HMAC key from bytes
    }

    // makes a new JWT when user logs in
    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId()); // stick user ID in token
        String roleName = user.getRole().getName();

        // gotta have ROLE_ prefix - spring security is picky about this
        String roleWithPrefix = roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName;
        claims.put("roles", List.of(roleWithPrefix)); // add roles as list

        // build and sign the token
        return Jwts.builder()
                .claims(claims) // our custom stuff
                .subject(user.getUserName()) // username goes in subject
                .issuedAt(new Date()) // current time
                .expiration(new Date(System.currentTimeMillis() + expiration)) // when it expires
                .signWith(key, Jwts.SIG.HS512) // sign w/ HS512
                .compact(); // make it a string
    }

    // gets and checks claims from a token
    // throws exceptions if token is bad/expired
    public Claims getClaimsFromToken(String token) {
        try {
            // parse and verify the token
            Claims claims = Jwts.parser()
                    .verifyWith(key) // check signature
                    .build()
                    .parseSignedClaims(token) // parse JWT
                    .getPayload(); // get body
            return claims;
        } catch (Exception e) {
            // could be expired, bad signature, malformed, whatever
            log.error("Error extracting claims from token: {}", e.getMessage());
            throw e; // let auth filter deal with it
        }
    }



    public String getUsernameFromToken(String token) {
        log.debug("Getting username from token");
        String username = getClaimsFromToken(token).getSubject();
        log.debug("Username from token: {}", username);
        return username;
    }

    // quick helper to get userId from token
    public Long getUserIdFromToken(String token) {
        return getClaimsFromToken(token).get("userId", Long.class);
    }

    @SuppressWarnings("unchecked") // compiler warning but it's fine
    public List<String> getRolesFromToken(String token) {
        log.debug("Getting roles from token");
        List<String> roles = getClaimsFromToken(token).get("roles", List.class);
        log.debug("Roles from token: {}", roles);
        return roles;
    }

    // checks if token is expired
    // true = expired, false = still good
    public boolean isTokenExpired(String token) {
        log.debug("Checking if token is expired");
        Date expiration = getClaimsFromToken(token).getExpiration();
        boolean isExpired = expiration.before(new Date()); // check against current time
        log.debug("Token expired: {}, expiration: {}", isExpired, expiration);
        return isExpired; // true means it's expired
    }
    // TODO: add refresh token support later
}
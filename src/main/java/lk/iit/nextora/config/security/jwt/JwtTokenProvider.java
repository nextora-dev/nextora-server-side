package lk.iit.nextora.config.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lk.iit.nextora.common.enums.StudentRoleType;
import lk.iit.nextora.module.auth.entity.BaseUser;
import lk.iit.nextora.module.auth.entity.Student;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        // Create a secure key - ensure secret is at least 256 bits (32 chars) for HS256
        byte[] keyBytes = jwtProperties.getSecretKey().getBytes();
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(UserDetails userDetails) {
        Map<String, Object> claims = buildClaims(userDetails);
        return buildToken(userDetails.getUsername(), claims, jwtProperties.getExpiration());
    }

    public String generateRefreshToken(UserDetails userDetails) {
        // Refresh token only contains minimal claims
        return buildToken(userDetails.getUsername(), new HashMap<>(), jwtProperties.getRefreshToken().getExpiration());
    }

    private Map<String, Object> buildClaims(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();

        // Add authorities/permissions
        List<String> authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        claims.put("authorities", authorities);

        // Add user-specific claims if BaseUser
        if (userDetails instanceof BaseUser baseUser) {
            claims.put("role", baseUser.getRole().name());
            claims.put("userId", baseUser.getId());
            claims.put("userType", baseUser.getUserType());
            claims.put("fullName", baseUser.getFullName());

            // Add student sub-roles if applicable
            if (baseUser instanceof Student student) {
                java.util.Set<StudentRoleType> roleTypes = student.getStudentRoleTypes();
                if (roleTypes != null && !roleTypes.isEmpty()) {
                    claims.put("studentRoleTypes", roleTypes.stream()
                            .map(StudentRoleType::name)
                            .toList());
                    claims.put("primaryStudentRoleType", student.getPrimaryRoleType().name());
                }
            }
        }

        return claims;
    }

    private String buildToken(String subject, Map<String, Object> claims, long expirationMillis) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMillis);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    public String extractUsername(String token) {
        return getClaims(token).getSubject();
    }

    public Date extractExpiration(String token) {
        return getClaims(token).getExpiration();
    }

    public String extractRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    public Long extractUserId(String token) {
        return getClaims(token).get("userId", Long.class);
    }

    public String extractUserType(String token) {
        return getClaims(token).get("userType", String.class);
    }

    @SuppressWarnings("unchecked")
    public List<String> extractAuthorities(String token) {
        return getClaims(token).get("authorities", List.class);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            String username = extractUsername(token);
            return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        return getClaims(token).getExpiration().before(new Date());
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Date getAccessTokenExpiryDate() {
        return new Date(System.currentTimeMillis() + jwtProperties.getExpiration());
    }

    public Date getRefreshTokenExpiryDate() {
        return new Date(System.currentTimeMillis() + jwtProperties.getRefreshToken().getExpiration());
    }
}
//package lk.iit.nextora.config.security;
//
//import io.jsonwebtoken.Claims;
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.SignatureAlgorithm;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.stereotype.Service;
//
//import java.util.Date;
//
//@Service
//public class JwtTokenProvider {
//
//    private final JwtProperties jwtProperties;
//
//    public JwtTokenProvider(JwtProperties jwtProperties) {
//        this.jwtProperties = jwtProperties;
//    }
//
//    public String generateAccessToken(UserDetails userDetails) {
//        return buildToken(userDetails.getUsername(), jwtProperties.getExpiration());
//    }
//
//    public String generateRefreshToken(UserDetails userDetails) {
//        return buildToken(userDetails.getUsername(), jwtProperties.getRefreshToken().getExpiration());
//    }
//
//    private String buildToken(String subject, long expirationMillis) {
//        return Jwts.builder()
//                .setSubject(subject)
//                .setIssuedAt(new Date())
//                .setExpiration(new Date(System.currentTimeMillis() + expirationMillis))
//                .signWith(SignatureAlgorithm.HS256, jwtProperties.getSecretKey())
//                .compact();
//    }
//
//    public String extractUsername(String token) {
//        return getClaims(token).getSubject();
//    }
//
//    public Date extractExpiration(String token) {
//        return getClaims(token).getExpiration();
//    }
//
//    public boolean isTokenValid(String token, UserDetails userDetails) {
//        return extractUsername(token).equals(userDetails.getUsername()) && !isTokenExpired(token);
//    }
//
//    private boolean isTokenExpired(String token) {
//        return getClaims(token).getExpiration().before(new Date());
//    }
//
//    private Claims getClaims(String token) {
//        return Jwts.parser()
//                .setSigningKey(jwtProperties.getSecretKey())
//                .parseClaimsJws(token)
//                .getBody();
//    }
//
//    public Date getAccessTokenExpiryDate() {
//        return new Date(System.currentTimeMillis() + accessTokenValidityMs);
//    }
//}

package lk.iit.nextora.config.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    public String generateAccessToken(UserDetails userDetails) {
        return buildToken(userDetails.getUsername(), jwtProperties.getExpiration());
    }

    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(userDetails.getUsername(), jwtProperties.getRefreshToken().getExpiration());
    }

    private String buildToken(String subject, long expirationMillis) {
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMillis))
                .signWith(SignatureAlgorithm.HS256, jwtProperties.getSecretKey())
                .compact();
    }

    public String extractUsername(String token) {
        return getClaims(token).getSubject();
    }

    public Date extractExpiration(String token) {
        return getClaims(token).getExpiration();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        return extractUsername(token).equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return getClaims(token).getExpiration().before(new Date());
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .setSigningKey(jwtProperties.getSecretKey())
                .parseClaimsJws(token)
                .getBody();
    }

    public Date getAccessTokenExpiryDate() {
        return new Date(System.currentTimeMillis() + jwtProperties.getExpiration());
    }

    public Date getRefreshTokenExpiryDate() {
        return new Date(System.currentTimeMillis() + jwtProperties.getRefreshToken().getExpiration());
    }
}
package lk.iit.nextora.config.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.iit.nextora.common.exception.ErrorResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;
    private final JwtBlacklistService jwtBlacklistService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                if (jwtBlacklistService.isBlacklisted(token)) {
                    log.warn("Blacklisted token used for request: {}", request.getRequestURI());
                    sendErrorResponse(response, request, HttpStatus.UNAUTHORIZED,
                            "Token has been invalidated. Please login again.");
                    return;
                }

                String username = jwtTokenProvider.extractUsername(token);
                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    if (jwtTokenProvider.isTokenValid(token, userDetails)) {
                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    } else {
                        log.warn("Invalid token for user: {}", username);
                        sendErrorResponse(response, request, HttpStatus.UNAUTHORIZED,
                                "Invalid or expired token. Please login again.");
                        return;
                    }
                }
            } catch (io.jsonwebtoken.ExpiredJwtException e) {
                log.warn("Expired token used: {}", e.getMessage());
                sendErrorResponse(response, request, HttpStatus.UNAUTHORIZED,
                        "Token has expired. Please login again.");
                return;
            } catch (io.jsonwebtoken.security.SignatureException e) {
                log.warn("Invalid token signature: {}", e.getMessage());
                sendErrorResponse(response, request, HttpStatus.UNAUTHORIZED,
                        "Invalid token signature.");
                return;
            } catch (io.jsonwebtoken.MalformedJwtException e) {
                log.warn("Malformed token: {}", e.getMessage());
                sendErrorResponse(response, request, HttpStatus.UNAUTHORIZED,
                        "Malformed token.");
                return;
            } catch (Exception e) {
                log.error("JWT authentication error: {}", e.getMessage());
                sendErrorResponse(response, request, HttpStatus.UNAUTHORIZED,
                        "Authentication failed: " + e.getMessage());
                return;
            }
        }
        chain.doFilter(request, response);
    }

    private void sendErrorResponse(HttpServletResponse response, HttpServletRequest request,
                                   HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .build();

        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
}

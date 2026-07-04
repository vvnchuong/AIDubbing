package com.tool.aidubbing.config;

import com.tool.aidubbing.dto.request.IntrospectRequest;
import com.tool.aidubbing.dto.response.IntrospectResponse;
import com.tool.aidubbing.service.AuthenticationService;
import com.nimbusds.jwt.SignedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final AuthenticationService authenticationService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            IntrospectResponse introspect = authenticationService.introspect(
                    IntrospectRequest.builder().token(token).build()
            );

            if (introspect.isValidated()) {
                try {
                    SignedJWT signedJWT = SignedJWT.parse(token);
                    Long userId = Long.valueOf(signedJWT.getJWTClaimsSet().getSubject());
                    String role = (String) signedJWT.getJWTClaimsSet().getClaim("scope");

                    var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));

                    var authToken = new UsernamePasswordAuthenticationToken(userId, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } catch (ParseException e) {
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
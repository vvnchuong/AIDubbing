package com.tool.aidubbing.config;

import com.tool.aidubbing.entity.User;
import com.tool.aidubbing.repository.UserRepository;
import com.tool.aidubbing.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final AuthenticationService authenticationService;
    private final UserRepository userRepository;

    @Value("${app.frontend-redirect-url}")
    private String frontendRedirectUrl; // vd: http://localhost:3000/auth/callback

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();

        Long userId = ((Number) oauth2User.getAttribute("internal_user_id")).longValue();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user vừa tạo"));

        String token = authenticationService.generateTokenForUser(user);

        String redirectUrl = frontendRedirectUrl + "?token=" + token;
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
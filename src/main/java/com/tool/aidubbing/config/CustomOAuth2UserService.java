package com.tool.aidubbing.config;

import com.tool.aidubbing.entity.User;
import com.tool.aidubbing.enums.UserRole;
import com.tool.aidubbing.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // super.loadUser() = Spring tự gọi sang Google lấy userinfo, verify xong hết rồi mới trả về đây
        OAuth2User oauth2User = super.loadUser(userRequest);

        String googleId = oauth2User.getAttribute("sub");
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");
        String avatarUrl = oauth2User.getAttribute("picture");

        if (googleId == null) {
            throw new OAuth2AuthenticationException("Không lấy được id từ Google");
        }

        User user = userRepository.findByGoogleId(googleId).orElseGet(() -> {
            log.info("Tạo user mới từ Google, email={}", email);
            User newUser = new User();
            newUser.setGoogleId(googleId);
            newUser.setEmail(email);
            newUser.setName(name);
            newUser.setAvatarUrl(avatarUrl);
            newUser.setRole(UserRole.USER);
            return userRepository.save(newUser);
        });

        // Google đổi tên/avatar thì cập nhật lại cho khớp
        boolean changed = false;
        if (email != null && !email.equals(user.getEmail())) { user.setEmail(email); changed = true; }
        if (name != null && !name.equals(user.getName())) { user.setName(name); changed = true; }
        if (avatarUrl != null && !avatarUrl.equals(user.getAvatarUrl())) { user.setAvatarUrl(avatarUrl); changed = true; }
        if (changed) userRepository.save(user);

        // attributes gốc từ Google là map bất biến -> tạo map mới, nhét thêm userId nội bộ
        // để bước sau (OAuth2LoginSuccessHandler) lấy ra dùng, khỏi phải query DB lại lần nữa
        Map<String, Object> attributes = new HashMap<>(oauth2User.getAttributes());
        attributes.put("internal_user_id", user.getId());

        return new DefaultOAuth2User(oauth2User.getAuthorities(), attributes, "sub");
    }
}
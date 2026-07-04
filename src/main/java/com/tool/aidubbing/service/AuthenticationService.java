package com.tool.aidubbing.service;

import com.tool.aidubbing.dto.request.IntrospectRequest;
import com.tool.aidubbing.dto.request.LogoutRequest;
import com.tool.aidubbing.dto.request.RefreshTokenRequest;
import com.tool.aidubbing.dto.response.AuthenticationResponse;
import com.tool.aidubbing.dto.response.IntrospectResponse;
import com.tool.aidubbing.entity.InvalidatedToken;
import com.tool.aidubbing.entity.User;
import com.tool.aidubbing.enums.ErrorCode;
import com.tool.aidubbing.exception.AppException;
import com.tool.aidubbing.repository.InvalidatedTokenRepository;
import com.tool.aidubbing.repository.UserRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

/**
 * KHÁC BẢN GỐC CHỖ NÀO:
 * - Bỏ hết method authenticate(email, password) vì không còn login bằng password nữa,
 *   toàn bộ việc "xác thực đúng người" đã do Google + Spring Security lo (xem CustomOAuth2UserService).
 * - Đổi generateToken() từ private -> public, đặt tên generateTokenForUser(), để
 *   OAuth2LoginSuccessHandler gọi được sau khi Google login xong.
 * - introspect(), logout(), refreshToken(), verifyToken() GIỮ NGUYÊN 100% so với bản gốc,
 *   vì JWT của hệ thống mình không liên quan gì tới việc user login bằng cách nào.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final InvalidatedTokenRepository invalidatedTokenRepository;
    private final UserRepository userRepository;

    @Value("${jwt.signerKey}")
    private String SIGNED_KEY;

    @Value("${jwt.validDuration}")
    private long VALID_DURATION;

    public IntrospectResponse introspect(IntrospectRequest request) {
        String token = request.getToken();
        boolean isValid = true;

        try {
            verifyToken(token);
        } catch (JOSEException | ParseException e) {
            isValid = false;
        }

        return IntrospectResponse.builder()
                .validated(isValid)
                .build();
    }

    /**
     * Gọi hàm này sau khi Google đã xác thực xong (từ OAuth2LoginSuccessHandler).
     * Không còn check password ở đây nữa - user truyền vào chắc chắn đã hợp lệ.
     */
    public String generateTokenForUser(User user) {
        return generateToken(user);
    }

    public void logout(LogoutRequest request) throws ParseException, JOSEException {
        try {
            SignedJWT signedJWT = verifyToken(request.getToken());

            String jit = signedJWT.getJWTClaimsSet().getJWTID();
            Instant expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime().toInstant();

            InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                    .id(jit)
                    .expiryTime(expiryTime)
                    .build();

            invalidatedTokenRepository.save(invalidatedToken);
        } catch (AppException e) {
            log.info("Token already expired.");
        }
    }

    public AuthenticationResponse refreshToken(RefreshTokenRequest request)
            throws ParseException, JOSEException {
        SignedJWT signedJWT = verifyToken(request.getToken());

        String jit = signedJWT.getJWTClaimsSet().getJWTID();
        Instant expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime().toInstant();

        InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                .id(jit)
                .expiryTime(expiryTime)
                .build();

        invalidatedTokenRepository.save(invalidatedToken);

        // subject của JWT giờ lưu userId thay vì email (Google user có thể không có email cố định
        // để tra cứu lại, googleId/userId mới là khóa đáng tin cậy)
        String userIdStr = signedJWT.getJWTClaimsSet().getSubject();

        User user = userRepository.findById(Long.valueOf(userIdStr))
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        String token = generateToken(user);

        return AuthenticationResponse.builder()
                .authenticated(true)
                .token(token)
                .build();
    }

    private SignedJWT verifyToken(String token) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(SIGNED_KEY.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        boolean verified = signedJWT.verify(verifier);

        if (!(verified && expiryTime.after(new Date())))
            throw new RuntimeException();

        if (invalidatedTokenRepository
                .existsById(signedJWT.getJWTClaimsSet().getJWTID()))
            throw new RuntimeException();

        return signedJWT;
    }

    private String generateToken(User user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(String.valueOf(user.getId())) // đổi subject sang userId thay vì email
                .issuer("aidubbing.com")
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now()
                        .plus(VALID_DURATION, ChronoUnit.SECONDS)
                        .toEpochMilli()))
                .jwtID(UUID.randomUUID().toString())
                .claim("email", user.getEmail())
                .claim("name", user.getName())
                .claim("scope", user.getRole().name())
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(SIGNED_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
    }

}
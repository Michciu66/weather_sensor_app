package com.chudzik.weather_sensors.service;

import com.chudzik.weather_sensors.dto.LoginInputDto;
import com.chudzik.weather_sensors.dto.LoginResponseDto;
import com.chudzik.weather_sensors.dto.PasswordChangeDto;
import com.chudzik.weather_sensors.exception.IncorrectLoginException;
import com.chudzik.weather_sensors.exception.IncorrectPasswordException;
import com.chudzik.weather_sensors.exception.InvalidTokenException;
import com.chudzik.weather_sensors.model.Admin;
import com.chudzik.weather_sensors.model.AuthToken;
import com.chudzik.weather_sensors.repository.AdminRepository;
import com.chudzik.weather_sensors.repository.AuthTokenRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

@Service
public class AuthService {
    private final AdminRepository adminRepo;
    private final PasswordEncoder passwordEncoder;
    private final AuthTokenRepository authTokenRepo;
    private final SecureRandom secureRandom = new SecureRandom();
    private static final long TOKEN_LIFETIME_SECONDS = 3600;

    public AuthService(AdminRepository adminRepo, PasswordEncoder passwordEncoder, AuthTokenRepository authTokenRepo) {
        this.adminRepo = adminRepo;
        this.passwordEncoder = passwordEncoder;
        this.authTokenRepo = authTokenRepo;
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    @Transactional
    public LoginResponseDto logIn(LoginInputDto input) {
        Optional<Admin> account = adminRepo.findByUsername(input.username());
        if (account.isEmpty()) {
            throw new IncorrectLoginException();
        }

        boolean correct = passwordEncoder.matches(input.password(), account.get().getPasswordHash());
        if (!correct) {
            throw new IncorrectLoginException();
        }

        String rawToken = generateToken();

        AuthToken token = new AuthToken(account.get(), HashService.sha256(rawToken), Instant.now().plusSeconds(TOKEN_LIFETIME_SECONDS));

        authTokenRepo.save(token);

        return new LoginResponseDto(rawToken, "Bearer", TOKEN_LIFETIME_SECONDS);

    }

    @Transactional
    public void logOut(String token) {
        AuthToken authToken = authTokenRepo.findByTokenHash(HashService.sha256(token)).orElseThrow(InvalidTokenException::new);
        authToken.setRevokedAt(Instant.now());
    }

    @Transactional
    public void changePassword(String token, PasswordChangeDto input) {
        AuthToken authToken = authTokenRepo.findByTokenHash(HashService.sha256(token)).orElseThrow(InvalidTokenException::new);

        String passwordHash = authToken.getAdmin().getPasswordHash();
        if (!passwordEncoder.matches(input.currentPassword(), passwordHash)) {
            throw new IncorrectPasswordException();
        }
        authToken.getAdmin().setPasswordHash(passwordEncoder.encode(input.newPassword()));
    }
}

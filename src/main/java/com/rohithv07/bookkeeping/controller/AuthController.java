package com.rohithv07.bookkeeping.controller;

import com.rohithv07.bookkeeping.dto.LoginRequest;
import com.rohithv07.bookkeeping.dto.SignupRequest;
import com.rohithv07.bookkeeping.model.AppUser;
import com.rohithv07.bookkeeping.repository.AppUserRepository;
import com.rohithv07.bookkeeping.security.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Refill;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtUtil jwtUtil;
    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Map<String, Bucket> loginBuckets = new ConcurrentHashMap<>();

    @Value("${app.security.jwt.expiration}")
    private long jwtExpiration;

    public AuthController(JwtUtil jwtUtil, AppUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/signup")
    public ResponseEntity<String> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"message\": \"Error: Username is already taken!\"}");
        }

        // Create new user's account
        AppUser user = AppUser.builder()
                .username(signUpRequest.getUsername())
                .password(passwordEncoder.encode(signUpRequest.getPassword()))
                .build();

        userRepository.save(user);

        return ResponseEntity.ok("{\"message\": \"User registered successfully!\"}");
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody LoginRequest loginRequest) {
        String inputUsername = loginRequest.getUsername();
        Bucket bucket = loginBuckets.computeIfAbsent(inputUsername, k -> Bucket.builder()
                .addLimit(Bandwidth.classic(5, Refill.greedy(5, Duration.ofMinutes(1))))
                .build());

        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("{\"message\": \"Too many login attempts. Please try again later.\"}");
        }

        Optional<AppUser> userOptional = userRepository.findByUsername(inputUsername);

        if (userOptional.isPresent()
                && passwordEncoder.matches(loginRequest.getPassword(), userOptional.get().getPassword())) {
            String token = jwtUtil.generateToken(userOptional.get().getUsername());

            ResponseCookie cookie = ResponseCookie.from("jwt", token)
                    .httpOnly(true)
                    .secure(true) // Typically true in production for HTTPS
                    .path("/")
                    .maxAge(Duration.ofMillis(jwtExpiration))
                    .sameSite("None") // "None" if cross-domain in prod, but needs Secure=true
                    .build();

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body("{\"message\": \"Login successful\"}");
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"message\": \"Invalid credentials\"}");
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        ResponseCookie cookie = ResponseCookie.from("jwt", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("None")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body("{\"message\": \"Logout successful\"}");
    }
}

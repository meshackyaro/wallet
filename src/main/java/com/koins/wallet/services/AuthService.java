package com.koins.wallet.services;

import com.koins.wallet.dto.*;
import com.koins.wallet.entities.Otp;
import com.koins.wallet.entities.User;
import com.koins.wallet.enums.AccountStatus;
import com.koins.wallet.enums.Role;
import com.koins.wallet.repositories.OtpRepository;
import com.koins.wallet.repositories.UserRepository;
import com.koins.wallet.security.JwtService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final OtpRepository otpRepository;
    private final EmailService emailService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public AuthResponse signup(SignupRequest request) {
        if (userRepository.existsByEmailAddress(request.getEmailAddress())) {
            throw new IllegalArgumentException("Email already exists");
        }
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new IllegalArgumentException("Phone number already exists");
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .emailAddress(request.getEmailAddress())
                .phoneNumber(request.getPhoneNumber())
                .password(passwordEncoder.encode(request.getPassword()))
                .bvnNin(request.getBvnNin())
                .accountStatus(AccountStatus.ACTIVE)
                .role(Role.USER)
                .build();

        userRepository.save(user);

        // Publish event to auto-create wallet
        eventPublisher.publishEvent(user);

        String jwtToken = jwtService.generateToken(user);
        return AuthResponse.builder()
                .token(jwtToken)
                .message("User registered successfully. Wallet has been created.")
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmailAddress(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (user.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new IllegalArgumentException("Account is " + user.getAccountStatus() + ". Please contact support.");
        }

        String jwtToken = jwtService.generateToken(user);
        return AuthResponse.builder()
                .token(jwtToken)
                .message("Login successful")
                .build();
    }

    @Transactional
    public String forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmailAddress(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        generateOtp(user);

        return "OTP has been sent to your email";
    }

    private void generateOtp(User user) {
        otpRepository.deleteByEmail(user.getEmailAddress());

        String otpCode = String.format("%06d", new Random().nextInt(999999));
        Otp otp = Otp.builder()
                .email(user.getEmailAddress())
                .otpCode(otpCode)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .build();

        otpRepository.save(otp);
        emailService.sendOtpEmail(user.getEmailAddress(), otpCode);
    }

    @Transactional
    public String resetPassword(ResetPasswordRequest request) {
        Otp otp = otpRepository.findTopByEmailOrderByExpiresAtDesc(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired OTP"));

        if (otp.isExpired() || !otp.getOtpCode().equals(request.getOtp())) {
            throw new IllegalArgumentException("Invalid or expired OTP");
        }

        User user = userRepository.findByEmailAddress(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        otpRepository.deleteByEmail(request.getEmail());

        return "Password has been reset successfully";
    }

    @Transactional
    public String resendOtp(String email) {
        User user = userRepository.findByEmailAddress(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        generateOtp(user);

        return "OTP has been resent to your email";
    }

    @Transactional
    public String updateProfile(String email, UpdateProfileRequest request) {
        User user = userRepository.findByEmailAddress(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName());
        }
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()) {
            // Check if phone is already taken by another user
            if (!request.getPhoneNumber().equals(user.getPhoneNumber()) && userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
                throw new IllegalArgumentException("Phone number already exists");
            }
            user.setPhoneNumber(request.getPhoneNumber());
        }

        userRepository.save(user);
        return "Profile updated successfully";
    }

    public String logout(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            jwtService.blacklistToken(token.substring(7));
        }
        return "Logged out successfully";
    }
}

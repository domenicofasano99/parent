package com.bok.parent.controller;

import com.bok.parent.config.TotpManager;
import com.bok.parent.dto.*;
import com.bok.parent.exception.BadRequestException;
import com.bok.parent.exception.EmailAlreadyExistsException;
import com.bok.parent.exception.UsernameAlreadyExistsException;
import com.bok.parent.model.User;
import com.bok.parent.service.UserService;
import com.bok.parent.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@Slf4j
public class AuthController {

    @Autowired
    UserService userService;

    @Autowired
    TotpManager totpManager;

    @PostMapping("/signin")
    public JwtAuthenticationResponse authenticateUser(@RequestBody LoginRequest loginRequest) {
        log.debug("user {} attempting login", loginRequest.username);
        String token = userService.loginUser(loginRequest.username, loginRequest.password);
        return new JwtAuthenticationResponse(token, ObjectUtils.isEmpty(token));
    }

    @PostMapping("/verify")
    public JwtAuthenticationResponse verifyCode(@RequestBody VerifyCodeRequest verifyCodeRequest) {
        String token = userService.verify(verifyCodeRequest.username, verifyCodeRequest.code);
        return new JwtAuthenticationResponse(token, ObjectUtils.isEmpty(token));
    }

    @PostMapping(value = "/register", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createUser(@RequestBody SignUpRequest payload) {
        log.info("creating user {}", payload.username);

        User user = User
                .builder()
                .username(payload.username)
                .email(payload.email)
                .password(payload.password)
                .mfa(payload.mfa)
                .build();

        User saved;
        try {
            saved = userService.registerUser(user, Constants.USER);
        } catch (UsernameAlreadyExistsException | EmailAlreadyExistsException e) {
            throw new BadRequestException(e.getMessage());
        }

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath().path("/users/{username}")
                .buildAndExpand(user.getUsername()).toUri();

        return ResponseEntity
                .created(location)
                .body(new SignupResponse(saved.isMfa(),
                        totpManager.getUriForImage(saved.getSecret())));
    }
}

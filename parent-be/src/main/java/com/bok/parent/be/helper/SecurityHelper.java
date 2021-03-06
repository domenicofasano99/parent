package com.bok.parent.be.helper;

import com.bok.parent.be.service.TokenService;
import com.bok.parent.integration.dto.AccountLoginDTO;
import com.bok.parent.integration.dto.KeepAliveResponseDTO;
import com.bok.parent.integration.dto.LastAccessInfoDTO;
import com.bok.parent.integration.dto.LoginResponseDTO;
import com.bok.parent.integration.dto.LogoutResponseDTO;
import com.bok.parent.integration.dto.PasswordChangeRequestDTO;
import com.bok.parent.integration.dto.PasswordChangeResponseDTO;
import com.bok.parent.integration.dto.TokenInfoResponseDTO;
import com.bok.parent.model.AccessInfo;
import com.bok.parent.model.Account;
import com.bok.parent.model.Token;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static java.util.Objects.nonNull;

@Component
@Slf4j
public class SecurityHelper {

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    AuthenticationHelper authenticationHelper;

    @Autowired
    TokenService tokenService;

    @Autowired
    AuditHelper auditHelper;

    @Autowired
    AccountHelper accountHelper;

    @Autowired
    AccessInfoHelper accessInfoHelper;

    public LoginResponseDTO login(AccountLoginDTO request) {
        Account account = accountHelper.findByEmail(request.email);
        if (account.isDeleted()) {
            throw new RuntimeException("This account has been deleted");
        }
        return authenticationHelper.login(account, request.password);
    }

    public Long getAccountId(String token) {
        return authenticationHelper.extractAccountIdFromToken(token);
    }

    public TokenInfoResponseDTO getTokenInfo(String token) {
        return tokenService.getTokenInfo(token);
    }

    public KeepAliveResponseDTO keepAlive(String tokenString) {
        KeepAliveResponseDTO keepAliveResponse = new KeepAliveResponseDTO();
        Token token = tokenService.findByTokenString(tokenString);
        if (token.isExpiringSoon()) {
            token = tokenService.replaceOldToken(token);
        }
        keepAliveResponse.token = token.getTokenString();
        return keepAliveResponse;
    }

    public LogoutResponseDTO logout(String token) {
        tokenService.revoke(token);
        return new LogoutResponseDTO(true);
    }

    public LastAccessInfoDTO lastAccessInfo(String tokenString) {
        Token token = tokenService.findByTokenString(tokenString);
        AccessInfo accessInfo = auditHelper.findLastAccessInfo(token.getAccount().getId());
        LastAccessInfoDTO lastAccessInfo = new LastAccessInfoDTO();
        if (nonNull(accessInfo)) {
            lastAccessInfo.lastAccessDateTime = LocalDateTime.ofInstant(accessInfo.getTimestamp(), ZoneOffset.UTC);
            lastAccessInfo.lastAccessIP = accessInfo.getIpAddress();
        } else {
            lastAccessInfo.lastAccessDateTime = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC);
            lastAccessInfo.lastAccessIP = "";
        }
        return lastAccessInfo;
    }


    public PasswordChangeResponseDTO changePassword(String tokenString, PasswordChangeRequestDTO passwordChangeRequestDTO) {
        Token token = tokenService.findByTokenString(tokenString);
        Account account = token.getAccount();
        log.info("user {} is changing his password", account.getCredentials().getEmail());
        String newEncryptedPassword = passwordEncoder.encode(passwordChangeRequestDTO.newPassword);
        boolean changed = accountHelper.setNewPassword(account, newEncryptedPassword);
        return new PasswordChangeResponseDTO(changed);
    }

    public Boolean checkForPasswordResetNeeded(Long accountId) {
        return accountHelper.findById(accountId).isPasswordResetNeeded();
    }
}

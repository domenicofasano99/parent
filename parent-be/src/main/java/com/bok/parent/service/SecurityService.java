package com.bok.parent.service;

import com.bok.parent.integration.dto.AccountLoginDTO;
import com.bok.parent.integration.dto.KeepAliveResponseDTO;
import com.bok.parent.integration.dto.LastAccessInfoDTO;
import com.bok.parent.integration.dto.LoginResponseDTO;
import com.bok.parent.integration.dto.LogoutResponseDTO;
import com.bok.parent.integration.dto.PasswordChangeRequestDTO;
import com.bok.parent.integration.dto.PasswordChangeResponseDTO;
import com.bok.parent.integration.dto.TokenInfoResponseDTO;
import org.springframework.stereotype.Service;

@Service
public interface SecurityService {
    LoginResponseDTO login(AccountLoginDTO accountLoginDTO);

    Long getAccountId(String token);

    TokenInfoResponseDTO tokenInfo(String token);

    KeepAliveResponseDTO keepAlive(String token);

    LogoutResponseDTO logout(String token);

    LastAccessInfoDTO lastAccessInfo(String token);

    PasswordChangeResponseDTO changePassword(String token, PasswordChangeRequestDTO passwordChangeRequestDTO);

    void checkTokenValidity(String token);
}

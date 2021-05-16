package com.bok.parent.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.bok.parent.exception.TokenAuthenticationException;
import com.bok.parent.helper.AccountHelper;
import com.bok.parent.helper.TokenHelper;
import com.bok.parent.model.Account;
import com.bok.parent.model.Token;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;

import static com.bok.parent.utils.Constants.ACCOUNT_ID;
import static com.bok.parent.utils.Constants.EMAIL;
import static com.bok.parent.utils.Constants.EXPIRATION_INSTANT;

@Service
public class JWTService {

    private final Algorithm algorithm;
    private final int defaultExpiration;

    @Autowired
    CryptoUtils cryptoUtils;

    @Autowired
    TokenHelper tokenHelper;

    @Autowired
    AccountHelper accountHelper;


    public JWTService(
            @Value("${jwt.security.secret}") String secret,
            @Value("${jwt.security.expiration}") int defaultExpirationSeconds) {
        this.algorithm = Algorithm.HMAC256(secret);
        this.defaultExpiration = defaultExpirationSeconds;
    }

    public Token create(Account account) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plusSeconds(defaultExpiration);

        String tokenString;
        tokenString = cryptoUtils.encryptToken(JWT.create()
                .withIssuedAt(Date.from(issuedAt))
                .withExpiresAt(Date.from(expiresAt))
                .withClaim(EMAIL, account.getCredentials().getEmail())
                .withClaim(EXPIRATION_INSTANT, issuedAt.toString())
                .withClaim(ACCOUNT_ID, account.getId())
                .withIssuer("BOK")
                .sign(algorithm));
        return new Token(tokenString, issuedAt, expiresAt, "BOK", account, false);
    }

    public Token verify(String token) {

        JWTVerifier verifier = JWT.require(algorithm).build();
        try {
            token = cryptoUtils.decryptToken(token);
            DecodedJWT jwt = verifier.verify(token);

            Token tokenInfo = new Token();
            tokenInfo.issuedAt = jwt.getIssuedAt().toInstant();
            tokenInfo.expiresAt = jwt.getExpiresAt().toInstant();
            tokenInfo.expired = jwt.getExpiresAt().toInstant().isBefore(Instant.now());
            tokenInfo.issuer = jwt.getIssuer();
            tokenInfo.account = accountHelper.findById(jwt.getClaim(ACCOUNT_ID).asLong());

            return tokenInfo;
        } catch (Exception e) {
            throw new TokenAuthenticationException("TOKEN_VERIFICATION_EXCEPTION");
        }
    }
}

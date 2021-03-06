package com.mmadu.identity.providers.token.creationstrategies;

import com.mmadu.identity.entities.AuthorizationCodeGrantData;
import com.mmadu.identity.entities.DomainIdentityConfiguration;
import com.mmadu.identity.entities.GrantAuthorization;
import com.mmadu.identity.entities.Token;
import com.mmadu.identity.exceptions.TokenException;
import com.mmadu.identity.models.client.MmaduClient;
import com.mmadu.identity.models.token.TokenRequest;
import com.mmadu.identity.models.token.TokenResponse;
import com.mmadu.identity.models.token.TokenSpecification;
import com.mmadu.identity.models.token.error.InvalidClient;
import com.mmadu.identity.models.token.error.InvalidRequest;
import com.mmadu.identity.providers.token.TokenFactory;
import com.mmadu.identity.repositories.GrantAuthorizationRepository;
import com.mmadu.identity.services.domain.DomainIdentityConfigurationService;
import com.mmadu.identity.utils.GrantTypeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.ZonedDateTime;
import java.util.List;

import static com.mmadu.identity.utils.ZoneDateTimeUtils.min;

@Component
public class AuthorizationCodeTokenCreationStrategy implements TokenCreationStrategy {
    private GrantAuthorizationRepository grantAuthorizationRepository;
    private TokenFactory tokenFactory;
    private DomainIdentityConfigurationService domainIdentityConfigurationService;

    @Autowired(required = false)
    public void setGrantAuthorizationRepository(GrantAuthorizationRepository grantAuthorizationRepository) {
        this.grantAuthorizationRepository = grantAuthorizationRepository;
    }

    @Autowired
    public void setTokenFactory(TokenFactory tokenFactory) {
        this.tokenFactory = tokenFactory;
    }

    @Autowired
    public void setDomainIdentityConfigurationService(DomainIdentityConfigurationService domainIdentityConfigurationService) {
        this.domainIdentityConfigurationService = domainIdentityConfigurationService;
    }

    @Override
    public boolean apply(TokenRequest request, MmaduClient client) {
        return GrantTypeUtils.AUTHORIZATION_CODE.equals(request.getGrant_type());
    }

    @Override
    public TokenResponse getToken(TokenRequest request, MmaduClient client) {
        GrantAuthorization authorization = grantAuthorizationRepository.
                findByClientIdentifierAndAuthorizationCode(client.getClientIdentifier(), request.getCode())
                .orElseThrow(AuthorizationCodeTokenCreationStrategy::invalidCodeError);

        if (authorization.isActive() || authorization.isExpired() || authorization.isRevoked()) {
            throw invalidRequest();
        }
        if (!(authorization.getData() instanceof AuthorizationCodeGrantData)) {
            throw invalidGrantData();
        }

        if (authorization.isRedirectUriSpecified() && StringUtils.isEmpty(request.getRedirect_uri())) {
            throw redirectUriIsRequired();
        }

        DomainIdentityConfiguration configuration = domainIdentityConfigurationService.findByDomainId(client.getDomainId())
                .orElseThrow(AuthorizationCodeTokenCreationStrategy::invalidClient);

        ZonedDateTime now = ZonedDateTime.now();
        Token accessToken = tokenFactory.createToken(
                TokenSpecification.builder()
                        .configuration(configuration.getAccessTokenProperties())
                        .domainId(client.getDomainId())
                        .grantAuthorization(authorization)
                        .activationTime(now)
                        .issueTime(now)
                        .expirationTime(
                                min(now.plusSeconds(client.getAccessTokenTTLSeconds()), authorization.getExpiryTime())
                        )
                        .labels(List.of("access_token"))
                        .provider(configuration.getAccessTokenProvider())
                        .type("access_token")
                        .category(client.getTokenCategory())
                        .active(true)
                        .build()
        );
        authorization.addAccessToken(accessToken);
        String refreshTokenString = "";

        if (configuration.isRefreshTokenEnabled() && client.issueRefreshTokens()) {
            Token refreshToken = tokenFactory.createToken(
                    TokenSpecification.builder()
                            .configuration(configuration.getRefreshTokenProperties())
                            .domainId(client.getDomainId())
                            .grantAuthorization(authorization)
                            .labels(List.of("refresh_token"))
                            .provider(configuration.getRefreshTokenProvider())
                            .activationTime(now)
                            .issueTime(now)
                            .expirationTime(
                                    min(now.plusSeconds(client.getRefreshTokenTTLSeconds()), authorization.getExpiryTime())
                            )
                            .type("refresh_token")
                            .category(client.getTokenCategory())
                            .active(true)
                            .build()
            );
            refreshTokenString = refreshToken.getCredentials().getTokenString();
            authorization.addRefreshToken(refreshToken);
            authorization.setRefreshTokenIssued(true);
        }
        authorization.setActive(true);
        grantAuthorizationRepository.save(authorization);
        return TokenResponse.builder()
                .accessToken(accessToken.getCredentials().getTokenString())
                .expiresIn(authorization.getExpiryTime())
                .refreshToken(refreshTokenString)
                .tokenIdentifier(accessToken.getTokenIdentifier())
                .tokenType(accessToken.getCategory())
                .expiresIn(accessToken.getExpiryTime())
                .build();
    }

    private static TokenException invalidCodeError() {
        return new TokenException(new InvalidRequest("code.invalid", ""));
    }

    private static TokenException redirectUriIsRequired() {
        return new TokenException(new InvalidRequest("redirect_uri.required", ""));
    }

    private static TokenException invalidGrantData() {
        return new TokenException(new InvalidRequest("grant_data.invalid", ""));
    }

    private static TokenException invalidClient() {
        return new TokenException(new InvalidClient("client.domain.invalid", ""));
    }

    private static TokenException invalidRequest() {
        return new TokenException(new InvalidRequest("request.invalid", ""));
    }
}

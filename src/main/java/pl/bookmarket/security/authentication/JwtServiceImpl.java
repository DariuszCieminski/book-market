package pl.bookmarket.security.authentication;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Clock;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.DefaultClock;
import io.jsonwebtoken.jackson.io.JacksonDeserializer;
import io.jsonwebtoken.lang.Maps;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.KeyPair;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Component
public class JwtServiceImpl implements JwtService {

    private static final String ISSUER = "Book Market";
    private static final String CLAIM_ID = "id";
    private static final String CLAIM_ROLES = "roles";
    private static final String CLAIM_ISREFRESH = "isRefresh";

    private final Long ACCESS_TOKEN_DURATION;
    private final Long REFRESH_TOKEN_DURATION;
    private final KeyPair keys;
    private final JwtParser parser;

    @Autowired
    public JwtServiceImpl(@Value("${bm.jwt.access-token-duration}") Long accessTokenDuration,
                          @Value("${bm.jwt.refresh-token-duration}") Long refreshTokenDuration) {
        this(accessTokenDuration, refreshTokenDuration, new DefaultClock());
    }

    public JwtServiceImpl(Long accessTokenDuration, Long refreshTokenDuration, Clock clock) {
        this.ACCESS_TOKEN_DURATION = accessTokenDuration;
        this.REFRESH_TOKEN_DURATION = refreshTokenDuration;
        this.keys = Keys.keyPairFor(SignatureAlgorithm.ES256);
        this.parser = Jwts.parserBuilder()
                          .deserializeJsonWith(new JacksonDeserializer(Maps.of(CLAIM_ROLES, String[].class).build()))
                          .setSigningKey(keys.getPublic())
                          .setClock(clock)
                          .build();
    }

    @Override
    public String generateAccessToken(Authentication authentication) {
        checkAuthenticationType(authentication);
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        Date expirationDate = new Date(Instant.now().plus(Duration.ofMinutes(ACCESS_TOKEN_DURATION)).toEpochMilli());
        String[] userRoles = user.getAuthorities().stream().map(this::userAuthorityMapper).toArray(String[]::new);

        return Jwts.builder()
                   .setIssuer(ISSUER)
                   .setExpiration(expirationDate)
                   .claim(CLAIM_ID, user.getId())
                   .claim(CLAIM_ROLES, userRoles)
                   .claim(CLAIM_ISREFRESH, false)
                   .signWith(keys.getPrivate())
                   .compact();
    }

    @Override
    public String generateRefreshToken(Authentication authentication) {
        checkAuthenticationType(authentication);
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        Date expirationDate = new Date(Instant.now().plus(Duration.ofMinutes(REFRESH_TOKEN_DURATION)).toEpochMilli());

        return Jwts.builder()
                   .setIssuer(ISSUER)
                   .setExpiration(expirationDate)
                   .claim(CLAIM_ID, user.getId())
                   .claim(CLAIM_ISREFRESH, true)
                   .signWith(keys.getPrivate())
                   .compact();
    }

    @Override
    public boolean validateToken(String token) {
        try {
            parser.parse(token);
            if (!ISSUER.equals(getClaim(token, Claims.ISSUER, String.class))) {
                throw new JwtException(null);
            }
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public Map<String, Object> getClaims(String token) throws BearerTokenException {
        try {
            return parser.parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        } catch (JwtException | IllegalArgumentException e) {
            throw new BearerTokenException(e.getMessage());
        }
    }

    @Override
    public <T> T getClaim(String token, String claim, Class<T> claimType) throws BearerTokenException {
        try {
            return parser.parseClaimsJws(token).getBody().get(claim, claimType);
        } catch (ExpiredJwtException e) {
            return e.getClaims().get(claim, claimType);
        } catch (JwtException | IllegalArgumentException e) {
            throw new BearerTokenException(e.getMessage());
        }
    }

    @Override
    public Authentication buildAuthentication(String token) throws BearerTokenException {
        if (getClaim(token, CLAIM_ISREFRESH, Boolean.class)) {
            throw new BearerTokenException("Authentication object cannot be built from refresh token!");
        }

        Long userId = getClaim(token, CLAIM_ID, Long.class);
        String[] roles = getClaim(token, CLAIM_ROLES, String[].class);
        if (userId == null || roles == null) {
            throw new BearerTokenException("Required claims are missing from token!");
        }
        AuthenticatedUser principal = AuthenticatedUser.builder()
                                                       .username(userId.toString())
                                                       .password("[PROTECTED]")
                                                       .id(userId)
                                                       .authorities(roles)
                                                       .build();

        return new UsernamePasswordAuthenticationToken(principal, principal.getPassword(), principal.getAuthorities());
    }

    private void checkAuthenticationType(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser)) {
            throw new IllegalArgumentException("Authentication principal must be of type " + AuthenticatedUser.class.getSimpleName());
        }
    }

    private String userAuthorityMapper(GrantedAuthority grantedAuthority) {
        String authority = grantedAuthority.getAuthority();
        if (authority.startsWith("ROLE_")) {
            return authority.substring(5);
        }
        return authority;
    }
}
package pl.bookmarket.security.authentication;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.PrivateKey;
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
    private final PrivateKey secretKey;
    private final JwtParser parser;

    public JwtServiceImpl(@Value("${bm.jwt.access-token-duration}") Long accessTokenDuration,
                          @Value("${bm.jwt.refresh-token-duration}") Long refreshTokenDuration) {
        this.ACCESS_TOKEN_DURATION = accessTokenDuration;
        this.REFRESH_TOKEN_DURATION = refreshTokenDuration;
        this.secretKey = Keys.keyPairFor(SignatureAlgorithm.ES256).getPrivate();
        this.parser = Jwts.parserBuilder().setSigningKey(secretKey).build();
    }

    @Override
    public String generateAccessToken(Authentication authentication) {
        checkAuthenticationType(authentication);
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        Date expirationDate = new Date(Instant.now().plus(Duration.ofMinutes(ACCESS_TOKEN_DURATION)).toEpochMilli());
        String[] userRoles = user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toArray(String[]::new);

        return Jwts.builder()
                   .setIssuer(ISSUER)
                   .setExpiration(expirationDate)
                   .claim(CLAIM_ID, user.getId())
                   .claim(CLAIM_ROLES, userRoles)
                   .claim(CLAIM_ISREFRESH, false)
                   .signWith(secretKey)
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
                   .signWith(secretKey)
                   .compact();
    }

    @Override
    public boolean validateToken(String token) {
        try {
            parser.parse(token);
            if (!ISSUER.equals(getClaim(token, ISSUER, String.class))) {
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
        AuthenticatedUser principal = AuthenticatedUser.builder().id(userId).authorities(roles).build();

        return new UsernamePasswordAuthenticationToken(principal, principal.getPassword(), principal.getAuthorities());
    }

    private void checkAuthenticationType(Authentication authentication) {
        if (!(authentication.getPrincipal() instanceof AuthenticatedUser)) {
            throw new IllegalArgumentException("Authentication principal must be of type " + AuthenticatedUser.class.getSimpleName());
        }
    }
}
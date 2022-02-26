package pl.bookmarket.security.authentication;

import org.springframework.security.core.Authentication;

import java.util.Map;

public interface JwtService {

    String generateAccessToken(Authentication authentication);

    String generateRefreshToken(Authentication authentication);

    boolean validateToken(String token);

    Map<String, Object> getClaims(String token) throws BearerTokenException;

    <T> T getClaim(String token, String claim, Class<T> claimType) throws BearerTokenException;

    Authentication buildAuthentication(String token) throws BearerTokenException;
}
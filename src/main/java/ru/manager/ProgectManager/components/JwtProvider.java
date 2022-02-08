package ru.manager.ProgectManager.components;

import io.jsonwebtoken.*;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.manager.ProgectManager.enums.TokenStatus;
import ru.manager.ProgectManager.exception.ExpiredTokenException;
import ru.manager.ProgectManager.exception.InvalidTokenException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;

@Component
@Log
public class JwtProvider {
    private HttpServletRequest request;
    @Autowired
    private void setRequest(HttpServletRequest httpServletRequest){
        request = httpServletRequest;
    }

    @Value("${jwt.secret}")
    private String jwtSecret;

    public String generateToken(String login) {
        Date date = Date.from(LocalDateTime.now().plusMinutes(15).atZone(ZoneId.systemDefault()).toInstant());
        return Jwts.builder()
                .setSubject(login)
                .setExpiration(date)
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    public TokenStatus validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
            return TokenStatus.OK;
        } catch (ExpiredJwtException expEx) {
            log.severe("Token expired");
            return TokenStatus.EXPIRED;
        } catch (UnsupportedJwtException unsEx) {
            log.severe("Unsupported jwt");
            return TokenStatus.INVALID;
        } catch (MalformedJwtException mjEx) {
            log.severe("Malformed jwt");
            return TokenStatus.INVALID;
        } catch (SignatureException sEx) {
            log.severe("Invalid signature");
            return TokenStatus.INVALID;
        } catch (Exception e) {
            log.severe("invalid token");
            return TokenStatus.INVALID;
        }
    }

    public String getLoginFromToken(String token) {
        Claims claims = Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody();
        return claims.getSubject();
    }

    public String getLoginFromToken(){
        String accessToken = getBearerTokenHeader();
        TokenStatus status = validateToken(accessToken);
        if(status == TokenStatus.OK) {
            Claims claims = Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(accessToken).getBody();
            return claims.getSubject();
        } else if (status == TokenStatus.INVALID){
            throw new InvalidTokenException();
        } else {
            throw new ExpiredTokenException();
        }
    }

    private String getBearerTokenHeader() {
        Cookie[] cookies = request.getCookies();
        if(cookies != null) {
            return Arrays.stream(cookies)
                    .filter(c -> c.getName().equals("access"))
                    .findAny()
                    .map(Cookie::getValue)
                    .orElse(null);
        } else{
            return null;
        }
    }
}
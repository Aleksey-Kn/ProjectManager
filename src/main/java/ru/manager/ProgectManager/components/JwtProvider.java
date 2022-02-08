package ru.manager.ProgectManager.components;

import io.jsonwebtoken.*;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
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

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException expEx) {
            log.severe("Token expired");
        } catch (UnsupportedJwtException unsEx) {
            log.severe("Unsupported jwt");
        } catch (MalformedJwtException mjEx) {
            log.severe("Malformed jwt");
        } catch (SignatureException sEx) {
            log.severe("Invalid signature");
        } catch (Exception e) {
            log.severe("invalid token");
        }
        return false;
    }

    public String getLoginFromToken(String token) {
        Claims claims = Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody();
        return claims.getSubject();
    }

    public String getLoginFromToken(){
        String accessToken = getBearerTokenHeader();
        if(validateToken(accessToken)) {
            Claims claims = Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(accessToken).getBody();
            return claims.getSubject();
        } else{
            throw new InvalidTokenException(accessToken);
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
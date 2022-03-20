package ru.manager.ProgectManager.components;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.manager.ProgectManager.exception.ExpiredTokenException;

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
    private void setRequest(HttpServletRequest httpServletRequest) {
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
        } catch (Exception e) {
            log.severe("invalid token");
            return false;
        }
    }

    public String getLoginFromToken(String token) {
        Claims claims = Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody();
        return claims.getSubject();
    }

    public String getLoginFromToken() {
        String accessToken = getBearerToken();
        boolean status = validateToken(accessToken);
        if (status) {
            Claims claims = Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(accessToken).getBody();
            return claims.getSubject();
        } else {
            throw new ExpiredTokenException();
        }
    }

    private String getBearerToken() {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            return Arrays.stream(cookies)
                    .filter(c -> c.getName().equals("access"))
                    .findAny()
                    .map(Cookie::getValue)
                    .orElse(null);
        } else {
            return null;
        }
    }
}
package ru.manager.ProgectManager.components.authorization;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import ru.manager.ProgectManager.exception.ExpiredTokenException;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

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
        Optional<String> accessToken = getBearerToken();
        boolean status = accessToken.isPresent() && validateToken(accessToken.get());
        if (status) {
            Claims claims = Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(accessToken.get()).getBody();
            return claims.getSubject();
        } else {
            throw new ExpiredTokenException();
        }
    }

    private Optional<String> getBearerToken() {
        String bearer = request.getHeader("Authorization");
        if(StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return Optional.of(bearer.substring(7));
        } else {
            return Optional.empty();
        }
    }
}
package ru.manager.ProgectManager.handler;

import com.google.gson.Gson;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import ru.manager.ProgectManager.DTO.response.ErrorResponse;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

public class CustomAuthenticationFailureHandler implements AuthenticationEntryPoint {
    private final Gson gson = new Gson();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
            throws IOException, ServletException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        if(exception.getCause() instanceof ExpiredJwtException){
            response.getOutputStream()
                    .println(gson.toJson(new ErrorResponse(Collections.singletonList("Token: expired token"))));
        } else {
            response.getOutputStream()
                    .println(gson.toJson(new ErrorResponse(Collections.singletonList("Token: invalid token"))));
        }
    }
}

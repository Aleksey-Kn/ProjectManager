package ru.manager.ProgectManager.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.manager.ProgectManager.entitys.RefreshToken;
import ru.manager.ProgectManager.repositories.RefreshTokenRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;

    public String createToken(String login){
        refreshTokenRepository.findByLogin(login).ifPresent(refreshTokenRepository::delete);
        String token = UUID.randomUUID().toString();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(token);
        refreshToken.setLogin(login);
        refreshToken.setTimeToDie(LocalDate.now().plusDays(15).toEpochDay());
        refreshTokenRepository.save(refreshToken);
        return token;
    }

    public Optional<String> findLoginFromToken(String token){
        Optional<RefreshToken> refreshToken = refreshTokenRepository.findById(token);
        if(refreshToken.isPresent()){
            if(LocalDate.ofEpochDay(refreshToken.get().getTimeToDie()).isAfter(LocalDate.now())) {
                return Optional.of(refreshToken.get().getLogin());
            } else{
                refreshTokenRepository.delete(refreshToken.get());
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
    }
}

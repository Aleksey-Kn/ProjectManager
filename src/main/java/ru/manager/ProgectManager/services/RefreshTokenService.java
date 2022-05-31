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

    public String createToken(String login) {
        String token = UUID.randomUUID().toString();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(token);
        refreshToken.setLogin(login);
        refreshToken.setTimeToDie(LocalDate.now().plusDays(15).toEpochDay());
        refreshTokenRepository.save(refreshToken);
        return token;
    }

    public Optional<String> findLogin(String token) {
        return refreshTokenRepository.findById(token)
                .filter(value -> LocalDate.ofEpochDay(value.getTimeToDie()).isAfter(LocalDate.now()))
                .map(RefreshToken::getLogin);
    }

    public Optional<String> findLoginAndDropToken(String token) {
        Optional<RefreshToken> refreshToken = refreshTokenRepository.findById(token);
        if (refreshToken.isPresent()) {
            refreshTokenRepository.delete(refreshToken.get());
            return (LocalDate.ofEpochDay(refreshToken.get().getTimeToDie()).isAfter(LocalDate.now())
                    ? Optional.of(refreshToken.get().getLogin())
                    : Optional.empty());
        } else {
            return Optional.empty();
        }
    }
}

package ru.manager.ProgectManager.repositories;

import org.springframework.data.repository.CrudRepository;
import ru.manager.ProgectManager.entitys.user.UsedAddress;

import java.util.Optional;

public interface UsedAddressRepository extends CrudRepository<UsedAddress, Long> {
    Optional<UsedAddress> findByIp(String ip);
}

package dev.folomkin.bankrest.demo_testcontiners.repository;

import dev.folomkin.bankrest.demo_testcontiners.entity.SimpleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SimpleRepository extends JpaRepository<SimpleEntity, Long> {
}
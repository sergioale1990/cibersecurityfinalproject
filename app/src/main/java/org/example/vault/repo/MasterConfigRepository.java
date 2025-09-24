package org.example.vault.repo;

import org.example.vault.model.MasterConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MasterConfigRepository extends JpaRepository<MasterConfig, Long> {}
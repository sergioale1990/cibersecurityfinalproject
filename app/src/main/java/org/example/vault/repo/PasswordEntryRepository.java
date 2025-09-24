package org.example.vault.repo;


import org.example.vault.model.PasswordEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PasswordEntryRepository extends JpaRepository<PasswordEntry, Long> {
    List<PasswordEntry> findByNameContainingIgnoreCase(String q);
}
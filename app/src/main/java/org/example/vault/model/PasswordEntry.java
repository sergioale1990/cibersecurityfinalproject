package org.example.vault.model;

import jakarta.persistence.*;
import java.time.Instant;

//Clase responsable de guardar la informacion de un password para un servicio
@Entity
public class PasswordEntry {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;        // Ej: "Gmail"
    private String username;    // Ej: "sergio@example.com"

    @Lob
    private String cipherText;  // Base64(AES-GCM)
    private String iv;          // Base64(12 bytes)

    private Instant createdAt;
    private Instant updatedAt;

    public PasswordEntry() {}
    public PasswordEntry(String name, String username, String cipherText, String iv) {
        this.name = name;
        this.username = username;
        this.cipherText = cipherText;
        this.iv = iv;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    public void onUpdate(){ this.updatedAt = Instant.now(); }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getUsername() { return username; }
    public String getCipherText() { return cipherText; }
    public String getIv() { return iv; }
    public Instant getUpdatedAt() { return updatedAt; }
}
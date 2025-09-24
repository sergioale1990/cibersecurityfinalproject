package org.example.vault.model;

import jakarta.persistence.*;

//Entidad de con la informacion de la configuracion del master password de la aplicacion
@Entity
public class MasterConfig {
    @Id
    private Long id = 1L;

    private String bCryptHash;  // hash del master password
    private String kdfSaltB64;  // salt para PBKDF2 (Base64)
    private int    kdfIterations;

    public MasterConfig() {}
    public MasterConfig(String bCryptHash, String kdfSaltB64, int kdfIterations) {
        this.bCryptHash = bCryptHash;
        this.kdfSaltB64 = kdfSaltB64;
        this.kdfIterations = kdfIterations;
    }

    public String getBCryptHash() { return bCryptHash; }
    public String getKdfSaltB64() { return kdfSaltB64; }
    public int    getKdfIterations() { return kdfIterations; }
}
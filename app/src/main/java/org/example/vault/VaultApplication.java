package org.example.vault;

import org.example.vault.services.CryptoService;
import org.example.vault.model.MasterConfig;
import org.example.vault.model.PasswordEntry;
import org.example.vault.repo.MasterConfigRepository;
import org.example.vault.repo.PasswordEntryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCrypt;

import java.io.Console;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

@SpringBootApplication
public class VaultApplication implements CommandLineRunner {

    private final PasswordEntryRepository entries;
    private final MasterConfigRepository configs;
    private final SecureRandom random = new SecureRandom();

    public VaultApplication(PasswordEntryRepository entries, MasterConfigRepository configs) {
        this.entries = entries;
        this.configs = configs;
    }

    public static void main(String[] args) {
        SpringApplication.run(VaultApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        Console console = System.console();
        Scanner sc = new Scanner(System.in);

        MasterConfig cfg = configs.findById(1L).orElse(null);
        if (cfg == null) {
            System.out.println("== Primer uso ==");
            char[] mpw = readPassword(console, sc, "Cree un master password: ");
            char[] mpw2 = readPassword(console, sc, "Repita el master password: ");
            if (!java.util.Arrays.equals(mpw, mpw2)) {
                System.err.println("No coinciden. Saliendo.");
                return;
            }
            byte[] salt = new byte[16];
            random.nextBytes(salt);
            int iterations = 120_000;

            String bHash = BCrypt.hashpw(new String(mpw), BCrypt.gensalt(12));
            cfg = new MasterConfig(bHash, Base64.getEncoder().encodeToString(salt), iterations);
            configs.save(cfg);
            wipe(mpw); wipe(mpw2);
            System.out.println("Master password inicializado.");
        }

        // Login
        char[] mpLogin = readPassword(console, sc, "Master password: ");
        if (!BCrypt.checkpw(new String(mpLogin), cfg.getBCryptHash())) {
            System.err.println("Master password incorrecto.");
            wipe(mpLogin);
            return;
        }
        CryptoService crypto = new CryptoService(
                mpLogin,
                Base64.getDecoder().decode(cfg.getKdfSaltB64()),
                cfg.getKdfIterations()
        );

        // Loop principal
        while (true) {
            System.out.println("""
                \n== Password Vault ==
                1) Crear
                2) Listar
                3) Ver detalle
                4) Eliminar
                5) Buscar
                0) Salir
                """);
            System.out.print("Opción: ");
            String opt = sc.nextLine().trim();

            switch (opt) {
                case "1" -> createEntry(sc, crypto);
                case "2" -> listEntries();
                case "3" -> viewEntry(sc, crypto);
                case "4" -> deleteEntry(sc);
                case "5" -> searchEntries(sc);
                case "0" -> { wipe(mpLogin); System.out.println("Adiós!"); return; }
                default -> System.out.println("Opción inválida.");
            }
        }
    }

    private void createEntry(Scanner sc, CryptoService crypto) {
        try {
            System.out.print("Nombre (ej: Gmail): ");
            String name = sc.nextLine().trim();
            System.out.print("Usuario: ");
            String user = sc.nextLine().trim();
            System.out.print("Contraseña: ");
            String pass = sc.nextLine();

            String[] enc = crypto.encrypt(pass);
            PasswordEntry e = new PasswordEntry(name, user, enc[0], enc[1]);
            entries.save(e);
            System.out.println("Guardado con id=" + e.getId());
        } catch (Exception ex) {
            System.err.println("Error al guardar: " + ex.getMessage());
        }
    }

    private void listEntries() {
        List<PasswordEntry> all = entries.findAll();
        if (all.isEmpty()) { System.out.println("(vacío)"); return; }
        System.out.printf("%-5s | %-25s | %-30s | %-20s%n", "ID","Nombre","Usuario","Actualizado");
        System.out.println("-----+---------------------------+--------------------------------+----------------------");
        all.forEach(e -> System.out.printf("%-5d | %-25s | %-30s | %-20s%n",
                e.getId(), e.getServiceName(), e.getUsername(), e.getUpdatedAt()));
    }

    private void viewEntry(Scanner sc, CryptoService crypto) {
        try {
            System.out.print("ID: ");
            long id = Long.parseLong(sc.nextLine());
            Optional<PasswordEntry> op = entries.findById(id);
            if (op.isEmpty()) { System.out.println("No existe."); return; }
            PasswordEntry e = op.get();
            String plain = crypto.decrypt(e.getCipherText(), e.getIv());
            System.out.println("Nombre:   " + e.getServiceName());
            System.out.println("Usuario:  " + e.getUsername());
            System.out.println("Password: " + plain);
        } catch (Exception ex) {
            System.err.println("Error: " + ex.getMessage());
        }
    }

    private void deleteEntry(Scanner sc) {
        System.out.print("ID a eliminar: ");
        try {
            long id = Long.parseLong(sc.nextLine());
            if (entries.existsById(id)) {
                entries.deleteById(id);
                System.out.println("Eliminado.");
            } else System.out.println("No existe.");
        } catch (NumberFormatException e) {
            System.out.println("ID inválido.");
        }
    }

    private void searchEntries(Scanner sc) {
        System.out.print("Buscar por nombre: ");
        String q = sc.nextLine();
        List<PasswordEntry> list = entries.findByNameContainingIgnoreCase(q);
        list.forEach(e -> System.out.printf("[%d] %s (%s)%n", e.getId(), e.getServiceName(), e.getUsername()));
        if (list.isEmpty()) System.out.println("(sin resultados)");
    }

    // Utilidades

    private static char[] readPassword(Console console, Scanner sc, String prompt) {
        if (console != null) return console.readPassword(prompt);
        System.out.print(prompt);
        return sc.nextLine().toCharArray();
    }
    private static void wipe(char[] a){ if (a!=null) java.util.Arrays.fill(a, '\0'); }
}
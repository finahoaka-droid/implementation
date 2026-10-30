package org.example.facebookregression.weka;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Charge la configuration depuis l'environnement puis depuis le fichier .env local. */
public final class PredictionConfig {

    private static final Map<String, String> DOTENV = chargerDotenv();

    private PredictionConfig() {
    }

    /**
     * Retourne la variable d'environnement si elle existe, sinon sa valeur .env.
     * L'environnement explicite reste prioritaire pour les déploiements automatisés.
     */
    static String get(String name) {
        String environment = System.getenv(name);
        if (environment != null && !environment.isBlank()) return environment;
        return DOTENV.get(name);
    }

    static String required(String name) {
        String value = get(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    name + " doit être défini dans l'environnement ou dans .env");
        }
        return value;
    }

    private static Map<String, String> chargerDotenv() {
        Map<String, String> values = new HashMap<>();
        Path path = Path.of(".env");
        if (!Files.isRegularFile(path)) return values;

        try {
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            for (String original : lines) {
                String line = original.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                if (line.startsWith("export ")) line = line.substring("export ".length()).trim();

                int separator = line.indexOf('=');
                if (separator <= 0) continue;
                String key = line.substring(0, separator).trim();
                String value = line.substring(separator + 1).trim();
                if (value.length() >= 2
                        && ((value.startsWith("\"") && value.endsWith("\""))
                        || (value.startsWith("'") && value.endsWith("'")))) {
                    value = value.substring(1, value.length() - 1);
                }
                values.put(key, value);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Impossible de lire .env : " + e.getMessage(), e);
        }
        return values;
    }
}


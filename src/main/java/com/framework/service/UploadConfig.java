package com.framework.service;

import jakarta.servlet.ServletContext;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class UploadConfig {

    public static final String LOCATION = "/WEB-INF/upload.properties";
    public static final String KEY_DIR = "upload.dir";

    private static final Pattern PLACEHOLDER = Pattern.compile("\\$\\{([^}]+)}");
    private static volatile Path directory;

    private UploadConfig() {
    }

    public static void load(ServletContext context) throws IOException {
        directory = null;
        try (InputStream in = context.getResourceAsStream(LOCATION)) {
            if (in == null) {
                context.log("[UploadConfig] " + LOCATION + " introuvable : upload non configuré");
                return;
            }
            Properties props = new Properties();
            props.load(in);

            String value = props.getProperty(KEY_DIR);
            if (value == null || value.isBlank()) {
                context.log("[UploadConfig] clé " + KEY_DIR + " absente ou vide dans " + LOCATION);
                return;
            }
            directory = Path.of(resolvePlaceholders(value.trim())).toAbsolutePath().normalize();
            context.log("[UploadConfig] répertoire d upload : " + directory);
        }
    }

    public static Path getDirectory() {
        Path dir = directory;
        if (dir == null) {
            throw new IllegalStateException("Répertoire d upload non configuré : renseigner "
                    + KEY_DIR + " dans " + LOCATION);
        }
        return dir;
    }

    private static String resolvePlaceholders(String value) {
        Matcher m = PLACEHOLDER.matcher(value);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            String replacement = System.getProperty(m.group(1));
            if (replacement == null) {
                throw new IllegalStateException("Propriété système inconnue dans " + KEY_DIR + " : " + m.group(1));
            }
            m.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        m.appendTail(sb);
        return sb.toString();
    }
}

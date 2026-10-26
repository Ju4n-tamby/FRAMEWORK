package com.framework.model;

import com.framework.service.UploadConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class MapFile {
    private byte[] data;
    private String extension;

    public MapFile(byte[] data, String extension) {
        this.data = data;
        this.extension = extension;
    }

    public MapFile() {
    }

    public byte[] getData() {
        return this.data;
    }

    public String getExtension() {
        return this.extension;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public Path save(String nom) throws IOException {
        Path dossier = UploadConfig.getDirectory();
        String fichier = (extension == null || extension.isEmpty()) ? nom : nom + "." + extension;

        Path cible = dossier.resolve(fichier).normalize();
        // Le nom et l extension viennent du client : le fichier doit rester dans le répertoire configuré
        if (!dossier.equals(cible.getParent())) {
            throw new IllegalArgumentException("Nom de fichier invalide : " + fichier);
        }

        Files.createDirectories(dossier);
        Files.write(cible, data);
        return cible;
    }
}

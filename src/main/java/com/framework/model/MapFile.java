package com.framework.model;

public class MapFile {

    private byte[] data;
    private String extension;

    public  MapFile(byte[] data, String extension) {
        this.data = data;
        this.extension = extension;
    }

    public MapFile() {}

    public byte[] getData() {
        return data;
    }

    public String getExtension() {
        return extension;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }
}

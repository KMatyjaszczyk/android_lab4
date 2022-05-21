package com.example.lab4;

public class FileInfo {
    private final int size;
    private final String type;
    private final boolean hasError;
    private final String errorMessage;

    public FileInfo(int size, String type) {
        this.size = size;
        this.type = type;
        this.hasError = false;
        this.errorMessage = null;
    }

    public FileInfo(int size, String type, boolean hasError, String errorMessage) {
        this.size = size;
        this.type = type;
        this.hasError = hasError;
        this.errorMessage = errorMessage;
    }

    public int getSize() {
        return size;
    }

    public String getType() {
        return type;
    }

    public boolean hasError() {
        return hasError;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}

package com.appsonair.appremark.models;

public class RemarkFileInfo {
    final String key;
    final String fileType;
    final double size;

    private RemarkFileInfo(Builder builder) {
        this.key = builder.key;
        this.fileType = builder.fileType;
        this.size = builder.size;
    }

    public String getKey() {
        return key;
    }

    public String getFileType() {
        return fileType;
    }

    public double getFileSize() {
        return size;
    }

    public static class Builder {
        private String key;
        private String fileType;
        private double size;

        public Builder setKey(String key) {
            this.key = key;
            return this;
        }

        public Builder setFileType(String fileType) {
            this.fileType = fileType;
            return this;
        }
        public Builder setFileSize(double size) {
            this.size = size;
            return this;
        }

        public RemarkFileInfo build() {
            return new RemarkFileInfo(this);
        }
    }
}

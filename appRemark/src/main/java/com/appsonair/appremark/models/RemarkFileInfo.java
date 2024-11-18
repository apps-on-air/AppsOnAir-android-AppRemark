package com.appsonair.appremark.models;

public class RemarkFileInfo {
    final String key;
    final String fileType;

    private RemarkFileInfo(Builder builder) {
        this.key = builder.key;
        this.fileType = builder.fileType;
    }

    public String getKey() {
        return key;
    }

    public String getFileType() {
        return fileType;
    }

    public static class Builder {
        private String key;
        private String fileType;

        public Builder setKey(String key) {
            this.key = key;
            return this;
        }

        public Builder setFileType(String fileType) {
            this.fileType = fileType;
            return this;
        }

        public RemarkFileInfo build() {
            return new RemarkFileInfo(this);
        }
    }
}

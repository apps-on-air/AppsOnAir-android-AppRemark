package com.appsonair.appremark.models;

import android.net.Uri;

public class ImageData {
    final Uri imageUri;
    final String fileName;
    final String fileType;

    private ImageData(Builder builder) {
        this.imageUri = builder.imageUri;
        this.fileName = builder.fileName;
        this.fileType = builder.fileType;
    }

    public Uri getImageUri() {
        return imageUri;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public static class Builder {
        private Uri imageUri;
        private String fileName;
        private String fileType;

        public Builder setImageUri(Uri imageUri) {
            this.imageUri = imageUri;
            return this;
        }

        public Builder setFileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public Builder setFileType(String fileType) {
            this.fileType = fileType;
            return this;
        }

        public ImageData build() {
            return new ImageData(this);
        }
    }
}

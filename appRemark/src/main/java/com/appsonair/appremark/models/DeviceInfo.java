package com.appsonair.appremark.models;

public class DeviceInfo {
    final String deviceModel;
    final String deviceOsVersion;
    final String deviceBatteryLevel;
    final String deviceScreenSize;
    final String deviceOrientation;
    final String deviceRegionCode;
    final String deviceRegionName;
    final String timezone;
    final String buildVersionNumber;
    final String releaseVersionNumber;
    final String bundleIdentifier;
    final String appName;
    final String deviceUsedStorage;
    final String deviceTotalStorage;
    final String deviceMemory;
    final String appMemoryUsage;
    final String appsOnAirSDKVersion;
    final String networkState;

    private DeviceInfo(Builder builder) {
        this.deviceModel = builder.deviceModel;
        this.deviceOsVersion = builder.deviceOsVersion;
        this.deviceBatteryLevel = builder.deviceBatteryLevel;
        this.deviceScreenSize = builder.deviceScreenSize;
        this.deviceOrientation = builder.deviceOrientation;
        this.deviceRegionCode = builder.deviceRegionCode;
        this.deviceRegionName = builder.deviceRegionName;
        this.timezone = builder.timezone;
        this.buildVersionNumber = builder.buildVersionNumber;
        this.releaseVersionNumber = builder.releaseVersionNumber;
        this.bundleIdentifier = builder.bundleIdentifier;
        this.appName = builder.appName;
        this.deviceUsedStorage = builder.deviceUsedStorage;
        this.deviceTotalStorage = builder.deviceTotalStorage;
        this.deviceMemory = builder.deviceMemory;
        this.appMemoryUsage = builder.appMemoryUsage;
        this.appsOnAirSDKVersion = builder.appsOnAirSDKVersion;
        this.networkState = builder.networkState;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public String getDeviceOsVersion() {
        return deviceOsVersion;
    }

    public String getDeviceBatteryLevel() {
        return deviceBatteryLevel;
    }

    public String getDeviceScreenSize() {
        return deviceScreenSize;
    }

    public String getDeviceOrientation() {
        return deviceOrientation;
    }

    public String getDeviceRegionCode() {
        return deviceRegionCode;
    }

    public String getDeviceRegionName() {
        return deviceRegionName;
    }

    public String getTimezone() {
        return timezone;
    }

    public String getBuildVersionNumber() {
        return buildVersionNumber;
    }

    public String getReleaseVersionNumber() {
        return releaseVersionNumber;
    }

    public String getBundleIdentifier() {
        return bundleIdentifier;
    }

    public String getAppName() {
        return appName;
    }

    public String getDeviceUsedStorage() {
        return deviceUsedStorage;
    }

    public String getDeviceTotalStorage() {
        return deviceTotalStorage;
    }

    public String getDeviceMemory() {
        return deviceMemory;
    }

    public String getAppMemoryUsage() {
        return appMemoryUsage;
    }

    public String getAppsOnAirSDKVersion() {
        return appsOnAirSDKVersion;
    }

    public String getNetworkState() {
        return networkState;
    }

    public static class Builder {
        private String deviceModel;
        private String deviceOsVersion;
        private String deviceBatteryLevel;
        private String deviceScreenSize;
        private String deviceOrientation;
        private String deviceRegionCode;
        private String deviceRegionName;
        private String timezone;
        private String buildVersionNumber;
        private String releaseVersionNumber;
        private String bundleIdentifier;
        private String appName;
        private String deviceUsedStorage;
        private String deviceTotalStorage;
        private String deviceMemory;
        private String appMemoryUsage;
        private String appsOnAirSDKVersion;
        private String networkState;

        public Builder setDeviceModel(String deviceModel) {
            this.deviceModel = deviceModel;
            return this;
        }

        public Builder setDeviceOsVersion(String deviceOsVersion) {
            this.deviceOsVersion = deviceOsVersion;
            return this;
        }

        public Builder setDeviceBatteryLevel(String deviceBatteryLevel) {
            this.deviceBatteryLevel = deviceBatteryLevel;
            return this;
        }

        public Builder setDeviceScreenSize(String deviceScreenSize) {
            this.deviceScreenSize = deviceScreenSize;
            return this;
        }

        public Builder setDeviceOrientation(String deviceOrientation) {
            this.deviceOrientation = deviceOrientation;
            return this;
        }

        public Builder setDeviceRegionCode(String deviceRegionCode) {
            this.deviceRegionCode = deviceRegionCode;
            return this;
        }

        public Builder setDeviceRegionName(String deviceRegionName) {
            this.deviceRegionName = deviceRegionName;
            return this;
        }

        public Builder setTimezone(String timezone) {
            this.timezone = timezone;
            return this;
        }

        public Builder setBuildVersionNumber(String buildVersionNumber) {
            this.buildVersionNumber = buildVersionNumber;
            return this;
        }

        public Builder setReleaseVersionNumber(String releaseVersionNumber) {
            this.releaseVersionNumber = releaseVersionNumber;
            return this;
        }

        public Builder setBundleIdentifier(String bundleIdentifier) {
            this.bundleIdentifier = bundleIdentifier;
            return this;
        }

        public Builder setAppName(String appName) {
            this.appName = appName;
            return this;
        }

        public Builder setDeviceUsedStorage(String deviceUsedStorage) {
            this.deviceUsedStorage = deviceUsedStorage;
            return this;
        }

        public Builder setDeviceTotalStorage(String deviceTotalStorage) {
            this.deviceTotalStorage = deviceTotalStorage;
            return this;
        }

        public Builder setDeviceMemory(String deviceMemory) {
            this.deviceMemory = deviceMemory;
            return this;
        }

        public Builder setAppMemoryUsage(String appMemoryUsage) {
            this.appMemoryUsage = appMemoryUsage;
            return this;
        }

        public Builder setAppsOnAirSDKVersion(String appsOnAirSDKVersion) {
            this.appsOnAirSDKVersion = appsOnAirSDKVersion;
            return this;
        }

        public Builder setNetworkState(String networkState) {
            this.networkState = networkState;
            return this;
        }

        public DeviceInfo build() {
            return new DeviceInfo(this);
        }
    }
}

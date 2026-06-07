package com.gestionmorgue.update;

public class UpdateCheckResult {
    private final boolean updateAvailable;
    private final VersionInfo versionInfo;
    private final String message;

    public UpdateCheckResult(boolean updateAvailable, VersionInfo versionInfo, String message) {
        this.updateAvailable = updateAvailable;
        this.versionInfo = versionInfo;
        this.message = message;
    }

    public static UpdateCheckResult updateAvailable(VersionInfo versionInfo) {
        return new UpdateCheckResult(true, versionInfo,
                "Mise à jour " + versionInfo.getVersion() + " disponible");
    }

    public boolean isUpdateAvailable() { return updateAvailable; }
    public VersionInfo getVersionInfo() { return versionInfo; }
    public String getMessage() { return message; }
}

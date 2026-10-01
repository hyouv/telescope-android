// Telescope: adapted from TMessagesProj_AppHockeyApp (beta updater), pointed at our own update server.
package org.telegram.telescope.update;

import org.telegram.messenger.*;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import org.json.JSONObject;
import org.telegram.ui.LaunchActivity;
import org.telegram.ui.web.HttpGetFileTask;
import org.telegram.ui.web.HttpGetTask;

import java.io.File;

public class TelescopeUpdater {

    private static TelescopeUpdater instance;
    public static TelescopeUpdater getInstance() {
        if (instance == null) {
            instance = new TelescopeUpdater();
        }
        return instance;
    }

    private String version;
    private int versionCode;
    private String changelog;
    private String path;
    private long lastCheck;

    private String fileUrl;
    private String sha256;

    public static final String UPDATE_URL = "https://dl.hyv.moe/android/latest.json";

    public TelescopeUpdater() {
        load();
    }

    private SharedPreferences getSharedPreferences() {
        return ApplicationLoader.applicationContext.getSharedPreferences("telescope_update", Activity.MODE_PRIVATE);
    }

    private void load() {
        final SharedPreferences prefs = getSharedPreferences();

        version = prefs.getString("version", null);
        versionCode = prefs.getInt("versionCode", 0);
        changelog = prefs.getString("changelog", null);
        path = prefs.getString("path", null);
        lastCheck = prefs.getLong("lastCheck", 0L);

        if (getCurrentVersionCode() >= versionCode || !TextUtils.isEmpty(path) && !new File(path).exists()) {
            version = null;
            versionCode = 0;
            path = null;
            changelog = null;
            lastCheck = 0;
            save();
        }
    }

    private void save() {
        final SharedPreferences.Editor e = getSharedPreferences().edit();
        if (TextUtils.isEmpty(version)) {
            e.remove("version");
        } else {
            e.putString("version", version);
        }
        if (TextUtils.isEmpty(changelog)) {
            e.remove("changelog");
        } else {
            e.putString("changelog", changelog);
        }
        if (versionCode == 0) {
            e.remove("versionCode");
        } else {
            e.putInt("versionCode", versionCode);
        }
        if (TextUtils.isEmpty(path)) {
            e.remove("path");
        } else {
            e.putString("path", path);
        }
        if (lastCheck == 0) {
            e.remove("lastCheck");
        } else {
            e.putLong("lastCheck", lastCheck);
        }
        e.apply();
    }

    private final static long CHECK_INTERVAL_PAUSED = 1000 * 60 * 60 * 24; // 1 day
    private final static long CHECK_INTERVAL = 1000 * 60 * 60 * 6; // 6 hours
    private final static long CHECK_INTERVAL_PRIVATE = 1000 * 60 * 4; // 5 minutes

    private boolean firstCheck = true;
    private boolean checkingForUpdate;
    private final Runnable scheduledUpdateCheck = () -> checkForUpdate(false, null);
    public void checkForUpdate(boolean force, Runnable whenDone) {
        if (checkingForUpdate) return;

        if (firstCheck) {
            force = true;
        }
        if (!force && System.currentTimeMillis() - lastCheck < (ApplicationLoader.mainInterfacePaused ? CHECK_INTERVAL_PAUSED : (BuildVars.DEBUG_PRIVATE_VERSION ? CHECK_INTERVAL_PRIVATE : CHECK_INTERVAL))) {
            if (whenDone != null) {
                whenDone.run();
            }
            return;
        }

        final String url = UPDATE_URL;
        checkingForUpdate = true;
        firstCheck = false;
        new HttpGetTask(str -> AndroidUtilities.runOnUIThread(() -> {
            checkingForUpdate = false;
            try {
                final JSONObject json = new JSONObject(str);
                final String newVersion = json.getString("version");
                final int newVersionCode = json.getInt("version_code");
                final String fileUrl = json.getString("file_url");
                final String changelog = json.optString("changelog", null);
                final String sha256 = json.optString("sha256", null);

                final int oldVersionCode = this.versionCode;

                if (
                    (version == null || SharedConfig.versionBiggerOrEqual(newVersion, version) && newVersionCode > versionCode) &&
                    SharedConfig.versionBiggerOrEqual(newVersion, getCurrentVersion()) && newVersionCode > getCurrentVersionCode()
                ) { // received newer version
                    if (!TextUtils.isEmpty(path)) {
                        final File file = new File(path);
                        try {
                            file.delete();
                        } catch (Exception e) {
                            FileLog.e(e);
                        }
                    }
                    path = null;
                    version = newVersion;
                    versionCode = newVersionCode;
                    this.fileUrl = fileUrl;
                    this.sha256 = sha256;
                    this.changelog = changelog;
                } else if (
                    version != null && versionCode != 0 && SharedConfig.versionBiggerOrEqual(version, newVersion) && versionCode == newVersionCode
                ) { // received the same version
                    this.fileUrl = fileUrl;
                    this.sha256 = sha256;
                    this.changelog = changelog;
                } else { // received lower version: remove update
                    if (!TextUtils.isEmpty(path)) {
                        final File file = new File(path);
                        try {
                            file.delete();
                        } catch (Exception e) {
                            FileLog.e(e);
                        }
                    }
                    path = null;
                    if (SharedConfig.versionBiggerOrEqual(getCurrentVersion(), newVersion) && getCurrentVersionCode() < newVersionCode) {
                        // remote version is still newer than current installed, even though local downloaded was higher
                        version = newVersion;
                        versionCode = newVersionCode;
                        this.fileUrl = fileUrl;
                        this.sha256 = sha256;
                        this.changelog = changelog;
                    } else {
                        // remove version is the same or less than current installed
                        version = null;
                        versionCode = 0;
                        this.fileUrl = null;
                        this.sha256 = null;
                        this.changelog = null;
                    }
                }

                this.lastCheck = System.currentTimeMillis();
                save();

                if (this.versionCode != oldVersionCode) {
                    NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.appUpdateAvailable);
                }

                AndroidUtilities.cancelRunOnUIThread(this.scheduledUpdateCheck);
                AndroidUtilities.runOnUIThread(this.scheduledUpdateCheck, BuildVars.DEBUG_PRIVATE_VERSION ? CHECK_INTERVAL_PRIVATE : CHECK_INTERVAL);
                if (whenDone != null) {
                    whenDone.run();
                } else if (this.versionCode != oldVersionCode && !ApplicationLoader.mainInterfacePaused) {
                    final Context context = LaunchActivity.instance != null ? LaunchActivity.instance : ApplicationLoader.applicationContext;
                    final BetaUpdate pendingUpdate = getUpdate();
                    if (context != null && pendingUpdate != null) {
                        ApplicationLoader.applicationLoaderInstance.showCustomUpdateAppPopup(context, pendingUpdate, UserConfig.selectedAccount);
                    }
                }
            } catch (Exception e) {
                FileLog.e("Failed to check for beta update at " + url + " received: " + str, e);
            }
        })).execute(url);
    }

    public BetaUpdate getUpdate() {
        if (version == null || versionCode == 0) {
            return null;
        }
        return new BetaUpdate(version, versionCode, changelog);
    }

    private boolean downloading;
    private float downloadingProgress;
    private HttpGetFileTask downloadingTask;
    public void downloadUpdate() {
        downloadUpdate(false);
    }
    private void downloadUpdate(boolean triedGettingFileUrl) {
        if (downloading || !TextUtils.isEmpty(path)) return;

        downloading = true;
        downloadingProgress = 0.0f;
        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.appUpdateLoading);

        if (TextUtils.isEmpty(fileUrl)) {
            if (!triedGettingFileUrl) {
                checkForUpdate(true, () -> downloadUpdate(true));
            } else {
                downloading = false;
            }
            return;
        }

        downloadingTask = new HttpGetFileTask(
            downloadedFile -> AndroidUtilities.runOnUIThread(() -> {
                if (downloadedFile != null && !checkSha256(downloadedFile, sha256)) {
                    FileLog.e("Telescope update: sha256 mismatch, discarding " + downloadedFile);
                    downloadedFile.delete();
                    downloading = false;
                    NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.appUpdateAvailable);
                } else if (downloadedFile != null) {
                    if (!TextUtils.isEmpty(path)) {
                        final File file = new File(path);
                        try {
                            file.delete();
                        } catch (Exception e) {
                            FileLog.e(e);
                        }
                    }
                    path = downloadedFile.getAbsolutePath();
                    save();
                    downloadingProgress = 1.0f;
                    downloading = false;
                    NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.appUpdateAvailable);
                } else {
                    downloading = false;
                    NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.appUpdateAvailable);
                }
            }),
            progress -> {
                downloadingProgress = progress;
                NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.appUpdateLoading);
            }
        ).setOverrideExtension("apk");
        downloadingTask.execute(fileUrl);
    }
    public void cancelDownloadingUpdate() {
        if (!downloading) return;
        if (downloadingTask != null) {
            downloadingTask.cancel(false);
        }
        downloading = false;
        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.appUpdateAvailable);
    }

    public boolean isDownloading() {
        return downloading;
    }

    public float getDownloadingProgress() {
        return downloadingProgress;
    }

    public File getDownloadedFile() {
        if (path == null)
            return null;
        final File file = new File(path);
        if (!file.exists()) {
            path = null;
            save();
            return null;
        }
        return file;
    }

    private static boolean checkSha256(File file, String expected) {
        if (TextUtils.isEmpty(expected)) {
            return false;
        }
        try (java.io.InputStream in = new java.io.FileInputStream(file)) {
            final java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            final byte[] buf = new byte[64 * 1024];
            int n;
            while ((n = in.read(buf)) > 0) {
                md.update(buf, 0, n);
            }
            final StringBuilder sb = new StringBuilder();
            for (byte b : md.digest()) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString().equalsIgnoreCase(expected.trim());
        } catch (Exception e) {
            FileLog.e(e);
            return false;
        }
    }

    private String getCurrentVersion() {
        try {
            return ApplicationLoader.applicationContext.getPackageManager().getPackageInfo(ApplicationLoader.applicationContext.getPackageName(), 0).versionName;
        } catch (Exception e) {
            FileLog.e(e);
            return "";
        }
    }

    private int getCurrentVersionCode() {
        try {
            return ApplicationLoader.applicationContext.getPackageManager().getPackageInfo(ApplicationLoader.applicationContext.getPackageName(), 0).versionCode;
        } catch (Exception e) {
            FileLog.e(e);
            return 0;
        }
    }

}

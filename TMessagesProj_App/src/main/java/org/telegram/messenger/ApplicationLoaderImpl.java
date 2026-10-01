package org.telegram.messenger;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.view.ViewGroup;

import org.telegram.messenger.regular.BuildConfig;
import org.telegram.telescope.update.TelescopeUpdater;
import org.telegram.telescope.update.UpdateAppAlertDialog;
import org.telegram.telescope.update.UpdateLayout;
import org.telegram.ui.Components.AlertsCreator;
import org.telegram.ui.IUpdateLayout;

import java.io.File;

public class ApplicationLoaderImpl extends ApplicationLoader {
    @Override
    protected String onGetApplicationId() {
        return BuildConfig.APPLICATION_ID;
    }

    // Telescope: self-hosted updates (see org.telegram.telescope.update.TelescopeUpdater)

    @Override
    public boolean isCustomUpdate() {
        return true;
    }

    @Override
    public BetaUpdate getUpdate() {
        return TelescopeUpdater.getInstance().getUpdate();
    }

    @Override
    public void checkUpdate(boolean force, Runnable whenDone) {
        TelescopeUpdater.getInstance().checkForUpdate(force, whenDone);
    }

    @Override
    public void downloadUpdate() {
        TelescopeUpdater.getInstance().downloadUpdate();
    }

    @Override
    public void cancelDownloadingUpdate() {
        TelescopeUpdater.getInstance().cancelDownloadingUpdate();
    }

    @Override
    public boolean isDownloadingUpdate() {
        return TelescopeUpdater.getInstance().isDownloading();
    }

    @Override
    public float getDownloadingUpdateProgress() {
        return TelescopeUpdater.getInstance().getDownloadingProgress();
    }

    @Override
    public File getDownloadedUpdateFile() {
        return TelescopeUpdater.getInstance().getDownloadedFile();
    }

    @Override
    public IUpdateLayout takeUpdateLayout(Activity activity, ViewGroup sideMenuContainer) {
        return new UpdateLayout(activity, sideMenuContainer);
    }

    @Override
    public boolean showCustomUpdateAppPopup(Context context, BetaUpdate update, int account) {
        try {
            new UpdateAppAlertDialog(context, update, account).show();
        } catch (Exception e) {
            FileLog.e(e);
        }
        return true;
    }

    @Override
    public boolean checkApkInstallPermissions(final Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !ApplicationLoader.applicationContext.getPackageManager().canRequestPackageInstalls()) {
            AlertsCreator.createApkRestrictedDialog(context, null).show();
            return false;
        }
        return true;
    }
}

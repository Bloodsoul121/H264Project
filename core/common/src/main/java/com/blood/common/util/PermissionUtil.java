package com.blood.common.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;

public class PermissionUtil {

    public static void launchAppDetailsSettings(final Context context) {
        Intent intent = getLaunchAppDetailsSettingsIntent(context.getPackageName(), true);
        if (!isIntentAvailable(context, intent)) return;
        context.startActivity(intent);
    }

    private static Intent getLaunchAppDetailsSettingsIntent(final String pkgName, final boolean isNewTask) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + pkgName));
        return getIntent(intent, isNewTask);
    }

    private static Intent getIntent(final Intent intent, final boolean isNewTask) {
        return isNewTask ? intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) : intent;
    }

    private static boolean isIntentAvailable(final Context context, final Intent intent) {
        return context.getPackageManager()
                .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
                .size() > 0;
    }

}

package org.telegram.telescope;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.R;

/**
 * Telescope: single place for all feature switches of this fork.
 * Upstream code is patched with minimal one-line checks against these flags,
 * so merges from upstream stay small and every change is easy to find
 * by searching for "Telescope.".
 */
public final class Telescope {

    /** Don't request or show sponsored messages, video ads and sponsored search results. */
    public static final boolean HIDE_SPONSORED = true;

    /** Don't load, show or post stories. */
    public static final boolean HIDE_STORIES = true;

    /** Hide Premium / Stars / gifts entry points and upsell UI. */
    public static final boolean HIDE_PREMIUM = true;

    private Telescope() {
    }

    /** Android account type for contact sync; must not clash with the official app. */
    public static String accountType() {
        return ApplicationLoader.applicationContext.getString(R.string.telescope_account_type);
    }
}

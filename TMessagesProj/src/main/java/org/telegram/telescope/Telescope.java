package org.telegram.telescope;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;

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

    /** Hide the alternate launcher icon picker; only our own icon is shipped. */
    public static final boolean HIDE_APP_ICONS = true;

    /** Remove every "send a gift" entry point. */
    public static final boolean HIDE_GIFTS = true;

    /** Hide the name / profile color editor. */
    public static final boolean HIDE_PEER_COLORS = true;

    /** Hide Stars / TON balances, the Wallet bot entry, star (paid) reactions and sending paid media. */
    public static final boolean HIDE_STARS = true;

    /** Hide collectible gift actions: upgrade, transfer, resale, buying, offers and crafting. */
    public static final boolean HIDE_COLLECTIBLES = true;

    /** Hide channel monetization: boosts, revenue, giveaways (via boosts) and affiliate programs. Plain statistics stay. */
    public static final boolean HIDE_MONETIZATION = true;

    /** Hide the promo / nag hints above the chat list (server promos, birthday and photo setup, low Stars balance). */
    public static final boolean HIDE_DIALOGS_HINTS = true;

    /** Don't show Premium-only emoji, emoji packs and message effects to non-Premium users instead of showing them locked. */
    public static final boolean HIDE_LOCKED_PREMIUM = true;

    /** Hide the emoji keyboard settings button, trending emoji packs in the keyboard and animated emoji suggestions while typing. */
    public static final boolean HIDE_EMOJI_SUGGESTIONS = true;

    /** Hide the "Invite friends" entry in contacts. */
    public static final boolean HIDE_INVITE_FRIENDS = true;

    /** Hide "Apply for me and <peer>" when setting a chat wallpaper; wallpapers are only set for yourself. */
    public static final boolean HIDE_WALLPAPER_FOR_BOTH = true;

    /** Never ask for contacts access on our own (startup, contacts tab, the "!" badge). Sharing a contact still asks. */
    public static final boolean HIDE_CONTACTS_PROMPT = true;

    /** Hide the gifts tab on user / channel profiles and the gifts floating around the profile photo. */
    public static final boolean HIDE_PROFILE_GIFTS = true;

    /** Drop emoji statuses (including collectible ones) from users and channels, so none show next to names. */
    public static final boolean HIDE_EMOJI_STATUS = true;

    /** Hide the Premium star after other users' names, so Premium users look the same as everyone else. */
    public static final boolean HIDE_PREMIUM_BADGE = true;

    /** Hide folder tags in the chat list and the folder color setting. */
    public static final boolean HIDE_FOLDER_TAGS = true;

    private Telescope() {
    }

    /** True when locked Premium content should be left out for this account. */
    public static boolean hideLockedPremium(int account) {
        return HIDE_LOCKED_PREMIUM && !UserConfig.getInstance(account).isPremium();
    }

    /** Android account type for contact sync; must not clash with the official app. */
    public static String accountType() {
        return ApplicationLoader.applicationContext.getString(R.string.telescope_account_type);
    }
}

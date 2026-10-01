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

    /** Remove every "send a gift" entry point (received gifts can still be viewed). */
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

    /** Hide the "Invite friends" entry in contacts. */
    public static final boolean HIDE_INVITE_FRIENDS = true;

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

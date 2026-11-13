package io.github.doubleddoge.splithappens.data.pref

object PrefFiles {
	const val APP = "split_happens_prefs"
	const val AUTH = "split_happens_auth"
}

object PrefKeys {
	const val DARK_THEME = "pref_dark_theme"
	const val SHOW_CARD_TOTAL = "pref_show_card_total"
	const val DEALER_RULE_BANNER = "pref_dealer_rule_banner"
	const val SOUND_EFFECTS = "pref_sound_effects"
	const val HAPTIC_VIBRATION = "pref_haptic_vibration"
	const val FAST_DEAL = "pref_fast_deal"
	const val AUTO_STAND_ON_21 = "pref_auto_stand_21"

	const val ACTIVE_USER_ID = "active_user_id"
	private const val LAST_DAILY_CLAIM_PREFIX = "pref_last_daily_claim_"
	fun lastDailyClaim(userId: String) = "$LAST_DAILY_CLAIM_PREFIX$userId"

	const val SAVED_USER_ID = "saved_user_id"
	const val SAVED_USERNAME = "saved_username"
	const val REMEMBER_ME = "remember_me"
	private const val LAST_USED_PREFIX = "last_used_"
	fun lastUsed(userId: String) = "$LAST_USED_PREFIX$userId"
}
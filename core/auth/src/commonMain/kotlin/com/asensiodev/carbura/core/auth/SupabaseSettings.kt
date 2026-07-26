package com.asensiodev.carbura.core.auth

data class SupabaseSettings(
    val url: String,
    val anonKey: String,
    val googleClientId: String = "",
    val environment: String = "production",
) {
    fun validate() {
        require(url.isNotBlank()) { "SUPABASE_URL is missing in local.properties" }
        require(anonKey.isNotBlank()) { "SUPABASE_ANON_KEY is missing in local.properties" }
        require(environment.isNotBlank()) { "Supabase environment is missing" }
    }
}

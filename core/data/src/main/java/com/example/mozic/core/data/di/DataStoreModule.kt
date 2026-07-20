package com.example.mozic.core.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

private const val USER_PREFERENCES_DATASTORE_NAME = "user_preferences"
private const val AUTH_SESSION_DATASTORE_NAME = "auth_session"

private val Context.userPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = USER_PREFERENCES_DATASTORE_NAME,
)

private val Context.authSessionDataStore: DataStore<Preferences> by preferencesDataStore(
    name = AUTH_SESSION_DATASTORE_NAME,
)

/**
 * A real Supabase Auth session (C5) is a different concern from device-local
 * prefs (theme/premium/…) — a separate DataStore keeps their key-namespaces apart.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthSessionDataStore

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {
    @Provides
    @Singleton
    fun provideUserPreferencesDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.userPreferencesDataStore

    @Provides
    @Singleton
    @AuthSessionDataStore
    fun provideAuthSessionDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.authSessionDataStore
}

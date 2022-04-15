package com.example.traders.hilt

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @ApplicationScopeDefault
    @Provides
    @Singleton
    fun provideApplicationScope() = CoroutineScope(SupervisorJob() + Dispatchers.Default)
}

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScopeDefault

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScopeIO
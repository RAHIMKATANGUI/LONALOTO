package com.lonaloto.di

import android.content.Context
import androidx.room.Room
import com.lonaloto.data.local.AppDatabase
import com.lonaloto.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun fournirAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.NOM_BASE)
            // .addMigrations(AppDatabase.MIGRATION_1_2, ...) — à ajouter au fil des versions
            .build()

    @Provides
    fun fournirUtilisateurDao(db: AppDatabase): UtilisateurDao = db.utilisateurDao()

    @Provides
    fun fournirActiviteDao(db: AppDatabase): ActiviteDao = db.activiteDao()

    @Provides
    fun fournirMoisDao(db: AppDatabase): MoisDao = db.moisDao()

    @Provides
    fun fournirVenteDao(db: AppDatabase): VenteDao = db.venteDao()

    @Provides
    fun fournirPalierBonusDao(db: AppDatabase): PalierBonusDao = db.palierBonusDao()

    @Provides
    fun fournirParametreDao(db: AppDatabase): ParametreDao = db.parametreDao()

    @Provides
    fun fournirHistoriqueAuditDao(db: AppDatabase): HistoriqueAuditDao = db.historiqueAuditDao()
}

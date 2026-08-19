package com.lonaloto.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.lonaloto.data.local.dao.*
import com.lonaloto.data.local.entities.*

@Database(
    entities = [
        Utilisateur::class,
        Activite::class,
        Mois::class,
        Vente::class,
        PalierBonus::class,
        Parametre::class,
        HistoriqueAudit::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Convertisseurs::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun utilisateurDao(): UtilisateurDao
    abstract fun activiteDao(): ActiviteDao
    abstract fun moisDao(): MoisDao
    abstract fun venteDao(): VenteDao
    abstract fun palierBonusDao(): PalierBonusDao
    abstract fun parametreDao(): ParametreDao
    abstract fun historiqueAuditDao(): HistoriqueAuditDao

    companion object {
        const val NOM_BASE = "lonaloto.db"

        // Futures migrations à ajouter ici (ex: ajout d'une colonne, nouvelle table)
        // sans jamais perdre les données existantes sur le terrain :
        // val MIGRATION_1_2 = object : Migration(1, 2) { ... }
    }
}

package com.lonaloto.data.backup

import android.content.Context
import android.net.Uri
import com.lonaloto.data.local.AppDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

sealed class ResultatSauvegarde {
    data class Succes(val fichier: File) : ResultatSauvegarde()
    data class Erreur(val message: String) : ResultatSauvegarde()
}

sealed class ResultatRestauration {
    data object Succes : ResultatRestauration()
    data class Erreur(val message: String) : ResultatRestauration()
}

/**
 * Export/Import brut du fichier SQLite Room — répond au point "Sauvegarde et
 * Restauration : Export/Import de la base en .db" du cahier des charges (ADMIN uniquement).
 *
 * ⚠️ La restauration remplace le fichier de base pendant que l'app est en cours
 * d'exécution : Room garde une connexion ouverte, donc l'app DOIT redémarrer
 * juste après un import pour que les nouvelles données soient prises en compte
 * correctement (fait remonter dans l'UI, voir SauvegardeViewModel).
 */
@Singleton
class SauvegardeManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val formatHorodatage = SimpleDateFormat("yyyy-MM-dd_HHmm", Locale.FRANCE)

    /** Copie le fichier de base actuel vers un dossier accessible, prêt à être exporté/partagé. */
    fun exporterVersFichierLocal(): ResultatSauvegarde {
        return try {
            val dbFile = context.getDatabasePath(AppDatabase.NOM_BASE)
            if (!dbFile.exists()) {
                return ResultatSauvegarde.Erreur("Aucune base de données trouvée")
            }

            val dossierSauvegardes = File(context.filesDir, "sauvegardes").apply { mkdirs() }
            val nomFichier = "LONALOTO_sauvegarde_${formatHorodatage.format(java.util.Date())}.db"
            val fichierDestination = File(dossierSauvegardes, nomFichier)

            dbFile.copyTo(fichierDestination, overwrite = true)
            ResultatSauvegarde.Succes(fichierDestination)
        } catch (e: Exception) {
            ResultatSauvegarde.Erreur("Échec de l'export : ${e.message}")
        }
    }

    /** Copie le contenu d'un Uri (choisi via le sélecteur de fichiers système) vers un fichier local, en écrasant la base actuelle. */
    fun importerDepuisUri(uri: Uri): ResultatRestauration {
        return try {
            val dbFile = context.getDatabasePath(AppDatabase.NOM_BASE)

            context.contentResolver.openInputStream(uri)?.use { entree ->
                dbFile.outputStream().use { sortie -> entree.copyTo(sortie) }
            } ?: return ResultatRestauration.Erreur("Impossible de lire le fichier sélectionné")

            // Supprime les fichiers auxiliaires SQLite (WAL/SHM) pour forcer Room
            // à relire proprement le nouveau fichier au prochain démarrage.
            File("${dbFile.path}-wal").delete()
            File("${dbFile.path}-shm").delete()

            ResultatRestauration.Succes
        } catch (e: Exception) {
            ResultatRestauration.Erreur("Échec de l'import : ${e.message}")
        }
    }
}

package com.lonaloto.ui.audit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lonaloto.data.local.entities.TypeAction
import java.text.SimpleDateFormat
import java.util.Locale

private val formatDate = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE)

@Composable
fun AuditScreen(
    viewModel: AuditViewModel = hiltViewModel()
) {
    val lignes by viewModel.lignes.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Journal d'audit", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        if (lignes.isEmpty()) {
            Text("Aucune action enregistrée pour l'instant.", style = MaterialTheme.typography.bodyMedium)
        }

        LazyColumn {
            items(lignes) { ligne ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text(libelleAction(ligne.entree.action), fontWeight = FontWeight.Bold)
                            Text(formatDate.format(ligne.entree.dateAction), style = MaterialTheme.typography.bodySmall)
                        }
                        Text("Par ${ligne.nomUtilisateur} — table \"${ligne.entree.tableCible}\"")
                        ligne.entree.enregistrementId?.let { Text("Enregistrement #$it", style = MaterialTheme.typography.bodySmall) }
                    }
                }
            }
        }
    }
}

private fun libelleAction(action: TypeAction): String = when (action) {
    TypeAction.CREATION -> "Création"
    TypeAction.MODIFICATION -> "Modification"
    TypeAction.SUPPRESSION -> "Suppression"
    TypeAction.VALIDATION -> "Validation"
    TypeAction.CONNEXION -> "Connexion"
    TypeAction.EXPORT -> "Export"
    TypeAction.IMPORT -> "Import"
}

package com.lonaloto.ui.parametres

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lonaloto.data.local.entities.Activite
import com.lonaloto.data.local.entities.PalierBonus
import kotlinx.coroutines.flow.MutableStateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParametresScreen(
    viewModel: ParametresViewModel = hiltViewModel()
) {
    val etat by viewModel.etat.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var afficherDialogueRenommage by remember { mutableStateOf<Activite?>(null) }
    var afficherDialogueNouvelleActivite by remember { mutableStateOf(false) }
    var afficherDialogueTaux by remember { mutableStateOf<Activite?>(null) }
    var afficherDialoguePalier by remember { mutableStateOf(false) }

    val lanceurImport = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            viewModel.importerSauvegarde(it) {
                // Redémarre l'app pour que Room relise le fichier restauré proprement.
                val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
                intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                Runtime.getRuntime().exit(0)
            }
        }
    }

    val activiteSelectionnee = etat.activites.find { it.id == etat.activiteSelectionneeId }
    val paliers by (activiteSelectionnee?.let { viewModel.paliersDeLActivite(it.id) }
        ?: remember { MutableStateFlow(emptyList<PalierBonus>()) }).collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Paramètres", style = MaterialTheme.typography.headlineSmall)
            IconButton(onClick = { afficherDialogueNouvelleActivite = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Nouvelle activité")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sélecteur d'activité (onglets défilants)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(etat.activites) { activite ->
                FilterChip(
                    selected = activite.id == etat.activiteSelectionneeId,
                    onClick = { viewModel.selectionnerActivite(activite.id) },
                    label = { Text(activite.nom) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        activiteSelectionnee?.let { activite ->
            LazyColumn(modifier = Modifier.weight(1f)) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(activite.nom, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                Row {
                                    IconButton(onClick = { afficherDialogueRenommage = activite }) {
                                        Icon(Icons.Filled.Edit, contentDescription = "Renommer")
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Taux Recette : ${pourcent(activite.tauxRecette)}")
                            Text("Taux Paiement : ${pourcent(activite.tauxPaiement)}")
                            Text("Taux LONACI : ${pourcent(activite.tauxLonaci)}")
                            Text("Taux Salaire Coupeur : ${pourcent(activite.tauxSalaireCoupeur)}")
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(onClick = { afficherDialogueTaux = activite }) {
                                Text("Modifier les taux")
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Paliers de bonus", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { afficherDialoguePalier = true }) {
                            Icon(Icons.Filled.Add, contentDescription = "Ajouter un palier")
                        }
                    }
                    if (paliers.isEmpty()) {
                        Text(
                            "Aucun palier configuré — aucun bonus n'est appliqué (comportement identique à l'Excel d'origine).",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                items(paliers) { palier ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Dès ${palier.seuilMin.toInt()} FCFA${palier.seuilMax?.let { " → ${it.toInt()} FCFA" } ?: " (sans plafond)"}")
                                Text("Bonus : ${pourcent(palier.tauxBonus)}", fontWeight = FontWeight.Bold)
                            }
                            IconButton(onClick = { viewModel.supprimerPalier(palier) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Supprimer")
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Sauvegarde de la base", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = {
                            val fichier = viewModel.exporterSauvegarde()
                            fichier?.let {
                                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", it)
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "application/octet-stream"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(intent, "Exporter la sauvegarde"))
                            }
                        }) { Text("Exporter (.db)") }

                        OutlinedButton(onClick = { lanceurImport.launch(arrayOf("*/*")) }) {
                            Text("Importer")
                        }
                    }
                    Text(
                        "L'import remplace toutes les données actuelles et redémarre l'application.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        etat.messageSucces?.let {
            Text(it, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 8.dp))
        }
        etat.messageErreur?.let {
            Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
        }
    }

    afficherDialogueRenommage?.let { activite ->
        DialogueRenommage(
            nomActuel = activite.nom,
            onConfirmer = { nouveauNom ->
                viewModel.renommerActivite(activite.id, nouveauNom)
                afficherDialogueRenommage = null
            },
            onAnnuler = { afficherDialogueRenommage = null }
        )
    }

    if (afficherDialogueNouvelleActivite) {
        DialogueNouvelleActivite(
            onConfirmer = { nom, tr, tp, tl, ts ->
                viewModel.creerActivite(nom, tr, tp, tl, ts)
                afficherDialogueNouvelleActivite = false
            },
            onAnnuler = { afficherDialogueNouvelleActivite = false }
        )
    }

    afficherDialogueTaux?.let { activite ->
        DialogueModifierTaux(
            activite = activite,
            onConfirmer = { activiteModifiee ->
                viewModel.modifierTaux(activiteModifiee)
                afficherDialogueTaux = null
            },
            onAnnuler = { afficherDialogueTaux = null }
        )
    }

    if (afficherDialoguePalier && activiteSelectionnee != null) {
        DialogueNouveauPalier(
            onConfirmer = { seuilMin, seuilMax, taux ->
                viewModel.ajouterPalier(activiteSelectionnee.id, seuilMin, seuilMax, taux)
                afficherDialoguePalier = false
            },
            onAnnuler = { afficherDialoguePalier = false }
        )
    }
}

private fun pourcent(taux: Double): String = "${(taux * 100).let { if (it == it.toInt().toDouble()) it.toInt().toString() else it.toString() }}%"

@Composable
private fun DialogueRenommage(nomActuel: String, onConfirmer: (String) -> Unit, onAnnuler: () -> Unit) {
    var nom by remember { mutableStateOf(nomActuel) }
    AlertDialog(
        onDismissRequest = onAnnuler,
        title = { Text("Renommer l'activité") },
        text = {
            OutlinedTextField(value = nom, onValueChange = { nom = it }, singleLine = true, label = { Text("Nouveau nom") })
        },
        confirmButton = { TextButton(onClick = { onConfirmer(nom) }) { Text("Renommer") } },
        dismissButton = { TextButton(onClick = onAnnuler) { Text("Annuler") } }
    )
}

@Composable
private fun DialogueNouvelleActivite(
    onConfirmer: (nom: String, tr: Double, tp: Double, tl: Double, ts: Double) -> Unit,
    onAnnuler: () -> Unit
) {
    var nom by remember { mutableStateOf("") }
    var tr by remember { mutableStateOf("0.13") }
    var tp by remember { mutableStateOf("0.03") }
    var tl by remember { mutableStateOf("0.02") }
    var ts by remember { mutableStateOf("0.04") }

    AlertDialog(
        onDismissRequest = onAnnuler,
        title = { Text("Nouvelle activité") },
        text = {
            Column {
                OutlinedTextField(value = nom, onValueChange = { nom = it }, singleLine = true, label = { Text("Nom") })
                OutlinedTextField(value = tr, onValueChange = { tr = it }, singleLine = true, label = { Text("Taux Recette (ex: 0.13)") })
                OutlinedTextField(value = tp, onValueChange = { tp = it }, singleLine = true, label = { Text("Taux Paiement") })
                OutlinedTextField(value = tl, onValueChange = { tl = it }, singleLine = true, label = { Text("Taux LONACI") })
                OutlinedTextField(value = ts, onValueChange = { ts = it }, singleLine = true, label = { Text("Taux Salaire Coupeur") })
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirmer(
                    nom,
                    tr.toDoubleOrNull() ?: 0.0,
                    tp.toDoubleOrNull() ?: 0.0,
                    tl.toDoubleOrNull() ?: 0.0,
                    ts.toDoubleOrNull() ?: 0.0
                )
            }) { Text("Créer") }
        },
        dismissButton = { TextButton(onClick = onAnnuler) { Text("Annuler") } }
    )
}

@Composable
private fun DialogueModifierTaux(activite: Activite, onConfirmer: (Activite) -> Unit, onAnnuler: () -> Unit) {
    var tr by remember { mutableStateOf(activite.tauxRecette.toString()) }
    var tp by remember { mutableStateOf(activite.tauxPaiement.toString()) }
    var tl by remember { mutableStateOf(activite.tauxLonaci.toString()) }
    var ts by remember { mutableStateOf(activite.tauxSalaireCoupeur.toString()) }

    AlertDialog(
        onDismissRequest = onAnnuler,
        title = { Text("Modifier les taux — ${activite.nom}") },
        text = {
            Column {
                OutlinedTextField(value = tr, onValueChange = { tr = it }, singleLine = true, label = { Text("Taux Recette") })
                OutlinedTextField(value = tp, onValueChange = { tp = it }, singleLine = true, label = { Text("Taux Paiement") })
                OutlinedTextField(value = tl, onValueChange = { tl = it }, singleLine = true, label = { Text("Taux LONACI") })
                OutlinedTextField(value = ts, onValueChange = { ts = it }, singleLine = true, label = { Text("Taux Salaire Coupeur") })
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirmer(
                    activite.copy(
                        tauxRecette = tr.toDoubleOrNull() ?: activite.tauxRecette,
                        tauxPaiement = tp.toDoubleOrNull() ?: activite.tauxPaiement,
                        tauxLonaci = tl.toDoubleOrNull() ?: activite.tauxLonaci,
                        tauxSalaireCoupeur = ts.toDoubleOrNull() ?: activite.tauxSalaireCoupeur
                    )
                )
            }) { Text("Enregistrer") }
        },
        dismissButton = { TextButton(onClick = onAnnuler) { Text("Annuler") } }
    )
}

@Composable
private fun DialogueNouveauPalier(
    onConfirmer: (seuilMin: Double, seuilMax: Double?, taux: Double) -> Unit,
    onAnnuler: () -> Unit
) {
    var seuilMin by remember { mutableStateOf("") }
    var seuilMax by remember { mutableStateOf("") }
    var taux by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onAnnuler,
        title = { Text("Nouveau palier de bonus") },
        text = {
            Column {
                OutlinedTextField(value = seuilMin, onValueChange = { seuilMin = it }, singleLine = true, label = { Text("Seuil minimum (FCFA)") })
                OutlinedTextField(value = seuilMax, onValueChange = { seuilMax = it }, singleLine = true, label = { Text("Seuil maximum (vide = sans plafond)") })
                OutlinedTextField(value = taux, onValueChange = { taux = it }, singleLine = true, label = { Text("Taux de bonus (ex: 0.01 pour 1%)") })
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirmer(
                    seuilMin.toDoubleOrNull() ?: 0.0,
                    seuilMax.toDoubleOrNull(),
                    taux.toDoubleOrNull() ?: 0.0
                )
            }) { Text("Ajouter") }
        },
        dismissButton = { TextButton(onClick = onAnnuler) { Text("Annuler") } }
    )
}

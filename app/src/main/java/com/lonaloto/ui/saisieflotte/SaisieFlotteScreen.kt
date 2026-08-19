package com.lonaloto.ui.saisieflotte

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

private val formatFcfa = NumberFormat.getNumberInstance(Locale.FRANCE)
private val formatDateCourte = SimpleDateFormat("dd/MM", Locale.FRANCE)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaisieFlotteScreen(
    viewModel: SaisieFlotteViewModel = hiltViewModel()
) {
    val etat by viewModel.etat.collectAsStateWithLifecycle()
    var menuVendeurOuvert by remember { mutableStateOf(false) }

    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(24.dp)) {

        item {
            Text("Saisir pour un vendeur", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))

            Box {
                OutlinedButton(onClick = { menuVendeurOuvert = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(etat.vendeurs.find { it.id == etat.vendeurSelectionneId }?.nom ?: "Choisir un vendeur")
                }
                DropdownMenu(expanded = menuVendeurOuvert, onDismissRequest = { menuVendeurOuvert = false }) {
                    etat.vendeurs.forEach { vendeur ->
                        DropdownMenuItem(
                            text = { Text(vendeur.nom) },
                            onClick = { viewModel.selectionnerVendeur(vendeur.id); menuVendeurOuvert = false }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = etat.recette,
                onValueChange = viewModel::onRecetteChange,
                label = { Text("Recette journalière (FCFA)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = etat.paiement,
                onValueChange = viewModel::onPaiementChange,
                label = { Text("Paiement journalier (FCFA)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            if (etat.messageErreur != null) {
                Text(etat.messageErreur!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
            }
            if (etat.messageSucces != null) {
                Text(etat.messageSucces!!, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 8.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = viewModel::enregistrer,
                enabled = !etat.enCours,
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text("ENREGISTRER POUR CE VENDEUR")
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text("En attente de validation", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            if (etat.enAttenteDeValidation.isEmpty()) {
                Text("Rien à valider pour le moment.", style = MaterialTheme.typography.bodySmall)
            }
        }

        items(etat.enAttenteDeValidation) { ligne ->
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(ligne.nomVendeur, fontWeight = FontWeight.Bold)
                        Text("${formatDateCourte.format(ligne.vente.date)} — Recette ${formatFcfa.format(ligne.vente.recette)} / Paiement ${formatFcfa.format(ligne.vente.paiement)}")
                    }
                    Button(onClick = { viewModel.valider(ligne.vente.id) }) { Text("Valider") }
                }
            }
        }
    }
}

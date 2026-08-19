package com.lonaloto.ui.saisie

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Saisie de la recette/paiement du jour — écran pensé pour le terrain :
 * gros champs numériques, un seul bouton d'action, retour visuel immédiat.
 */
@Composable
fun SaisieVenteScreen(
    viewModel: SaisieVenteViewModel = hiltViewModel()
) {
    val etat by viewModel.etat.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "Saisie du jour",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = etat.recette,
            onValueChange = viewModel::onRecetteChange,
            label = { Text("Recette journalière (FCFA)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            textStyle = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = etat.paiement,
            onValueChange = viewModel::onPaiementChange,
            label = { Text("Paiement journalier (FCFA)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            textStyle = MaterialTheme.typography.titleLarge
        )

        if (etat.messageErreur != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = etat.messageErreur!!, color = MaterialTheme.colorScheme.error)
        }
        if (etat.messageSucces != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = etat.messageSucces!!, color = MaterialTheme.colorScheme.primary)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = viewModel::enregistrer,
            enabled = !etat.enCours,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            if (etat.enCours) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("ENREGISTRER", fontSize = 18.sp)
            }
        }
    }
}

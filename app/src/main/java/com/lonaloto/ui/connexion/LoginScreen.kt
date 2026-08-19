package com.lonaloto.ui.connexion

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Écran de connexion — Nom + code PIN.
 * Boutons et champs volontairement larges pour une utilisation sur le terrain
 * (gants, luminosité extérieure, saisie rapide), conformément au cahier des charges.
 */
@Composable
fun LoginScreen(
    onConnexionReussie: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val etat by viewModel.etat.collectAsStateWithLifecycle()

    LaunchedEffect(etat.connexionReussie) {
        if (etat.connexionReussie) {
            onConnexionReussie()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "LONALOTO",
                fontSize = 32.sp,
                style = MaterialTheme.typography.headlineLarge
            )

            Spacer(modifier = Modifier.height(48.dp))

            OutlinedTextField(
                value = etat.nom,
                onValueChange = viewModel::onNomChange,
                label = { Text("Nom d'utilisateur") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                textStyle = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = etat.pin,
                onValueChange = viewModel::onPinChange,
                label = { Text("Code PIN") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                textStyle = MaterialTheme.typography.titleMedium
            )

            if (etat.messageErreur != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = etat.messageErreur!!,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = viewModel::seConnecter,
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
                    Text("SE CONNECTER", fontSize = 18.sp)
                }
            }
        }
    }
}

package com.lonaloto.ui.utilisateurs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lonaloto.data.local.entities.Activite
import com.lonaloto.data.local.entities.Role
import com.lonaloto.data.local.entities.Utilisateur

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UtilisateursScreen(
    viewModel: UtilisateursViewModel = hiltViewModel()
) {
    val etat by viewModel.etat.collectAsStateWithLifecycle()
    var afficherDialogueCreation by remember { mutableStateOf(false) }
    var afficherDialogueResetPin by remember { mutableStateOf<Utilisateur?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Utilisateurs", style = MaterialTheme.typography.headlineSmall)
            IconButton(onClick = { afficherDialogueCreation = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Nouvel utilisateur")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(etat.utilisateurs) { utilisateur ->
                val nomActivite = etat.activites.find { it.id == utilisateur.activiteId }?.nom
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(utilisateur.nom, fontWeight = FontWeight.Bold)
                            Text("${libelleRole(utilisateur.role)}${nomActivite?.let { " — $it" } ?: ""}")
                        }
                        Row {
                            IconButton(onClick = { afficherDialogueResetPin = utilisateur }) {
                                Icon(Icons.Filled.LockReset, contentDescription = "Réinitialiser le PIN")
                            }
                            IconButton(onClick = { viewModel.desactiver(utilisateur.id) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Désactiver")
                            }
                        }
                    }
                }
            }
        }

        etat.messageSucces?.let { Text(it, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 8.dp)) }
        etat.messageErreur?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp)) }
    }

    if (afficherDialogueCreation) {
        DialogueNouvelUtilisateur(
            activites = etat.activites,
            onConfirmer = { nom, pin, role, activiteId ->
                viewModel.creerUtilisateur(nom, pin, role, activiteId)
                afficherDialogueCreation = false
            },
            onAnnuler = { afficherDialogueCreation = false }
        )
    }

    afficherDialogueResetPin?.let { utilisateur ->
        DialogueResetPin(
            nomUtilisateur = utilisateur.nom,
            onConfirmer = { nouveauPin ->
                viewModel.reinitialiserPin(utilisateur.id, nouveauPin)
                afficherDialogueResetPin = null
            },
            onAnnuler = { afficherDialogueResetPin = null }
        )
    }
}

private fun libelleRole(role: Role): String = when (role) {
    Role.ADMIN -> "Administrateur"
    Role.CHEF_DE_FLOTTE -> "Chef de flotte"
    Role.VENDEUR -> "Vendeur"
}

@Composable
private fun DialogueNouvelUtilisateur(
    activites: List<Activite>,
    onConfirmer: (nom: String, pin: String, role: Role, activiteId: Long?) -> Unit,
    onAnnuler: () -> Unit
) {
    var nom by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(Role.VENDEUR) }
    var activiteId by remember { mutableStateOf<Long?>(activites.firstOrNull()?.id) }
    var menuRoleOuvert by remember { mutableStateOf(false) }
    var menuActiviteOuvert by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onAnnuler,
        title = { Text("Nouvel utilisateur") },
        text = {
            Column {
                OutlinedTextField(value = nom, onValueChange = { nom = it }, singleLine = true, label = { Text("Nom") })
                OutlinedTextField(
                    value = pin,
                    onValueChange = { pin = it.filter(Char::isDigit).take(8) },
                    singleLine = true,
                    label = { Text("Code PIN (4 à 8 chiffres)") }
                )

                Spacer(modifier = Modifier.height(8.dp))
                Box {
                    OutlinedButton(onClick = { menuRoleOuvert = true }) { Text(libelleRole(role)) }
                    DropdownMenu(expanded = menuRoleOuvert, onDismissRequest = { menuRoleOuvert = false }) {
                        Role.entries.forEach { r ->
                            DropdownMenuItem(text = { Text(libelleRole(r)) }, onClick = { role = r; menuRoleOuvert = false })
                        }
                    }
                }

                if (role != Role.ADMIN) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box {
                        OutlinedButton(onClick = { menuActiviteOuvert = true }) {
                            Text(activites.find { it.id == activiteId }?.nom ?: "Choisir une activité")
                        }
                        DropdownMenu(expanded = menuActiviteOuvert, onDismissRequest = { menuActiviteOuvert = false }) {
                            activites.forEach { a ->
                                DropdownMenuItem(text = { Text(a.nom) }, onClick = { activiteId = a.id; menuActiviteOuvert = false })
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirmer(nom, pin, role, if (role == Role.ADMIN) null else activiteId) }) {
                Text("Créer")
            }
        },
        dismissButton = { TextButton(onClick = onAnnuler) { Text("Annuler") } }
    )
}

@Composable
private fun DialogueResetPin(nomUtilisateur: String, onConfirmer: (String) -> Unit, onAnnuler: () -> Unit) {
    var pin by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onAnnuler,
        title = { Text("Réinitialiser le PIN — $nomUtilisateur") },
        text = {
            OutlinedTextField(
                value = pin,
                onValueChange = { pin = it.filter(Char::isDigit).take(8) },
                singleLine = true,
                label = { Text("Nouveau code PIN") }
            )
        },
        confirmButton = { TextButton(onClick = { onConfirmer(pin) }) { Text("Réinitialiser") } },
        dismissButton = { TextButton(onClick = onAnnuler) { Text("Annuler") } }
    )
}

package com.lonaloto.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.lonaloto.domain.auth.Permission
import com.lonaloto.domain.auth.Permissions
import com.lonaloto.domain.auth.SessionManager
import com.lonaloto.ui.audit.AuditScreen
import com.lonaloto.ui.parametres.ParametresScreen
import com.lonaloto.ui.rapports.RapportScreen
import com.lonaloto.ui.saisie.SaisieVenteScreen
import com.lonaloto.ui.utilisateurs.UtilisateursScreen

private data class OngletBas(val route: String, val label: String, val icone: androidx.compose.ui.graphics.vector.ImageVector, val permissionRequise: Permission?)

private val ONGLETS = listOf(
    OngletBas(Destinations.ACCUEIL_TAB, "Accueil", Icons.Filled.Home, permissionRequise = null),
    OngletBas(Destinations.SAISIE, "Saisie", Icons.Filled.EditNote, Permission.SAISIR_VENTES_PERSONNELLES),
    OngletBas(Destinations.RAPPORTS, "Rapports", Icons.Filled.Assessment, Permission.VOIR_BILAN_PERSONNEL),
    OngletBas(Destinations.PARAMETRES, "Paramètres", Icons.Filled.Settings, Permission.MODIFIER_TAUX),
    OngletBas(Destinations.UTILISATEURS, "Utilisateurs", Icons.Filled.People, Permission.GERER_UTILISATEURS),
    OngletBas(Destinations.AUDIT, "Audit", Icons.Filled.History, Permission.VOIR_JOURNAL_AUDIT)
)

/**
 * Coquille principale post-connexion : onglets en bas, filtrés selon les
 * permissions du rôle connecté (ex: un VENDEUR ne voit pas "Paramètres").
 */
@Composable
fun EcranPrincipal(sessionManager: SessionManager, accueilContent: @Composable () -> Unit) {
    val session by sessionManager.utilisateurConnecte.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val routeActuelle = backStackEntry?.destination?.route

    val ongletsVisibles = ONGLETS.filter { onglet ->
        onglet.permissionRequise == null || Permissions.autorise(session, onglet.permissionRequise)
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                ongletsVisibles.forEach { onglet ->
                    NavigationBarItem(
                        selected = routeActuelle == onglet.route,
                        onClick = {
                            navController.navigate(onglet.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(onglet.icone, contentDescription = onglet.label) },
                        label = { Text(onglet.label) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Destinations.ACCUEIL_TAB,
            modifier = Modifier.padding(padding)
        ) {
            composable(Destinations.ACCUEIL_TAB) { accueilContent() }
            composable(Destinations.SAISIE) { SaisieVenteScreen() }
            composable(Destinations.RAPPORTS) { RapportScreen() }
            composable(Destinations.PARAMETRES) { ParametresScreen() }
            composable(Destinations.UTILISATEURS) { UtilisateursScreen() }
            composable(Destinations.AUDIT) { AuditScreen() }
        }
    }
}

package com.lonaloto.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lonaloto.data.local.entities.Role
import com.lonaloto.domain.auth.SessionManager
import com.lonaloto.ui.connexion.LoginScreen

@Composable
fun LonalotoNavGraph(
    sessionManager: SessionManager,
    navController: NavHostController = rememberNavController()
) {
    NavHost(navController = navController, startDestination = Destinations.CONNEXION) {

        composable(Destinations.CONNEXION) {
            LoginScreen(
                onConnexionReussie = {
                    val session = sessionManager.utilisateurConnecte.value
                    val destination = when (session?.role) {
                        Role.ADMIN -> Destinations.ACCUEIL_ADMIN
                        Role.CHEF_DE_FLOTTE -> Destinations.ACCUEIL_CHEF_FLOTTE
                        Role.VENDEUR -> Destinations.ACCUEIL_VENDEUR
                        null -> Destinations.CONNEXION
                    }
                    navController.navigate(destination) {
                        // Empêche de revenir à l'écran de connexion avec le bouton "retour"
                        popUpTo(Destinations.CONNEXION) { inclusive = true }
                    }
                }
            )
        }

        // Chaque rôle arrive sur EcranPrincipal (onglets bas filtrés par permission),
        // mais avec un contenu d'accueil différent — l'ADMIN aura un dashboard
        // transverse (multi-activités), les autres un dashboard centré sur leur activité.
        composable(Destinations.ACCUEIL_ADMIN) {
            EcranPrincipal(sessionManager = sessionManager) {
                TableauDeBordPlaceholder(titre = "Dashboard ADMIN — accès total")
            }
        }
        composable(Destinations.ACCUEIL_CHEF_FLOTTE) {
            EcranPrincipal(sessionManager = sessionManager) {
                TableauDeBordPlaceholder(titre = "Dashboard CHEF DE FLOTTE")
            }
        }
        composable(Destinations.ACCUEIL_VENDEUR) {
            EcranPrincipal(sessionManager = sessionManager) {
                TableauDeBordPlaceholder(titre = "Dashboard VENDEUR")
            }
        }
    }
}

@Composable
private fun TableauDeBordPlaceholder(titre: String) {
    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Text(text = titre, style = MaterialTheme.typography.headlineSmall)
    }
}

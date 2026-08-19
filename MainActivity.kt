package com.lonaloto.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.lonaloto.domain.auth.SessionManager
import com.lonaloto.ui.navigation.LonalotoNavGraph
import com.lonaloto.ui.theme.LonalotoTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LonalotoTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    LonalotoNavGraph(sessionManager = sessionManager)
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        // Sécurité terrain : si l'app passe en arrière-plan, on referme la session.
        // Évite qu'un téléphone partagé entre plusieurs vendeurs reste connecté
        // sous le mauvais compte. Peut être rendu configurable via Parametres plus tard.
        sessionManager.fermerSession()
    }
}

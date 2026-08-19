package com.lonaloto.ui.audit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lonaloto.data.local.dao.HistoriqueAuditDao
import com.lonaloto.data.local.dao.UtilisateurDao
import com.lonaloto.data.local.entities.HistoriqueAudit
import com.lonaloto.data.local.entities.Utilisateur
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class LigneAudit(
    val entree: HistoriqueAudit,
    val nomUtilisateur: String
)

@HiltViewModel
class AuditViewModel @Inject constructor(
    private val historiqueAuditDao: HistoriqueAuditDao,
    private val utilisateurDao: UtilisateurDao
) : ViewModel() {

    val lignes: StateFlow<List<LigneAudit>> = combine(
        historiqueAuditDao.tout(),
        utilisateurDao.tousActifs()
    ) { entrees, utilisateurs ->
        val nomsParId = utilisateurs.associateBy(Utilisateur::id)
        entrees.map { entree ->
            LigneAudit(
                entree = entree,
                nomUtilisateur = nomsParId[entree.utilisateurId]?.nom ?: "Utilisateur #${entree.utilisateurId}"
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

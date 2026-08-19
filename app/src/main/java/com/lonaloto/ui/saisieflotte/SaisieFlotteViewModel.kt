package com.lonaloto.ui.saisieflotte

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lonaloto.data.local.entities.Utilisateur
import com.lonaloto.data.local.entities.Vente
import com.lonaloto.data.repository.MoisRepository
import com.lonaloto.data.repository.ResultatSaisie
import com.lonaloto.data.repository.UtilisateurRepository
import com.lonaloto.data.repository.VenteRepository
import com.lonaloto.domain.auth.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

data class LigneEnAttente(val vente: Vente, val nomVendeur: String)

data class EtatSaisieFlotte(
    val vendeurs: List<Utilisateur> = emptyList(),
    val vendeurSelectionneId: Long? = null,
    val recette: String = "",
    val paiement: String = "",
    val enCours: Boolean = false,
    val messageErreur: String? = null,
    val messageSucces: String? = null,
    val enAttenteDeValidation: List<LigneEnAttente> = emptyList()
)

/**
 * Écran du CHEF DE FLOTTE : saisir la vente d'un vendeur de sa flotte (ex: vendeur
 * qui n'a pas de téléphone / n'a pas encore saisi lui-même) et valider les saisies
 * en attente. Utilise les mêmes repositories que l'écran Saisie personnel — seule
 * la cible (vendeurId choisi plutôt que soi-même) change.
 */
@HiltViewModel
class SaisieFlotteViewModel @Inject constructor(
    private val venteRepository: VenteRepository,
    private val utilisateurRepository: UtilisateurRepository,
    private val moisRepository: MoisRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val recette = MutableStateFlow("")
    private val paiement = MutableStateFlow("")
    private val vendeurSelectionneId = MutableStateFlow<Long?>(null)
    private val messages = MutableStateFlow<Pair<String?, String?>>(null to null)
    private val enCours = MutableStateFlow(false)

    private val activiteId: Long? get() = sessionManager.utilisateurConnecte.value?.activiteId
    private val auteurId: Long get() = sessionManager.utilisateurConnecte.value?.id ?: 0L

    private val vendeursFlow = activiteId?.let { utilisateurRepository.parActivite(it) } ?: flowOf(emptyList())

    private val enAttenteFlow: Flow<List<LigneEnAttente>> = combine(
        vendeursFlow,
        flow {
            val id = activiteId
            if (id != null) {
                val mois = moisRepository.moisCourant(auteurId)
                emitAll(venteRepository.ventesNonValidees(id, mois.id))
            } else {
                emit(emptyList())
            }
        }
    ) { vendeurs, ventes ->
        val nomsParId = vendeurs.associateBy(Utilisateur::id)
        ventes.map { vente -> LigneEnAttente(vente, nomsParId[vente.vendeurId]?.nom ?: "Vendeur #${vente.vendeurId}") }
    }

    val etat: StateFlow<EtatSaisieFlotte> = combine(
        vendeursFlow, vendeurSelectionneId, recette, paiement, enCours, messages, enAttenteFlow
    ) { valeurs ->
        @Suppress("UNCHECKED_CAST")
        val vendeurs = valeurs[0] as List<Utilisateur>
        val selectionId = valeurs[1] as Long?
        val r = valeurs[2] as String
        val p = valeurs[3] as String
        val chargement = valeurs[4] as Boolean
        val (erreur, succes) = valeurs[5] as Pair<String?, String?>
        @Suppress("UNCHECKED_CAST")
        val enAttente = valeurs[6] as List<LigneEnAttente>

        EtatSaisieFlotte(
            vendeurs = vendeurs,
            vendeurSelectionneId = selectionId ?: vendeurs.firstOrNull()?.id,
            recette = r,
            paiement = p,
            enCours = chargement,
            messageErreur = erreur,
            messageSucces = succes,
            enAttenteDeValidation = enAttente
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), EtatSaisieFlotte())

    fun selectionnerVendeur(id: Long) { vendeurSelectionneId.value = id }
    fun onRecetteChange(v: String) { recette.value = v.filter { it.isDigit() }.take(9) }
    fun onPaiementChange(v: String) { paiement.value = v.filter { it.isDigit() }.take(9) }

    fun enregistrer() {
        val idActivite = activiteId
        val idVendeur = etat.value.vendeurSelectionneId
        val rec = recette.value.toDoubleOrNull()
        val pai = paiement.value.toDoubleOrNull()

        if (idActivite == null || idVendeur == null) {
            messages.value = "Aucun vendeur sélectionné" to null
            return
        }
        if (rec == null || pai == null) {
            messages.value = "Recette et paiement doivent être des nombres valides" to null
            return
        }

        viewModelScope.launch {
            enCours.value = true
            val mois = moisRepository.moisCourant(auteurId)

            when (val resultat = venteRepository.saisir(
                activiteId = idActivite,
                moisId = mois.id,
                vendeurId = idVendeur,
                date = Date(),
                recette = rec,
                paiement = pai,
                saisiParId = auteurId
            )) {
                is ResultatSaisie.Succes -> {
                    recette.value = ""
                    paiement.value = ""
                    messages.value = null to "Saisie enregistrée pour ce vendeur"
                }
                is ResultatSaisie.MoisCloture -> messages.value = "Ce mois est clôturé" to null
                is ResultatSaisie.Erreur -> messages.value = resultat.message to null
            }
            enCours.value = false
        }
    }

    fun valider(venteId: Long) {
        viewModelScope.launch {
            venteRepository.valider(venteId, auteurId)
            messages.value = null to "Saisie validée"
        }
    }
}

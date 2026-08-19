package com.lonaloto.ui.rapports

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.NumberFormat
import java.util.Locale

private val formatFcfa: NumberFormat = NumberFormat.getNumberInstance(Locale.FRANCE)

private fun fcfa(valeur: Double): String = "${formatFcfa.format(valeur)} FCFA"

@Composable
fun RapportScreen(
    viewModel: RapportViewModel = hiltViewModel()
) {
    val etat by viewModel.etat.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Dès qu'un fichier est prêt, ouvre le sélecteur "Partager/Ouvrir avec" du système.
    LaunchedEffect(etat.fichierAExporter) {
        etat.fichierAExporter?.let { fichier ->
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", fichier)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = if (fichier.extension == "pdf") "application/pdf"
                       else "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Partager le rapport"))
            viewModel.fichierPartage()
        }
    }

    when {
        etat.chargement -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        etat.messageErreur != null -> {
            Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Text(etat.messageErreur!!, color = MaterialTheme.colorScheme.error)
            }
        }
        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(24.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Bilan — ${etat.libelleMois}",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        if (etat.peutExporter) {
                            Row {
                                TextButton(onClick = viewModel::exporterPdf) { Text("PDF") }
                                TextButton(onClick = viewModel::exporterExcel) { Text("Excel") }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                etat.bilanMensuel?.let { bilan ->
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Calcul de la paie du mois", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(12.dp))
                                LigneBilan("Total recette", fcfa(bilan.totalRecette))
                                LigneBilan("Total paiement", fcfa(bilan.totalPaiement))
                                Divider(modifier = Modifier.padding(vertical = 8.dp))
                                LigneBilan("Montant total", fcfa(bilan.montantTotal))
                                LigneBilan("Montant total LONACI", fcfa(bilan.montantTotalLonaci))
                                LigneBilan("Point paiement", fcfa(bilan.pointPaiement))
                                LigneBilan("Salaire coupeur", fcfa(bilan.salaireCoupeur))
                                if (bilan.montantBonus > 0) {
                                    LigneBilan("Bonus appliqué", fcfa(bilan.montantBonus))
                                }
                                Divider(modifier = Modifier.padding(vertical = 8.dp))
                                LigneBilan(
                                    "Salaire mensuel",
                                    fcfa(bilan.salaireMensuelFinal),
                                    accent = true
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                item {
                    Text("Détail par semaine", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                }

                items(etat.bilanParSemaine.size) { index ->
                    val semaine = etat.bilanParSemaine[index]
                    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Semaine ${index + 1}", fontWeight = FontWeight.Bold)
                            LigneBilan("Recette", fcfa(semaine.totalRecette))
                            LigneBilan("Paiement", fcfa(semaine.totalPaiement))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LigneBilan(libelle: String, valeur: String, accent: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(libelle, style = if (accent) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium)
        Text(
            valeur,
            style = if (accent) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            fontWeight = if (accent) FontWeight.Bold else FontWeight.Normal,
            color = if (accent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

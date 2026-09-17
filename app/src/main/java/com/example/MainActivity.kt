package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MyApplicationTheme
import java.util.Locale

// 1. Modelo de dados para armazenar as propriedades nutricionais fixas de cada produto
data class NutrientesBase(
    val nome: String,
    val porcaoRef: Double,
    val kcal: Double,
    val carboidratos: Double,
    val proteinas: Double,
    val gordurasTotais: Double,
    val gordurasSaturadas: Double,
    val fibras: Double,
    val sodio: Double
)

// 2. Estado mutável para capturar os dados dinâmicos inseridos pelo usuário no app
class ProdutoEstado(
    val base: NutrientesBase,
    quantidadeUsadaInit: String = "",
    precoEmbalagemInit: String = "",
    pesoEmbalagemInit: String = ""
) {
    var quantidadeUsada by mutableStateOf(quantidadeUsadaInit)
    var precoEmbalagem by mutableStateOf(precoEmbalagemInit)
    var pesoEmbalagem by mutableStateOf(pesoEmbalagemInit)

    val quantidadeG: Double
        get() = quantidadeUsada.trim().replace(",", ".").toDoubleOrNull() ?: 0.0

    val precoRS: Double
        get() = precoEmbalagem.trim().replace(",", ".").toDoubleOrNull() ?: 0.0

    val `precoR$`: Double
        get() = precoRS

    val pesoG: Double
        get() = pesoEmbalagem.trim().replace(",", ".").toDoubleOrNull() ?: 0.0
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CalculadoraScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CalculadoraScreen() {
    // Lista de produtos padrão especificada
    val listaProdutos = remember {
        mutableStateListOf(
            ProdutoEstado(NutrientesBase("Farinha de Amendoim", 20.0, 125.0, 7.4, 4.7, 9.0, 3.2, 1.6, 0.0)),
            ProdutoEstado(NutrientesBase("Extrato de Soja", 45.0, 207.0, 14.9, 16.5, 9.1, 0.0, 9.0, 21.0)),
            ProdutoEstado(NutrientesBase("Leite de Coco em Pó", 10.0, 54.0, 1.0, 0.6, 5.3, 5.3, 0.0, 16.0)),
            ProdutoEstado(NutrientesBase("Farinha de Aveia", 100.0, 324.0, 60.0, 12.0, 6.0, 1.4, 10.0, 12.0)),
            ProdutoEstado(NutrientesBase("Achocolatado em Pó", 20.0, 80.0, 17.0, 0.7, 0.5, 0.2, 0.8, 25.0))
        )
    }

    var porcaoConsumoInput by remember { mutableStateOf("30") }
    val porcaoConsumo by remember {
        derivedStateOf {
            porcaoConsumoInput.trim().replace(",", ".").toDoubleOrNull() ?: 30.0
        }
    }

    // Modal para adicionar ingrediente personalizado
    var showAddDialog by remember { mutableStateOf(false) }

    // Cálculos em tempo real baseados no peso total da mistura
    val pesoTotalMistura by remember {
        derivedStateOf { listaProdutos.sumOf { it.quantidadeG } }
    }

    val totaisNutrientes by remember {
        derivedStateOf {
            val totais = mutableMapOf(
                "kcal" to 0.0, "carbo" to 0.0, "prot" to 0.0,
                "gord" to 0.0, "sat" to 0.0, "fibra" to 0.0, "sodio" to 0.0
            )
            listaProdutos.forEach { p ->
                if (p.quantidadeG > 0.0 && p.base.porcaoRef > 0.0) {
                    val fator = p.quantidadeG / p.base.porcaoRef
                    totais["kcal"] = totais["kcal"]!! + (p.base.kcal * fator)
                    totais["carbo"] = totais["carbo"]!! + (p.base.carboidratos * fator)
                    totais["prot"] = totais["prot"]!! + (p.base.proteinas * fator)
                    totais["gord"] = totais["gord"]!! + (p.base.gordurasTotais * fator)
                    totais["sat"] = totais["sat"]!! + (p.base.gordurasSaturadas * fator)
                    totais["fibra"] = totais["fibra"]!! + (p.base.fibras * fator)
                    totais["sodio"] = totais["sodio"]!! + (p.base.sodio * fator)
                }
            }
            totais
        }
    }

    val custoTotalMistura by remember {
        derivedStateOf {
            listaProdutos.sumOf { p ->
                if (p.quantidadeG > 0.0 && p.pesoG > 0.0) {
                    (p.precoRS / p.pesoG) * p.quantidadeG
                } else 0.0
            }
        }
    }

    val custoPorPorcao by remember {
        derivedStateOf {
            if (pesoTotalMistura > 0.0 && porcaoConsumo > 0.0) {
                (custoTotalMistura / pesoTotalMistura) * porcaoConsumo
            } else 0.0
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                "Mistura Nutricional",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                "Calculadora de Nutrientes & Custo",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            // Preencher exemplo prático
                            listaProdutos.forEachIndexed { index, p ->
                                when (index) {
                                    0 -> { // Farinha de Amendoim
                                        p.quantidadeUsada = "100"
                                        p.precoEmbalagem = "15.00"
                                        p.pesoEmbalagem = "500"
                                    }
                                    1 -> { // Extrato de Soja
                                        p.quantidadeUsada = "150"
                                        p.precoEmbalagem = "12.00"
                                        p.pesoEmbalagem = "400"
                                    }
                                    2 -> { // Leite de Coco
                                        p.quantidadeUsada = "50"
                                        p.precoEmbalagem = "18.00"
                                        p.pesoEmbalagem = "200"
                                    }
                                    3 -> { // Aveia
                                        p.quantidadeUsada = "200"
                                        p.precoEmbalagem = "8.50"
                                        p.pesoEmbalagem = "500"
                                    }
                                    4 -> { // Achocolatado
                                        p.quantidadeUsada = "50"
                                        p.precoEmbalagem = "11.00"
                                        p.pesoEmbalagem = "400"
                                    }
                                }
                            }
                        },
                        modifier = Modifier.testTag("action_fill_example")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Restaurant,
                            contentDescription = "Preencher Exemplo",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = {
                            listaProdutos.forEach {
                                it.quantidadeUsada = ""
                                it.precoEmbalagem = ""
                                it.pesoEmbalagem = ""
                            }
                        },
                        modifier = Modifier.testTag("action_clear_all")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ClearAll,
                            contentDescription = "Limpar Tudo",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Banner explicativo com métrica rápida
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Grain,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "Calcule os valores de sua mistura",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            "Insira quanto usará na receita e os dados da embalagem para obter tabela nutricional e custo exato por porção.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Cabeçalho de Ingredientes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Ingredientes da Mistura",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Gramas Usados / Preço Pago / Peso Embalagem",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                OutlinedButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.testTag("btn_add_ingredient")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Adicionar", fontSize = 12.sp)
                }
            }

            // Cards para cada produto
            listaProdutos.forEachIndexed { index, produto ->
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_product_$index"),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    produto.base.nome,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    "Ref: ${String.format(Locale.US, "%.0fg", produto.base.porcaoRef)} = ${String.format(Locale.US, "%.0f", produto.base.kcal)} kcal (Prot: ${String.format(Locale.US, "%.1fg", produto.base.proteinas)} | Carbo: ${String.format(Locale.US, "%.1fg", produto.base.carboidratos)})",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (listaProdutos.size > 1) {
                                IconButton(
                                    onClick = { listaProdutos.removeAt(index) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Remover ingrediente",
                                        tint = MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = produto.quantidadeUsada,
                                onValueChange = { produto.quantidadeUsada = it },
                                label = { Text("Usado (g)") },
                                placeholder = { Text("Ex: 100") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Decimal,
                                    imeAction = ImeAction.Next
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_used_$index")
                            )
                            OutlinedTextField(
                                value = produto.precoEmbalagem,
                                onValueChange = { produto.precoEmbalagem = it },
                                label = { Text("Preço (R$)") },
                                placeholder = { Text("Ex: 15.00") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Decimal,
                                    imeAction = ImeAction.Next
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_price_$index")
                            )
                            OutlinedTextField(
                                value = produto.pesoEmbalagem,
                                onValueChange = { produto.pesoEmbalagem = it },
                                label = { Text("Pacote (g)") },
                                placeholder = { Text("Ex: 500") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Decimal,
                                    imeAction = ImeAction.Done
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_pack_weight_$index")
                            )
                        }

                        // Indicador de custo proporcional do item se preenchido
                        if (produto.quantidadeG > 0.0 && produto.pesoG > 0.0 && produto.precoRS > 0.0) {
                            val custoItem = (produto.precoRS / produto.pesoG) * produto.quantidadeG
                            Text(
                                text = "Custo nesta mistura: ${String.format(Locale.US, "R$ %.2f", custoItem)} (${String.format(Locale.US, "%.1f", (produto.quantidadeG / produto.base.porcaoRef) * produto.base.kcal)} kcal adicionadas)",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Seção: Configuração da porção diária de consumo
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Scale,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Porção Diária de Consumo",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    OutlinedTextField(
                        value = porcaoConsumoInput,
                        onValueChange = { porcaoConsumoInput = it },
                        label = { Text("Tamanho da sua porção diária de consumo (g)") },
                        placeholder = { Text("Ex: 30") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_portion_size")
                    )

                    // Atalhos rápidos para porções padrão
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("20", "30", "40", "50", "60").forEach { preset ->
                            FilterChip(
                                selected = porcaoConsumoInput == preset,
                                onClick = { porcaoConsumoInput = preset },
                                label = { Text("${preset}g") },
                                modifier = Modifier.testTag("chip_portion_$preset")
                            )
                        }
                    }
                }
            }

            // Resultados em Tempo Real
            if (pesoTotalMistura > 0.0) {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + slideInVertically()
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("card_results"),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.FitnessCenter,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Resultados da Mistura",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }

                            // Métricas principais de peso e custo
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                MetricHighlightBox(
                                    title = "Peso Total",
                                    value = "${String.format(Locale.US, "%.1f", pesoTotalMistura)}g",
                                    subtitle = "${String.format(Locale.US, "%.0f", pesoTotalMistura / porcaoConsumo)} porções de ${String.format(Locale.US, "%.0f", porcaoConsumo)}g",
                                    modifier = Modifier.weight(1f)
                                )
                                MetricHighlightBox(
                                    title = "Custo Total",
                                    value = String.format(Locale.US, "R$ %.2f", custoTotalMistura),
                                    subtitle = "Toda a receita",
                                    modifier = Modifier.weight(1f)
                                )
                                MetricHighlightBox(
                                    title = "Por Porção",
                                    value = String.format(Locale.US, "R$ %.2f", custoPorPorcao),
                                    subtitle = "Porção de ${String.format(Locale.US, "%.0f", porcaoConsumo)}g",
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 4.dp),
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.2f)
                            )

                            // Tabela Nutricional Estilizada
                            Text(
                                "Tabela Nutricional (Porção de ${String.format(Locale.US, "%.0f", porcaoConsumo)}g):",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )

                            val exibirNutrientes = listOf(
                                "Valor Energético" to ("kcal" to "kcal"),
                                "Carboidratos" to ("carbo" to "g"),
                                "Proteínas" to ("prot" to "g"),
                                "Gorduras Totais" to ("gord" to "g"),
                                "Gorduras Saturadas" to ("sat" to "g"),
                                "Fibra Alimentar" to ("fibra" to "g"),
                                "Sódio" to ("sodio" to "mg")
                            )

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.outlineVariant,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    exibirNutrientes.forEachIndexed { idx, (nome, keys) ->
                                        val (chave, unidade) = keys
                                        val totalAbsoluto = totaisNutrientes[chave] ?: 0.0
                                        val valorPorcao = (totalAbsoluto / pesoTotalMistura) * porcaoConsumo

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                nome,
                                                fontSize = 14.sp,
                                                fontWeight = if (chave == "kcal" || chave == "prot") FontWeight.Bold else FontWeight.Normal,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                "${String.format(Locale.US, "%.1f", valorPorcao)} $unidade",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (chave == "prot") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                            )
                                        }

                                        if (idx < exibirNutrientes.size - 1) {
                                            HorizontalDivider(
                                                thickness = 0.5.dp,
                                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Insira a quantidade usada (g) em pelo menos um ingrediente para ver a tabela nutricional e o custo da mistura.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Diálogo para Adicionar Novo Ingrediente
    if (showAddDialog) {
        AddIngredientDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { novoIngrediente ->
                listaProdutos.add(ProdutoEstado(novoIngrediente))
                showAddDialog = false
            }
        )
    }
}

@Composable
fun MetricHighlightBox(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                title,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                subtitle,
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
fun AddIngredientDialog(
    onDismiss: () -> Unit,
    onAdd: (NutrientesBase) -> Unit
) {
    var nome by remember { mutableStateOf("") }
    var porcaoRef by remember { mutableStateOf("100") }
    var kcal by remember { mutableStateOf("") }
    var carbo by remember { mutableStateOf("") }
    var prot by remember { mutableStateOf("") }
    var gord by remember { mutableStateOf("") }
    var sat by remember { mutableStateOf("") }
    var fibra by remember { mutableStateOf("") }
    var sodio by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adicionar Ingrediente") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it },
                    label = { Text("Nome do Ingrediente") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = porcaoRef,
                    onValueChange = { porcaoRef = it },
                    label = { Text("Porção de Referência (g)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = kcal,
                        onValueChange = { kcal = it },
                        label = { Text("Kcal") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = carbo,
                        onValueChange = { carbo = it },
                        label = { Text("Carbo (g)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = prot,
                        onValueChange = { prot = it },
                        label = { Text("Proteína (g)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = gord,
                        onValueChange = { gord = it },
                        label = { Text("Gorduras (g)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = sat,
                        onValueChange = { sat = it },
                        label = { Text("Saturadas (g)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = fibra,
                        onValueChange = { fibra = it },
                        label = { Text("Fibras (g)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = sodio,
                    onValueChange = { sodio = it },
                    label = { Text("Sódio (mg)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nome.isNotBlank()) {
                        val novo = NutrientesBase(
                            nome = nome.trim(),
                            porcaoRef = porcaoRef.trim().replace(",", ".").toDoubleOrNull() ?: 100.0,
                            kcal = kcal.trim().replace(",", ".").toDoubleOrNull() ?: 0.0,
                            carboidratos = carbo.trim().replace(",", ".").toDoubleOrNull() ?: 0.0,
                            proteinas = prot.trim().replace(",", ".").toDoubleOrNull() ?: 0.0,
                            gordurasTotais = gord.trim().replace(",", ".").toDoubleOrNull() ?: 0.0,
                            gordurasSaturadas = sat.trim().replace(",", ".").toDoubleOrNull() ?: 0.0,
                            fibras = fibra.trim().replace(",", ".").toDoubleOrNull() ?: 0.0,
                            sodio = sodio.trim().replace(",", ".").toDoubleOrNull() ?: 0.0
                        )
                        onAdd(novo)
                    }
                },
                enabled = nome.isNotBlank()
            ) {
                Text("Adicionar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}

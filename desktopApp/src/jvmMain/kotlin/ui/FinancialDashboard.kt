package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import database.ExpenseDatabase
import database.Expense
import database.Receita
import java.util.Locale

@Composable
fun FinancialDashboard(database: ExpenseDatabase) {
    var tabIndex by remember { mutableStateOf(0) }
    var expenses by remember { mutableStateOf(database.getAllExpenses()) }
    var receitas by remember { mutableStateOf(database.getAllReceitas()) }
    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var showAddReceitaDialog by remember { mutableStateOf(false) }
    var showConfirmDeleteExpense by remember { mutableStateOf(false) }
    var showConfirmDeleteReceita by remember { mutableStateOf(false) }
    var expenseToDelete by remember { mutableStateOf<Expense?>(null) }
    var receitaToDelete by remember { mutableStateOf<Receita?>(null) }
    var showConfirmStatusExpense by remember { mutableStateOf(false) }
    var showConfirmStatusReceita by remember { mutableStateOf(false) }
    var expenseToChangeStatus by remember { mutableStateOf<Expense?>(null) }
    var receitaToChangeStatus by remember { mutableStateOf<Receita?>(null) }

    // Recalcular totais em tempo real baseado nas listas de despesas e receitas
    val totalDespesas = expenses.sumOf { it.valor }
    val totalDespesasPagas = expenses.filter { it.status == "Pago" }.sumOf { it.valor }
    val totalDespesasPendentes = expenses.filter { it.status == "Aguardando" }.sumOf { it.valor }
    val totalReceitas = receitas.sumOf { it.valor }
    val totalReceitasRecebidas = receitas.filter { it.status == "Recebido" }.sumOf { it.valor }
    val totalReceitasPendentes = receitas.filter { it.status == "Pendente" }.sumOf { it.valor }
    val saldo = totalReceitasRecebidas - totalDespesas

    val brazilLocale = Locale.forLanguageTag("pt-BR")
    fun formatCurrency(value: Double): String {
        return java.text.NumberFormat.getCurrencyInstance(brazilLocale).format(value)
    }

    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFEEF2F7))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ===== HEADER COM TÍTULO =====
            Text(
                text = "💰 Controle de Gastos",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF065F46)
            )

            // ===== TABS =====
            TabRow(
                selectedTabIndex = tabIndex,
                backgroundColor = Color.Transparent,
                contentColor = Color(0xFF059669)
            ) {
                Tab(
                    selected = tabIndex == 0,
                    onClick = { tabIndex = 0 },
                    text = { Text("💸 Despesas") }
                )
                Tab(
                    selected = tabIndex == 1,
                    onClick = { tabIndex = 1 },
                    text = { Text("💰 Receitas") }
                )
                Tab(
                    selected = tabIndex == 2,
                    onClick = { tabIndex = 2 },
                    text = { Text("📊 Resumo") }
                )
            }

            // ===== CONTEÚDO DAS ABAS =====
            Box(modifier = Modifier.weight(1f)) {
                when (tabIndex) {
                    0 -> ExpenseListSection(
                        expenses = expenses,
                        onAddClick = { showAddExpenseDialog = true },
                        onStatusToggle = { expense ->
                            expenseToChangeStatus = expense
                            showConfirmStatusExpense = true
                        },
                        onDelete = { expense ->
                            expenseToDelete = expense
                            showConfirmDeleteExpense = true
                        }
                    )
                    1 -> ReceitaListPanel(
                        receitas = receitas,
                        onAddClick = { showAddReceitaDialog = true },
                        onStatusToggle = { receita ->
                            receitaToChangeStatus = receita
                            showConfirmStatusReceita = true
                        },
                        onDelete = { receita ->
                            receitaToDelete = receita
                            showConfirmDeleteReceita = true
                        }
                    )
                    2 -> SummaryPanel(
                        totalDespesas = totalDespesas,
                        totalDespesasPagas = totalDespesasPagas,
                        totalDespesasPendentes = totalDespesasPendentes,
                        totalReceitas = totalReceitas,
                        totalReceitasRecebidas = totalReceitasRecebidas,
                        totalReceitasPendentes = totalReceitasPendentes,
                        saldo = saldo,
                        formatCurrency = { formatCurrency(it) }
                    )
                }
            }

            // ===== DIÁLOGOS =====
            if (showAddExpenseDialog) {
                AddExpenseDialog(
                    database = database,
                    onDismiss = { showAddExpenseDialog = false },
                    onSave = {
                        expenses = database.getAllExpenses()
                        showAddExpenseDialog = false
                    }
                )
            }

            if (showAddReceitaDialog) {
                AddReceitaDialog(
                    database = database,
                    onDismiss = { showAddReceitaDialog = false },
                    onSave = {
                        receitas = database.getAllReceitas()
                        showAddReceitaDialog = false
                    }
                )
            }

            // Diálogo de confirmação de exclusão de despesa
            if (showConfirmDeleteExpense && expenseToDelete != null) {
                ConfirmationDialog(
                    onDismiss = { showConfirmDeleteExpense = false },
                    onConfirm = {
                        expenseToDelete?.let { expense ->
                            database.deleteExpense(expense.id)
                            expenses = database.getAllExpenses()
                        }
                        showConfirmDeleteExpense = false
                    },
                    title = "Excluir Despesa",
                    message = "Tem certeza que deseja excluir esta despesa?",
                    confirmButtonText = "Excluir",
                    dismissButtonText = "Cancelar"
                )
            }

            // Diálogo de confirmação de exclusão de receita
            if (showConfirmDeleteReceita && receitaToDelete != null) {
                ConfirmationDialog(
                    onDismiss = { showConfirmDeleteReceita = false },
                    onConfirm = {
                        receitaToDelete?.let { receita ->
                            database.deleteReceita(receita.id)
                            receitas = database.getAllReceitas()
                        }
                        showConfirmDeleteReceita = false
                    },
                    title = "Excluir Receita",
                    message = "Tem certeza que deseja excluir esta receita?",
                    confirmButtonText = "Excluir",
                    dismissButtonText = "Cancelar"
                )
            }

            // Diálogo de confirmação de mudança de status de despesa
            if (showConfirmStatusExpense && expenseToChangeStatus != null) {
                ConfirmationDialog(
                    onDismiss = { showConfirmStatusExpense = false },
                    onConfirm = {
                        expenseToChangeStatus?.let { expense ->
                            val newStatus = if (expense.status == "Pago") "Aguardando" else "Pago"
                            database.updateExpenseStatus(expense.id, newStatus)
                            expenses = database.getAllExpenses()
                        }
                        showConfirmStatusExpense = false
                    },
                    title = "Alterar Status de Despesa",
                    message = "Deseja alterar o status desta despesa?",
                    confirmButtonText = "Confirmar",
                    dismissButtonText = "Cancelar"
                )
            }

            // Diálogo de confirmação de mudança de status de receita
            if (showConfirmStatusReceita && receitaToChangeStatus != null) {
                ConfirmationDialog(
                    onDismiss = { showConfirmStatusReceita = false },
                    onConfirm = {
                        receitaToChangeStatus?.let { receita ->
                            val newStatus = if (receita.status == "Recebido") "Pendente" else "Recebido"
                            database.updateReceitaStatus(receita.id, newStatus)
                            receitas = database.getAllReceitas()
                        }
                        showConfirmStatusReceita = false
                    },
                    title = "Alterar Status de Receita",
                    message = "Deseja alterar o status desta receita?",
                    confirmButtonText = "Confirmar",
                    dismissButtonText = "Cancelar"
                )
            }
        }
    }
}

@Composable
private fun DashboardCard(
    title: String,
    value: String,
    bgColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = bgColor,
        modifier = modifier.height(100.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                color = textColor.copy(alpha = 0.8f),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = value,
                color = textColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}

@Composable
fun ReceitaListPanel(
    receitas: List<Receita>,
    onAddClick: () -> Unit,
    onStatusToggle: (Receita) -> Unit,
    onDelete: (Receita) -> Unit
) {
    val brazilLocale = Locale.forLanguageTag("pt-BR")
    fun formatCurrency(value: Double): String {
        return java.text.NumberFormat.getCurrencyInstance(brazilLocale).format(value)
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        elevation = 8.dp,
        color = Color.White,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📋 Receitas (${receitas.size})",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF065F46)
                )
                Button(
                    onClick = onAddClick,
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color(0xFF10B981),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(40.dp)
                ) {
                    Text("+ Adicionar Receita", fontWeight = FontWeight.Bold)
                }
            }

            if (receitas.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Nenhuma receita registrada", color = Color(0xFF9CA3AF))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(receitas) { receita ->
                        ReceitaRow(
                            receita = receita,
                            onStatusToggle = { onStatusToggle(receita) },
                            onDelete = { onDelete(receita) },
                            formatCurrency = { formatCurrency(it) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReceitaRow(
    receita: Receita,
    onStatusToggle: () -> Unit,
    onDelete: () -> Unit,
    formatCurrency: (Double) -> String
) {
    val statusColor = if (receita.status == "Recebido") Color(0xFF10B981) else Color(0xFFF59E0B)
    val statusBgColor = if (receita.status == "Recebido") Color(0xFFD1FAE5) else Color(0xFFFEF3C7)

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFFAFAFA),
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = receita.descricao,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF1F2937)
                )
                Text(
                    text = receita.categoria,
                    color = Color(0xFF6B7280),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = formatCurrency(receita.valor),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF2563EB)
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusBgColor
                ) {
                    Text(
                        text = receita.status,
                        color = statusColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(6.dp, 3.dp)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Button(
                        onClick = onStatusToggle,
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = statusColor,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(32.dp)
                            .width(90.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(if (receita.status == "Recebido") "Desfazer" else "Marcar", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    TextButton(onClick = onDelete, modifier = Modifier.height(32.dp)) {
                        Text("🗑️", fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpenseListSection(
    expenses: List<Expense>,
    onAddClick: () -> Unit,
    onStatusToggle: (Expense) -> Unit,
    onDelete: (Expense) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(20.dp),
        elevation = 8.dp,
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📋 Despesas (${expenses.size})",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF065F46)
                )
                Button(
                    onClick = onAddClick,
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color(0xFFEF4444),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(40.dp)
                ) {
                    Text("+ Adicionar Despesa", fontWeight = FontWeight.Bold)
                }
            }

            if (expenses.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Nenhuma despesa registrada", color = Color(0xFF9CA3AF))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(expenses) { expense ->
                        ExpenseItemRow(
                            expense = expense,
                            onStatusToggle = { onStatusToggle(expense) },
                            onDelete = { onDelete(expense) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpenseItemRow(
    expense: Expense,
    onStatusToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val brazilLocale = Locale.forLanguageTag("pt-BR")
    fun formatCurrency(value: Double): String {
        return java.text.NumberFormat.getCurrencyInstance(brazilLocale).format(value)
    }

    val statusColor = if (expense.status == "Pago") Color(0xFF10B981) else Color(0xFFF59E0B)
    val statusBgColor = if (expense.status == "Pago") Color(0xFFD1FAE5) else Color(0xFFFEF3C7)

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFFAFAFA),
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = expense.fornecedor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF1F2937)
                )
                Text(
                    text = expense.categoria,
                    color = Color(0xFF6B7280),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = formatCurrency(expense.valor),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFDC2626)
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusBgColor
                ) {
                    Text(
                        text = expense.status,
                        color = statusColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(6.dp, 3.dp)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Button(
                        onClick = onStatusToggle,
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = statusColor,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(32.dp)
                            .width(90.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(if (expense.status == "Pago") "Desfazer" else "Marcar", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    TextButton(onClick = onDelete, modifier = Modifier.height(32.dp)) {
                        Text("🗑️", fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun AddExpenseDialog(
    database: ExpenseDatabase,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    var fornecedor by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("Alimentação") }
    var valor by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Aguardando") }
    var showError by remember { mutableStateOf("") }

    val categorias = listOf(
        "Alimentação", "Transporte", "Saúde", "Educação",
        "Lazer", "Utilidades", "Trabalho", "Moradia", "Cartão", "Empréstimo", "Outros"
    )
    val statusOptions = listOf("Aguardando", "Pago")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            modifier = Modifier
                .width(500.dp)
                .padding(16.dp),
            elevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = "➕ Adicionar Nova Despesa",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF065F46)
                )

                // Campo Fornecedor
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Fornecedor/Descrição",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF374151)
                    )
                    TextField(
                        value = fornecedor,
                        onValueChange = { fornecedor = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp, max = 120.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.textFieldColors(
                            backgroundColor = Color(0xFFF3F4F6),
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            focusedLabelColor = Color(0xFF059669),
                            textColor = Color.Black
                        ),
                        maxLines = 3
                    )
                }

                // Campo Categoria
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Categoria",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF374151)
                    )
                    var expandedCat by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { expandedCat = !expandedCat },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color(0xFFF3F4F6),
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(categoria, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Start)
                            Text("▼", fontSize = 12.sp)
                        }
                        DropdownMenu(
                            expanded = expandedCat,
                            onDismissRequest = { expandedCat = false },
                            modifier = Modifier.fillMaxWidth(0.95f)
                        ) {
                            categorias.forEach { cat ->
                                DropdownMenuItem(onClick = {
                                    categoria = cat
                                    expandedCat = false
                                }) {
                                    Text(cat)
                                }
                            }
                        }
                    }
                }

                // Campo Valor
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Valor (R$)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF374151)
                    )
                    TextField(
                        value = valor,
                        onValueChange = { newVal ->
                            valor = newVal.filter { it.isDigit() || it == ',' || it == '.' }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp, max = 120.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.textFieldColors(
                            backgroundColor = Color(0xFFF3F4F6),
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            focusedLabelColor = Color(0xFF059669),
                            textColor = Color.Black
                        ),
                        maxLines = 3,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }


                // Campo Status com Cor Dinâmica
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Status",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF374151)
                    )
                    var expandedStatus by remember { mutableStateOf(false) }
                    val statusColor = if (status == "Pago") Color(0xFF10B981) else Color(0xFFFB923C)
                    
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { expandedStatus = !expandedStatus },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = statusColor.copy(alpha = 0.15f),
                                contentColor = statusColor
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(status, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Start)
                            Text("▼", fontSize = 12.sp)
                        }
                        DropdownMenu(
                            expanded = expandedStatus,
                            onDismissRequest = { expandedStatus = false },
                            modifier = Modifier.fillMaxWidth(0.95f)
                        ) {
                            statusOptions.forEach { statusOpt ->
                                DropdownMenuItem(onClick = {
                                    status = statusOpt
                                    expandedStatus = false
                                }) {
                                    Text(statusOpt)
                                }
                            }
                        }
                    }
                }

                // Mensagem de Erro
                if (showError.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFFEDD5),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            showError,
                            color = Color(0xFF92400E),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                // Botões
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color(0xFFE5E7EB),
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancelar", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            if (fornecedor.isBlank() || valor.isBlank()) {
                                showError = "⚠️ Preencha fornecedor e valor"
                            } else {
                                try {
                                    val valorDouble = valor.replace(",", ".").toDouble()
                                    val today = java.time.LocalDate.now()
                                    database.addExpense(
                                        Expense(
                                            fornecedor = fornecedor,
                                            categoria = categoria,
                                            valor = valorDouble,
                                            vencimento = today,
                                            status = status
                                        )
                                    )
                                    onSave()
                                } catch (e: Exception) {
                                    showError = "❌ Erro: ${e.message}"
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color(0xFFEF4444),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Salvar", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun AddReceitaDialog(
    database: ExpenseDatabase,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    var descricao by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("Salário") }
    var valor by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Pendente") }
    var showError by remember { mutableStateOf("") }

    val categorias = listOf(
        "Salário", "Freelance", "Investimentos", "Vendas",
        "Bônus", "Presente", "Devolução", "Outros"
    )
    val statusOptions = listOf("Pendente", "Recebido")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            modifier = Modifier
                .width(500.dp)
                .padding(16.dp),
            elevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = "➕ Adicionar Nova Receita",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF065F46)
                )

                // Campo Descrição
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Descrição",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF374151)
                    )
                    TextField(
                        value = descricao,
                        onValueChange = { descricao = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp, max = 120.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.textFieldColors(
                            backgroundColor = Color(0xFFF3F4F6),
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            focusedLabelColor = Color(0xFF059669),
                            textColor = Color.Black
                        ),
                        maxLines = 3
                    )
                }

                // Campo Categoria
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Categoria",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF374151)
                    )
                    var expandedCat by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { expandedCat = !expandedCat },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color(0xFFF3F4F6),
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(categoria, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Start)
                            Text("▼", fontSize = 12.sp)
                        }
                        DropdownMenu(
                            expanded = expandedCat,
                            onDismissRequest = { expandedCat = false },
                            modifier = Modifier.fillMaxWidth(0.95f)
                        ) {
                            categorias.forEach { cat ->
                                DropdownMenuItem(onClick = {
                                    categoria = cat
                                    expandedCat = false
                                }) {
                                    Text(cat)
                                }
                            }
                        }
                    }
                }

                // Campo Valor
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Valor (R$)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF374151)
                    )
                    TextField(
                        value = valor,
                        onValueChange = { newVal ->
                            valor = newVal.filter { it.isDigit() || it == ',' || it == '.' }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp, max = 120.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.textFieldColors(
                            backgroundColor = Color(0xFFF3F4F6),
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            focusedLabelColor = Color(0xFF059669),
                            textColor = Color.Black
                        ),
                        maxLines = 3,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }


                // Campo Status com Cor Dinâmica
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Status",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF374151)
                    )
                    var expandedStatus by remember { mutableStateOf(false) }
                    val statusColor = if (status == "Recebido") Color(0xFF10B981) else Color(0xFFFB923C)
                    
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { expandedStatus = !expandedStatus },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = statusColor.copy(alpha = 0.15f),
                                contentColor = statusColor
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(status, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Start)
                            Text("▼", fontSize = 12.sp)
                        }
                        DropdownMenu(
                            expanded = expandedStatus,
                            onDismissRequest = { expandedStatus = false },
                            modifier = Modifier.fillMaxWidth(0.95f)
                        ) {
                            statusOptions.forEach { statusOpt ->
                                DropdownMenuItem(onClick = {
                                    status = statusOpt
                                    expandedStatus = false
                                }) {
                                    Text(statusOpt)
                                }
                            }
                        }
                    }
                }

                // Mensagem de Erro
                if (showError.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFFEDD5),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            showError,
                            color = Color(0xFF92400E),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                // Botões
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color(0xFFE5E7EB),
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancelar", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            if (descricao.isBlank() || valor.isBlank()) {
                                showError = "⚠️ Preencha todos os campos"
                            } else {
                                try {
                                    val valorDouble = valor.replace(",", ".").toDouble()
                                    val today = java.time.LocalDate.now()
                                    database.addReceita(
                                        Receita(
                                            descricao = descricao,
                                            categoria = categoria,
                                            valor = valorDouble,
                                            data = today,
                                            status = status
                                        )
                                    )
                                    onSave()
                                } catch (e: Exception) {
                                    showError = "❌ Erro: ${e.message}"
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color(0xFF10B981),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Salvar", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryPanel(
    totalDespesas: Double,
    totalDespesasPagas: Double,
    totalDespesasPendentes: Double,
    totalReceitas: Double,
    totalReceitasRecebidas: Double,
    totalReceitasPendentes: Double,
    saldo: Double,
    formatCurrency: (Double) -> String
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        elevation = 8.dp,
        color = Color.White,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "📊 Resumo Financeiro",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF065F46)
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Seção de Receitas
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "💰 Receitas",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF065F46)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            DashboardCard(
                                title = "Total Receitas",
                                value = formatCurrency(totalReceitas),
                                bgColor = Color(0xFFD1FAE5),
                                textColor = Color(0xFF065F46),
                                modifier = Modifier.weight(1f)
                            )
                            DashboardCard(
                                title = "Recebidas",
                                value = formatCurrency(totalReceitasRecebidas),
                                bgColor = Color(0xFF10B981),
                                textColor = Color.White,
                                modifier = Modifier.weight(1f)
                            )
                            DashboardCard(
                                title = "Pendentes",
                                value = formatCurrency(totalReceitasPendentes),
                                bgColor = Color(0xFFFEF3C7),
                                textColor = Color(0xFF92400E),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Seção de Despesas
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "💸 Despesas",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF065F46)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            DashboardCard(
                                title = "Total Despesas",
                                value = formatCurrency(totalDespesas),
                                bgColor = Color(0xFFFFEDD5),
                                textColor = Color(0xFF92400E),
                                modifier = Modifier.weight(1f)
                            )
                            DashboardCard(
                                title = "Pagas",
                                value = formatCurrency(totalDespesasPagas),
                                bgColor = Color(0xFFDEBEAA),
                                textColor = Color(0xFF7C2D12),
                                modifier = Modifier.weight(1f)
                            )
                            DashboardCard(
                                title = "Pendentes",
                                value = formatCurrency(totalDespesasPendentes),
                                bgColor = Color(0xFFFFB6C1),
                                textColor = Color(0xFF8B0000),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Saldo Líquido
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (saldo >= 0) Color(0xFF10B981) else Color(0xFFEF4444),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "💵 Saldo Líquido",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = formatCurrency(saldo),
                                fontSize = 32.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    title: String,
    message: String,
    confirmButtonText: String = "Confirmar",
    dismissButtonText: String = "Cancelar"
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            modifier = Modifier
                .width(400.dp)
                .padding(16.dp),
            elevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF065F46)
                )
                Text(
                    text = message,
                    fontSize = 14.sp,
                    color = Color(0xFF374151)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color(0xFFE5E7EB),
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(dismissButtonText, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            onConfirm()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color(0xFFEF4444),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(confirmButtonText, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}


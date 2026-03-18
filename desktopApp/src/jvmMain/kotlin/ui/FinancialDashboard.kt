package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import database.ExpenseDatabase
import database.Expense
import database.Receita
import java.util.Locale

@Composable
fun FinancialDashboard(database: ExpenseDatabase) {
    var tabIndex by remember { mutableStateOf(0) }
    var expenses by remember { mutableStateOf(database.getAllExpenses()) }
    var receitas by remember { mutableStateOf(database.getAllReceitas()) }

    val totalDespesas = database.getTotalDespesas()
    val totalDespesasPagas = database.getTotalDespesasPagas()
    val totalDespesasPendentes = database.getTotalDespesasPendentes()
    val totalReceitas = database.getTotalReceitas()
    val totalReceitasRecebidas = database.getTotalReceitasRecebidas()
    val totalReceitasPendentes = database.getTotalReceitasPendentes()
    val saldo = database.getSaldo()

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
            // ===== HEADER COM DASHBOARD =====
            Surface(
                shape = RoundedCornerShape(20.dp),
                elevation = 8.dp,
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "📊 Resumo Financeiro",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF065F46)
                    )

                    // Grid de totalizadores
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            DashboardCard(
                                title = "💰 Total Receitas",
                                value = formatCurrency(totalReceitas),
                                bgColor = Color(0xFFD1FAE5),
                                textColor = Color(0xFF065F46),
                                modifier = Modifier.weight(1f)
                            )
                            DashboardCard(
                                title = "✅ Receitas Recebidas",
                                value = formatCurrency(totalReceitasRecebidas),
                                bgColor = Color(0xFF10B981),
                                textColor = Color.White,
                                modifier = Modifier.weight(1f)
                            )
                            DashboardCard(
                                title = "⏳ Receitas Pendentes",
                                value = formatCurrency(totalReceitasPendentes),
                                bgColor = Color(0xFFFEF3C7),
                                textColor = Color(0xFF92400E),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            DashboardCard(
                                title = "💸 Total Despesas",
                                value = formatCurrency(totalDespesas),
                                bgColor = Color(0xFFFFEDD5),
                                textColor = Color(0xFF92400E),
                                modifier = Modifier.weight(1f)
                            )
                            DashboardCard(
                                title = "✅ Despesas Pagas",
                                value = formatCurrency(totalDespesasPagas),
                                bgColor = Color(0xFFDEBEAA),
                                textColor = Color(0xFF7C2D12),
                                modifier = Modifier.weight(1f)
                            )
                            DashboardCard(
                                title = "⏳ Despesas Pendentes",
                                value = formatCurrency(totalDespesasPendentes),
                                bgColor = Color(0xFFFFB6C1),
                                textColor = Color(0xFF8B0000),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Card de saldo destacado
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (saldo >= 0) Color(0xFF10B981) else Color(0xFFEF4444),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "💵 Saldo Líquido",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = formatCurrency(saldo),
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

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
            }

            // ===== CONTEÚDO DAS ABAS =====
            Box(modifier = Modifier.weight(1f)) {
                when (tabIndex) {
                    0 -> ExpenseListSection(
                        expenses = expenses,
                        onStatusToggle = { expense ->
                            try {
                                val newStatus = if (expense.status == "Pago") "Aguardando" else "Pago"
                                database.updateExpenseStatus(expense.id, newStatus)
                                expenses = database.getAllExpenses()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        },
                        onDelete = { expense ->
                            try {
                                database.deleteExpense(expense.id)
                                expenses = database.getAllExpenses()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    )
                    1 -> ReceitaListPanel(
                        receitas = receitas,
                        onStatusToggle = { receita ->
                            try {
                                val newStatus = if (receita.status == "Recebido") "Pendente" else "Recebido"
                                database.updateReceitaStatus(receita.id, newStatus)
                                receitas = database.getAllReceitas()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        },
                        onDelete = { receita ->
                            try {
                                database.deleteReceita(receita.id)
                                receitas = database.getAllReceitas()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    )
                }
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
            Text(
                text = "📋 Receitas (${receitas.size})",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF065F46)
            )

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
                Text(
                    text = "📅 ${receita.data}",
                    color = Color(0xFF6B7280),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 6.dp)
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
                    color = Color(0xFF059669)
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
            Text(
                text = "📋 Despesas (${expenses.size})",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF065F46)
            )

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
                Text(
                    text = "📅 ${expense.vencimento}",
                    color = Color(0xFF6B7280),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 6.dp)
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
                    color = Color(0xFF059669)
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


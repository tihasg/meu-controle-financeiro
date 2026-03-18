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
import database.Expense
import database.ExpenseDatabase
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

@Composable
fun ExpenseManagerUI(database: ExpenseDatabase) {
    var expenses by remember { mutableStateOf(database.getAllExpenses()) }
    val totalEmAberto = expenses.filter { it.status == "Aguardando" }.sumOf { it.valor }
    val totalGeral = expenses.sumOf { it.valor }

    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFEEF2F7))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AddExpensePanel(
                    modifier = Modifier.width(360.dp),
                    onAddExpense = { expense ->
                        try {
                            database.addExpense(expense)
                            expenses = database.getAllExpenses()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    },
                    totalCount = expenses.size,
                    totalEmAberto = totalEmAberto,
                    totalGeral = totalGeral
                )
                ExpenseListPanel(
                    modifier = Modifier.weight(1f),
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
            }
        }
    }
}

@Composable
private fun AddExpensePanel(
    modifier: Modifier = Modifier,
    onAddExpense: (Expense) -> Unit,
    totalCount: Int,
    totalEmAberto: Double,
    totalGeral: Double
) {
    var fornecedor by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("") }
    var valor by remember { mutableStateOf("") }
    var vencimento by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Surface(
        modifier = Modifier
            .then(modifier)
            .fillMaxHeight(),
        shape = RoundedCornerShape(20.dp),
        elevation = 8.dp,
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Header
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "💰 Controle de Gastos",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF065F46)
                )
                Text(
                    text = "Gerencie suas despesas de forma simples e eficiente",
                    color = Color(0xFF6B7280),
                    fontSize = 14.sp
                )
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFE5E7EB))

            // Summary Cards com cores diferentes
            SummaryCard(
                title = "📊 Total de Despesas",
                value = totalCount.toString(),
                backgroundColor = Color(0xFFECFDF5),
                textColor = Color(0xFF065F46)
            )
            SummaryCard(
                title = "⏳ Pendente",
                value = formatCurrency(totalEmAberto),
                backgroundColor = Color(0xFFFEF3C7),
                textColor = Color(0xFF92400E)
            )
            SummaryCard(
                title = "💵 Total Geral",
                value = formatCurrency(totalGeral),
                backgroundColor = Color(0xFFF0FDF4),
                textColor = Color(0xFF15803D)
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFE5E7EB))

            // Form Section
            Text(
                text = "➕ Nova Despesa",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937)
            )

            ExpenseTextField(
                value = fornecedor,
                onValueChange = { fornecedor = it },
                label = "🏢 Fornecedor"
            )
            ExpenseTextField(
                value = categoria,
                onValueChange = { categoria = it },
                label = "🏷️ Categoria"
            )
            ExpenseTextField(
                value = valor,
                onValueChange = { valor = it },
                label = "💰 Valor",
                keyboardType = KeyboardType.Decimal
            )
            ExpenseTextField(
                value = vencimento,
                onValueChange = { vencimento = it },
                label = "📅 Vencimento (AAAA-MM-DD)"
            )

            errorMessage?.let { message ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFEE2E2)
                ) {
                    Text(
                        text = "⚠️ $message",
                        color = Color(0xDC2626),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Button(
                onClick = {
                    val parsedValue = valor.replace(',', '.').toDoubleOrNull()
                    val parsedDate = try {
                        LocalDate.parse(vencimento)
                    } catch (_: DateTimeParseException) {
                        null
                    }

                    errorMessage = when {
                        fornecedor.isBlank() -> "Informe o fornecedor."
                        categoria.isBlank() -> "Informe a categoria."
                        parsedValue == null || parsedValue <= 0.0 -> "Informe um valor numérico maior que zero."
                        parsedDate == null -> "Use o formato de vencimento AAAA-MM-DD."
                        else -> null
                    }

                    if (errorMessage == null) {
                        try {
                            onAddExpense(
                                Expense(
                                    fornecedor = fornecedor.trim(),
                                    categoria = categoria.trim(),
                                    valor = parsedValue!!,
                                    status = "Aguardando",
                                    vencimento = parsedDate!!
                                )
                            )
                            fornecedor = ""
                            categoria = ""
                            valor = ""
                            vencimento = ""
                        } catch (e: Exception) {
                            errorMessage = "Erro ao adicionar: ${e.message}"
                            e.printStackTrace()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(0xFF059669),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(14.dp),
                elevation = ButtonDefaults.elevation(8.dp)
            ) {
                Text("➕ Adicionar Despesa", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SummaryCard(
    title: String,
    value: String,
    backgroundColor: Color = Color(0xFFF8FAFC),
    textColor: Color = Color(0xFF0F172A)
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                color = Color(0xFF6B7280),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = value,
                color = textColor,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}

@Composable
private fun ExpenseListPanel(
    modifier: Modifier = Modifier,
    expenses: List<Expense>,
    onStatusToggle: (Expense) -> Unit,
    onDelete: (Expense) -> Unit
) {
    Surface(
        modifier = modifier.fillMaxHeight(),
        shape = RoundedCornerShape(20.dp),
        elevation = 8.dp,
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp)
        ) {
            Text(
                text = "📋 Despesas Cadastradas",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF065F46)
            )
            Text(
                text = "Total: ${expenses.size} despesa${if (expenses.size != 1) "s" else ""}",
                color = Color(0xFF6B7280),
                modifier = Modifier.padding(top = 4.dp, bottom = 18.dp),
                fontSize = 14.sp
            )

            if (expenses.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("💭", fontSize = 48.sp)
                        Text(
                            text = "Nenhuma despesa registrada ainda",
                            color = Color(0xFF9CA3AF),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(items = expenses, key = { it.id }) { expense ->
                        ExpenseRow(
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
private fun ExpenseRow(
    expense: Expense,
    onStatusToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val statusColor = if (expense.status == "Pago") Color(0xFF10B981) else Color(0xFFF59E0B)
    val statusBgColor = if (expense.status == "Pago") Color(0xFFD1FAE5) else Color(0xFFFEF3C7)
    val daysUntilDue = java.time.temporal.ChronoUnit.DAYS.between(java.time.LocalDate.now(), expense.vencimento)
    val isOverdue = daysUntilDue < 0
    val urgencyColor = when {
        isOverdue -> Color(0xFFEF4444)
        daysUntilDue in 0..3 -> Color(0xFFF59E0B)
        else -> Color(0xFF6B7280)
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFFAFAFA),
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = expense.fornecedor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF1F2937)
                )
                Text(
                    text = expense.categoria,
                    color = Color(0xFF6B7280),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    text = if (isOverdue) {
                        "⚠️ Vencido há ${-daysUntilDue} dia${if (daysUntilDue != -1L) "s" else ""}"
                    } else if (daysUntilDue == 0L) {
                        "⏰ Vence hoje!"
                    } else {
                        "📅 Vence em $daysUntilDue dia${if (daysUntilDue != 1L) "s" else ""}"
                    },
                    color = urgencyColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = formatCurrency(expense.valor),
                    fontSize = 20.sp,
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
                        fontSize = 12.sp,
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
                        Text(if (expense.status == "Pago") "Desfazer" else "Marcar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    TextButton(
                        onClick = onDelete,
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("🗑️", fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpenseTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = Color(0xFF059669),
            focusedLabelColor = Color(0xFF059669),
            cursorColor = Color(0xFF059669),
            backgroundColor = Color(0xFFFAFAFA)
        ),
        shape = RoundedCornerShape(12.dp),
        textStyle = androidx.compose.material.LocalTextStyle.current.copy(fontSize = 14.sp)
    )
}

private val uiDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
private val brazilLocale = Locale.forLanguageTag("pt-BR")

private fun formatCurrency(value: Double): String {
    return java.text.NumberFormat.getCurrencyInstance(brazilLocale).format(value)
}



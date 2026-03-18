package ui

import androidx.compose.foundation.background
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
                .background(Color(0xFFF4F6F8))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                AddExpensePanel(
                    modifier = Modifier.width(340.dp),
                    onAddExpense = { expense ->
                        database.addExpense(expense)
                        expenses = database.getAllExpenses()
                    },
                    totalCount = expenses.size,
                    totalEmAberto = totalEmAberto,
                    totalGeral = totalGeral
                )
                ExpenseListPanel(
                    modifier = Modifier.weight(1f),
                    expenses = expenses,
                    onStatusToggle = { expense ->
                        val newStatus = if (expense.status == "Pago") "Aguardando" else "Pago"
                        database.updateExpenseStatus(expense.id, newStatus)
                        expenses = database.getAllExpenses()
                    },
                    onDelete = { expense ->
                        database.deleteExpense(expense.id)
                        expenses = database.getAllExpenses()
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
        shape = RoundedCornerShape(24.dp),
        elevation = 2.dp,
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Controle de despesas",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )
            Text(
                text = "Cadastre fornecedores, acompanhe pagamentos e mantenha a lista ordenada pelos maiores valores.",
                color = Color(0xFF64748B)
            )

            SummaryCard(title = "Despesas", value = totalCount.toString())
            SummaryCard(title = "Em aberto", value = formatCurrency(totalEmAberto))
            SummaryCard(title = "Total", value = formatCurrency(totalGeral))

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Nova despesa",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1E293B)
            )

            ExpenseTextField(
                value = fornecedor,
                onValueChange = { fornecedor = it },
                label = "Fornecedor"
            )
            ExpenseTextField(
                value = categoria,
                onValueChange = { categoria = it },
                label = "Categoria"
            )
            ExpenseTextField(
                value = valor,
                onValueChange = { valor = it },
                label = "Valor",
                keyboardType = KeyboardType.Decimal
            )
            ExpenseTextField(
                value = vencimento,
                onValueChange = { vencimento = it },
                label = "Vencimento (AAAA-MM-DD)"
            )

            errorMessage?.let { message ->
                Text(
                    text = message,
                    color = Color(0xFFB91C1C),
                    fontSize = 12.sp
                )
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
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(0xFF0F766E),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Adicionar despesa")
            }
        }
    }
}

@Composable
private fun SummaryCard(
    title: String,
    value: String
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFFF8FAFC)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                color = Color(0xFF64748B),
                fontSize = 13.sp
            )
            Text(
                text = value,
                color = Color(0xFF0F172A),
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
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
        shape = RoundedCornerShape(24.dp),
        elevation = 2.dp,
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Text(
                text = "Despesas cadastradas",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )
            Text(
                text = "Lista ordenada por valor, do maior para o menor.",
                color = Color(0xFF64748B),
                modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
            )

            if (expenses.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nenhuma despesa registrada ainda.",
                        color = Color(0xFF94A3B8),
                        fontSize = 15.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
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
    val statusColor = if (expense.status == "Pago") Color(0xFF15803D) else Color(0xFFD97706)

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFFF8FAFC)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = expense.fornecedor,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 17.sp,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = expense.categoria,
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatCurrency(expense.valor),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F766E)
                    )
                    Text(
                        text = "Vence em ${expense.vencimento.format(uiDateFormatter)}",
                        color = Color(0xFF64748B),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onStatusToggle,
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = statusColor,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(expense.status)
                }
                TextButton(onClick = onDelete) {
                    Text("Remover", color = Color(0xFFB91C1C))
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
            focusedBorderColor = Color(0xFF0F766E),
            focusedLabelColor = Color(0xFF0F766E),
            cursorColor = Color(0xFF0F766E)
        ),
        shape = RoundedCornerShape(14.dp)
    )
}

private val uiDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
private val brazilLocale = Locale.forLanguageTag("pt-BR")

private fun formatCurrency(value: Double): String {
    return java.text.NumberFormat.getCurrencyInstance(brazilLocale).format(value)
}


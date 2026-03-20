import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.unit.dp
import database.ExpenseDatabase
import ui.FinancialDashboard

fun main() {
    try {
        application {
            val database = ExpenseDatabase()
            
            Window(
                onCloseRequest = {
                    database.close()
                    exitApplication()
                },
                title = "💰 Gastos - Controle Financeiro",
                state = WindowState(width = 1600.dp, height = 900.dp)
            ) {
                FinancialDashboard(database)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        println("Erro ao inicializar aplicação: ${e.message}")
        e.cause?.printStackTrace()
    }
}


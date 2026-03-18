import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import database.ExpenseDatabase
import ui.ExpenseManagerUI

fun main() = application {
    val database = ExpenseDatabase()
    
    Window(
        onCloseRequest = {
            database.close()
            exitApplication()
        },
        title = "Gerenciador de Despesas"
    ) {
        ExpenseManagerUI(database)
    }
}
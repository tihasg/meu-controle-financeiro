package database

import java.sql.Connection
import java.sql.DriverManager
import java.sql.Statement
import java.time.LocalDate
import java.nio.file.Files
import java.nio.file.Path

data class Expense(
    val id: Int = 0,
    val fornecedor: String,
    val categoria: String,
    val valor: Double,
    val status: String, // "Pago" or "Aguardando"
    val vencimento: LocalDate
)

class ExpenseDatabase {
    private val dbPath: Path = Path.of(System.getProperty("user.home"), ".expense-manager", "expenses.db")
    private val dbUrl = "jdbc:sqlite:${dbPath.toAbsolutePath()}"
    private var connection: Connection? = null

    init {
        Class.forName("org.sqlite.JDBC")
        Files.createDirectories(dbPath.parent)
        initializeDatabase()
    }

    private fun getConnection(): Connection {
        if (connection == null || connection?.isClosed == true) {
            connection = DriverManager.getConnection(dbUrl)
        }
        return connection!!
    }

    private fun initializeDatabase() {
        getConnection().createStatement().use { statement ->
            statement.execute(
                """
                CREATE TABLE IF NOT EXISTS expenses (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    fornecedor TEXT NOT NULL,
                    categoria TEXT NOT NULL,
                    valor REAL NOT NULL CHECK(valor >= 0),
                    status TEXT NOT NULL CHECK(status IN ('Pago', 'Aguardando')),
                    vencimento TEXT NOT NULL
                )
                """.trimIndent()
            )
        }
    }

    fun addExpense(expense: Expense): Int {
        getConnection().prepareStatement(
            "INSERT INTO expenses (fornecedor, categoria, valor, status, vencimento) VALUES (?, ?, ?, ?, ?)",
            Statement.RETURN_GENERATED_KEYS
        ).use { stmt ->
            stmt.setString(1, expense.fornecedor)
            stmt.setString(2, expense.categoria)
            stmt.setDouble(3, expense.valor)
            stmt.setString(4, expense.status)
            stmt.setString(5, expense.vencimento.toString())
            stmt.executeUpdate()

            stmt.generatedKeys.use { generatedKeys ->
                return if (generatedKeys.next()) generatedKeys.getInt(1) else 0
            }
        }
    }

    fun getAllExpenses(): List<Expense> {
        val expenses = mutableListOf<Expense>()

        getConnection().createStatement().use { stmt ->
            stmt.executeQuery("SELECT * FROM expenses ORDER BY valor DESC, vencimento ASC, id DESC").use { rs ->
                while (rs.next()) {
                    expenses.add(
                        Expense(
                            id = rs.getInt("id"),
                            fornecedor = rs.getString("fornecedor"),
                            categoria = rs.getString("categoria"),
                            valor = rs.getDouble("valor"),
                            status = rs.getString("status"),
                            vencimento = LocalDate.parse(rs.getString("vencimento"))
                        )
                    )
                }
            }
        }

        return expenses
    }

    fun updateExpenseStatus(id: Int, status: String) {
        getConnection().prepareStatement("UPDATE expenses SET status = ? WHERE id = ?").use { stmt ->
            stmt.setString(1, status)
            stmt.setInt(2, id)
            stmt.executeUpdate()
        }
    }

    fun deleteExpense(id: Int) {
        getConnection().prepareStatement("DELETE FROM expenses WHERE id = ?").use { stmt ->
            stmt.setInt(1, id)
            stmt.executeUpdate()
        }
    }

    fun close() {
        connection?.close()
        connection = null
    }
}


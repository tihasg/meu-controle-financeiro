# Gerenciador de Despesas - Expense Manager

A desktop expense management application built with Kotlin Compose Desktop using SQLite for data persistence.

## Features

✅ **List of Expenses** - View all registered expenses in a clean, organized list
✅ **Expense Fields** - Track:
  - **Fornecedor** (Supplier/Vendor)
  - **Categoria** (Category)
  - **Valor** (Amount in R$)
  - **Status** (Paid or Pending)
  - **Vencimento** (Due Date)

✅ **Add Expense** - Button to add new expenses with a simple dialog form
✅ **Toggle Status** - Click the status button to toggle between "Pago" (Paid) and "Aguardando" (Pending)
✅ **Delete Expense** - Remove expenses from the database
✅ **SQLite Persistence** - Data is automatically saved to `expenses.db`
✅ **Sorted Display** - Expenses are displayed sorted by amount (valor) in descending order
✅ **Clean UI** - Simple and intuitive Material Design interface

## Project Structure

```
desktopApp/
├── src/
│   ├── desktopMain/
│   │   └── kotlin/
│   │       └── main.kt                 # Entry point for the desktop application
│   └── jvmMain/
│       └── kotlin/
│           ├── database/
│           │   └── ExpenseDatabase.kt  # Database model and operations
│           └── ui/
│               └── ExpenseManagerUI.kt # Compose UI components
├── build.gradle.kts                    # Desktop app dependencies
```

## Technical Stack

- **Language**: Kotlin
- **UI Framework**: Jetbrains Compose Desktop
- **Database**: SQLite with JDBC
- **Build System**: Gradle with Kotlin DSL

## Dependencies Added

The following dependency was added to `desktopApp/build.gradle.kts`:
```kotlin
implementation("org.xerial:sqlite-jdbc:3.44.0.0")
```

## Database Schema

The application creates and uses an SQLite database with the following table:

```sql
CREATE TABLE expenses (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    fornecedor TEXT NOT NULL,
    categoria TEXT NOT NULL,
    valor REAL NOT NULL,
    status TEXT NOT NULL,
    vencimento TEXT NOT NULL
)
```

## Building and Running

### Prerequisites
- Java 17 or higher
- Kotlin 1.9.21 or higher
- Gradle 8.0.2 or higher

### Build the project
```bash
./gradlew desktopApp:build
```

### Run the application
```bash
./gradlew desktopApp:run
```

## How to Use

1. **Launch the Application** - Run the app using the command above
2. **Add Expense** - Click "+ Adicionar Despesa" button
3. **Fill the Form**:
   - Fornecedor: Enter the supplier/vendor name
   - Categoria: Enter the expense category
   - Valor: Enter the amount (numeric value)
   - Vencimento: Enter the due date in format YYYY-MM-DD
4. **Confirm** - Click "Adicionar" to save the expense
5. **Toggle Status** - Click the status button (green for "Pago", orange for "Aguardando") to change status
6. **Delete** - Click the "Deletar" button to remove an expense

## Data Persistence

- All expenses are automatically saved to `expenses.db` file in the working directory
- The database is created automatically on first run
- Data persists between application sessions

## UI Components

### ExpenseManagerUI
Main composable function that manages the state and layout of the expense manager.

### ExpenseItem
Individual expense card component showing:
- Supplier name and category
- Amount and due date
- Status toggle button
- Delete button

### AddExpenseDialog
Modal dialog with form fields for adding new expenses with validation.

## Example Expenses

The application supports tracking various types of expenses:
- **Fornecedor**: "Telefônica", "EDP", "Supermercado ABC"
- **Categoria**: "Internet", "Energia", "Alimentação"
- **Valor**: Any positive number (e.g., 150.50, 89.99)
- **Status**: "Pago" or "Aguardando"
- **Vencimento**: Dates in YYYY-MM-DD format (e.g., 2026-03-31)

## Code Example

```kotlin
// Create database instance
val database = ExpenseDatabase()

// Add an expense
database.addExpense(
    Expense(
        fornecedor = "Telefônica",
        categoria = "Internet",
        valor = 120.00,
        status = "Aguardando",
        vencimento = LocalDate.of(2026, 3, 31)
    )
)

// Get all expenses (sorted by valor descending)
val expenses = database.getAllExpenses()

// Update status
database.updateExpenseStatus(expenseId, "Pago")

// Delete expense
database.deleteExpense(expenseId)

// Close connection
database.close()
```

## Notes

- All amounts should be entered as positive numbers with the format `123.45`
- Dates must be in ISO format: `YYYY-MM-DD`
- The database file (`expenses.db`) is created in the application's working directory
- Status can only be "Pago" or "Aguardando"
- The list is automatically sorted by amount in descending order

## License

This project is part of the Compose Multiplatform Template and follows the same license.


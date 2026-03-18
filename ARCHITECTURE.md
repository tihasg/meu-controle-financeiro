# Expense Manager - Architecture & Code Structure

## 🏗️ Project Architecture

```
compose-multiplatform-template/
├── desktopApp/                          # Desktop application module
│   ├── build.gradle.kts                 # Desktop app build configuration
│   │   └── SQLite JDBC dependency added
│   │
│   └── src/
│       ├── desktopMain/
│       │   └── kotlin/
│       │       └── main.kt              # Application entry point
│       │           ├── Initializes ExpenseDatabase
│       │           ├── Creates Compose Window
│       │           └── Manages lifecycle
│       │
│       └── jvmMain/
│           └── kotlin/
│               ├── database/            # Data layer
│               │   └── ExpenseDatabase.kt
│               │       ├── Expense data class
│               │       └── Database operations (CRUD)
│               │
│               └── ui/                  # Presentation layer
│                   └── ExpenseManagerUI.kt
│                       ├── ExpenseManagerUI() - Main composable
│                       ├── ExpenseItem() - Card component
│                       └── AddExpenseDialog() - Form dialog
│
└── expenses.db                          # SQLite database (created at runtime)
```

---

## 🔄 Component Hierarchy

```
main()
└── application block
    └── Window (Desktop App)
        └── ExpenseManagerUI
            ├── Column (Main Layout)
            │   ├── Header Text
            │   ├── Add Button
            │   └── LazyColumn (List)
            │       └── ExpenseItem (repeated)
            │           ├── Card
            │           ├── Expense Details
            │           └── Action Buttons
            │               ├── Status Toggle
            │               └── Delete
            │
            └── AddExpenseDialog (conditional)
                ├── Title
                ├── Column (Form)
                │   ├── OutlinedTextField (Fornecedor)
                │   ├── OutlinedTextField (Categoria)
                │   ├── OutlinedTextField (Valor)
                │   └── OutlinedTextField (Vencimento)
                └── Buttons
                    ├── Cancel
                    └── Add
```

---

## 📊 Data Flow

```
┌─────────────────────────────────────────┐
│         ExpenseManagerUI                │
│  (State: expenses, showAddDialog)       │
└────────────────────┬────────────────────┘
                     │
        ┌────────────┼────────────┐
        │            │            │
        ▼            ▼            ▼
   [Load Data]  [Add Dialog]  [Update UI]
        │            │            │
        └────────────┴────────────┘
              │
              ▼
    ┌─────────────────────────┐
    │   ExpenseDatabase       │
    │ (SQLite Connection)     │
    └────────────┬────────────┘
                 │
        ┌────────┼────────┬──────────┐
        │        │        │          │
        ▼        ▼        ▼          ▼
    [CREATE]  [READ]  [UPDATE]  [DELETE]
        │        │        │          │
        └────────┴────────┴──────────┘
              │
              ▼
    ┌─────────────────────────┐
    │    expenses.db          │
    │  (SQLite Database)      │
    └─────────────────────────┘
```

---

## 📝 Data Model

### Expense Class
```kotlin
data class Expense(
    val id: Int = 0,              // Auto-incremented primary key
    val fornecedor: String,       // Vendor/Supplier name
    val categoria: String,        // Expense category
    val valor: Double,            // Amount in Brazilian Real
    val status: String,           // "Pago" or "Aguardando"
    val vencimento: LocalDate     // Due date
)
```

### Database Table Schema
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

---

## 🔧 ExpenseDatabase Class

### Public Methods

```kotlin
// Initialize database and create tables
ExpenseDatabase()

// Add new expense and return generated ID
fun addExpense(expense: Expense): Int

// Get all expenses sorted by valor descending
fun getAllExpenses(): List<Expense>

// Toggle status between "Pago" and "Aguardando"
fun updateExpenseStatus(id: Int, status: String)

// Delete expense by ID
fun deleteExpense(id: Int)

// Close database connection
fun close()
```

### Private Methods

```kotlin
// Get or create database connection
private fun getConnection(): Connection

// Create table if not exists
private fun initializeDatabase()
```

---

## 🎨 UI Components

### 1. ExpenseManagerUI (Main Composable)

**Purpose**: Manages overall UI state and layout

**State Variables**:
- `expenses: List<Expense>` - Current expense list
- `showAddDialog: Boolean` - Show/hide add dialog

**Key Features**:
- Loads expenses from database on startup
- Handles add button clicks
- Refreshes list after add/update/delete
- Displays empty state message

### 2. ExpenseItem (Card Component)

**Purpose**: Display individual expense information

**Input Parameters**:
- `expense: Expense` - The expense to display
- `onStatusToggle: (String) -> Unit` - Callback for status changes
- `onDelete: () -> Unit` - Callback for delete action

**Displays**:
- Supplier name (bold)
- Category (gray, smaller)
- Amount (blue, semibold)
- Due date
- Status button (green or orange)
- Delete button (red)

### 3. AddExpenseDialog (Form Dialog)

**Purpose**: Modal form for adding new expenses

**Input Parameters**:
- `onDismiss: () -> Unit` - Cancel callback
- `onAdd: (Expense) -> Unit` - Submit callback

**Form Fields**:
- Fornecedor (TextInput)
- Categoria (TextInput)
- Valor (TextInput)
- Vencimento (TextInput)

**Features**:
- Input validation (non-empty required)
- Format validation (date format YYYY-MM-DD)
- Value parsing (string to Double)
- Error handling

---

## 🎯 State Management

### Using Compose State

```kotlin
var expenses by remember { mutableStateOf(database.getAllExpenses()) }
```

**How it works**:
1. `remember` preserves state across recompositions
2. `mutableStateOf` makes state observable
3. Composables automatically recompose when state changes
4. No external state management needed (local state)

### Updating State

```kotlin
// When expense is added
database.addExpense(newExpense)
expenses = database.getAllExpenses()  // Refresh from database

// When status is toggled
database.updateExpenseStatus(id, newStatus)
expenses = database.getAllExpenses()  // Refresh list

// When expense is deleted
database.deleteExpense(id)
expenses = database.getAllExpenses()  // Refresh list
```

---

## 🔀 User Interactions

### Add Expense Flow
```
User clicks "Add" button
        ↓
Dialog opens with form fields
        ↓
User fills form and clicks "Adicionar"
        ↓
Form validation checks
        ↓
Parse data (valor to Double, vencimento to LocalDate)
        ↓
database.addExpense(expense)
        ↓
expenses = database.getAllExpenses()  // Refresh
        ↓
UI re-renders with new expense
```

### Toggle Status Flow
```
User clicks status button on expense
        ↓
Determine new status (Pago ↔ Aguardando)
        ↓
database.updateExpenseStatus(id, newStatus)
        ↓
expenses = database.getAllExpenses()  // Refresh
        ↓
UI re-renders with updated status
```

### Delete Expense Flow
```
User clicks "Deletar" button
        ↓
database.deleteExpense(id)
        ↓
expenses = database.getAllExpenses()  // Refresh
        ↓
UI re-renders without deleted expense
```

---

## 💾 Database Operations

### Reading Data (getAllExpenses)
```kotlin
SELECT * FROM expenses ORDER BY valor DESC
```
- Sorts by amount in descending order
- Highest amounts first
- Used on app startup and after modifications

### Creating Data (addExpense)
```sql
INSERT INTO expenses (fornecedor, categoria, valor, status, vencimento)
VALUES (?, ?, ?, ?, ?)
```
- Parameterized to prevent SQL injection
- Returns auto-generated ID
- Default status: "Aguardando"

### Updating Data (updateExpenseStatus)
```sql
UPDATE expenses SET status = ? WHERE id = ?
```
- Only updates the status field
- Other fields remain unchanged
- Changes persist immediately

### Deleting Data (deleteExpense)
```sql
DELETE FROM expenses WHERE id = ?
```
- Removes entire expense record
- Cannot be undone
- Deletion cascades to UI automatically

---

## 🎨 Color Scheme & Typography

### Colors
```kotlin
// Primary Blue
Color(0xFF1976D2)  // Headers, main buttons

// Success Green (Paid)
Color(0xFF4CAF50)  // "Pago" status button

// Warning Orange (Pending)
Color(0xFFFFA726)  // "Aguardando" status button

// Error Red (Delete)
Color(0xFFF44336)  // Delete button

// Background
Color(0xFFF5F5F5)  // Light gray background

// Text Colors
Color.Black        // Primary text
Color.Gray         // Secondary text
Color.White        // Button text
```

### Typography
```kotlin
// Header: 28.sp Bold Blue
// Card Title: 16.sp Bold Black
// Card Category: 12.sp Gray
// Amount: 14.sp Semibold Blue
// Date: 12.sp Gray
// Button Text: 11.sp Bold
```

---

## 📱 UI Layout Details

### Main Layout
- Modifier: `fillMaxSize()` + `padding(16.dp)` + `background(lightGray)`
- Column with vertical arrangement
- Header text (28sp bold)
- Add button (top-right aligned)
- Scrollable list

### Expense Card
- Card elevation: 4.dp
- Padding: 16.dp
- Row with weight-based layout:
  - Left column: Details (weight=1f)
  - Right column: Buttons (fixed width)
- Buttons: 100.dp width each
- Button spacing: 8.dp vertical

### Dialog
- Standard Material AlertDialog
- Column for form fields with spacedBy(12.dp)
- OutlinedTextField for inputs
- Row for buttons with spacedBy(8.dp)

---

## 🧪 Testing the Application

### Manual Test Checklist

```
✓ Startup
  - App launches without errors
  - Database created in working directory
  - Empty state shows message

✓ Add Expense
  - Button opens dialog
  - All fields accept input
  - Validation works (non-empty required)
  - Date format validation
  - Successfully adds to list
  - List updates immediately

✓ View Expenses
  - Expenses appear in list
  - Sorted by amount descending
  - All fields display correctly
  - Formatting looks good

✓ Toggle Status
  - Buttons are clickable
  - Color changes from orange to green
  - Changes persist on restart
  - Status updates immediately

✓ Delete Expense
  - Delete button removes expense
  - List updates immediately
  - Deleted expense doesn't reappear

✓ Data Persistence
  - Add expense, restart app, expense is still there
  - Status changes persist
  - Deletions persist

✓ Edge Cases
  - Very long vendor names
  - Large amounts (999999.99)
  - Special characters in text fields
  - Empty list state
```

---

## 📦 Dependencies

### Kotlin Compose Desktop
- Provides desktop-specific Compose components
- Window management
- Application lifecycle

### SQLite JDBC
- Pure Java SQLite driver
- No native dependencies
- Easy cross-platform support
- Version: 3.44.0.0

### Standard Library
- `java.time.LocalDate` - Date handling
- `java.sql.*` - Database connectivity
- `kotlin.stdlib` - Core language features

---

## 🚀 Build & Run Commands

### Build
```bash
./gradlew desktopApp:build
```

### Run
```bash
./gradlew desktopApp:run
```

### Clean Build
```bash
./gradlew clean desktopApp:build
```

### Package Application
```bash
./gradlew desktopApp:package
```
Creates MSI/DMG/DEB packages depending on OS

---

## 📚 Related Files

- **Main Entry Point**: `desktopApp/src/desktopMain/kotlin/main.kt`
- **Database Logic**: `desktopApp/src/jvmMain/kotlin/database/ExpenseDatabase.kt`
- **UI Components**: `desktopApp/src/jvmMain/kotlin/ui/ExpenseManagerUI.kt`
- **Build Config**: `desktopApp/build.gradle.kts`

---

This architecture provides a clean separation of concerns with the database layer completely separated from the UI layer, making the code maintainable and testable.


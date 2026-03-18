# Expense Manager - Quick Start Guide

## 🚀 Getting Started in 5 Minutes

### Step 1: Build the Project
Open your terminal/PowerShell in the project root and run:

```bash
./gradlew desktopApp:build
```

This will download all dependencies and compile the project. First build may take 2-3 minutes.

### Step 2: Run the Application
```bash
./gradlew desktopApp:run
```

The Expense Manager window should open automatically.

### Step 3: Add Your First Expense
1. Click the **"+ Adicionar Despesa"** button in the top-right corner
2. A dialog will pop up with four fields to fill:
   - **Fornecedor**: The company/person you're paying (e.g., "Telefônica")
   - **Categoria**: Type of expense (e.g., "Internet")
   - **Valor**: Amount in Brazilian Real (e.g., "120.50")
   - **Vencimento**: Due date in YYYY-MM-DD format (e.g., "2026-04-15")
3. Click **"Adicionar"** to save

### Step 4: Manage Your Expenses
- **Toggle Status**: Click the colored button (green/"Pago" or orange/"Aguardando") to mark payment status
- **Delete**: Click the red "Deletar" button to remove an expense
- **View**: All expenses automatically appear sorted by amount (highest first)

---

## 📋 Quick Reference

### Date Format
Always use: **YYYY-MM-DD**
- ✅ Correct: `2026-04-15`
- ❌ Wrong: `15/04/2026` or `04-15-2026`

### Amount Format
Use decimal point: **123.45**
- ✅ Correct: `150.50`, `1000.00`, `89.99`
- ❌ Wrong: `150,50` (comma), `R$ 150.50` (no symbols)

### Status Options
Only two options:
- **Pago** - Payment made (green button)
- **Aguardando** - Waiting for payment (orange button)

### File Locations
- **Database**: `expenses.db` (created in your working directory)
- **Main code**: `desktopApp/src/desktopMain/kotlin/main.kt`
- **Database logic**: `desktopApp/src/jvmMain/kotlin/database/ExpenseDatabase.kt`
- **UI code**: `desktopApp/src/jvmMain/kotlin/ui/ExpenseManagerUI.kt`

---

## 🔍 Example Workflow

### Scenario: Managing Monthly Bills

1. **Add Internet Bill**
   - Fornecedor: "Telefônica"
   - Categoria: "Internet"
   - Valor: "150.00"
   - Vencimento: "2026-03-31"
   - Status: "Aguardando" (default)

2. **Add Water Bill**
   - Fornecedor: "Sabesp"
   - Categoria: "Água"
   - Valor: "125.50"
   - Vencimento: "2026-03-28"
   - Status: "Aguardando"

3. **Add Electricity Bill**
   - Fornecedor: "EDP"
   - Categoria: "Energia"
   - Valor: "350.00"
   - Vencimento: "2026-04-15"
   - Status: "Aguardando"

4. **View Results**
   - List shows expenses sorted by amount:
     1. EDP - R$ 350.00 (highest)
     2. Telefônica - R$ 150.00
     3. Sabesp - R$ 125.50

5. **Pay and Update**
   - Click status buttons to mark "Pago" as you pay bills
   - Buttons change from orange to green
   - Data persists automatically to database

---

## 💡 Tips & Tricks

### Organize Your Categories
Keep category names consistent for better tracking:
- **Bills**: Internet, Água, Energia, Gás
- **Food**: Alimentação, Restaurante, Supermercado
- **Transportation**: Combustível, Uber, Ônibus
- **Health**: Farmácia, Médico, Dentista
- **Entertainment**: Cinema, Streaming, Eventos

### Vendor Names
Use full company names for clarity:
- Instead of: "Tel" → Use: "Telefônica Brasil"
- Instead of: "Caixa" → Use: "Banco Caixa Econômica"

### Regular Maintenance
- Review your list weekly
- Delete old/paid expenses to keep database clean
- Update status as you make payments

### Data Backup
The database file `expenses.db` contains all your data:
- It's created in the directory where you run the app
- You can copy it as a backup
- Simply restore it to recover all data

---

## 🐛 Troubleshooting

### Issue: "Java not found" error
**Solution**: Install Java 17+ from https://www.oracle.com/java/technologies/downloads/

### Issue: Application won't start
**Solution**: Try cleaning and rebuilding:
```bash
./gradlew clean desktopApp:build
./gradlew desktopApp:run
```

### Issue: Database file location
**Solution**: The `expenses.db` file appears in your current directory when running the app. Check:
- Your project root directory
- Or wherever you executed the `./gradlew desktopApp:run` command

### Issue: Date format error
**Solution**: Use only YYYY-MM-DD format:
- `2026-03-31` ✅
- `2026-3-31` ❌ (single digit month)
- Use `2026-03-05` not `2026-3-5`

---

## 📚 Learn More

For detailed information, see:
- `EXPENSE_MANAGER_README.md` - Full documentation
- `desktopApp/src/jvmMain/kotlin/database/ExpenseDatabase.kt` - Database code
- `desktopApp/src/jvmMain/kotlin/ui/ExpenseManagerUI.kt` - UI code

---

## ✨ Features Summary

| Feature | Status |
|---------|--------|
| Add expenses | ✅ |
| List expenses | ✅ |
| Delete expenses | ✅ |
| Toggle status | ✅ |
| Sort by amount | ✅ |
| SQLite persistence | ✅ |
| Clean UI | ✅ |
| Form validation | ✅ |
| Automatic database | ✅ |

---

**Happy expense tracking! 💰📊**


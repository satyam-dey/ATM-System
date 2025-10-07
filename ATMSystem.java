import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ATMSystem {
    // File to persist accounts
    private static final String DATA_FILE = "accounts.db";

    // In-memory storage
    private final Map<String, Account> accounts;

    // Input scanner
    private final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        ATMSystem atm = new ATMSystem();
        atm.start();
    }

    public ATMSystem() {
        accounts = loadAccounts();
        if (accounts.isEmpty()) {
            // Optional: create a demo account for quick testing
            Account demo = Account.createNew("Demo User", 5000.0, "1234");
            accounts.put(demo.getAccountNumber(), demo);
            saveAccounts();
            System.out.println("Demo account created. Account Number: " + demo.getAccountNumber() + " PIN: 1234");
        }
    }

    private void start() {
        System.out.println("=== Welcome to Java ATM Simulator ===");
        boolean running = true;
        while (running) {
            System.out.println("\nMain Menu:");
            System.out.println("1. Create new account");
            System.out.println("2. Login to account");
            System.out.println("3. List accounts (admin)"); // simple utility
            System.out.println("4. Exit");
            System.out.print("Choose (1-4): ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> createAccountFlow();
                case "2" -> loginFlow();
                case "3" -> listAccounts();
                case "4" -> {
                    running = false; saveAccounts(); System.out.println("Goodbye!");
                }
                default -> System.out.println("Invalid choice, try again.");
            }
        }
    }

    // ------------------ Flows ------------------

    private void createAccountFlow() {
        System.out.println("\n--- Create Account ---");
        System.out.print("Full name: ");
        String name = scanner.nextLine().trim();
        double initial = 0.0;
        while (true) {
            System.out.print("Initial deposit (>=0): ");
            String s = scanner.nextLine().trim();
            try {
                initial = Double.parseDouble(s);
                if (initial < 0) throw new NumberFormatException();
                break;
            } catch (NumberFormatException ex) {
                System.out.println("Enter valid non-negative number.");
            }
        }
        String pin = readPinWithConfirm();
        Account acc = Account.createNew(name, initial, pin);
        accounts.put(acc.getAccountNumber(), acc);
        saveAccounts();
        System.out.println("Account created! Account Number: " + acc.getAccountNumber());
    }

    private void loginFlow() {
        System.out.println("\n--- Login ---");
        System.out.print("Account Number: ");
        String accNum = scanner.nextLine().trim();
        Account acc = accounts.get(accNum);
        if (acc == null) {
            System.out.println("Account not found.");
            return;
        }
        String pin = readPin("Enter PIN: ");
        if (!acc.checkPin(pin)) {
            System.out.println("Incorrect PIN.");
            return;
        }
        System.out.println("Welcome, " + acc.getHolderName() + "!");
        accountMenu(acc);
    }

    private void accountMenu(Account acc) {
        boolean logged = true;
        while (logged) {
            System.out.println("\nAccount Menu:");
            System.out.println("1. Show balance");
            System.out.println("2. Deposit");
            System.out.println("3. Withdraw");
            System.out.println("4. Transfer");
            System.out.println("5. Mini-statement (last 10)");
            System.out.println("6. Change PIN");
            System.out.println("7. Logout");
            System.out.print("Choose (1-7): ");
            String c = scanner.nextLine().trim();
            switch (c) {
                case "1" -> System.out.printf("Balance: %.2f\n", acc.getBalance());
                case "2" -> depositFlow(acc);
                case "3" -> withdrawFlow(acc);
                case "4" -> transferFlow(acc);
                case "5" -> printMiniStatement(acc, 10);
                case "6" -> changePinFlow(acc);
                case "7" -> {
                    logged = false; saveAccounts(); System.out.println("Logged out.");
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private void depositFlow(Account acc) {
        System.out.print("Amount to deposit: ");
        String s = scanner.nextLine().trim();
        try {
            double amt = Double.parseDouble(s);
            if (amt <= 0) { System.out.println("Amount must be positive."); return; }
            acc.deposit(amt, "Cash deposit");
            saveAccounts();
            System.out.printf("Deposited %.2f. New balance: %.2f\n", amt, acc.getBalance());
        } catch (NumberFormatException ex) { System.out.println("Invalid amount."); }
    }

    private void withdrawFlow(Account acc) {
        System.out.print("Amount to withdraw: ");
        String s = scanner.nextLine().trim();
        try {
            double amt = Double.parseDouble(s);
            if (amt <= 0) { System.out.println("Amount must be positive."); return; }
            if (acc.withdraw(amt, "Cash withdrawal")) {
                saveAccounts();
                System.out.printf("Withdrawn %.2f. New balance: %.2f\n", amt, acc.getBalance());
            } else {
                System.out.println("Insufficient balance.");
            }
        } catch (NumberFormatException ex) { System.out.println("Invalid amount."); }
    }

    private void transferFlow(Account acc) {
        System.out.print("Target Account Number: ");
        String target = scanner.nextLine().trim();
        if (target.equals(acc.getAccountNumber())) { System.out.println("Cannot transfer to same account."); return; }
        Account tgt = accounts.get(target);
        if (tgt == null) { System.out.println("Target account not found."); return; }
        System.out.print("Amount to transfer: ");
        String s = scanner.nextLine().trim();
        try {
            double amt = Double.parseDouble(s);
            if (amt <= 0) { System.out.println("Amount must be positive."); return; }
            if (!acc.withdraw(amt, "Transfer to " + target)) { System.out.println("Insufficient balance."); return; }
            tgt.deposit(amt, "Transfer from " + acc.getAccountNumber());
            // add detailed transaction notes:
            acc.addTransaction(new Transaction(Transaction.Type.TRANSFER_OUT, amt, "To " + target));
            tgt.addTransaction(new Transaction(Transaction.Type.TRANSFER_IN, amt, "From " + acc.getAccountNumber()));
            saveAccounts();
            System.out.printf("Transferred %.2f to %s. Your new balance: %.2f\n", amt, target, acc.getBalance());
        } catch (NumberFormatException ex) { System.out.println("Invalid amount."); }
    }

    private void changePinFlow(Account acc) {
        System.out.println("Change PIN:");
        String oldPin = readPin("Enter current PIN: ");
        if (!acc.checkPin(oldPin)) { System.out.println("Incorrect current PIN."); return; }
        String newPin = readPinWithConfirm();
        acc.changePin(newPin);
        saveAccounts();
        System.out.println("PIN changed successfully.");
    }

    private void printMiniStatement(Account acc, int n) {
        System.out.println("\n--- Mini Statement ---");
        List<Transaction> last = acc.getLastNTransactions(n);
        if (last.isEmpty()) {
            System.out.println("No transactions yet.");
            return;
        }
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        for (Transaction t : last) {
            System.out.printf("%s | %-12s | %8.2f | %s\n",
                    t.getTimestamp().format(fmt),
                    t.getType(),
                    t.getAmount(),
                    t.getDetails() == null ? "" : t.getDetails());
        }
    }

    private void listAccounts() {
        System.out.println("\n--- Accounts in system (for testing) ---");
        if (accounts.isEmpty()) { System.out.println("No accounts."); return; }
        for (String k : accounts.keySet()) {
            Account a = accounts.get(k);
            System.out.printf("Account: %s | Name: %s | Balance: %.2f\n", k, a.getHolderName(), a.getBalance());
        }
    }

    // --------------- Persistence ----------------
    @SuppressWarnings("unchecked")
    private Map<String, Account> loadAccounts() {
        File f = new File(DATA_FILE);
        if (!f.exists()) return new HashMap<>();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            Object o = ois.readObject();
            if (o instanceof HashMap) {
                return (HashMap<String, Account>) o;
            } else {
                System.out.println("Data file format mismatch. Starting fresh.");
                return new HashMap<>();
            }
        } catch (Exception e) {
            System.out.println("Could not read data file: " + e.getMessage());
            return new HashMap<>();
        }
    }

    private void saveAccounts() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(accounts);
        } catch (Exception e) {
            System.out.println("Failed to save accounts: " + e.getMessage());
        }
    }

    // --------------- Helpers ----------------

    private String readPinWithConfirm() {
        while (true) {
            String p1 = readPin("Set PIN (4-8 digits recommended): ");
            String p2 = readPin("Confirm PIN: ");
            if (!p1.equals(p2)) { System.out.println("PINs do not match. Try again."); continue; }
            if (p1.length() < 3) { System.out.println("PIN too short."); continue; }
            return p1;
        }
    }

    private String readPin(String prompt) {
        Console console = System.console();
        if (console != null) {
            char[] pass = console.readPassword(prompt);
            return new String(pass);
        } else {
            System.out.print(prompt);
            return scanner.nextLine().trim();
        }
    }

    // ------------------ Inner classes ------------------

    static class Account implements Serializable {
        private static final long serialVersionUID = 1L;
        private final String accountNumber;
        private final String holderName;
        private double balance;
        private String pinHash;      // hex string
        private String saltBase64;   // salt stored as base64
        private final List<Transaction> transactions = new ArrayList<>();

        private Account(String accountNumber, String holderName, double initialBalance, String pinHash, String saltBase64) {
            this.accountNumber = accountNumber;
            this.holderName = holderName;
            this.balance = initialBalance;
            this.pinHash = pinHash;
            this.saltBase64 = saltBase64;
            // initial deposit transaction if > 0
            if (initialBalance > 0) {
                transactions.add(new Transaction(Transaction.Type.DEPOSIT, initialBalance, "Initial deposit"));
            }
        }

        public static Account createNew(String holderName, double initialBalance, String plainPin) {
            String accNum = generateAccountNumber();
            byte[] salt = generateSalt();
            String saltB64 = Base64.getEncoder().encodeToString(salt);
            String hash = hashPin(plainPin, salt);
            return new Account(accNum, holderName, initialBalance, hash, saltB64);
        }

        // ---------- Business operations ----------
        public synchronized void deposit(double amount, String details) {
            balance += amount;
            transactions.add(new Transaction(Transaction.Type.DEPOSIT, amount, details));
        }

        public synchronized boolean withdraw(double amount, String details) {
            if (amount <= balance) {
                balance -= amount;
                transactions.add(new Transaction(Transaction.Type.WITHDRAW, amount, details));
                return true;
            }
            return false;
        }

        public synchronized void addTransaction(Transaction t) {
            transactions.add(t);
        }

        public synchronized List<Transaction> getLastNTransactions(int n) {
            int size = transactions.size();
            if (size == 0) return Collections.emptyList();
            int from = Math.max(0, size - n);
            return new ArrayList<>(transactions.subList(from, size));
        }

        public String getAccountNumber() { return accountNumber; }
        public String getHolderName() { return holderName; }
        public double getBalance() { return balance; }

        // PIN check
        public boolean checkPin(String plainPin) {
            byte[] salt = Base64.getDecoder().decode(saltBase64);
            String hash = hashPin(plainPin, salt);
            return hash.equals(pinHash);
        }

        public void changePin(String newPin) {
            byte[] salt = generateSalt();
            this.saltBase64 = Base64.getEncoder().encodeToString(salt);
            this.pinHash = hashPin(newPin, salt);
            transactions.add(new Transaction(Transaction.Type.DEPOSIT, 0, "PIN changed")); // note: type is arbitrary here
        }

        // ------------ Utilities --------------
        private static String generateAccountNumber() {
            // 10-digit numeric account number
            Random rnd = new Random();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 10; i++) sb.append(rnd.nextInt(10));
            return sb.toString();
        }

        private static byte[] generateSalt() {
            byte[] salt = new byte[16];
            new SecureRandom().nextBytes(salt);
            return salt;
        }

        private static String hashPin(String pin, byte[] salt) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                md.update(salt);
                byte[] hashed = md.digest(pin.getBytes(StandardCharsets.UTF_8));
                return bytesToHex(hashed);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException("Hashing algorithm not available.", e);
            }
        }

        private static String bytesToHex(byte[] bytes) {
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        }
    }

    static class Transaction implements Serializable {
        private static final long serialVersionUID = 1L;
        enum Type {DEPOSIT, WITHDRAW, TRANSFER_IN, TRANSFER_OUT}
        private final Type type;
        private final double amount;
        private final LocalDateTime timestamp;
        private final String details;

        public Transaction(Type type, double amount, String details) {
            this.type = type;
            this.amount = amount;
            this.timestamp = LocalDateTime.now();
            this.details = details;
        }

        public Type getType() { return type; }
        public double getAmount() { return amount; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public String getDetails() { return details; }
    }
}

# ATM-System

An **Object-Oriented ATM System** application built using **Java**. This project simulates core banking functionalities, allowing users to perform standard transactions like checking balances, depositing, and withdrawing funds.

---

## 🚀 Features

* **Account Management:** Secure storage and retrieval of account data.
* **Balance Inquiry:** Check the current balance of an account.
* **Deposit:** Add funds to an account.
* **Withdrawal:** Deduct funds from an account (with checks for sufficient balance).
* **Transaction Tracking:** Basic recording or logging of transaction types.
* **Database Integration:** Uses a simple **`accounts.db`** file (likely a SQLite or similar simple database file) for persistence.

---

## 🛠️ Technologies Used

* **Java:** The primary programming language used to build the system.
* **Database:** A simple database file (`accounts.db`) is used for storing and managing account information.

---

## 📂 Project Structure

The repository contains the following key files:

| File Name | Description |
| :--- | :--- |
| `ATMSystem.java` | The main class containing the entry point (`main` method) of the application. |
| `ATMSystem.class` | Compiled bytecode for the main application class. |
| `ATMSystem$Account.class` | Compiled class file, likely representing the **Account** object or an inner class related to account operations. |
| `ATMSystem$Transaction.class` | Compiled class file, likely representing the **Transaction** object. |
| `ATMSystem$TransactionType.class` | Compiled class file, likely an **Enum** defining the types of transactions (e.g., DEPOSIT, WITHDRAW, BALANCE_INQUIRY). |
| `accounts.db` | The database file used to store user account details. |
| `.gitignore` | Specifies intentionally untracked files to ignore (e.g., compiled `.class` files, IDE-specific files). |

---

## 💻 Getting Started

### Prerequisites

You'll need the following installed on your machine:

* **Java Development Kit (JDK) 8 or newer**

### Installation and Setup

1.  **Clone the repository:**
    ```bash
    git clone [https://github.com/your-username/ATM-System.git](https://github.com/your-username/ATM-System.git)
    cd ATM-System
    ```

2.  **Ensure Database Connection:** The application is designed to read and write to the `accounts.db` file. Make sure this file is present in the project's root directory and that the Java code has the necessary drivers and permissions to access it (if an external driver is needed, you may need to specify it here).

3.  **Compile the Java files (if necessary):**
    *Since the `.class` files are already included, you might be able to skip this step.*
    ```bash
    javac ATMSystem.java
    ```

### Running the Application

1.  **Execute the compiled application:**
    ```bash
    java ATMSystem
    ```

2.  The application will start in the console, prompting you for account credentials and transaction choices. Follow the on-screen instructions to interact with the ATM system.

---

## 🤝 Contribution

Feel free to **fork** this repository, submit **issues**, or propose **pull requests** to help improve this project.

1.  Fork the Project
2.  Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3.  Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4.  Push to the Branch (`git push origin feature/AmazingFeature`)
5.  Open a Pull Request


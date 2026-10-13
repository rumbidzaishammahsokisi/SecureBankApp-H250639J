# Secure Banking Application - H250639J

## Student Info
Name: Rumbidzai Shammah Sokisi
Reg: H250639J
Course: Secure Coding 

## What This Project Does
This is a console-based banking app. A user can register, login, check balance, deposit and withdraw money. All data is saved in files so it does not disappear when you close the app.

## Features Done
1.  User Registration and Login
2.  Account Creation
3.  Deposit Money
4.  Withdraw Money
5.  View Balance
6.  Transaction History saved to file

## OOP Principles Used
- Encapsulation: All fields in User.java and BankAccount.java are private. Accessed via getters.
- Abstraction: BankAccount class hides deposit/withdraw logic.
- Separation of Concerns: 6 different classes for different jobs (Auth, Security, File, etc.)

## Secure Coding Practices Used
1.  Password Hashing: We use SHA-256 + Base64. No plain text password in users.txt
2.  Input Validation: Check if username >=3 chars, password >=6 chars, amount >0
3.  Error Handling: Used try-catch for file reading/writing
4.  No Hardcoded Secrets

## File Persistence
- users.txt -> stores username and hashed password
- accounts.txt -> stores accountNumber, owner, balance
- These files are created automatically when you run the program.

## How to Run
1. Open terminal in the project folder
2. Compile: javac *.java
3. Run: java Main
4. Follow menu: 1=Register, 2=Login, then banking menu

## GitHub Repo Link
https://github.com/rumbidzaishammahsokisi/SecureBankApp-H250639J
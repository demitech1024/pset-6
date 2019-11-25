import java.io.IOException;
import java.util.Scanner;

public class ATM {
    
    private Scanner in;
    private BankAccount activeAccount;
    private Bank bank;

    public static final int VIEW = 1;
    public static final int DEPOSIT = 2;
    public static final int WITHDRAW = 3;
    public static final int TRANSFER = 4;
    public static final int LOGOUT = 5;

    public static final int INVALID = 0;
    public static final int INSUFFICIENT = 1;
    public static final int OVERFLOW = 2;
    public static final int INVALID_DEST = 3;
    public static final int RECURSIVE_TRANSFER = 4;
    public static final int SUCCESS = 9;

    public static final int FIRST_NAME_WIDTH = 20;
    public static final int LAST_NAME_WIDTH = 30;
    
    ///////////////////// ///////////////////////////////////////////////////////
    //                                                                        //
    // Refer to the Simple ATM tutorial to fill in the details of this class. //
    // You'll need to implement the new features yourself.                    //
    //                                                                        //
    ////////////////////////////////////////////////////////////////////////////
    
    /**
     * Constructs a new instance of the ATM class.
     */
    
    public ATM() {
        this.in = new Scanner(System.in);

        try {
			this.bank = new Bank();
		} catch (IOException e) {
            // cleanup any resources (i.e., the Scanner) and exit
            //in.close();
		}
    }
    
    public void startup() {
        activeAccount = null;
        System.out.println("Welcome to the AIT ATM!");
        while (true) {
            System.out.print("\nAccount No.: ");
            String temp = in.next();
            if (temp.equals("+")) {
                newAccount();
                if (activeAccount != null) {
                    System.out.printf("\nThank you. Your account number is %d.\nPlease log in to access your newly created account.\n", activeAccount.getAccountNo());
                }
            } else {
                long accountNo = Long.valueOf(temp);
            
                System.out.print("PIN        : ");
                int pin = in.nextInt();
                activeAccount = bank.login(accountNo, pin);
                if (isValidLogin(accountNo, pin)) {
                    //activeAccount = Bank.login(accountNo, pin);
                    System.out.println("\nHello again, " + activeAccount.getAccountHolder().getFirstName() + "!\n");
                    boolean validLogin = true;
                    while (validLogin) {
                        switch (getSelection()) {
                            case VIEW: showBalance(); break;
                            case DEPOSIT: deposit(); break;
                            case WITHDRAW: withdraw(); break;
                            case TRANSFER: transfer(); break;
                            case LOGOUT: validLogin = false; bank.update(activeAccount); bank.save(); break;
                            default: // invalid selection
                                System.out.println("\nInvalid selection.\n");
                                break;
                        }
                    }
                } else {
                    if (accountNo == -1 && pin == -1) {
                        shutdown();
                    } else {
                        System.out.println("\nInvalid account number and/or PIN.");
                    }
                }
            }
        }
    }

    public void newAccount() {
        boolean valid = false;
        String newFirstName = "";
        String newLastName = "";
        int newPin = 0;
        while (!valid) {
            System.out.print("\nFirst name: ");
            newFirstName = in.next().strip();

            System.out.print("Last name: ");
            newLastName = in.next().strip();

            if (newFirstName.length() > 20 || newFirstName.length() < 1|| newLastName.length() > 30 || newLastName.length() < 1) {
                System.out.println("Invalid first or last name. First name must be 1 character to 20 characters, ");
                System.out.println("while last name must be 1 character to 30 characters. To exit, type -1 for first and/or last name fields.");
                valid = false;
            } else if (newFirstName.equals("-1") || newLastName.equals("-1")) {
                System.out.println("\nExiting creation of new account...");
                return;
            } else {
                valid = true;
            }
        }
        
        valid = false;
        while (!valid) {
            System.out.print("PIN: ");
            newPin = in.nextInt();
            if (newPin == -1) {
                return;
            } else if (newPin > 9999 || newPin < 1000) {
                System.out.println("Invalid pin selected. Pin number must be no smaller (numerically) than 1000, and no greater than 9999.");
                System.out.println("If you would like to exit account creation, just type -1 for your pin selection.");
                valid = false;
            } else {
                valid = true;
            }
        }
        
        activeAccount = bank.createAccount(newPin, new User(newFirstName, newLastName));
        bank.update(activeAccount);
        bank.save();
    }

    public boolean isValidLogin(long accountNo, int pin) {
        try {
            return accountNo == activeAccount.getAccountNo() && pin == activeAccount.getPin();
        } catch (Exception e) {
            return false;
        }
        
    }

    public int getSelection() {
        System.out.println("[1] View balance");
        System.out.println("[2] Deposit money");
        System.out.println("[3] Withdraw money");
        System.out.println("[4] Transfer money");
        System.out.println("[5] Logout");
    
        String selection = in.next();
        int selectionInt = 9;
        try {
            selectionInt = Integer.valueOf(selection);
        } catch (Exception e) {}
        return selectionInt;
    }

    public void showBalance() {
        System.out.println("\nCurrent balance: " + activeAccount.getBalance() + "\n");
        bank.update(activeAccount); 
        bank.save();
    }

    public void deposit() {
        System.out.print("\nEnter amount: ");
        double depositAmt = in.nextDouble();

        int status = activeAccount.deposit(depositAmt);
        System.out.println(statusMessage(status, "Deposit"));

        bank.update(activeAccount); 
        bank.save();
    }

    public void withdraw() {
        System.out.print("\nEnter amount: ");
        double withdrawAmt = in.nextDouble();

        int status = activeAccount.withdraw(withdrawAmt);
        System.out.println(statusMessage(status, "Withdrawal"));

        bank.update(activeAccount); 
        bank.save();
        
    }

    public void transfer() {
        System.out.print("\nEnter account: ");
        long destAccNo = in.nextLong();
        System.out.print("Enter amount: ");
        double transferAmt = in.nextDouble();
        
        int status = activeAccount.transfer(transferAmt, bank.getAccount(destAccNo));
        System.out.println(statusMessage(status, "Transfer"));

        bank.update(activeAccount);
        try {
            bank.update(bank.getAccount(destAccNo));
        } catch (Exception e) {}
        bank.save();
    }

    public String statusMessage(int status, String type) {
        String output = "\n" + type;

        switch (status) {
            case INVALID:
                output += " rejected. Amount must be greater than $0.00.\n";
                break;
            case INVALID_DEST:
                output += " rejected. Destination account not found.\n";
                break;
            case RECURSIVE_TRANSFER:
                output += " rejected. Destination account matched origin.\n";
                break;
            case OVERFLOW:
                if (type.equals("Transfer")){
                    output += " rejected. Amount would cause destination balance to exceed $999,999,999,999.99.\n"; 
                } else {
                    output += " rejected. Amount would cause balance to exceed $999,999,999,999.99.\n";
                }
                break;
            case INSUFFICIENT:
                output += " rejected. Insufficient funds.\n";
                break;
            default:
                output += " accepted.\n";
                break;
        }

        return output;
    }

    public void shutdown() {
        if (in != null) {
            in.close();
        }

        System.out.println("\nGoodbye!");
        System.exit(0);
    }
   
    /*
     * Application execution begins here.
     */
    
    public static void main(String[] args) {
        ATM atm = new ATM();

        atm.startup();
    }
}

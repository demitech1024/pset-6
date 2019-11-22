import java.io.IOException;
import java.util.Scanner;

public class ATM {
    
    private Scanner in;
    private BankAccount activeAccount;
    private Bank bank;

    public static final int VIEW = 1;
    public static final int DEPOSIT = 2;
    public static final int WITHDRAW = 3;
    public static final int LOGOUT = 4;
    
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
        System.out.println("Welcome to the AIT ATM!");
        while (true) {
            System.out.print("\nAccount No.: ");
            String temp = in.next();
            if (temp.equals("+")) {
                newUser();
                System.out.printf("\nThank you. Your account number is %d.\nPlease log in to access your newly created account.\n", activeAccount.getAccountNo());
            } else {
                long accountNo = Long.valueOf(temp);
            
                System.out.print("PIN        : ");
                int pin = in.nextInt();
                
                if (isValidLogin(accountNo, pin)) {
                    activeAccount = Bank.login(accountNo, pin);
                    System.out.println("\nHello again, " + activeAccount.getAccountHolder().getFirstName() + "!\n");
                    boolean validLogin = true;
                    while (validLogin) {
                        switch (getSelection()) {
                            case VIEW: showBalance(); break;
                            case DEPOSIT: deposit(); break;
                            case WITHDRAW: withdraw(); break;
                            case LOGOUT: validLogin = false; break;
                            default: // invalid selection
                                System.out.println("\nInvalid selection.\n");
                                break;
                        }
                    }
                } else {
                    if (accountNo == -1 && pin == -1) {
                        shutdown();
                    } else {
                        System.out.println("\nInvalid account number and/or PIN.\n");
                    }
                }
            }
            
        }
    }

    public void newUser() {
        System.out.print("\nFirst name: ");
        String newFirstName = in.next();
        System.out.print("Last name: ");
        String newLastName = in.next();
        System.out.print("PIN: ");
        int newPin = in.nextInt();
        activeAccount = new BankAccount(newPin, new User(newFirstName, newLastName));
    }

    public boolean isValidLogin(long accountNo, int pin) {
        return accountNo == activeAccount.getAccountNo() && pin == activeAccount.getPin();
    }

    public int getSelection() {
        System.out.println("[1] View balance");
        System.out.println("[2] Deposit money");
        System.out.println("[3] Withdraw money");
        System.out.println("[4] Logout");

        return in.nextInt();
    }

    public void showBalance() {
        System.out.println("\nCurrent balance: " + activeAccount.getBalance() + "\n");
    }

    public void deposit() {
        System.out.print("\nEnter amount: ");
        try {
            double depositAmt = in.nextDouble();

            activeAccount.deposit(depositAmt);
        } catch (Error e) {
            
        }
        System.out.println();
    }

    public void withdraw() {
        System.out.print("\nEnter amount: ");
        double withdrawAmt = in.nextDouble();

        activeAccount.withdraw(withdrawAmt);
        System.out.println();
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

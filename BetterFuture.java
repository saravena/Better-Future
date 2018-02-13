import java.util.*;
import java.sql.*;
import java.lang.*;

public class BetterFuture {

	public static Connection connection;
	public static Statement statement;
	public static ResultSet resultSet;
	public static String query;

	public static Scanner kb = new Scanner(System.in);

	public static void main(String[] args) {

		try {
			DriverManager.registerDriver (new oracle.jdbc.driver.OracleDriver());
			String url = "jdbc:oracle:thin:@class3.cs.pitt.edu:1521:dbclass";
			connection = DriverManager.getConnection(url, "bml49", "3985224");
		}
		catch(Exception ex) {
			System.out.println("Error connecting to database.");
			ex.printStackTrace();
		}

		System.out.println("What type of user are you?");
		System.out.println("\t1.\tAdministrator\n\t2.\tCustomer");
		int user = kb.nextInt();
		// checks if user is an administrator
		if (user == 1) {
			adminInterface();
		}
		// checks if user is a returning customer
		else if (user == 2) {
			customerInterface();
		}
		else {
			System.out.println("Incorrect input.");
		}
	} // end main()

	// interface used for the administrator
	public static void adminInterface() {
		int menuChoice = 0;
		Admin admin = new Admin();
		admin = adminLogin();

		while (menuChoice < 6) {
			System.out.println("\nChoose one of the following:");
			System.out.println("\t1.\tNew user registration");
			System.out.println("\t2.\tUpdate share quotes for a day");
			System.out.println("\t3.\tAdd a new mutual fund");
			System.out.println("\t4.\tUpdate the time and date");
			System.out.println("\t5.\tView current statistics");
			System.out.println("\t6.\tExit");
			// determines input
			menuChoice = checkInput();
			if (menuChoice == -1) continue;
			// different menu options
			switch (menuChoice) {
				// NEW USER REGISTRATION
				case 1:
					boolean ifSuccess = false;
					System.out.println("Is the new user and administrator? (Y/N)");
					String isAdmin = kb.next();
					if (isAdmin.charAt(0) == 'Y' || isAdmin.charAt(0) == 'y') {
						// insert new login into Admin db
						System.out.println("Please enter the following information:");
						System.out.print("New Login: ");
						String adminLog = kb.next();
						// confirm login is okay
						while (admin.checkLogin(adminLog, true) != true) {
							System.out.println("This login already exists. Please enter a new one.");
							System.out.print("New Login: ");
							adminLog = kb.next();
						} // end while
						System.out.print("Password: ");
						String adminPass = kb.next();
						System.out.print("First Name: ");
						String firstName = kb.next();
						System.out.print("Last Name: ");
						String lastName = kb.next();
						String adminName = firstName + " " + lastName;
						System.out.print("Email: ");
						String adminEmail = kb.next();
						System.out.print("Address: ");
						String adminAddress = kb.next();

						// embedded sql
						try {
							String update = "insert into ADMINISTRATOR values(?, ?, ?, ?, ?)";
							PreparedStatement ps = connection.prepareStatement(update);
							ps.setString(1, adminLog);
							ps.setString(2, adminName);
							ps.setString(3, adminEmail);
							ps.setString(4, adminAddress);
							ps.setString(5, adminPass);
							ps.executeUpdate();

							System.out.println("\nAdministrator data has been stored successfully!\n");
						}
						catch (Exception ex) {
							ex.printStackTrace();
						}
					}
					// If the new user is not an admin
					else {
						System.out.println("Please enter the following information:");
						System.out.println("New Login: ");
						String customerLog = kb.next();
						// confirm login is okay
						while (admin.checkLogin(customerLog, false) != true) {
							System.out.println("This login already exists. Please enter a new one.");
							System.out.print("New Login: \n");
							customerLog = kb.next();
						} // end while
						System.out.print("Password: ");
						String customerPass = kb.next();
						System.out.print("First Name: ");
						String firstName = kb.next();
						System.out.print("Last Name: ");
						String lastName = kb.next();
						String customerName = firstName + " " + lastName;
						System.out.print("Email: ");
						String customerEmail = kb.next();
						System.out.print("Address: ");
						String customerAddress = kb.next();

						// embedded sql
						try {
							String update = "insert into CUSTOMER values (?, ?, ?, ?, ?, 0)";
							PreparedStatement ps = connection.prepareStatement(update);
							ps.setString(1, customerLog);
							ps.setString(2, customerName);
							ps.setString(3, customerEmail);
							ps.setString(4, customerAddress);
							ps.setString(5, customerPass);
							ps.executeUpdate();
							// sql

							System.out.println("\nCustomer data has been stored successfully!\n");
						}
						catch (Exception ex) {
							ex.printStackTrace();
						}
					}
					break;
				// UPDATE SHARE QUOTES FOR THE DAY
				case 2:
					String choice = "";
					System.out.println("Which mutual fund would you like to update the share quotes for?");
					System.out.println("\t1. Money Market\n\t2. Real-Estate\n\t3. Short-term Bonds\n\t"
																+"4. Long-term Bonds\n\t5. Balance Bonds Stocks\n\t"
																+"6. Social Responsibility Bonds Stocks\n\t7. General Stocks\n\t"
																+"8. Aggressive Stocks\n\t9. International Market Stocks\n\n");
					int fundChoice = kb.nextInt();
					// proceeds depending on mutual fund choice
					switch(fundChoice) {
						case 1:
								choice = "money-market";
								admin.updateShare(choice);
								break;
						case 2:
								choice = "real-estate";
								admin.updateShare(choice);
								break;
						case 3:
								choice = "short-term-bonds";
								admin.updateShare(choice);
								break;
						case 4:
								choice = "long-term-bonds";
								admin.updateShare(choice);
								break;
						case 5:
								choice = "balance-bonds-stocks";
								admin.updateShare(choice);
								break;
						case 6:
								choice = "social-responsibility-bonds-stocks";
								admin.updateShare(choice);
								break;
						case 7:
								choice = "general-stocks";
								admin.updateShare(choice);
								break;
						case 8:
								choice = "aggressive-stocks";
								admin.updateShare(choice);
								break;
						case 9:
								choice = "international-markets-stocks";
								admin.updateShare(choice);
								break;
						default:
								System.out.println("Error: Please try again");
					} // end switch
					break;
				// ADD A NEW MUTUAL FUND
				case 3:
					System.out.println("What's the symbol for the mutual fund you want to add? ");
					String symbol = kb.next();
					System.out.println("Name? ");
					String name = kb.next();
					System.out.println("Description? ");
					String description = kb.nextLine();
					System.out.println("What category will this fund be put under: ");
					System.out.println("\t1. Bonds\n\t2. Stocks\n\t3. Fixed\n\t4. Mixed");
					int cat = kb.nextInt();
					String category = null;

					// Possibly do category check here instead of the SQL level?
					switch(cat) {
						case 1:
								category = "bonds";
								break;
						case 2:
								category = "stocks";
								break;
						case 3:
								category = "fixed";
								break;
						case 4:
								category = "mixed";
								break;
						default:
								System.out.println("Error: Please try again.");
						admin.addFund(symbol, name, description, category);

					} // end switch
					break;
				// UPDATE THE TIME AND DATE
				case 4:
					admin.updateTime();
					break;
				// VIEW CURRENT STATISTICS
				case 5:
					int monthNum;
					int top;

					System.out.print("How many past months of information would you like to see?: ");
					monthNum = kb.nextInt();
					// checks for valid month inout
					while (monthNum < 0) {
						System.out.println("Error: Number of months cannot be below zero.");
						System.out.print("How many past months of information would you like to see?: ");
						monthNum = kb.nextInt();
					} // end while

					System.out.print("Please enter the top number of highest volume categories and "
															+"investors you would like to see: ");
					top = kb.nextInt();
					// check for valid topk input
					while (top < 0) {
						System.out.println("Error: Number cannot be below zero.");
						System.out.print("Please enter the top number of highest volume categories and "
																+"investors you would like to see: ");
						top = kb.nextInt();
					} // end while
					// print stats
					admin.printStats(monthNum, top);
					break;
				// EXIT
				case 6:
					// not sure if programming is ending or getting taken back to orignal
					// home menu
					// if exit program entirely
					System.out.println("Good-bye!");
					System.exit(0);
					break;
				default:
					System.out.println("Type in a number corresponding to your choice.");
			} // end switch
		} // end while
	} // end adminInterface()

	// interface used for customer
	public static void customerInterface() {
		int menuChoice = 0;
		Customer customer = customerLogin();

		while (menuChoice < 9) {
			System.out.println("\nChoose one of the following:");
			System.out.println("\t1.\tBrowse mutual funds");
			System.out.println("\t2.\tSearch mutual funds by text");
			System.out.println("\t3.\tInvest");
			System.out.println("\t4.\tSell shares");
			System.out.println("\t5.\tBuy shares");
			System.out.println("\t6.\tInvest with conditions");
			System.out.println("\t7.\tChange allocation preferences");
			System.out.println("\t8.\tSee customer portfolio");
			System.out.println("\t9.\tExit");
			// determines user input
			menuChoice = checkInput();
			if (menuChoice == -1) continue;
			// if user wants to browse mutual funds
			if (menuChoice == 1) {
				System.out.println("Would you like to...");
				System.out.println("\t1.\tSee all mutual funds");
				System.out.println("\t2.\tSee mutual funds by category");
				System.out.println("\t3.\tSee mutual funds sorted by highest price for a specific date");
				System.out.println("\t4.\tSee mutual funds sorted alphabetically");
				// retrieves user input
				menuChoice = checkInput();
				if (menuChoice == -1) continue;

				customer.browse(menuChoice);
			}
			// if user wants to search for mutual funds
			else if (menuChoice == 2) {
				String keyword2 = "";
				System.out.println("Search for up to two words in the mutual funds.");
				System.out.print("Please enter one word: ");
				String keyword1 = kb.next();
				System.out.println("Would you like to enter a another word? (Y/N)");
				String input = kb.next();
				if (input.equals("Y") || input.equals("y")) {
					System.out.print("Please enter another word: ");
					keyword2 = kb.next();
				} else {
					keyword2 = null;
				}
				customer.search(keyword1, keyword2);
			}
			// if user wants to invest
			else if (menuChoice == 3) {
				System.out.print("How much would you like to invest? ");
				float invest = kb.nextFloat();

				//** this doesn't do anything! **/
				customer.invest(invest);
			}
			// if user would like to sell shares
			else if (menuChoice == 4) {
				System.out.print("What is the mutual fund symbol? ");
				String symbol = kb.next();
				System.out.print("How many shares would you like to sell? ");
				int shares = kb.nextInt();

				customer.sell(symbol, shares);
			}
			// if user would like to buy shares
			else if (menuChoice == 5) {
				System.out.print("What is the mutual fund symbol? ");
				String symbol = kb.next();
				System.out.println("Would you like to buy by \n\t1.)\tnumber of shares \n\t2.)\tprice amount");
				menuChoice = checkInput();
				if (menuChoice == -1) continue;

				customer.buy(symbol, menuChoice);
			}
			// if user wants to invest with conditions
			else if (menuChoice == 6) {
				//** this doesn't do anything! **/
				customer.conditionInvest();
			}
			// if user wants to change allocation preferences
			else if (menuChoice == 7) {
				System.out.println("What is the allocation number of the preference you'd like to change? ");
				int number = kb.nextInt();
				customer.changePreference(number);
			}
			// if user wants to see there porfolio
			else if (menuChoice == 8) {
				StringBuilder date = new StringBuilder();
				System.out.println("Please enter a date in number format.");
				System.out.print("Day (dd): ");
				String day = kb.next();
				date.append(day+"-");
				System.out.print("Month (mm): ");
				String month = kb.next();
				date.append(month+"-");
				System.out.print("Year (yy): ");
				String year = kb.next();
				date.append(year);
				customer.printPortfolio(date.toString());
			}
		} // end while
	} // end customerInterface()

	// checks for valid user input
	public static int checkInput() {
		try {
			int input = kb.nextInt();
			if (input < 1) input = -1;
			return input;
		} catch (IllegalArgumentException iae) {
			System.out.println("Type in the number corresponding to your choice.");
			return -1;
		}
	} // end checkInput()

	// Retrieves customer login information
	public static Customer customerLogin() {
		System.out.println("Login name: ");
		String login = kb.next();
		System.out.println("Password: ");
		String password = kb.next();
		String name = "";
		String email = "";
		String address = "";
		float balance = 0;

		try {
			query = "select * from CUSTOMER where login = ? and password = ?";
			PreparedStatement updateStatement = connection.prepareStatement(query);
			updateStatement.setString(1, login);
			updateStatement.setString(2, password);
			resultSet = updateStatement.executeQuery();

			if (resultSet.next()) {
				name = resultSet.getString("name");
				email = resultSet.getString("email");
				address = resultSet.getString("address");
				balance = resultSet.getFloat("balance");
			}
			else {
				System.out.println("The username or password is incorrect.");
				System.exit(0);
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
			System.exit(0);
		}
		return new Customer(login, name, email, address, balance);
	} // end customerLogin()

	// Retrieves admin login information
	public static Admin adminLogin() {
		System.out.println("Login name: ");
		String login = kb.next();
		System.out.println("Password: ");
		String password = kb.next();
		String real_password = "";
		String name = "";
		String email = "";
		String address = "";

		//** embedded sql **//
		//call check_login_admin(login, real_password, name, email, address);
		try {
			query = "select * from ADMINISTRATOR where login = ? and password = ?";
			PreparedStatement ps = connection.prepareStatement(query);
			ps.setString(1, login);
			ps.setString(2, password);
			resultSet = ps.executeQuery();

			if (resultSet.next()) {
				name = resultSet.getString("name");
				email = resultSet.getString("email");
				address = resultSet.getString("address");
			}
			else {
				System.out.println("The username or password is incorrect.");
				System.exit(0);
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
			System.exit(0);
		}

		//** embedded sql **//

		return new Admin(login, name, email, address);
	} // end adminLogin()
}

import java.util.*;
import java.sql.*;

public class Customer {
	private static String login;
	private static String name;
	private static String email;
	private static String address;
	private static float balance;
	private static java.sql.Date currDate;
	public static Scanner kb = new Scanner(System.in);
	private Connection connection;
	private Statement statement;
	private ResultSet res;
	private String query, update;
	private PreparedStatement ps;

	/** Constructor */
	public Customer(String login, String name, String email, String address, float balance) {
		try {
			DriverManager.registerDriver (new oracle.jdbc.driver.OracleDriver());
			String url = "jdbc:oracle:thin:@class3.cs.pitt.edu:1521:dbclass";
			connection = DriverManager.getConnection(url, "bml49", "3985224");

			this.login = login;
			this.name = name;
			this.email = email;
			this.address = address;
			this.balance = balance;

			// hard coded date because I couldn't figure out how to get date to work
			java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("yyyy-MM-dd");
			currDate = new java.sql.Date(df.parse("21-04-17").getTime());

		}
		catch(Exception ex) {
			System.out.println("Error connecting to database.");
			ex.printStackTrace();
		}
	}

	/** The user inputs a choice from a text menu,
	 *	based on the choice, a mutual fund table is printed
	 */
	public void browse(int choice) {

		// prints the entire mutual fund table if the user choses 1 or types in a number that isn't an option
		if (choice <= 1 || choice > 4) {
			try {
				statement = connection.createStatement();
				res = statement.executeQuery("select * from MUTUALFUND");
				System.out.println("Symbol\tName\t\tDescription\t\tCategory");

				while (res.next()) {
					System.out.print(res.getString("symbol")+"\t");
					System.out.print(res.getString("name")+"\t\t");
					System.out.print(res.getString("description")+"\t\t");
					System.out.print(res.getString("category")+"\n");
				}
			} catch (Exception ex) { ex.printStackTrace(); }
		}

		// prints all the mutual funds of a specific category
		else if (choice == 2) {
			System.out.println("Which category? Fixed, bonds, mixed, or stocks?");
			String category = kb.next();

			//** sql **//
			try {
				ps = connection.prepareStatement("select * from MUTUALFUND where category = ?");
				ps.setString(1, category);

				res = ps.executeQuery();
				System.out.println("Symbol\tName\t\tDescription\t\tCategory");

				while (res.next()) {
					System.out.print(res.getString("symbol")+"\t");
					System.out.print(res.getString("name")+"\t\t");
					System.out.print(res.getString("description")+"\t\t");
					System.out.print(res.getString("category")+"\n");
				}
			} catch (Exception ex) { ex.printStackTrace(); }
			//** sql **//
		}

		// prints all the mutual funds that were created on a user specified day, ordered by price ascending
		else if (choice == 3) {
			System.out.print("Type in the date (dd-mm-yy): ");
			String date_input = kb.next();

			try {
				java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("dd-mm-yy");
				java.sql.Date input_date = new java.sql.Date(df.parse(date_input).getTime());

				//** sql **//
				query = "select * "+
						"from MUTUALFUND natural join CLOSINGPRICE "+
						"where c_date = ? "+
						"order by price asc";
				ps = connection.prepareStatement(query);
				ps.setDate(1, input_date);

				res = ps.executeQuery();

				System.out.println("Symbol\tName\t\tDescription\t\tCategory");

				while (res.next()) {
					System.out.print(res.getString("symbol")+"\t");
					System.out.print(res.getString("name")+"\t\t");
					System.out.print(res.getString("description")+"\t\t");
					System.out.print(res.getString("category")+"\n");
				}
			} catch (Exception ex) { ex.printStackTrace(); }
			//** sql **//
		}

		// prints all the mutual funds in order by name ascending
		else {
			try {
				statement = connection.createStatement();
				res = statement.executeQuery("select * from MUTUALFUND order by name asc");

				System.out.println("Symbol\tName\t\tDescription\t\tCategory");

				while (res.next()) {
					System.out.print(res.getString("symbol")+"\t");
					System.out.print(res.getString("name")+"\t\t");
					System.out.print(res.getString("description")+"\t\t");
					System.out.print(res.getString("category")+"\n");
				}
			} catch (Exception ex) { ex.printStackTrace(); }
		}
	}

	/** searches descriptions of mutual funds for the user specified keywords
	 */
	public void search(String key1, String key2) {
		//** sql **//
		try {
			query = "select * from MUTUALFUND "+
					"where description like %?% or description like %?%";
			ps = connection.prepareStatement(query);
			ps.setString(1, key1);
			ps.setString(2, key2);

			res = ps.executeQuery();

			System.out.println("Symbol\tName\t\tDescription\t\tCategory");

			while (res.next()) {
				System.out.print(res.getString("symbol")+"\t");
				System.out.print(res.getString("name")+"\t\t");
				System.out.print(res.getString("description")+"\t\t");
				System.out.print(res.getString("category")+"\n");
			}
		} catch (Exception ex) { ex.printStackTrace(); }
		//** sql **//
	}

	/* a certain percentage of the amount is invested in buying a specific mutual fund
	 * this doesn't check to make sure all the possible mutual funds can be bought
	 */
	public void invest(float total_amount) {
		//** sql **//
		try {
			update = "insert into TRXLOG(trans_id, login, t_date, action, amount) values(1, ?, ?, 'deposit', ?)";
			ps = connection.prepareStatement(update);
			ps.setString(1, login);
			ps.setDate(2, currDate);
			ps.setFloat(3, total_amount);
			ps.executeUpdate();
			// this triggers 'on_deposit_log'
		} catch (Exception ex) { ex.printStackTrace(); }
		//** sql **//

		balance -= total_amount;
	}

	/** return the price of one share and the total price of all shares sold
	 *	this data gets inserted into the trxlog table which triggers an increase on customer's balance
	 *	and the customer object's balance is updated as well
	 */
	public void sell(String symbol, int shares) {
		float total_price = 0;
		float price_of_one_share = 0;

		//** sql **//
		try {
			query = "select price * shares, price "+
					"from MUTUALFUND natural join CLOSINGPRICE "+
					"where symbol = ?";
			ps = connection.prepareStatement(query);
			ps.setString(1, symbol);

			res = ps.executeQuery();

			total_price = res.getInt(1);
			price_of_one_share = res.getInt(2);

			update = "insert into TRXLOG values(1, ?, ?, ?, 'sell', ?, ?, ?)";
			ps = connection.prepareStatement(update);
			ps.setString(1, login);
			ps.setString(2, symbol);
			ps.setDate(3, currDate);
			ps.setInt(4, shares);
			ps.setFloat(5, price_of_one_share);
			ps.setFloat(6, total_price);
			ps.executeUpdate();
			// this will trigger 'increase_customer_balance'
		} catch (Exception ex) { ex.printStackTrace(); }
		//** sql **//

		// update the customer object balance field
		balance += total_price;
	}

	/** user can chose to buy mutual funds based on number of shares or price
	 *	this data gets inserted into the trxlog which triggers a decrease on customer's balance
	 *	and puts the newly owned funds into the owns table
	 */
	public void buy(String symbol, int choice) {

		// buying based on shares
		if (choice == 1) {
			System.out.print("How many shares would you like to buy? ");
			int shares = kb.nextInt();
			float total_price;
			float price_of_one_share;

			//** sql **//
			try {
				// finds the price of a single share of the mutual fund and the total price of all the shares
				query = "select price * ?, price "+
						"from MUTUALFUND natural join CLOSINGPRICE "+
						"where symbol = ?";
				ps = connection.prepareStatement(query);
				ps.setInt(1, shares);
				ps.setString(2, symbol);

				res = ps.executeQuery();

				total_price = res.getInt(1);
				price_of_one_share = res.getInt(2);
				//** sql **//

				if (total_price > balance) {
					System.out.println("You don't have enough money to buy this amount of shares.");
				}
				else {
					balance -= total_price;

					//** sql **//
					// try to insert an entry into trxlog
					update = "insert into TRXLOG values(1, ?, ?, ?, 'buy', ?, ?, ?)";
					ps = connection.prepareStatement(update);
					ps.setString(1, login);
					ps.setString(2, symbol);
					ps.setDate(3, currDate);
					ps.setInt(4, shares);
					ps.setFloat(5, price_of_one_share);
					ps.setFloat(6, total_price);
					ps.executeUpdate();

					// this will trigger 'decrease_customer_balance'
					//** sql **/
				}
			} catch (Exception ex) { ex.printStackTrace(); }
		}

		// buying based on price
		else if (choice == 2) {
			System.out.print("How much would you like to spend? ");
			float total_price = kb.nextFloat();
			int shares = 0;
			float price_of_one_share;

			if (total_price > balance)
				System.out.println("You don't have enough money to buy this amount of shares.");
			else {
				//** sql **//
				try {
					query = "round(?/price, 0, 1), price "+
							"from MUTUALFUND natural join CLOSINGPRICE "+
							"where symbol = ?";
					ps = connection.prepareStatement(query);
					ps.setFloat(1, total_price);
					ps.setString(2, symbol);

					res = ps.executeQuery();

					shares = (int)res.getFloat(1);
					price_of_one_share = res.getInt(2);

					update = "insert into TRXLOG values(1, ?, ?, ?, 'buy', ?, ?, ?)";
					ps = connection.prepareStatement(update);
					ps.setString(1, login);
					ps.setString(2, symbol);
					ps.setDate(3, currDate);
					ps.setInt(4, shares);
					ps.setFloat(5, price_of_one_share);
					ps.setFloat(6, total_price);
					ps.executeUpdate();
					// this will trigger 'decrease_customer_balance'

					balance -= total_price;

				} catch (Exception ex) { ex.printStackTrace(); }
				//** sql **//
			}
		}
		// incorrect input
		else System.out.println("Your only options are 1 and 2");
	}

	// invest with condiiions
	public void conditionInvest() {
		System.out.println("This option doesn't work at the moment");
	}

 	// change allocation preferences
	public void changePreference(int alloc_number) {
		
	}

	/** prints all transactions that this user has implemented */
	public void printPortfolio(String input_date) {
		try {
			java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("yyyy-mm-dd");
			java.sql.Date t_date = new java.sql.Date(df.parse(input_date).getTime());

			// printing the symbol, price, and number of shares bought on a specific date
			// as well as the current price of the mutual fund
			query = "select trx.symbol, trx.price, trx.num_shares, cp.price "+
					"from TRXLOG as trx natural join (select * from MUTUALFUND natural join CLOSINGPRICE) as cp "+
					"where t_date = ? and login = ?";
			ps = connection.prepareStatement(query);
			ps.setDate(1, t_date);
			ps.setString(2, login);

			res = ps.executeQuery();

			System.out.println("Symbol\tPrice\tNumber of Shares\tCurrent Value");

			while (res.next()) {
				System.out.print(res.getString(1)+"\t");
				System.out.print(res.getString(2)+"\t");
				System.out.print(res.getInt(3)+"\t");
				System.out.print(res.getFloat(4)+"\t\n");
			}

			// finds the yield of the customer's portfolio by subtracting the mutual funds
			// that were bought from the mutual funds that were sold
			query = "select sum(price) - (select sum(price) from TRXLOG where login = ? and action = 'buy') "+
					"from TRXLOG where login = ? and action = 'sell'";
			ps = connection.prepareStatement(query);
			ps.setString(1, login);
			ps.setString(2, login);

			res = ps.executeQuery();

			System.out.println("\nYield");
			System.out.println(res.getInt(1));

			// prints the total value of all money deposited or withdrawn by the customer
			query = "select sum(price) from TRXLOG where login = ?";
			ps = connection.prepareStatement(query);
			ps.setString(1, login);

			res = ps.executeQuery();
			res.next();

			System.out.println("\nTotal Value");
			System.out.println(res.getFloat(1));
		} catch (Exception ex) { ex.printStackTrace(); }
	}
}

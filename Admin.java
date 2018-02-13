import java.sql.*;
import java.util.*;
import java.lang.*;
import java.io.*;

public class Admin {
  String login;
  String name;
  String email;
  String address;
  public static Scanner kb = new Scanner(System.in);
  private Connection connection;
  private Statement statement;
  private ResultSet res;
  private String query, update;
  private PreparedStatement ps;

  public Admin() {
  }

  // Parameterized constructor
  public Admin(String login, String name, String email, String address) {
    this.login = login;
    this.name = name;
    this.email = email;
    this.address = address;
  } // end Customer(String, String, String, String)

  // checks if the login has been used before in either db
  public boolean checkLogin(String userlogin, boolean isAdmin) {
    boolean exists = false;
    // if user is an admin
    if (isAdmin == true)
      query = "select count(login) from ADMINISTRATOR where login = ?";

    // if registered user is a customer
    else
      query = "select count(login) from CUSTOMER where login = ?";

    try {
      ps = connection.prepareStatement(query);
      ps.setString(1, userlogin);
      res = ps.executeQuery();

      if (res.getInt(1) == 0)
        exists = false;

      else
        exists = true;
    } catch (Exception ex) { ex.printStackTrace(); }

    return exists;
  } // end checkLogin

  // updates the shares in the specified mutual fund
  public void updateShare(String choice) {
    System.out.println("Here are the following share quotes for this mutual fund:");

    try {
      // Embedded SQL code
      query = "select * from CLOSINGPRICE where symbol = ?";
      ps = connection.prepareStatement(query);
      ps.setString(1, choice);
      res = ps.executeQuery();

      System.out.println("Symbol\nPrice\nDate");

      while (res.next()) {
        System.out.println(res.getString(1)+"\t"+res.getFloat(2)+"\t"+res.getDate(3));
      }

      System.out.print("\nWhat price would you like to update the shares to: $");
      float updatePrice = kb.nextFloat();

      // updates the information
      // embedded SQL code here
      update = "insert into CLOSINGPRICE values (?, ?, 29-05-95";
      ps = connection.prepareStatement(update);
      ps.setString(1, choice);
      ps.setFloat(2, updatePrice);

      ps.executeUpdate();
    } catch (Exception ex) { ex.printStackTrace(); }

    System.out.println("The shares have been updated successfully!");
  } // end updateShare(String)

  // inserts a new mutual fund
  public void addFund(String symbol, String name, String description, String category) {
    // embedded SQL
    // INSERT INTO MUTUALFUND VALUES ();

    try {
      update = "insert into MUTUALFUND values(?, ?, ?, ?, '07-12-45)";
      ps = connection.prepareStatement(update);
      ps.setString(1, symbol);
      ps.setString(2, name);
      ps.setString(3, description);
      ps.setString(4, category);
      ps.executeUpdate();
    } catch (Exception ex) { ex.printStackTrace(); }

    System.out.println("The mutual fund has been added successfully!");
  } // end addFund(String, String)

  // updates the time and date requested from the administrator
  public void updateTime() {
    try {
      query = "select to_char(sysdate) as today from dual";
      ps = connection.prepareStatement(query);
      res = ps.executeQuery();
      res.next();
      // Print current date
      System.out.println("Today's date is: \n\t\t" + res.getString("today"));
      System.out.println("Updating time and date...");

      update = "delete from MUTUALDATE";
      ps = connection.prepareStatement(update);
      ps.executeQuery();
      update = "insert into MUTUALDATE values (to_date();(sysdate))";
      ps = connection.prepareStatement(update);
      ps.executeQuery();
    } catch (Exception e) { e.printStackTrace(); }
    // Update confirmation
    System.out.println("The time and date have been updated successfully!");
  } // end updateTime(String, String)

  // prints current stats specifed by the admin
  public void printStats(int monthNum, int topK) {
    int count = 0;
    String monthNeeded = "";
    // gets date
    String[] months = {"JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP",
                       "OCT", "NOV", "DEC"};
    int april = 3;
    if ((april - monthNum) < 0) {
      int monthInd = (april - monthNum) * -1;
      monthNeeded = months[months.length - ((april - monthNum) * 1)];
    } else {
      monthNeeded = months[april - monthNum];
    }
    String date = "01" + "-" + monthNeeded + "-" + "2017";

    try {
      // Categories
      query = "select symbol, sum(amount) from TRXLOG where action <> 'deposit' and action <> 'sell' and trunc(t_date) between to_date(?) and to_date(sysdate) group by login order by sum(amount) desc";
      ps = connection.prepareStatement(query);
      ps.setString(1, date);
      res = ps.executeQuery();
      // Display
      System.out.println("Here are the categories.. ");
      System.out.println("\tSymbol: \t\tSum: ");
      while (topK >= count) {
        res.next();
        System.out.println("\t" + res.getString("symbol") + " \t\t" + res.getString("sum(amount)"));
        count++;
      } // end while

      // TopK Users
      query = "select login, sum(amount) from TRXLOG where action <> 'deposit'"
              + "and action <> 'sell' and trunc(t_date) between to_date(?)"
              + "and to_date(sysdate) group by login order by sum(amount) desc";
      ps = connection.prepareStatement(query);
      ps.setString(1, date);
      res = ps.executeQuery();
      // Display
      System.out.println("\nHere are top investors by login.. ");
      System.out.println("\tLogin: \t\tSum: ");
      count = 0;
      while (topK >= count) {
        res.next();
        System.out.println("\t" + res.getString("login") + "\t\t" + res.getString("sum(amount)"));
        count++;
      } // end while
    } catch (Exception e) { e.printStackTrace(); }
  } // end printStats(int, int)

}

/*
 * LibraryModel.java
 * Author:
 * Created on:
 */



import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.*;

public class LibraryModel {

	// For use in creating dialogs and making them modal
	private JFrame dialogParent;
	private Connection con = null;

	public LibraryModel(JFrame parent, String userid, String password) {
		this.dialogParent = parent;
		String url = "jdbc:postgresql://db.ecs.vuw.ac.nz/" + userid + "_jdbc";

		try {
			this.con = DriverManager.getConnection(url,userid,password);
		} catch (SQLException e) {
			e.printStackTrace();
			System.err.println("Error:" + e.getMessage());
			System.exit(0);
		}

		System.out.println("Connection to database successful");

	}

	public String bookLookup(int isbn) {
		Statement s1 = null;
		Statement s2 = null;
		ResultSet bookSet = null;
		ResultSet authorSet = null;
		boolean validIBSN = false;
		boolean loopedOnce = false;
		boolean moreThanOneAuthor = false;
		StringBuilder authors = new StringBuilder();
		StringBuilder output = new StringBuilder("Book Lookup:\n");

		try {
			s1 = con.createStatement();
			bookSet = s1.executeQuery("SELECT isbn, title, edition_no, numofcop, numleft "
					+ "FROM book "
					+ "WHERE isbn = " + isbn + ";");

			s2 = con.createStatement();
			authorSet = s2.executeQuery("SELECT surname "
					+ "FROM book NATURAL JOIN book_author NATURAL JOIN author "
					+ "WHERE isbn = " + isbn + ";");

			while(bookSet.next()) {
				output.append("\t" + bookSet.getInt(1) // ISBN
				+ ": " + bookSet.getString(2).trim() + "\n"); // Book title

				output.append("\tEdition: " + bookSet.getInt(3)
				+ " - Number of copies: " + bookSet.getInt(4)
				+ " - Copies left: " + bookSet.getInt(5) + "\n");
				validIBSN = true;
			}

			while(authorSet.next()) {
				if(loopedOnce) {
					moreThanOneAuthor = true;
					authors.append(", ");
				}
				authors.append(authorSet.getString(1).trim());
				loopedOnce = true;

			}

			if(!validIBSN) {
				output.append("\tNo such ISBN: " + isbn);
			} else {
				if(!loopedOnce) {
					output.append("\t(no authors)");

				} else {
					output.append("\tAuthor");

					if(moreThanOneAuthor) {
						output.append("s");
					}

					output.append(": " + authors.toString());

				}
			}


		} catch (SQLException e) {
			e.printStackTrace();

		}

		return output.toString();
	}

	public String showCatalogue() {
		Statement s1 = null;
		Statement s2 = null;
		ResultSet bookSet = null;
		ResultSet authorSet = null;
		boolean loopedOnce = false;
		boolean moreThanOneAuthor = false;
		StringBuilder authors = new StringBuilder();
		StringBuilder output = new StringBuilder("Show Catalogue:\n");

		try {
			s1 = con.createStatement();
			bookSet = s1.executeQuery("SELECT isbn, title, edition_no, numofcop, numleft "
					+ "FROM book "
					+ "ORDER BY isbn;");

			while(bookSet.next()) {
				output.append("\n\t" + bookSet.getInt(1) // ISBN
				+ ": " + bookSet.getString(2) + "\n"); // Title

				output.append("\t\tEdition: " + bookSet.getInt(3)
				+ " - Number of copies: " + bookSet.getInt(4)
				+ " - Copies left: " + bookSet.getInt(5) + "\n");

				s2 = con.createStatement();
				authorSet = s2.executeQuery("SELECT isbn, surname "
						+ "FROM book NATURAL JOIN book_author NATURAL JOIN author "
						+ "WHERE isbn = " + bookSet.getInt(1) + ";");

				while(authorSet.next()) {
					if(loopedOnce) {
						authors.append(", ");
						moreThanOneAuthor = true;
					}
					authors.append(authorSet.getString(2).trim());
					loopedOnce = true;

				}

				if(loopedOnce) {
					output.append("\t\tAuthor");

					if(moreThanOneAuthor) {
						output.append("s");
					}

					output.append(": " + authors.toString());
				} else {
					output.append("\t\t(no authors)");
				}

				output.append("\n");

				loopedOnce = false;
				moreThanOneAuthor = false;

				authors = new StringBuilder();
			}

		} catch (SQLException e) {
			e.printStackTrace();

		}
		return output.toString();
	}

	public String showLoanedBooks() {
		Statement s1 = null;
		Statement s2 = null;
		Statement s3 = null;
		ResultSet loanedSet = null;
		ResultSet authorSet = null;
		ResultSet borrowerSet = null;
		boolean loansExist = false;
		boolean loopedOnce = false;
		boolean moreThanOneAuthor = false;
		StringBuilder output = new StringBuilder("Show Loaned Books:\n");
		StringBuilder authors = new StringBuilder();
		StringBuilder borrowers = new StringBuilder();

		try {
			s1 = con.createStatement();
			loanedSet = s1.executeQuery("SELECT isbn, title, edition_no, numofcop, numleft "
					+ "FROM book NATURAL JOIN cust_book "
					+ "ORDER BY isbn;");

			while(loanedSet.next()) {

				output.append("\n\t" + loanedSet.getInt(1) // ISBN
				+ ": " + loanedSet.getString(2) + "\n"); // Title

				output.append("\t\tEdition: " + loanedSet.getInt(3)
				+ " - Number of copies: " + loanedSet.getInt(4)
				+ " - Copies left: " + loanedSet.getInt(5) + "\n");

				s2 = con.createStatement();
				authorSet = s2.executeQuery("SELECT isbn, surname "
						+ "FROM book NATURAL JOIN book_author NATURAL JOIN author "
						+ "WHERE isbn = " + loanedSet.getInt(1) + ";");

				while(authorSet.next()) {
					if(loopedOnce) {
						authors.append(", ");
						moreThanOneAuthor = true;
					}
					authors.append(authorSet.getString(2).trim());
					loopedOnce = true;

				}

				if(loopedOnce) {
					output.append("\t\tAuthor");

					if(moreThanOneAuthor) {
						output.append("s");
					}

					output.append(": " + authors.toString());
				} else {
					output.append("\t\t(no authors)");
				}

				output.append("\n");

				loopedOnce = false;
				moreThanOneAuthor = false;
				authors = new StringBuilder();

				s3 = con.createStatement();
				borrowerSet = s3.executeQuery("SELECT customerid, l_name, f_name, city, isbn "
						+ "FROM customer NATURAL JOIN cust_book "
						+ "WHERE isbn = " + loanedSet.getInt(1) + ";");

				output.append("\tBorrowers:\n");
				while(borrowerSet.next()) {
					borrowers.append("\t\t" + borrowerSet.getInt(1) + ": " + borrowerSet.getString(2).trim() + ", " + borrowerSet.getString(3).trim() + " - ");

					String city  = "(no city)";
					if(borrowerSet.getString(4) != null) {
						city = borrowerSet.getString(4).trim();
					}

					borrowers.append(city + "\n");

				}
				output.append(borrowers.toString());

				borrowers = new StringBuilder();

				loansExist = true;
			}

			if(!loansExist) {
				output.append("\t(No Loaned Books)");
			}


		} catch (SQLException e) {
			e.printStackTrace();

		}

		return output.toString();
	}

	public String showAuthor(int authorID) {
		Statement s = null;
		ResultSet rs = null;
		boolean loopedOnce = false;
		boolean moreThanOneBook = false;
		StringBuilder books = new StringBuilder();
		StringBuilder output = new StringBuilder("Show Author:\n");

		try {
			s = con.createStatement();
			rs = s.executeQuery("SELECT authorid, name, surname, isbn, title "
					+ "FROM book NATURAL JOIN book_author NATURAL JOIN author "
					+ "WHERE authorid = " + authorID + ";");

			while(rs.next()) {
				if(!loopedOnce) {
					output.append("\t" + rs.getInt(1)
					+ " - " + rs.getString(2).trim() + " " + rs.getString(3).trim() + "\n");

					loopedOnce = true;
				} else {
					moreThanOneBook = true;
					books.append("\n");

				}

				books.append("\t\t" + rs.getInt(4) + " - " + rs.getString(5).trim());

			}

			if(!loopedOnce) {
				output.append("\tNo such author ID: " + authorID);
			} else {
				output.append("\tBook");

				if(moreThanOneBook) {
					output.append("s");
				}

				output.append(" written:\n");

				output.append(books.toString());
			}

			s.close();


		} catch (SQLException e) {
			e.printStackTrace();

		}

		return output.toString();
	}

	public String showAllAuthors() {
		Statement s = null;
		ResultSet rs = null;
		StringBuilder output = new StringBuilder("Show All Authors:\n");

		try {
			s = con.createStatement();
			rs = s.executeQuery("SELECT authorid, surname, name "
					+ "FROM author "
					+ "ORDER BY authorid;");

			while(rs.next()) {
				output.append("\t" + rs.getInt(1) + ": " + rs.getString(2).trim() + ", " + rs.getString(3).trim() + "\n");
			}


		} catch (SQLException e) {
			e.printStackTrace();

		}

		return output.toString();
	}

	public String showCustomer(int customerID) {
		Statement s1 = null;
		Statement s2 = null;
		ResultSet customerSet = null;
		ResultSet borrowSet = null;
		boolean validCustomer = false;
		boolean booksBorrowed = false;
		StringBuilder borrows = new StringBuilder();
		StringBuilder output = new StringBuilder("Show Customer:\n");

		try {
			s1 = con.createStatement();
			customerSet = s1.executeQuery("SELECT customerid, l_name, f_name, city "
					+ "FROM customer "
					+ "WHERE customerid = " + customerID + ";");

			while(customerSet.next()) {
				output.append("\t" + customerSet.getInt(1) + ": "  // ID
						+ customerSet.getString(2).trim() + ", " + customerSet.getString(3).trim() + " - "); // Lastname, Firstname

				String city  = "(no city)";
				if(customerSet.getString(4) != null) {
					city = customerSet.getString(4).trim();
				}

				output.append(city + "\n");

				validCustomer = true;

				s2 = con.createStatement();
				borrowSet = s2.executeQuery("SELECT isbn, title "
						+ "FROM customer NATURAL JOIN cust_book NATURAL JOIN book "
						+ "WHERE customerid = " + customerID + ";");

				while(borrowSet.next()) {
					borrows.append("\t\t" + borrowSet.getInt(1) + " - " + borrowSet.getString(2).trim() + "\n");

					booksBorrowed = true;
				}

			}

			if(!validCustomer) {
				output.append("\tNo such customer ID: " + customerID);
			} else {
				if(booksBorrowed) {
					output.append("\tBooks Borrowed:\n");
					output.append(borrows.toString());

				} else {
					output.append("\t(No books borrowed)");

				}

			}


		} catch (SQLException e) {
			e.printStackTrace();

		}

		return output.toString();
	}

	public String showAllCustomers() {
		Statement s1 = null;
		ResultSet customerSet = null;
		StringBuilder output = new StringBuilder("Show All Customers:\n");

		try {
			s1 = con.createStatement();
			customerSet = s1.executeQuery("SELECT customerid, l_name, f_name, city "
					+ "FROM customer "
					+ "ORDER BY customerid;");

			while(customerSet.next()) {
				output.append("\t" + customerSet.getInt(1) + ": "  // ID
						+ customerSet.getString(2).trim() + ", " + customerSet.getString(3).trim() + " - "); // Lastname, Firstname

				String city  = "(no city)";
				if(customerSet.getString(4) != null) {
					city = customerSet.getString(4).trim();
				}

				output.append(city);

				output.append("\n");

			}


		} catch (SQLException e) {
			e.printStackTrace();

		}

		return output.toString();
	}

	public String borrowBook(int isbn, int customerID,
			int day, int month, int year) {
		Statement s1 = null;
		Statement s2 = null;
		PreparedStatement s3 = null;
		PreparedStatement s4 = null;
		ResultSet checkBook = null;
		ResultSet checkCustomer = null;
		boolean validISBN = false;
		boolean validCustomer = false;
		StringBuilder output = new StringBuilder("Borrow Book:\n");

		try {
			s1 = con.createStatement();
			checkBook = s1.executeQuery("SELECT isbn, numleft, title "
					+ "FROM book "
					+ "WHERE isbn = " + isbn + ";");

			String bookName = "";
			int booksLeft = 0;
			while(checkBook.next()) {
				booksLeft = checkBook.getInt(2);
				if(booksLeft < 1) {
					output.append("\tNot enough copies of book " + isbn + " left");

					return output.toString();
				}

				bookName = checkBook.getString(3).trim();
				validISBN = true;
			}

			if(!validISBN) {
				output.append("\tNo such ISBN: " + isbn);

				return output.toString();
			}

			s2 = con.createStatement();
			checkCustomer = s2.executeQuery("SELECT customerid, f_name, l_name "
					+ "FROM customer "
					+ "WHERE customerid = " + customerID + ";");

			String customerName = "";
			while(checkCustomer.next()) {
				customerName = checkCustomer.getString(2).trim() + " " + checkCustomer.getString(3).trim();
				validCustomer = true;
			}

			if(!validCustomer) {
				output.append("\tNo such customer ID: " + customerID);

				return output.toString();
			}

			output.append("\tBook: " + isbn + " (" + bookName + ")\n");
			output.append("\tLoaned to: " + customerID + " (" + customerName + ")\n");
			output.append("\tDue Date: " + day + " ");

			output.append(returnMonth(month));
			output.append(year);

			String theDate = year + "-" + month + "-" + day;

			s3 = con.prepareStatement("INSERT INTO cust_book "
					+ "VALUES (" + isbn + ", date'" + theDate + "', " + customerID + ");");

			s4 = con.prepareStatement("UPDATE book "
					+ "SET numleft = " + (booksLeft - 1)
					+ " WHERE isbn = " + isbn + ";");

			JOptionPane.showMessageDialog(this.dialogParent, "<html>Confirm " + customerName + " (" + customerID + ")<br>"
					+ "borrowing book: " + bookName + " (" + isbn + ")", "Borrow", JOptionPane.DEFAULT_OPTION);

			s3.executeUpdate();
			s4.executeUpdate();


		} catch (SQLException e) {
			e.printStackTrace();

		}

		return output.toString();
	}

	private String returnMonth(int month) {
		String monthWord = "";

		switch(month) {
		case 0:
			monthWord = "January ";
			break;
		case 1:
			monthWord = "Febuary ";
			break;
		case 2:
			monthWord = "March ";
			break;
		case 3:
			monthWord = "April ";
			break;
		case 4:
			monthWord = "May ";
			break;
		case 5:
			monthWord = "June ";
			break;
		case 6:
			monthWord = "July ";
			break;
		case 7:
			monthWord = "August ";
			break;
		case 8:
			monthWord = "September ";
			break;
		case 9:
			monthWord = "October ";
			break;
		case 10:
			monthWord = "November ";
			break;
		case 11:
			monthWord = "December ";
			break;
		default:
			monthWord = "Invalid Month ";
			break;
		}
		return monthWord;
	}

	public String returnBook(int isbn, int customerID) {
		Statement s1 = null;
		Statement s2 = null;
		Statement s3 = null;
		PreparedStatement s4 = null;
		PreparedStatement s5 = null;
		ResultSet checkBook = null;
		ResultSet checkCustomer = null;
		ResultSet checkLoan = null;
		boolean validISBN = false;
		boolean validCustomer = false;
		boolean validLoan = false;
		StringBuilder output = new StringBuilder("Return Book:\n");

		try {
			s1 = con.createStatement();
			checkBook = s1.executeQuery("SELECT isbn, numleft, title "
					+ "FROM book "
					+ "WHERE isbn = " + isbn + ";");

			String bookName = "";
			int booksLeft = 0;
			while(checkBook.next()) {
				booksLeft = checkBook.getInt(2);
				bookName = checkBook.getString(3);
				validISBN = true;
			}

			if(!validISBN) {
				output.append("\tNo such ISBN: " + isbn);

				return output.toString();
			}

			s2 = con.createStatement();
			checkCustomer = s2.executeQuery("SELECT customerid, f_name, l_name "
					+ "FROM customer "
					+ "WHERE customerid = " + customerID + ";");

			String customerName = "";
			while(checkCustomer.next()) {
				customerName = checkCustomer.getString(2).trim() + " " + checkCustomer.getString(3).trim();
				validCustomer = true;
			}

			if(!validCustomer) {
				output.append("\tNo such customer ID: " + customerID);

				return output.toString();
			}

			s3 = con.createStatement();
			checkLoan = s3.executeQuery("SELECT isbn, customerid "
					+ "FROM cust_book "
					+ "WHERE isbn = " + isbn + " AND customerid = " + customerID + ";");

			while(checkLoan.next()) {
				validLoan = true;
			}

			if(!validLoan) {
				output.append("\tBook " + isbn + " is not loaned to customer " + customerID);

				return output.toString();
			}

			s4 = con.prepareStatement("DELETE FROM cust_book "
					+ "WHERE isbn = " + isbn + " AND customerid = " + customerID + ";");

			s5 = con.prepareStatement("UPDATE book "
					+ "SET numleft = " + (booksLeft + 1)
					+ " WHERE isbn = " + isbn + ";");

			JOptionPane.showMessageDialog(this.dialogParent, "<html>Confirm " + customerName + " (" + customerID + ")<br>"
					+ "returning book: " + bookName + " (" + isbn + ")", "Return", JOptionPane.DEFAULT_OPTION);

			s4.executeUpdate();
			s5.executeUpdate();

			output.append("\tBook " + isbn + " returned for customer " + customerID);

		} catch (SQLException e) {
			e.printStackTrace();

		}

		return output.toString();
	}

	public void closeDBConnection() {
		try {
			this.con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public String deleteCus(int customerID) {
		return "Delete Customer";
	}

	public String deleteAuthor(int authorID) {
		return "Delete Author";
	}

	public String deleteBook(int isbn) {
		return "Delete Book";
	}
}
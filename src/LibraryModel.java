/*
 * LibraryModel.java
 * Author:
 * Created on:
 */



import java.sql.Connection;
import java.sql.DriverManager;
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
		Statement s = null;
		ResultSet rs = null;
		boolean loopedOnce = false;
		boolean moreThanOneAuthor = false;
		StringBuilder authors = new StringBuilder();
		StringBuilder output = new StringBuilder("Book Lookup:\n");

		try {
			s = con.createStatement();
			rs = s.executeQuery("SELECT isbn, title, edition_no, numofcop, numleft, surname "
					+ "FROM book NATURAL JOIN book_author NATURAL JOIN author "
					+ "WHERE isbn = " + isbn + ";");

			while(rs.next()) {
				if(!loopedOnce) {
					output.append("\t" + rs.getInt(1) // ISBN
					+ ": " + rs.getString(2) + "\n"); // Book title

					output.append("\tEdition: " + rs.getInt(3)
					+ " - Number of copies: " + rs.getInt(4)
					+ " - Copies left: " + rs.getInt(5) + "\n");

					authors.append(rs.getString(6).trim());

					loopedOnce = true;
				} else {
					authors.append(", " + rs.getString(6).trim());
					moreThanOneAuthor = true;

				}

			}

			if(!loopedOnce) {
				output.append("\tNo such ISBN: " + isbn);
			} else {
				output.append("\tAuthor");

				if(moreThanOneAuthor) {
					output.append("s");
				}

				output.append(": " + authors.toString());
			}


		} catch (SQLException e) {
			e.printStackTrace();

		}

		return output.toString();
	}

	public String showCatalogue() {
		Statement s = null;
		ResultSet rs = null;
		boolean loopedOnce = false;
		boolean moreThanOneAuthor = false;
		boolean oneBook = false;
		StringBuilder authors = new StringBuilder();
		StringBuilder output = new StringBuilder("Show Catalogue:\n");
		int recordedISBN = -1;

		try {
			s = con.createStatement();
			rs = s.executeQuery("SELECT isbn, title, edition_no, numofcop, numleft, surname "
					+ "FROM book NATURAL JOIN book_author NATURAL JOIN author "
					+ "ORDER BY isbn;");

			while(rs.next()) {

				if(recordedISBN == rs.getInt(1)) {
					authors.append(", " + rs.getString(6).trim());
					moreThanOneAuthor = true;
					oneBook = false;

				} else {
					oneBook = true;
				}

				if(oneBook && loopedOnce) {
					output.append("\t\tAuthor");
					if(moreThanOneAuthor) {
						moreThanOneAuthor = false;
						output.append("s");
					}

					output.append(": " + authors.toString() + "\n");
					authors = new StringBuilder();
					loopedOnce = false;
				}

				if(!loopedOnce){
					output.append("\n\t" + rs.getInt(1)
					+ ": " + rs.getString(2) + "\n");

					output.append("\t\tEdition: " + rs.getInt(3)
					+ " - Number of copies: " + rs.getInt(4)
					+ " - Copies left: " + rs.getInt(5) + "\n");

					authors.append(rs.getString(6).trim());
					loopedOnce = true;
				}
				recordedISBN = rs.getInt(1);

			}

			if(moreThanOneAuthor) {
				output.append("\t\tAuthors: ");
			} else {
				output.append("\t\tAuthor: ");
			}

			output.append(authors.toString());



		} catch (SQLException e) {
			e.printStackTrace();

		}
		return output.toString();
	}

	public String showLoanedBooks() {
		return "Show Loaned Books Stub";
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
		Statement s = null;
		ResultSet rs = null;
		ResultSet borrowQuery = null;
		boolean validCustomer = false;
		boolean booksBorrowed = false;
		StringBuilder borrows = new StringBuilder();
		StringBuilder output = new StringBuilder("Show Customer:\n");

		try {
			s = con.createStatement();
			rs = s.executeQuery("SELECT customerid, l_name, f_name, city"
					+ "FROM customer"
					+ "WHERE customerid = " + customerID + ";");

			borrowQuery = s.executeQuery("SELECT isbn, title "
					+ "FROM customer NATURAL JOIN cust_book NATURAL JOIN book "
					+ "WHERE customerid = " + customerID + ";");

			while(rs.next()) {
				output.append("\t" + rs.getInt(1) + ": "  // ID
						+ rs.getString(2).trim() + ", " + rs.getString(3).trim()  // Lastname, Firstname
						+ " - " + rs.getString(4).trim() + "\n"); // City

				validCustomer = true;

			}

			while(borrowQuery.next()) {
				borrows.append("\t" + borrowQuery.getInt(1) + " - " + borrowQuery.getString(2).trim() + "\n");

				booksBorrowed = true;
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
		return "Show All Customers Stub";
	}

	public String borrowBook(int isbn, int customerID,
			int day, int month, int year) {
		return "Borrow Book Stub";
	}

	public String returnBook(int isbn, int customerid) {
		return "Return Book Stub";
	}

	public void closeDBConnection() {
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
package Code;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class trial extends JFrame implements ActionListener {

    private static final long serialVersionUID = 1L;
    private JLabel[] labels;
    private JTextField[] textFields;
    private JButton[] buttons;
    private JPanel panel;
    private ArrayList<String[]> books = new ArrayList<>();
    private Connection con;
    
    public trial() {
        setTitle("Library Management System");
        setSize(600, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        // Ensure MySQL JDBC driver is loaded
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "MySQL JDBC Driver not found", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        // Initialize database connection
        try {
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/jd", "root", "root");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to connect to database", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        labels = new JLabel[]{
                new JLabel("Book ID"),
                new JLabel("Book Title"),
                new JLabel("Author"),
                new JLabel("Publisher"),
                new JLabel("Year of Publication"),
                new JLabel("ISBN"),
                new JLabel("Number of Copies")
        };

        textFields = new JTextField[labels.length];
        for (int i = 0; i < labels.length; i++) {
            textFields[i] = new JTextField(20);
        }

        buttons = new JButton[]{
                new JButton("Add"),
                new JButton("View"),
                new JButton("Edit"),
                new JButton("Delete"),
                new JButton("Clear"),
                new JButton("Exit")
        };

        for (JButton button : buttons) {
            button.addActionListener(this);
        }

        panel = new JPanel(new GridLayout(labels.length + 1, 2, 5, 5));
        for (int i = 0; i < labels.length; i++) {
            panel.add(labels[i]);
            panel.add(textFields[i]);
        }

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        for (JButton button : buttons) {
            buttonPanel.add(button);
        }

        add(panel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == buttons[0]) { // Add Button
            addBook();
        } else if (e.getSource() == buttons[1]) { // View Button
            viewBooks();
        } else if (e.getSource() == buttons[2]) { // Edit Button
            editBook();
        } else if (e.getSource() == buttons[3]) { // Delete Button
            deleteBook();
        } else if (e.getSource() == buttons[4]) { // Clear Button
            clearFields();
        } else if (e.getSource() == buttons[5]) { // Exit Button
            System.exit(0);
        }
    }

    private void addBook() {
        // Validate book_id as an integer
        try {
            int bookId = Integer.parseInt(textFields[0].getText());

            // ... Further processing with book data

            String[] book = new String[textFields.length];
            for (int i = 0; i < textFields.length; i++) {
                book[i] = textFields[i].getText();
            }
            books.add(book);
            insertIntoDatabase(bookId, book);

            // ... The rest of the method
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid integer value for Book ID", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
    }
    private void insertIntoDatabase(int bookId, String[] book) {
        String query = "INSERT INTO books (book_id, title, author, publisher, year, isbn, copies) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = con.prepareStatement(query)) {
            statement.setInt(1, bookId);
            for (int i = 0; i < book.length; i++) {
                statement.setString(i + 1, book[i]);
            }
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to add book to database", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    private void viewBooks() {
        try {
            String query = "SELECT * FROM books";
            PreparedStatement statement = con.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            if (!resultSet.isBeforeFirst()) {
                JOptionPane.showMessageDialog(this, "No books available", "Information", JOptionPane.INFORMATION_MESSAGE);
            } else {
                ArrayList<String[]> data = new ArrayList<>();
                while (resultSet.next()) {
                    String[] rowData = new String[]{
                            resultSet.getString("book_id"),
                            resultSet.getString("title"),
                            resultSet.getString("author"),
                            resultSet.getString("publisher"),
                            resultSet.getString("year"),
                            resultSet.getString("isbn"),
                            resultSet.getString("copies")
                    };
                    data.add(rowData);
                }

                String[] columns = {"Book ID", "Book Title", "Author", "Publisher", "Year of Publication", "ISBN", "Number of Copies"};
                Object[][] tableData = new Object[data.size()][textFields.length];
                for (int i = 0; i < data.size(); i++) {
                    tableData[i] = data.get(i);
                }

                JTable table = new JTable(tableData, columns);
                JScrollPane scrollPane = new JScrollPane(table);
                JFrame frame = new JFrame("View Books");
                frame.add(scrollPane);
                frame.setSize(1000, 500);
                frame.setVisible(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to fetch books from database", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editBook() {
        String bookID = JOptionPane.showInputDialog(this, "Enter book ID to edit:");
        try {
            String query = "SELECT * FROM books WHERE book_id=?";
            PreparedStatement statement = con.prepareStatement(query);
            statement.setString(1, bookID);
            ResultSet resultSet = statement.executeQuery();

            if (!resultSet.isBeforeFirst()) {
                JOptionPane.showMessageDialog(this, "Book not found", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                // Found the book, allow editing
                String[] bookData = new String[textFields.length];
                while (resultSet.next()) {
                    for (int i = 0; i < textFields.length; i++) {
                        bookData[i] = resultSet.getString(i + 1);
                    }
                }

                // Populate text fields with existing book data
                for (int i = 0; i < textFields.length; i++) {
                    textFields[i].setText(bookData[i]);
                }

                // Disable add button
                buttons[0].setEnabled(false);
                // Enable save button
                buttons[2].setEnabled(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to fetch book from database", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteBook() {
        String bookID = JOptionPane.showInputDialog(this, "Enter book ID to delete:");
        try {
            String query = "DELETE FROM books WHERE book_id=?";
            PreparedStatement statement = con.prepareStatement(query);
            statement.setString(1, bookID);
            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Book deleted successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Book not found", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to delete book", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void clearFields() {
        for (JTextField textField : textFields) {
            textField.setText("");
        }
    }

    public static void main(String[] args) throws ClassNotFoundException {
        // Ensure MySQL JDBC driver is loaded
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "MySQL JDBC Driver not found", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        new trial();
    }
}
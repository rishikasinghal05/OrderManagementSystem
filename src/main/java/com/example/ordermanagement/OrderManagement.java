
package com.example.ordermanagement;

import java.sql.*;
import java.util.Scanner;

public class OrderManagement {

    static final String URL = "jdbc:mysql://localhost:3306/ecommerce_db";
    static final String USER = "root";
    static final String PASSWORD = "Rishika@2057";

    static Connection con;
    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection(URL, USER, PASSWORD);

            System.out.println("MySQL Connected Successfully!");

            int choice;

            do {
                System.out.println("\n===== ORDER MANAGEMENT SYSTEM =====");
                System.out.println("1. Insert Three Orders");
                System.out.println("2. Search Orders By Status");
                System.out.println("3. Increase Order Quantity");
                System.out.println("4. Update Order Status");
                System.out.println("5. Calculate Total and Delete Cancelled Order");
                System.out.println("6. Exit");

                System.out.print("Enter your choice: ");
                choice = sc.nextInt();
                sc.nextLine();

                switch (choice) {
                    case 1:
                        insertOrders();
                        break;
                    case 2:
                        searchOrders();
                        break;
                    case 3:
                        increaseQuantity();
                        break;
                    case 4:
                        updateStatus();
                        break;
                    case 5:
                        calculateAndDelete();
                        break;
                    case 6:
                        System.out.println("Thank you!");
                        break;
                    default:
                        System.out.println("Invalid choice!");
                }

            } while (choice != 6);

            con.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // A. INSERT THREE ORDERS
    static void insertOrders() throws SQLException {

        String sql = "INSERT INTO Orders VALUES (?, ?, ?, ?, ?, ?)";

        PreparedStatement ps = con.prepareStatement(sql);

        Object[][] orders = {
            {101, "Rahul", "Laptop", 2, 55000.0, "Pending"},
            {102, "Priya", "Keyboard", 5, 1200.0, "Shipped"},
            {103, "Aman", "Mouse", 4, 500.0, "Cancelled"}
        };

        for (Object[] o : orders) {
            try {
                ps.setInt(1, (int) o[0]);
                ps.setString(2, (String) o[1]);
                ps.setString(3, (String) o[2]);
                ps.setInt(4, (int) o[3]);
                ps.setDouble(5, (double) o[4]);
                ps.setString(6, (String) o[5]);

                ps.executeUpdate();
                System.out.println("Order " + o[0] + " inserted!");

            } catch (SQLIntegrityConstraintViolationException e) {
                System.out.println("Duplicate Order ID: " + o[0]);
            }
        }

        ps.close();
    }

    // B. SEARCH ORDERS BY STATUS
    static void searchOrders() throws SQLException {

        System.out.print("Enter order status: ");
        String status = sc.nextLine();

        String sql = "SELECT * FROM Orders WHERE status = ? " +
                     "AND quantity > 2 ORDER BY unit_price DESC";

        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, status);

        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            displayOrder(rs);
        }

        rs.close();
        ps.close();
    }

    // C. INCREASE QUANTITY
    static void increaseQuantity() throws SQLException {

        System.out.print("Enter Order ID: ");
        int id = sc.nextInt();

        System.out.print("Enter additional quantity: ");
        int qty = sc.nextInt();

        if (qty <= 0) {
            System.out.println("Quantity must be positive!");
            return;
        }

        String sql = "UPDATE Orders SET quantity = quantity + ? " +
                     "WHERE order_id = ?";

        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, qty);
        ps.setInt(2, id);

        int rows = ps.executeUpdate();

        if (rows > 0)
            System.out.println("Quantity updated successfully!");
        else
            System.out.println("Order does not exist!");

        ps.close();
    }

    // D. UPDATE ORDER STATUS
    static void updateStatus() throws SQLException {

        System.out.print("Enter Order ID: ");
        int id = sc.nextInt();
        sc.nextLine();

        String check = "SELECT * FROM Orders WHERE order_id = ?";

        PreparedStatement ps = con.prepareStatement(check);
        ps.setInt(1, id);

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {

            System.out.println("Current Order Details:");
            displayOrder(rs);

            System.out.print("Enter new status: ");
            String status = sc.nextLine();

            if (!status.equalsIgnoreCase("Pending") &&
                !status.equalsIgnoreCase("Shipped") &&
                !status.equalsIgnoreCase("Delivered")) {

                System.out.println("Invalid status!");
            } else {

                String sql = "UPDATE Orders SET status = ? WHERE order_id = ?";

                PreparedStatement update = con.prepareStatement(sql);
                update.setString(1, status);
                update.setInt(2, id);

                update.executeUpdate();

                System.out.println("Status updated successfully!");
                String updatedQuery = "SELECT * FROM Orders WHERE order_id = ?";
                PreparedStatement updatedPs = con.prepareStatement(updatedQuery);
                updatedPs.setInt(1, id);

                ResultSet updatedRs = updatedPs.executeQuery();

                if (updatedRs.next()) {
                    System.out.println("Updated Order Details:");
                    displayOrder(updatedRs);
                }

                updatedRs.close();
                updatedPs.close();

                update.close();
            }

        } else {
            System.out.println("Order does not exist!");
        }

        rs.close();
        ps.close();
    }

    // E. TOTAL ORDER VALUE AND DELETE CANCELLED ORDER
    static void calculateAndDelete() throws SQLException {

        String total = "SELECT SUM(quantity * unit_price) FROM Orders";

        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(total);

        if (rs.next()) {
            System.out.println("Total Order Value: " + rs.getDouble(1));
        }

        rs.close();
        st.close();

        System.out.print("Enter Order ID to delete: ");
        int id = sc.nextInt();

        String sql = "DELETE FROM Orders WHERE order_id = ? " +
                     "AND status = 'Cancelled'";

        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, id);

        int rows = ps.executeUpdate();

        if (rows > 0)
            System.out.println("Cancelled order deleted successfully!");
        else
            System.out.println("Order cannot be deleted. It does not exist or is not Cancelled.");

        ps.close();
    }

    // DISPLAY ORDER DETAILS
    static void displayOrder(ResultSet rs) throws SQLException {

        System.out.println(
            "ID: " + rs.getInt("order_id") +
            " | Customer: " + rs.getString("customer_name") +
            " | Product: " + rs.getString("product_name") +
            " | Quantity: " + rs.getInt("quantity") +
            " | Price: " + rs.getDouble("unit_price") +
            " | Status: " + rs.getString("status")
        );
    }
}


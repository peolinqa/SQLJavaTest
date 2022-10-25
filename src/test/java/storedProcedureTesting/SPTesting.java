package storedProcedureTesting;

import java.sql.*;

import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * Syntax                          Stored procedures
 * { call procedure name() }        Accept no parameters and return no value
 * { call procedure name(?,?) }     Accept two parameters and return no value
 * { ?= call procedure name() }     Accept no parameter and return value
 * { ?= call procedure name(?) }    Accept one parameter and return value
 */

public class SPTesting {
    Connection con = null;
    Statement stmt = null;
    ResultSet rs;
    CallableStatement cStmt;
    ResultSet rs1;
    ResultSet rs2;

    public boolean compareResultSets(ResultSet resultSet1, ResultSet resultSet2) throws SQLException {
        while (resultSet1.next()) {
            resultSet2.next();
            int count = resultSet1.getMetaData().getColumnCount();
            for (int i = 1; i <= count; i++) {
                if (!StringUtils.equals(resultSet1.getString(i), resultSet2.getString(i))) {
                    return false;
                }
            }
        }
        return true;
    }

    @BeforeClass
    void setup() throws SQLException {
        con = DriverManager.getConnection("jdbc:mysql://localhost:3306/classicmodels", "root", "root1234");
    }

    @AfterClass
    void tearDown() throws SQLException {
        con.close();
    }

    @Test(priority = 1)
    void test_storedProceduresExist() throws SQLException {
        stmt = con.createStatement();
        rs = stmt.executeQuery("SHOW PROCEDURE STATUS WHERE Name = 'SelectAllCustomers'");
        rs.next();

        Assert.assertEquals(rs.getString("Name"), "SelectAllCustomers");
    }


    @Test(priority = 2)
    void test_SelectAllCustomers() throws SQLException {
        cStmt = con.prepareCall("{CALL SelectAllCustomers()}");
        rs1 = cStmt.executeQuery();

        Statement stmt = con.createStatement();
        rs2 = stmt.executeQuery("select * from customers");

        Assert.assertEquals(compareResultSets(rs1, rs2), true);
    }

    @Test(priority = 3)
    void test_SelectAllCustomersByCity() throws SQLException {
        cStmt = con.prepareCall("{call SelectAllCustomersByCity(?)}");
        cStmt.setString(1, "Singapore");
        rs1 = cStmt.executeQuery();

        Statement stmt = con.createStatement();
        rs2 = stmt.executeQuery("SELECT * FROM Customers WHERE city = 'Singapore'");

        Assert.assertEquals(compareResultSets(rs1, rs2), true);
    }

    @Test(priority = 4)
    void test_SelectAllCustomersByCityAndPin() throws SQLException {
        cStmt = con.prepareCall("{call SelectAllCustomersByCityAndPin(?, ?)}");
        cStmt.setString(1, "Singapore");
        cStmt.setString(2, "079903");
        rs1 = cStmt.executeQuery();

        Statement stmt = con.createStatement();
        rs2 = stmt.executeQuery("SELECT * FROM Customers WHERE city = 'Singapore' and postalCode = '079903'");

        Assert.assertEquals(compareResultSets(rs1, rs2), true);
    }

    @Test(priority = 5)
    void test_get_order_by_cust() throws SQLException {
        cStmt = con.prepareCall("{call get_order_by_cust(?,?,?,?,?)}");
        cStmt.setInt(1, 141);

        cStmt.registerOutParameter(2, Types.INTEGER);
        cStmt.registerOutParameter(3, Types.INTEGER);
        cStmt.registerOutParameter(4, Types.INTEGER);
        cStmt.registerOutParameter(5, Types.INTEGER);

        cStmt.executeQuery();

        int shipped = cStmt.getInt(2);
        int canceled = cStmt.getInt(3);
        int resolved = cStmt.getInt(4);
        int disputed = cStmt.getInt(5);

        //System.out.println(shipped + " " + canceled + " " + resolved + " " + disputed);

        Statement stmt = con.createStatement();
        rs = stmt.executeQuery("SELECT \n"
                + "(SELECT COUNT(*) AS 'shipped' FROM orders WHERE customerNumber = 141 AND status = 'Shipped') AS Shipped,\n"
                + "\n"
                + "(SELECT COUNT(*) AS 'canceled' FROM orders WHERE customerNumber = 141 AND status = 'Canceled') AS Canceled,\n"
                + "     \n"
                + "(SELECT COUNT(*) AS 'resolved' FROM orders WHERE customerNumber = 141 AND status = 'Resolved') AS Resolved,\n"
                + "     \n"
                + "(SELECT COUNT(*) AS 'disputed' FROM orders WHERE customerNumber = 141 AND status = 'Disputed') AS Disputed;");

        rs.next();

        int exp_shipped = rs.getInt("shipped");
        int exp_canceled = rs.getInt("canceled");
        int exp_resolved = rs.getInt("resolved");
        int exp_disputed = rs.getInt("disputed");

        if (shipped == exp_shipped && canceled == exp_canceled
                && resolved == exp_resolved && disputed == exp_disputed)
            Assert.assertTrue(true);
        else
            Assert.assertTrue(false);
    }

    @Test(priority = 6)
    void test_GetCustomerShipping() throws SQLException {
        cStmt = con.prepareCall("{call GetCustomerShipping(?,?)}");
        cStmt.setInt(1, 112);
        cStmt.registerOutParameter(2, Types.VARCHAR);

        cStmt.executeQuery();

        String shippingTime = cStmt.getString(2);

        Statement stmt = con.createStatement();
        rs = stmt.executeQuery("SELECT country,\n"
                + "CASE\n"
                + "\tWHEN country = 'USA' THEN '2-day Shipping'\n"
                + "\tWHEN country = 'Canada' THEN '3-day Shipping'\n"
                + "\tELSE '5-day Shipping'\n"
                + "END AS ShippingTime\n"
                + "FROM customers WHERE customerNumber = 112;");
        rs.next();
        String exp_shippingTime = rs.getString("ShippingTime");

        Assert.assertEquals(shippingTime, exp_shippingTime);
    }
}

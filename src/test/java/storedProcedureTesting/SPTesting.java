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

    @BeforeClass
    void setup() throws SQLException {
        con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/classicmodels", "root", "root1234");
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
        cStmt.executeQuery();
    }

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
}

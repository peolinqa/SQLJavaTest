package storedProcedureTesting;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SPTesting {
    Connection con = null;

    @BeforeClass
    void setup() throws SQLException {
        con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/classicmodels", "root", "root1234");
    }

    @AfterClass
    void tearDown() throws SQLException {
        con.close();
    }
}

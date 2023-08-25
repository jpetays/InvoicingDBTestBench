package homebeach;

import org.neo4j.driver.Config;
import org.neo4j.driver.Logging;
import org.neo4j.driver.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

public class DataGenerator {

    private static final Logger logger = LoggerFactory.getLogger(DataGenerator.class);

    private HashMap<String, String[]> sql_databases;

    private HashMap<String, String> neo4j_settings;

    private String default_db_url;

    private List<String> firstnames;
    private List<String> surnames;
    private List<HashMap<String, String>> addresses;

    public DataGenerator(HashMap<String, String[]> sql_databases, HashMap<String, String> neo4j_settings, String default_db_url) {
        this.sql_databases = sql_databases;
        this.neo4j_settings = neo4j_settings;
        this.default_db_url = default_db_url;
    }

    public void executeSQLUpdate(String sqlQuery, String db_url, String[] db_settings) {

        String driver = db_settings[0];
        String username = db_settings[1];
        String password = db_settings[2];

        Connection conn = null;
        Statement stmt = null;
        ResultSet resultSet = null;

        try {

            Class.forName(driver);

            conn = DriverManager.getConnection(db_url, username, password);
            stmt = conn.createStatement();

            stmt.executeUpdate(sqlQuery);


        } catch (SQLException e) {
            logger.debug("SQLException");
            logger.error("unhandled exception", e);
        } catch (Exception e) {
            logger.debug("Exception");
            logger.error("unhandled exception", e);
        } finally {

            try {
                if (stmt != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                logger.debug("SQLException");
                logger.error("unhandled exception", e);
            }
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                logger.debug("SQLException");
                logger.error("unhandled exception", e);
            }
        }

    }


    public ResultSet executeSQLQuery(String sqlQuery) {

        Connection conn = null;
        Statement stmt = null;
        ResultSet resultSet = null;

        String[] db_settings = sql_databases.get(default_db_url);

        String jdbc_driver = db_settings[0];

        String username = db_settings[1];
        String password = db_settings[2];

        try {

            Class.forName(jdbc_driver);

            conn = DriverManager.getConnection(default_db_url, username, password);
            stmt = conn.createStatement();

            resultSet = stmt.executeQuery(sqlQuery);

        } catch (SQLException e) {
            logger.error("unhandled exception", e);
        } catch (Exception e) {
            logger.error("unhandled exception", e);
        } finally {

            try {
                if (stmt != null) {
                    conn.close();
                }
            } catch (SQLException se) {
            }
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                logger.error("unhandled exception", e);
            }
        }

        return resultSet;
    }

    public void truncateDatabases() {

        Connection conn = null;
        Statement stmt = null;
        ResultSet resultSet = null;

        String neo4j_db_url = neo4j_settings.get("NEO4J_DB_URL");
        String neo4j_username = neo4j_settings.get("NEO4J_USERNAME");
        String neo4j_password = neo4j_settings.get("NEO4J_PASSWORD");

        org.neo4j.driver.Driver driver = QueryTester.getNeo4jDriver(neo4j_db_url, AuthTokens.basic(neo4j_username, neo4j_password));

        Session session = driver.session();

        session.run("MATCH (n) DETACH DELETE n");

        session.close();
        driver.close();

        for (String db_url : sql_databases.keySet()) {
            String[] db_info = sql_databases.get(db_url);

            String db_driver = db_info[0];
            String db_username = db_info[1];
            String db_password = db_info[2];

            try {

                Class.forName(db_driver);

                conn = DriverManager.getConnection(db_url, db_username, db_password);
                stmt = conn.createStatement();

                stmt.addBatch("SET FOREIGN_KEY_CHECKS=0;");
                stmt.addBatch("TRUNCATE TABLE warehouse.customer;");
                stmt.addBatch("TRUNCATE TABLE warehouse.invoice;");
                stmt.addBatch("TRUNCATE TABLE warehouse.work;");
                stmt.addBatch("TRUNCATE TABLE warehouse.workhours;");
                stmt.addBatch("TRUNCATE TABLE warehouse.workinvoice;");
                stmt.addBatch("TRUNCATE TABLE warehouse.worktarget;");
                stmt.addBatch("TRUNCATE TABLE warehouse.target;");
                stmt.addBatch("TRUNCATE TABLE warehouse.useditem;");

                stmt.addBatch("TRUNCATE TABLE warehouse.worktype;");
                stmt.addBatch("TRUNCATE TABLE warehouse.item;");

                stmt.addBatch("SET FOREIGN_KEY_CHECKS=1;");
                stmt.executeBatch();

            } catch (Exception e) {
                logger.error("unhandled exception", e);
            } finally {

                try {
                    if (stmt != null) {
                        conn.close();
                    }
                } catch (SQLException se) {
                }
                try {
                    if (conn != null) {
                        conn.close();
                    }
                } catch (SQLException e) {
                    logger.error("unhandled exception", e);
                }

            }

        }

    }

    public void truncateDatabasesWork() {

        Connection conn = null;
        Statement stmt = null;
        ResultSet resultSet = null;

        String neo4j_db_url = neo4j_settings.get("NEO4J_DB_URL");
        String neo4j_username = neo4j_settings.get("NEO4J_USERNAME");
        String neo4j_password = neo4j_settings.get("NEO4J_PASSWORD");

        org.neo4j.driver.Driver driver = QueryTester.getNeo4jDriver(neo4j_db_url, AuthTokens.basic(neo4j_username, neo4j_password));

        Session session = driver.session();


        session.run("MATCH (w:work) DETACH DELETE w");

        session.close();
        driver.close();

        for (String db_url : sql_databases.keySet()) {
            String[] db_info = sql_databases.get(db_url);

            String db_driver = db_info[0];
            String db_username = db_info[1];
            String db_password = db_info[2];

            try {

                Class.forName(db_driver);

                conn = DriverManager.getConnection(db_url, db_username, db_password);
                stmt = conn.createStatement();

                stmt.addBatch("SET FOREIGN_KEY_CHECKS=0;");
                stmt.addBatch("TRUNCATE TABLE warehouse.work;");
                stmt.addBatch("TRUNCATE TABLE warehouse.useditem;");
                stmt.addBatch("TRUNCATE TABLE warehouse.workhours;");
                stmt.addBatch("SET FOREIGN_KEY_CHECKS=1;");
                stmt.executeBatch();

            } catch (Exception e) {
                logger.error("unhandled exception", e);
            } finally {

                try {
                    if (stmt != null) {
                        conn.close();
                    }
                } catch (SQLException se) {
                }
                try {
                    if (conn != null) {
                        conn.close();
                    }
                } catch (SQLException e) {
                    logger.error("unhandled exception", e);
                }

            }

        }

    }


    public void truncateDatabasesCustomer() {

        Connection conn = null;
        Statement stmt = null;
        ResultSet resultSet = null;

        String neo4j_db_url = neo4j_settings.get("NEO4J_DB_URL");
        String neo4j_username = neo4j_settings.get("NEO4J_USERNAME");
        String neo4j_password = neo4j_settings.get("NEO4J_PASSWORD");

        org.neo4j.driver.Driver driver = QueryTester.getNeo4jDriver(neo4j_db_url, AuthTokens.basic(neo4j_username, neo4j_password));

        Session session = driver.session();

        session.run("MATCH (c:customer) DETACH DELETE c");
        session.run("MATCH (i:invoice) DETACH DELETE i");
        session.run("MATCH (t:target) DETACH DELETE t");

        session.close();
        driver.close();

        for (String db_url : sql_databases.keySet()) {
            String[] db_info = sql_databases.get(db_url);

            String db_driver = db_info[0];
            String db_username = db_info[1];
            String db_password = db_info[2];

            try {

                Class.forName(db_driver);

                conn = DriverManager.getConnection(db_url, db_username, db_password);
                stmt = conn.createStatement();

                stmt.addBatch("SET FOREIGN_KEY_CHECKS=0;");

                stmt.addBatch("TRUNCATE TABLE warehouse.workinvoice;");
                stmt.addBatch("TRUNCATE TABLE warehouse.worktarget;");
                stmt.addBatch("TRUNCATE TABLE warehouse.target;");
                stmt.addBatch("TRUNCATE TABLE warehouse.invoice;");
                stmt.addBatch("TRUNCATE TABLE warehouse.customer;");

                stmt.addBatch("SET FOREIGN_KEY_CHECKS=1;");
                stmt.executeBatch();

            } catch (Exception e) {
                logger.error("unhandled exception", e);
            } finally {

                try {
                    if (stmt != null) {
                        conn.close();
                    }
                } catch (SQLException se) {
                }
                try {
                    if (conn != null) {
                        conn.close();
                    }
                } catch (SQLException e) {
                    logger.error("unhandled exception", e);
                }

            }

        }

    }


    public void createSampleTables(String db_url) {

        String[] db_settings = sql_databases.get(db_url);

        String database = "testdata";

        String dropDatabase = "DROP DATABASE `" + database + "`";

        String createDatabase = "CREATE DATABASE IF NOT EXISTS `" + database + "`";

        String firstnames = "CREATE TABLE IF NOT EXISTS `firstnames` (" +
                "`id` serial," +
                "`firstname` varchar(100) NOT NULL," +
                "PRIMARY KEY (`id`))";

        String surnames = "CREATE TABLE IF NOT EXISTS `surnames` (" +
                "`id` serial," +
                "`surname` varchar(100) NOT NULL," +
                "PRIMARY KEY (`id`))";

        String addresses = "CREATE TABLE IF NOT EXISTS `addresses` (" +
                "`id` serial," +
                "`street` varchar(200) NOT NULL," +
                "`city` varchar(100) NOT NULL," +
                "`district` varchar(100) NOT NULL," +
                "`region` varchar(50) NOT NULL," +
                "`postcode` varchar(50) NOT NULL," +
                "PRIMARY KEY (`id`))";

        executeSQLUpdate(dropDatabase, "jdbc:mariadb://127.0.0.1/", db_settings);
        executeSQLUpdate(createDatabase, "jdbc:mariadb://127.0.0.1/", db_settings);
        executeSQLUpdate(firstnames, "jdbc:mariadb://127.0.0.1/" + database, db_settings);
        executeSQLUpdate(surnames, "jdbc:mariadb://127.0.0.1/" + database, db_settings);
        executeSQLUpdate(addresses, "jdbc:mariadb://127.0.0.1/" + database, db_settings);

    }


    public void getSampleData() {

        try {

            firstnames = new ArrayList<String>();
            surnames = new ArrayList<String>();
            addresses = new ArrayList<HashMap<String, String>>();

            ResultSet rs = executeSQLQuery("SELECT firstname FROM testdata.firstnames;");
            while (rs.next()) {
                String firstName = rs.getString("firstname");
                firstnames.add(firstName);
            }

            rs = executeSQLQuery("SELECT surname FROM testdata.surnames;");
            while (rs.next()) {
                String surName = rs.getString("surname");
                surnames.add(surName);
            }

            rs = executeSQLQuery("SELECT street, city, district, region, postcode FROM testdata.addresses;");
            while (rs.next()) {

                HashMap<String, String> address = new HashMap<String, String>();

                address.put("street", rs.getString("street"));
                address.put("city", rs.getString("city"));
                address.put("district", rs.getString("district"));
                address.put("region", rs.getString("region"));
                address.put("postcode", rs.getString("postcode"));

                addresses.add(address);
            }


        } catch (Exception e) {
            System.err.println("Exception: "
                    + e.getMessage());
        }

    }


    public void printSampleDataSizes() {

        logger.debug("Firstnames size: " + firstnames.size());
        logger.debug("Surnames size: " + surnames.size());
        logger.debug("Addresses size: " + addresses.size());

    }

    public int getWorkCount() throws SQLException {

        ResultSet rs = executeSQLQuery("SELECT COUNT(*) AS WORKCOUNT FROM WAREHOUSE.WORK");

        int workCount = 0;

        while (rs.next()) {

            workCount = rs.getInt("WORKCOUNT");

        }

        return workCount;
    }

    public int getWorkTypeCount() throws SQLException {

        ResultSet rs = executeSQLQuery("SELECT COUNT(*) AS WORKTYPECOUNT FROM WAREHOUSE.WORKTYPE");

        int workTypeCount = 0;

        while (rs.next()) {

            workTypeCount = rs.getInt("WORKTYPECOUNT");

        }

        return workTypeCount;
    }

    public int getItemCount() throws SQLException {

        ResultSet rs = executeSQLQuery("SELECT COUNT(*) AS ITEMCOUNT FROM WAREHOUSE.ITEM");

        int itemCount = 0;

        while (rs.next()) {

            itemCount = rs.getInt("ITEMCOUNT");

        }

        return itemCount;
    }

    public int getCustomerCount() throws SQLException {

        ResultSet rs = executeSQLQuery("SELECT COUNT(*) AS CUSTOMERCOUNT FROM WAREHOUSE.CUSTOMER");

        int customerCount = 0;

        while (rs.next()) {

            customerCount = rs.getInt("CUSTOMERCOUNT");

        }

        return customerCount;
    }

    public int getCustomerCountCypher() {
        return getNodeCountCypher("customer");
    }
    public int getInvoiceCountCypher() {
        return getNodeCountCypher("invoice");
    }
    public int getItemCountCypher() {
        return getNodeCountCypher("item");
    }
    public int getWorkCountCypher() {
        return getNodeCountCypher("work");
    }
    public int getWorkTypeCountCypher() {
        return getNodeCountCypher("worktype");
    }

    private int getNodeCountCypher(final String nodeName) {

        String neo4j_db_url = neo4j_settings.get("NEO4J_DB_URL");
        String neo4j_username = neo4j_settings.get("NEO4J_USERNAME");
        String neo4j_password = neo4j_settings.get("NEO4J_PASSWORD");

        try (org.neo4j.driver.Driver driver = QueryTester.getNeo4jDriver(neo4j_db_url, AuthTokens.basic(neo4j_username, neo4j_password))) {
            try (Session session = driver.session()) {

                Result result = session.run("MATCH (n:" + nodeName + ") RETURN count(n) as count");
                var record = result.single();
                var value = record.get(0);
                return value.asInt();
            }
        }
    }

    public int getInvoiceCount() throws SQLException {

        ResultSet rs = executeSQLQuery("SELECT COUNT(*) AS INVOICECOUNT FROM WAREHOUSE.INVOICE");

        int invoiceCount = 0;

        while (rs.next()) {

            invoiceCount = rs.getInt("INVOICECOUNT");

        }

        return invoiceCount;
    }

    public int getTargetCount() throws SQLException {

        ResultSet rs = executeSQLQuery("SELECT COUNT(*) AS TARGETCOUNT FROM WAREHOUSE.TARGET");

        int targetCount = 0;

        while (rs.next()) {

            targetCount = rs.getInt("TARGETCOUNT");

        }

        return targetCount;
    }

    public int getLastCustomerId() throws SQLException {

        ResultSet rs = executeSQLQuery("SELECT MAX(ID) AS LASTID FROM WAREHOUSE.CUSTOMER");

        int lastCustomerId = 0;

        while (rs.next()) {

            lastCustomerId = rs.getInt("LASTID");

        }

        return lastCustomerId;
    }

    public int getLastWorkId() throws SQLException {

        ResultSet rs = executeSQLQuery("SELECT MAX(ID) AS LASTID FROM WAREHOUSE.WORK");

        int workId = 0;

        while (rs.next()) {

            workId = rs.getInt("LASTID");

        }

        return workId;
    }

    public int getLastInvoiceId() throws SQLException {

        ResultSet rs = executeSQLQuery("SELECT MAX(ID) AS LASTID FROM WAREHOUSE.INVOICE");

        int invoiceId = 0;

        while (rs.next()) {

            invoiceId = rs.getInt("LASTID");

        }

        return invoiceId;
    }

    public int getLastTargetId() throws SQLException {

        ResultSet rs = executeSQLQuery("SELECT MAX(ID) AS LASTID FROM WAREHOUSE.TARGET");

        int targetId = 0;

        while (rs.next()) {

            targetId = rs.getInt("LASTID");

        }

        return targetId;
    }

    public int getLastItemId() throws SQLException {

        ResultSet rs = executeSQLQuery("SELECT MAX(ID) AS LASTID FROM WAREHOUSE.ITEM");

        int itemId = 0;

        while (rs.next()) {

            itemId = rs.getInt("LASTID");

        }

        return itemId;
    }

    public int getLastWorkTypeId() throws SQLException {

        ResultSet rs = executeSQLQuery("SELECT MAX(ID) AS LASTID FROM WAREHOUSE.WORKTYPE");

        int workTypeId = 0;

        while (rs.next()) {

            workTypeId = rs.getInt("LASTID");

        }

        return workTypeId;
    }

    public void cleanSequentialInvoices(int customerId) {


        String neo4j_db_url = neo4j_settings.get("NEO4J_DB_URL");
        String neo4j_username = neo4j_settings.get("NEO4J_USERNAME");
        String neo4j_password = neo4j_settings.get("NEO4J_PASSWORD");

        org.neo4j.driver.Driver driver = QueryTester.getNeo4jDriver(neo4j_db_url, AuthTokens.basic(neo4j_username, neo4j_password));

        Session session = driver.session();

        String deleteInvoicesCypher = "MATCH (i:invoice) WHERE i.customerId=" + customerId + " DETACH DELETE i";
        String deleteCustomerCypher = "MATCH (c:customer) WHERE c.customerId=" + customerId + " DETACH DELETE c";

        session.run(deleteInvoicesCypher);
        session.run(deleteCustomerCypher);

        session.close();
        driver.close();

        String deleteInvoicesSQL = "DELETE FROM warehouse.invoice WHERE customerId = " + customerId;
        String deleteCustomerSQL = "DELETE FROM warehouse.customer WHERE id = " + customerId;

        for (String db_url : sql_databases.keySet()) {

            String[] db_settings = sql_databases.get(db_url);
            executeSQLUpdate(deleteInvoicesSQL, db_url, db_settings);
            executeSQLUpdate(deleteCustomerSQL, db_url, db_settings);

        }

    }

    public void createIndexes() {

        createIndexesSQL();
        createIndexesCypher();

    }

    public void deleteIndexes() {

        deleteIndexesSQL();
        deleteIndexesCypher();

    }

    public void createIndexesSQL() {


        String createInvoiceIndexIfNotExistsSQL = "CREATE INDEX IF NOT EXISTS invoiceIndex ON warehouse.invoice(previousinvoice)";
        String createInvoiceIndexSQL = "CREATE INDEX invoiceIndex ON warehouse.invoice(previousinvoice)";
        String createWorktypeIndexIfNotExistsSQL = "CREATE INDEX IF NOT EXISTS worktypeIndex ON warehouse.worktype(price)";
        String createWorktypeIndexSQL = "CREATE INDEX worktypeIndex ON warehouse.worktype(price)";
        String createWorkhoursIndexIfNotExistsSQL = "CREATE INDEX IF NOT EXISTS workhoursIndex ON warehouse.workhours(hours,discount)";
        String createWorkhoursIndexSQL = "CREATE INDEX workhoursIndex ON warehouse.workhours(hours,discount)";
        String createUseditemIndexIfNotExistsSQL = "CREATE INDEX IF NOT EXISTS usedItemIndex ON warehouse.useditem(amount, discount)";
        String createUseditemIndexSQL = "CREATE INDEX usedItemIndex ON warehouse.useditem(amount, discount)";
        String createItemIndexIfNotExistsSQL = "CREATE INDEX IF NOT EXISTS ItemIndex ON warehouse.item(purchaseprice)";
        String createItemIndexSQL = "CREATE INDEX ItemIndex ON warehouse.item(purchaseprice)";


        for (String db_url : sql_databases.keySet()) {

            if (db_url.contains("mariadb")) {

                String[] db_settings = sql_databases.get(db_url);
                executeSQLUpdate(createInvoiceIndexIfNotExistsSQL, db_url, db_settings);
                executeSQLUpdate(createWorktypeIndexIfNotExistsSQL, db_url, db_settings);
                executeSQLUpdate(createWorkhoursIndexIfNotExistsSQL, db_url, db_settings);
                executeSQLUpdate(createUseditemIndexIfNotExistsSQL, db_url, db_settings);
                executeSQLUpdate(createItemIndexIfNotExistsSQL, db_url, db_settings);

            } else {

                String[] db_settings = sql_databases.get(db_url);
                executeSQLUpdate(createInvoiceIndexSQL, db_url, db_settings);
                executeSQLUpdate(createWorktypeIndexSQL, db_url, db_settings);
                executeSQLUpdate(createWorkhoursIndexSQL, db_url, db_settings);
                executeSQLUpdate(createUseditemIndexSQL, db_url, db_settings);
                executeSQLUpdate(createItemIndexSQL, db_url, db_settings);

            }

        }

    }

    public void createIndexesCypher() {


        String neo4j_db_url = neo4j_settings.get("NEO4J_DB_URL");
        String neo4j_username = neo4j_settings.get("NEO4J_USERNAME");
        String neo4j_password = neo4j_settings.get("NEO4J_PASSWORD");

        org.neo4j.driver.Driver driver = QueryTester.getNeo4jDriver(neo4j_db_url, AuthTokens.basic(neo4j_username, neo4j_password));

        Session session = driver.session();

        String createInvoiceIndexCypher = "CREATE INDEX invoiceIndex IF NOT EXISTS " +
                "FOR (inv:invoice) " +
                "ON (inv.invoiceId, inv.previousinvoice)";

        String createCustomerIndexCypher = "CREATE INDEX customerIndex IF NOT EXISTS " +
                "FOR (c:customer) " +
                "ON (c.customerId) ";

        String createWorkTypeIndexCypher = "CREATE INDEX worktypeIndex IF NOT EXISTS " +
                "FOR (wt:worktype) " +
                "ON (wt.price)";

        String createWorkhoursIndexCypher = "CREATE INDEX workhoursIndex IF NOT EXISTS " +
                "FOR (h:WORKHOURS) " +
                "ON (h.hours, h.discount)";

        String createUseditemIndexCypher = "CREATE INDEX useditemIndex IF NOT EXISTS " +
                "FOR (u:USED_ITEM) " +
                "ON (u.amount, u.discount)";

        String createItemIndexCypher = "CREATE INDEX itemIndex IF NOT EXISTS " +
                "FOR (i:item) " +
                "ON (i.purchaseprice)";

        session.run(createInvoiceIndexCypher);
        session.run(createCustomerIndexCypher);
        session.run(createWorkTypeIndexCypher);
        session.run(createWorkhoursIndexCypher);
        session.run(createUseditemIndexCypher);
        session.run(createItemIndexCypher);

        session.close();
        driver.close();

    }


    public void deleteIndexesSQL() {

        String dropInvoiceIndexIfExistsSQL = "DROP INDEX IF EXISTS invoiceIndex ON warehouse.invoice;";
        String dropInvoiceIndexSQL = "DROP INDEX invoiceIndex ON warehouse.invoice;";
        String dropWorktypeIndexIfExistsSQL = "DROP INDEX IF EXISTS worktypeIndex ON warehouse.worktype;";
        String dropWorktypeIndexSQL = "DROP INDEX worktypeIndex ON warehouse.worktype;";
        String dropWorkhoursIndexIfExistsSQL = "DROP INDEX IF EXISTS workhoursIndex ON warehouse.workhours;";
        String dropWorkhoursIndexSQL = "DROP INDEX workhoursIndex ON warehouse.workhours;";
        String dropUseditemIndexIfExistsSQL = "DROP INDEX IF EXISTS useditemIndex ON warehouse.useditem;";
        String dropUseditemIndexSQL = "DROP INDEX useditemIndex ON warehouse.useditem;";
        String dropItemIndexIfExistsSQL = "DROP INDEX IF EXISTS itemIndex ON warehouse.item;";
        String dropItemIndexSQL = "DROP INDEX itemIndex ON warehouse.item;";

        for (String db_url : sql_databases.keySet()) {

            if (db_url.contains("mariadb")) {

                String[] db_settings = sql_databases.get(db_url);
                executeSQLUpdate(dropInvoiceIndexIfExistsSQL, db_url, db_settings);
                executeSQLUpdate(dropWorktypeIndexIfExistsSQL, db_url, db_settings);
                executeSQLUpdate(dropWorkhoursIndexIfExistsSQL, db_url, db_settings);
                executeSQLUpdate(dropUseditemIndexIfExistsSQL, db_url, db_settings);
                executeSQLUpdate(dropItemIndexIfExistsSQL, db_url, db_settings);

            } else {

                String[] db_settings = sql_databases.get(db_url);
                executeSQLUpdate(dropInvoiceIndexSQL, db_url, db_settings);
                executeSQLUpdate(dropWorktypeIndexSQL, db_url, db_settings);
                executeSQLUpdate(dropWorkhoursIndexSQL, db_url, db_settings);
                executeSQLUpdate(dropUseditemIndexSQL, db_url, db_settings);
                executeSQLUpdate(dropItemIndexSQL, db_url, db_settings);

            }

        }

    }

    public void deleteIndexesCypher() {


        String neo4j_db_url = neo4j_settings.get("NEO4J_DB_URL");
        String neo4j_username = neo4j_settings.get("NEO4J_USERNAME");
        String neo4j_password = neo4j_settings.get("NEO4J_PASSWORD");

        org.neo4j.driver.Driver driver = QueryTester.getNeo4jDriver(neo4j_db_url, AuthTokens.basic(neo4j_username, neo4j_password));

        Session session = driver.session();

        String dropCustomerIndexCypher = "DROP INDEX customerIndex IF EXISTS";
        String dropInvoiceIndexCypher = "DROP INDEX invoiceIndex IF EXISTS";
        String dropWorkTypeIndexCypher = "DROP INDEX worktypeIndex IF EXISTS";
        String dropWorkhoursIndexCypher = "DROP INDEX workhoursIndex IF EXISTS";
        String dropUseditemIndexCypher = "DROP INDEX useditemIndex IF EXISTS";
        String dropItemIndexCypher = "DROP INDEX itemIndex IF EXISTS";


        session.run(dropInvoiceIndexCypher);
        session.run(dropCustomerIndexCypher);
        session.run(dropWorkTypeIndexCypher);
        session.run(dropWorkhoursIndexCypher);
        session.run(dropUseditemIndexCypher);
        session.run(dropItemIndexCypher);

        session.close();
        driver.close();

    }


    public void loadSampleData(int batchExecuteValue, String db_url) {

        String[] db_settings = sql_databases.get(db_url);

        String jdbc_driver = db_settings[0];

        String username = db_settings[1];
        String password = db_settings[2];

        String firstnamesFile = "./data/firstnames.csv";
        String surnamesFile = "./data/surnames.csv";
        String addressesFile = "./data/city_of_houston.csv";

        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";


        try {

            Connection connection = DriverManager.getConnection(db_url, username, password);

            PreparedStatement firstnames = connection.prepareStatement("INSERT INTO testdata.firstnames (firstname) VALUES (?)");
            PreparedStatement surnames = connection.prepareStatement("INSERT INTO testdata.surnames (surname) VALUES (?)");
            PreparedStatement addresses = connection.prepareStatement("INSERT INTO testdata.addresses (street,city,district,region,postcode) VALUES (?,?,?,?,?)");


            boolean firstIteration = true;

            int index = 0;

            br = new BufferedReader(new FileReader(firstnamesFile));
            while ((line = br.readLine()) != null) {

                if (!firstIteration) {

                    // use comma as separator
                    String[] firstNameInArray = line.split(cvsSplitBy);

                    String firstName = firstNameInArray[0].replaceAll("[\\\\/:*?\"<>|]", "");

                    firstnames.setString(1, firstName);
                    firstnames.addBatch();

                    if (index % batchExecuteValue == 0) {

                        firstnames.executeBatch();

                    }

                } else {

                    firstIteration = false;

                }

                index++;
            }

            firstnames.executeBatch();

            index = 0;

            firstIteration = true;

            br = new BufferedReader(new FileReader(surnamesFile));
            while ((line = br.readLine()) != null) {

                if (!firstIteration) {

                    // use comma as separator
                    String[] surnameInArray = line.split(cvsSplitBy);

                    String surname = surnameInArray[0].toLowerCase().replaceAll("[\\\\/:*?\"<>|]", "");
                    surname = surname.substring(0, 1).toUpperCase() + surname.substring(1);

                    surnames.setString(1, surname);
                    surnames.addBatch();

                    if (index % batchExecuteValue == 0) {

                        surnames.executeBatch();

                    }

                } else {

                    firstIteration = false;

                }
                index++;
            }

            surnames.executeBatch();

            index = 0;

            firstIteration = true;

            br = new BufferedReader(new FileReader(addressesFile));
            while ((line = br.readLine()) != null) {

                if (!firstIteration) {

                    String[] addressInArray = line.split(cvsSplitBy);

                    String street = addressInArray[3].toLowerCase().toLowerCase().replaceAll("[\\\\/:*?\"<>|]", "");

                    if (street.length() > 1) {
                        street = street.substring(0, 1).toUpperCase() + street.substring(1);
                    }

                    String city = addressInArray[5].toLowerCase();

                    if (city.length() > 1) {
                        city = city.substring(0, 1).toUpperCase() + city.substring(1);
                    }

                    String district = addressInArray[6].toLowerCase();

                    if (district.length() > 1) {
                        district = district.substring(0, 1).toUpperCase() + district.substring(1);
                    }


                    String region = addressInArray[7].toLowerCase();

                    if (region.length() > 1) {
                        region = region.substring(0, 1).toUpperCase() + region.substring(1);
                    }

                    String postcode = addressInArray[8];

                    addresses.setString(1, street);
                    addresses.setString(2, city);
                    addresses.setString(3, district);
                    addresses.setString(4, region);
                    addresses.setString(5, postcode);
                    addresses.addBatch();

                    if (index % batchExecuteValue == 0) {

                        addresses.executeBatch();

                    }

                } else {

                    firstIteration = false;

                }
                index++;
            }

            addresses.executeBatch();


        } catch (FileNotFoundException e) {
            logger.error("unhandled exception", e);
        } catch (IOException e) {
            logger.error("unhandled exception", e);
        } catch (SQLException e) {
            logger.error("unhandled exception", e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    logger.error("unhandled exception", e);
                }
            }
        }
    }


    public void createTables() {


        String database = "warehouse";

        String dropDatabase = "DROP DATABASE IF EXISTS `" + database + "`";

        String createDatabase = "CREATE DATABASE IF NOT EXISTS `" + database + "`";

        String customer = "CREATE TABLE IF NOT EXISTS `customer` (" +
                "`id` bigint(20) unsigned NOT NULL," +
                "`name` varchar(50) NOT NULL CHECK (`name` <> '')," +
                "`address` varchar(150) NOT NULL CHECK (`address` <> '')," +
                "PRIMARY KEY (`id`))";

        String item = "CREATE TABLE IF NOT EXISTS `item` (" +
                "`id` bigint(20) unsigned NOT NULL," +
                "`name` varchar(100) NOT NULL CHECK (`name` <> '')," +
                "`balance` int(11) NOT NULL," +
                "`unit` varchar(10) NOT NULL CHECK (`unit` <> '')," +
                "`purchaseprice` decimal(65,10) NOT NULL," +
                "`vat` decimal(65,2) NOT NULL," +
                "`removed` tinyint(1) NOT NULL DEFAULT 0," +
                "PRIMARY KEY (`id`))";

        String workType = "CREATE TABLE IF NOT EXISTS `worktype` (" +
                "`id` bigint(20) unsigned NOT NULL," +
                "`name` varchar(20) NOT NULL CHECK (`name` <> '')," +
                "`price` bigint(20) NOT NULL," +
                "PRIMARY KEY (`id`))";

        String invoice = "CREATE TABLE IF NOT EXISTS `invoice` (" +
                "`id` bigint(20) unsigned NOT NULL," +
                "`customerId` bigint(20) unsigned NOT NULL," +
                "`state` int(11) NOT NULL," +
                "`duedate` date DEFAULT NULL," +
                "`previousinvoice` bigint(20) unsigned NOT NULL," +
                "PRIMARY KEY (`id`)," +
                "KEY `customerId` (`customerId`)," +
                "CONSTRAINT `customer_ibfk_1` FOREIGN KEY (`customerId`) REFERENCES `customer` (`id`))";

        String target = "CREATE TABLE IF NOT EXISTS `target` (" +
                "`id` bigint(20) unsigned NOT NULL," +
                "`name` varchar(100) NOT NULL CHECK (`name` <> '')," +
                "`address` varchar(100) NOT NULL CHECK (`address` <> '')," +
                "`customerid` bigint(20) unsigned NOT NULL," +
                "PRIMARY KEY (`id`)," +
                "KEY `customerid` (`customerid`)," +
                "CONSTRAINT `target_ibfk_1` FOREIGN KEY (`customerid`) REFERENCES `customer` (`id`))";

        String work = "CREATE TABLE IF NOT EXISTS `work` (" +
                "`id` bigint(20) unsigned NOT NULL," +
                "`name` varchar(100) NOT NULL CHECK (`name` <> '')," +
                "PRIMARY KEY (`id`))";

        String workInvoice = "CREATE TABLE IF NOT EXISTS `workinvoice` (" +
                "`workId` bigint(20) unsigned NOT NULL," +
                "`invoiceId` bigint(20) unsigned NOT NULL," +
                "PRIMARY KEY (`workId`,`invoiceId`)," +
                "KEY `workId` (`workId`)," +
                "KEY `invoiceId` (`invoiceId`)," +
                "CONSTRAINT `workinvoice_ibfk_1` FOREIGN KEY (`workId`) REFERENCES `work` (`id`)," +
                "CONSTRAINT `workinvoice_ibfk_2` FOREIGN KEY (`invoiceId`) REFERENCES `invoice` (`id`))";

        String workTarget = "CREATE TABLE IF NOT EXISTS `worktarget` (" +
                "`workId` bigint(20) unsigned NOT NULL," +
                "`targetId` bigint(20) unsigned NOT NULL," +
                "PRIMARY KEY (`workId`,`targetId`)," +
                "KEY `workId` (`workId`)," +
                "KEY `targetId` (`targetId`)," +
                "CONSTRAINT `worktarget_ibfk_1` FOREIGN KEY (`workId`) REFERENCES `work` (`id`)," +
                "CONSTRAINT `worktarget_ibfk_2` FOREIGN KEY (`targetId`) REFERENCES `target` (`id`))";

        String usedItem = "CREATE TABLE IF NOT EXISTS `useditem` (" +
                "`amount` int(11) DEFAULT NULL CHECK (`amount` > 0)," +
                "`discount` decimal(65,2) DEFAULT NULL," +
                "`workId` bigint(20) unsigned NOT NULL," +
                "`itemId` bigint(20) unsigned NOT NULL," +
                "PRIMARY KEY (`workId`,`itemId`)," +
                "KEY `itemId` (`itemId`)," +
                "CONSTRAINT `useditem_ibfk_1` FOREIGN KEY (`workId`) REFERENCES `work` (`id`)," +
                "CONSTRAINT `useditem_ibfk_2` FOREIGN KEY (`itemId`) REFERENCES `item` (`id`))";

        String workHours = "CREATE TABLE IF NOT EXISTS `workhours` (" +
                "`worktypeId` bigint(20) unsigned NOT NULL," +
                "`hours` int(11) NOT NULL," +
                "`discount` decimal(65,2) DEFAULT NULL," +
                "`workId` bigint(20) unsigned NOT NULL," +
                "PRIMARY KEY (`workId`,`worktypeId`)," +
                "KEY `worktypeId` (`worktypeId`)," +
                "KEY `workId` (`workId`)," +
                "CONSTRAINT `workhours_ibfk_1` FOREIGN KEY (`workId`) REFERENCES `work` (`id`)," +
                "CONSTRAINT `workhours_ibfk_2` FOREIGN KEY (`worktypeId`) REFERENCES `worktype` (`id`))";


        for (String db_url : sql_databases.keySet()) {

            String[] db_settings = sql_databases.get(db_url);

            executeSQLUpdate(dropDatabase, db_url, db_settings);
            executeSQLUpdate(createDatabase, db_url, db_settings);
            executeSQLUpdate(customer, db_url + database, db_settings);
            executeSQLUpdate(item, db_url + database, db_settings);
            executeSQLUpdate(workType, db_url + database, db_settings);
            executeSQLUpdate(invoice, db_url + database, db_settings);
            executeSQLUpdate(target, db_url + database, db_settings);
            executeSQLUpdate(work, db_url + database, db_settings);
            executeSQLUpdate(workInvoice, db_url + database, db_settings);
            executeSQLUpdate(workTarget, db_url + database, db_settings);
            executeSQLUpdate(usedItem, db_url + database, db_settings);
            executeSQLUpdate(workHours, db_url + database, db_settings);

        }

    }

    public void insertCustomerData(int threadCount, int iterationsPerThread, int batchExecuteValue, int invoiceFactor, int sequentialInvoices, int targetFactor, int workFactor) {

        try {

            int customerIndex;

            if (getCustomerCount() == 0) {
                customerIndex = 0;
            } else {
                customerIndex = getLastCustomerId() + 1;
            }

            int invoiceIndex;

            if (getInvoiceCount() == 0) {
                invoiceIndex = 0;
            } else {
                invoiceIndex = getLastInvoiceId() + 1;
            }

            int targetIndex;

            if (getTargetCount() == 0) {
                targetIndex = 0;
            } else {
                targetIndex = getLastTargetId() + 1;
            }

            int workCount = getWorkCount();

            if (workCount < 1) {
                throw new Exception("Work count is smaller than 1!");
            }

            getSampleData();

            ExecutorService executor = Executors.newFixedThreadPool(threadCount);

            long startTimeInMilliseconds = System.currentTimeMillis();

            Timestamp startTime = new Timestamp(startTimeInMilliseconds);

            ReentrantLock lock = new ReentrantLock();

            logger.debug("Insertion of Customer related data started at: " + startTime.toString());

            for (int i = 0; i < threadCount; i++) {
                DataGeneratorThreadCustomer thread = new DataGeneratorThreadCustomer(i, iterationsPerThread, batchExecuteValue, sql_databases, neo4j_settings, lock, invoiceFactor, targetFactor, workFactor, sequentialInvoices, firstnames, surnames, addresses, customerIndex, invoiceIndex, targetIndex, workCount);
                executor.execute(thread);
                customerIndex = customerIndex + iterationsPerThread;
                invoiceIndex = invoiceIndex + iterationsPerThread * invoiceFactor;
                targetIndex = targetIndex + iterationsPerThread * targetFactor;
            }

            executor.shutdown();
            while (!executor.isTerminated()) {
            }

            long endTimeInMilliseconds = System.currentTimeMillis();

            Timestamp endTime = new Timestamp(endTimeInMilliseconds);

            long elapsedTimeMilliseconds = endTimeInMilliseconds - startTimeInMilliseconds;

            String elapsedTime = (new SimpleDateFormat("mm:ss")).format(new Date(elapsedTimeMilliseconds));

            logger.debug("Insertion of Customer related data finished at: " + endTime.toString());
            logger.debug("Time elapsed: " + elapsedTime);


        } catch (Exception e) {
            logger.error("unhandled exception", e);
        }
    }

    public HashMap<String, Integer> insertSequentialInvoices(int threadCount, int batchExecuteValue, int sequentialInvoices) {

        int firstInvoiceIndex = 0;

        HashMap<String, Integer> customerInvoice = new HashMap<String, Integer>();

        try {

            int customerIndex = getLastCustomerId() + 1;
            int invoiceIndex = getLastInvoiceId() + 1;

            customerInvoice.put("customerIndex", customerIndex);
            customerInvoice.put("invoiceIndex", invoiceIndex);

            getSampleData();

            ExecutorService executor = Executors.newFixedThreadPool(threadCount);

            long startTimeInMilliseconds = System.currentTimeMillis();

            Timestamp startTime = new Timestamp(startTimeInMilliseconds);

            ReentrantLock lock = new ReentrantLock();

            logger.debug("Insertion of sequential invoices started at: " + startTime.toString());

            String name = firstnames.get(0) + " " + surnames.get(0);

            String streetAddress = addresses.get(0).get("street") + " " + addresses.get(0).get("city") + " " + addresses.get(0).get("district") + " " + addresses.get(0).get("region") + " " + addresses.get(0).get("postcode");

            for (String db_url : sql_databases.keySet()) {

                String[] db_info = sql_databases.get(db_url);

                String db_driver = db_info[0];
                String db_username = db_info[1];
                String db_password = db_info[2];

                Class.forName(db_driver);

                Connection connection = DriverManager.getConnection(db_url, db_username, db_password);

                PreparedStatement customer = connection.prepareStatement("INSERT INTO warehouse.customer (id, name, address) VALUES (?,?,?)");

                String sqlInsert = "INSERT INTO warehouse.customer (id, name, address) VALUES (" + customerIndex + ",\"" + name + "\",\"" + streetAddress + "\")";

                customer.setInt(1, customerIndex);
                customer.setString(2, name);
                customer.setString(3, streetAddress);
                customer.addBatch();
                customer.executeBatch();

            }

            String neo4j_db_url = neo4j_settings.get("NEO4J_DB_URL");
            String neo4j_username = neo4j_settings.get("NEO4J_USERNAME");
            String neo4j_password = neo4j_settings.get("NEO4J_PASSWORD");

            org.neo4j.driver.Driver driver = QueryTester.getNeo4jDriver(neo4j_db_url, AuthTokens.basic(neo4j_username, neo4j_password));

            Session session = driver.session();

            String cypherCreate = "CREATE (a:customer {customerId: " + customerIndex + ", name:\"" + name + "\",address:\"" + streetAddress + "\"})";
            session.run(cypherCreate);

            session.close();
            driver.close();

            firstInvoiceIndex = invoiceIndex;

            for (int i = 0; i < threadCount; i++) {

                DataGeneratorThreadSequentialInvoices thread = new DataGeneratorThreadSequentialInvoices(i, batchExecuteValue, sql_databases, neo4j_settings, lock, sequentialInvoices, customerIndex, invoiceIndex, firstInvoiceIndex);
                executor.execute(thread);
                invoiceIndex = invoiceIndex + sequentialInvoices;
            }

            executor.shutdown();
            while (!executor.isTerminated()) {
            }

            long endTimeInMilliseconds = System.currentTimeMillis();

            Timestamp endTime = new Timestamp(endTimeInMilliseconds);

            long elapsedTimeMilliseconds = endTimeInMilliseconds - startTimeInMilliseconds;

            String elapsedTime = (new SimpleDateFormat("mm:ss")).format(new Date(elapsedTimeMilliseconds));

            logger.debug("Insertion of sequential invoices finished at: " + endTime.toString());
            logger.debug("Time elapsed: " + elapsedTime);


        } catch (Exception e) {
            logger.error("unhandled exception", e);
        }

        return customerInvoice;
    }


    public void insertWorkData(int threadCount, int iterationsPerThread, int batchExecuteValue, int workTypeFactor, int itemFactor) {

        try {


            int workIndex;

            if (getWorkCount() == 0) {
                workIndex = 0;
            } else {
                workIndex = getLastWorkId() + 1;
            }

            int itemCount = getItemCount();

            int workTypeCount = getWorkTypeCount();

            ExecutorService executor = Executors.newFixedThreadPool(threadCount);

            long startTimeInMilliseconds = System.currentTimeMillis();

            Timestamp startTime = new Timestamp(startTimeInMilliseconds);

            ReentrantLock lock = new ReentrantLock();

            logger.debug("Insertion of Work related data started at: " + startTime.toString());

            for (int i = 0; i < threadCount; i++) {

                DataGeneratorThreadWork thread = new DataGeneratorThreadWork(i, iterationsPerThread, batchExecuteValue, sql_databases, neo4j_settings, lock, workIndex, itemFactor, itemCount, workTypeFactor, workTypeCount);
                executor.execute(thread);
                workIndex = workIndex + iterationsPerThread;

            }

            executor.shutdown();
            while (!executor.isTerminated()) {
            }

            long endTimeInMilliseconds = System.currentTimeMillis();

            Timestamp endTime = new Timestamp(endTimeInMilliseconds);

            long elapsedTimeMilliseconds = endTimeInMilliseconds - startTimeInMilliseconds;

            String elapsedTime = (new SimpleDateFormat("mm:ss")).format(new Date(elapsedTimeMilliseconds));

            logger.debug("Insertion of Work related data finished at: " + endTime.toString());
            logger.debug("Time elapsed: " + elapsedTime);

        } catch (Exception e) {
            logger.error("unhandled exception", e);
        }
    }


    public void insertItemsAndWorkTypes(int threadCount, int batchExecuteValue, int itemCount, int workTypeCount) {

        try {

            int itemIndex;

            if (getItemCount() == 0) {
                itemIndex = 0;
            } else {
                itemIndex = getLastItemId() + 1;
            }

            int workTypeIndex;

            if (getWorkTypeCount() == 0) {
                workTypeIndex = 0;
            } else {
                workTypeIndex = getLastWorkTypeId() + 1;
            }

            ExecutorService executor = Executors.newFixedThreadPool(threadCount);

            long startTimeInMilliseconds = System.currentTimeMillis();

            Timestamp startTime = new Timestamp(startTimeInMilliseconds);

            ReentrantLock lock = new ReentrantLock();

            logger.debug("Insertion of items and work types started at: " + startTime.toString());

            for (int i = 0; i < threadCount; i++) {

                DataGeneratorThreadItemsAndWorkTypes thread = new DataGeneratorThreadItemsAndWorkTypes(i, batchExecuteValue, sql_databases, neo4j_settings, lock, itemIndex, itemCount, workTypeIndex, workTypeCount);
                executor.execute(thread);
                itemIndex = itemIndex + itemCount;
                workTypeIndex = workTypeIndex + workTypeCount;

            }

            executor.shutdown();
            while (!executor.isTerminated()) {
            }

            long endTimeInMilliseconds = System.currentTimeMillis();

            Timestamp endTime = new Timestamp(endTimeInMilliseconds);

            long elapsedTimeMilliseconds = endTimeInMilliseconds - startTimeInMilliseconds;

            String elapsedTime = (new SimpleDateFormat("mm:ss")).format(new Date(elapsedTimeMilliseconds));

            logger.debug("Insertion of items and work types finished at: " + endTime.toString());
            logger.debug("Time elapsed: " + elapsedTime);

        } catch (Exception e) {
            logger.error("unhandled exception", e);
        }

    }

}
package homebeach;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class Main
{
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args)
    {

        HashMap<String, String[]> sql_databases = new HashMap<String, String[]>();

        String db_mariadb_url = "jdbc:mariadb://127.0.0.1:3306/";
        String db_driver = "org.mariadb.jdbc.Driver";
        String db_username = "root";
        String db_password = "root";

        String[] db_settings = new String[3];

        db_settings[0] = db_driver;
        db_settings[1] = db_username;
        db_settings[2] = db_password;

        sql_databases.put(db_mariadb_url, db_settings);

        String mysql_db_url = "jdbc:mysql://127.0.0.1:3307/";
        db_driver = "org.mariadb.jdbc.Driver";
        db_username = "root";
        db_password = "root";

        db_settings = new String[3];

        db_settings[0] = db_driver;
        db_settings[1] = db_username;
        db_settings[2] = db_password;

        sql_databases.put(mysql_db_url, db_settings);

        HashMap<String, String> neo4j_settings = new HashMap<String, String>();

        String neo4J_db_url = "bolt://localhost:7687";
        String neo4J_username = "neo4j";
        String neo4j_password = "admin";

        neo4j_settings.put("NEO4J_DB_URL", neo4J_db_url);
        neo4j_settings.put("NEO4J_USERNAME", neo4J_username);
        neo4j_settings.put("NEO4J_PASSWORD", neo4j_password);

        DataGenerator dataGenerator = new DataGenerator(sql_databases, neo4j_settings, db_mariadb_url);

        dataGenerator.createTables();

        dataGenerator.createSampleTables("jdbc:mariadb://127.0.0.1:3306/");

        dataGenerator.loadSampleData(10, "jdbc:mariadb://127.0.0.1:3306/");

        dataGenerator.insertItemsAndWorkTypes(10, 10, 10000, 10000);
        dataGenerator.insertWorkData(10,1000,10,10,10);
        dataGenerator.insertCustomerData(10,1000,10,10,0,10,10);

        QueryTester queryTester = new QueryTester(sql_databases, neo4j_settings);

        logger.debug("NO INDEXES");

        queryTester.executeQueryTestsSQL(12, true, null);
        queryTester.executeQueryTestsCypher(12, true, null);

        logger.trace("");
        logger.debug("CREATING INDEXES");
        logger.trace("");

        dataGenerator.createIndexes();

        queryTester.executeQueryTestsSQL(12, true, null);
        queryTester.executeQueryTestsCypher(12, true, null);


        logger.trace("");
        logger.debug("DELETING INDEXES");
        logger.trace("");

        dataGenerator.deleteIndexes();

        queryTester.executeComplexQueryTestSQL(12, true, null);
        queryTester.executeComplexQueryTestCypher(12, true, null);

        logger.trace("");
        logger.debug("CREATING INDEXES");
        logger.trace("");

        dataGenerator.createIndexes();

        queryTester.executeComplexQueryTestSQL(12, true, null);
        queryTester.executeComplexQueryTestCypher(12, true, null);

        logger.trace("");
        logger.debug("DELETING INDEXES");
        logger.trace("");

        dataGenerator.deleteIndexes();

        logger.trace("");
        logger.debug("REMOVING MySQL");
        logger.trace("");
        sql_databases.remove("jdbc:mysql://127.0.0.1:3307/");


        queryTester.executeQueryWithDefinedKeySQL(12, true, null);
        queryTester.executeQueryWithDefinedKeyCypher(12, true, null);

        logger.trace("");
        logger.debug("CREATING INDEXES");
        logger.trace("");

        dataGenerator.createIndexes();

        queryTester.executeQueryWithDefinedKeySQL(12, true, null);
        queryTester.executeQueryWithDefinedKeyCypher(12, true, null);

        logger.trace("");
        logger.debug("DELETING INDEXES");
        logger.trace("");

        dataGenerator.deleteIndexes();

        HashMap<String, Integer> customerInvoice =  dataGenerator.insertSequentialInvoices(1,10,100);

        int invoiceIndex = customerInvoice.get("invoiceIndex");
        int customerIndex = customerInvoice.get("customerIndex");

        queryTester.executeRecursiveQueryTestSQL(12, true, invoiceIndex, null);
        queryTester.executeRecursiveQueryTestCypher(12, true, invoiceIndex, null);

        logger.debug("customerIndex " + customerIndex);
        dataGenerator.cleanSequentialInvoices(customerIndex);

        customerInvoice =  dataGenerator.insertSequentialInvoices(1,10,1000);

        invoiceIndex = customerInvoice.get("invoiceIndex");

        queryTester.executeRecursiveQueryTestSQL(12, true, invoiceIndex, null);
        queryTester.executeRecursiveQueryTestCypher(12, true, invoiceIndex, null);

        dataGenerator.cleanSequentialInvoices(customerIndex);

        logger.trace("");
        logger.debug("CREATING INDEXES");
        logger.trace("");

        dataGenerator.createIndexes();

        customerInvoice =  dataGenerator.insertSequentialInvoices(1,10,100);

        invoiceIndex = customerInvoice.get("invoiceIndex");
        customerIndex = customerInvoice.get("customerIndex");

        queryTester.executeRecursiveQueryTestSQL(12, true, invoiceIndex, null);
        queryTester.executeRecursiveQueryTestCypher(12, true, invoiceIndex, null);

        logger.debug("customerIndex " + customerIndex);
        dataGenerator.cleanSequentialInvoices(customerIndex);

        customerInvoice =  dataGenerator.insertSequentialInvoices(1,10,1000);

        invoiceIndex = customerInvoice.get("invoiceIndex");

        queryTester.executeRecursiveQueryTestSQL(12, true, invoiceIndex, null);
        queryTester.executeRecursiveQueryTestCypher(12, true, invoiceIndex, null);
        
        dataGenerator.cleanSequentialInvoices(customerIndex);

    }
}
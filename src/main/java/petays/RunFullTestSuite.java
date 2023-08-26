package petays;

import homebeach.DataGenerator;
import homebeach.QueryTester;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class RunFullTestSuite {

    private static final Logger logger = LoggerFactory.getLogger(RunFullTestSuite.class);

    public static void main(String[] args) {
        final DataGenerator dataGenerator = new DataGenerator(Config.sql_databases, Config.neo4j_settings, Config.db_mariadb_url);

        dataGenerator.createTables();

        dataGenerator.createSampleTables(Config.db_mariadb_url);

        dataGenerator.loadSampleData(Param.batchExecuteValue, Config.db_mariadb_url);

        dataGenerator.insertItemsAndWorkTypes(Param.threadCount, Param.batchExecuteValue, Param.itemCount, Param.workTypeCount);
        dataGenerator.insertWorkData(Param.threadCount, Param.iterationsPerThread, Param.batchExecuteValue, Param.workTypeFactor, Param.itemFactor);
        dataGenerator.insertCustomerData(Param.threadCount, Param.iterationsPerThread, Param.batchExecuteValue, Param.invoiceFactor, Param.sequentialInvoices_0, Param.targetFactor, Param.workFactor);

        QueryTester queryTester = new QueryTester(Config.sql_databases, Config.neo4j_settings);

        logger.debug("NO INDEXES");

        queryTester.executeQueryTestsSQL(Param.iterations, Param.showAll, null);
        queryTester.executeQueryTestsCypher(Param.iterations, Param.showAll, null);

        logger.trace("");
        logger.debug("CREATING INDEXES");
        logger.trace("");

        dataGenerator.createIndexes();

        queryTester.executeQueryTestsSQL(Param.iterations, Param.showAll, null);
        queryTester.executeQueryTestsCypher(Param.iterations, Param.showAll, null);


        logger.trace("");
        logger.debug("DELETING INDEXES");
        logger.trace("");

        dataGenerator.deleteIndexes();

        queryTester.executeComplexQueryTestSQL(Param.iterations, Param.showAll, null);
        queryTester.executeComplexQueryTestCypher(Param.iterations, Param.showAll, null);

        logger.trace("");
        logger.debug("CREATING INDEXES");
        logger.trace("");

        dataGenerator.createIndexes();

        queryTester.executeComplexQueryTestSQL(Param.iterations, Param.showAll, null);
        queryTester.executeComplexQueryTestCypher(Param.iterations, Param.showAll, null);

        logger.trace("");
        logger.debug("DELETING INDEXES");
        logger.trace("");

        dataGenerator.deleteIndexes();

        logger.trace("");
        logger.debug("REMOVING MySQL");
        logger.trace("");
        Config.sql_databases.remove(Config.mysql_db_url);


        queryTester.executeQueryWithDefinedKeySQL(Param.iterations, Param.showAll, null);
        queryTester.executeQueryWithDefinedKeyCypher(Param.iterations, Param.showAll, null);

        logger.trace("");
        logger.debug("CREATING INDEXES");
        logger.trace("");

        dataGenerator.createIndexes();

        queryTester.executeQueryWithDefinedKeySQL(Param.iterations, Param.showAll, null);
        queryTester.executeQueryWithDefinedKeyCypher(Param.iterations, Param.showAll, null);

        logger.trace("");
        logger.debug("DELETING INDEXES");
        logger.trace("");

        dataGenerator.deleteIndexes();

        HashMap<String, Integer> customerInvoice = dataGenerator.insertSequentialInvoices(1, Param.batchExecuteValue, Param.sequentialInvoices_100);

        int invoiceIndex = customerInvoice.get("invoiceIndex");
        int customerIndex = customerInvoice.get("customerIndex");

        queryTester.executeRecursiveQueryTestSQL(Param.iterations, Param.showAll, invoiceIndex, null);
        queryTester.executeRecursiveQueryTestCypher(Param.iterations, Param.showAll, invoiceIndex, null);

        logger.debug("customerIndex " + customerIndex);
        dataGenerator.cleanSequentialInvoices(customerIndex);

        customerInvoice = dataGenerator.insertSequentialInvoices(1, Param.batchExecuteValue, Param.sequentialInvoices_1000);

        invoiceIndex = customerInvoice.get("invoiceIndex");

        queryTester.executeRecursiveQueryTestSQL(Param.iterations, Param.showAll, invoiceIndex, null);
        queryTester.executeRecursiveQueryTestCypher(Param.iterations, Param.showAll, invoiceIndex, null);

        dataGenerator.cleanSequentialInvoices(customerIndex);

        logger.trace("");
        logger.debug("CREATING INDEXES");
        logger.trace("");

        dataGenerator.createIndexes();

        customerInvoice = dataGenerator.insertSequentialInvoices(1, Param.batchExecuteValue, Param.sequentialInvoices_100);

        invoiceIndex = customerInvoice.get("invoiceIndex");
        customerIndex = customerInvoice.get("customerIndex");

        queryTester.executeRecursiveQueryTestSQL(Param.iterations, Param.showAll, invoiceIndex, null);
        queryTester.executeRecursiveQueryTestCypher(Param.iterations, Param.showAll, invoiceIndex, null);

        logger.debug("customerIndex " + customerIndex);
        dataGenerator.cleanSequentialInvoices(customerIndex);

        customerInvoice = dataGenerator.insertSequentialInvoices(1, Param.batchExecuteValue, Param.sequentialInvoices_1000);

        invoiceIndex = customerInvoice.get("invoiceIndex");

        queryTester.executeRecursiveQueryTestSQL(Param.iterations, Param.showAll, invoiceIndex, null);
        queryTester.executeRecursiveQueryTestCypher(Param.iterations, Param.showAll, invoiceIndex, null);

        dataGenerator.cleanSequentialInvoices(customerIndex);

    }
}

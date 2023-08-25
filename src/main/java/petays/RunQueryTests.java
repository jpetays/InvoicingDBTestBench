package petays;

import homebeach.DataGenerator;
import homebeach.QueryTester;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class RunQueryTests {

    private static final Logger logger = LoggerFactory.getLogger(RunQueryTests.class);

    public static void main(String[] args) {

        final QueryTester queryTester = new QueryTester(Config.sql_databases, Config.neo4j_settings);

        // iterations = 2
        // This run takes about 20 minutes on AMD Ryzen 7 2700X Eight-Core Processor 3.70 GHz.
        // iterations = 3
        // This run takes about 30 minutes on AMD Ryzen 7 2700X Eight-Core Processor 3.70 GHz.
        // This code is single threaded and CPU utilization is about 10%.
        Param.iterations = 3;
        Param.sequentialInvoices_100 = 10;
        Param.sequentialInvoices_1000 = 100;

        logger.info("Simple query tests");
        queryTester.executeQueryTestsSQL(Param.iterations, Param.showAll);
        queryTester.executeQueryTestsCypher(Param.iterations, Param.showAll);

        logger.info("Complex query tests");
        queryTester.executeComplexQueryTestSQL(Param.iterations, Param.showAll);
        queryTester.executeComplexQueryTestCypher(Param.iterations, Param.showAll);

        logger.info("Complex query tests - no MySQL");
        queryTester.executeQueryWithDefinedKeySQL(Param.iterations, Param.showAll);
        queryTester.executeQueryWithDefinedKeyCypher(Param.iterations, Param.showAll);

        final int[] sequentialInvoices = new int[]{Param.sequentialInvoices_100, Param.sequentialInvoices_1000};
        logger.info("Sequential Invoices tests #{}", sequentialInvoices.length);

        final DataGenerator dataGenerator = new DataGenerator(Config.sql_databases, Config.neo4j_settings, Config.db_mariadb_url);

        for (int sequentialInvoice : sequentialInvoices) {

            logger.info("Insert Sequential Invoices {}", sequentialInvoice);
            final HashMap<String, Integer> customerInvoice = dataGenerator.insertSequentialInvoices(1, Param.batchExecuteValue, sequentialInvoice);

            final int customerIndex = customerInvoice.get("customerIndex");
            final int invoiceIndex = customerInvoice.get("invoiceIndex");

            logger.info("Recursive query tests {}, customerIndex {}, invoiceIndex {}", sequentialInvoice, customerIndex, invoiceIndex);
            queryTester.executeRecursiveQueryTestSQL(Param.iterations, Param.showAll, invoiceIndex);
            queryTester.executeRecursiveQueryTestCypher(Param.iterations, Param.showAll, invoiceIndex);

            logger.debug("Delete Sequential Invoices: {}, customerIndex {}", sequentialInvoice, customerIndex);
            dataGenerator.cleanSequentialInvoices(customerIndex);
        }
    }
}

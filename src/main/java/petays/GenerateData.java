package petays;

import homebeach.DataGenerator;

public class GenerateData {

    public static void main(final String[] args) {

        final int threadCount = 10;
        final int batchExecuteValue = 10;
        final int iterationsPerThread = 1000;

        final int itemCount = 10000;
        final int workTypeCount = 10000;

        final int workTypeFactor = 10;
        final int itemFactor = 10;

        final int invoiceFactor = 10;
        final int sequentialInvoices = 0;
        final int targetFactor = 10;
        final int workFactor = 10;

        final DataGenerator dataGenerator = new DataGenerator(Config.sql_databases, Config.neo4j_settings, Config.db_mariadb_url);

        dataGenerator.createSampleTables(Config.db_mariadb_url);
        dataGenerator.loadSampleData(batchExecuteValue, Config.db_mariadb_url);

        dataGenerator.createTables();
        dataGenerator.insertItemsAndWorkTypes(threadCount, batchExecuteValue, itemCount, workTypeCount);
        dataGenerator.insertWorkData(threadCount, iterationsPerThread, batchExecuteValue, workTypeFactor, itemFactor);
        dataGenerator.insertCustomerData(threadCount, iterationsPerThread, batchExecuteValue, invoiceFactor, sequentialInvoices, targetFactor, workFactor);
    }
}

package petays;

import homebeach.DataGenerator;

public class GenerateData {

    public static void main(final String[] args) {

        final DataGenerator dataGenerator = new DataGenerator(Config.sql_databases, Config.neo4j_settings, Config.db_mariadb_url);

        dataGenerator.createSampleTables(Config.db_mariadb_url);
        dataGenerator.loadSampleData(Param.batchExecuteValue, Config.db_mariadb_url);

        dataGenerator.createTables();
        dataGenerator.insertItemsAndWorkTypes(Param.threadCount, Param.batchExecuteValue, Param.itemCount, Param.workTypeCount);
        dataGenerator.insertWorkData(Param.threadCount, Param.iterationsPerThread, Param.batchExecuteValue, Param.workTypeFactor, Param.itemFactor);
        dataGenerator.insertCustomerData(Param.threadCount, Param.iterationsPerThread, Param.batchExecuteValue, Param.invoiceFactor, Param.sequentialInvoices_0, Param.targetFactor, Param.workFactor);
    }
}

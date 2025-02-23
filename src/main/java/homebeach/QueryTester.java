package homebeach;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import petays.RunQueryTests;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.StringJoiner;

public class QueryTester {

    private static final Logger logger = LoggerFactory.getLogger(QueryTester.class);

    private HashMap<String, String[]> sql_databases;

    private HashMap<String, String> neo4j_settings;

    //private HashMap<String, ArrayList<Long>> resultLists;

    //private List<Long> results;

    private Logger dataLogger;

    public QueryTester(HashMap<String, String[]> sql_databases, HashMap<String, String> neo4j_settings) {
        this.sql_databases = sql_databases;
        this.neo4j_settings = neo4j_settings;
    }

    public HashMap<String, ArrayList<Long>> measureQueryTimeSQL(String sqlQuery, int iterations) {

        HashMap<String, ArrayList<Long>> resultLists = new HashMap<String, ArrayList<Long>>();

        ArrayList<Long> results = null;

        Connection connection = null;
        Statement stmt = null;

        logger.debug("Executing SQL Query: " + sqlQuery + " in " + sql_databases.size() + " databases with " + iterations + " iterations.");

        try {

            for (String db_url : sql_databases.keySet()) {

                String[] db_info = sql_databases.get(db_url);

                String db_driver = db_info[0];
                String db_username = db_info[1];
                String db_password = db_info[2];

                Class.forName(db_driver);

                connection = DriverManager.getConnection(db_url + "warehouse", db_username, db_password);

                DatabaseMetaData meta = connection.getMetaData();

                String productName = meta.getDatabaseProductName();
                String productVersion = meta.getDatabaseProductVersion();

                stmt = connection.createStatement();

                results = new ArrayList<Long>();

                int rowCount = 0;

                for (int i = 0; i < iterations; i++) {

                    logger.trace("Starting iteration: " + i + ".");

                    long startTimeInMilliseconds = System.currentTimeMillis();

                    final var resultSet = stmt.executeQuery(sqlQuery);
                    rowCount = logQueryResult(i, resultSet, sqlQuery);

                    long endTimeInMilliseconds = System.currentTimeMillis();
                    long elapsedTimeMilliseconds = endTimeInMilliseconds - startTimeInMilliseconds;

                    results.add(elapsedTimeMilliseconds);

                }

                resultLists.put(productVersion, results);

                logger.debug("SQL Query returned " + rowCount + " rows.");
            }

        } catch (Exception e) {
            logger.error("unhandled exception", e);
            throw new RuntimeException(e);
        } finally {

            try {
                if (stmt != null) {
                    connection.close();
                }
            } catch (SQLException se) {
            }
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                logger.error("unhandled exception", e);
            }
        }

        logger.trace("");

        return resultLists;
    }

    public List<Long> measureQueryTimeCypher(String cypherQuery, int iterations) {

        String neo4j_db_url = neo4j_settings.get("NEO4J_DB_URL");
        String neo4j_username = neo4j_settings.get("NEO4J_USERNAME");
        String neo4j_password = neo4j_settings.get("NEO4J_PASSWORD");

        org.neo4j.driver.Driver driver = getNeo4jDriver(neo4j_db_url, AuthTokens.basic(neo4j_username, neo4j_password));

        Session session = driver.session();

        List<Long> results = new ArrayList<Long>();

        int rowCount = 0;

        logger.debug("Executing Cypher Query: " + cypherQuery + " with " + iterations + " iterations.");

        for (int i = 0; i < iterations; i++) {

            logger.trace("Starting iteration: " + i + ".");

            long startTimeInMilliseconds = System.currentTimeMillis();

            final var result = session.run(cypherQuery);
            rowCount = logQueryResult(i, result, cypherQuery);

            long endTimeInMilliseconds = System.currentTimeMillis();
            long elapsedTimeMilliseconds = endTimeInMilliseconds - startTimeInMilliseconds;

            results.add(elapsedTimeMilliseconds);

        }

        logger.debug("Cypher query returned: " + rowCount + " records.");
        session.close();
        driver.close();

        return results;
    }

    public void showResults(List<Long> results, boolean showAll) {

        if (results.size() == 0) {
            return;
        }

        //Collections.sort(results);

        /*if (showAll) {
            logger.debug("Smallest number in result set: {}", results.get(0));
            logger.debug("Biggest number in result set: {}", results.get(results.size() - 1));
        }

        if (results.size() > 2) {
            results.remove(0);
            results.remove(results.size() - 1);
        }*/

        long sum = 0;

        if (showAll) {
            logger.trace("");
        }

        String prefix = "Query timings[" + results.size() + "]: ";
        StringJoiner joiner = new StringJoiner(", ", prefix, "");
        for (Long result : results) {
            joiner.add(result.toString());
            sum = sum + result;
        }
        long average = sum / results.size();
        int standardDeviation = (int) (calculateStandardDeviation(results) + 0.5);

        logger.debug("{}. Standard deviation {} (ms) and average time for query {}", joiner, standardDeviation, formatDurationMs(average));
        logger.trace("");


    }

    private static String formatDurationMs(final long elapsedTimeMs) {
        if (elapsedTimeMs < 1000) {
            return String.format("%d", elapsedTimeMs) + " ms";
        }
        final var elapsedTimeSec = elapsedTimeMs / 1000.0;
        if (elapsedTimeSec < 60.0) {
            return String.format("%.1f", elapsedTimeSec) + " s";
        }
        if (elapsedTimeSec < 3600.0) {
            return String.format("%.1f", elapsedTimeSec / 60.0) + " m";
        }
        return String.format("%.1f", elapsedTimeSec / 3600.0) + " h";
    }

    public static double calculateStandardDeviation(List<Long> results) {
        double sum = 0.0, standardDeviation = 0.0;
        int size = results.size();

        for (long result : results) {
            sum += result;
        }

        double mean = sum / size;

        for (double result : results) {
            standardDeviation += Math.pow(result - mean, 2);
        }

        return Math.sqrt(standardDeviation / size);
    }

    public void executeQueryTestsSQL(int iterations, boolean showAll, Logger dataLogger) {

        setDataLogger(dataLogger, "test.sqlLogger");
        logger.debug("*");
        logger.debug("Short query, work price");

        String workPriceSQL =
                "SELECT work.id AS workId, " +
                        "SUM( " +
                        "(worktype.price * workhours.hours * workhours.discount) " +
                        ") AS price " +
                        "FROM work " +
                        "INNER JOIN workhours ON work.id = workhours.workId " +
                        "INNER JOIN worktype ON worktype.id = workhours.worktypeId " +
                        "GROUP BY work.id";

        var resultLists = measureQueryTimeSQL(workPriceSQL, iterations);

        for (String databaseVersion : resultLists.keySet()) {

            if (databaseVersion.contains("MariaDB")) {
                logger.debug("Results for MariaDB version " + databaseVersion);
            } else {
                logger.debug("Results for MySQL version " + databaseVersion);
            }

            final var results = resultLists.get(databaseVersion);
            showResults(results, showAll);

        }

        logger.debug("Long query, work price");

        String workPriceWithItemsSQL =
                "SELECT work.id AS workId, " +
                        "SUM(" +
                        "(worktype.price * workhours.hours * workhours.discount) + " +
                        "(item.purchaseprice * useditem.amount * useditem.discount) " +
                        ") AS price " +
                        "FROM work " +
                        "INNER JOIN workhours ON work.id = workhours.workId " +
                        "INNER JOIN worktype ON worktype.id = workhours.worktypeId " +
                        "INNER JOIN useditem ON work.id = useditem.workId " +
                        "INNER JOIN item ON useditem.itemId = item.id " +
                        "GROUP BY work.id";


        resultLists = measureQueryTimeSQL(workPriceWithItemsSQL, iterations);

        for (String databaseVersion : resultLists.keySet()) {

            if (databaseVersion.contains("MariaDB")) {
                logger.debug("Results for MariaDB version " + databaseVersion);
            } else {
                logger.debug("Results for MySQL version " + databaseVersion);
            }

            final var results = resultLists.get(databaseVersion);
            showResults(results, showAll);

        }

        logger.debug("Query with defined key, work of invoiceId 0");

        String workOfInvoiceSQL = "SELECT * FROM work INNER JOIN workInvoice ON work.id=workInvoice.workId INNER JOIN invoice ON workInvoice.workId=invoice.id AND invoice.id=0";

        resultLists = measureQueryTimeSQL(workOfInvoiceSQL, iterations);

        for (String databaseVersion : resultLists.keySet()) {

            if (databaseVersion.contains("MariaDB")) {
                logger.debug("Results for MariaDB version " + databaseVersion);
            } else {
                logger.debug("Results for MySQL version " + databaseVersion);
            }

            final var results = resultLists.get(databaseVersion);
            showResults(results, showAll);

        }

    }

    public void executeQueryWithDefinedKeySQL(int iterations, boolean showAll, Logger dataLogger) {

        setDataLogger(dataLogger, "test.sqlLogger");
        logger.debug("*");
        logger.debug("Query with defined key, invoice prices for customerId 0");

        String invoicePricesForCustomerSQL = "SELECT q1.customerId, q2.invoiceId, SUM(q3.price) AS invoicePrice FROM " +
                "( SELECT customer.id AS customerId, invoice.id AS invoiceId FROM invoice INNER JOIN customer ON invoice.customerId=customer.id ) AS q1 INNER JOIN " +
                "( SELECT workinvoice.invoiceId, workinvoice.workId FROM workinvoice INNER JOIN invoice ON workinvoice.invoiceId = invoice.id ) AS q2 USING (invoiceId) INNER JOIN " +
                "( SELECT workhours.workid AS workId, SUM( (worktype.price * workhours.hours * workhours.discount) + (item.purchaseprice * useditem.amount * useditem.discount) ) AS price FROM workhours INNER JOIN worktype ON workhours.worktypeid = worktype.id INNER JOIN useditem ON workhours.workid = useditem.workid INNER JOIN item ON useditem.itemid = item.id GROUP BY workhours.workid ) " +
                "AS q3 USING (workId) WHERE q1.customerId=0 GROUP BY q2.invoiceId";

        final var resultLists = measureQueryTimeSQL(invoicePricesForCustomerSQL, iterations);

        for (String databaseVersion : resultLists.keySet()) {

            if (databaseVersion.contains("MariaDB")) {
                logger.debug("Results for MariaDB version " + databaseVersion);
            } else {
                logger.debug("Results for MySQL version " + databaseVersion);
            }

            final var results = resultLists.get(databaseVersion);
            showResults(results, showAll);

        }

    }

    public void executeQueryWithDefinedKeyCypher(int iterations, boolean showAll, Logger dataLogger) {

        setDataLogger(dataLogger, "test.cypherLogger");
        logger.debug("*");
        logger.debug("Query with defined key, invoice prices for customerId 0");

        String invoicePricesForCustomerCypher = "MATCH (c:customer)-[:PAYS]->(inv:invoice) WHERE c.customerId=0 " +
                "WITH c, inv " +
                "OPTIONAL MATCH (inv)-[:WORK_INVOICE]->(w:work) " +
                "WITH c, inv, w " +
                "OPTIONAL MATCH (wt:worktype)-[h:WORKHOURS]->(w:work)-[u:USED_ITEM]->(i:item) " +
                "WITH c, inv, w, SUM((h.hours*h.discount*wt.price)+(u.amount*u.discount*i.purchaseprice)) as workPrice " +
                "RETURN c, inv, SUM(workPrice) as invoicePrice";

        var results = measureQueryTimeCypher(invoicePricesForCustomerCypher, iterations);

        showResults(results, showAll);

        logger.trace("");


        logger.debug("Query with defined key with CALL, invoice prices for customerId 0");

        String invoicePricesForCustomerCypher3 = "MATCH (inv:invoice) WHERE inv.customerId=0 " +
                "CALL { " +
                "   WITH inv " +
                "   MATCH (c:customer)-[:PAYS]->(inv) " +
                "   RETURN c " +
                "}" +
                "CALL { " +
                "   WITH c, inv " +
                "   MATCH (inv)-[:WORK_INVOICE]->(w:work) " +
                "   RETURN w " +
                "} " +
                "CALL { " +
                "   WITH w " +
                "   MATCH (wt:worktype)-[h:WORKHOURS]->(w)-[u:USED_ITEM]->(i:item) " +
                "   RETURN SUM((h.hours*h.discount*wt.price)+(u.amount*u.discount*i.purchaseprice)) as workPrice " +
                "} " +
                "RETURN c, inv, SUM(workPrice) as invoicePrice";

        results = measureQueryTimeCypher(invoicePricesForCustomerCypher3, iterations);

        showResults(results, showAll);

        /*


         */


    }

    public void executeQueryTestsCypher(int iterations, boolean showAll, Logger dataLogger) {

        setDataLogger(dataLogger, "test.cypherLogger");
        logger.debug("*");
        logger.debug("Short query1, work price");

        String workPriceCypher = "MATCH (wt:worktype)-[h:WORKHOURS]->(w:work) WITH SUM(h.hours*h.discount*wt.price) as price, w RETURN w.workId as workId, price;";

        var results = measureQueryTimeCypher(workPriceCypher, iterations);

        showResults(results, showAll);

        logger.trace("");

        logger.debug("Short query2, work price");

        String workPriceCypher2 = "MATCH (w:work) " +
                "CALL { " +
                "    WITH w " +
                "    MATCH (wt:worktype)-[h:WORKHOURS]->(w) " +
                "    RETURN SUM((h.hours*h.discount*wt.price)) as price " +
                "} " +
                "RETURN w.workId as workId, price;";

        results = measureQueryTimeCypher(workPriceCypher2, iterations);

        showResults(results, showAll);

        logger.trace("");


        logger.debug("Long query1, work price");

        String workPriceWithItemsCypher = "MATCH (wt:worktype)-[h:WORKHOURS]->(w:work)-[u:USED_ITEM]->(i:item) WITH SUM((h.hours*h.discount*wt.price)+(u.amount*u.discount*i.purchaseprice)) as price, w RETURN w.workId as workId, price";

        results = measureQueryTimeCypher(workPriceWithItemsCypher, iterations);

        showResults(results, showAll);

        logger.debug("Long query2, work price");

        String workPriceWithItemsCypher2 = "MATCH (w:work) " +
                "CALL { " +
                "    WITH w " +
                "    MATCH (wt:worktype)-[h:WORKHOURS]->(w)-[u:USED_ITEM]->(i:item) " +
                "    RETURN SUM((h.hours*h.discount*wt.price)+(u.amount*u.discount*i.purchaseprice)) as price " +
                "} " +
                "RETURN w.workId as workId, price;";

        results = measureQueryTimeCypher(workPriceWithItemsCypher2, iterations);

        showResults(results, showAll);

        logger.trace("");

        logger.debug("Query with defined key, work of invoice");

        String workOfInvoiceCypher = "MATCH (i:invoice { invoiceId:0 })-[wi:WORK_INVOICE]->(w:work) RETURN *";

        results = measureQueryTimeCypher(workOfInvoiceCypher, iterations);

        showResults(results, showAll);

    }

    public void executeComplexQueryTestSQL(int iterations, boolean showAll, Logger dataLogger) {

        setDataLogger(dataLogger, "test.sqlLogger");
        logger.debug("*");
        logger.debug("Complex query, invoice price");

        String invoicePriceSQL =
                "SELECT q1.invoiceId, SUM(q2.price) AS invoicePrice " +
                        "FROM ( " +
                        "SELECT workinvoice.invoiceId, workinvoice.workId " +
                        "FROM workinvoice " +
                        "INNER JOIN invoice ON workinvoice.invoiceId = invoice.id " +
                        ") AS q1 " +
                        "INNER JOIN ( " +
                        "SELECT workhours.workid AS workId, " +
                        "SUM( " +
                        "(worktype.price * workhours.hours * workhours.discount) + " +
                        "(item.purchaseprice * useditem.amount * useditem.discount) " +
                        ") AS price " +
                        "FROM workhours " +
                        "INNER JOIN worktype ON workhours.worktypeid = worktype.id " +
                        "INNER JOIN useditem ON workhours.workid = useditem.workid " +
                        "INNER JOIN item ON useditem.itemid = item.id " +
                        "GROUP BY workhours.workid " +
                        ") AS q2 USING (workId) " +
                        "GROUP BY q1.invoiceId";

        final var resultLists = measureQueryTimeSQL(invoicePriceSQL, iterations);

        for (String databaseVersion : resultLists.keySet()) {

            if (databaseVersion.contains("MariaDB")) {
                logger.debug("Results for MariaDB version " + databaseVersion);
            } else {
                logger.debug("Results for MySQL version " + databaseVersion);
            }

            final var results = resultLists.get(databaseVersion);
            showResults(results, showAll);

        }

        logger.trace("");

    }

    public void executeComplexQueryTestCypher(int iterations, boolean showAll, Logger dataLogger) {

        setDataLogger(dataLogger, "test.cypherLogger");
        logger.debug("*");
        logger.debug("Complex query, invoice price");

        logger.trace("");

        String invoicePriceCypher = "MATCH (inv:invoice)-[:WORK_INVOICE]->(w:work) " +
                "WITH inv, w " +
                "OPTIONAL MATCH (wt:worktype)-[h:WORKHOURS]->(w:work)-[u:USED_ITEM]->(i:item) " +
                "WITH inv, w, SUM((h.hours*h.discount*wt.price)+(u.amount*u.discount*i.purchaseprice)) as workPrice " +
                "RETURN inv, SUM(workPrice) as invoicePrice";

        var results = measureQueryTimeCypher(invoicePriceCypher, iterations);

        showResults(results, showAll);

        logger.debug("Complex query with CALL, invoice price");

        logger.trace("");

        String invoicePriceCypher3 =
                "MATCH (inv:invoice) " +
                        "CALL { " +
                        "WITH inv " +
                        "MATCH (inv)-[:WORK_INVOICE]->(w:work) " +
                        "RETURN w" +
                        "} " +
                        "CALL { " +
                        "WITH w " +
                        "MATCH (wt:worktype)-[h:WORKHOURS]->(w)-[u:USED_ITEM]->(i:item) " +
                        "RETURN SUM((h.hours*h.discount*wt.price)+(u.amount*u.discount*i.purchaseprice)) as workPrice " +
                        "} " +
                        "RETURN inv, SUM(workPrice) as invoicePrice";

        results = measureQueryTimeCypher(invoicePriceCypher3, iterations);

        showResults(results, showAll);

    }


    public void executeCyclicQueryTestSQL(int iterations, boolean showAll, int invoiceId) {

        logger.debug("Executing recursive query test");

        logger.debug("Cyclic query SQL, invoices related to invoice id " + invoiceId);

        String previousInvoicesSQL = "SELECT  id,customerid,state,duedate,previousinvoice " +
                "FROM (SELECT * FROM invoice " +
                "ORDER BY previousinvoice, id) invoices_sorted, " +
                "(SELECT @pv := '" + invoiceId + "') initialisation " +
                "WHERE find_in_set(previousinvoice, @pv) " +
                "AND length(@pv := concat(@pv, ',', id))";

        final var resultLists = measureQueryTimeSQL(previousInvoicesSQL, iterations);

        for (String databaseVersion : resultLists.keySet()) {

            if (databaseVersion.contains("MariaDB")) {
                logger.debug("Results for MariaDB version " + databaseVersion);
            } else {
                logger.debug("Results for MySQL version " + databaseVersion);
            }

            final var results = resultLists.get(databaseVersion);
            showResults(results, showAll);

        }

    }

    public void executeRecursiveQueryTestCypher(int iterations, boolean showAll, int invoiceId, Logger dataLogger) {

        setDataLogger(dataLogger, "test.cypherLogger");
        logger.debug("*");
        logger.debug("Executing recursive query test");

        logger.debug("Recursive query Cypher, invoices related to invoice id " + invoiceId);

        String previousInvoicesCypher = "MATCH (i:invoice { invoiceId:" + invoiceId + " })-[p:PREVIOUS_INVOICE *0..]->(j:invoice) RETURN *";

        var results = measureQueryTimeCypher(previousInvoicesCypher, iterations);

        showResults(results, showAll);

        logger.trace("");

        logger.debug("Recursive query Cypher optimized, invoices related to invoice id " + invoiceId);

        String previousInvoicesCypherOptimized = "MATCH inv=(i:invoice { invoiceId:" + invoiceId + "})-[p:PREVIOUS_INVOICE *0..]->(j:invoice) WHERE NOT (j)-[:PREVIOUS_INVOICE]->() RETURN nodes(inv)";

        results = measureQueryTimeCypher(previousInvoicesCypherOptimized, iterations);

        showResults(results, showAll);


    }

    public void executeRecursiveQueryTestSQL(int iterations, boolean showAll, int invoiceId, Logger dataLogger) {

        setDataLogger(dataLogger, "test.sqlLogger");
        logger.debug("*");
        logger.debug("Executing recursive query test for optimized queries");

        logger.debug("Recursive query SQL with Common Table Expressions, invoices related to invoice id " + invoiceId);

        String previousInvoicesCTESQL = "WITH RECURSIVE previous_invoices AS (" +
                "SELECT id, customerId, state, duedate, previousinvoice " +
                "FROM invoice " +
                "WHERE id=" + invoiceId + " " +
                "UNION ALL " +
                "SELECT i.id, i.customerId, i.state, i.duedate, i.previousinvoice " +
                "FROM invoice AS i INNER JOIN previous_invoices AS j " +
                "ON i.previousinvoice = j.id " +
                "WHERE i.previousinvoice <> i.id" +
                ") " +
                "SELECT * FROM previous_invoices";

        HashMap<String, String[]> tempSql_databases = (HashMap<String, String[]>) this.sql_databases.clone();

        this.sql_databases.remove("jdbc:mysql://127.0.0.1:3307/");

        final var resultLists = measureQueryTimeSQL(previousInvoicesCTESQL, iterations);

        for (String databaseVersion : resultLists.keySet()) {

            if (databaseVersion.contains("MariaDB")) {
                logger.debug("Results for MariaDB version " + databaseVersion);
            }

            final var results = resultLists.get(databaseVersion);
            showResults(results, showAll);

        }

        this.sql_databases = (HashMap<String, String[]>) tempSql_databases.clone();

    }

    private void setDataLogger(Logger dataLogger, String defaultLoggerName) {
        this.dataLogger = dataLogger != null ? dataLogger : LoggerFactory.getLogger(defaultLoggerName);
    }

    private static final int logRowCount = 10;
    private static final int maxIterations = 1;

    private int logQueryResult(final int iteration, final ResultSet resultSet, final String query) throws SQLException {
        if (iteration >= maxIterations) {
            resultSet.last();
            return resultSet.getRow();
        }
        dataLogger.debug("query:\t{}", query);
        // Used Statement is TYPE_FORWARD_ONLY and CONCUR_READ_ONLY
        final var metaData = resultSet.getMetaData();
        final var columnCount = metaData.getColumnCount();
        {
            final var joiner = new StringJoiner("\t", "", "");
            for (var i = 1; i <= columnCount; ++i) {
                joiner.add(metaData.getColumnName(i));
            }
            dataLogger.debug("{}:\t{}", 0, joiner);
        }
        var rowCount = 0;
        while (resultSet.next()) {
            if (resultSet.isLast()) {
                // At end.
                break;
            }
            final var joiner = new StringJoiner("\t", "", "");
            for (var col = 1; col <= columnCount; ++col) {
                joiner.add(resultSet.getObject(col).toString());
            }
            dataLogger.debug("{}:\t{}", ++rowCount, joiner);
            if (rowCount == logRowCount) {
                // Skip to end.
                resultSet.last();
                break;
            }
        }
        rowCount = resultSet.getRow();
        dataLogger.debug("{}\trows", rowCount);
        return rowCount;
    }

    private int logQueryResult(final int iteration, final Result result, final String query) {
        if (iteration >= maxIterations) {
            return result.list().size();
        }
        dataLogger.debug("query:\t{}", query);
        final var records = result.list();
        var columnCount = 0;
        var rowCount = Math.min(records.size(), logRowCount);
        for (var i = 0; i < rowCount; ++i) {
            final var record = records.get(i);
            if (i == 0) {
                columnCount = record.keys().size();
                final var joiner = new StringJoiner("\t", "", "");
                for (var col = 0; col < columnCount; ++col) {
                    joiner.add(record.keys().get(col));
                }
                dataLogger.debug("{}:\t{}", 0, joiner);
            }
            final var joiner = new StringJoiner("\t", "", "");
            for (var col = 0; col < columnCount; ++col) {
                joiner.add(record.values().get(col).toString());
            }
            dataLogger.debug("{}:\t{}", i + 1, joiner);
        }
        rowCount = records.size();
        dataLogger.debug("{}\trows", rowCount);
        return rowCount;
    }

    public static org.neo4j.driver.Driver getNeo4jDriver(final String neo4j_db_url, final AuthToken authToken) {
        // https://neo4j.com/docs/api/java-driver/current/org.neo4j.driver/org/neo4j/driver/Logging.html
        final var config = Config.builder().withLogging(Logging.none()).build();
        return GraphDatabase.driver(neo4j_db_url, authToken, config);
    }
}
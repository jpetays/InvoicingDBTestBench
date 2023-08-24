package petays;

import homebeach.QueryTester;

public class RunQueryTests {

    public static void main(String[] args) {

        final QueryTester queryTester = new QueryTester(Config.sql_databases, Config.neo4j_settings);

        final int iterations = 12;
        final boolean showAll = true;
        queryTester.executeQueryTestsSQL(iterations, showAll);
        queryTester.executeQueryTestsCypher(iterations, showAll);

    }
}

package petays;

import java.util.HashMap;
import java.util.Map;

import static java.util.Map.entry;

public abstract class Config {

    public static String db_mariadb_url;
    public static String mysql_db_url;
    public static String db_neo4j_url;

    public static HashMap<String, String[]> sql_databases;
    public static HashMap<String, String> neo4j_settings;

    static {
        db_mariadb_url = "jdbc:mariadb://localhost:3306/";
        mysql_db_url = "jdbc:mysql://127.0.0.1:3307/";
        db_neo4j_url = "bolt://localhost:7687";

        sql_databases = new HashMap<>(
                Map.ofEntries(
                        entry(db_mariadb_url, new String[]{"org.mariadb.jdbc.Driver", "root", "root"})
                )
        );

        neo4j_settings = new HashMap<>(
                Map.ofEntries(
                        entry("NEO4J_DB_URL", db_neo4j_url),
                        entry("NEO4J_USERNAME", "neo4j"),
                        entry("NEO4J_PASSWORD", "neo4j")
                )
        );

    }
}

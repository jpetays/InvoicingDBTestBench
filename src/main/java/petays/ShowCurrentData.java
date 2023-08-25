package petays;

import homebeach.DataGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

public class ShowCurrentData {
    private static final Logger logger = LoggerFactory.getLogger(ShowCurrentData.class);

    public static void main(final String[] args) throws SQLException {

        final DataGenerator dataGenerator = new DataGenerator(Config.sql_databases, Config.neo4j_settings, Config.db_mariadb_url);

        String[] db_settings = Config.sql_databases.get(Config.db_mariadb_url);

        logger.debug("Get SQL row counts from {}", Config.db_mariadb_url);
        final var customerCount = dataGenerator.getCustomerCount();
        final var lastCustomerId = dataGenerator.getLastCustomerId();
        final var invoiceCount = dataGenerator.getInvoiceCount();
        final var lastInvoiceId = dataGenerator.getLastInvoiceId();
        final var itemCount = dataGenerator.getItemCount();
        final var lastItemId = dataGenerator.getLastItemId();
        final var workCount = dataGenerator.getWorkCount();
        final var lastWorkId = dataGenerator.getLastWorkId();
        final var workTypeCount = dataGenerator.getWorkTypeCount();
        final var lastWorkTypeId = dataGenerator.getLastWorkTypeId();
        logger.debug("SQL customerCount {} lastCustomerId {}", customerCount, lastCustomerId);
        logger.debug("SQL invoiceCount {} lastInvoiceId {}", invoiceCount, lastInvoiceId);
        logger.debug("SQL itemCount {} lastItemId {}", itemCount, lastItemId);
        logger.debug("SQL workCount {} lastWorkId {}", workCount, lastWorkId);
        logger.debug("SQL workTypeCount {} lastWorkTypeId {}", workTypeCount, lastWorkTypeId);

        logger.debug("Get NEO4J row counts from {}", Config.db_neo4j_url);
        logger.debug("NEO4J customerCount {}", dataGenerator.getCustomerCountCypher());
        logger.debug("NEO4J invoiceCount {}", dataGenerator.getInvoiceCountCypher());
        logger.debug("NEO4J itemCount {}", dataGenerator.getItemCountCypher());
        logger.debug("NEO4J workCount {}", dataGenerator.getWorkCountCypher());
        logger.debug("NEO4J workTypeCount {}", dataGenerator.getWorkTypeCountCypher());
    }
}

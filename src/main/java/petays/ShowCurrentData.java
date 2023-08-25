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
        logger.debug("customerCount {} lastCustomerId {}", customerCount, lastCustomerId);
        final var invoiceCount = dataGenerator.getInvoiceCount();
        final var lastInvoiceId = dataGenerator.getLastInvoiceId();
        logger.debug("invoiceCount {} lastInvoiceId {}", invoiceCount, lastInvoiceId);
        final var itemCount = dataGenerator.getItemCount();
        final var lastItemId = dataGenerator.getLastItemId();
        logger.debug("itemCount {} lastItemId {}", itemCount, lastItemId);
        final var workCount = dataGenerator.getWorkCount();
        final var lastWorkId = dataGenerator.getLastWorkId();
        logger.debug("workCount {} lastWorkId {}", workCount, lastWorkId);
        final var workTypeCount = dataGenerator.getWorkTypeCount();
        final var lastWorkTypeId = dataGenerator.getLastWorkTypeId();
        logger.debug("workTypeCount {} lastWorkTypeId {}", workTypeCount, lastWorkTypeId);
    }
}

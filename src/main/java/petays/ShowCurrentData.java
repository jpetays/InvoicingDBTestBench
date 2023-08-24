package petays;

import homebeach.DataGenerator;

import java.sql.SQLException;

public class ShowCurrentData {
    public static void main(final String[] args) throws SQLException {

        final DataGenerator dataGenerator = new DataGenerator(Config.sql_databases, Config.neo4j_settings, Config.db_mariadb_url);

        System.out.println("Get row counts");

        final var customerCount = dataGenerator.getCustomerCount();
        final var lastCustomerId = dataGenerator.getLastCustomerId();
        System.out.println("customerCount " + customerCount + " lastCustomerId " + lastCustomerId);
        final var invoiceCount = dataGenerator.getInvoiceCount();
        final var lastInvoiceId = dataGenerator.getLastInvoiceId();
        System.out.println("invoiceCount " + invoiceCount + " lastInvoiceId " + lastInvoiceId);
        final var itemCount = dataGenerator.getItemCount();
        final var lastItemId = dataGenerator.getLastItemId();
        System.out.println("itemCount " + itemCount + " lastItemId " + lastItemId);
        final var workCount = dataGenerator.getWorkCount();
        final var lastWorkId = dataGenerator.getLastWorkId();
        System.out.println("workCount " + workCount + " lastWorkId " + lastWorkId);
        final var workTypeCount = dataGenerator.getWorkTypeCount();
        final var lastWorkTypeId = dataGenerator.getLastWorkTypeId();
        System.out.println("workTypeCount " + workTypeCount + " lastWorkTypeId " + lastWorkTypeId);
    }
}

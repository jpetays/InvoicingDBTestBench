package petays;

public abstract class Param {
    public static int threadCount;
    public static int batchExecuteValue;
    public static int iterationsPerThread;

    public static int itemCount;
    public static int workTypeCount;

    public static int workTypeFactor;
    public static int itemFactor;

    public static int invoiceFactor;
    public static int sequentialInvoices_0;
    public static int sequentialInvoices_100;
    public static int sequentialInvoices_1000;
    public static int targetFactor;
    public static int workFactor;

    public static int iterations;
    public static boolean showAll;

    static {
        threadCount = 10;
        batchExecuteValue = 10;
        iterationsPerThread = 1000;

        itemCount = 10000;
        workTypeCount = 10000;

        workTypeFactor = 10;
        itemFactor = 10;

        invoiceFactor = 10;
        sequentialInvoices_0 = 0;
        sequentialInvoices_100 = 100;
        sequentialInvoices_1000 = 1000;
        targetFactor = 10;
        workFactor = 10;

        iterations = 12;
        showAll = true;
    }
}

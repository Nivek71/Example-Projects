package nivek71.api.utility;

import nivek71.api.utility.functions.RunnableEx;
import nivek71.api.utility.functions.SupplierEx;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.time.Instant;
import java.util.function.Supplier;

public class Logger {
    private static final byte MAX_RECURSIVE_TRIES = 3;
    public static File ERROR_DIRECTORY = new File("errors");
    private static byte recursiveTries = 0;

    public static <T> T fetchOrLog(SupplierEx<T> supplierEx, Supplier<T> fallback) {
        try {
            return supplierEx.getEx();
        } catch (Exception ex) {
            logException(ex);
            return fallback.get();
        }
    }

    public static <T> T fetchOrLog(SupplierEx<T> supplierEx) {
        return fetchOrLog(supplierEx, () -> null);
    }

    public static boolean tryOrLog(RunnableEx runnableEx) {
        return fetchOrLog(() -> {
            runnableEx.runEx();
            return true;
        }, () -> false);
    }

    private static File getErrorFile(String name) throws IOException {
        File file;
        // / \ : * ? " < > |
        name = name.replace('\\', '~').replace('/', '-').replace(':', '~').replace('*', '~').
                replace('?', '~').replace('\"', '~').replace('<', '~').replace('>', '~')
                .replace('|', '~');
        int index = 1;
        do {
            file = new File(ERROR_DIRECTORY + File.separator + name + "#" + (index++) + ".txt");
        } while (file.exists());
        file.getParentFile().mkdirs();
        file.createNewFile();
        return file;
    }

    /**
     * File name is based on the error. If multiple errors happen within the same second, sub-indexes will be used to
     * mark different errors. If the file name is too long, the second at which the error occurred will be used instead.
     *
     * @return a unique filename within the {@link Logger#ERROR_DIRECTORY}
     */
    private static File getErrorFile(Throwable throwable) throws IOException {
        if (throwable.getMessage() != null) {
            try {
                // create file based on error message; may result in path being too long, in which case return
                // file based on time
                return getErrorFile(throwable.getMessage());
            } catch (IOException ignored) {
            }
        }
        return getErrorFile("err-" + (Instant.now().getEpochSecond()));
    }

    /**
     * Writes error header data, which includes the plugin version and time of error.
     *
     * @param writer the writer to use to give data
     * @param plugin the plugin responsible for the error
     * @throws IOException if the given writer throws an exception
     */
    private static void writeErrorData(BufferedWriter writer, Plugin plugin) throws IOException {
        writer.write('{');
        writer.write("\n\tPLUGIN: " + (plugin == null ? "UNKNOWN" : plugin.getDescription().getName()));
        writer.write("\n\tVERSION: " + (plugin == null ? "UNKNOWN" : plugin.getDescription().getVersion()));
        writer.write("\n\tTIME: " + Date.from(Instant.now()).toString());
//        writer.write("\n\tAdditional Logs: " + ""); // log additional data?
        writer.write("\n}\n");
    }

    public static void logException(Throwable throwable, Level level, Plugin plugin) {
        throwable.printStackTrace(level.getPrintStream());

        try {
            File file = getErrorFile(throwable);
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
                writeErrorData(writer, plugin);
                throwable.printStackTrace(new PrintWriter(writer));
            }
        } catch (Exception ex) {
            if (++recursiveTries >= MAX_RECURSIVE_TRIES) { // tried to log exception 3 times; another exception thrown each time
                Level.WARNING.log("Failed to log exception to file; tried " + MAX_RECURSIVE_TRIES + " times but failed each time.");
            } else Logger.logException(ex);
        } finally {
            recursiveTries = 0;
        }
    }

    /**
     * Logs the given exception to both the level output stream and an error file
     *
     * @param throwable the exception to log
     * @param level     the level to log the exception as
     * @see Logger#logException(Throwable)
     */
    public static void logException(Throwable throwable, Level level) {
        logException(throwable, level, getCallingPlugin());
    }

    /**
     * Logs the given exception to both the level output stream and an error file.
     * <p>
     * Exceptions are logged as {@link Level#ERROR}.
     *
     * @param throwable the exception to log
     * @see Logger#logException(Throwable, Level)
     */
    public static void logException(Throwable throwable) {
        logException(throwable, Level.ERROR, getCallingPlugin());
    }

    private static JavaPlugin getCallingPlugin() {
        try {
            return JavaPlugin.getProvidingPlugin(Class.forName(Thread.currentThread().getStackTrace()[1].getClassName()));
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public enum Level {
        ERROR(System.err),
        WARNING(System.out),
        INFO(System.out),
        TRIVIAL_NOTE(System.out);

        private final PrintStream printStream;

        Level(PrintStream printStream) {
            this.printStream = printStream;
        }

        public PrintStream getPrintStream() {
            return printStream;
        }

        /**
         * Loggers the given message.
         *
         * @param message the message to log
         */
        public void log(String message) {
            printStream.println(message);
        }
    }
}

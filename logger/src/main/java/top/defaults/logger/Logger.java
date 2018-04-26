package top.defaults.logger;

import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@SuppressWarnings({"WeakerAccess", "unused"})
public final class Logger {

    private static final String DEFAULT_TAG = "TopDefaultsLogger";
    private static String tagPrefix = DEFAULT_TAG;
    private static int level = Log.VERBOSE;
    private static String logFilePath = null;
    private static int logFileSizeInMegabytes = 2;
    private static final String siblingLogFileSuffix = "_1";
    private static boolean isLoggingToSibling = false;

    /**
     * Set the log level, which reuse the definition of {@link Log}, {@link Log#VERBOSE},
     * {@link Log#DEBUG}, etc.
     *
     * @param level The log level
     */
    public static void setLevel(int level) {
        Logger.level = level;
    }

    /**
     * Get the log level, which reuse the definition of {@link Log}, {@link Log#VERBOSE},
     * {@link Log#DEBUG}, etc.
     *
     * @return The log level
     */
    public static int getLevel() {
        return level;
    }

    /**
     * Set the prefix of log tag, the real log tag will be determined at runtime, which
     * will contain the file and line info.
     *
     * @param tagPrefix The prefix of log tag
     */
    public static void setTagPrefix(String tagPrefix) {
        Logger.tagPrefix = tagPrefix;
    }

    /**
     * Set the path for log file，we will keep at most two log files (one with the suffix "_1")
     * and the file size will be limited at {@link #setLogFileMaxSizeInMegabytes(int)}, if one
     * file exceed the limit, the following logs will be written to the other one.
     *
     * Default value is null, which means the logs will not be saved to file.
     *
     * Caveat: Please make sure you have the WRITE_EXTERNAL_STORAGE permission when needed.
     * 
     * @param filePath Path for log file
     */
    public static void setLogFile(String filePath) {
        logFilePath = filePath;
    }

    /**
     * The maximum size in megabytes a log file can be.
     *
     * @param sizeInMegabytes The size of log file in megabytes
     */
    public static void setLogFileMaxSizeInMegabytes(int sizeInMegabytes) {
        logFileSizeInMegabytes = sizeInMegabytes;
    }

    public static void v(String message) {
        v(realTag(), message);
    }

    public static void d(String message) {
        d(realTag(), message);
    }

    public static void i(String message) {
        i(realTag(), message);
    }

    public static void w(String message) {
        w(realTag(), message);
    }

    public static void e(String message) {
        e(realTag(), message);
    }

    public static void wtf(String message) {
        wtf(realTag(), message);
    }

    public static void v(String tag, String message) {
        if (level > Log.VERBOSE && !Log.isLoggable(Logger.tagPrefix, Log.DEBUG))
            return;
        Log.v(tag, message);
        writeLogFile(tag + "\t" + message);
    }

    public static void d(String tag, String message) {
        if (level > Log.DEBUG && !Log.isLoggable(Logger.tagPrefix, Log.DEBUG))
            return;
        Log.d(tag, message);
        writeLogFile(tag + "\t" + message);
    }

    public static void i(String tag, String message) {
        if (level > Log.INFO && !Log.isLoggable(Logger.tagPrefix, Log.DEBUG))
            return;
        Log.i(tag, message);
        writeLogFile(tag + "\t" + message);
    }

    public static void w(String tag, String message) {
        if (level > Log.WARN && !Log.isLoggable(Logger.tagPrefix, Log.DEBUG))
            return;
        Log.w(tag, message);
        writeLogFile(tag + "\t" + message);
    }

    public static void e(String tag, String message) {
        if (level > Log.ERROR && !Log.isLoggable(Logger.tagPrefix, Log.DEBUG))
            return;
        Log.e(tag, message);
        writeLogFile(tag + "\t" + message);
    }

    public static void wtf(String tag, String message) {
        if (level > Log.ASSERT && !Log.isLoggable(Logger.tagPrefix, Log.DEBUG))
            return;
        Log.wtf(tag, message);
        writeLogFile(tag + "\t" + message);
    }

    public static void logThreadStart() {
        d(realTag(), ">>>>>>>> " + Thread.currentThread().getClass() + " start running >>>>>>>>");
    }

    public static void logThreadFinish() {
        d(realTag(), "<<<<<<<< " + Thread.currentThread().getClass() + " finished running <<<<<<<<");
    }

    private static void writeLogFile(String message) {
        if (logFilePath != null) {
            String fileName = logFilePath + (isLoggingToSibling ? siblingLogFileSuffix : "");
            File currentLogFile = new File(fileName);
            if (currentLogFile.length() >= logFileSizeInMegabytes * 1024 * 1024) {
                isLoggingToSibling = !isLoggingToSibling;
                
                // delete file before write
                try {
                    PrintWriter writer = new PrintWriter(fileName);
                    writer.print("");
                    writer.close();
                } catch (FileNotFoundException ignored) {}
            }
            try {
                BufferedWriter logOut = new BufferedWriter(new FileWriter(fileName, true));
                SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                String strDate = sdfDate.format(new Date());
                logOut.append(strDate).append("\t").append(message).append("\n");
                logOut.close();
            } catch (IOException ignored) {}
        }
    }

    private static String realTag() {
        return tagPrefix + "|" + getLineInfo();
    }
    
    /**
     * The magic number 5 is determined because of the stack trace:
     *
     * <pre> StackTraceElement[]:
     * 0 = {java.lang.StackTraceElement@5260} "dalvik.system.VMStack.getThreadStackTrace(Native Method)"
     * 1 = {java.lang.StackTraceElement@5261} "java.lang.Thread.getStackTrace(Thread.java:1556)"
     * 2 = {java.lang.StackTraceElement@5262} "top.defaults.logger.Logger.getLineInfo(Logger.java:164)"
     * 3 = {java.lang.StackTraceElement@5262} "top.defaults.logger.Logger.realTag(Logger.java:166)"
     * 4 = {java.lang.StackTraceElement@5263} "top.defaults.logger.Logger.d(Logger.java:60)"
     * 5 = {java.lang.StackTraceElement@5264} ... // the caller
     * </pre>
     *
     * So the real call site's StackTraceElement is stackTraceElement[4], then we use this stackTraceElement
     * to retrieve the line info.
     *
     * @return The line info which will be used as log tag, for example:
     */
    private static String getLineInfo() {
        StackTraceElement[] stackTraceElement = Thread.currentThread().getStackTrace();
        String fileName = stackTraceElement[5].getFileName();
        int lineNumber = stackTraceElement[5].getLineNumber();
        return ".(" + fileName + ":" + lineNumber + ")";
    }
}

package top.defaults.logger;

import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

@SuppressWarnings({"WeakerAccess", "unused"})
public final class Logger {

    private static final String DEFAULT_TAG = "TopDefaultsLogger";
    private static String tagPrefix = DEFAULT_TAG;
    private static int level = Log.VERBOSE;
    private static String logFilePath;
    private static int logFileSizeInMegabytes = 2;
    private static final String prevLogFileSuffix = "-prev";
    private static BufferedWriter logWriter;
    private static ExecutorService executorService;
    private static Timer timer;
    private static TimerTask scheduledFlushTask;
    private static TimerTask scheduledCloseTask;

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
     * Set the path for log file，we will keep at most two log files (one with the suffix "-prev")
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

        if (filePath == null) {
            executorService.shutdown();
            timer.cancel();
            return;
        }

        if (executorService == null) {
            executorService = Executors.newSingleThreadExecutor();
        }

        if (timer == null) {
            timer = new Timer();
        }
    }

    /**
     * The maximum size in megabytes a log file can be.
     *
     * @param sizeInMegabytes The size of log file in megabytes
     */
    public static void setLogFileMaxSizeInMegabytes(int sizeInMegabytes) {
        logFileSizeInMegabytes = sizeInMegabytes;
    }

    public static void v(String message, Object... args) {
        vWithTag(realTag(), message, args);
    }

    public static void d(String message, Object... args) {
        dWithTag(realTag(), message, args);
    }

    public static void i(String message, Object... args) {
        iWithTag(realTag(), message, args);
    }

    public static void w(String message, Object... args) {
        wWithTag(realTag(), message, args);
    }

    public static void e(String message, Object... args) {
        eWithTag(realTag(), message, args);
    }

    public static void wtf(String message, Object... args) {
        wtfWithTag(realTag(), message, args);
    }

    public static void vWithTag(String tag, String message, Object... args) {
        log(Log.VERBOSE, tag, message, args);
    }

    public static void dWithTag(String tag, String message, Object... args) {
        log(Log.DEBUG, tag, message, args);
    }

    public static void iWithTag(String tag, String message, Object... args) {
        log(Log.INFO, tag, message, args);
    }

    public static void wWithTag(String tag, String message, Object... args) {
        log(Log.WARN, tag, message, args);
    }

    public static void eWithTag(String tag, String message, Object... args) {
        log(Log.ERROR, tag, message, args);
    }

    public static void wtfWithTag(String tag, String message, Object... args) {
        log(Log.ASSERT, tag, message, args);
    }

    public static void logThreadStart() {
        dWithTag(realTag(), ">>>>>>>> " + Thread.currentThread().getClass() + " start running >>>>>>>>");
    }

    public static void logThreadFinish() {
        dWithTag(realTag(), "<<<<<<<< " + Thread.currentThread().getClass() + " finished running <<<<<<<<");
    }

    private static void log(int priority, String tag, String message, Object... args) {
        if (level > priority && !Log.isLoggable(Logger.tagPrefix, Log.DEBUG))
            return;

        message = formatMessage(message, args);
        if (priority == Log.ASSERT) {
            Log.wtf(tag, message);
        } else {
            Log.println(priority, tag, message);
        }

        try {
            writeLogFile(priorityAbbr(priority) + "/" + tag + "\t" + message);
        } catch (Exception ignored) {} // fail silent
    }

    private static SparseArray<String> PRIORITY_MAP = new SparseArray<>();

    static {
        PRIORITY_MAP.append(Log.VERBOSE, "V");
        PRIORITY_MAP.append(Log.DEBUG, "D");
        PRIORITY_MAP.append(Log.INFO, "I");
        PRIORITY_MAP.append(Log.WARN, "W");
        PRIORITY_MAP.append(Log.ERROR, "E");
        PRIORITY_MAP.append(Log.ASSERT, "X");
    }

    private static String priorityAbbr(int priority) {
        return PRIORITY_MAP.get(priority);
    }

    /**
     * A convenient method which can be used to adapt to <a href="https://github.com/JakeWharton/timber">Timber</a>.
     *
     * @param priority The priority of the log
     * @param customTag The custom tag of the log, the real log tag will be determined at runtime, which
     *                 will contain the file and line info
     * @param message The log message
     */
    public static void logWithTimber(int priority, String customTag, String message) {
        String tag = realTimberTag(customTag);
        switch (priority) {
            case Log.VERBOSE:
                vWithTag(tag, message);
                break;
            case Log.DEBUG:
                dWithTag(tag, message);
                break;
            case Log.INFO:
                iWithTag(tag, message);
                break;
            case Log.WARN:
                wWithTag(tag, message);
                break;
            case Log.ERROR:
                eWithTag(tag, message);
                break;
            case Log.ASSERT:
                wtfWithTag(tag, message);
                break;
            default:
                break;
        }
    }

    private static String formatMessage(String message, Object[] args) {
        if (args != null && args.length > 0) {
            message = String.format(message, args);
        }
        return message;
    }

    private static void writeLogFile(final String message) {
        if (logFilePath != null) {
            SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);
            final String strDate = sdfDate.format(new Date());

            String line = strDate + " " + message;
            executorService.submit(new LogWriterRunnable(line));

            if (scheduledFlushTask != null) {
                scheduledFlushTask.cancel();
            }
            scheduledFlushTask = new TimerTask() {
                @Override
                public void run() {
                    FutureTask<Void> flushTask = new FutureTask<>(new Callable<Void>() {
                        @Override
                        public Void call() throws Exception {
                            logWriter.flush();
                            return null;
                        }
                    });
                    executorService.submit(flushTask);
                }
            };
            timer.schedule(scheduledFlushTask, 1000);

            if (scheduledCloseTask != null) {
                scheduledCloseTask.cancel();
            }
            scheduledCloseTask = new TimerTask() {
                @Override
                public void run() {
                    FutureTask<Void> closeTask = new FutureTask<>(new Callable<Void>() {
                        @Override
                        public Void call() throws Exception {
                            logWriter.close();
                            logWriter = null;
                            return null;
                        }
                    });
                    executorService.submit(closeTask);
                }
            };
            timer.schedule(scheduledCloseTask, 1000 * 60);
        }
    }

    private static class LogWriterRunnable implements Runnable {

        private final String line;

        LogWriterRunnable(String line) {
            this.line = line;
        }

        @Override
        public void run() {
            try {
                if (logWriter == null) {
                    logWriter = new BufferedWriter(new FileWriter(logFilePath, true));
                }
                logWriter.append(line);
                logWriter.newLine();
            } catch (IOException ignored) {}

            File currentFile = new File(logFilePath);
            if (currentFile.length() >= logFileSizeInMegabytes * 1024 * 1024) {
                // delete previous log file and change current log file to prev
                File prevFile = new File(logFilePath + prevLogFileSuffix);

                //noinspection ResultOfMethodCallIgnored
                currentFile.renameTo(prevFile);
                try {
                    logWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                logWriter = null;
            }
        }
    }

    private static String realTag() {
        return tagPrefix + "|" + getLineInfo();
    }

    private static String realTimberTag(String timberTag) {
        return (TextUtils.isEmpty(timberTag) ? tagPrefix : timberTag) + "|" + getLineInfoBypassTimber();
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

    private static String getLineInfoBypassTimber() {
        StackTraceElement[] stackTraceElement = Thread.currentThread().getStackTrace();
        int offset = getStackOffsetBypassTimber(stackTraceElement);
        if (offset < 0) return "";
        String fileName = stackTraceElement[offset].getFileName();
        int lineNumber = stackTraceElement[offset].getLineNumber();
        return ".(" + fileName + ":" + lineNumber + ")";
    }

    private static int getStackOffsetBypassTimber(StackTraceElement[] stackTraceElements) {
        for (int i = 6; i < stackTraceElements.length; i++) {
            StackTraceElement e = stackTraceElements[i];
            String name = e.getClassName();
            if (!name.startsWith("timber.log.Timber")) {
                return i;
            }
        }
        return -1;
    }
}

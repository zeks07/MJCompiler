package rs.ac.bg.etf.pp1.logger;

import rs.ac.bg.etf.pp1.util.Context;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;


public class CompilerLogger {

    private static final Context.Key<CompilerLogger> loggerKey = new Context.Key<>();

    private final List<String> messages = new ArrayList<>();

    public static CompilerLogger getInstance(Context context) {
        CompilerLogger logger = context.get(loggerKey);
        if (logger == null) {
            logger = new CompilerLogger(context);
        }
        return logger;
    }
    private CompilerLogger(Context context) {
        context.put(loggerKey, this);
    }

    public void log(LogLevel level, String message, int line) {
        String format = "[" + level + "]" + (line >= 0 ? " at line " + line : "") + ": " + message;

        messages.add(format);
        System.err.println(format);
    }

    public void info(String message, int line) {
        log(LogLevel.INFO, message, line);
    }

    public void warning(String message, int line) {
        log(LogLevel.WARNING, message, line);
    }

    public void error(String message, int line) {
        log(LogLevel.ERROR, message, line);
    }

    public void error(String message) {
        log(LogLevel.ERROR, message, -1);
    }

    public void writeToFile(Path file) throws IOException {
        Files.write(file, messages);
    }
}

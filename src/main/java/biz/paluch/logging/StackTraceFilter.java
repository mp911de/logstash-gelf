package biz.paluch.logging;

import static biz.paluch.logging.RuntimeContainerProperties.getProperty;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

import biz.paluch.logging.gelf.intern.Closer;

/**
 * Filtering Facility for stack traces. This is to shorten very long Traces. It creates a redacted trace containing only the
 * interesting parts. Please provide an own Resource {@code /StackTraceFilter.packages} with the package prefixes if you want to
 * use a custom filter (one package per line).
 *
 * <code>
 # Packages to filter
 org.h2
 org.apache.catalina
 org.apache.coyote
 org.apache.tomcat
 com.arjuna
 org.apache.cxf
 * </code>
 *
 * Original stack trace: <code>java.lang.RuntimeException: entryMethod
	at biz.paluch.logging.StackTraceFilterTest.entryMethod(StackTraceFilterTest.java:49)
	at biz.paluch.logging.StackTraceFilterTest.printStackTrace(StackTraceFilterTest.java:35)
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.lang.reflect.Method.invoke(Method.java:497)
	at org.junit.runners.model.FrameworkMethod$1.runReflectiveCall(FrameworkMethod.java:47)
	at org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:12)
	at org.junit.runners.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java:44)
	at org.junit.internal.runners.statements.InvokeMethod.evaluate(InvokeMethod.java:17)
	at org.junit.internal.runners.statements.RunBefores.evaluate(RunBefores.java:26)
	at org.junit.runners.ParentRunner.runLeaf(ParentRunner.java:271)
	at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:70)
	at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:50)
	at org.junit.runners.ParentRunner$3.run(ParentRunner.java:238)
	at org.junit.runners.ParentRunner$1.schedule(ParentRunner.java:63)
	at org.junit.runners.ParentRunner.runChildren(ParentRunner.java:236)
	at org.junit.runners.ParentRunner.access$000(ParentRunner.java:53)
	at org.junit.runners.ParentRunner$2.evaluate(ParentRunner.java:229)
	at org.junit.runners.ParentRunner.run(ParentRunner.java:309)
	at org.junit.runner.JUnitCore.run(JUnitCore.java:160)
	at com.intellij.junit4.JUnit4IdeaTestRunner.startRunnerWithArgs(JUnit4IdeaTestRunner.java:68)
	at com.intellij.rt.execution.junit.IdeaTestRunner$Repeater.startRunnerWithArgs(IdeaTestRunner.java:51)
	at com.intellij.rt.execution.junit.JUnitStarter.prepareStreamsAndStart(JUnitStarter.java:237)
	at com.intellij.rt.execution.junit.JUnitStarter.main(JUnitStarter.java:70)
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.lang.reflect.Method.invoke(Method.java:497)
	at com.intellij.rt.execution.application.AppMain.main(AppMain.java:147)
Caused by: biz.paluch.logging.StackTraceFilterTest$MyException: Intermediate 2
	at biz.paluch.logging.StackTraceFilterTest.intermediate2(StackTraceFilterTest.java:58)
	at biz.paluch.logging.StackTraceFilterTest.intermediate1(StackTraceFilterTest.java:53)
	... 30 more
	Suppressed: java.lang.RuntimeException: surpressed
		at biz.paluch.logging.StackTraceFilterTest.intermediate2(StackTraceFilterTest.java:59)
		... 31 more
		Suppressed: java.lang.RuntimeException: surpressed
			at biz.paluch.logging.StackTraceFilterTest.intermediate2(StackTraceFilterTest.java:60)
			... 31 more
		Suppressed: java.lang.IllegalArgumentException: Failed to parse byte.
			at org.jboss.common.beans.property.ByteEditor.setAsText(ByteEditor.java:48)
			at biz.paluch.logging.StackTraceFilterTest.intermediate2(StackTraceFilterTest.java:65)
			... 31 more
		Caused by: java.lang.NumberFormatException: For input string: "adsd"
			at java.lang.NumberFormatException.forInputString(NumberFormatException.java:65)
			at java.lang.Integer.parseInt(Integer.java:580)
			at java.lang.Integer.valueOf(Integer.java:740)
			at java.lang.Integer.decode(Integer.java:1197)
			at java.lang.Byte.decode(Byte.java:277)
			at org.jboss.common.beans.property.ByteEditor.setAsText(ByteEditor.java:45)
			... 32 more
Caused by: biz.paluch.logging.StackTraceFilterTest$MyException: Message
	at biz.paluch.logging.StackTraceFilterTest.intermediate3(StackTraceFilterTest.java:77)
	... 32 more
	Suppressed: java.lang.IllegalStateException: Some illegal state
		at biz.paluch.logging.StackTraceFilterTest.cause(StackTraceFilterTest.java:84)
		at biz.paluch.logging.StackTraceFilterTest.intermediate3(StackTraceFilterTest.java:78)
		... 32 more
	Suppressed: java.lang.IllegalStateException: Some illegal state
		at biz.paluch.logging.StackTraceFilterTest.cause(StackTraceFilterTest.java:84)
		at biz.paluch.logging.StackTraceFilterTest.intermediate3(StackTraceFilterTest.java:79)
		... 32 more
Caused by: java.lang.IllegalStateException: Some illegal state
	at biz.paluch.logging.StackTraceFilterTest.cause(StackTraceFilterTest.java:84)
	... 33 more
    </code>
 *
 * Filtered stack trace: <code>java.lang.RuntimeException: entryMethod
	at biz.paluch.logging.StackTraceFilterTest.entryMethod(StackTraceFilterTest.java:49)
	at biz.paluch.logging.StackTraceFilterTest.filterWholeStackTrace(StackTraceFilterTest.java:43)
			19 lines skipped for [java.lang, sun., org.junit]
	at com.intellij.junit4.JUnit4IdeaTestRunner.startRunnerWithArgs(JUnit4IdeaTestRunner.java:68)
	at com.intellij.rt.execution.junit.IdeaTestRunner$Repeater.startRunnerWithArgs(IdeaTestRunner.java:51)
	at com.intellij.rt.execution.junit.JUnitStarter.prepareStreamsAndStart(JUnitStarter.java:237)
	at com.intellij.rt.execution.junit.JUnitStarter.main(JUnitStarter.java:70)
			4 lines skipped for [java.lang, sun.]
	at com.intellij.rt.execution.application.AppMain.main(AppMain.java:147)
Caused by: biz.paluch.logging.StackTraceFilterTest$MyException: Intermediate 2
	at biz.paluch.logging.StackTraceFilterTest.intermediate2(StackTraceFilterTest.java:58)
	at biz.paluch.logging.StackTraceFilterTest.intermediate1(StackTraceFilterTest.java:53)
	... 30 more
	Suppressed: java.lang.RuntimeException: surpressed
		at biz.paluch.logging.StackTraceFilterTest.intermediate2(StackTraceFilterTest.java:59)
		... 31 more
		Suppressed: java.lang.RuntimeException: surpressed
			at biz.paluch.logging.StackTraceFilterTest.intermediate2(StackTraceFilterTest.java:60)
			... 31 more
		Suppressed: java.lang.IllegalArgumentException: Failed to parse byte.
			at org.jboss.common.beans.property.ByteEditor.setAsText(ByteEditor.java:48)
			at biz.paluch.logging.StackTraceFilterTest.intermediate2(StackTraceFilterTest.java:65)
			... 31 more
		Caused by: java.lang.NumberFormatException: For input string: "adsd"
					6 lines skipped for [java.lang, org.jboss]
			... 32 more
Caused by: biz.paluch.logging.StackTraceFilterTest$MyException: Message
	at biz.paluch.logging.StackTraceFilterTest.intermediate3(StackTraceFilterTest.java:77)
	... 32 more
	Suppressed: java.lang.IllegalStateException: Some illegal state
		at biz.paluch.logging.StackTraceFilterTest.cause(StackTraceFilterTest.java:84)
		at biz.paluch.logging.StackTraceFilterTest.intermediate3(StackTraceFilterTest.java:78)
		... 32 more
	Suppressed: java.lang.IllegalStateException: Some illegal state
		at biz.paluch.logging.StackTraceFilterTest.cause(StackTraceFilterTest.java:84)
		at biz.paluch.logging.StackTraceFilterTest.intermediate3(StackTraceFilterTest.java:79)
		... 32 more
Caused by: java.lang.IllegalStateException: Some illegal state
	at biz.paluch.logging.StackTraceFilterTest.cause(StackTraceFilterTest.java:84)
	... 33 more</code>
 *
 * @author Mark Paluch
 */
public class StackTraceFilter {

    public static final String VERBOSE_LOGGING_PROPERTY = "logstash-gelf.StackTraceFilter.verbose";
    public static final String FILTER_SETTINGS = "/" + StackTraceFilter.class.getSimpleName() + ".packages";

    private static final String INDENT = "\t";
    private static final boolean VERBOSE_LOGGING = Boolean.parseBoolean(getProperty(VERBOSE_LOGGING_PROPERTY, "false"));

    private static final Pattern AT_PATTERN = Pattern.compile("(" + INDENT + ")+at");
    private static final Pattern SURPRESSED_PATTERN = Pattern.compile("(" + INDENT + ")+Suppressed\\:");

    /**
     * List of suppressed Packages.
     */
    private static Set<String> suppressedPackages;

    static {
        loadSetttings(FILTER_SETTINGS);
    }

    public static void loadSetttings(String resourceName) {
        InputStream is = null;
        try {
            is = getStream(resourceName);
            if (is == null) {
                verboseLog("No " + resourceName + " resource present, using defaults");
                suppressedPackages = new HashSet<String>(getDefaults());
            } else {
                Properties p = new Properties();
                p.load(is);
                suppressedPackages = (Set) p.keySet();
            }

        } catch (IOException e) {
            verboseLog("Could not parse " + resourceName + " resource, using defaults");
            suppressedPackages = new HashSet<String>(getDefaults());
        } finally {
            Closer.close(is);
        }
    }

    private static InputStream getStream(String resourceName) {

        Thread thread = Thread.currentThread();
        InputStream is = StackTraceFilter.class.getResourceAsStream(resourceName);
        if (is == null && thread.getContextClassLoader() != null) {
            is = thread.getContextClassLoader().getResourceAsStream(resourceName);
        }
        return is;
    }

    public static List<String> getDefaults() {

        return Arrays.asList("org.h2", "org.apache.catalina", "org.apache.coyote", "org.apache.tomcat", "com.arjuna",
                "org.apache.cxf", "org.hibernate", "org.junit", "org.jboss", "java.lang.reflect.Method", "sun.", "com.sun",
                "org.eclipse", "junit.framework", "com.sun.faces", "javax.faces", "org.richfaces", "org.apache.el",
                "javax.servlet");
    }

    /**
     * Utility-Constructor.
     */
    private StackTraceFilter() {
    }

    /**
     * Get a filterered stack trace.
     *
     * @param t the throwable
     * @return String containing the filtered stack trace.
     */
    public static String getFilteredStackTrace(Throwable t) {
        return getFilteredStackTrace(t, true);
    }

    /**
     * Get a filterered stack trace.
     *
     * @param t the throwable
     * @param shouldFilter true in case filtering should be performed. Else stack trace as string will be returned.
     * @return String containing the stack trace.
     * @deprecated since 1.11.1. Use {@link #getStackTrace(Throwable, int)} to get the stack trace without filtering or
     *             {@link #getFilteredStackTrace(Throwable)} to get the filtered the stack trace.
     */
    @Deprecated
    public static String getFilteredStackTrace(Throwable t, boolean shouldFilter) {

        if (shouldFilter) {
            return getFilteredStackTrace(t, 0);
        }

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        pw.close();

        return sw.getBuffer().toString();
    }

    /**
     * Filter stack trace by selecting the {@link Throwable} using a reference position. Intermediate throwables will be printed
     * with just their header.
     *
     * @param t the throwable
     * @param ref throwable reference position, see {@link #getThrowable(List, int)}.
     * @return String containing the stack trace.
     * @since 1.11.1
     */
    public static String getFilteredStackTrace(Throwable t, int ref) {

        StringWriter sw = new StringWriter();
        FilterWriter filterWriter = new StackTraceFilterWriter(sw);

        printStackTrace(t, filterWriter, ref);

        return sw.getBuffer().toString();
    }

    /**
     * Get the {@link Throwable}'s stack trace.
     *
     * @param t the throwable
     * @return String containing the stack trace.
     * @since 1.11.1
     */
    public static String getStackTrace(Throwable t) {
        return getStackTrace(t, 0);
    }

    /**
     * Get the stack trace by selecting the {@link Throwable} using a reference position. Intermediate throwables will be
     * printed with just their header.
     *
     * @param t the throwable
     * @param ref throwable reference position, see {@link #getThrowable(List, int)}.
     * @return String containing the stack trace.
     * @since 1.11.1
     */
    public static String getStackTrace(Throwable t, int ref) {

        StringWriter sw = new StringWriter();
        printStackTrace(t, sw, ref);

        return sw.getBuffer().toString();
    }

    private static void printStackTrace(Throwable t, Writer writer, int ref) {

        List<Throwable> throwables = getThrowables(t);
        PrintWriter exceptionWriter = new PrintWriter(writer);
        Throwable subject = ref == 0 ? throwables.get(ref) : getThrowable(throwables, ref);
        boolean first = true;

        for (Throwable throwable : throwables) {

            if (subject == throwable) {
                break;
            }

            if (!first) {
                exceptionWriter.print("Caused by: ");
            }

            first = false;

            exceptionWriter.print(throwable.toString());
            exceptionWriter.println();
        }

        if (ref != 0) {
            exceptionWriter.print("Caused by: ");
        }

        subject.printStackTrace(exceptionWriter);
    }

    /**
     * Return a {@link Throwable} by its reference position.
     * <p>
     * This method extracts a {@link Throwable} from the exception chain of {@link Throwable#getCause() causes}. A reference of
     * {@code 0} returns the original {@link Throwable}, values greater zero will walk the cause chain up so a reference
     * {@code 1} returns {@code t.getCause()}. Negative reference values will walk the causing exception from the root cause
     * side. A reference of {@code -1} returns the root cause, {@code -2} the exception that wraps the root cause and so on.
     *
     * @param throwable the caught {@link Throwable}.
     * @param ref reference position
     * @return the selected {@link Throwable}.
     */
    public static Throwable getThrowable(Throwable throwable, int ref) {
        return getThrowable(getThrowables(throwable), ref);
    }

    private static Throwable getThrowable(List<Throwable> throwables, int ref) {

        if (ref >= 0) {
            return throwables.get(Math.min(ref, throwables.size() - 1));
        }

        return throwables.get(Math.max(throwables.size() + ref, 0));
    }

    private static List<Throwable> getThrowables(Throwable t) {

        List<Throwable> throwables = new ArrayList<Throwable>();

        Throwable current = t;

        do {
            throwables.add(current);
            current = current.getCause();
        } while (current != null && !throwables.contains(current));
        return throwables;
    }

    private static int getIndentation(String traceElement) {

        int index = traceElement.indexOf(INDENT);
        int indentationLevel = 0;

        while (index != -1) {
            indentationLevel++;
            index = traceElement.indexOf(INDENT, index + 1);
        }

        return indentationLevel;

    }

    // 37 lines skipped for [org.h2, org.hibernate, sun.,
    // java.lang.reflect.Method, $Proxy]
    private static String getSkippedPackagesMessage(Set<String> skippedPackages, int skippedLines, int indentationLevel) {

        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < 2 + indentationLevel; i++) {
            stringBuilder.append(INDENT);
        }

        stringBuilder.append(skippedLines).append(" line").append((skippedLines == 1 ? "" : "s")).append(" skipped for ")
                .append(skippedPackages);

        return stringBuilder.toString();
    }

    /**
     * Checks to see if the class is part of a forbidden package. If so, it returns the package name from the list of suppressed
     * packages that matches, otherwise it returns null.
     *
     * @param classAndMethod StackTraceElement
     * @return forbidden package name or null.
     */
    private static String tryGetForbiddenPackageName(String classAndMethod) {

        for (String pkg : suppressedPackages) {
            if (classAndMethod.startsWith(pkg)) {
                return pkg;
            }
        }
        return null;
    }

    private static void verboseLog(String message) {
        if (VERBOSE_LOGGING) {
            System.out.println(message);
        }
    }

    static class StackTraceFilterWriter extends FilterWriter {

        private final String traceElementPrefix = INDENT + "at ";
        Set<String> skippedPackages = new HashSet<String>();
        int skippedLines;
        boolean endsWithNewLine;
        boolean first = true;
        int indentationLevel;

        private final String lineSeparator = java.security.AccessController
                .doPrivileged(new sun.security.action.GetPropertyAction("line.separator"));

        public StackTraceFilterWriter(Writer s) {
            super(s);
        }

        @Override
        public void write(String str, int off, int len) throws IOException {

            String toWrite = str.substring(off, len);

            if (skippedLines > 0 && toWrite.equals(lineSeparator) && endsWithNewLine) {
                return;
            }

            if (toWrite.equals(lineSeparator)) {
                endsWithNewLine = true;
                super.write(str, off, len);
                return;
            }

            if (SURPRESSED_PATTERN.matcher(toWrite).find()) {
                first = true;
            }

            if (endsWithNewLine && AT_PATTERN.matcher(toWrite).find()) {

                String traceElement = getTraceElement(toWrite);

                String forbiddenPackageName = null;

                if (!first) {
                    forbiddenPackageName = tryGetForbiddenPackageName(traceElement);
                }

                first = false;

                if (forbiddenPackageName == null) {
                    afterFiltering();
                    super.write(str, off, len);
                } else {

                    indentationLevel = getIndentation(toWrite);
                    skippedLines++;
                    skippedPackages.add(forbiddenPackageName);
                }
                return;
            }

            afterFiltering();

            super.write(str, off, len);
            endsWithNewLine = str.equals(lineSeparator);
        }

        private void afterFiltering() throws IOException {

            if (!skippedPackages.isEmpty()) {
                // 37 lines skipped for [org.h2, org.hibernate, sun.,
                // java.lang.reflect.Method, $Proxy]
                String skippedPackagesMessage = getSkippedPackagesMessage(skippedPackages, skippedLines, indentationLevel);

                skippedPackages.clear();
                skippedLines = 0;
                write(skippedPackagesMessage);
                write(lineSeparator);

                // at hib.HibExample.test(HibExample.java:18)
                indentationLevel = 0;
            }
        }

        private String getTraceElement(String toWrite) {
            return toWrite.substring(toWrite.indexOf(traceElementPrefix) + traceElementPrefix.length());
        }

        @Override
        public void close() throws IOException {
            if (skippedLines > 0) {
                write(getSkippedPackagesMessage(skippedPackages, skippedLines, indentationLevel));
            }
            super.close();
        }
    }
}

package biz.paluch.logging;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

/**
 * Filtering Facility for Stack-Traces. This is to shorten very long Traces. It leads to a very short Trace containing only the
 * interesting parts. Please provide an own Resource /StackTraceFilter.packages with the packages you want to have filtered out
 * (one package per line)
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
 * Filtered Example: <code>
 Exception: javax.faces.FacesException: #{documentQueryController.executeQuery}: com.kaufland.dms.commons.exception.DmsTechnicalException: java.io.IOException: aargh
 Caused by: javax.faces.el.EvaluationException: com.kaufland.dms.commons.exception.DmsTechnicalException: java.io.IOException: aargh
 Caused by: com.kaufland.dms.commons.exception.DmsTechnicalException: java.io.IOException: aargh
 Caused by: java.io.IOException: aargh
 	at com.kaufland.dms.core.business.document.query.DocumentQueryBO.queryDocuments(DocumentQueryBO.java:76)
 		52 lines skipped for [sun., org.jboss, java.lang.reflect.Method]
 	at com.kaufland.dms.core.business.document.query.DocumentQueryBO$$$view47.queryDocuments(Unknown Source)
 	at com.kaufland.dms.core.facade.DocumentFacade.queryDocuments(DocumentFacade.java:169)
 		10 lines skipped for [sun., org.jboss, java.lang.reflect.Method]
 	at com.kaufland.dms.commons.exception.safe.ExceptionDecouplingInterceptor.interceptInvocation(ExceptionDecouplingInterceptor.java:31)
 		41 lines skipped for [sun., org.jboss, java.lang.reflect.Method]
 	at com.kaufland.dms.core.service.DocumentService$$$view37.queryDocuments(Unknown Source)
 	at com.kaufland.dms.web.ui.document.query.DocumentQueryController.executeQuery(DocumentQueryController.java:201)
 	at com.kaufland.dms.web.ui.document.query.DocumentQueryController.executeQuery(DocumentQueryController.java:196)
 		4 lines skipped for [sun., java.lang.reflect.Method]
 	at org.apache.el.parser.AstValue.invoke(AstValue.java:258)
 	at org.apache.el.MethodExpressionImpl.invoke(MethodExpressionImpl.java:278)
 		2 lines skipped for [org.jboss]
 	at com.sun.faces.facelets.el.TagMethodExpression.invoke(TagMethodExpression.java:105)
 	at javax.faces.component.MethodBindingMethodExpressionAdapter.invoke(MethodBindingMethodExpressionAdapter.java:87)
 	at com.sun.faces.application.ActionListenerImpl.processAction(ActionListenerImpl.java:101)
 	at javax.faces.component.UICommand.broadcast(UICommand.java:315)
 	at javax.faces.component.UIViewRoot.broadcastEvents(UIViewRoot.java:786)
 	at javax.faces.component.UIViewRoot.processApplication(UIViewRoot.java:1251)
 	at com.sun.faces.lifecycle.InvokeApplicationPhase.execute(InvokeApplicationPhase.java:81)
 	at com.sun.faces.lifecycle.Phase.doPhase(Phase.java:101)
 	at com.sun.faces.lifecycle.LifecycleImpl.execute(LifecycleImpl.java:118)
 	at javax.faces.webapp.FacesServlet.service(FacesServlet.java:593)
 		2 lines skipped for [org.apache.catalina]
 	at com.kaufland.dms.web.servlet.IE9CompatibilityFilter.doFilter(IE9CompatibilityFilter.java:26)
 		19 lines skipped for [org.apache.coyote, org.jboss, org.apache.catalina, org.apache.tomcat]
 	at java.lang.Thread.run(Thread.java:722)
    </code>
 * 
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class StackTraceFilter {

    private static final String INDENT = "\t";
    private static final String FILTER_SETTINGS = "/" + StackTraceFilter.class.getSimpleName() + ".packages";

    /**
     * List of Surpressed Packages.
     */
    private static Set<String> suppressedPackages;

    static {

        InputStream is = null;
        try {
            is = getStream();
            if (is == null) {
                System.out.println("No " + FILTER_SETTINGS + " resource present, using defaults");
                suppressedPackages = new HashSet<String>(getDefaults());
            } else {
                Properties p = new Properties();
                p.load(is);
                suppressedPackages = (Set) p.keySet();
            }

        } catch (IOException e) {
            System.out.println("Could not parse " + FILTER_SETTINGS + " resource, using defaults");
            suppressedPackages = new HashSet<String>(getDefaults());
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                // ignore
            }
        }

    }

    private static InputStream getStream() {

        Thread thread = Thread.currentThread();
        InputStream is = StackTraceFilter.class.getResourceAsStream(FILTER_SETTINGS);
        if (is == null && thread.getContextClassLoader() != null) {
            is = thread.getContextClassLoader().getResourceAsStream(FILTER_SETTINGS);
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
     * Filter Stack-Trace
     * 
     * @param t the throwable
     * @return String containing the filtered Stack-Trace.
     */
    public static String getFilteredStackTrace(Throwable t) {

        return getFilteredStackTrace(t, true);
    }

    /**
     * Filter Stack-Trace
     * 
     * @param t the throwable
     * @param shouldFilter true in case filtering should be performed. Else stack-trace as string will be returned.
     * @return String containing the Stack-Trace.
     */
    public static String getFilteredStackTrace(Throwable t, boolean shouldFilter) {

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        writeCleanStackTrace(t, pw, shouldFilter);
        return sw.getBuffer().toString();
    }

    private static void writeCleanStackTrace(Throwable t, PrintWriter s, boolean wantsFilter) {

        s.print("Exception: ");

        printExceptionChain(t, s);

        Set<String> skippedPackages = new HashSet<String>();
        int skippedLines = 0;
        boolean shouldFilter = wantsFilter;
        boolean first = true;

        for (StackTraceElement traceElement : getBottomThrowable(t).getStackTrace()) {
            String forbiddenPackageName = null;

            if (shouldFilter && !first) {
                forbiddenPackageName = tryGetForbiddenPackageName(traceElement);
            }

            first = false;

            if (forbiddenPackageName == null) {

                if (!skippedPackages.isEmpty()) {
                    // 37 lines skipped for [org.h2, org.hibernate, sun.,
                    // java.lang.reflect.Method, $Proxy]
                    s.println(getSkippedPackagesMessage(skippedPackages, skippedLines));
                }

                // at hib.HibExample.test(HibExample.java:18)
                s.println(INDENT + "at " + traceElement);
                skippedPackages.clear();
                skippedLines = 0;
            } else {
                skippedLines++;
                skippedPackages.add(forbiddenPackageName);
            }
        }

        if (skippedLines > 0) {
            s.println(getSkippedPackagesMessage(skippedPackages, skippedLines));
        }
    }

    // 37 lines skipped for [org.h2, org.hibernate, sun.,
    // java.lang.reflect.Method, $Proxy]
    private static String getSkippedPackagesMessage(Set<String> skippedPackages, int skippedLines) {

        return INDENT + INDENT + skippedLines + " line" + (skippedLines == 1 ? "" : "s") + " skipped for " + skippedPackages;
    }

    private static Throwable getBottomThrowable(Throwable t) {

        Throwable result = t;
        while (result.getCause() != null) {
            result = result.getCause();
        }
        return result;
    }

    private static void printExceptionChain(Throwable t, PrintWriter s) {

        s.println(t);
        if (t.getCause() != null) {
            s.print("Caused by: ");
            printExceptionChain(t.getCause(), s);
        }
    }

    /**
     * Checks to see if the class is part of a forbidden package. If so, it returns the package name from the list of suppressed
     * packages that matches, otherwise it returns null.
     * 
     * @param traceElement StackTraceElement
     * @return forbidden package name or null.
     */
    private static String tryGetForbiddenPackageName(StackTraceElement traceElement) {

        String classAndMethod = traceElement.getClassName() + "." + traceElement.getMethodName();
        for (String pkg : suppressedPackages) {
            if (classAndMethod.startsWith(pkg)) {
                return pkg;
            }
        }
        return null;
    }
}

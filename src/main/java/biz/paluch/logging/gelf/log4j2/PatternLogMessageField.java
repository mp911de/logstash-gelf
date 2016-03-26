package biz.paluch.logging.gelf.log4j2;

import org.apache.logging.log4j.core.layout.PatternLayout;

import biz.paluch.logging.gelf.LogMessageField;

/**
 * @author Mark Paluch
 */
public class PatternLogMessageField extends LogMessageField {

    private PatternLayout patternLayout;

    public PatternLogMessageField(String name, NamedLogField namedLogField, PatternLayout patternLayout) {
        super(name, namedLogField);
        this.patternLayout = patternLayout;
    }

    public PatternLayout getPatternLayout() {
        return patternLayout;
    }
}

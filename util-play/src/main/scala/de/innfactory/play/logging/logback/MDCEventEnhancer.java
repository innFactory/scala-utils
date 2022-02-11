package  de.innfactory.play.logging.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.google.cloud.logging.LogEntry;
import com.google.cloud.logging.logback.LoggingEventEnhancer;

import java.util.Map;


final class MDCEventEnhancer implements LoggingEventEnhancer {

    @Override
    public final void enhanceLogEntry(LogEntry.Builder builder, ILoggingEvent e) {
        for (Map.Entry<String, String> entry : e.getMDCPropertyMap().entrySet()) {
            if (null != entry.getKey() && null != entry.getValue()) {
                builder.addLabel(entry.getKey(), entry.getValue());
            }
        }
    }
}
/**
 * 
 */
package logbook.internal;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Nekopanda
 *
 */
public class LoggerHolder {

    private Logger logger = null;
    private Class<?> clazz;
    private String name;

    public LoggerHolder(Class<?> clazz) {
        this.clazz = clazz;
    }

    public LoggerHolder(String name) {
        this.name = name;
    }

    public synchronized Logger get() {
        if (this.logger == null) {
            if (this.clazz != null) {
                this.logger = LogManager.getLogger(this.clazz);
            }
            else if (this.name != null) {
                this.logger = LogManager.getLogger(this.name);
            }
        }
        return this.logger;
    }
}

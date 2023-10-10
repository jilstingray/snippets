package org.jilstingray.kafka.kafka;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ContextAware
        implements ApplicationContextAware
{
    @Getter
    private static ApplicationContext applicationContext = null;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
    {
        if (ContextAware.applicationContext == null) {
            ContextAware.applicationContext = applicationContext;
        }
        log.info("KafkaContextAware.applicationContext == " + ContextAware.applicationContext);
    }

    public static <T> T getBean(Class<T> c)
    {
        return getApplicationContext().getBean(c);
    }
}

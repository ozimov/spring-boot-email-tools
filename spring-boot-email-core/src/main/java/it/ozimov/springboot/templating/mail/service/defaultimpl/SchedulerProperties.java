package it.ozimov.springboot.templating.mail.service.defaultimpl;

import com.google.common.base.Preconditions;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Component
@ConfigurationProperties(prefix="spring.mail.scheduler")
public class SchedulerProperties {

    @Getter
    // spring.mail.scheduler.priorityLevels
    private int priorityLevels = 10;

    @Getter
    // spring.mail.scheduler.persistenceLayer.*
    private PersistenceLayer persistenceLayer = new PersistenceLayer();

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PersistenceLayer {

        // spring.mail.scheduler.persistenceLayer.desiredBatchSize
        @Getter
        private int desiredBatchSize = 500;

        // spring.mail.scheduler.persistenceLayer.maxKeptInMemory
        @Getter
        private int maxKeptInMemory = 2000;
    }

    @PostConstruct
    private void validate() {
        Preconditions.checkState(priorityLevels>0, "Expected at least one priority level");
    }

}
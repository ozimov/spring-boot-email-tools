package it.ozimov.springboot.templating.mail.service.defaultimpl;

import com.google.common.base.Preconditions;
import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Component
@ConfigurationProperties(prefix = "spring.mail.scheduler")
public class SchedulerProperties {

    // spring.mail.scheduler.priorityLevels
    private int priorityLevels;

    // spring.mail.scheduler.persistenceLayer.*
    private PersistenceLayer persistenceLayer;

    public static class SchedulerPropertiesBuilder {
        private int priorityLevels = 10;

        private PersistenceLayer persistenceLayer = new PersistenceLayer();
    }

    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PersistenceLayer {

        // spring.mail.scheduler.persistenceLayer.desiredBatchSize
        @Getter
        private int desiredBatchSize = 500;

        // spring.mail.scheduler.persistenceLayer.maxKeptInMemory
        @Getter
        private int maxKeptInMemory = 2000;

        public static class PersistenceLayerBuilder {
            private int desiredBatchSize = 500;
            private int maxKeptInMemory = 2000;
        }

    }

    @PostConstruct
    boolean validate() {
        Preconditions.checkState(priorityLevels > 0, "Expected at least one priority level");
        return true;
    }

}
package it.ozimov.springboot.templating.mail.service.defaultimpl;

import com.google.common.base.Preconditions;
import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import static java.util.Objects.isNull;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Component
@ConfigurationProperties(prefix = "spring.mail.scheduler")
public class SchedulerProperties {

    // spring.mail.scheduler.priorityLevels
    private int priorityLevels = 10;

    // spring.mail.scheduler.persistenceLayer.*
    private PersistenceLayer persistenceLayer = new PersistenceLayer();

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

        // spring.mail.scheduler.persistenceLayer.minKeptInMemory
        @Getter
        private int minKeptInMemory = 250;

        // spring.mail.scheduler.persistenceLayer.maxKeptInMemory
        @Getter
        private int maxKeptInMemory = 2000;

        public static class PersistenceLayerBuilder {
            private int desiredBatchSize = 500;
            private int minKeptInMemory = 250;
            private int maxKeptInMemory = 2000;
        }

    }

    @PostConstruct
    boolean validate() {
        Preconditions.checkState(priorityLevels > 0,
                "Expected at least one priority level. Review property 'spring.mail.scheduler.priorityLevels'.");

        Preconditions.checkState(isNull(persistenceLayer) || persistenceLayer.getDesiredBatchSize() > 0,
                "Expected at least a batch of size one, otherwise the persistence layer will not work. Review property 'spring.mail.scheduler.persistenceLayer.desiredBatchSize'.");

        Preconditions.checkState(isNull(persistenceLayer) || persistenceLayer.getMinKeptInMemory() >= 0,
                "Expected a non negative amount of email to be kept in memory. Review property 'spring.mail.scheduler.persistenceLayer.minKeptInMemory'.");

        Preconditions.checkState(isNull(persistenceLayer) || persistenceLayer.getMaxKeptInMemory() > 0,
                "Expected at least one email to be available in memory, otherwise the persistence layer will not work. Review property 'spring.mail.scheduler.persistenceLayer.maxKeptInMemory'.");

        Preconditions.checkState(isNull(persistenceLayer) ||
                        (persistenceLayer.getMaxKeptInMemory() >= persistenceLayer.getMinKeptInMemory()),
                "The application properties key '%s' should not have a value smaller than the value in property '%s'.",
                "spring.mail.scheduler.persistenceLayer.maxKeptInMemory", "spring.mail.scheduler.persistenceLayer.minKeptInMemory");

        Preconditions.checkState(isNull(persistenceLayer) ||
                        (persistenceLayer.getMaxKeptInMemory() >= persistenceLayer.getDesiredBatchSize()),
                "The application properties key '%s' should not have a value smaller than the value in property '%s'.",
                "spring.mail.scheduler.persistenceLayer.maxKeptInMemory", "spring.mail.scheduler.persistenceLayer.desiredBatchSize");
        return true;
    }

}
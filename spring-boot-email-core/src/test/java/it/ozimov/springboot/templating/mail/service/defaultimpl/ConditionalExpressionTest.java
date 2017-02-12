package it.ozimov.springboot.templating.mail.service.defaultimpl;

import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;

import static it.ozimov.springboot.templating.mail.service.defaultimpl.ConditionalExpression.*;

public class ConditionalExpressionTest {

    @Rule
    public final JUnitSoftAssertions assertions = new JUnitSoftAssertions();

    @Test
    public void shouldConstantsRemainUnchanged() {
        assertions.assertThat(SCHEDULER_IS_ENABLED)
                .as("The condition for enabling the scheduler should not change")
                .isEqualTo("'${spring.mail.scheduler.enabled:false}' == 'true'");
        assertions.assertThat(PERSISTENCE_IS_ENABLED)
                .as("The condition for enabling the persistence layer should not change")
                .isEqualTo("'${spring.mail.scheduler.enabled:false}' == 'true'" +
                        " && '${spring.mail.scheduler.persistence.enabled:false}' == 'true'");
        assertions.assertThat(PERSISTENCE_IS_ENABLED_WITH_EMBEDDED_REDIS)
                .as("The condition for enabling the persistence layer using embedded redis should not change")
                .isEqualTo("'${spring.mail.scheduler.enabled:false}' == 'true'" +
                        " && '${spring.mail.scheduler.persistence.enabled:false}' == 'true'" +
                        " && '${spring.mail.scheduler.persistence.redis.enabled:false}' == 'true'" +
                        " && '${spring.mail.scheduler.persistence.redis.embedded:false}' == 'true'");
    }

}
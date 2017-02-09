package it.ozimov.springboot.templating.mail.service.defaultimpl;

import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;

import static it.ozimov.springboot.templating.mail.service.defaultimpl.ConditionalExpression.PERSISTENCE_ENABLED_IS_TRUE;
import static it.ozimov.springboot.templating.mail.service.defaultimpl.ConditionalExpression.PERSISTENCE_WITH_EMBEDDED_REDIS_ENABLED_IS_TRUE;

public class ConditionalExpressionTest {

    @Rule
    public final JUnitSoftAssertions assertions = new JUnitSoftAssertions();

    @Test
    public void shouldConstantsRemainUnchanged() {
        assertions.assertThat(PERSISTENCE_ENABLED_IS_TRUE)
                .isEqualTo("'${spring.mail.persistence.enabled:false}' == 'true'");
        assertions.assertThat(PERSISTENCE_WITH_EMBEDDED_REDIS_ENABLED_IS_TRUE)
                .isEqualTo("'${spring.mail.persistence.enabled:false}' == 'true'" +
                        " && '${spring.mail.persistence.redis.enabled:false}' == 'true'" +
                        " && '${spring.mail.persistence.redis.embedded:false}' == 'true'");
    }

}
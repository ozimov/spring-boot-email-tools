package it.ozimov.springboot.templating.mail.service.defaultimpl;

import lombok.experimental.UtilityClass;

import static it.ozimov.springboot.templating.mail.service.ApplicationPropertiesConstants.*;

@UtilityClass
public class ConditionalExpression {

    public static final String SCHEDULER_IS_ENABLED = "'${" + SPRING_MAIL_SCHEDULER_ENABLED + ":false}' == 'true'";

    public static final String PERSISTENCE_IS_ENABLED = SCHEDULER_IS_ENABLED + " && '${" + SPRING_MAIL_PERSISTENCE_ENABLED + ":false}' == 'true'";

    public static final String PERSISTENCE_IS_ENABLED_WITH_REDIS =
            PERSISTENCE_IS_ENABLED +
                    " && '${" + SPRING_MAIL_SCHEDULER_PERSISTENCE_REDIS_ENABLED + ":false}' == 'true'";

    public static final String PERSISTENCE_IS_ENABLED_WITH_EMBEDDED_REDIS =
            PERSISTENCE_IS_ENABLED +
                    " && '${" + SPRING_MAIL_SCHEDULER_PERSISTENCE_REDIS_ENABLED + ":false}' == 'true'" +
                    " && '${" + SPRING_MAIL_SCHEDULER_PERSISTENCE_REDIS_EMBEDDED + ":false}' == 'true'";

}

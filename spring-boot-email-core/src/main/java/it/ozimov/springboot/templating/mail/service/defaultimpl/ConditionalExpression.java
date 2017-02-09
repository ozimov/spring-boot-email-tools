package it.ozimov.springboot.templating.mail.service.defaultimpl;

import it.ozimov.springboot.templating.mail.service.ApplicationPropertiesConstants;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ConditionalExpression {

    public static final String PERSISTENCE_ENABLED_IS_TRUE =
            "'${" + ApplicationPropertiesConstants.SPRING_MAIL_PERSISTENCE_ENABLED + ":false}' == 'true'";

    public static final String PERSISTENCE_WITH_EMBEDDED_REDIS_ENABLED_IS_TRUE =
            "'${" + ApplicationPropertiesConstants.SPRING_MAIL_PERSISTENCE_ENABLED + ":false}' == 'true'" +
                    " && '${" + ApplicationPropertiesConstants.SPRING_MAIL_PERSISTENCE_REDIS_ENABLED + ":false}' == 'true'" +
                    " && '${" + ApplicationPropertiesConstants.SPRING_MAIL_PERSISTENCE_REDIS_EMBEDDED + ":false}' == 'true'";

}

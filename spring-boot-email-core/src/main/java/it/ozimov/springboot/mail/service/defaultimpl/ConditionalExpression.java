/*
 * Copyright 2012-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.ozimov.springboot.mail.service.defaultimpl;

import lombok.experimental.UtilityClass;

import static it.ozimov.springboot.mail.configuration.ApplicationPropertiesConstants.*;

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


    public static final String EMAIL_LOGGING_RENDERER_IS_ENABLED = "'${" + SPRING_MAIL_LOGGING_ENABLED + ":true}' == 'true'";

}

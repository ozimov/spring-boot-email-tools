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

import com.google.common.base.Preconditions;
import lombok.experimental.UtilityClass;

@UtilityClass
public class RedisBasedPersistenceServiceConstants {

    public static final String ORDERING_KEY_PREFIX = "priority-level:";

    public static final String orderingKey(final int priorityLevel) {
        Preconditions.checkArgument(priorityLevel > 0, "Priority level must be a positive integer number");
        return orderingKeyPrefix() + priorityLevel;
    }

    public static final String orderingKeyPrefix() {
        return ORDERING_KEY_PREFIX;
    }

}
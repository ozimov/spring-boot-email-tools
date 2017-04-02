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

package it.ozimov.springboot.mail.configuration;

import it.ozimov.springboot.mail.UnitTest;
import it.ozimov.springboot.mail.model.EmailSchedulingData;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class EmailRedisTemplateConfigurationTest implements UnitTest {

    @Rule
    public final JUnitSoftAssertions assertions = new JUnitSoftAssertions();

    @Mock
    public RedisConnectionFactory redisConnectionFactory;

    public EmailRedisTemplateConfiguration emailRedisTemplateConfiguration;

    @Before
    public void setUp() {
        emailRedisTemplateConfiguration = new EmailRedisTemplateConfiguration(redisConnectionFactory);
    }

    @Test
    public void createOrderingTemplate() throws Exception {
        //Act
        StringRedisTemplate template = emailRedisTemplateConfiguration.createOrderingTemplate();

        //Assert
        assertions.assertThat(template).isNotNull();
    }

    @Test
    public void createValueTemplate() throws Exception {
        //Act
        RedisTemplate<String, EmailSchedulingData> template = emailRedisTemplateConfiguration.createValueTemplate();

        //Assert
        assertions.assertThat(template).isNotNull();
        assertions.assertThat(template.getKeySerializer()).isInstanceOf(StringRedisSerializer.class);
        assertions.assertThat(template.getHashKeySerializer()).isInstanceOf(StringRedisSerializer.class);
        assertions.assertThat(template.getHashValueSerializer()).isInstanceOf(StringRedisSerializer.class);
        assertions.assertThat(template.getValueSerializer()).isInstanceOf(JdkSerializationRedisSerializer.class);
    }

}
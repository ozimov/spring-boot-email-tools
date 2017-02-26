package it.ozimov.springboot.mail.configuration;

import it.ozimov.springboot.mail.UnitTest;
import it.ozimov.springboot.mail.model.EmailSchedulingData;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static it.ozimov.springboot.mail.service.defaultimpl.ConditionalExpression.PERSISTENCE_IS_ENABLED_WITH_REDIS;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
public class EmailRedisTemplateConfigurationTest implements UnitTest {

    @Rule
    public final JUnitSoftAssertions assertions = new JUnitSoftAssertions();

    @Mock
    public RedisConnectionFactory redisConnectionFactory;

    public EmailRedisTemplateConfiguration emailRedisTemplateConfiguration;

    @Before
    public void setUp(){
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
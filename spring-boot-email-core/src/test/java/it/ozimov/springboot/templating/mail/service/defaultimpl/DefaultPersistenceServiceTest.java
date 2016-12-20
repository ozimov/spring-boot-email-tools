package it.ozimov.springboot.templating.mail.service.defaultimpl;

import it.ozimov.cirneco.hamcrest.java7.javautils.IsUUID;
import it.ozimov.springboot.templating.mail.BaseRedisTest;
import it.ozimov.springboot.templating.mail.model.EmailSchedulingData;
import it.ozimov.springboot.templating.mail.model.defaultimpl.DefaultEmailSchedulingData;
import it.ozimov.springboot.templating.mail.service.PersistenceService;
import it.ozimov.springboot.templating.mail.utils.TimeUtils;
import lombok.NonNull;
import lombok.ToString;
import org.assertj.core.api.Condition;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.danhaywood.java.assertjext.Conditions.matchedBy;
import static it.ozimov.cirneco.hamcrest.java7.javautils.IsUUID.UUID;
import static it.ozimov.springboot.templating.mail.utils.DefaultEmailToMimeMessageTest.getSimpleMail;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {BaseRedisTest.JedisContextConfiguration.class})
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
//@ContextConfiguration(initializers = ConfigFileApplicationContextInitializer.class)
//@TestPropertySource(properties = { "spring.mail.persistence.redis.enabled = true"})
@Transactional(transactionManager = "transactionManager")
public class DefaultPersistenceServiceTest extends BaseRedisTest {

    @Rule public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Rule
    public Timeout timeout = new Timeout(120, TimeUnit.SECONDS);

    @Rule
    public final JUnitSoftAssertions assertions = new JUnitSoftAssertions();

//    @Spy
//    @Resource
    @SpyBean
    @Qualifier("orderingTemplate")
    private StringRedisTemplate orderingTemplate;

//    @Spy
//    @Resource
    @SpyBean
    @Qualifier("valueTemplate")
    private RedisTemplate<String, EmailSchedulingData> valueTemplate;

//    @InjectMocks
//    @Autowired
    @SpyBean
    @Qualifier("defaultEmailPersistenceService")
    private DefaultPersistenceService defaultPersistenceService;

    @Override
    public void additionalSetUp() {
//        ReflectionTestUtils.setField(defaultPersistenceService, "orderingTemplate", orderingTemplate);
//        ReflectionTestUtils.setField(defaultPersistenceService, "valueTemplate", valueTemplate);
    }

    @Captor
    private ArgumentCaptor<String> valueTemplateKeyArgumentCaptor;
    @Captor
    private ArgumentCaptor<String> orderingTemplateKeyArgumentCaptor;


    @Test
    @Rollback(false)
    public void shouldAdd() throws Exception {
        //Arrange
        final int assignedPriority = 1;

        final String expectedOrderingKey = RedisBasedPersistenceServiceConstants.orderingKey(assignedPriority);

        final OffsetDateTime dateTime = TimeUtils.offsetDateTimeNow();

        final DefaultEmailSchedulingData defaultEmailSchedulingData = DefaultEmailSchedulingData.defaultEmailSchedulingDataBuilder()
                .email(getSimpleMail())
                .scheduledDateTime(dateTime)
                .assignedPriority(assignedPriority)
                .desiredPriority(assignedPriority)
                .build();

        setBeforeTransactionAssertion(connection -> {
            assertions.assertThat(connection.exists(expectedOrderingKey.getBytes())).isFalse();
            assertions.assertThat(connection.exists(expectedOrderingKey.getBytes())).isFalse();
        });
        setAfterTransactionAssertion(connection -> {
            assertions.assertThat(connection.exists(expectedOrderingKey.getBytes())).isTrue();
        });

        //Act
        defaultPersistenceService.add(defaultEmailSchedulingData);

        //Assert
        InOrder inOrder = inOrder(orderingTemplate, valueTemplate);

        inOrder.verify(orderingTemplate).boundZSetOps(orderingTemplateKeyArgumentCaptor.capture());
        String orderingKey = orderingTemplateKeyArgumentCaptor.getValue();
        assertions.assertThat(orderingKey).isEqualTo(expectedOrderingKey);

        inOrder.verify(valueTemplate).boundValueOps(valueTemplateKeyArgumentCaptor.capture());
        String valueKey = valueTemplateKeyArgumentCaptor.getValue();
        assertions.assertThat(valueKey).is(matchedBy(UUID()));
    }

    @Test
    @Rollback(false)
    public void shouldGet() throws Exception {
        //Arrange
        final int assignedPriority = 1;

        final OffsetDateTime dateTime = TimeUtils.offsetDateTimeNow();

        final DefaultEmailSchedulingData defaultEmailSchedulingData = DefaultEmailSchedulingData.defaultEmailSchedulingDataBuilder()
                .email(getSimpleMail())
                .scheduledDateTime(dateTime)
                .assignedPriority(assignedPriority)
                .desiredPriority(assignedPriority)
                .build();
        defaultPersistenceService.add(defaultEmailSchedulingData);

        //Act
        Optional<EmailSchedulingData> givenOptionalEmailSchedulingData =
                defaultPersistenceService.get(defaultEmailSchedulingData.getId());

        //Assert
        assertions.assertThat(givenOptionalEmailSchedulingData)
                .isNotEmpty()
                .hasValue(defaultEmailSchedulingData)
                .hasSameClassAs(DefaultEmailSchedulingData.class);
    }


    //
//    @Test
//    public void addOps() throws Exception {
//
//    }
//
//    @Test
//    public void get() throws Exception {
//
//    }
//
//    @Test
//    public void getOps() throws Exception {
//
//    }
//
//    @Test
//    public void remove() throws Exception {
//
//    }
//
//    @Test
//    public void removeOps() throws Exception {
//
//    }
//
//    @Test
//    public void addAll() throws Exception {
//
//    }
//
//    @Test
//    public void addAllOps() throws Exception {
//
//    }
//
//    @Test
//    public void getNextBatch() throws Exception {
//
//    }
//
//    @Test
//    public void getNextBatchOps() throws Exception {
//
//    }
//
//    @Test
//    public void getNextBatchOps1() throws Exception {
//
//    }
//
//    @Test
//    public void getNextBatch1() throws Exception {
//
//    }
//
//    @Test
//    public void getNextBatchOps2() throws Exception {
//
//    }
//
//    @Test
//    public void removeAll() throws Exception {
//
//    }
//
//    @Test
//    public void removeAllOps() throws Exception {
//
//    }
//
//    @Test
//    public void removeAll1() throws Exception {
//
//    }
//
//    @Test
//    public void removeAllOps1() throws Exception {
//
//    }
//
//    @Test
//    public void removeAll2() throws Exception {
//
//    }
//
    @Test
    public void removeAllOps2() throws Exception {
        System.out.println(valueTemplate);
        System.out.println(valueTemplate instanceof org.mockito.cglib.proxy.Factory);

    }

}
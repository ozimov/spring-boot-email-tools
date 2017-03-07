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

import com.google.common.collect.ImmutableList;
import it.ozimov.springboot.mail.BaseRedisTest;
import it.ozimov.springboot.mail.model.EmailSchedulingData;
import it.ozimov.springboot.mail.model.defaultimpl.DefaultEmailSchedulingData;
import it.ozimov.springboot.mail.model.defaultimpl.TemplateEmailSchedulingData;
import it.ozimov.springboot.mail.utils.TimeUtils;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.danhaywood.java.assertjext.Conditions.matchedBy;
import static it.ozimov.cirneco.hamcrest.java7.javautils.IsUUID.UUID;
import static it.ozimov.springboot.mail.service.defaultimpl.EmailSchedulingDataUtils.createDefaultEmailSchedulingDataWithPriority;
import static it.ozimov.springboot.mail.service.defaultimpl.EmailSchedulingDataUtils.createTemplateEmailSchedulingDataWithPriority;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.inOrder;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = BaseRedisTest.ContextConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DefaultPersistenceServiceTest extends BaseRedisTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Rule
    public Timeout timeout = new Timeout(10, TimeUnit.SECONDS);

    @Rule
    public final JUnitSoftAssertions assertions = new JUnitSoftAssertions();

    @SpyBean
    @Qualifier("orderingTemplate")
    private StringRedisTemplate orderingTemplate;

    @SpyBean
    @Qualifier("valueTemplate")
    private RedisTemplate<String, EmailSchedulingData> valueTemplate;

    @SpyBean
    @Qualifier("defaultEmailPersistenceService")
    private DefaultPersistenceService defaultPersistenceService;

    @Captor
    private ArgumentCaptor<String> valueTemplateKeyArgumentCaptor;
    @Captor
    private ArgumentCaptor<String> orderingTemplateKeyArgumentCaptor;

    @Test
    public void shouldAddThrowNullPointerExceptionWhenInputParamIsNull() throws Exception {
        //Arrange
        expectedException.expect(NullPointerException.class);

        //Act
        defaultPersistenceService.add(null);

        //Assert
        fail();
    }

    @Test
    public void shouldAddInsertNewDefaultEmailSchedulingData() throws Exception {
        final int assignedPriority = 1;
        final DefaultEmailSchedulingData defaultEmailSchedulingData = createDefaultEmailSchedulingDataWithPriority(assignedPriority);

        shouldAddInsertNewSpecificTypeOfEmailSchedulingData(defaultEmailSchedulingData);
    }

    @Test
    public void shouldAddInsertNewTemplateEmailSchedulingData() throws Exception {
        final int assignedPriority = 1;
        final TemplateEmailSchedulingData templateEmailSchedulingData = createTemplateEmailSchedulingDataWithPriority(assignedPriority);

        shouldAddInsertNewSpecificTypeOfEmailSchedulingData(templateEmailSchedulingData);
    }

    private void shouldAddInsertNewSpecificTypeOfEmailSchedulingData(EmailSchedulingData emailSchedulingData) {
        //Arrange
        final int assignedPriority = emailSchedulingData.getAssignedPriority();
        final String expectedOrderingKey = RedisBasedPersistenceServiceConstants.orderingKey(assignedPriority);
        final String expectedValueKey = emailSchedulingData.getId();

        setBeforeTransactionAssertion(connection -> {
            assertions.assertThat(connection.exists(expectedOrderingKey.getBytes())).isFalse();
            assertions.assertThat(connection.exists(expectedValueKey.getBytes())).isFalse();
        });
        setAfterTransactionAssertion(connection -> {
            assertions.assertThat(connection.exists(expectedOrderingKey.getBytes()))
                    .as("After adding we should have the ordering key in REDIS")
                    .isTrue();
            assertions.assertThat(connection.exists(expectedValueKey.getBytes()))
                    .as("After adding we should have the value key in REDIS")
                    .isTrue();
        });

        //Act
        defaultPersistenceService.add(emailSchedulingData);

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
    public void shouldAddReplaceEmailSchedulingDataWhenTheValueKeyWasAlreadySet() throws Exception {
        //Arrange
        final int assignedPriority = 1;
        final DefaultEmailSchedulingData defaultEmailSchedulingData = createDefaultEmailSchedulingDataWithPriority(assignedPriority);

        final String expectedOrderingKey = RedisBasedPersistenceServiceConstants.orderingKey(assignedPriority);
        final String expectedValueKey = defaultEmailSchedulingData.getId();

        final DefaultEmailSchedulingData defaultEmailSchedulingDataSameId =
                createDefaultEmailSchedulingDataWithPriority(assignedPriority);
        final OffsetDateTime dateTime = TimeUtils.offsetDateTimeNow().plusYears(1);
        ReflectionTestUtils.setField(defaultEmailSchedulingDataSameId, "id", expectedValueKey);
        ReflectionTestUtils.setField(defaultEmailSchedulingDataSameId, "scheduledDateTime", dateTime);


        setBeforeTransactionAssertion(connection -> {
            assertions.assertThat(connection.exists(expectedOrderingKey.getBytes())).isFalse();
            assertions.assertThat(connection.exists(expectedValueKey.getBytes())).isFalse();
        });
        setAfterTransactionAssertion(connection -> {
            assertions.assertThat(connection.exists(expectedOrderingKey.getBytes()))
                    .as("After adding we should have the ordering key in REDIS")
                    .isTrue();
            assertions.assertThat(connection.exists(expectedValueKey.getBytes()))
                    .as("After adding we should have the value key in REDIS")
                    .isTrue();
        });

        //Act
        defaultPersistenceService.add(defaultEmailSchedulingData);
        defaultPersistenceService.add(defaultEmailSchedulingDataSameId);

        //Assert
        Optional<EmailSchedulingData> givenOptionalEmailSchedulingData = defaultPersistenceService.get(expectedValueKey);
        assertions.assertThat(givenOptionalEmailSchedulingData)
                .as("Should not have been replaced")
                .contains(defaultEmailSchedulingDataSameId);
    }

    @Test
    public void shouldAddNotReplaceEmailSchedulingDataWhenTheAssignedPriorityAndTimestampIsTheSame() throws Exception {
        //Arrange
        final int assignedPriority = 1;
        final DefaultEmailSchedulingData defaultEmailSchedulingData = createDefaultEmailSchedulingDataWithPriority(assignedPriority);

        final String expectedOrderingKey = RedisBasedPersistenceServiceConstants.orderingKey(assignedPriority);
        final String expectedValueKey_1 = defaultEmailSchedulingData.getId();

        final DefaultEmailSchedulingData defaultEmailSchedulingDataSamePriorityAndTimestamp =
                createDefaultEmailSchedulingDataWithPriority(assignedPriority);
        final String expectedValueKey_2 = defaultEmailSchedulingData.getId();
        final OffsetDateTime dateTime = defaultEmailSchedulingData.getScheduledDateTime();
        ReflectionTestUtils.setField(defaultEmailSchedulingDataSamePriorityAndTimestamp, "scheduledDateTime", dateTime);

        setBeforeTransactionAssertion(connection -> {
            assertions.assertThat(connection.exists(expectedOrderingKey.getBytes())).isFalse();
            assertions.assertThat(connection.exists(expectedValueKey_1.getBytes())).isFalse();
            assertions.assertThat(connection.exists(expectedValueKey_2.getBytes())).isFalse();
        });
        setAfterTransactionAssertion(connection -> {
            assertions.assertThat(connection.exists(expectedOrderingKey.getBytes()))
                    .as("After adding we should have the ordering key in REDIS")
                    .isTrue();
            assertions.assertThat(connection.exists(expectedValueKey_1.getBytes()))
                    .as("After adding we should have the value key in REDIS")
                    .isTrue();
            assertions.assertThat(connection.exists(expectedValueKey_2.getBytes()))
                    .as("After adding we should have the value key in REDIS")
                    .isTrue();
        });

        //Act
        defaultPersistenceService.add(defaultEmailSchedulingData);
        defaultPersistenceService.add(defaultEmailSchedulingDataSamePriorityAndTimestamp);
    }

    @Test
    public void shouldGetThrowNullPointerExceptionWhenInputParamIsNull() throws Exception {
        //Arrange
        expectedException.expect(NullPointerException.class);

        //Act
        defaultPersistenceService.get(null);

        //Assert
        fail();
    }

    @Test
    @Rollback(false)
    public void shouldGetReturnDefaultEmailSchedulingDataWhenPresent() throws Exception {
        //Arrange
        final int assignedPriority = 1;
        final DefaultEmailSchedulingData defaultEmailSchedulingData = createDefaultEmailSchedulingDataWithPriority(assignedPriority);

        shouldGetReturnSpecificTypeEmailSchedulingDataWhenPresent(defaultEmailSchedulingData);
    }

    @Test
    @Rollback(false)
    public void shouldGetReturnTemplateEmailSchedulingDataWhenPresent() throws Exception {
        //Arrange
        final int assignedPriority = 1;
        final TemplateEmailSchedulingData templateEmailSchedulingData = createTemplateEmailSchedulingDataWithPriority(assignedPriority);

        shouldGetReturnSpecificTypeEmailSchedulingDataWhenPresent(templateEmailSchedulingData);
    }

    private void shouldGetReturnSpecificTypeEmailSchedulingDataWhenPresent(EmailSchedulingData emailSchedulingData) {
        defaultPersistenceService.add(emailSchedulingData);

        //Act
        Optional<EmailSchedulingData> givenOptionalEmailSchedulingData =
                defaultPersistenceService.get(emailSchedulingData.getId());

        //Assert
        assertions.assertThat(givenOptionalEmailSchedulingData)
                .isNotEmpty()
                .containsInstanceOf(emailSchedulingData.getClass())
                .contains(emailSchedulingData);
    }

    @Test
    public void shouldGetReturnEmptyOptionalWhenNotPresent() throws Exception {
        //Arrange
        final String expectedValueKey = UUID.randomUUID().toString();

        setBeforeTransactionAssertion(connection -> {
            assertions.assertThat(connection.exists(expectedValueKey.getBytes())).isFalse();
        });

        //Act
        Optional<EmailSchedulingData> givenOptionalEmailSchedulingData =
                defaultPersistenceService.get(expectedValueKey);

        //Assert
        assertions.assertThat(givenOptionalEmailSchedulingData)
                .isEmpty();
    }

    @Test
    public void shouldRemoveThrowNullPointerExceptionWhenInputParamIsNull() throws Exception {
        //Arrange
        expectedException.expect(NullPointerException.class);

        //Act
        defaultPersistenceService.remove(null);

        //Assert
        fail();
    }

    @Test
    public void shouldRemoveReturnTrueWhenEmailSchedulingDataWasPresent() throws Exception {
        //Arrange
        final int assignedPriority = 1;
        final DefaultEmailSchedulingData defaultEmailSchedulingData = createDefaultEmailSchedulingDataWithPriority(assignedPriority);

        final String expectedOrderingKey = RedisBasedPersistenceServiceConstants.orderingKey(assignedPriority);
        final String expectedValueKey = defaultEmailSchedulingData.getId();

        setBeforeTransactionAssertion(connection -> {
            assertions.assertThat(connection.exists(expectedOrderingKey.getBytes())).isFalse();
            assertions.assertThat(connection.exists(expectedValueKey.getBytes())).isFalse();
        });
        setAfterTransactionAssertion(connection -> {
            assertions.assertThat(connection.exists(expectedOrderingKey.getBytes()))
                    .as("After removal we should not have the ordering key in REDIS")
                    .isTrue();
            assertions.assertThat(connection.exists(expectedValueKey.getBytes()))
                    .as("After removal we should not have the value key in REDIS")
                    .isTrue();
        });

        defaultPersistenceService.add(defaultEmailSchedulingData);

        //Act
        final boolean removed = defaultPersistenceService.remove(expectedValueKey);

        //Assert
        assertions.assertThat(removed).isTrue();

        InOrder inOrder = inOrder(orderingTemplate, valueTemplate);

        inOrder.verify(valueTemplate).delete(expectedValueKey);
        inOrder.verify(orderingTemplate).boundZSetOps(expectedOrderingKey);
    }

    @Test
    public void shouldRemoveReturnFalseWhenEmailSchedulingDataWasNotPresent() throws Exception {
        //Arrange
        final String expectedOrderingKey = RedisBasedPersistenceServiceConstants.orderingKey(1);
        final String expectedValueKey = UUID.randomUUID().toString();

        setBeforeTransactionAssertion(connection -> {
            assertions.assertThat(connection.exists(expectedOrderingKey.getBytes())).isFalse();
            assertions.assertThat(connection.exists(expectedValueKey.getBytes())).isFalse();
        });

        //Act
        final boolean removed = defaultPersistenceService.remove(expectedValueKey);

        //Assert
        assertions.assertThat(removed).isFalse();
    }


    @Test
    public void shouldAddAllThrowNullPointerExceptionWhenInputParamIsNull() throws Exception {
        //Arrange
        expectedException.expect(NullPointerException.class);

        //Act
        defaultPersistenceService.addAll(null);

        //Assert
        fail();
    }

    @Test
    public void shouldAddAllInsertCollectionOfEmailSchedulingData() throws Exception {
        //Arrange
        final int assignedPriority_1 = 1;
        final DefaultEmailSchedulingData defaultEmailSchedulingData_1_1 = createDefaultEmailSchedulingDataWithPriority(assignedPriority_1);
        final DefaultEmailSchedulingData defaultEmailSchedulingData_1_2 = createDefaultEmailSchedulingDataWithPriority(assignedPriority_1);
        final DefaultEmailSchedulingData defaultEmailSchedulingData_1_3 = createDefaultEmailSchedulingDataWithPriority(assignedPriority_1);

        final int assignedPriority_2 = 2;
        assertions.assertThat(assignedPriority_1).isNotEqualTo(assignedPriority_2);
        final DefaultEmailSchedulingData defaultEmailSchedulingData_2_1 = createDefaultEmailSchedulingDataWithPriority(assignedPriority_2);
        final DefaultEmailSchedulingData defaultEmailSchedulingData_2_2 = createDefaultEmailSchedulingDataWithPriority(assignedPriority_2);

        final String expectedOrderingKey_1 = RedisBasedPersistenceServiceConstants.orderingKey(assignedPriority_1);
        final String expectedValueKey_1_1 = defaultEmailSchedulingData_1_1.getId();
        final String expectedValueKey_1_2 = defaultEmailSchedulingData_1_2.getId();
        final String expectedValueKey_1_3 = defaultEmailSchedulingData_1_3.getId();

        final String expectedOrderingKey_2 = RedisBasedPersistenceServiceConstants.orderingKey(assignedPriority_2);
        final String expectedValueKey_2_1 = defaultEmailSchedulingData_2_1.getId();
        final String expectedValueKey_2_2 = defaultEmailSchedulingData_2_2.getId();

        setBeforeTransactionAssertion(connection -> {
            assertions.assertThat(connection.exists(expectedOrderingKey_1.getBytes())).isFalse();
            assertions.assertThat(connection.exists(expectedValueKey_1_1.getBytes())).isFalse();
            assertions.assertThat(connection.exists(expectedValueKey_1_2.getBytes())).isFalse();
            assertions.assertThat(connection.exists(expectedValueKey_1_3.getBytes())).isFalse();

            assertions.assertThat(connection.exists(expectedOrderingKey_2.getBytes())).isFalse();
            assertions.assertThat(connection.exists(expectedValueKey_2_1.getBytes())).isFalse();
            assertions.assertThat(connection.exists(expectedValueKey_2_2.getBytes())).isFalse();
        });
        setAfterTransactionAssertion(connection -> {
            assertions.assertThat(connection.exists(expectedOrderingKey_1.getBytes())).isTrue();
            assertions.assertThat(connection.exists(expectedValueKey_1_1.getBytes())).isTrue();
            assertions.assertThat(connection.exists(expectedValueKey_1_2.getBytes())).isTrue();
            assertions.assertThat(connection.exists(expectedValueKey_1_3.getBytes())).isTrue();

            assertions.assertThat(connection.exists(expectedOrderingKey_2.getBytes())).isTrue();
            assertions.assertThat(connection.exists(expectedValueKey_2_1.getBytes())).isTrue();
            assertions.assertThat(connection.exists(expectedValueKey_2_2.getBytes())).isTrue();
        });

        //Act
        defaultPersistenceService.addAll(ImmutableList.of(defaultEmailSchedulingData_1_1, defaultEmailSchedulingData_1_2, defaultEmailSchedulingData_1_3,
                defaultEmailSchedulingData_2_1, defaultEmailSchedulingData_2_2));

        //Assert

    }

    @Test
    public void shouldGetNextBatchForOrderingKeyReturnNothingGivenNonPositiveBatchSize() throws Exception {
        //Arrange
        int assignedPriority = 1;

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Batch size should be a positive integer.");

        //Act
        defaultPersistenceService.getNextBatch(assignedPriority, 0);
    }

    @Test
    public void shouldGetNextBatchForOrderingKeyReturnNothingGivenNoEmailSchedulingData() throws Exception {
        //Arrange
        int assignedPriority = 1;

        //Act
        Collection<EmailSchedulingData> givenBatch = defaultPersistenceService.getNextBatch(assignedPriority, 100);

        //Assert
        assertions.assertThat(givenBatch).isEmpty();
    }

    @Test
    public void shouldGetNextBatchForOrderingKeyReturnDesiredAmountOfEmailSchedulingData() throws Exception {
        //Arrange
        final int assignedPriority_1 = 1;
        final DefaultEmailSchedulingData defaultEmailSchedulingData_1_1 = createDefaultEmailSchedulingDataWithPriority(assignedPriority_1);
        final DefaultEmailSchedulingData defaultEmailSchedulingData_1_2 = createDefaultEmailSchedulingDataWithPriority(assignedPriority_1);
        TimeUnit.MILLISECONDS.sleep(1);
        final DefaultEmailSchedulingData defaultEmailSchedulingData_1_3 = createDefaultEmailSchedulingDataWithPriority(assignedPriority_1);

        final int assignedPriority_2 = 2;
        assertions.assertThat(assignedPriority_1).isNotEqualTo(assignedPriority_2);
        final DefaultEmailSchedulingData defaultEmailSchedulingData_2_1 = createDefaultEmailSchedulingDataWithPriority(assignedPriority_2);
        final DefaultEmailSchedulingData defaultEmailSchedulingData_2_2 = createDefaultEmailSchedulingDataWithPriority(assignedPriority_2);

        defaultPersistenceService.addAll(ImmutableList.of(defaultEmailSchedulingData_1_1, defaultEmailSchedulingData_1_2, defaultEmailSchedulingData_1_3,
                defaultEmailSchedulingData_2_1, defaultEmailSchedulingData_2_2));

        int desiredBatchSize = 2;

        //Act
        Collection<EmailSchedulingData> givenBatch = defaultPersistenceService.getNextBatch(assignedPriority_1, desiredBatchSize);

        //Assert
        assertions.assertThat(givenBatch)
                .hasSize(desiredBatchSize)
                .containsOnly(defaultEmailSchedulingData_1_1, defaultEmailSchedulingData_1_2);

    }

    @Test
    public void shouldGetNextBatchForOrderingKeyReturnAvailableEmailSchedulingDataWhenBatchSizeExceedAvailableEntries() throws Exception {
        //Arrange
        final int assignedPriority_1 = 1;
        final DefaultEmailSchedulingData defaultEmailSchedulingData_1_1 = createDefaultEmailSchedulingDataWithPriority(assignedPriority_1);
        final DefaultEmailSchedulingData defaultEmailSchedulingData_1_2 = createDefaultEmailSchedulingDataWithPriority(assignedPriority_1);
        TimeUnit.MILLISECONDS.sleep(1);
        final DefaultEmailSchedulingData defaultEmailSchedulingData_1_3 = createDefaultEmailSchedulingDataWithPriority(assignedPriority_1);

        Collection<EmailSchedulingData> emailSchedulingDataCollection = ImmutableList.of(defaultEmailSchedulingData_1_1, defaultEmailSchedulingData_1_2, defaultEmailSchedulingData_1_3);
        defaultPersistenceService.addAll(emailSchedulingDataCollection);

        int availableBatchSize = emailSchedulingDataCollection.size();
        int desiredBatchSize = availableBatchSize + 2;
        assertions.assertThat(desiredBatchSize).isGreaterThan(availableBatchSize);

        //Act
        Collection<EmailSchedulingData> givenBatch = defaultPersistenceService.getNextBatch(assignedPriority_1, desiredBatchSize);

        //Assert
        assertions.assertThat(givenBatch)
                .hasSize(availableBatchSize)
                .containsOnlyElementsOf(emailSchedulingDataCollection);
    }

    @Test
    public void shouldGetNextBatchThrowExceptionGivenNonPositiveBatchSize() throws Exception {
        //Arrange
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Batch size should be a positive integer, while 0 given.");

        //Act
        defaultPersistenceService.getNextBatch(0);

        //Assert
        fail();
    }

    @Test
    public void shouldGetNextBatchReturnNothingGivenNoEmailSchedulingData() throws Exception {
        //Act
        Collection<EmailSchedulingData> givenBatch = defaultPersistenceService.getNextBatch(100);

        //Assert
        assertions.assertThat(givenBatch).isEmpty();
    }

    @Test
    public void shouldGetNextBatchReturnDesiredAmountOfEmailSchedulingData() throws Exception {
        //Arrange
        final int assignedPriority_1 = 1;
        final int assignedPriority_2 = 2;
        assertions.assertThat(assignedPriority_1).isNotEqualTo(assignedPriority_2);

        final DefaultEmailSchedulingData defaultEmailSchedulingData_1_1 = createDefaultEmailSchedulingDataWithPriority(assignedPriority_1);
        TimeUnit.MILLISECONDS.sleep(1);
        final DefaultEmailSchedulingData defaultEmailSchedulingData_2_1 = createDefaultEmailSchedulingDataWithPriority(assignedPriority_2);
        TimeUnit.MILLISECONDS.sleep(1);
        final DefaultEmailSchedulingData defaultEmailSchedulingData_2_2 = createDefaultEmailSchedulingDataWithPriority(assignedPriority_2);
        TimeUnit.MILLISECONDS.sleep(1);
        final DefaultEmailSchedulingData defaultEmailSchedulingData_1_2 = createDefaultEmailSchedulingDataWithPriority(assignedPriority_1);
        TimeUnit.MILLISECONDS.sleep(1);
        final DefaultEmailSchedulingData defaultEmailSchedulingData_1_3 = createDefaultEmailSchedulingDataWithPriority(assignedPriority_1);

        defaultPersistenceService.addAll(ImmutableList.of(defaultEmailSchedulingData_1_1, defaultEmailSchedulingData_1_2, defaultEmailSchedulingData_1_3,
                defaultEmailSchedulingData_2_1, defaultEmailSchedulingData_2_2));

        int desiredBatchSize = 3;

        //Act
        Collection<EmailSchedulingData> givenBatch = defaultPersistenceService.getNextBatch(desiredBatchSize);

        //Assert
        assertions.assertThat(givenBatch)
                .hasSize(desiredBatchSize)
                .containsOnly(defaultEmailSchedulingData_1_1, defaultEmailSchedulingData_2_1, defaultEmailSchedulingData_2_2);
    }

    @Test
    public void shouldGetNextBatchReturnAvailableEmailSchedulingDataWhenBatchSizeExceedAvailableEntries() throws Exception {
        //Arrange
        final int assignedPriority_1 = 1;
        final DefaultEmailSchedulingData defaultEmailSchedulingData_1_1 = createDefaultEmailSchedulingDataWithPriority(assignedPriority_1);
        TimeUnit.MILLISECONDS.sleep(1);
        final DefaultEmailSchedulingData defaultEmailSchedulingData_1_2 = createDefaultEmailSchedulingDataWithPriority(assignedPriority_1);
        TimeUnit.MILLISECONDS.sleep(1);
        final DefaultEmailSchedulingData defaultEmailSchedulingData_1_3 = createDefaultEmailSchedulingDataWithPriority(assignedPriority_1);

        Collection<EmailSchedulingData> emailSchedulingDataCollection = ImmutableList.of(defaultEmailSchedulingData_1_1, defaultEmailSchedulingData_1_2, defaultEmailSchedulingData_1_3);
        defaultPersistenceService.addAll(emailSchedulingDataCollection);

        int availableBatchSize = emailSchedulingDataCollection.size();
        int desiredBatchSize = availableBatchSize + 2;
        assertions.assertThat(desiredBatchSize).isGreaterThan(availableBatchSize);

        //Act
        Collection<EmailSchedulingData> givenBatch = defaultPersistenceService.getNextBatch(desiredBatchSize);

        //Assert
        assertions.assertThat(givenBatch)
                .hasSize(availableBatchSize)
                .containsOnlyElementsOf(emailSchedulingDataCollection);
    }

    @Test
    public void shouldRemoveWithPriorityLevelAllDeleteAllEmailSchedulingData() throws Exception {
        //Arrange
        final int assignedPriority_1 = 1;
        final int assignedPriority_2 = 2;
        assertions.assertThat(assignedPriority_1).isNotEqualTo(assignedPriority_2);

        final DefaultEmailSchedulingData defaultEmailSchedulingData_1_1 = createDefaultEmailSchedulingDataWithPriority(assignedPriority_1);
        final DefaultEmailSchedulingData defaultEmailSchedulingData_1_2 = createDefaultEmailSchedulingDataWithPriority(assignedPriority_1);
        final DefaultEmailSchedulingData defaultEmailSchedulingData_1_3 = createDefaultEmailSchedulingDataWithPriority(assignedPriority_1);

        final DefaultEmailSchedulingData defaultEmailSchedulingData_2_1 = createDefaultEmailSchedulingDataWithPriority(assignedPriority_2);
        final DefaultEmailSchedulingData defaultEmailSchedulingData_2_2 = createDefaultEmailSchedulingDataWithPriority(assignedPriority_2);


        final List<String> allKeys = ImmutableList.of(
                RedisBasedPersistenceServiceConstants.orderingKey(assignedPriority_1),
                RedisBasedPersistenceServiceConstants.orderingKey(assignedPriority_2),
                defaultEmailSchedulingData_1_1.getId(),
                defaultEmailSchedulingData_1_2.getId(),
                defaultEmailSchedulingData_1_3.getId(),
                defaultEmailSchedulingData_2_1.getId(),
                defaultEmailSchedulingData_2_2.getId()
        );

        setAfterTransactionAssertion(connection -> {
            for (String key : allKeys) {
                assertions.assertThat(connection.exists(key.getBytes())).isFalse();
            }
        });
        defaultPersistenceService.addAll(ImmutableList.of(defaultEmailSchedulingData_1_1,
                defaultEmailSchedulingData_1_2,
                defaultEmailSchedulingData_1_3,
                defaultEmailSchedulingData_2_1,
                defaultEmailSchedulingData_2_2));

        //Act
        defaultPersistenceService.removeAll();
    }

    @Test
    public void shouldRemoveAllDeleteAllEmailSchedulingData() throws Exception {
        //Arrange
        final int assignedPriority_1 = 1;
        final int assignedPriority_2 = 2;
        assertions.assertThat(assignedPriority_1).isNotEqualTo(assignedPriority_2);

        final DefaultEmailSchedulingData defaultEmailSchedulingData_1_1 = createDefaultEmailSchedulingDataWithPriority(assignedPriority_1);
        final DefaultEmailSchedulingData defaultEmailSchedulingData_1_2 = createDefaultEmailSchedulingDataWithPriority(assignedPriority_1);
        final DefaultEmailSchedulingData defaultEmailSchedulingData_1_3 = createDefaultEmailSchedulingDataWithPriority(assignedPriority_1);

        final DefaultEmailSchedulingData defaultEmailSchedulingData_2_1 = createDefaultEmailSchedulingDataWithPriority(assignedPriority_2);
        final DefaultEmailSchedulingData defaultEmailSchedulingData_2_2 = createDefaultEmailSchedulingDataWithPriority(assignedPriority_2);

        final List<String> allKeysToBeRemoved = ImmutableList.of(
                RedisBasedPersistenceServiceConstants.orderingKey(assignedPriority_1),
                defaultEmailSchedulingData_1_1.getId(),
                defaultEmailSchedulingData_1_2.getId(),
                defaultEmailSchedulingData_1_3.getId()
        );

        final List<String> allKeysToBeKept = ImmutableList.of(
                RedisBasedPersistenceServiceConstants.orderingKey(assignedPriority_2),
                defaultEmailSchedulingData_2_1.getId(),
                defaultEmailSchedulingData_2_2.getId()
        );

        setAfterTransactionAssertion(connection -> {
            for (String key : allKeysToBeRemoved) {
                assertions.assertThat(connection.exists(key.getBytes())).isFalse();
            }
            for (String key : allKeysToBeKept) {
                assertions.assertThat(connection.exists(key.getBytes())).isTrue();
            }
        });

        defaultPersistenceService.addAll(ImmutableList.of(defaultEmailSchedulingData_1_1,
                defaultEmailSchedulingData_1_2,
                defaultEmailSchedulingData_1_3,
                defaultEmailSchedulingData_2_1,
                defaultEmailSchedulingData_2_2));

        //Act
        defaultPersistenceService.removeAll(assignedPriority_1);
    }

    @Test
    public void shouldRemoveAllDeleteSpecificEmailSchedulingData() throws Exception {
        //Arrange
        final int assignedPriority_1 = 1;
        final int assignedPriority_2 = 2;
        assertions.assertThat(assignedPriority_1).isNotEqualTo(assignedPriority_2);

        final DefaultEmailSchedulingData defaultEmailSchedulingData_1_1 = createDefaultEmailSchedulingDataWithPriority(assignedPriority_1);
        final DefaultEmailSchedulingData defaultEmailSchedulingData_1_2 = createDefaultEmailSchedulingDataWithPriority(assignedPriority_1);
        final DefaultEmailSchedulingData defaultEmailSchedulingData_1_3 = createDefaultEmailSchedulingDataWithPriority(assignedPriority_1);

        final DefaultEmailSchedulingData defaultEmailSchedulingData_2_1 = createDefaultEmailSchedulingDataWithPriority(assignedPriority_2);
        final DefaultEmailSchedulingData defaultEmailSchedulingData_2_2 = createDefaultEmailSchedulingDataWithPriority(assignedPriority_2);

        final List<String> allKeysToBeRemoved = ImmutableList.of(
                defaultEmailSchedulingData_1_1.getId(),
                defaultEmailSchedulingData_2_1.getId()
        );

        final List<String> allKeysToBeKept = ImmutableList.of(
                defaultEmailSchedulingData_1_2.getId(),
                defaultEmailSchedulingData_1_3.getId(),
                defaultEmailSchedulingData_2_2.getId()
        );

        setAfterTransactionAssertion(connection -> {
            for (String key : allKeysToBeRemoved) {
                assertions.assertThat(connection.exists(key.getBytes())).isFalse();
            }
            for (String key : allKeysToBeKept) {
                assertions.assertThat(connection.exists(key.getBytes())).isTrue();
            }
        });

        defaultPersistenceService.addAll(ImmutableList.of(defaultEmailSchedulingData_1_1,
                defaultEmailSchedulingData_1_2,
                defaultEmailSchedulingData_1_3,
                defaultEmailSchedulingData_2_1,
                defaultEmailSchedulingData_2_2));

        //Act
        defaultPersistenceService.removeAll(allKeysToBeRemoved);
    }

    @Test
    public void shouldRemoveAllDoNothingWhenNoDataIsPersisted() throws Exception {
        //Arrange
        setBeforeTransactionAssertion(connection -> {
            assertions.assertThat(connection.exists("*".getBytes())).isFalse();
        });

        //Act
        defaultPersistenceService.removeAll();
    }

    @Test
    public void shouldRemoveAllDoNothingOnUnknownPriorityLevel() throws Exception {
        //Arrange
        final int assignedPriority = 1;
        final String unmappedOrderingKey = RedisBasedPersistenceServiceConstants.orderingKey(assignedPriority);

        setBeforeTransactionAssertion(connection -> {
            assertions.assertThat(connection.exists(unmappedOrderingKey.getBytes())).isFalse();
        });

        //Act
        defaultPersistenceService.removeAll(assignedPriority);
    }

    @Test
    public void shouldRemoveDoNothingOnUnknownKey() throws Exception {
        //Arrange
        String unmappedKey = UUID.randomUUID().toString();

        setBeforeTransactionAssertion(connection -> {
            assertions.assertThat(connection.exists(unmappedKey.getBytes())).isFalse();
        });

        //Act
        defaultPersistenceService.removeAll(ImmutableList.of(unmappedKey));
    }

}
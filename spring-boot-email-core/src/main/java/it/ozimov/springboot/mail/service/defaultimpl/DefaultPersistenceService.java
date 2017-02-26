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
import it.ozimov.springboot.mail.model.EmailSchedulingData;
import it.ozimov.springboot.mail.service.PersistenceService;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.BoundZSetOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.temporal.ChronoField;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static it.ozimov.springboot.mail.service.defaultimpl.ConditionalExpression.PERSISTENCE_IS_ENABLED;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Objects.nonNull;

@Service("defaultEmailPersistenceService")
@ConditionalOnExpression(PERSISTENCE_IS_ENABLED)
public class DefaultPersistenceService implements PersistenceService {

    private static final String MATCH_ALL = "*";

    private final StringRedisTemplate orderingTemplate;
    private final RedisTemplate<String, EmailSchedulingData> valueTemplate;

    @Autowired
    public DefaultPersistenceService(@Qualifier("orderingTemplate") @NonNull final StringRedisTemplate orderingTemplate,
                                     @Qualifier("valueTemplate") @NonNull final RedisTemplate<String, EmailSchedulingData> valueTemplate) {
        this.orderingTemplate = orderingTemplate;
        this.orderingTemplate.setEnableTransactionSupport(true);

        this.valueTemplate = valueTemplate;
        RedisSerializer<String> stringSerializer = new StringRedisSerializer();
        JdkSerializationRedisSerializer jdkSerializationRedisSerializer = new JdkSerializationRedisSerializer();
        this.valueTemplate.setKeySerializer(stringSerializer);
        this.valueTemplate.setValueSerializer(jdkSerializationRedisSerializer);
        this.valueTemplate.setHashKeySerializer(stringSerializer);
        this.valueTemplate.setHashValueSerializer(stringSerializer);
        this.valueTemplate.setEnableTransactionSupport(true);
        this.valueTemplate.afterPropertiesSet();
    }

    @Override
    public void add(@NonNull final EmailSchedulingData emailSchedulingData) {
        addOps(emailSchedulingData);
    }

    protected void addOps(final EmailSchedulingData emailSchedulingData) {
        final String orderingKey = orderingKey(emailSchedulingData);
        final String valueKey = emailSchedulingData.getId();

        final double score = calculateScore(emailSchedulingData);

        BoundZSetOperations<String, String> orderingZSetOps = orderingTemplate.boundZSetOps(orderingKey);
        orderingZSetOps.add(valueKey, score);
        orderingZSetOps.persist();

        BoundValueOperations<String, EmailSchedulingData> valueValueOps = valueTemplate.boundValueOps(valueKey);
        valueValueOps.set(emailSchedulingData);
        valueValueOps.persist();
    }

    @Override
    public Optional<EmailSchedulingData> get(@NonNull final String id) {
        return Optional.ofNullable(getOps(id));
    }

    protected EmailSchedulingData getOps(final String id) {
        //valueTemplate.
        BoundValueOperations<String, EmailSchedulingData> boundValueOps = valueTemplate.boundValueOps(id);
        EmailSchedulingData emailSchedulingData = boundValueOps.get();
        return emailSchedulingData;
    }

    @Override
    public boolean remove(@NonNull final String id) {
        return removeOps(id);
    }

    protected boolean removeOps(final String id) {
        final EmailSchedulingData emailSchedulingData = getOps(id);
        if (nonNull(emailSchedulingData)) {
            valueTemplate.delete(id);
            final String orderingKey = orderingKey(emailSchedulingData);
            orderingTemplate.boundZSetOps(orderingKey).remove(id);
            return true;
        }

        return false;
    }

    @Override
    public void addAll(@NonNull final Collection<EmailSchedulingData> emailSchedulingDataList) {
        addAllOps(emailSchedulingDataList);
    }

    protected void addAllOps(final Collection<EmailSchedulingData> emailSchedulingDataList) {
        for (EmailSchedulingData emailSchedulingData : emailSchedulingDataList) {
            addOps(emailSchedulingData);
        }
    }

    @Override
    public Collection<EmailSchedulingData> getNextBatch(final int priorityLevel, final int batchMaxSize) {
        Preconditions.checkArgument(batchMaxSize > 0, "Batch size should be a positive integer.");

        final String orderingKey = RedisBasedPersistenceServiceConstants.orderingKey(priorityLevel);
        return getNextBatchOps(orderingKey, batchMaxSize);
    }

    protected Collection<EmailSchedulingData> getNextBatchOps(final String orderingKey, final int batchMaxSize) {
        Preconditions.checkArgument(batchMaxSize > 0, "Batch size should be a positive integer.");

        final BoundZSetOperations<String, String> boundZSetOperations = orderingTemplate.boundZSetOps(orderingKey);
        final long amount = boundZSetOperations.size();
        final Set<String> valueIds = boundZSetOperations.range(0, max(0, min(amount, batchMaxSize) - 1));
        return valueIds.stream()
                .map(id -> getOps(id))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    @Override
    public Collection<EmailSchedulingData> getNextBatch(final int batchMaxSize) {
        Preconditions.checkArgument(batchMaxSize > 0, "Batch size should be a positive integer, while %s given.", batchMaxSize);

        final Set<String> keys = new TreeSet<>(orderingTemplate.keys(RedisBasedPersistenceServiceConstants.orderingKeyPrefix() + MATCH_ALL));

        final Set<EmailSchedulingData> emailSchedulingDataSet = new TreeSet<>(EmailSchedulingData.DEFAULT_COMPARATOR);

        for (String key : keys) {
            emailSchedulingDataSet.addAll(getNextBatchOps(key, batchMaxSize));
        }

        return emailSchedulingDataSet.stream()
                .limit(min(batchMaxSize, emailSchedulingDataSet.size()))
                .collect(Collectors.toList());
    }

    @Override
    public void removeAll() {
        orderingTemplate.delete(MATCH_ALL);
        valueTemplate.delete(MATCH_ALL);
    }

    @Override
    public void removeAll(final int priorityLevel) {
        final String orderingKey = RedisBasedPersistenceServiceConstants.orderingKey(priorityLevel);

        BoundZSetOperations<String, String> boundZSetOperations = orderingTemplate.boundZSetOps(orderingKey);
        long amount = boundZSetOperations.size();

        final int offset = 2_000;

        IntStream.range(0, (int) Math.ceil(amount / offset))
                .parallel()
                .forEach(i -> {
                    long start = i * offset;
                    long end = min(amount, start + offset);
                    Set<String> valueIds = boundZSetOperations.range(start, end);
                    valueTemplate.delete(valueIds);

                });

        orderingTemplate.delete(orderingKey);
    }

    @Override
    public void removeAll(@NonNull final Collection<String> ids) {
        ids.parallelStream().forEach(id -> removeOps(id));
    }

    private String orderingKey(final EmailSchedulingData emailSchedulingData) {
        return RedisBasedPersistenceServiceConstants.orderingKey(emailSchedulingData.getAssignedPriority());
    }

    private double calculateScore(final EmailSchedulingData emailSchedulingData) {
        final long nanos = emailSchedulingData.getScheduledDateTime().getLong(ChronoField.NANO_OF_SECOND);
        final int desiredPriority = emailSchedulingData.getDesiredPriority();

        final String scoreStringValue = new StringBuilder()
                .append(nanos)
                .append(".")
                .append(desiredPriority)
                .append(Math.abs(emailSchedulingData.getId().hashCode()))
                .toString();
        final double score = new BigDecimal(scoreStringValue).doubleValue();
        return score;
    }

}
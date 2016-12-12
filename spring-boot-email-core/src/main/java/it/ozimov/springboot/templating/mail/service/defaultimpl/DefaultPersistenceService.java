package it.ozimov.springboot.templating.mail.service.defaultimpl;

import it.ozimov.springboot.templating.mail.model.EmailSchedulingData;
import it.ozimov.springboot.templating.mail.service.PersistenceService;
import it.ozimov.springboot.templating.mail.utils.ByteArrayToSerializable;
import it.ozimov.springboot.templating.mail.utils.SerializableToByteArray;
import lombok.NonNull;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.temporal.ChronoField;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Objects.nonNull;

@Service
@ConditionalOnProperty("${spring.mail.persistence.redis.enabled:false}")
public class DefaultPersistenceService implements PersistenceService {


    private final RedisTemplate redisTemplate;
    private final StringRedisTemplate orderingTemplate;
    private final RedisTemplate<String, EmailSchedulingData> valueTemplate;


    private static class SerializerInstanceHolder {
        public static final SerializableToByteArray<EmailSchedulingData> INSTANCE = new SerializableToByteArray<>();
    }

    private static class DeserializerInstanceHolder {
        public static final ByteArrayToSerializable<EmailSchedulingData> INSTANCE = new ByteArrayToSerializable<>();
    }

    public DefaultPersistenceService(@NonNull final RedisTemplate redisTemplate,
                                     @NonNull final StringRedisTemplate orderingTemplate,
                                     @NonNull final RedisTemplate<String, EmailSchedulingData> valueTemplate) {
        this.redisTemplate = redisTemplate;

        this.orderingTemplate = orderingTemplate;

        this.valueTemplate = valueTemplate;
        this.valueTemplate.setKeySerializer(new StringRedisSerializer());
        this.valueTemplate.setValueSerializer(new JdkSerializationRedisSerializer());
        this.valueTemplate.setEnableDefaultSerializer(false);
        this.valueTemplate.afterPropertiesSet();
    }

    @Override
    public void add(@NonNull final EmailSchedulingData emailSchedulingData) {
        redisTemplate.execute((RedisCallback) connection -> {
            addOps(emailSchedulingData);
            connection.exec();
            return null;
        });
    }

    protected void addOps(final EmailSchedulingData emailSchedulingData) {
        final String orderingKey = orderingKey(emailSchedulingData);
        final String valueKey = emailSchedulingData.getId();

        final double score = calculateScore(emailSchedulingData);

        orderingTemplate.boundZSetOps(orderingKey).add(valueKey, score);
        orderingTemplate.boundZSetOps(orderingKey).persist();

        valueTemplate.boundValueOps(valueKey).set(emailSchedulingData);
        valueTemplate.boundValueOps(valueKey).persist();
    }

    @Override
    public Optional<EmailSchedulingData> get(@NonNull final String id) {
        return Optional.ofNullable(getOps(id));
    }

    protected EmailSchedulingData getOps(final String id) {
        BoundValueOperations<String, EmailSchedulingData> boundValueOps = valueTemplate.boundValueOps(id);
        EmailSchedulingData emailSchedulingData = boundValueOps.get();
        return emailSchedulingData;
    }

    @Override
    public boolean remove(@NonNull final String id) {
        return (Boolean) redisTemplate.execute(
                (RedisCallback<Boolean>) connection -> {
                    boolean result = removeOps(id);
                    if(result) connection.exec();

                    return result;
                });
    }

    protected boolean removeOps(final String id){
        final EmailSchedulingData emailSchedulingData = getOps(id);
        if(nonNull(emailSchedulingData)){
            valueTemplate.delete(id);
            final String orderingKey = orderingKey(emailSchedulingData);
            orderingTemplate.boundZSetOps(orderingKey).remove(id);
            return true;
        }

        return false;
    }

    @Override
    public void addAll(@NonNull final Collection<EmailSchedulingData> emailSchedulingDataList) {
        redisTemplate.executePipelined((RedisCallback) connection -> {
            addAllOps(emailSchedulingDataList);
            connection.exec();
            return null;
        });
    }

    protected void addAllOps(final Collection<EmailSchedulingData> emailSchedulingDataList) {
        for(EmailSchedulingData emailSchedulingData : emailSchedulingDataList) {
            addOps(emailSchedulingData);
        }
    }

    @Override
    public Collection<EmailSchedulingData> getNextBatch(final int priorityLevel, final int batchMaxSize) {
        return (Collection<EmailSchedulingData>) redisTemplate.execute(
                (RedisCallback<Collection<EmailSchedulingData>>) connection -> getNextBatchOps(priorityLevel, batchMaxSize));
    }

    protected Collection<EmailSchedulingData> getNextBatchOps(final int priorityLevel, final int batchMaxSize) {
        final String orderingKey = orderingKey(priorityLevel);
        return getNextBatchOps(orderingKey, batchMaxSize);
    }

    protected Collection<EmailSchedulingData> getNextBatchOps(final String orderingKey, final int batchMaxSize) {
        BoundZSetOperations<String, String> boundZSetOperations = orderingTemplate.boundZSetOps(orderingKey);
        long amount = boundZSetOperations.size();
        Set<String> valueIds = boundZSetOperations.range(0, Math.min(amount, batchMaxSize));
        return valueIds.stream()
                .map(id -> getOps(id))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    @Override
    public Collection<EmailSchedulingData> getNextBatch(final int batchMaxSize) {
        return (Collection<EmailSchedulingData>) redisTemplate.execute(
                (RedisCallback<Collection<EmailSchedulingData>>) connection -> getNextBatchOps(batchMaxSize));
    }

    protected Collection<EmailSchedulingData> getNextBatchOps(final int batchMaxSize) {
        Set<String> keys = new TreeSet<>(orderingTemplate.keys(orderingKeyPrefix()+"*"));

        Set<EmailSchedulingData> emailSchedulingDataSet = new HashSet<>();
        for(String key : keys) {
            emailSchedulingDataSet.addAll(getNextBatchOps(key, Math.min(batchMaxSize, emailSchedulingDataSet.size())));
        }
        return emailSchedulingDataSet;
    }

    @Override
    public void removeAll() {
        redisTemplate.executePipelined(
                (RedisCallback) connection -> {
                    removeAllOps();
                    connection.exec();
                    return null;
                }
        );
    }

    protected void removeAllOps(){
        orderingTemplate.delete("*");
        valueTemplate.delete("*");
    }

    @Override
    public void removeAll(final int priorityLevel) {
        redisTemplate.executePipelined(
                (RedisCallback) connection -> {
                    removeAllOps(priorityLevel);
                    connection.exec();
                    return null;
                }
        );
    }

    protected void removeAllOps(int priorityLevel){
        final String orderingKey = orderingKey(priorityLevel);

        BoundZSetOperations<String, String> boundZSetOperations = orderingTemplate.boundZSetOps(orderingKey);
        long amount = boundZSetOperations.size();

        final int offset = 2_000;

        IntStream.range(0, (int) Math.ceil(amount/offset))
                .parallel()
                .forEach(i -> {
                    long start = i * offset;
                    long end = Math.min(amount, start + offset);
                    Set<String> valueIds = boundZSetOperations.range(start, end);
                    valueTemplate.delete(valueIds);

                });

        orderingTemplate.delete(orderingKey);
    }

    @Override
    public void removeAll(@NonNull final Collection<String> ids) {
        redisTemplate.executePipelined(
                (RedisCallback) connection -> {
                    removeAllOps(ids);
                    connection.exec();
                    return null;
                }
        );
    }

    protected void removeAllOps(final Collection<String> ids){
        ids.parallelStream()
                .forEach(id -> removeOps(id));
    }

    private String orderingKey(final EmailSchedulingData emailSchedulingData) {
        return orderingKey(emailSchedulingData.getAssignedPriority());
    }

    private String orderingKey(final int priorityLevel) {
        return orderingKeyPrefix() + priorityLevel;
    }

    private String orderingKeyPrefix() {
        return "priority-level:";
    }


    private double calculateScore(final EmailSchedulingData emailSchedulingData) {
        final long nanos = emailSchedulingData.getScheduledDateTime().getLong(ChronoField.NANO_OF_SECOND);
        final int desiredPriority = emailSchedulingData.getDesiredPriority();

        final String scoreStringValue = new StringBuilder().append(nanos).append(".").append(desiredPriority).toString();
        final double score = new BigDecimal(scoreStringValue).doubleValue();
        return score;
    }

}
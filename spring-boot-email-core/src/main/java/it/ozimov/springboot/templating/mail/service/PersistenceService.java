package it.ozimov.springboot.templating.mail.service;

import it.ozimov.springboot.templating.mail.model.EmailSchedulingData;

import java.util.Collection;
import java.util.Optional;

public interface PersistenceService {

    /**
     * Persist the {@linkplain EmailSchedulingData} into the persistence service. The key used to store the
     * instance if the one returned by {@linkplain EmailSchedulingData#getId()}.
     * <p>
     * Observe that in case of duplicate keys, the new values is used to override
     * the oldest without any warning.
     * </p>
     *
     * @param emailSchedulingData the data to be stored.
     */
    void add(EmailSchedulingData emailSchedulingData);

    /**
     * Retrieve an {@linkplain Optional} containing the {@linkplain EmailSchedulingData} stored for the
     * given emailSchedulingDataId, if the key exists and a value was found.
     * <p>In case of no value forund the the given key, an empty {@linkplain Optional} is returned.</p>
     *
     * @param id the emailSchedulingDataId used to store an {@linkplain EmailSchedulingData}.
     * @return an optional containing the scheduling data of an email if any was found for the
     * given emailSchedulingDataId; otherwise, an empty optional.
     */
    Optional<EmailSchedulingData> get(String id);

    /**
     * Remove the {@linkplain EmailSchedulingData} associated to the given id, returning {@code true}
     * if the key was found in the persistence layer, {@code false} otherwise.
     *
     * @param id the id of stored {@linkplain EmailSchedulingData}.
     * @return {@code true} if the id exists and the value was successfully removed; {@code false} otherwise.
     */
    boolean remove(String id);

    /**
     * Persist all the {@linkplain EmailSchedulingData} contained in the given
     * collection into the persistence service.  The key used to store each the
     * instance if the one returned by {@linkplain EmailSchedulingData#getId()}.
     * <p>
     * Observe that in case of duplicate keys, the new values is used to override
     * the oldest without any warning.
     * </p>
     *
     * @param emailSchedulingDataList collection of {@linkplain EmailSchedulingData} to be persisted
     */
    void addAll(Collection<EmailSchedulingData> emailSchedulingDataList);

    /**
     * Retrieves a batch of {@linkplain EmailSchedulingData} from the persistence layer, such that
     * up to {@code batchMaxSize} instances are returned from a specific priority level.
     * <p>
     * If no data is persisted, an empty collection is returned.
     * If less than {@code batchMaxSize} are persisted, {@code batchMaxSize} are returned.
     * </p>
     *
     * @param priorityLevel the priority level from which we want to extract the batch.
     * @param batchMaxSize the desired size of the batch.
     * @return a batch of {@linkplain EmailSchedulingData} to be retrieved from the persistence layer.
     */
    Collection<EmailSchedulingData> getNextBatch(int priorityLevel, int batchMaxSize);


    /**
     * Retrieves a batch of {@linkplain EmailSchedulingData} from the persistence layer, such that
     * up to {@code batchMaxSize} instances are returned from a whatsoever priority level.
     * <p>
     * If no data is persisted, an empty collection is returned.
     * If less than {@code batchMaxSize} are persisted, {@code batchMaxSize} are returned.
     * </p>
     *
     * @param batchMaxSize the desired size of the batch.
     * @return a batch of {@linkplain EmailSchedulingData} to be retrieved from the persistence layer.
     */
    Collection<EmailSchedulingData> getNextBatch(int batchMaxSize);

    /**
     * Remove all the {@linkplain EmailSchedulingData} in the peristence layer.
     *
     */
    void removeAll();

    /**
     * Remove all the {@linkplain EmailSchedulingData} associated to the given priority level.
     *
     * @param priorityLevel the id of the prioroty level that has to be erased.
     */
    void removeAll(int priorityLevel);


    /**
     * Remove all the {@linkplain EmailSchedulingData} associated to the ids in the given collection.
     * If for a specific id no key or value was found in the persistence layer, then the remove
     * operation for that id is simply skipped.
     *
     * @param ids a collection of ids of stored {@linkplain EmailSchedulingData}.
     */
    void removeAll(Collection<String> ids);

}
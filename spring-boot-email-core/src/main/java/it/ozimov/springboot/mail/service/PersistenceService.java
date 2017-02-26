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

package it.ozimov.springboot.mail.service;

import it.ozimov.springboot.mail.model.EmailSchedulingData;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional(rollbackFor = Exception.class)
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
    @Transactional(readOnly = true)
    Optional<EmailSchedulingData> get(String id);

    /**
     * Remove the {@linkplain EmailSchedulingData} associated to the given id, returning {@code true}
     * if the key was found in the persistence layer, {@code false} otherwise.
     *
     * @param id the id of stored {@linkplain EmailSchedulingData}.
     * @return {@code true} if the id exists and the value was successfully removed; {@code false} otherwise.
     */
    @Transactional(rollbackFor = Exception.class)
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
    @Transactional(rollbackFor = Exception.class)
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
     * @param batchMaxSize  the desired size of the batch.
     * @return a batch of {@linkplain EmailSchedulingData} to be retrieved from the persistence layer.
     */
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
    Collection<EmailSchedulingData> getNextBatch(int batchMaxSize);

    /**
     * Remove all the {@linkplain EmailSchedulingData} in the peristence layer.
     */
    @Transactional(rollbackFor = Exception.class)
    void removeAll();

    /**
     * Remove all the {@linkplain EmailSchedulingData} associated to the given priority level.
     *
     * @param priorityLevel the id of the prioroty level that has to be erased.
     */
    @Transactional(rollbackFor = Exception.class)
    void removeAll(int priorityLevel);


    /**
     * Remove all the {@linkplain EmailSchedulingData} associated to the ids in the given collection.
     * If for a specific id no key or value was found in the persistence layer, then the remove
     * operation for that id is simply skipped.
     *
     * @param ids a collection of ids of stored {@linkplain EmailSchedulingData}.
     */
    @Transactional(rollbackFor = Exception.class)
    void removeAll(Collection<String> ids);

}
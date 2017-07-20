package edu.stanford.bmir.protege.web.server.collection;

import edu.stanford.bmir.protege.web.server.persistence.Repository;
import edu.stanford.bmir.protege.web.shared.collection.CollectionElementData;
import edu.stanford.bmir.protege.web.shared.collection.CollectionElementId;
import edu.stanford.bmir.protege.web.shared.collection.CollectionId;
import edu.stanford.bmir.protege.web.shared.form.FormData;
import edu.stanford.bmir.protege.web.shared.inject.ApplicationSingleton;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.UpdateOptions;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static edu.stanford.bmir.protege.web.shared.collection.CollectionElementData.*;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 13 Jul 2017
 */
@ApplicationSingleton
public class CollectionElementDataRepository implements Repository {

    @Nonnull
    private final Datastore datastore;

    @Inject
    public CollectionElementDataRepository(@Nonnull Datastore datastore) {
        this.datastore = checkNotNull(datastore);
    }

    @Override
    public void ensureIndexes() {
        datastore.ensureIndexes(CollectionElementData.class);
    }

    public void save(CollectionElementData data) {
        Query<CollectionElementData> query = createQuery(data.getCollectionId())
                .field(ELEMENT_ID).equal(data.getElementId());
        UpdateOperations<CollectionElementData> updateOperations = datastore.createUpdateOperations(CollectionElementData.class);
        Optional<FormData> formData = data.getFormData();
        if(formData.isPresent()) {
            updateOperations.set(FORM_DATA, formData.get());
        }
        else {
            updateOperations.unset(FORM_DATA);
        }
        datastore.update(query, updateOperations, new UpdateOptions().upsert(true));
    }

    public long count(CollectionId collectionId) {
        return createQuery(collectionId)
                .count();
    }

    @Nonnull
    public List<CollectionElementData> find(CollectionId collectionId) {
        return createQuery(collectionId).asList();
    }

    @Nonnull
    public CollectionElementData find(@Nonnull CollectionId collectionId,
                                      @Nonnull CollectionElementId elementId) {
        CollectionElementData data = createQuery(collectionId)
                .field(ELEMENT_ID).equal(elementId).get();
        if(data == null) {
            return new CollectionElementData(collectionId, elementId);
        }
        else {
            return data;
        }

    }

    private Query<CollectionElementData> createQuery(CollectionId collectionId) {
        return datastore.createQuery(CollectionElementData.class)
                        .field(COLLECTION_ID).equal(collectionId);
    }
}

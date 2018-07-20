package edu.stanford.bmir.protege.web.shared.entity;

import edu.stanford.bmir.protege.web.shared.HasBrowserText;
import edu.stanford.bmir.protege.web.shared.HasSignature;
import org.semanticweb.owlapi.model.OWLEntity;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 28/11/2012
 * <p>
 *     Represents data about some object.  This data includes browser text for the object.
 * </p>
 */
public interface ObjectData<O> extends HasBrowserText, HasSignature {

    @Nonnull
    O getObject();

    /**
     * Gets the signature of this object.
     * @return A (possibly empty) set representing the signature of this object.
     */
    @Override
    default Set<OWLEntity> getSignature() {
        if(getObject() instanceof HasSignature) {
            return ((HasSignature) getSignature()).getSignature();
        }
        else {
            return Collections.emptySet();
        }
    }
}

package edu.stanford.bmir.protege.web.client.frame;

import com.google.common.base.Optional;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import edu.stanford.bmir.protege.web.client.dispatch.DispatchServiceManager;
import edu.stanford.bmir.protege.web.client.primitive.PrimitiveDataEditor;
import edu.stanford.bmir.protege.web.client.primitive.PrimitiveDataListEditor;
import edu.stanford.bmir.protege.web.client.editor.EditorView;
import edu.stanford.bmir.protege.web.client.library.common.EventStrategy;
import edu.stanford.bmir.protege.web.resources.WebProtegeClientBundle;
import edu.stanford.bmir.protege.web.shared.DirtyChangedEvent;
import edu.stanford.bmir.protege.web.shared.DirtyChangedHandler;
import edu.stanford.bmir.protege.web.shared.HasEntityDataProvider;
import edu.stanford.bmir.protege.web.shared.PrimitiveType;
import edu.stanford.bmir.protege.web.shared.entity.OWLEntityData;
import edu.stanford.bmir.protege.web.shared.entity.OWLPrimitiveData;
import edu.stanford.bmir.protege.web.shared.frame.ClassFrame;
import edu.stanford.bmir.protege.web.shared.frame.PropertyValue;
import edu.stanford.bmir.protege.web.shared.frame.PropertyValueList;
import edu.stanford.bmir.protege.web.shared.project.ProjectId;
import org.semanticweb.owlapi.model.OWLClass;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 03/12/2012
 */
public class ClassFrameEditor extends AbstractFrameEditor<LabelledFrame<ClassFrame>> implements ClassFrameEditorPresenter, EditorView<LabelledFrame<ClassFrame>> {

    @UiField
    protected TextBox iriField;

    @UiField(provided = true)
    protected final PropertyValueListEditor annotations;

    @UiField(provided = true)
    protected final PropertyValueListEditor properties;

    @UiField(provided = true)
    protected final PrimitiveDataListEditor classes;

    private LabelledFrame<ClassFrame> lastClassFrame;

    private OWLClass currentSubject;

    private boolean enabled = true;

    private boolean editable = true;

    private boolean dirty;

    interface ClassFrameEditor2UiBinder extends UiBinder<HTMLPanel, ClassFrameEditor> {

    }

    private static ClassFrameEditor2UiBinder ourUiBinder = GWT.create(ClassFrameEditor2UiBinder.class);

    @Inject
    public ClassFrameEditor(ProjectId projectId, Provider<PrimitiveDataEditor> primitiveDataEditorProvider, DispatchServiceManager dispatchServiceManager,  PropertyValueListEditor annotations, PropertyValueListEditor properties) {
        super(projectId, dispatchServiceManager);
        this.annotations = annotations;
        this.annotations.setGrammar(PropertyValueGridGrammar.getAnnotationsGrammar());
        this.classes = new PrimitiveDataListEditor(primitiveDataEditorProvider, PrimitiveType.CLASS);
        this.properties = properties;
        this.properties.setGrammar(PropertyValueGridGrammar.getClassGrammar());

        WebProtegeClientBundle.BUNDLE.style().ensureInjected();
        HTMLPanel rootElement = ourUiBinder.createAndBindUi(this);
        setWidget(rootElement);

    }

    public void setValue(final LabelledFrame<ClassFrame> lcf, HasEntityDataProvider entityDataProvider) {
        GWT.log("[EditorView] setValue: " + lcf);

        setDirty(false, EventStrategy.DO_NOT_FIRE_EVENTS);
        lastClassFrame = lcf;
        currentSubject = lcf.getFrame().getSubject();
        iriField.setValue(lcf.getFrame().getSubject().getIRI().toString());
        annotations.setValue(new PropertyValueList(new ArrayList<PropertyValue>(lcf.getFrame().getAnnotationPropertyValues())));
        properties.setValue(new PropertyValueList(new ArrayList<PropertyValue>(lcf.getFrame().getLogicalPropertyValues())));

        List<OWLPrimitiveData> dataList = new ArrayList<>();
        for (OWLClass cls : lcf.getFrame().getClassEntries()) {
            final Optional<OWLEntityData> rendering = entityDataProvider.getEntityData(cls);
            if (rendering.isPresent()) {
                dataList.add(rendering.get());
            }
        }
        classes.setValue(dataList);
    }


    /**
     * Returns true if the widget is enabled, false if not.
     */
    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets whether this widget is enabled.
     * @param enabled <code>true</code> to enable the widget, <code>false</code>
     * to disable it
     */
    @Override
    public void setEnabled(boolean enabled) {
        setEnabledInternal(enabled);
    }

    private void setEnabledInternal(boolean enabled) {
        this.enabled = enabled;
        iriField.setEnabled(false);
        annotations.setEnabled(enabled);
        properties.setEnabled(enabled);
        classes.setEnabled(enabled);
    }

    /**
     * Determines if the object implementing this interface is editable.
     * @return {@code true} if the object is editable, otherwise {@code false}.
     */
    @Override
    public boolean isEditable() {
        return editable;
    }

    /**
     * Sets the editable state of the object implementing this interface.
     * @param editable If {@code true} then the state is set to editable, if {@code false} then the state is set to
     * not editable.
     */
    @Override
    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    @Override
    public boolean isDirty() {
        return annotations.isDirty() || classes.isDirty() || properties.isDirty() || dirty;
    }



    @Override
    public HandlerRegistration addDirtyChangedHandler(DirtyChangedHandler handler) {
        return addHandler(handler, DirtyChangedEvent.TYPE);
    }

    private boolean isQuoted(String s) {
        return s.startsWith("'") && s.endsWith("'");
    }

    private String removeQuotes(String s) {
        if (s.startsWith("'") && s.endsWith("'")) {
            return s.substring(1, s.length() - 1);
        }
        else {
            return s;
        }
    }


    @Override
    public Optional<LabelledFrame<ClassFrame>> getValue() {
        if(currentSubject == null) {
            return Optional.absent();
        }
        else {
            ClassFrame.Builder builder = new ClassFrame.Builder(currentSubject);
            for(OWLPrimitiveData cls : classes.getValue().get()) {
                builder.addClass((OWLClass) cls.getObject());
            }
            builder.addPropertyValues(annotations.getValue().get().getPropertyValues());
            builder.addPropertyValues(properties.getValue().get().getPropertyValues());
            ClassFrame cf = builder.build();
            LabelledFrame<ClassFrame> labelledClassFrame = new LabelledFrame<>(lastClassFrame.getDisplayName(), cf);
            GWT.log("[EditorView] getValue: " + labelledClassFrame);
            return Optional.of(labelledClassFrame);
        }
    }

    @Override
    public boolean isWellFormed() {
        return annotations.isWellFormed() && properties.isWellFormed();
    }

    @Override
    public void clearValue() {
        annotations.clearValue();
        properties.clearValue();
        classes.clearValue();
    }

    @UiHandler("annotations")
    protected void handleAnnotationsValueChanged(ValueChangeEvent<Optional<PropertyValueList>> evt) {
        if(isWellFormed()) {
            ValueChangeEvent.fire(this, getValue());
        }
    }

    @UiHandler("annotations")
    protected void handleAnnotationsDirtyChanged(DirtyChangedEvent evt) {
        setDirty(true, EventStrategy.FIRE_EVENTS);
        if(isWellFormed()) {
            ValueChangeEvent.fire(this, getValue());
        }
    }

    @UiHandler("properties")
    protected void handlePropertiesValueChange(ValueChangeEvent<Optional<PropertyValueList>> evt) {
        if(isWellFormed()) {
            ValueChangeEvent.fire(this, getValue());
        }
    }

    @UiHandler("classes")
    protected void handleClassesValueChange(ValueChangeEvent<Optional<List<OWLPrimitiveData>>> evt) {
        if(isWellFormed()) {
            this.dirty = true;
            ValueChangeEvent.fire(this, getValue());
        }
    }



    @UiHandler("properties")
    protected void handlePropertiesDirtyChanged(DirtyChangedEvent evt) {
        setDirty(true, EventStrategy.FIRE_EVENTS);
    }

    private void setDirty(boolean dirty, EventStrategy eventStrategy) {
        this.dirty = dirty;
        if(eventStrategy == EventStrategy.FIRE_EVENTS) {
            fireEvent(new DirtyChangedEvent());
            if (isWellFormed()) {
                ValueChangeEvent.fire(this, getValue());
            }
        }
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Optional<LabelledFrame<ClassFrame>>> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    @Override
    public Widget getWidget() {
        return this;
    }
}
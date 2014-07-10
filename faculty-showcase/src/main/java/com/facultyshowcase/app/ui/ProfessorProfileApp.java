/*
 * Copyright (c) Interactive Information R & D (I2RD) LLC.
 * All Rights Reserved.
 *
 * This software is confidential and proprietary information of
 * I2RD LLC ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered
 * into with I2RD.
 */

package com.facultyshowcase.app.ui;

import com.facultyshowcase.app.model.ProfessorProfile;
import com.facultyshowcase.app.model.ProfessorProfileDAO;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.i2rd.cms.miwt.TimeAuditableColumn;
import com.i2rd.contentmodel.miwt.PropertyEditor;

import net.proteusframework.cms.CmsSite;
import net.proteusframework.core.JunctionOperator;
import net.proteusframework.core.hibernate.dao.EntityRetriever;
import net.proteusframework.core.html.HTMLElement;
import net.proteusframework.core.locale.LocaleContext;
import net.proteusframework.core.locale.LocalizedText;
import net.proteusframework.core.locale.TextSources;
import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;
import net.proteusframework.core.notification.NotificationImpl;
import net.proteusframework.internet.http.SiteLoader;
import net.proteusframework.ui.mcolumn.FixedValueColumn;
import net.proteusframework.ui.miwt.MIWTSession;
import net.proteusframework.ui.miwt.ReflectiveAction;
import net.proteusframework.ui.miwt.component.Component;
import net.proteusframework.ui.miwt.component.Container;
import net.proteusframework.ui.miwt.component.Label;
import net.proteusframework.ui.miwt.component.PushButton;
import net.proteusframework.ui.miwt.component.composite.CustomCellRenderer;
import net.proteusframework.ui.miwt.component.composite.DateFormatLabel;
import net.proteusframework.ui.miwt.data.SortOrder;
import net.proteusframework.ui.miwt.util.CommonActions;
import net.proteusframework.ui.miwt.util.CommonButtonText;
import net.proteusframework.ui.miwt.util.CommonColumnText;
import net.proteusframework.ui.search.ActionColumn;
import net.proteusframework.ui.search.JoinedQLBuilder;
import net.proteusframework.ui.search.QLBuilder;
import net.proteusframework.ui.search.QLBuilderImpl;
import net.proteusframework.ui.search.QLOrderByImpl;
import net.proteusframework.ui.search.SearchModelImpl;
import net.proteusframework.ui.search.SearchResultColumnImpl;
import net.proteusframework.ui.search.SearchSupplier;
import net.proteusframework.ui.search.SearchSupplierImpl;
import net.proteusframework.ui.search.SearchUI;
import net.proteusframework.ui.search.SearchUIApp;
import net.proteusframework.ui.search.SearchUIImpl;
import net.proteusframework.ui.search.SearchUIOperation;
import net.proteusframework.ui.search.SearchUIOperationContext;
import net.proteusframework.ui.search.SearchUIOperationHandlerImpl;
import net.proteusframework.ui.search.SimpleConstraint;
import net.proteusframework.ui.workspace.AbstractUITask;
import net.proteusframework.users.model.Name;

import static net.proteusframework.core.StringFactory.isEmptyString;
import static net.proteusframework.ui.search.SearchUIImpl.Options;

/**
 * Example application to demonstrate how to create and handle
 * UI views.
 *
 * You'll want to attach the following files to the page this is on:
 * <ul>
 *     <li>form-elements-buttons.css</li>
 *     <li>tables-trees.css</li>
 *     <li>messages.css</li>
 *     <li>shared-search.css</li>
 *     <li>workspace.css</li>
 *     <li>ProfessorProfileExample1.css *</li>
 *     <li>jquery.Jcrop.css *</li>
 *     <li>img-cropper.css *</li>
 * </ul>
 * The correct version of Jquery comes packaged with Jcrop.
 * <ul>
 *     <li>jquery.min.js *</li>
 *     <li>jquery.color.js *</li>
 *     <li>jquery.Jcrop.min.js *</li>
 *     <li>dnd-file.js *</li>
 *     <li>img-cropper.js *</li>
 * </ul>
 *
 * @author Russ Tennant (russ@i2rd.com)
 */
@I18NFile(
    symbolPrefix = "com.facultyshowcase.app.ProfessorProfileApp",
    i18n = {
        @I18N(symbol = "SearchSupplier.Name", l10n = @L10N("User Profile")),
        @I18N(symbol = "SearchSupplier.Description", l10n = @L10N("Default User Profile Search"))
    }
)
@Configurable
public class ProfessorProfileApp extends SearchUIApp
{
    /** Logger. */
    private final static Logger _logger = Logger.getLogger(ProfessorProfileApp.class);

    /** Mock Data Access Object. */
    @Autowired
    private ProfessorProfileDAO _ProfessorProfileDAO;
    /** Site Loader. */
    @Autowired
    private SiteLoader _siteLoader;


    /**
     * Create an instance of the application for spring.
     */
    public ProfessorProfileApp()
    {
        super(null);
    }

    /**
     * Create an instance of the application.
     *
     * @param session the session.
     */
    public ProfessorProfileApp(MIWTSession session)
    {
        super(session);
        // Set the default application name used in toString() and some other circumstances.
        // Since internationalization (i18n) is not a concern for our application, we use the
        /// TextSources.create method as a placeholder in case we change our mind about i18n later.
        /// This will allow you to quickly / programmatically find text needing i18n and l10n (localization).
        this.defAppName = TextSources.create("User Profile Example App");


    }


    @Override
    protected void userStarted()
    {
        // All startup code for an application goes here.
        // You do *not* need to call super.userStarted() if extending MIWTApplication directly - we are not.
        super.userStarted();

        setupSearch();
    }

    /**
     * Setup the search UI.
     */
    public void setupSearch()
    {
        SearchSupplier searchSupplier = createSearchSupplier();
        Options options = new Options("User Profile");
        ReflectiveAction addAction = CommonActions.ADD.defaultAction();
        addAction.setTarget(this, "addProfessorProfile");
        options.addEntityAction(addAction);
        options.addSearchSupplier(searchSupplier);
        SearchUIImpl searchUI = new SearchUIImpl(options);
        setSearchUI(searchUI);
    }


    /**
     * Add a new user profile.
     */
    void addProfessorProfile()
    {
        final ProfessorProfile ProfessorProfile = new ProfessorProfile();
        ProfessorProfile.setSite((CmsSite) _siteLoader.getOperationalSite());
        setupEditor(ProfessorProfile);
    }

    /**
     * Setup the editor by creating the View and wiring in the necessary
     * controls for persistence with our DAO.
     *
     * @param ProfessorProfile the user profile.
     */
    void setupEditor(final ProfessorProfile ProfessorProfile)
    {
        getActiveSearchUI().getWorkspace().addUITask(new AbstractUITask("upe#" + ProfessorProfile.getId())
        {
            @Override
            public LocalizedText getLabel(LocaleContext localeContext)
            {
                return new LocalizedText("User Profile - " + _getName(
                    EntityRetriever.getInstance().reattachIfNecessary(ProfessorProfile)
                ));
            }

            @Override
            public Component createTaskUI(LocaleContext localeContext)
            {

                ReflectiveAction saveAction = CommonActions.SAVE.defaultAction();
                ReflectiveAction cancelAction = CommonActions.CANCEL.defaultAction();
                PropertyEditor<ProfessorProfile> propertyEditor = new PropertyEditor<>();
                propertyEditor.setTitle(new Label(TextSources.create("User Profile")).setHTMLElement(HTMLElement.h2));
                propertyEditor.setValueEditor(new ProfessorProfileEditor(ProfessorProfile));
                propertyEditor.setPersistenceActions(saveAction, cancelAction);

                // We are creating the action logic after creating the UI components
                /// so we can reference the component variables in the actions.
                cancelAction.setActionListener(event -> {
                    // When the cancelAction is executed, close the editor UI
                    /// the switch to the viewer UI.

                    // If we want to warn the user about unsaved changes, then
                    /// we could check the ModificationState of the editor before
                    /// proceeding.

                    propertyEditor.close();
                    setupViewer(EntityRetriever.getInstance().reattachIfNecessary(ProfessorProfile));
                });

                saveAction.setActionListener(event -> {
                    // If the user presses one of the save buttons, then
                    /// validate that the data captured in the UI is valid
                    /// before attempting to save the changes.
                    /// Any validation errors will be added to the notifiable.
                    if (propertyEditor.validateValue())
                    {
                        boolean success = false;
                        try
                        {
                            if (propertyEditor.getModificationState().isModified())
                            {
                                // Editor indicated data is valid, commit the UI
                                /// data to the ProfessorProfile and attempt to save it.
                                final ProfessorProfile commitValue = propertyEditor.commitValue();
                                assert commitValue != null;
                                _ProfessorProfileDAO.saveProfessorProfile(commitValue);
                                success = true;
                            }
                            else success=true;
                        }
                        catch (Exception e)
                        {
                            if (ExceptionUtils.indexOfThrowable(e, ConstraintViolationException.class) > 0) {
                                _logger.error("Constraint violation", e);
                                // Let the user know that something bad happened.
                                propertyEditor.getNotifiable()
                                    .sendNotification(NotificationImpl.create(e, "Username already in use."));
                            } else {

                                _logger.error("Unexpected error committing changes", e);
                                // Let the user know that something bad happened.
                                propertyEditor.getNotifiable()
                                    .sendNotification(NotificationImpl.create(e, "I'm sorry, I was unable to save."));
                            }
                        }

                        if(success)
                        {
                            // Save was successful. We're executing the cancelAction
                            /// since it switches us to the viewer UI.
                            cancelAction.actionPerformed(event);
                        }
                    }
                });

                return propertyEditor;
            }
        });
    }

    /**
     * Setup the viewer by wiring in the necessary controls to switch to
     * the editor View.
     *
     * @param ProfessorProfile the user profile.
     */
    void setupViewer(final ProfessorProfile ProfessorProfile)
    {
        // This is very similar to setupEditor - just a little less complex.
        getActiveSearchUI().getWorkspace().addUITask(new AbstractUITask("up#" + ProfessorProfile.getId())
        {
            @Override
            public LocalizedText getLabel(LocaleContext localeContext)
            {
                return new LocalizedText("User Profile - " + _getName(
                    EntityRetriever.getInstance().reattachIfNecessary(ProfessorProfile)
                ));
            }

            @Override
            public Component createTaskUI(LocaleContext localeContext)
            {
                final Container viewerWrapper = Container.of(HTMLElement.div, "property-wrapper");
                ProfessorProfileViewer viewer = new ProfessorProfileViewer(ProfessorProfile);

                // We are only putting actions at the top of this UI - no bottom actions -
                /// because the viewer UI is much shorter than the editor UI and is likely
                /// to fit on most screens so the edit button should always be visible.
                PushButton editBtn = CommonActions.EDIT.push();
                Container actions = Container.of(HTMLElement.div, "actions top persistence-actions", editBtn);
                viewer.add(actions);

                viewerWrapper.add(new Label(TextSources.create("User Profile")).setHTMLElement(HTMLElement.h1));
                viewerWrapper.add(viewer);

                editBtn.addActionListener(event -> {
                    viewerWrapper.close();
                    setupEditor(EntityRetriever.getInstance().reattachIfNecessary(ProfessorProfile));
                });
                return viewerWrapper;
            }
        });
    }


    /**
     * Create a search supplier for ProfessorProfiles that you can modify.
     * This supplier has the name, description, and
     * @return a new supplier.
     */
    SearchSupplier createSearchSupplier()
    {
        SearchModelImpl searchModel = new SearchModelImpl();
        searchModel.setName("ProfessorProfile Search");
        searchModel.setDisplayName(ProfessorProfileAppLOK.SEARCHSUPPLIER_NAME());
        searchModel.getConstraints().add(new SimpleConstraint("keyword")
            {
                @Override
                public void addCriteria(QLBuilder builder, Component constraintComponent)
                {

                    final Object value = getValue(constraintComponent);
                    if (!shouldReturnConstraintForValue(value) || value == null)
                        return;
                    builder.startGroup(JunctionOperator.OR);
                    final String lowerCaseValue = value.toString().toLowerCase();
                    final String valueParamNameStartsWith = builder.param("valueStart", lowerCaseValue + '%');
                    final String valueParamNameContains = builder.param("valueStart", '%' + lowerCaseValue + '%');
                    final JoinedQLBuilder nameBuilder = builder.createJoin(QLBuilder.JoinType.LEFT, "name", "name");
                    nameBuilder.startGroup(JunctionOperator.OR);
                    nameBuilder.formatCriteria("LOWER(%s.first) LIKE %s", nameBuilder.getAlias(), valueParamNameStartsWith);
                    nameBuilder.formatCriteria("LOWER(%s.last) LIKE %s", nameBuilder.getAlias(), valueParamNameStartsWith);
                    nameBuilder.endGroup();
                    builder.formatCriteria("LOWER(%s.phoneNumber) LIKE %s", builder.getAlias(), valueParamNameStartsWith);
                    builder.formatCriteria("LOWER(%s.emailAddress) LIKE %s", builder.getAlias(), valueParamNameStartsWith);
                    builder.formatCriteria("LOWER(%s.aboutMeProse) LIKE %s", builder.getAlias(), valueParamNameContains);
                    builder.endGroup();
                }
            }
            .withLabel(CommonButtonText.KEYWORD)
        );

        searchModel.getResultColumns().add(new SearchResultColumnImpl()
                .withName("name")
                .withTableColumn(new FixedValueColumn().withColumnName(CommonColumnText.NAME))
                .withTableCellRenderer(new CustomCellRenderer("", input -> {
                    ProfessorProfile ProfessorProfile = (ProfessorProfile) input;
                    if (ProfessorProfile == null || ProfessorProfile.getName() == null) return null;
                    return _getName(ProfessorProfile);
                }))
                .withOrderBy(new QLOrderByImpl()
                {
                    @Override
                    public void updateOrderBy(QLBuilder builder, SortOrder sortOrder)
                    {
                        final String orderOperator = getOrderOperator(sortOrder);
                        builder.setOrderBy("name.first " + orderOperator + ", name.last " + orderOperator);
                    }
                })
        );

        searchModel.getResultColumns().add(new SearchResultColumnImpl()
                .withName("emailAddress")
                .withTableColumn(new FixedValueColumn().withColumnName(CommonColumnText.EMAIL))
                .withTableCellRenderer(new CustomCellRenderer("", input -> {
                    ProfessorProfile ProfessorProfile = (ProfessorProfile) input;
                    if (ProfessorProfile == null || ProfessorProfile.getEmailAddress() == null) return null;
                    return ProfessorProfile.getEmailAddress();
                }))
                .withOrderBy(new QLOrderByImpl("emailAddress"))
        );

        searchModel.getResultColumns().add(new SearchResultColumnImpl()
                .withName("lastModTime")
                .withTableColumn(new TimeAuditableColumn(TimeAuditableColumn.AuditableType.modified_time)
                    .withColumnName(CommonColumnText.MODIFIED).withComparator(null)
                )
                .withTableCellRenderer(DateFormatLabel.createFriendlyInstance(null))
                .withOrderBy(new QLOrderByImpl("lastModTime"))
        );
        final ActionColumn actionColumn = new ActionColumn();
        actionColumn.setIncludeCopy(false);
        searchModel.getResultColumns().add(actionColumn);

        SearchSupplierImpl searchSupplier = new SearchSupplierImpl();
        searchSupplier.setName(ProfessorProfileAppLOK.SEARCHSUPPLIER_NAME());
        searchSupplier.setDescription(ProfessorProfileAppLOK.SEARCHSUPPLIER_DESCRIPTION());
        searchSupplier.setBuilderSupplier(() -> new QLBuilderImpl(ProfessorProfile.class, "ProfessorProfile"));
        searchSupplier.setSearchModel(searchModel);
        searchSupplier.setSearchUIOperationHandler(new SearchUIOperationHandlerImpl()
        {
            @Override
            public boolean supportsOperation(SearchUIOperation operation)
            {
                switch (operation)
                {
                    case select:
                    case edit:
                    case delete:
                    case view:
                    case add:
                        return true;
                    case copy:
                    default:
                        return false;
                }
            }

            @Override
            public void handle(SearchUIOperationContext context)
            {
                switch (context.getOperation())
                {

                    case select:
                    case view:
                        viewProfessorProfiles(context);
                        break;

                    case edit:
                        editProfessorProfiles(context);
                        break;

                    case delete:
                        deleteProfessorProfiles(context);
                        break;

                    case add:
                        addProfessorProfile();
                        break;

                    default:
                        throw new AssertionError("Unhandled operation: " + context.getOperation());
                }
            }
        });


        return searchSupplier;
    }

    /**
     * Get the name.
     * @param ProfessorProfile the user profile.
     * @return  the name or an empty string.
     */
    private static String _getName(ProfessorProfile ProfessorProfile)
    {
        final Name name = ProfessorProfile.getName();
        if(name != null)
        {
            final boolean lastNameExists = !isEmptyString(name.getLast());
            final boolean firstNameExists = !isEmptyString(name.getFirst());
            if (firstNameExists)
            {
                if (lastNameExists)
                    return name.getFirst() + ' ' + name.getLast();
                else
                    return name.getFirst();
            }
            else if (lastNameExists)
                return name.getLast();
        }
        return "";
    }

    /**
     * Delete the requested user profiles.
     * @param context the context.
     */
    void deleteProfessorProfiles(SearchUIOperationContext context)
    {
        final SearchUI searchUI = context.getSearchUI();
        switch (context.getDataContext())
        {
            case new_instance:
            case lead_selection:
                ProfessorProfile ProfessorProfile = context.getData();
                if(ProfessorProfile != null)
                    _ProfessorProfileDAO.deleteProfessorProfile(ProfessorProfile);
                break;
            case selection:
                final QLBuilder selectionQLBuilder = searchUI.getSelectionQLBuilder();
                if(selectionQLBuilder == null)
                    _ProfessorProfileDAO.deleteProfessorProfiles(searchUI.getSelection());
                else
                    _ProfessorProfileDAO.deleteProfessorProfiles(selectionQLBuilder);
                break;
            case search:
                final QLBuilder currentSearchQLBuilder = searchUI.getCurrentSearchQLBuilder();
                _ProfessorProfileDAO.deleteProfessorProfiles(currentSearchQLBuilder);
                _logger.warn("Not viewing all profiles that match search.");
                break;
        }
    }

    /**
     * Edit the requested user profiles.
     * @param context the context.
     */
    void editProfessorProfiles(SearchUIOperationContext context)
    {
        switch (context.getDataContext())
        {

            case new_instance:
            case lead_selection:
                ProfessorProfile ProfessorProfile = context.getData();
                if(ProfessorProfile != null)
                    setupEditor(ProfessorProfile);
                else
                    _logger.warn("No data for " + context);
                break;
            case selection:
                _logger.warn("Not viewing all profiles that match selection.");
                break;
            case search:
                _logger.warn("Not viewing all profiles that match search.");
                break;
        }
    }

    /**
     * View the requested user profiles.
     * @param context the context.
     */
    void viewProfessorProfiles(SearchUIOperationContext context)
    {
        switch (context.getDataContext())
        {

            case new_instance:
            case lead_selection:
                ProfessorProfile ProfessorProfile = context.getData();
                if(ProfessorProfile != null)
                    setupViewer(ProfessorProfile);
                else
                    _logger.warn("No data for " + context);
                break;
            case selection:
                _logger.warn("Not viewing all profiles that match selection.");
                break;
            case search:
                _logger.warn("Not viewing all profiles that match search.");
                break;
        }
    }
}



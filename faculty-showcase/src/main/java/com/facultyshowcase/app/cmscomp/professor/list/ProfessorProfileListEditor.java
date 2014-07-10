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

package com.facultyshowcase.app.cmscomp.professor.list;

import com.i2rd.cms.miwt.LinkSelector;

import net.proteusframework.cms.component.editor.ContentBuilderBasedEditor;
import net.proteusframework.cms.component.editor.EditorUI;
import net.proteusframework.core.locale.LocalizedText;
import net.proteusframework.ui.miwt.component.Container;
import net.proteusframework.ui.miwt.component.Label;

/**
 * FIXME : Document this class.
 *
 * @author Jerry Maine (jmaine@venturetech.com)
 * @since 7/3/14 12:33 AM
 */
public class ProfessorProfileListEditor extends ContentBuilderBasedEditor<ProfessorProfileListContentBuilder>
{
    private LinkSelector linkSelector;

    /**
     * Constructor.
     *
     */
    public ProfessorProfileListEditor()
    {
        super(ProfessorProfileListContentBuilder.class);
    }

    @Override
    protected void _updateBuilder()
    {
        getBuilder().setUrl(linkSelector.getSelection());

    }

    @Override
    public void createUI(EditorUI editorUI)
    {
        // FIXME : Implement!
        super.createUI(editorUI);


        linkSelector = new LinkSelector();
        linkSelector.setExternalLinkOption(false);
        linkSelector.setSelection(getBuilder().getUrl());
        editorUI.addComponent(Container.of(new Label( new LocalizedText("Display Related Videos")),linkSelector));

    }
}

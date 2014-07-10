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

package com.facultyshowcase.app.cmscomp.professor.admin;

import com.facultyshowcase.app.ui.ProfessorProfileApp;

import javax.annotation.Nullable;

import com.i2rd.cms.bean.MIWTRenderingData;
import com.i2rd.cms.miwt.MIWTAppGenerator;

import net.proteusframework.cms.PageElement;
import net.proteusframework.cms.PageElementModelConfiguration;
import net.proteusframework.cms.category.Categorized;
import net.proteusframework.cms.category.CmsCategory;
import net.proteusframework.cms.component.AbstractContentElement;
import net.proteusframework.cms.component.editor.AnyComponentDelegationHelper;
import net.proteusframework.cms.component.generator.Generator;
import net.proteusframework.cms.controller.CmsRequest;
import net.proteusframework.core.locale.LocalizedObjectKey;
import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;

/**
 * FIXME : Document this class.
 *
 * @author Jerry Maine (jmaine@venturetech.com)
 * @since 7/2/14 11:26 AM
 */
@PageElementModelConfiguration(delegationHelper=AnyComponentDelegationHelper.class)
@Categorized(category= CmsCategory.WebContent)
@I18NFile(

    symbolPrefix = "ProfessorProfileAdminCMSBean",
    i18n = {
        @I18N(symbol = "content-element-name", l10n= @L10N("Professor Profile Admin")),
    }
)
public class ProfessorProfileAdminCMSBean extends AbstractContentElement
{

    @Nullable
    @Override
    public Generator<? extends PageElement> getGenerator(CmsRequest<? extends PageElement> request)
    {
        MIWTRenderingData rd = new MIWTRenderingData();
        rd.setApplicationClass(ProfessorProfileApp.class);
        return new MIWTAppGenerator(rd).setSessionDestroyParameter("restart");
    }

    @Override
    public LocalizedObjectKey getTypeKey()
    {
        return ProfessorProfileAdminCMSBeanLOK.CONTENT_ELEMENT_NAME();
    }
}

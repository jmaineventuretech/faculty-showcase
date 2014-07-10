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

import com.i2rd.cms.bean.contentmodel.CmsModelDataSet;

import net.proteusframework.cms.component.content.ContentBuilder;
import net.proteusframework.internet.http.Link;

/**
 * FIXME : Document this class.
 *
 * @author Jerry Maine (jmaine@venturetech.com)
 * @since 7/3/14 12:38 AM
 */
class ProfessorProfileListContentBuilder extends ContentBuilder<ProfessorProfileListContentBuilder.PropertyName >
{
    enum PropertyName {
        url,

    }

    public ProfessorProfileListContentBuilder() {
    }

    public static ProfessorProfileListContentBuilder load(final CmsModelDataSet cbds, final boolean useCache) {
        return load(cbds, ProfessorProfileListContentBuilder.class, useCache);
    }

    public Link getUrl() {

        return getLinkPropertyValue(ProfessorProfileListContentBuilder.PropertyName .url, null);
    }

    public void setUrl(Link url) {
        setLinkPropertyValue(ProfessorProfileListContentBuilder.PropertyName.url, url);
    }

}

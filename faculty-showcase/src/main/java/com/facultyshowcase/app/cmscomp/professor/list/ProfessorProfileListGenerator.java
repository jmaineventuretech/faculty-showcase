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

import com.facultyshowcase.app.model.ProfessorProfile;
import com.facultyshowcase.app.model.ProfessorProfileDAO;
import com.facultyshowcase.app.ui.UIUtil;
import org.apache.commons.collections.ComparatorUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.i2rd.cms.util.CmsHTMLClassNames;

import net.proteusframework.cms.PageElement;
import net.proteusframework.cms.PageElementPath;
import net.proteusframework.cms.component.generator.GeneratorImpl;
import net.proteusframework.cms.controller.CmsRequest;
import net.proteusframework.cms.controller.CmsResponse;
import net.proteusframework.cms.controller.LinkUtil;
import net.proteusframework.cms.controller.RenderChain;
import net.proteusframework.cms.dao.PageElementPathDAO;
import net.proteusframework.core.StringFactory;
import net.proteusframework.core.io.EntityUtilWriter;
import net.proteusframework.internet.http.Link;
import net.proteusframework.internet.http.ResponseURL;
import net.proteusframework.internet.http.resource.html.NDE;

/**
 * FIXME : Document this class.
 *
 * @author Jerry Maine (jmaine@venturetech.com)
 * @since 7/7/14 7:05 PM
 */
@Configurable
class ProfessorProfileListGenerator extends GeneratorImpl<ProfessorProfileListCMSBean>
{

    /** Logger. */
    private final static Logger _logger = Logger.getLogger(ProfessorProfileListGenerator.class);

    public static final String PROFESSOR_LINK = CmsHTMLClassNames.convertClassName("professor_link");

    @Autowired
    private ProfessorProfileDAO _ProfessorProfileDAO;


    @Override
    public List<NDE> getNDEs()
    {
        return Arrays.asList() ;
    }

    @Override
    public void render(CmsRequest<ProfessorProfileListCMSBean> request, CmsResponse response, RenderChain chain)
        throws IOException
    {
        //Partially inspired by com.i2rd.cms.generator.ArticleContainerGenerator code.

        EntityUtilWriter writer = response.getContentWriter();

        ProfessorProfileListContentBuilder bean = ProfessorProfileListContentBuilder.load(request.getBeanData(), true);

        Link url = bean.getUrl();


        PageElement pageElement = LinkUtil.getPageElement(url);
        PageElementPath pageElementPath =
            PageElementPathDAO.getInstance().getSinglePageElementPath(pageElement);

        //TODO add pagination and/or the ability to add a custom query to narrow the results.
        Collection<? extends ProfessorProfile> profiles =  _ProfessorProfileDAO.getProfessorProfiles();

        boolean first = true;
        for (ProfessorProfile profile : profiles) {
            if (first) {
                first = false;
            } else {
                writer.append("<br/>");
            }


            Link urlWithSlug = new Link(url);
            ResponseURL responceUrl = response.createURL(urlWithSlug);


            if (pageElementPath.isWildcard())
            {
                urlWithSlug.setPathInfo("/" + profile.getSlug());
            }
            else
            {
                responceUrl.addParameter("slug_id", profile.getSlug());
            }

            responceUrl.setClassName(UIUtil.PROFESSOR);
            responceUrl.outputOpeningAnchorTag(PROFESSOR_LINK);

            writer.append("<div ").appendEscapedAttribute("class", UIUtil.PROFESSOR).append(">");
            if (StringFactory.trimToNull(profile.getName().getFormOfAddress()) == null)
            {
                UIUtil.writeProperty(writer, UIUtil.PREFIX, profile.getName().getFormOfAddress());
                writer.append(" ");
            }
            UIUtil.writeProperty(writer, UIUtil.FIRST_NAME, profile.getName().getFirst());
                writer.append(" ");
            UIUtil.writeProperty(writer, UIUtil.LAST_NAME, profile.getName().getLast());

            if (StringFactory.trimToNull(profile.getName().getSuffix()) == null)
            {
                writer.append(" ");
                UIUtil.writeProperty(writer, UIUtil.SUFFIX, profile.getName().getSuffix());
            }
            writer.append("</div>");
            writer.append("</a>");
        }

    }


    protected void calculateDefaultExpireTime(CmsRequest<ProfessorProfileListCMSBean> request) {
        //
        setExpireTime(0);
    }


    public String getIdentity(CmsRequest<ProfessorProfileListCMSBean> request)
    {

        Date lastModifiedDate = (Date) ComparatorUtils.min(
            request.getPageElement().getLastModified(),
            _ProfessorProfileDAO.getLastModifiedDate(),
            ComparatorUtils.nullHighComparator(ComparatorUtils.naturalComparator()));

        return Long.toString(lastModifiedDate.getTime());

    }


}

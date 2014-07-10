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

package com.facultyshowcase.app.cmscomp.professor.viewer;

import com.facultyshowcase.app.model.ProfessorProfile;
import com.facultyshowcase.app.model.ProfessorProfileDAO;
import com.facultyshowcase.app.ui.UIUtil;
import org.apache.commons.collections.ComparatorUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import net.proteusframework.cms.component.generator.GeneratorImpl;
import net.proteusframework.cms.controller.CmsRequest;
import net.proteusframework.cms.controller.CmsResponse;
import net.proteusframework.cms.controller.ProcessChain;
import net.proteusframework.cms.controller.RenderChain;
import net.proteusframework.core.StringFactory;
import net.proteusframework.core.io.EntityUtilWriter;
import net.proteusframework.internet.http.resource.StringResource;
import net.proteusframework.internet.http.resource.html.NDE;
import net.proteusframework.internet.http.resource.html.NDEType;
import net.proteusframework.internet.http.resource.html.StringNDE;
import net.proteusframework.users.model.Address;

/**
 * FIXME : Document this class.
 *
 * @author Jerry Maine (jmaine@venturetech.com)
 * @since 7/7/14 6:18 PM
 */
@Configurable
class ProfessorProfileGenerator extends GeneratorImpl<ProfessorProfileCMSBean>
{
    /** Logger. */
    private final static Logger _logger = Logger.getLogger(ProfessorProfileGenerator.class);

    @Autowired
    private ProfessorProfileDAO _ProfessorProfileDAO;
    private List<NDE> _ndes;


    @Override
    public void preRenderProcess(CmsRequest<ProfessorProfileCMSBean> request, CmsResponse response, ProcessChain chain)
    {
        super.preRenderProcess(request, response, chain);

        StringBuilder b = new StringBuilder();

        b.append(".prop label{\n"
            + "  font-weight: bold;\n"
            + "  \n"
            + "}​\n"
            + "span.val{\n"
            + " \n"
            + "  margin: .3em\n"
            + "}​");
        _ndes = Arrays.<NDE>asList(new StringNDE(new StringResource(b.toString(),
            "text/css", "UTF-8", ProfessorProfileGenerator.class.getSimpleName() + ".css"), NDEType.CSS, false));

    }




    @Override
    public void render(CmsRequest<ProfessorProfileCMSBean> request, CmsResponse response, RenderChain chain) throws IOException
    {
        EntityUtilWriter writer = response.getContentWriter();

        String slug = getSlug(request);

        ProfessorProfile profile = null;
        try {
            profile = _ProfessorProfileDAO.getProfessorProfile(slug);
        } catch (Exception e) {
            //TODO do we want to print to the user that we had an error loading the profile
            _logger.error("error finding professor.", e);
        }

        if (profile != null) {

            UIUtil.writeProperty(writer, UIUtil.USER_ID, "UserId", profile.getSlug());

            UIUtil.writeProperty(writer, UIUtil.PREFIX, "Prefix", profile.getName().getFormOfAddress());
            UIUtil.writeProperty(writer, UIUtil.FIRST_NAME, "First Name", profile.getName().getFirst());
            UIUtil.writeProperty(writer, UIUtil.LAST_NAME, "Last Name", profile.getName().getLast());
            UIUtil.writeProperty(writer, UIUtil.SUFFIX, "Suffix", profile.getName().getSuffix());

            Address address = profile.getPostalAddress();
            String line1 = "";
            String line2 = "";
            if (address.getAddressLines().length > 0)
                line1 = address.getAddressLines()[0];
            if (address.getAddressLines().length > 1)
                line2 = address.getAddressLines()[1];


            UIUtil.writeProperty(writer, UIUtil.ADDRESS_LINE, "Address 1", line1);
            UIUtil.writeProperty(writer, UIUtil.ADDRESS_LINE, "Address 2", line2);


            UIUtil.writeProperty(writer, UIUtil.CITY, "City", profile.getPostalAddress().getCity());
            UIUtil.writeProperty(writer, UIUtil.STATE, "State", profile.getPostalAddress().getState());
            UIUtil.writeProperty(writer, UIUtil.POSTAL_CODE, "Postal Code (Zip)", profile.getPostalAddress().getPostalCode());

            UIUtil.writeProperty(writer, UIUtil.PHONE, "Phone", profile.getPhoneNumber());
            UIUtil.writeProperty(writer, UIUtil.EMAIL, "Email", profile.getEmailAddress());


            UIUtil.writeProperty(writer, UIUtil.FACEBOOK, "FaceBook", profile.getFacebookLink());
            UIUtil.writeProperty(writer, UIUtil.TWITTER, "Twitter", profile.getTwitterLink());
            UIUtil.writeProperty(writer, UIUtil.LINKEDIN, "LinkedIn", profile.getLinkedInLink());


            UIUtil.writePictureProperty(writer, UIUtil.PICTURE, "Picture", profile.getPicture(), "profile picture");
            UIUtil.writeProperty(writer, UIUtil.VIDEO, "Video", profile.getAboutMeVideoLink());
            UIUtil.writeHtmlProperty(writer, UIUtil.PROSE, "Hobbies, Interests...", profile.getAboutMeProse(), request, response);


            UIUtil.writeProperty(writer, UIUtil.DATE_JOINED, "Date Joined", profile.getDateJoined());
            UIUtil.writeProperty(writer, UIUtil.ON_SABBATICAL, "On Sabbatical", profile.isOnSabbatical());
            UIUtil.writeProperty(writer, UIUtil.RANK, "Professor Rank", profile.getProfessorRank());
            UIUtil.writeHtmlProperty(writer, UIUtil.PROSE, "Research Specialty", profile.getResearchSpecialty(), request, response);

        } else {
            UIUtil.writeProperty(writer, "professor_not_found", "Professor not found: ", slug );

            response.setStatusCode(404);
        }

    }

    private String getSlug(CmsRequest<ProfessorProfileCMSBean> request)
    {
        return request.getPageElementPath().isWildcard() ? StringUtils.stripStart(request.getPathInfo(), "/")
                : request.getParameter
                    ("slug_id");
    }

    protected void calculateDefaultExpireTime(CmsRequest<ProfessorProfileCMSBean> request) {
//
        setExpireTime(0);
    }


    @Override
    public String getIdentity(CmsRequest<ProfessorProfileCMSBean> request)
    {

        String slug = getSlug(request);

        if (StringFactory.trimToNull(slug) == null) {
            return super.getIdentity(request);
        }

        Date lastModifiedDate = (Date) ComparatorUtils.min(
            request.getPageElement().getLastModified(),
            _ProfessorProfileDAO.getLastModifiedDate(slug),
            ComparatorUtils.nullHighComparator(ComparatorUtils.naturalComparator()));

        return slug + " " + lastModifiedDate.getTime();

    }

    @Override
    public List<NDE> getNDEs()
    {
        return _ndes;
    }

}

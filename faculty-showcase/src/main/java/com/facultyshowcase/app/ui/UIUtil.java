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

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Whitelist;

import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.util.Date;

import com.i2rd.cms.util.CmsHTMLClassNames;

import net.proteusframework.cms.component.generator.XMLRenderer;
import net.proteusframework.cms.support.HTMLPageElementUtil;
import net.proteusframework.cms.support.ImageFileUtil;
import net.proteusframework.core.StringFactory;
import net.proteusframework.core.io.EntityUtilWriter;
import net.proteusframework.data.filesystem.FileEntity;
import net.proteusframework.internet.http.Request;
import net.proteusframework.internet.http.Response;
import net.proteusframework.internet.http.resource.FactoryBasedResource;

/**
 * FIXME : Document this class.
 *
 * @author Jerry Maine (jmaine@venturetech.com)
 * @since 7/7/14 3:50 PM
 */
public class UIUtil
{

    /** Logger. */
    private final static Logger _logger = Logger.getLogger(UIUtil.class);

    public static final String PROP = "prop";
    public static final String USER_ID = "userid";
    public static final String PREFIX = "prefix";
    public static final String FIRST_NAME = "given";
    public static final String LAST_NAME = "family";
    public static final String SUFFIX = "suffix";
    public static final String ADDRESS = "address";
    public static final String ADDRESS_LINES = ADDRESS + "_lines";
    public static final String ADDRESS_LINE = ADDRESS + "_line";
    public static final String CITY = "city";
    public static final String STATE = "state";
    public static final String POSTAL_CODE = "postal_code";
    public static final String PHONE = "phone";
    public static final String EMAIL = "email";
    public static final String SOCIAL = "social";
    public static final String TWITTER = "twitter";
    public static final String FACEBOOK = "facebook";
    public static final String LINKEDIN = "linkedin";
    public static final String ABOUT_ME = "about_me";
    public static final String PICTURE = "picture";
    public static final String PROSE = "prose";
    public static final String PROFESSIONAL_INFORMATION = "professional_information";
    public static final String RANK = "rank";
    public static final String DATE_JOINED = "date_joined";
    public static final String ON_SABBATICAL = "on_sabbatical";
    public static final String NAME = "name";
    public static final String PLACE = "place";
    public static final String PROP_GROUP = PROP + "_group";
    public static final String CONTACT = "contact";
    public static final String USER_ENTRY_REQUIRED = "user_entry_required";
    
    public static final String VIDEO = "video";
    public static final String PROFESSOR = "professor";
    public static final String CLASS = "class";


    public static void writeProperty(EntityUtilWriter writer, String cmsClass, String value)
    {
        writer.append("<span ");
        writer.appendEscapedAttribute(CLASS, CmsHTMLClassNames.convertClassName(cmsClass));
        writer.append(">");
        writer.appendEscapedData(value);
        writer.append("</span>");
    }

    public static void writeProperty(EntityUtilWriter writer, String cmsClass, String label, String namePart)
    {
        writeContainerBeginning(writer, cmsClass);

        writeLabel(writer, cmsClass, label);


        writer.append("<span ");
        writer.appendEscapedAttribute(CLASS, CmsHTMLClassNames.convertClassName("val"));
        writer.append(">");

        writer.appendEscapedData(namePart);
        writer.append("</span>");

        writeContainerEnd(writer);
    }

    private static void writeContainerEnd(EntityUtilWriter writer)
    {
        writer.append("</div>");
    }

    private static void writeContainerBeginning(EntityUtilWriter writer, String cmsClass)
    {
        writer.append("<div ").appendEscapedAttribute(CLASS, "prop " + cmsClass).append(">");
    }

    private static void writeLabel(EntityUtilWriter writer, String cmsClass, String label)
    {
//        String id = UUID.randomUUID().toString().replace("-", "").replace("-", "");

        writer.append("<label ");
//        writer.appendEscapedAttribute("for", id);
        writer.append(">");
        writer.appendEscapedData(label);
        writer.append("</label>");

//        return id;
    }


    public static void writeProperty(EntityUtilWriter writer, String htmlClass, String label, URL link)
    {
        writeContainerBeginning(writer, htmlClass);

        writeLabel(writer, htmlClass, label);

        if (link != null)
        {
            writer.append("<a ").appendEscapedAttribute("href", link.toString()).append(">");

            writer.append("<span ");
            writer.appendEscapedAttribute(CLASS, CmsHTMLClassNames.convertClassName(PROP + " " + htmlClass));
            writer.append(">");
            writer.appendEscapedData(StringUtils.substringAfter(link.toString(), "://"));
            writer.append("</span>");
            writer.append("</a>");
        } else {
            writer.append("<span ");
            writer.appendEscapedAttribute(CLASS, CmsHTMLClassNames.convertClassName(PROP + " " + htmlClass));
            writer.append(">");
            writer.append("</span>");
        }

        writeContainerEnd(writer);

    }

    public static void writeProperty(EntityUtilWriter writer, String htmlClass, String label, Date date)
    {
        final DateFormat parser = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
        
        writeContainerBeginning(writer, htmlClass);

        writeLabel(writer, htmlClass, label);


        writer.append("<span ");
        writer.appendEscapedAttribute(CLASS, CmsHTMLClassNames.convertClassName(PROP + " " + htmlClass));
        writer.append(">");
        writer.appendEscapedData(date != null ? parser.format(date) : StringUtils.EMPTY);
        writer.append("</span>");

        writeContainerEnd(writer);
    }

    public static void writeProperty(EntityUtilWriter writer, String htmlClass, String label,  boolean bool)
    {
        writeContainerBeginning(writer, htmlClass);

        writeLabel(writer, htmlClass, label);


        writer.append("<span ");
        writer.appendEscapedAttribute(CLASS, CmsHTMLClassNames.convertClassName(PROP + " " + htmlClass));
        writer.append(">");
        writer.appendEscapedData(BooleanUtils.toStringYesNo(bool));
        writer.append("</span>");

        writeContainerEnd(writer);
    }

    public static void writeProperty(EntityUtilWriter writer, String htmlClass, String label, Object object)
    {
        writeContainerBeginning(writer, htmlClass);

        writeLabel(writer, htmlClass, label);


        writer.append("<span ");
        writer.appendEscapedAttribute(CLASS, CmsHTMLClassNames.convertClassName(PROP + " " + htmlClass));
        writer.append(">");
        writer.appendEscapedData(ObjectUtils.toString(object, StringUtils.EMPTY));
        writer.append("</span>");

        writeContainerEnd(writer);


    }

    public static void writeHtmlProperty(EntityUtilWriter writer, String htmlClass, String label, String html, Request request,
        Response response)
    {
        writeContainerBeginning(writer, htmlClass);

        writeLabel(writer, htmlClass, label);


        writer.append("<div ");
        writer.appendEscapedAttribute(CLASS, CmsHTMLClassNames.convertClassName(PROP + " " + htmlClass));
        writer.append(">");

        writer.append(scrubHtml(html, request, response));
        writer.append("</div>");

        writeContainerEnd(writer);

    }




    public static void writePictureProperty(EntityUtilWriter writer, String htmlClass, String label, FileEntity picture, String alt)
    {
        writeContainerBeginning(writer, htmlClass);

        writeLabel(writer, htmlClass, label);


        if (ImageFileUtil.isFormatSupportedByImageIO(picture))
        {

            FactoryBasedResource resource = null;
            try
            {
                resource = ImageFileUtil.getResource(picture, 400, 200);
                String pictureUrl = HTMLPageElementUtil.getURL(resource);

                writer.append("<img ");
                writer.appendEscapedAttribute(CLASS, CmsHTMLClassNames.convertClassName(PROP + " " + htmlClass));
                writer.appendEscapedAttribute("src", pictureUrl);
                writer.append("/>");
            }
            catch (IOException e)
            {
                _logger.error("cannot load image", e);
            }

        }

        writeContainerEnd(writer);
    }
    
    public static String scrubHtml(String html, Request request, Response response) {
        if(!StringFactory.isEmptyString(html))
        {
            // Process the HTML converting links as necessary (adding JSESSIONID(s)
            /// for URL based session tracking, converting resource links to increase concurrent loading limit,
            /// CMS link externalization, etc).
            /// This is *not* sanitation and should always be done before rendering - never before persisting.
            /// We are doing this before sanitizing the HTML to avoid having to whitelist internal URL protocols, etc.

            try
            {
                html = XMLRenderer.parseWithRoot(html, request, response);
            }
            catch (IOException e)
            {
                _logger.error("Unable to accept HTML: " + html, e);
            }

            // We don't trust the input, so we sanitize it with a whitelist of allowed HTML.
            Document dirty = Jsoup.parseBodyFragment(html, "");
            Whitelist whitelist = Whitelist.relaxed();
            // Don't allow users to use our website as a link farm
            whitelist.addEnforcedAttribute("a", "rel", "nofollow");
            Cleaner cleaner = new Cleaner(whitelist);
            Document clean = cleaner.clean(dirty);
            html = clean.html();
            
            return html;
        } else {
            return "";
        }
    }

}

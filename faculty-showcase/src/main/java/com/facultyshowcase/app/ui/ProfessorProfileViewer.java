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
import com.google.common.base.Preconditions;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.net.URI;
import java.net.URL;
import java.rmi.RemoteException;
import java.text.DateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.i2rd.media.ICodec;
import com.i2rd.media.IMediaMetaData;
import com.i2rd.media.IMediaStream;
import com.i2rd.media.IMediaUtility;
import com.i2rd.media.MediaUtilityFactory;

import net.proteusframework.core.StringFactory;
import net.proteusframework.core.hibernate.dao.EntityRetriever;
import net.proteusframework.core.html.HTMLElement;
import net.proteusframework.core.locale.TextSources;
import net.proteusframework.core.metric.PixelMetric;
import net.proteusframework.data.filesystem.FileEntity;
import net.proteusframework.ui.miwt.Image;
import net.proteusframework.ui.miwt.component.Component;
import net.proteusframework.ui.miwt.component.Container;
import net.proteusframework.ui.miwt.component.Field;
import net.proteusframework.ui.miwt.component.HTMLComponent;
import net.proteusframework.ui.miwt.component.ImageComponent;
import net.proteusframework.ui.miwt.component.Label;
import net.proteusframework.ui.miwt.component.Media;
import net.proteusframework.ui.miwt.component.URILink;
import net.proteusframework.ui.miwt.data.MediaSource;
import net.proteusframework.ui.miwt.event.Event;
import net.proteusframework.users.model.Address;
import net.proteusframework.users.model.Name;

/**
 * UI view for ProfessorProfile.
 * @author Russ Tennant (russ@venturetech.net)
 */
@Configurable
public class ProfessorProfileViewer extends Container
{
    /** Logger. */
    private final static Logger _logger = Logger.getLogger(ProfessorProfileViewer.class);
    /** Profile. */
    private final ProfessorProfile _ProfessorProfile;

    /** DAO. */
    @Autowired
    private ProfessorProfileDAO _ProfessorProfileDAO;

    /**
     * Create a new viewer.
     * @param profile the profile.
     */
    public ProfessorProfileViewer(ProfessorProfile profile)
    {
        super();
        Preconditions.checkNotNull(profile);
        _ProfessorProfile = profile;
    }

    @Override
    public void init()
    {
        // Make sure you call super.init() at the top of this method.
        /// See the Javadoc for #init() for more information about what it does.
        super.init();

        // Set HTML element type and class names for presentation use on this Container component.
        setHTMLElement(HTMLElement.section);
        addClassName("user-profile-viewer");
        // property_viewer is a standard class name.
        addClassName(UIUtil.PROP + "erty-viewer");
        // Add microdata for programmatic / SEO use
        /// OR use RDFa support
        /// You typically only do this in viewers - not editors.
        setAttribute("itemscope", "");
        setAttribute("itemtype", "http://schema.org/Person");
        // setAttribute allows you to set any attribute as long as it will not interfere with a component's
        /// native HTML. For example, you cannot set the "value" attribute on a Field since
        /// it uses that attribute.

        // It's a good idea to *not* mark variables final that you don't want in the scope of event listeners.
        /// Hibernate/JPA entities are a great example of this pattern. You always need to re-attach
        /// entities before using them, so we should always call getProfessorProfile() in the context
        /// of handling an event. Note: our getProfessorProfile() method re-attaches the entity.
        ProfessorProfile ProfessorProfile = getProfessorProfile();

        Name name = ProfessorProfile.getName();
        // You can use a Field for displaying non-internationalized content.
        /// It is desirable to do this since you don't need to create a LocalizedText.
        /// However, you cannot change the HTMLElement of a Field at this time,
        /// so some of the following code uses a Label which does allow
        /// specification of the HTMLElement.
        final Field slug = new Field (ProfessorProfile.getSlug(), false);
        final Field namePrefix = new Field(name.getFormOfAddress(), false);
        final Field nameGiven = new Field(name.getFirst(), false);
        final Field nameFamily = new Field(name.getLast(), false);
        final Field nameSuffix = new Field(name.getSuffix(), false);
        // Sometimes it is easier and less error prone to make a component non-visible
        /// than checking for null on each use. Use this pattern with care. You don't
        /// want to consume a lot of resource unnecessarily.
        if(StringFactory.isEmptyString(namePrefix.getText())) namePrefix.setVisible(false);
        if(StringFactory.isEmptyString(nameSuffix.getText())) nameSuffix.setVisible(false);

        // Address
        Address address = ProfessorProfile.getPostalAddress();
        // Address lines are always on their own line so we make sure they are enclosed by a block element like a DIV..
        final Label addressLine1 = new Label();
        addressLine1.setHTMLElement(HTMLElement.div).addClassName(UIUtil.PROP).addClassName(UIUtil.ADDRESS_LINE);
        final Label addressLine2 = new Label();
        addressLine2.setHTMLElement(HTMLElement.div).addClassName(UIUtil.PROP).addClassName(UIUtil.ADDRESS_LINE);
        if(address.getAddressLines().length > 0) addressLine1.setText(TextSources.create(address.getAddressLines()[0]));
        if(address.getAddressLines().length > 1) addressLine2.setText(TextSources.create(address.getAddressLines()[1]));
        final HTMLComponent city = new HTMLComponent();
        // The "prop" class name is part of the standard HTML structure. It is always a good idea to also
        /// add a specific class name like "city" in this example. Please be consistent when using class names.
        /// For example, if everyone else is using "city", please use "city" too. Don't come up with another class name
        /// that means something similar like "town" or "locality". Consistency has a big impact on
        /// the time required to style HTML as well as the ability to reuse CSS.
        city.setHTMLElement(HTMLElement.span).addClassName(UIUtil.PROP).addClassName(UIUtil.CITY);
        if(!StringFactory.isEmptyString(address.getCity()))
        {
            // Our microdata for the city shouldn't include the comma, so this is a bit more complicated than the other examples.
            city.setText(TextSources.create("<span item" + UIUtil.PROP + "=\"" + UIUtil.ADDRESS + "Locality\">" + address.getCity()
                + "</span><span class=\"delimiter\">,</span>"));
        }
        else city.setVisible(false);
        final Label state = new Label(TextSources.create(address.getState()));
        state.addClassName(UIUtil.PROP).addClassName(UIUtil.STATE);
        final Label postalCode = new Label(TextSources.create(address.getPostalCode()));
        postalCode.addClassName(UIUtil.PROP).addClassName(UIUtil.POSTAL_CODE);

        // Other Contact
        final Field phoneNumber = new Field(ProfessorProfile.getPhoneNumber(), false);
        final Field emailAddress = new Field(ProfessorProfile.getEmailAddress(), false);

        // Social Contact
        final URILink twitterLink = ProfessorProfile.getTwitterLink() != null
            ? new URILink(_ProfessorProfileDAO.toURI(ProfessorProfile.getTwitterLink(), null)) : null;
        final URILink facebookLink = ProfessorProfile.getFacebookLink() != null
            ? new URILink(_ProfessorProfileDAO.toURI(ProfessorProfile.getFacebookLink(), null)) : null;
        final URILink linkedInLink = ProfessorProfile.getLinkedInLink() != null
            ? new URILink(_ProfessorProfileDAO.toURI(ProfessorProfile.getLinkedInLink(), null)) : null;

        // We are going to output HTML received from the outside, so we need to sanitize it first for security reasons.
        /// Sometimes you'll do this sanitation prior to persisting the data. It depends on whether or not you need to
        /// keep the original unsanitized HTML around.
        final HTMLComponent aboutMeProse = new HTMLComponent(UIUtil.scrubHtml(ProfessorProfile.getAboutMeProse(),
            Event.getRequest(), Event.getResponse()));
        Component aboutMeVideo = null;
        URL videoLink = ProfessorProfile.getAboutMeVideoLink();
        if(videoLink != null)
        {
            // There are several ways to link to media (Youtube video URL, Vimeo video URL, Flickr URL, internally hosted media file, etc).
            /// You can link to it.
            /// You can embed it. See http://oembed.com/ for a common protocol for doing this.
            /// If the link is to the media itself, you can create a player for it.
            /// Below is an example of creating a link to the video as well as a player.
            final URI videoLinkURI = _ProfessorProfileDAO.toURI(videoLink, null);
            URILink videoLinkComponent = new URILink(videoLinkURI, TextSources.create("My Video"));
            videoLinkComponent.setTarget("_blank");
            IMediaUtility util = MediaUtilityFactory.getUtility();
            try
            {
                // Check if we can parse the media and it has a stream we like.
                /// In our made up example, we're only accepting H.264 video. We don't care about the audio in this example.
                IMediaMetaData mmd;
                if(util.isEnabled()
                    && videoLinkURI != null
                    && (mmd=util.getMetaData(videoLinkURI.toString())).getStreams().length > 0)
                {
                    int width = 853, height = 480; // 480p default
                    boolean hasVideo = false;
                    for(IMediaStream stream : mmd.getStreams())
                    {
                        if(stream.getCodec().getType() == ICodec.Type.video
                            && "H264".equals(stream.getCodec().name()))
                        {
                            hasVideo = true;
                            if(stream.getWidth() > 0)
                            {
                                width = stream.getWidth();
                                height = stream.getHeight();
                            }
                            break;
                        }
                    }
                    if(hasVideo)
                    {
                        Media component = new Media();
                        component.setMediaType(Media.MediaType.video);
                        component.addSource(new MediaSource(videoLinkURI));
                        component.setFallbackContent(videoLinkComponent);
                        component.setSize(new PixelMetric(width), new PixelMetric(height));
                        aboutMeVideo = component;
                    }
                }
            }
            catch(IllegalArgumentException | RemoteException e)
            {
                _logger.error("Unable to get media information for " + videoLink, e);
            }
            if(aboutMeVideo == null)
            {
                // We could check for oEmbed support in case link was to youtube, vimeo, etc - http://oembed.com/
                // Since this is an example, we'll just output the link.
                aboutMeVideo = videoLinkComponent;
            }
        }
        ImageComponent picture = null;
        final FileEntity ProfessorProfilePicture = ProfessorProfile.getPicture();
        if(ProfessorProfilePicture != null)
        {
            picture = new ImageComponent(new Image(ProfessorProfilePicture));
            picture.setImageCaching(ProfessorProfilePicture.getLastModifiedTime().before(
                new Date(System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(60))
            ));
        }


        // Professional Information

        // We are going to output HTML received from the outside, so we need to sanitize it first for security reasons.
        /// Sometimes you'll do this sanitation prior to persisting the data. It depends on whether or not you need to
        /// keep the original unsanitized HTML around.
        final HTMLComponent researchSpecialty = new HTMLComponent(UIUtil.scrubHtml(ProfessorProfile.getAboutMeProse(),
            Event.getRequest(), Event.getResponse()));


        final Field rank = ProfessorProfile.getProfessorRank() != null ? new Field(ObjectUtils.toString(ProfessorProfile
        .getProfessorRank()), false) : null;
        final DateFormat parser = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
        final Field dateJoined = ProfessorProfile.getDateJoined() != null ? new Field(parser.format(ProfessorProfile.getDateJoined
        ()), false) : null;
        final Field onSabbatical = new Field(BooleanUtils.toStringYesNo(ProfessorProfile.isOnSabbatical()),false);

        // Now that we've initialized most of the content, we'll add all the components to this View
        /// using the standard HTML structure for a property viewer.
        add(of(HTMLElement.section,
            UIUtil.PROP + "-group " + UIUtil.NAME,
            new Label(TextSources.create("Name")).setHTMLElement(HTMLElement.h1),
            slug.setAttribute("item" + UIUtil.PROP, UIUtil.USER_ID)
                .addClassName(UIUtil.PROP).addClassName(UIUtil.USER_ID),
            namePrefix.setAttribute("item" + UIUtil.PROP, "honorificPrefix")
                .addClassName(UIUtil.PROP).addClassName(UIUtil.PREFIX),
            nameGiven.setAttribute("item" + UIUtil.PROP, "givenName")
                .addClassName(UIUtil.PROP).addClassName(UIUtil.FIRST_NAME),
            nameFamily.setAttribute("item" + UIUtil.PROP, "familyName")
                .addClassName(UIUtil.PROP).addClassName(UIUtil.LAST_NAME),
            nameSuffix.setAttribute("item" + UIUtil.PROP, "honorificSuffix")
                .addClassName(UIUtil.PROP).addClassName(UIUtil.SUFFIX)
        ));

        // Add wrapping DIV to group address lines if necessary.
        Component streetAddress = (!StringFactory.isEmptyString(addressLine1.getText()) && !StringFactory.isEmptyString(addressLine2.getText())
            ? of(HTMLElement.div, UIUtil.ADDRESS_LINES, addressLine1, addressLine2)
            : (StringFactory.isEmptyString(addressLine1.getText()) ? addressLine2 : addressLine1).setHTMLElement(HTMLElement.div));
        streetAddress.setAttribute("item" + UIUtil.PROP, "streetAddress");
        boolean hasAddress = (!StringFactory.isEmptyString(addressLine1.getText())
            ||!StringFactory.isEmptyString(addressLine2.getText())
            ||!StringFactory.isEmptyString(city.getText())
            ||!StringFactory.isEmptyString(state.getText())
            ||!StringFactory.isEmptyString(postalCode.getText())
        );
        boolean hasPhone = !StringFactory.isEmptyString(phoneNumber.getText());
        boolean hasEmail = !StringFactory.isEmptyString(emailAddress.getText());
        // We only want to output the enclosing HTML if we have content to display.
        if(hasAddress || hasPhone || hasEmail)
        {
            Container contactContainer = of(HTMLElement.section,
                "contact",
                new Label(TextSources.create("Contact Information")).setHTMLElement(HTMLElement.h1)
            );
            add(contactContainer);
            if(hasAddress)
            {
                contactContainer.add(of(HTMLElement.div,
                        UIUtil.PROP_GROUP + " " + UIUtil.ADDRESS,
                        // We are using an H2 here because are immediate ancestor is a DIV. If it was a SECTION,
                        /// then we would use an H1. See the ProfessorProfileViewer for a comparison.
                        new Label(TextSources.create("Address")).setHTMLElement(HTMLElement.h2),
                        streetAddress,
                        of(HTMLElement.div, UIUtil.PLACE,
                            city,
                            state.setAttribute("item" + UIUtil.PROP, UIUtil.ADDRESS + "Region"),
                            postalCode.setAttribute("item" + UIUtil.PROP, "postalCode"))
                    ).setAttribute("item" + UIUtil.PROP, UIUtil.ADDRESS)
                        .setAttribute("itemscope", "")
                        .setAttribute("itemtype", "http://schema.org/PostalAddress")
                );
            }
            if(hasPhone)
            {
                contactContainer.add(of(HTMLElement.div,
                        UIUtil.PROP + " " + UIUtil.PHONE,
                        new Label(TextSources.create("Phone")).setHTMLElement(HTMLElement.h2),
                        phoneNumber.setAttribute("item" + UIUtil.PROP, "telephone")
                        )
                );
            }
            if(hasEmail)
            {
                contactContainer.add(of(HTMLElement.div,
                        UIUtil.PROP + " " + UIUtil.EMAIL,
                        new Label(TextSources.create("Email")).setHTMLElement(HTMLElement.h2),
                        emailAddress.setAttribute("item" + UIUtil.PROP, UIUtil.EMAIL)
                        )
                );
            }
        }


        if(twitterLink != null || facebookLink != null || linkedInLink != null)
        {
            Container social = of(
                HTMLElement.section,
                UIUtil.SOCIAL,
                new Label(TextSources.create("Social Media Links")).setHTMLElement(HTMLElement.h1)
            );
            add(social);
            if(twitterLink != null)
            {
                twitterLink.setTarget("_blank");
                twitterLink.setText(TextSources.create("Twitter Link"));
                social.add(of(
                    HTMLElement.div,
                    UIUtil.PROP + " " + UIUtil.TWITTER,
                    TextSources.create("Twitter"),
                    twitterLink
                ));
            }
            if(facebookLink != null)
            {
                facebookLink.setTarget("_blank");
                facebookLink.setText(TextSources.create("Facebook Link"));
                social.add(of(
                    HTMLElement.div,
                    UIUtil.PROP + " " + UIUtil.FACEBOOK,
                    TextSources.create("Facebook"),
                    facebookLink
                ));
            }
            if(linkedInLink != null)
            {
                linkedInLink.setTarget("_blank");
                linkedInLink.setText(TextSources.create("LinkedIn Link"));
                social.add(of(
                    HTMLElement.div,
                    UIUtil.PROP + " " + UIUtil.LINKEDIN,
                    TextSources.create("LinkedIn"),
                    linkedInLink
                ));
            }
        }

        final boolean hasAboutMeProse = StringFactory.isEmptyString(aboutMeProse.getText());
        if(!hasAboutMeProse || aboutMeVideo != null)
        {
            Container aboutMe = of(
                HTMLElement.section,
                UIUtil.ABOUT_ME,
                new Label(TextSources.create("About Me")).setHTMLElement(HTMLElement.h1)
            );
            add(aboutMe);
            if(picture != null)
            {
                aboutMe.add(of(
                    HTMLElement.div,
                    UIUtil.PROP + " " + UIUtil.PICTURE,
                    TextSources.create("Picture"),
                    picture
                ));
            }
            if(hasAboutMeProse)
            {
                aboutMe.add(of(
                    HTMLElement.div,
                    UIUtil.PROP + " " + UIUtil.PROSE,
                    TextSources.create("Hobbies, Interests..."),
                    aboutMeProse
                ));
            }
            if(aboutMeVideo != null)
            {
                Label label = new Label(TextSources.create("Video")).setHTMLElement(HTMLElement.label);
                label.addClassName("vl");
                aboutMe.add(of(
                    HTMLElement.div,
                    UIUtil.PROP + " " + UIUtil.VIDEO,
                    label,
                    aboutMeVideo
                ));
            }

        }


        final boolean hasResearchSpecialty = StringFactory.isEmptyString(researchSpecialty.getText());
        if(!hasResearchSpecialty || rank != null || dateJoined != null || onSabbatical != null)
        {
            Container professionalInformation = of(
                HTMLElement.section,
                UIUtil.PROFESSIONAL_INFORMATION,
                new Label(TextSources.create("Professional Information")).setHTMLElement(HTMLElement.h1)
            );
            add(professionalInformation);
            if(rank != null)
            {
                professionalInformation.add(of(
                    HTMLElement.div,
                    UIUtil.PROP + " " + UIUtil.RANK,
                    TextSources.create("Professor Rank"),
                    rank
                ));
            }
            if(dateJoined != null)
            {
                Label label = new Label(TextSources.create("Date Joined")).setHTMLElement(HTMLElement.label);
                professionalInformation.add(of(
                    HTMLElement.div,
                    UIUtil.PROP + " " + UIUtil.DATE_JOINED,
                    label,
                    dateJoined
                ));
            }
            if(onSabbatical != null)
            {
                Label label = new Label(TextSources.create("On Sabbatical")).setHTMLElement(HTMLElement.label);
                professionalInformation.add(of(
                    HTMLElement.div,
                    UIUtil.PROP + " " + UIUtil.ON_SABBATICAL,
                    label,
                    onSabbatical
                ));
            }
            if(hasResearchSpecialty)
            {
                professionalInformation.add(of(
                    HTMLElement.div,
                    UIUtil.PROP + " " + UIUtil.PROSE,
                    TextSources.create("Research Specialty"),
                    aboutMeProse
                ));
            }
        }
    }

    /**
     * Get the user profile attached to the Hibernate Session or EntityManager.
     * @return the attached profile.
     */
    ProfessorProfile getProfessorProfile()
    {
        // Since we aren't actually persisting anything, this doesn't do anything other than return _ProfessorProfile
        /// It's just meant to demonstrate how to do it when you are using entities persisted in data store.
        return EntityRetriever.getInstance().reattachIfNecessary(_ProfessorProfile);
    }
}

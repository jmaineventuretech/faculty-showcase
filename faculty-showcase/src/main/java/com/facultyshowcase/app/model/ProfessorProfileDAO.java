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

package com.facultyshowcase.app.model;

import com.facultyshowcase.app.model.helpers.WithTransaction;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.jetbrains.annotations.Contract;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Repository;

import javax.annotation.Nullable;
import javax.mail.internet.ContentType;
import javax.mail.internet.ParseException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Date;

import net.proteusframework.cms.CmsSite;
import net.proteusframework.cms.FileSystemDirectory;
import net.proteusframework.core.hibernate.dao.DAOHelper;
import net.proteusframework.core.hibernate.dao.EntityRetriever;
import net.proteusframework.core.lang.CloseableIterator;
import net.proteusframework.core.net.ContentTypes;
import net.proteusframework.data.filesystem.DirectoryEntity;
import net.proteusframework.data.filesystem.FileEntity;
import net.proteusframework.data.filesystem.FileSystemDAO;
import net.proteusframework.data.filesystem.FileSystemEntityCreateMode;
import net.proteusframework.data.filesystem.TemporaryFileEntity;
import net.proteusframework.ui.search.QLBuilder;
import net.proteusframework.ui.search.QLResolver;
import net.proteusframework.ui.search.QLResolverOptions;

/**
 * DAO for user profile.
 *
 * @author Russ Tennant (russ@i2rd.com)
 */
@Configurable
@Repository()
public class ProfessorProfileDAO extends DAOHelper
{
    /** Default resource name. */
    public static final String RESOURCE_NAME = "FormDAO";

    /** Logger. */
    private final static Logger _logger = Logger.getLogger(ProfessorProfileDAO.class);

    /** FileSystem DAO. */
    @Autowired
    private FileSystemDAO _fileSystemDAO;




    public ProfessorProfileDAO() {
        _logger.trace("Started");
    }

    /**
     * Get the file extension.
     *
     * @param file the file.
     * @return the extension preceeded by a '.' or an empty string.
     */
    private static String _getFileExtensionWithDot(FileEntity file)
    {
        String ext = ""; // Cannot use the file name since the file may have been converted.
        final String contentType = file.getContentType();
        if (ext.isEmpty() && !ContentTypes.Application.octet_stream.toString().equals(contentType))
        {
            try
            {
                ext = new ContentType(contentType).getSubType().toLowerCase();
                switch(ext)
                {
                    case "jpeg":
                        ext = "jpg";
                        break;
                    case "tiff":
                        ext = "tif";
                        break;
                    case "svg+xml":
                        ext = "svg";
                        break;
                    case "x-portable-anymap":
                        ext = "pnm";
                        break;
                    case "x-portable-bitmap":
                        ext = "pbm";
                        break;
                    case "x-portable-graymap":
                        ext = "pgm";
                        break;
                    case "x-portable-pixmap":
                        ext = "ppm";
                        break;
                    default:break;
                }
            }
            catch (ParseException e)
            {
                _logger.error("Unable to parse content type: " + contentType, e);
            }
        }
        return ext.isEmpty() ? ext : '.' + ext;
    }

    /**
     * Save ProfessorProfile.
     *
     * @param ProfessorProfile the user profile to save.
     */
    @WithTransaction
    public void saveProfessorProfile(ProfessorProfile ProfessorProfile)
    {
        try
        {
            final long id = ProfessorProfile.getId();
            String name = ProfessorProfile.getName().getLast() + ", " + ProfessorProfile.getName().getFirst();
            String pictureName = name + " #" + id;
            final Session session = getSession();
            FileEntity picture = ProfessorProfile.getPicture();
            if (picture != null)
            {
                pictureName += _getFileExtensionWithDot(picture);
                // Ensure our picture file has a unique file name consistent with the profile.
                if (picture.getId() < 1)
                {
                    final CmsSite site = ProfessorProfile.getSite();
                    final DirectoryEntity rootDirectory = FileSystemDirectory.getRootDirectory(site);
                    DirectoryEntity parentDirectory = _fileSystemDAO.mkdirs(rootDirectory, null, "ProfessorProfilePictures");
                    picture.setName(pictureName);
                    picture = _fileSystemDAO.newFile(parentDirectory, picture, FileSystemEntityCreateMode.truncate);
                    ProfessorProfile.setPicture(picture);
                }
                else if(picture instanceof TemporaryFileEntity)
                {
                    TemporaryFileEntity tfe  = (TemporaryFileEntity) picture;
                    EntityRetriever er = EntityRetriever.getInstance();
                    picture = er.reattachIfNecessary(tfe.getFileEntity());
                    picture.setName(pictureName);
                    _fileSystemDAO.update(picture);
                    _fileSystemDAO.setStream(picture, tfe.getStream(), true);
                    ProfessorProfile.setPicture(picture); // In case we are cascading.
                    tfe.deleteStream();
                }
            }

            ProfessorProfile.setLastModTime(new Date());
            if (isTransient(ProfessorProfile) || isAttached(ProfessorProfile))
                session.saveOrUpdate(ProfessorProfile);
            else
                session.merge(ProfessorProfile);

            if (picture != null && id == 0)
            {
                // New user profile. Update picture name to include the ID
                pictureName = name + " #" + ProfessorProfile.getId() + _getFileExtensionWithDot(picture);
                picture.setName(pictureName);
                _fileSystemDAO.update(picture);
            }

        }
        catch (IOException ioe)
        {
            throw new RuntimeException("Unable to access filesystem.", ioe);
        }
    }

    /**
     * Delete the specified user profile.
     *
     * @param ProfessorProfile the user profile to delete.
     */
    @WithTransaction
    public void deleteProfessorProfile(ProfessorProfile ProfessorProfile)
    {
        final Session session = getSession();
        session.delete(ProfessorProfile);
    }

    /**
     * Delete the specified user profiles.
     *
     * @param ProfessorProfiles the user profiles to delete.
     */
    public void deleteProfessorProfiles(Collection<? extends ProfessorProfile> ProfessorProfiles)
    {
        final Session session = getSession();
        ProfessorProfiles.forEach(session::delete);
    }

    /**
     * Delete the specified user profiles.
     *
     * @param qlBuilder a QL builder that will return ProfessorProfiles to delete.
     */
    @WithTransaction
    public void deleteProfessorProfiles(QLBuilder qlBuilder)
    {

        final Session session = getSession();
        final QLResolver queryResolver = qlBuilder.getQueryResolver();
        final QLResolverOptions options = new QLResolverOptions();
        final int atATime = 200;
        options.setFetchSize(atATime);
        queryResolver.setOptions(options);
        try (CloseableIterator<ProfessorProfile> it = queryResolver.iterate())
        {
            int count = 0;
            while (it.hasNext())
            {
                ProfessorProfile ProfessorProfile = it.next();
                session.delete(ProfessorProfile);
                if (++count > atATime)
                {
                    count = 0;
                    session.flush(); // May need to clear action queues as well to free up memory.
                }
            }
        }
        catch (Exception e)
        {
            throw new HibernateException("Unable to iterate over query results.", e);
        }

    }

    /**
     * Evict the specified entity from the session.
     *
     * @param entity the entity.
     */
    public void evict(Object entity)
    {
        getSession().evict(entity);
    }

    /**
     * Convert a URL to a URI returning the default value on error.
     *
     * @param url the URL.
     * @param defaultValue the default value.
     * @return the URI.
     */
    @Contract("_,null->null;_,!null->!null")
    public URI toURI(URL url, URI defaultValue)
    {
        if (url == null) return defaultValue;
        try
        {
            return url.toURI();
        }
        catch (URISyntaxException e)
        {
            _logger.warn("Unable to convert URL to URI: " + url, e);
        }
        return defaultValue;
    }

    /**
     * Convert a URL to a string if possible or return null.
     *
     * @param link the link.
     * @return the link as a String or null.
     */
    @Nullable
    @Contract("null->null;!null->!null")
    public String toString(@Nullable URL link)
    {
        return link == null ? null : link.toString();
    }

    @WithTransaction
    public Collection<? extends ProfessorProfile> getProfessorProfiles()
    {
        Collection<? extends  ProfessorProfile> ret = null;

        final Session session = getSession();


        ret = session.createQuery("from " + ProfessorProfile.class.getName()).list();


        return ret;
    }


    @WithTransaction
    public ProfessorProfile getProfessorProfile(String slug) {
        return (ProfessorProfile) getSession()
            .createQuery("from " + ProfessorProfile.class.getName() + " p where p.slug = :slug")
            .setParameter("slug", slug)
            .uniqueResult();
    }

    @WithTransaction
    public Date getLastModifiedDate(String slug)
    {
        return (Date) getSession()
            .createQuery("select p.lastModTime from " + ProfessorProfile.class.getName() + " p where p.slug = :slug")
            .setParameter("slug", slug)
            .uniqueResult();

    }

    @WithTransaction
    public Date getLastModifiedDate()
    {
        return (Date) getSession()
            .createQuery("select min(p.lastModTime) from " + ProfessorProfile.class.getName() + " p ")
            .uniqueResult();

    }
}

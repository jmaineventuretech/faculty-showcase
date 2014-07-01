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

package com.facultyshowcase.app.config.automation;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.util.ArrayList;
import java.util.List;

import net.proteusframework.core.automation.DataConversion;
import net.proteusframework.core.automation.SQLDataConversion;
import net.proteusframework.core.automation.SQLStatement;
import net.proteusframework.core.automation.TaskQualifier;

// You can also put your code in the groovy source folder so you can more easily create
//  mult-line string literals. Don't duplicate the package-info.java though - only create one
//  for the same package.

/**
 * Data conversions for major version 1 of example app module
 * in the Proteus Framework project.
 *
 * @author Russ Tennant (russ@i2rd.com)
 */
@Configuration
//@Profile({"automation", "com.example.1"})
@Lazy
public class ProfessorShowcaseDataConversionVersion1
{
    // !!! PLEASE PUT THE LATEST conversion at the top so it is easy to determine the next version #
    /** Data Conversion Identifier. */
    private static final String IDENTIFIER = "faculty-app";


    /**
     * User Profile Entity
     *
     * @return Bean.
     */
    @TaskQualifier(TaskQualifier.Type.data_conversion)
    @Bean
    public DataConversion ProfessorProfileEntityDataConversion()
    {
        List<SQLStatement> ddl = new ArrayList<>();
        ddl.add(new SQLStatement(
            "create table ProfessorProfile (professor_profile_id int8 not null, aboutmeprose varchar(4000), aboutmevideolink varchar(255), "
                + "createtime timestamp, emailaddress varchar(255), facebooklink varchar(255), lastmodtime timestamp, "
                + "linkedinlink varchar(255), phonenumber varchar(255), twitterlink varchar(255), name_id int8, picture_id int8, "
                + "postaladdress_id int8, site_id int8, primary key (professor_profile_id))",
            null));
        ddl.add(new SQLStatement(
            "alter table professor_profile add constraint FK_mrdqdc1atkriccxn00383la4u foreign key (name_id) references personname",
            null));
        ddl.add(new SQLStatement(
            "alter table professor_profile add constraint FK_njox9f67ws67qbuo2x8mv6qyu foreign key (picture_id) references "
                + "FileSystemEntity", null ));
        ddl.add(new SQLStatement(
            "alter table professor_profile add constraint FK_3s1hv4k1gnvfe6j2fqtsjcj4f foreign key (postaladdress_id) references address",
            null));
        ddl.add(new SQLStatement(
            "alter table professor_profile add constraint FK_l51qbba4pla782w3b862hhxl7 foreign key (site_id) references site", null));
        ddl.add(new SQLStatement("create sequence ProfessorProfile_seq", null));

        return SQLDataConversion.createSchemaUpdate("faculty-app", "Professor Profile Entity", 3, false, ddl);
    }



}

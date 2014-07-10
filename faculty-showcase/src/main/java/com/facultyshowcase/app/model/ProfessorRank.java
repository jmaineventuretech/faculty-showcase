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

/**
 * FIXME : Document this class.
 *
 * @author Jerry Maine (jmaine@venturetech.com)
 * @since 7/2/14 6:19 PM
 */
public enum ProfessorRank
{
    Lecturer,
    AdjunctProfessor {
        @Override
        public String toString()
        {
            return "Adjunct Professor";
        }
    },
    AssistantProfessor{
        public String toString()
        {
            return "Assistant Professor";
        }
    },
    AssociateProfessor{
        public String toString()
        {
            return "Associate Professor";
        }
    },
    Professor
}

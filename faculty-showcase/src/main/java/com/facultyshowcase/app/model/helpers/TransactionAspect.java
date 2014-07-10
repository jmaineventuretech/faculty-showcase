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

package com.facultyshowcase.app.model.helpers;

import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import java.lang.reflect.Method;

import net.proteusframework.core.hibernate.dao.DAOHelper;

/**
 * Don't want to hand roll transaction logic every time....
 *
 * @author Jerry Maine (jmaine@venturetech.com)
 * @since 7/3/14 7:04 AM
 */
@Aspect
public class TransactionAspect
{

    /** Logger. */
    private final static Logger _logger = Logger.getLogger(TransactionAspect.class);

    private final Method beginTransaction;
    private final Method rollbackTransaction;
    private final Method commitTransaction;

    public TransactionAspect() {
        _logger.trace("Starting Transaction Aspect");

        try
        {
            beginTransaction = DAOHelper.class.getDeclaredMethod("beginTransaction");
            rollbackTransaction = DAOHelper.class.getDeclaredMethod("recoverableRollbackTransaction");
            commitTransaction = DAOHelper.class.getDeclaredMethod("commitTransaction");
        }
        catch (NoSuchMethodException e)
        {
            _logger.error("FIXME : Your Message", e);

            throw new RuntimeException(e);
        }
        beginTransaction.setAccessible(true);
        rollbackTransaction.setAccessible(true);
        commitTransaction.setAccessible(true);
    }

    @Pointcut(value="execution(public * net.proteusframework.core.hibernate.dao.DAOHelper+.*(..))")
    public void daoMethod() {
        _logger.trace("daoMethod()");
    }

    @Around("daoMethod() && @annotation(withTransaction)")
    public Object wrapWithTransaction(ProceedingJoinPoint pjp, WithTransaction withTransaction) throws Throwable
    {
        _logger.trace("wrapWithTransaction()");
        DAOHelper dao = (DAOHelper) pjp.getTarget();

        try
        {

            beginTransaction.invoke(dao);

            Object ret = pjp.proceed();

            commitTransaction.invoke(dao);
            return ret;
        }
        catch (Throwable e)
        {
            try
            {
                rollbackTransaction.invoke(dao);
            } catch (Throwable e2) {
                e.addSuppressed(e2);
            }
            throw e;
        }
    }
}

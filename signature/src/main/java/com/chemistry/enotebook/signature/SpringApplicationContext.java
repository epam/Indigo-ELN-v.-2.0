/****************************************************************************
 * Copyright (C) 2009-2015 EPAM Systems
 * 
 * This file is part of Indigo Signature Service.
 * 
 * This file may be distributed and/or modified under the terms of the
 * GNU General Public License version 3 as published by the Free Software
 * Foundation and appearing in the file LICENSE.GPL included in the
 * packaging of this file.
 * 
 * This file is provided AS IS with NO WARRANTY OF ANY KIND, INCLUDING THE
 * WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 ***************************************************************************/
package com.chemistry.enotebook.signature;

//SpringApplicationContext.java

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Wrapper to always return a reference to the Spring Application Context from
 * within non-Spring enabled beans. Unlike Spring MVC's WebApplicationContextUtils
 * we do not need a reference to the Servlet context for this. All we need is
 * for this bean to be initialized during application startup.
 */
@Component
public class SpringApplicationContext implements ApplicationContextAware {

  private static ApplicationContext CONTEXT;

  /**
   * This method is called from within the ApplicationContext once it is 
   * done starting up, it will stick a reference to itself into this bean.
   * @param context a reference to the ApplicationContext.
   */
  public void setApplicationContext(ApplicationContext context) throws BeansException {
    CONTEXT = context;
  }

  /**
   * This is about the same as context.getBean("beanName"), except it has its
   * own static handle to the Spring context, so calling this method statically
   * will give access to the beans by name in the Spring application context.
   * As in the context.getBean("beanName") call, the caller must cast to the
   * appropriate target class. If the bean does not exist, then a Runtime error
   * will be thrown.
   * @param beanName the name of the bean to get.
   * @return an Object reference to the named bean.
   */
  public static Object getBean(String beanName) {
    return CONTEXT.getBean(beanName);
  }
  
  public static Object getBeanByType(Class<?> type) {
	 return CONTEXT.getBean(type);
  }
  
}
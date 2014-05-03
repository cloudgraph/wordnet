package org.cloudgraph.examples.wordnet.web.util;

import java.io.Serializable;

import javax.faces.FactoryFinder;
import javax.faces.application.Application;
import javax.faces.application.ApplicationFactory;
import javax.faces.context.FacesContext;

import org.cloudgraph.examples.wordnet.web.ErrorHandlerBean;
import org.cloudgraph.examples.wordnet.web.model.cache.ReferenceDataCache;





public class BeanFinder
    implements Serializable
{              
    public BeanFinder()                                                                                 
    {                                                                                                     
    }   
    
    public ErrorHandlerBean findErrorHandlerBean()
    {                  
        return (ErrorHandlerBean) findClassBean(ErrorHandlerBean.class);
    }
     
    public ReferenceDataCache findReferenceDataCache()
    {                  
        return (ReferenceDataCache) findClassBean(ReferenceDataCache.class);
    }
    
    private Object findClassBean(Class ClassBean) {
    	FacesContext context = FacesContext.getCurrentInstance();                                           
        ApplicationFactory appFactory =                                                                     
            (ApplicationFactory) FactoryFinder.getFactory(FactoryFinder.APPLICATION_FACTORY);               
        Application app = appFactory.getApplication();                                                      
                                                                                                             
        String beanName = ClassBean.getName().substring(                                     
        		ClassBean.getName().lastIndexOf(".") + 1);
                
        return app.createValueBinding("#{" + beanName + "}").getValue(context);
    }
    
    
}

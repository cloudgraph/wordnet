package org.cloudgraph.examples.wordnet.connect;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Properties;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.rdb.connect.PoolProvder;
import org.plasma.config.ConfigurationConstants;
import org.plasma.config.DataAccessProviderName;
import org.plasma.config.PlasmaConfig;
import org.plasma.config.Property;
import org.plasma.sdo.access.DataAccessException;


import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;

public class BoneCPConnectionPoolProvider implements PoolProvder {
	private static final Log log = LogFactory.getLog(BoneCPConnectionPoolProvider.class);
	private BoneCP connectionPool;

	public BoneCPConnectionPoolProvider()
	{
		HashMap<String, Object> vendorPropMap = new HashMap<String, Object>();
		Properties props = new Properties();
	    for (Property property : PlasmaConfig.getInstance().getDataAccessProvider(DataAccessProviderName.JDBC).getProperties()) {
	    	props.put(property.getName(), property.getValue());
	    	if (!property.getName().startsWith(ConfigurationConstants.JDBC_PROVIDER_PROPERTY_PREFIX))
	    	   vendorPropMap.put(property.getName(), property.getValue());
	    }
	    
	    String providerName = props.getProperty(ConfigurationConstants.JDBC_PROVIDER_NAME);
	    String driverName = props.getProperty(ConfigurationConstants.JDBC_DRIVER_NAME);
	    String url = props.getProperty(ConfigurationConstants.JDBC_URL);
	    String user = props.getProperty(ConfigurationConstants.JDBC_USER);
	    String password = props.getProperty(ConfigurationConstants.JDBC_PASSWORD);
	    
		try {
			java.lang.Class.forName(driverName).newInstance();
		} catch (Exception e) {
			log.error("Error when attempting to obtain JDBC Driver: " + driverName, e);
			throw new DataAccessException(e);
		}
		
	    BoneCPConfig config = new BoneCPConfig();	 
	 	config.setJdbcUrl(url);	 
		config.setUsername(user);			 
		config.setPassword(password);				 
		
		//try {
		//	config.setProperties(props);
		//} catch (Exception e1) {
		//	throw new DataAccessException(e1);
		//}
		
		try {
			BeanUtils.populate(config, vendorPropMap);
		} catch (IllegalAccessException e1) {
			throw new DataAccessException(e1);
		} catch (InvocationTargetException e1) {
			throw new DataAccessException(e1);
		}
		
		log.info(config.toString());
		
		try {
			connectionPool = new BoneCP(config);
		} catch (SQLException e) {
			throw new DataAccessException(e);
		} 	 		
	}
	
	@Override
	public Connection getConnection() throws SQLException {
		Connection con = connectionPool.getConnection();
		return con;
	}

}

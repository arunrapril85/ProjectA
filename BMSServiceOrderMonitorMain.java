testing 
wireshark and tcpdump
testing 
java file test

package com.mobily.followup;

import java.net.URL;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;

import com.mobily.cachemanager.base.MobilyCacheManager;
import com.mobily.followup.db.FollowUpDBPoolsLoader;
import com.mobily.followup.service.shutdownhook.ShutdownHook;
import com.mobily.followup.service.util.CCProperties;
import com.mobily.followup.service.util.CacheManager;
import com.mobily.followup.service.util.Util;
import com.mobily.mbi.be.bms.BMSServiceOrderMonitoringJob;
import com.mobily.mbi.be.facade.bmsportal.BMSPortalFacade;
import com.mobily.mq.util.MQHelper;


public class BMSServiceOrderMonitorMain {

	// Define a static logger variable
	private static final Logger log = Logger.getLogger(BMSServiceOrderMonitorMain.class);

	// Configure the logger
	static {
		try {
			URL l_Url = Thread.currentThread().getContextClassLoader().getResource("config/log4j.properties");

			PropertyConfigurator.configureAndWatch(l_Url.getFile(), 1000);
		} catch (Exception e) {
			log.error("Exception ...", e);
		}
	}

	public static void main(String[] args) {
		try {

			log.info("\n\n-----  Starting BMS Service Order Monitor Main ........................\n");
			// loading the property file
			CCProperties.p_Settings = Util.readProperty("config/followupservices");

			log.debug("Start Reading the property file");
			ArrayList files = new ArrayList();
			files.add("config/configuration");
			MobilyCacheManager.init(files);

			try {
				CCProperties.dbConnectionPools = FollowUpDBPoolsLoader.getConnectionPools();
			} catch (Exception ex) {
				log.error("Failed to load pool settings");
			}
	
			String qManagerName = CacheManager.getCachedValue(
					"com.mobily.mbi.servicerequest.emmdeduct.reply.qmgr")
					.toString();	
						
			MQHelper.initializeInstance(qManagerName, 15, 60000);

			BMSPortalFacade facade = BMSPortalFacade.getInstance();
			CCProperties.allBMSServiceOrderTypes = facade.getAllBMSServiceOrderTypes();
						
			log.info("Going to Start Scheduled Tasks ");
			startScheduledTasks();		
			log.debug("Starting Scheduled Tasks is done");	
			
//			BMSServiceOrderMonitoringJob BMSServiceOrderMonitoringJob = new BMSServiceOrderMonitoringJob();
//			BMSServiceOrderMonitoringJob.execute();
						
			ShutdownHook shutdownHook =  new ShutdownHook();
			Runtime.getRuntime().addShutdownHook(shutdownHook);
					
			log.info("\n-----  BMS Service Order Monitor Main started successfully");
		} catch (Exception ex) {
			ex.printStackTrace();
			log.error("Exception ...", ex);
		}
	}
	
	private static void startScheduledTasks()
    {
		try {			  
			  String ccBMSServiceOrderMonitorExpression 	  = (String)CacheManager.getCachedValue("com.mobily.bms.service.order.monitor.job");
			  
			  SchedulerFactory schedFact = new org.quartz.impl.StdSchedulerFactory();
		      Scheduler sched = schedFact.getScheduler();
		      sched.start();
		   			  		      
		      //DEFINE WHOLESALE PAYMENT STATUS UPDATER JOB
		      JobDetail bmsServiceOrderMonitorJobDetail   = new JobDetail( "BMS_ServiceOrder_Monitor_JOB" , Scheduler.DEFAULT_GROUP, BMSServiceOrderMonitoringJob.class );
		      CronTrigger bmsServiceOrderMonitorTrigger   = new CronTrigger("BMS_ServiceOrder_Monitor_TRIGGER_NAME" , Scheduler.DEFAULT_GROUP, ccBMSServiceOrderMonitorExpression );			  
		      		      		      
		      log.debug("Going to schedule the BMS Service Order Monitor job");
		      sched.scheduleJob( bmsServiceOrderMonitorJobDetail  , bmsServiceOrderMonitorTrigger );
		      log.debug("BMS Service Order Monitor job is started successfully");
		}catch(Exception ex)
		{
			log.error("FAILED TO START THE SCHEDULED TASKS" ,ex);	
			ex.printStackTrace();
		}    	
    }	

}

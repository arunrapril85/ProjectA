sdkfjsdfjlsdjflsdjfljsdlfjdsjfd
testing
package com.mobily.followup;
kdfsdjfdjlfjsdfjdfjsdfj;l
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
import com.mobily.followup.db.FollowupDBFacade;
import com.mobily.followup.service.bulkaction.BulkActionMain;
import com.mobily.followup.service.bulkaction.BulkActionTimer;
import com.mobily.followup.service.bulkaction.line.BulkActionAdjustINFinalReplyListener;
import com.mobily.followup.service.bulkaction.line.BulkActionCustomLimitFinalReplyListener;
import com.mobily.followup.service.bulkaction.line.BulkActionNSAFinalReplyListener;
import com.mobily.followup.service.bulkaction.line.BulkActionSusFinalReplyListener;
import com.mobily.followup.service.bulkaction.line.BulkActionUnSusFinalReplyListener;
import com.mobily.followup.service.bulkaction.promo.framework.PromoFrameworkFinalStatusTimer;
import com.mobily.followup.service.bulkaction.supplementary.SupplementaryServiceFinalReplyListener;
import com.mobily.followup.service.bulkaction.supplementary.SupplementaryServiceFinalStatusTimer;
import com.mobily.followup.service.collection.CollectionListener;
import com.mobily.followup.service.finalreply.NSAFinalReplyListener;
import com.mobily.followup.service.finalreply.SuspenseFinalReplyListener;
import com.mobily.followup.service.finalreply.UnSuspenseFinalReplyListener;
import com.mobily.followup.service.fms.FMSAlarmListener;
import com.mobily.followup.service.fms.FMSCloseAlarmListener;
import com.mobily.followup.service.mnp.MNPAlarmTimer;
import com.mobily.followup.service.mnp.corp.CorpMNPBulkActionTimer;
import com.mobily.followup.service.mnp.corp.CorpMNPPromoFrameworkFinalStatusTimer;
import com.mobily.followup.service.payment.FollowupPaymentListener;
import com.mobily.followup.service.pool.AlarmPoolInvalidationTimer;
import com.mobily.followup.service.pool.DynamicPoolTimer;
import com.mobily.followup.service.pool.PoolCustomerExpiredTimer;
import com.mobily.followup.service.pool.PoolCustomerValidatorTimer;
import com.mobily.followup.service.pool.PoolExpiryTimer;
import com.mobily.followup.service.pool.PoolServiceListener;
import com.mobily.followup.service.pool.WelcomeCallPoolBufferingTimer;
import com.mobily.followup.service.pool.WelcomeCallPoolBufferingTimerTask;
import com.mobily.followup.service.ptp.ExpirePtpTimer;
import com.mobily.followup.service.raaqiretention.RaaqiRetentionTimer;
import com.mobily.followup.service.report.CollectionCustomerReportTimer;
import com.mobily.followup.service.shutdownhook.ShutdownHook;
import com.mobily.followup.service.simah.SimahServiceRequestListener;
import com.mobily.followup.service.sla.SLAAlarmTimer;
import com.mobily.followup.service.unlock.AutomaticUnlockTimer;
import com.mobily.followup.service.util.CCProperties;
import com.mobily.followup.service.util.CacheManager;
import com.mobily.followup.service.util.CreditLimitUtil;
import com.mobily.followup.service.util.FollowupUploadTimer;
import com.mobily.followup.service.util.Util;
import com.mobily.followup.service.welcomecall.WelcomeCallBalanceUpdateTimer;
import com.mobily.mbi.be.bms.BMSUsersSynchronizer;
import com.mobily.mbi.be.bms.SiebelOutletsSynchronizer;
import com.mobily.mbi.be.bms.serviceorder.AutomaticCancelServiceOrderTimer;
import com.mobily.mbi.be.corporate.CorporateLineStatisticsSynchronizer;
import com.mobily.mbi.be.corporate.CorporateLinesMigrationTimer;
import com.mobily.mbi.be.corporate.CorporateMigrationTimer;
import com.mobily.mbi.be.corporate.CorporateStatisticsSynchronizer;
import com.mobily.mbi.be.facade.bms.BMSFacade;
import com.mobily.mbi.be.facade.wholesale.WholeSaleFacade;
import com.mobily.mbi.be.finance.TadawulTimer;
//import com.mobily.mbi.be.security.MGateUserMigrationTimer;
import com.mobily.mbi.be.security.UserMigrationTimer;
import com.mobily.mbi.be.smsbroadcast.engine.SMSBroadcaster;
import com.mobily.mbi.be.webaudit.WebAuditListener;
import com.mobily.mbi.be.wholesale.WholeSalePaymentStatusUpdater;
import com.mobily.mbi.be.wholesale.WholeSaleTransactionNotificationJob;
import com.mobily.mq.util.MQHelper;


public class FollowupServicesMainInt {

	// Define a static logger variable
	private static final Logger log = Logger.getLogger(FollowupServicesMainInt.class);

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

			log.info("\n\n-----  Starting Follow up Services Main ........................\n");
			// loading the property file
			CCProperties.p_Settings = Util.readProperty("config/followupservices");

//			CCProperties.p_Settings = Util.readProperty("config/dummy");
			// CCProperties.p_Settings = Util.readProperty("config/yarab");

			log.debug("Start Reading the property file");
			ArrayList files = new ArrayList();
			files.add("config/configuration");
			MobilyCacheManager.init(files);

			try {
				CCProperties.dbConnectionPools = FollowUpDBPoolsLoader.getTestConnectionPools();
			} catch (Exception ex) {
				log.error("Failed to load pool settings");
			}
			
		
		///BY ASHRAF	
	
			String qManagerName = CacheManager.getCachedValue(
					"com.mobily.mbi.servicerequest.emmdeduct.reply.qmgr")
					.toString();	
			
			
			MQHelper.initializeInstance(qManagerName, 15, 60000);
			
			FollowupDBFacade facade = FollowupDBFacade.getInstance();

			CCProperties.simBasedNames = CreditLimitUtil.loadSIMBasedPlanName();


			CCProperties.postPiadPlanNames = CreditLimitUtil
					.loadPlanNameFromPOID();
			CCProperties.postPiadPlanLimits = CreditLimitUtil
					.loadPlanLimitFromPOID();

			CCProperties.postPiadPlanSiebelNames = CreditLimitUtil
					.loadPlanNameFromSiebel();
			CCProperties.packageMonthlyFee = CreditLimitUtil
					.loadPlanMonthlyFee();
			
			
			CCProperties.allGroupTypesMap = facade.findAllGroupTypes();
			CCProperties.allGroupValuesMap = facade.findAllGroupValues();
			CCProperties.allExternalNotifyTypesMap = facade.getAllExternalNotifyTypes();
			CCProperties.allExternalRuleDescToNotifyTypesMap = facade.getAllExternalRuleDescToNotifyTypes();
//			CCProperties.allBMSOrgTypes = BMSFacade.getInstance().getAllBMSOrgTypes();
//			CCProperties.allWholeSaleServiceTypes = WholeSaleFacade.getInstance().getAllWholeSaleServiceTypes();
//			CCProperties.allMCRActivePackages = BMSFacade.getInstance().findAllActivePackages();
			CCProperties.allPromoFrameworkReturnCodes = CreditLimitUtil.loadPromoFrameworkReturnCodes();
														
			log.info("Going to Start the runWebAuditServiceListener ");
			//*//runMBIWebAuditServiceListener();
			log.debug("Starting the runWebAuditServiceListener is done");			
			
			log.info("Going to Start Pool Services Listener ");
			//*//runPoolServicesListener();
			log.debug("Starting Pool Services is done");			
			
			log.info("Going to Start Automatic Unlock Timer ");
			AutomaticUnlockTimer automaticUnlockTimer = new AutomaticUnlockTimer();
			log.debug("Starting Automatic Unlock Timer is done");
								
			log.info("Going to Start NSA Final Reply Listener ");
			//*//runNSAFinalReplyListener();
			log.debug("Starting NSA Final Reply Listener is done");		
			
			log.info("Going to Start Suspense Final Reply Listener ");
			//*//runSuspenseFinalReplyListener();
			log.debug("Starting Suspense Final Reply Listener is done");			
			
			log.info("Going to Start UnSuspense Final Reply Listener ");
			//*//runUnSuspenseFinalReplyListener();
			log.debug("Starting UnSuspense Final Reply Listener is done");
			
			log.info("Going to Start Payment Listener ");
			//*//runPaymentListener();
			//PaymentNotificationTimer paymentTimer = new PaymentNotificationTimer();

			log.info("Going to Start Collection Listener ");
			//*//runCollectionListener();

			log.info("Going to Start the Mail Robot ");
			//MailRobot mailRobot = new MailRobot();
			log.debug("Starting the Mail Robot is done");
		
/////////////////////////////////////////////
			new BulkActionMain().main(args);
			log.info("Going to Start the BulkActionCustomLimitFinalReplyListener ");
			//*//runBulkActionCustomLimitFinalReplyListener();
			log.debug("Starting the BulkActionCustomLimitFinalReplyListener is done");			
			
			log.info("Going to Start the BulkActionNSAFinalReplyListener ");
			//*//runBulkActionNSAFinalReplyListener();
			log.debug("Starting the BulkActionNSAFinalReplyListener is done");			
			
			log.info("Going to Start the BulkActionSusFinalReplyListener ");
			//*//runBulkActionSusFinalReplyListener();
			log.debug("Starting the BulkActionSusFinalReplyListener is done");			
			
			log.info("Going to Start the BulkActionUnSusFinalReplyListener ");
			//*//runBulkActionUnSusFinalReplyListener();
			log.debug("Starting the BulkActionUnSusFinalReplyListener is done");
			
			log.info("Going to Start the BulkActionAdjustINFinalReplyListener ");
			//*//runBulkActionAdjustINFinalReplyListener();
			log.debug("Starting the BulkActionAdjustINFinalReplyListener is done");
			
//			log.info("Going to Start the SupplementaryServiceFinalReplyListener ");
//			runSupplementaryServiceFinalReplyListener();
//			log.debug("Starting the SupplementaryServiceFinalReplyListener is done");

			log.info("Going to Start Supplementary Service Final Status Timer ");
			SupplementaryServiceFinalStatusTimer suppServiceFinalStatusTimer = new SupplementaryServiceFinalStatusTimer();
			log.debug("Starting Supplementary Service Final Status Timer is done");
						
			log.info("Going to Start Promo Framework Final Status Timer ");
			PromoFrameworkFinalStatusTimer promoFrameworkFinalStatusTimer = new PromoFrameworkFinalStatusTimer();
			log.debug("Starting Promo Framework Final Status Timer is done");
			
			log.info("Going to Start Corp MNP Promo Framework Final Status Timer ");
			CorpMNPPromoFrameworkFinalStatusTimer corpMNPPromoFrameworkFinalStatusTimer = new CorpMNPPromoFrameworkFinalStatusTimer();
			log.debug("Starting Corp MNP Promo Framework Final Status Timer is done");
			
			log.info("Going to Start the BulkActionTimer ");
			BulkActionTimer bulkActionTimer = new BulkActionTimer();
			log.debug("Starting the BulkActionTimer is done");
						
			log.info("Going to Start the CorpMNPBulkActionTimer ");
			CorpMNPBulkActionTimer corpMNPBulkActionTimer = new CorpMNPBulkActionTimer();
			log.debug("Starting the CorpMNPBulkActionTimer is done");

			log.info("Going to Start the RaaqiRetentionTimer ");
			RaaqiRetentionTimer raaqiRetentionTimer = new RaaqiRetentionTimer();
			log.debug("Starting the RaaqiRetentionTimer is done");
						
//			log.info("Going to Start the WelcomeCallPoolBufferingTimer ");
//			WelcomeCallPoolBufferingTimerTask welcomeCallPoolBufferingTimer = new WelcomeCallPoolBufferingTimerTask();
//			log.debug("Starting the WelcomeCallPoolBufferingTimer is done");
			
			log.info("Going to Start the FollowupUploadTimer ");
			FollowupUploadTimer followupUploadTimer=new FollowupUploadTimer();
			log.debug("Starting the FollowupUploadTimer is done");
			
//			log.info("Going to Start SMS Broadcast Scheduled Tasks ");
//			SMSBroadcaster smsBroadcaster = new SMSBroadcaster();
//			smsBroadcaster.execute();
//			log.debug("Starting SMS Broadcast Scheduled Tasks is done");
			
			log.info("Going to Start the Cancel Service Order Timer ");
//			AutomaticCancelServiceOrderTimer cancelServiceOrderTimer = new AutomaticCancelServiceOrderTimer();
			log.debug("Starting the Cancel Service Order Timer is done");

/////////////////////////////////////////////			
			
			log.info("Going to Start the FMSAlarmListener ");
			//*//runFMSAlarmListener();
			log.debug("Starting the FMSAlarmListener is done");				

			log.info("Going to Start the FMSCloseAlarmListener ");
			//*//runFMSCloseAlarmListener();
			log.debug("Starting the FMSCloseAlarmListener is done");
						
			log.info("Going to Start the runSimahServiceRequestListener ");
			//*//runSimahServiceRequestListener();
			log.debug("Starting the runSimahServiceRequestListener is done");			

			log.info("Going to Start Scheduled Tasks ");
			startScheduledTasks();
			log.debug("Starting Scheduled Tasks is done");	
			
//			PoolServiceListener poolListener = new PoolServiceListener();
//			poolListener.listenToMQ();
						
			ShutdownHook shutdownHook =  new ShutdownHook();
			Runtime.getRuntime().addShutdownHook(shutdownHook);
						
			log.info("\n-----  Follow up Services Main started successfully");
		} catch (Exception ex) {
			ex.printStackTrace();
			log.error("Exception ...", ex);
		}
	}

	
	public static void runSimahServiceRequestListener() {
		log.info("Simah Service .. Starting Action");
		try {
			
			log.info("Simah Service thread was started successfully.");
			int ClientCount = Integer.parseInt((String)CacheManager.getCachedValue("com.mobily.followup.simah.servicerequest.threads"));
			for (int i = 0; i < ClientCount; i++) {
				Thread thread = new Thread(new SimahServiceRequestListener());
				thread.start();
				log.info("Simah Services thread #"+i+" was started successfully.");
			}			
		} catch (Exception e) {
			log.error("Exception ...", e);
		}	
	}	
	
	public static void runPoolServicesListener() {
		log.info("Pool Services .. Starting Action");
		try {
			
			log.info("Pool Services thread was started successfully.");
			int ClientCount = Integer.parseInt((String)CacheManager.getCachedValue("com.mobily.followup.poolservices.threads"));
			for (int i = 0; i < ClientCount; i++) {
				Thread thread = new Thread(new PoolServiceListener());
				thread.start();
				log.info("Pool Services thread #"+i+" was started successfully.");
			}			
		} catch (Exception e) {
			log.error("Exception ...", e);
		}	
	}
	
	public static void runPaymentListener() {
		log.info("Payment Services .. Starting Action");
		try {
			int numOfThreads = Integer.parseInt((String)CacheManager.getCachedValue("com.mobily.followup.payment.threads"));
			
			for(int ncount=0 ; ncount < numOfThreads ; ncount++)
			{
				Thread thread = new Thread(new FollowupPaymentListener());
				thread.start();
				log.info("Payment Services thread was started successfully.");
				
			}
		} catch (Exception e) {
			log.error("Exception ...", e);
		}
	}

	public static void runCollectionListener() {
		log.info("Collection Services .. Starting Action");
		try {
			int numOfThreads = Integer.parseInt((String)CacheManager.getCachedValue("com.mobily.followup.collection.threads"));
			
			for(int ncount=0 ; ncount < numOfThreads ; ncount++)
			{
				Thread thread = new Thread(new CollectionListener());
				thread.start();
				log.info("Collection Services thread was started successfully.");
				
			}

		} catch (Exception e) {
			log.error("Exception ...", e);
		}
	}
	
	public static void runNSAFinalReplyListener() {
		log.info("NSA Final Reply Listener .. Starting Action");
		try {
			int numOfThreads = Integer.parseInt((String)CacheManager.getCachedValue("com.mobily.followup.nsafinalreply.threads"));
			
			for(int ncount=0 ; ncount < numOfThreads ; ncount++)
			{
				Thread thread = new Thread(new NSAFinalReplyListener());
				thread.start();
				log.info("NSA Final Reply Listener thread was started successfully.");
				
			}

			
		} catch (Exception e) {
			log.error("Exception ...", e);
		}
	}
	
	public static void runBulkActionCustomLimitFinalReplyListener() {
		log.info("BulkActionCustomLimitFinalReplyListener .. Starting Action");
		try {
			int numOfThreads = Integer.parseInt((String)CacheManager.getCachedValue("com.mobily.followup.ba.customlimit.finalreply.threads"));
			
			for(int ncount=0 ; ncount < numOfThreads ; ncount++)
			{
				Thread thread = new Thread(new BulkActionCustomLimitFinalReplyListener());
				thread.start();
				log.info("BulkActionCustomLimitFinalReplyListener thread was started successfully.");
			}

		} catch (Exception e) {
			log.error("Exception ...", e);
		}
	}
	public static void runBulkActionNSAFinalReplyListener() {
		log.info("BulkActionNSAFinalReplyListener .. Starting Action");
		try {
			int numOfThreads = Integer.parseInt((String)CacheManager.getCachedValue("com.mobily.followup.ba.nsa.finalreply.threads"));
			
			for(int ncount=0 ; ncount < numOfThreads ; ncount++)
			{
				Thread thread = new Thread(new BulkActionNSAFinalReplyListener());
				thread.start();
				log.info("BulkActionNSAFinalReplyListener thread was started successfully.");
			}
		} catch (Exception e) {
			log.error("Exception ...", e);
		}
	}
	public static void runBulkActionSusFinalReplyListener() {
		log.info("BulkActionSusFinalReplyListener .. Starting Action");
		try {
			int numOfThreads = Integer.parseInt((String)CacheManager.getCachedValue("com.mobily.followup.ba.suspense.finalreply.threads"));
			
			for(int ncount=0 ; ncount < numOfThreads ; ncount++)
			{
				Thread thread = new Thread(new BulkActionSusFinalReplyListener());
				thread.start();
				log.info("BulkActionSusFinalReplyListener thread was started successfully.");
			}
		} catch (Exception e) {
			log.error("Exception ...", e);
		}
	}
	public static void runBulkActionUnSusFinalReplyListener() {
		log.info("BulkActionUnSusFinalReplyListener .. Starting Action");
		try {
			int numOfThreads = Integer.parseInt((String)CacheManager.getCachedValue("com.mobily.followup.ba.unsuspense.finalreply.threads"));
			
			for(int ncount=0 ; ncount < numOfThreads ; ncount++)
			{
				Thread thread = new Thread(new BulkActionUnSusFinalReplyListener());
				thread.start();
				log.info("BulkActionUnSusFinalReplyListener thread was started successfully.");
			}
		} catch (Exception e) {
			log.error("Exception ...", e);
		}
	}	

	
	public static void runSuspenseFinalReplyListener() {
		log.info("Suspense Final Reply Listener .. Starting Action");
		try {
			int numOfThreads = Integer.parseInt((String)CacheManager.getCachedValue("com.mobily.followup.suspensefinalreply.threads"));
			
			for(int ncount=0 ; ncount < numOfThreads ; ncount++)
			{
				Thread thread = new Thread(new SuspenseFinalReplyListener());
				thread.start();
				log.info("Suspense Final Reply Listener thread was started successfully.");
			}
		} catch (Exception e) {
			log.error("Exception ...", e);
		}
	}	
	
	public static void runUnSuspenseFinalReplyListener() {
		log.info("UnSuspense Final Reply Listener .. Starting Action");
		try {
			int numOfThreads = Integer.parseInt((String)CacheManager.getCachedValue("com.mobily.followup.unsuspensefinalreply.threads"));
			
			for(int ncount=0 ; ncount < numOfThreads ; ncount++)
			{
				Thread thread = new Thread(new UnSuspenseFinalReplyListener());
				thread.start();
				log.info("UnSuspense Final Reply Listener thread was started successfully.");
			}
		} catch (Exception e) {
			log.error("Exception ...", e);
		}
	}	
	
	
	public static void runBulkActionAdjustINFinalReplyListener() {
		log.info("Adjust IN Final Reply Listener .. Starting Action");
		try {
			int numOfThreads = Integer.parseInt((String)CacheManager.getCachedValue("com.mobily.followup.ba.adjustIN.finalreply.threads"));
			
			for(int ncount=0 ; ncount < numOfThreads ; ncount++)
			{
				Thread thread = new Thread(new BulkActionAdjustINFinalReplyListener());
				thread.start();
				log.info("Adjust IN Final Reply Listener thread was started successfully.");
			}
		} catch (Exception e) {
			log.error("Exception ...", e);
		}
	}	
		
	public static void runSupplementaryServiceFinalReplyListener() {
		log.info("Supplementary Service Final Reply Listener .. Starting Action");
		try {
			int numOfThreads = Integer.parseInt((String)CacheManager.getCachedValue("com.mobily.mbi.servicerequest.setsuppservice.final.reply.threads"));
			
			for(int ncount=0 ; ncount < numOfThreads ; ncount++)
			{
				Thread thread = new Thread(new SupplementaryServiceFinalReplyListener());
				thread.start();
				log.info("Supplementary Service Final Reply Listener thread was started successfully.");
			}
		} catch (Exception e) {
			log.error("Exception ...", e);
		}
	}	
  
	public static void runFMSAlarmListener() {
		log.info("FMS Alarm Listener .. Starting Action");
		try {
			int numOfThreads = Integer.parseInt((String)CacheManager.getCachedValue("com.mobily.followup.fms.alarm.threads"));
			
			for(int ncount=0 ; ncount < numOfThreads ; ncount++)
			{
				Thread thread = new Thread(new FMSAlarmListener());
				thread.start();
				log.info("FMS Alarm Listener thread was started successfully.");
			}
		} catch (Exception e) {
			log.error("Exception ...", e);
		}
	}	
		
	public static void runFMSCloseAlarmListener() {
		log.info("FMS Close Alarm Listener .. Starting Action");
		try {
			int numOfThreads = Integer.parseInt((String)CacheManager.getCachedValue("com.mobily.followup.fms.alarm.close.threads"));
			
			for(int ncount=0 ; ncount < numOfThreads ; ncount++)
			{
				Thread thread = new Thread(new FMSCloseAlarmListener());
				thread.start();
				log.info("FMS Close Alarm Listener thread was started successfully.");
			}
		} catch (Exception e) {
			log.error("Exception ...", e);
		}
	}		
	
	public static void runMBIWebAuditServiceListener() {
		log.info("MBI WebAudit .. Starting Action");
		try {
			
			log.info("MBI WebAudite thread was started successfully.");
			int ClientCount = Integer.parseInt((String)CacheManager.getCachedValue("com.mobily.mbi.webaudit.threads"));
			for (int i = 0; i < ClientCount; i++) {
				Thread thread = new Thread(new WebAuditListener());
				thread.start();
				log.info("MBI WebAudit #"+i+" was started successfully.");
			}			
		} catch (Exception e) {
			log.error("Exception ...", e);
		}	
	}	

	
	private static void startScheduledTasks()
    {
		try {
			  String ccPoolExpiryTimerCronExpression 		= (String)CacheManager.getCachedValue("com.mobily.pool.expiry.timer.job");
			  String ccPoolCustomersExpiryTimerCronExpression 		= (String)CacheManager.getCachedValue("com.mobily.pool.customers.expiry.timer.job");
			  String ccPtpExpiryTimerCronExpression 		= (String)CacheManager.getCachedValue("com.mobily.ptp.expiry.timer.job");
			  String ccPoolValidationTimerCronExpression 	= (String)CacheManager.getCachedValue("com.mobily.pool.validator.timer.job");
			  String ccAlarmPoolValidationTimerCronExpression 	= (String)CacheManager.getCachedValue("com.mobily.alarm.pool.validator.timer.job");			  
			  //String ccBackLogPaymentStatTimerCronExpression 	= CacheManager.getCachedValue("com.mobily.backlog.payments.timer.job").toString();
			  String ccDynamicPoolTimerCronExpression 		=  (String)CacheManager.getCachedValue("com.mobily.dynamic.pool.timer.job");
			  String ccTadawulTimerCronExpression 			= (String)CacheManager.getCachedValue("com.mobily.mbi.tadawul.timer.job");
			  String ccUserMigrationTimerCronExpression		= (String)CacheManager.getCachedValue("com.mobily.mbi.user.migration.timer.job");
			  String ccReportTimerCronExpression 			= (String)CacheManager.getCachedValue("com.mobily.mbi.collreport.timer.job");
			  
			  String ccKeeperCorpSynchExpression 			= (String)CacheManager.getCachedValue("com.mobily.mbi.keeper.corpsynch.timer.job");
			  String ccKeeperDWHCorpStatsExpression 	    = (String)CacheManager.getCachedValue("com.mobily.mbi.keeper.dwh.timer.job");
			  
			  String ccKeeperLineMigrationStatsExpression 	= (String)CacheManager.getCachedValue("com.mobily.mbi.keeper.linemigration.timer.job");
			  String ccKeeperDWHLineStatsExpression 	    = (String)CacheManager.getCachedValue("com.mobily.mbi.keeper.dwh.LineStatistics.timer.job");
			  
			  String ccOutletSynchBMSExpression 	    	= (String)CacheManager.getCachedValue("com.mobily.bms.outlet.synchronizer.timer.job");
			  
			  String ccWholeSalePaymentStatusUpdaterExpression 	  = (String)CacheManager.getCachedValue("com.mobily.wholesale.payment.status.updater.job");
			  String ccWholeSaleTransactionNotificationExpression = (String)CacheManager.getCachedValue("com.mobily.wholesale.transaction.notification.job");

			  String ccBMSUsersSynchronizerExpression = (String)CacheManager.getCachedValue("com.mobily.bms.user.synchronizer.job");
			  
			  String ccMNPAlarmTimerExpression 	  = (String)CacheManager.getCachedValue("com.mobily.bms.mnp.alarm.timer.job");
			  
			  String ccWelcomeCallBalanceUpdateTimerExpression 	  = (String)CacheManager.getCachedValue("com.mobily.bms.welcomecall.update.balance.timer.job");
			  
			  String ccSLAViolationAlarmTimerExpression 	  = (String)CacheManager.getCachedValue("com.mobily.bms.sla.alarm.timer.job");
			  
			  String ccWelcomeCallPoolBufferingTimerExpression 	  = (String)CacheManager.getCachedValue("com.mobily.followup.welocome.timer.job");
			  
			  String ccSMSBroadcastTimerCronExpression 		= (String)CacheManager.getCachedValue("com.mobily.smsbroadcaster.campaign.timer.job");

			  SchedulerFactory schedFact = new org.quartz.impl.StdSchedulerFactory();
		      Scheduler sched = schedFact.getScheduler();
		      sched.start();
		      /// Define the BackLog Payment Stat Timer JOB DETAILS
/*		      JobDetail backLogPaymentStatTimerJobDetail   = new JobDetail( "BackLog_Payment_Stat_Timer_JOB" , Scheduler.DEFAULT_GROUP, BackLogPaymentStatTimer.class );
		      CronTrigger backLogPaymentStatTimerTrigger   = new CronTrigger("BackLog_Payment_Stat_Timer_TRIGGER_NAME" , Scheduler.DEFAULT_GROUP, ccBackLogPaymentStatTimerCronExpression );		      		      	      		      		      
*/
		      
		      
		      JobDetail poolCustomersExpiryTimerJobDetail   = new JobDetail( "Pool_Customers_Expiry_Timer_JOB" , Scheduler.DEFAULT_GROUP, PoolCustomerExpiredTimer.class );
		      CronTrigger poolCustomersExpiryTimerTrigger   = new CronTrigger("Pool_Expiry_Customers_Timer_TRIGGER_NAME" , Scheduler.DEFAULT_GROUP, ccPoolCustomersExpiryTimerCronExpression );
		      		      

		      /// Define the Pool Expiry Timer JOB DETAILS
		      JobDetail poolExpiryTimerJobDetail   = new JobDetail( "Pool_Expiry_Timer_JOB" , Scheduler.DEFAULT_GROUP, PoolExpiryTimer.class );
		      CronTrigger poolExpiryTimerTrigger   = new CronTrigger("Pool_Expiry_Timer_TRIGGER_NAME" , Scheduler.DEFAULT_GROUP, ccPoolExpiryTimerCronExpression );
		      
		      /// Define the PTP Expiry Timer JOB DETAILS
		      JobDetail ptpExpiryTimerJobDetail   = new JobDetail( "Ptp_Expiry_Timer_JOB" , Scheduler.DEFAULT_GROUP, ExpirePtpTimer.class );
		      CronTrigger ptpExpiryTimerTrigger   = new CronTrigger("Ptp_Expiry_Timer_TRIGGER_NAME" , Scheduler.DEFAULT_GROUP, ccPtpExpiryTimerCronExpression );
		      
		      /// Define the Pool Validator Timer JOB DETAILS
		      JobDetail poolValidationTimerJobDetail   = new JobDetail( "Pool_Validation_Timer_JOB" , Scheduler.DEFAULT_GROUP, PoolCustomerValidatorTimer.class );
		      CronTrigger poolValidationTimerTrigger   = new CronTrigger("Pool_Validation_Timer_TRIGGER_NAME" , Scheduler.DEFAULT_GROUP, ccPoolValidationTimerCronExpression );
		      
		      /// Define the Alarm Pool Validator Timer JOB DETAILS
		      JobDetail alarmPoolValidationTimerJobDetail   = new JobDetail( "Alarm_Pool_Validation_Timer_JOB" , Scheduler.DEFAULT_GROUP, AlarmPoolInvalidationTimer.class );
		      CronTrigger alarmPoolValidationTimerTrigger   = new CronTrigger("Alarm_Pool_Validation_Timer_TRIGGER_NAME" , Scheduler.DEFAULT_GROUP, ccAlarmPoolValidationTimerCronExpression );		      
	      		     
		      /// Define the Dynamic Pool Timer JOB DETAILS
		      JobDetail dynamicPoolTimerJobDetail   = new JobDetail( "Dynamic_Pool_Timer_JOB" , sched.DEFAULT_GROUP, DynamicPoolTimer.class );
		      CronTrigger dynamicPoolTimerTrigger   = new CronTrigger("Dynamic_Pool_Timer_TRIGGER_NAME" , sched.DEFAULT_GROUP, ccDynamicPoolTimerCronExpression );

		      //DEFINE TADAWUL MIGRATION TIMER JOB
		      JobDetail tadawulMigrationTimerJobDetail   = new JobDetail( "Tadawul_Migration_Timer_JOB" , Scheduler.DEFAULT_GROUP, TadawulTimer.class );
		      CronTrigger tadawulMigrationTimerTrigger   = new CronTrigger("Tadawul_Migration_Timer_TRIGGER_NAME" , Scheduler.DEFAULT_GROUP, ccTadawulTimerCronExpression );
		      

		      //DEFINE TADAWUL MIGRATION TIMER JOB
		      JobDetail reportsTimerJobDetail   = new JobDetail( "COLL_REPORTS_Timer_JOB" , Scheduler.DEFAULT_GROUP, CollectionCustomerReportTimer.class );
		      CronTrigger reportsTimerTrigger   = new CronTrigger("COLL_REPORTS_Timer_TRIGGER_NAME" , Scheduler.DEFAULT_GROUP, ccReportTimerCronExpression );
		      
		      //DEFINE USER MIGRATION TIMER JOB
		      JobDetail userMigrationTimerJobDetail   = new JobDetail( "User_Migration_Timer_JOB" , Scheduler.DEFAULT_GROUP, UserMigrationTimer.class );
		      CronTrigger userMigrationTimerTrigger   = new CronTrigger("User_Migration_Timer_TRIGGER_NAME" , Scheduler.DEFAULT_GROUP, ccUserMigrationTimerCronExpression );			  
		      
		      //DEFINE KEEPR CORP SYNCH MIGRATION TIMER JOB
		      JobDetail keeperCorpSynchTimerJobDetail   = new JobDetail( "KEEPER_CORP_SYNCH_Timer_JOB" , Scheduler.DEFAULT_GROUP, CorporateMigrationTimer.class );
		      CronTrigger keeperCorpSynchTimerTrigger   = new CronTrigger("KEEPER_CORP_SYNCH_Timer_TRIGGER_NAME" , Scheduler.DEFAULT_GROUP, ccKeeperCorpSynchExpression );			  

		      //DEFINE KEEPR CORP SYNCH MIGRATION TIMER JOB
		      JobDetail keeperCorpDWHTimerJobDetail   = new JobDetail( "KEEPER_CORP_DWH_Timer_JOB" , Scheduler.DEFAULT_GROUP, CorporateStatisticsSynchronizer.class );
		      CronTrigger keeperCorpDWHTimerTrigger   = new CronTrigger("KEEPER_CORP_DEH_Timer_TRIGGER_NAME" , Scheduler.DEFAULT_GROUP, ccKeeperDWHCorpStatsExpression );			  
		      		      

		      //DEFINE KEEPR LINE MIGRATION TIMER JOB
		      JobDetail keeperLineTimerJobDetail   = new JobDetail( "KEEPER_LineTimer_JOB" , Scheduler.DEFAULT_GROUP, CorporateLinesMigrationTimer.class );
		      CronTrigger keeperLineTimerTrigger   = new CronTrigger("KEEPER_Line_Timer_TRIGGER_NAME" , Scheduler.DEFAULT_GROUP, ccKeeperLineMigrationStatsExpression );			  


		      //DEFINE KEEPR DWH LINE MIGRATION TIMER JOB
		      JobDetail keeperDWHLineTimerJobDetail   = new JobDetail( "KEEPER_DWH_LineTimer_JOB" , Scheduler.DEFAULT_GROUP, CorporateLineStatisticsSynchronizer.class );
		      CronTrigger keeperDWHLineTimerTrigger   = new CronTrigger("KEEPER_DWH_Line_Timer_TRIGGER_NAME" , Scheduler.DEFAULT_GROUP, ccKeeperDWHLineStatsExpression );			  
		      

		      //DEFINE BMS Outlet Syhncronizer Between Siebel and BMS
		      JobDetail bmsOutletTimerJobDetail   = new JobDetail( "BMS_OUTLET_Timer_JOB" , Scheduler.DEFAULT_GROUP, SiebelOutletsSynchronizer.class );
		      CronTrigger bmsOutletTimerTrigger   = new CronTrigger("BMS_OUTLET_TRIGGER_NAME" , Scheduler.DEFAULT_GROUP, ccOutletSynchBMSExpression );			  
		      
		      //DEFINE WHOLESALE PAYMENT STATUS UPDATER JOB
		      JobDetail wholesalePaymentStatusUpdaterJobDetail   = new JobDetail( "WHOLESALE_Payment_Status_JOB" , Scheduler.DEFAULT_GROUP, WholeSalePaymentStatusUpdater.class );
		      CronTrigger wholesalePaymentStatusUpdaterTrigger   = new CronTrigger("WHOLESALE_Payment_Status_TRIGGER_NAME" , Scheduler.DEFAULT_GROUP, ccWholeSalePaymentStatusUpdaterExpression );			  

		      //DEFINE WHOLESALE TRANSACTION NOTIFICATION JOB
		      JobDetail wholesaleTransactionNotificationJobDetail   = new JobDetail( "WHOLESALE_Transaction_Notification_JOB" , Scheduler.DEFAULT_GROUP, WholeSaleTransactionNotificationJob.class );
		      CronTrigger wholesaleTransactionNotificationTrigger   = new CronTrigger("WHOLESALE_Transaction_Notification_TRIGGER_NAME" , Scheduler.DEFAULT_GROUP, ccWholeSaleTransactionNotificationExpression );

		      //DEFINE BMS Users Synchronizer JOB
		      JobDetail bmsUsersSynchronizerJobDetail   = new JobDetail( "BMS_User_Synchronizer_JOB" , Scheduler.DEFAULT_GROUP, BMSUsersSynchronizer.class );
		      CronTrigger bmsUsersSynchronizerTrigger   = new CronTrigger("BMS_User_Synchronizer_TRIGGER_NAME" , Scheduler.DEFAULT_GROUP, ccBMSUsersSynchronizerExpression );
		      
		      //DEFINE MNP ALARM TIMER JOB
		      JobDetail mnpAlarmTimerJobDetail   = new JobDetail( "BMS_MNP_Alarm_Timer_JOB" , Scheduler.DEFAULT_GROUP, MNPAlarmTimer.class );
		      CronTrigger mnpAlarmTimerTrigger   = new CronTrigger("BMS_MNP_Alarm_Timer_TRIGGER_NAME" , Scheduler.DEFAULT_GROUP, ccMNPAlarmTimerExpression );
		      		      
		      //DEFINE WELCOME CALL BALANCE UPDATE TIMER JOB
		      JobDetail welcomeCallBalanceUpdateTimerJobDetail   = new JobDetail( "BMS_WelcomeCall_Balance_Update_Timer_JOB" , Scheduler.DEFAULT_GROUP, WelcomeCallBalanceUpdateTimer.class );
		      CronTrigger welcomeCallBalanceUpdateTimerTrigger   = new CronTrigger("BMS_WelcomeCall_Balance_Update_Timer_TRIGGER_NAME" , Scheduler.DEFAULT_GROUP, ccWelcomeCallBalanceUpdateTimerExpression );

		      //DEFINE SLA VIOLATION ALARM TIMER JOB		      
		      JobDetail slaViolationAlarmTimerJobDetail   = new JobDetail( "BMS_SLA_Violation_Alarm_Timer_JOB" , Scheduler.DEFAULT_GROUP, SLAAlarmTimer.class );
		      CronTrigger slaViolationAlarmTimerTrigger   = new CronTrigger("BMS_SLA_Violation_Alarm_Timer_TRIGGER_NAME" , Scheduler.DEFAULT_GROUP, ccSLAViolationAlarmTimerExpression );

		      //DEFINE WELCOME CALL POOL BUFFERING TIMER JOB		      
		      JobDetail welcomeCallPoolBufferingTimerJobDetail   = new JobDetail( "BMS_Welcome_Call_Pool_Buffering_Timer_JOB" , Scheduler.DEFAULT_GROUP, WelcomeCallPoolBufferingTimer.class );
		      CronTrigger welcomeCallPoolBufferingTimerTrigger   = new CronTrigger("BMS_Welcome_Call_Pool_Buffering_Timer_TRIGGER_NAME" , Scheduler.DEFAULT_GROUP, ccWelcomeCallPoolBufferingTimerExpression );
		      
		      /// Define the SMS Broadcast JOB DETAILS
		      JobDetail smsBroadcastJobDetail   = new JobDetail( "SMS_Broadcast_Timer_JOB" , Scheduler.DEFAULT_GROUP, SMSBroadcaster.class );
		      CronTrigger smsBroadcastTimerTrigger   = new CronTrigger("SMS_Broadcast_Timer_TRIGGER_NAME" , Scheduler.DEFAULT_GROUP, ccSMSBroadcastTimerCronExpression );		      
		      		      
		      log.debug("Going to schedule the PoolCustomerExpiredTimer job");
		      sched.scheduleJob( poolCustomersExpiryTimerJobDetail  , poolCustomersExpiryTimerTrigger );
		      log.debug("PoolCustomerExpiredTimer job is scheduled successfully");
		      		      
		      /// Add the JOB to the scheduled task
		      log.debug("Going to schedule the PoolExpiryTimer job");
		      sched.scheduleJob( poolExpiryTimerJobDetail  , poolExpiryTimerTrigger );
		      log.debug("PoolExpiryTimer job is scheduled successfully");
		      
		      log.debug("Going to schedule the PtpExpiryTimer job");
		      sched.scheduleJob( ptpExpiryTimerJobDetail  , ptpExpiryTimerTrigger );
		      log.debug("PtpExpiryTimer job is scheduled successfully");
		      
		      log.debug("Going to schedule the PoolValidationTimer job");
		      sched.scheduleJob( poolValidationTimerJobDetail  , poolValidationTimerTrigger );
		      log.debug("PoolValidationTimer job is scheduled successfully");
		      
		      log.debug("Going to schedule the AlarmPoolValidationTimer job");
		      sched.scheduleJob( alarmPoolValidationTimerJobDetail  , alarmPoolValidationTimerTrigger );
		      log.debug("AlarmPoolValidationTimer job is scheduled successfully");
		      
		      log.debug("Going to schedule the DynamicPoolTimer job");
		      sched.scheduleJob( dynamicPoolTimerJobDetail  , dynamicPoolTimerTrigger );
		      log.debug("DynamicPoolTimer job is scheduled successfully");

		      /// Add the JOB to the scheduled task
		      log.debug("Going to schedule the SMS Broadcaster job");
		      sched.scheduleJob( smsBroadcastJobDetail , smsBroadcastTimerTrigger );
		      log.debug("SMS Broadcaster job is scheduled successfully");
		      
		      
		      log.debug("Going to schedule the TadawulTimer job");
		      sched.scheduleJob( tadawulMigrationTimerJobDetail  , tadawulMigrationTimerTrigger );
		      log.debug("TadawulTimer job is scheduled successfully");

		      log.debug("Going to schedule the Report job");
		      //sched.scheduleJob( reportsTimerJobDetail  , reportsTimerTrigger );
		      log.debug("TadawulTimer job is Report successfully");		      
		      
		      log.debug("Going to schedule the Keeper Corp DWH job");
		      sched.scheduleJob( keeperCorpDWHTimerJobDetail  , keeperCorpDWHTimerTrigger );
		      log.debug("Keeper Corp DWH job is started successfully");


		      log.debug("Going to schedule the Keeper Corp Synch job");
		      sched.scheduleJob( keeperCorpSynchTimerJobDetail  , keeperCorpSynchTimerTrigger );
		      log.debug("Keeper Corp Synch job is started successfully");		      
		      		      
		      
		      log.debug("Going to schedule the Keeper Line Synch job");
		      sched.scheduleJob( keeperLineTimerJobDetail  , keeperLineTimerTrigger );
		      log.debug("Keeper Line Synch job is started successfully");		
		      
		      log.debug("Going to schedule the DWH Keeper Line Synch job");
		      sched.scheduleJob( keeperDWHLineTimerJobDetail  , keeperDWHLineTimerTrigger );
		      log.debug("Keeper Line DWH Synch job is started successfully");		

		      
		      /// Add the JOB to the scheduled task
		      log.debug("Going to schedule the SIEBEL-BMS Outlet Syhncrminzer job");
		      //sched.scheduleJob( bmsOutletTimerJobDetail  , bmsOutletTimerTrigger );
		      log.debug("SIEBEL-BMS Outlet Syhncrminzer job is scheduled successfully");
		      
		      
		      log.debug("Going to schedule the Wholesale Payment Status Updater job");
		      sched.scheduleJob( wholesalePaymentStatusUpdaterJobDetail  , wholesalePaymentStatusUpdaterTrigger );
		      log.debug("Wholesale Payment Status Updater job is started successfully");
		      
		      
		      log.debug("Going to schedule the Wholesale Transaction Notification job");
		      sched.scheduleJob( wholesaleTransactionNotificationJobDetail  , wholesaleTransactionNotificationTrigger );
		      log.debug("Wholesale Transaction Notification job is started successfully");
		      
		      log.debug("Going to schedule the BMS Users Synchronizer");
		      sched.scheduleJob( bmsUsersSynchronizerJobDetail  , bmsUsersSynchronizerTrigger );
		      log.debug("BMS Users Synchronizer is started successfully");
		      
		      log.debug("Going to schedule the MNP Alarm Timer job");
		      sched.scheduleJob( mnpAlarmTimerJobDetail  , mnpAlarmTimerTrigger );
		      log.debug("MNP Alarm Timer job is started successfully");
		      		      
		      log.debug("Going to schedule the Welcome Call Balance Update Timer job");
//		      sched.scheduleJob( welcomeCallBalanceUpdateTimerJobDetail  , welcomeCallBalanceUpdateTimerTrigger );
		      log.debug("Welcome Call Balance Update Timer job is started successfully");
		      		      
		      log.debug("Going to schedule the SLA Violation Alarm Timer job");
		      sched.scheduleJob( slaViolationAlarmTimerJobDetail  , slaViolationAlarmTimerTrigger );
		      log.debug("SLA Violation Alarm Timer job is started successfully");
		      
		      log.debug("Going to schedule the Welcome Call Pool Buffering Timer job");
		      sched.scheduleJob( welcomeCallPoolBufferingTimerJobDetail  , welcomeCallPoolBufferingTimerTrigger );
		      log.debug("Welcome Call Pool Buffering Timer job is started successfully");
		      
		      log.debug("Going to schedule the UserMigrationTimer job");
		      sched.scheduleJob( userMigrationTimerJobDetail  , userMigrationTimerTrigger );
		      log.debug("UserMigrationTimer job is scheduled successfully");		      
		      
		      
		     		      
		      /// Add the JOB to the scheduled task
		     // sched.scheduleJob( backLogPaymentStatTimerJobDetail  , backLogPaymentStatTimerTrigger );
		}catch(Exception ex)
		{
			log.error("FAILED TO START THE SCHEDULED TASKS" ,ex);	
			ex.printStackTrace();
		}    	
    }	

}

testing
package com.mobily.followup;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import java.net.URL;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz
import com.mobily.cachemanager.base.MobilyCacheManager;
import com.mobily.followup.common.mail.MailRobot;
import com.mobily.followup.db.BillingDBFacade;
import com.mobily.followup.db.DataWarehouseDBFacade;
import com.mobily.followup.db.FollowUpDBPoolsLoader;
import com.mobily.followup.db.FollowupDBFacade;
import com.mobily.followup.db.SiebelDBFacade;
import com.mobily.followup.service.bulkaction.BulkActionTimer;
import com.mobily.followup.service.bulkaction.line.BulkActionAdjustINFinalReplyListener;
import com.mobily.followup.service.bulkaction.line.BulkActionCustomLimitFinalReplyListener;
import com.mobily.followup.service.bulkaction.line.BulkActionGraceFinalReplyListener;
import com.mobily.followup.service.bulkaction.line.BulkActionNSAFinalReplyListener;
import com.mobily.followup.service.bulkaction.line.BulkActionSusFinalReplyListener;
import com.mobily.followup.service.bulkaction.line.BulkActionUnSusFinalReplyListener;
import com.mobily.followup.service.bulkaction.line.BulkActionWIMAXSusFinalReplyListener;
import com.mobily.followup.service.bulkaction.line.BulkActionWIMAXUnsusFinalReplyListener;
import com.mobily.followup.service.bulkaction.supplementary.BulkBISForemanFinalReplyListener;
import com.mobily.followup.service.bulkaction.supplementary.BulkSoftPhoneFinalReplyListener;
import com.mobily.followup.service.bulkaction.supplementary.SupplementaryServiceFinalReplyListener;
import com.mobily.followup.service.bulkaction.supplementary.SupplementaryServiceFinalStatusTimer;
import com.mobily.followup.service.collection.CollectionListener;
import com.mobily.followup.service.creditlimit.SetCustomLimitValidatorListener;
import com.mobily.followup.service.finalreply.NSAFinalReplyListener;
import com.mobily.followup.service.finalreply.SuspenseFinalReplyListener;
import com.mobily.followup.service.finalreply.UnSuspenseFinalReplyListener;
import com.mobily.followup.service.fms.CCFMSAlarmListener;
import com.mobily.followup.service.fms.FMSAlarmListener;
import com.mobily.followup.service.fms.FMSCloseAlarmListener;
import com.mobily.followup.service.fms.NewFMSAlarmListener;
import com.mobily.followup.service.payment.FollowupPaymentListener;
import com.mobily.followup.service.payment.PaymentNotificationTimer;
import com.mobily.followup.service.pool.DynamicPoolTimer;
import com.mobily.followup.service.pool.MNPPoolSynchServiceListener;
import com.mobily.followup.service.pool.PoolCustomerValidatorTimer;
import com.mobily.followup.service.pool.PoolExpiryTimer;
import com.mobily.followup.service.pool.PoolServiceListener;
import com.mobily.followup.service.pool.WCMNPPoolListener;
import com.mobily.followup.service.ptp.ExpirePtpTimer;
import com.mobily.followup.service.ptp.PTPServiceListener;
import com.mobily.followup.service.report.CollectionCustomerReportTimer;
import com.mobily.followup.service.s1100sms.Siebel1100SMSAdminListener;
import com.mobily.followup.service.shutdownhook.ShutdownHook;
import com.mobily.followup.service.simah.SimahInquiryListener;
import com.mobily.followup.service.simah.SimahServiceRequestListener;
import com.mobily.followup.service.unlock.AutomaticUnlockTimer;
import com.mobily.followup.service.util.CCProperties;
import com.mobily.followup.service.util.CacheManager;
import com.mobily.followup.service.util.CreditLimitUtil;
import com.mobily.followup.service.util.FollowupUploadTimer;
import com.mobily.followup.service.util.Util;
import com.mobily.followup.service.welcomecall.WelcomeCallPoolCustomerPickListener;
import com.mobily.mbi.be.bms.serviceorder.CancelServiceOrderListener;
import com.mobily.mbi.be.corporate.CorporateMigrationTimer;
import com.mobily.mbi.be.corporate.CorporateStatisticsSynchronizer;
import com.mobily.mbi.be.facade.bms.BMSFacade;
import com.mobily.mbi.be.finance.TadawulTimer;
//import com.mobily.mbi.be.security.MGateUserMigrationTimer;
import com.mobily.mbi.be.partnerwholesale.service.WimaxMediationResponseListener;
import com.mobily.mbi.be.security.UserMigrationTimer;
import com.mobily.mbi.be.smsbroadcast.CampaignListener;
import com.mobily.mbi.be.webaudit.WebAuditListener;
import com.mobily.mbi.be.webaudit.WebAuditListenerFile;
import com.mobily.mq.util.MQHelper;

public class MBIListenersMainInt {

	// Define a static logger variable
	private static final Logger log = Logger.getLogger(MBIListenersMainInt.class);

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
			
			CCProperties.customerToSIMAHIdTypes  =  CreditLimitUtil.loadSIMAHIdTypeToSiebelIdTypeMapping();
			
			
			CCProperties.allGroupTypesMap = facade.findAllGroupTypes();
			CCProperties.allGroupValuesMap = facade.findAllGroupValues();
			CCProperties.allExternalNotifyTypesMap = facade.getAllExternalNotifyTypes();
			CCProperties.allExternalRuleDescToNotifyTypesMap = facade.getAllExternalRuleDescToNotifyTypes();
//			CCProperties.allMCRActivePackages = BMSFacade.getInstance().findAllActivePackages();
			CCProperties.allPromoFrameworkReturnCodes = CreditLimitUtil.loadPromoFrameworkReturnCodes();
				
			FollowupPaymentListener followupPaymentListener = new FollowupPaymentListener();
//			followupPaymentListener.listenToMQ("<EE_EAI_MESSAGE><ReferenceNumber>1-13358394734</ReferenceNumber><AccountNumber>1000112261614773</AccountNumber><LineNumber>966546869680</LineNumber><RequestorChannelId>CRM</RequestorChannelId><EAIReference>MOBILY212228</EAIReference><Amount>13631</Amount><PaymentDateTime>2012-12-17 14:54:50.000000000</PaymentDateTime><PaymentType>02</PaymentType><PaymentMode>01</PaymentMode><ChannelId>CRM</ChannelId><PaymentReason>18</PaymentReason></EE_EAI_MESSAGE>");
			
//			new Siebel1100SMSAdminListener().listenToMQ("<?xml version=\"1.0\" encoding=\"UTF-8\"?><EE_EAI_MESSAGE><EE_EAI_HEADER><MsgFormat>1100SMS_TO_BMS</MsgFormat><MsgVersion>0000</MsgVersion><Operation>Add</Operation><RequestorChannelId>SIEBEL</RequestorChannelId><RequestorChannelFunction></RequestorChannelFunction><RequestorUserId></RequestorUserId><RequestorLanguage></RequestorLanguage><RequestorSecurityInfo></RequestorSecurityInfo></EE_EAI_HEADER><TemplateId></TemplateId><MainServiceRowId>1-67FY5VD</MainServiceRowId><SubServiceRowId>1-67FY5VG</SubServiceRowId><ServiceTypeRowId>1-67FY5VJ</ServiceTypeRowId><SMSRowId></SMSRowId><Language>English</Language><MainService>Country</MainService><SubService>State</SubService><ServiceType>City</ServiceType><SMSScript></SMSScript></EE_EAI_MESSAGE>");
//			new Siebel1100SMSAdminListener().listenToMQ("<?xml version=\"1.0\" encoding=\"UTF-8\"?><EE_EAI_MESSAGE><EE_EAI_HEADER><MsgFormat>1100SMS_TO_BMS</MsgFormat><MsgVersion>0000</MsgVersion><Operation>Update</Operation><RequestorChannelId>SIEBEL</RequestorChannelId><RequestorChannelFunction></RequestorChannelFunction><RequestorUserId></RequestorUserId><RequestorLanguage></RequestorLanguage><RequestorSecurityInfo></RequestorSecurityInfo></EE_EAI_HEADER><TemplateId></TemplateId><MainServiceRowId>1-67GFYV5</MainServiceRowId><SubServiceRowId></SubServiceRowId><ServiceTypeRowId></ServiceTypeRowId><SMSRowId></SMSRowId><Language>English</Language><MainService>Dubai</MainService><SubService></SubService><ServiceType></ServiceType><SMSScript></SMSScript></EE_EAI_MESSAGE>");
			
//			log.info("Going to Start the runWebAuditServiceListener ");
//			runMBIWebAuditServiceListener();
//			runMBIWebAuditFileServiceListener();
//			log.debug("Starting the runWebAuditServiceListener is done");						
//			
			log.info("Going to Start the CCFMSAlarmListener ");
			runCCFMSAlarmListener();
			log.debug("Starting the CCFMSAlarmListener is done");	
			
			log.info("Going to Start Pool Services Listener ");
			runPoolServicesListener();
			log.debug("Starting Pool Services is done");
//			
//			log.info("Going to Start PTP Services Listener ");
//			runPTPServicesListener();
//			log.debug("Starting PTP Services is done");
//			
//			log.info("Going to Start Automatic Unlock Timer ");
//			//*//AutomaticUnlockTimer automaticUnlockTimer = new AutomaticUnlockTimer();
//			log.debug("Starting Automatic Unlock Timer is done");
//								
//			log.info("Going to Start NSA Final Reply Listener ");
//			runNSAFinalReplyListener();
//			log.debug("Starting NSA Final Reply Listener is done");		
//			
//			log.info("Going to Start Suspense Final Reply Listener ");
//			runSuspenseFinalReplyListener();
//			log.debug("Starting Suspense Final Reply Listener is done");			
//			
//			log.info("Going to Start UnSuspense Final Reply Listener ");
//			runUnSuspenseFinalReplyListener();
//			log.debug("Starting UnSuspense Final Reply Listener is done");
//			
//
//			log.info("Going to Start Wimax Suspense Final Reply Listener ");
//			runBulkActionWIMAXSusFinalReplyListener();
//			log.debug("Starting Wimax Suspense Final Reply Listener is done");			
//
//			log.info("Going to Start Wimax UnSuspense Final Reply Listener ");
//			runBulkActionWIMAXUnsusFinalReplyListener();
//			log.debug("Starting Wimax UnSuspense Final Reply Listener is done");
//			
			log.info("Going to Start Payment Listener ");
			runPaymentListener();
//			PaymentNotificationTimer paymentTimer = new PaymentNotificationTimer();
//
//			log.info("Going to Start Collection Listener ");
//			runCollectionListener();
//
//			log.info("Going to Start the Mail Robot ");
//			//*//MailRobot mailRobot = new MailRobot();
//			log.debug("Starting the Mail Robot is done");
		
/////////////////////////////////////////////
			
			log.info("Going to Start the BulkActionCustomLimitFinalReplyListener ");
			runBulkActionCustomLimitFinalReplyListener();
			log.debug("Starting the BulkActionCustomLimitFinalReplyListener is done");
			
//			log.info("Going to Start the BulkActionGraceFinalReplyListener ");
//			runBulkActionGraceFinalReplyListener();
//			log.debug("Starting the BulkActionGraceFinalReplyListener is done");			
			
			log.info("Going to Start the BulkActionNSAFinalReplyListener ");
			runBulkActionNSAFinalReplyListener();
			log.debug("Starting the BulkActionNSAFinalReplyListener is done");			
			
			log.info("Going to Start the BulkActionSusFinalReplyListener ");
			runBulkActionSusFinalReplyListener();
			log.debug("Starting the BulkActionSusFinalReplyListener is done");			
			
			log.info("Going to Start the BulkActionUnSusFinalReplyListener ");
			runBulkActionUnSusFinalReplyListener();
			log.debug("Starting the BulkActionUnSusFinalReplyListener is done");
			
			log.info("Going to Start the BulkActionAdjustINFinalReplyListener ");
			runBulkActionAdjustINFinalReplyListener();
			log.debug("Starting the BulkActionAdjustINFinalReplyListener is done");
						
			log.info("Going to Start the BulkActionSoftPhoneFinalReplyListener ");
			runBulkActionSoftPhoneFinalReplyListener();
			log.debug("Starting the BulkActionSoftPhoneFinalReplyListener is done");
			
			log.info("Going to Start the BulkActionBISForemanFinalReplyListener() ");
			runBulkActionBISForemanFinalReplyListener();
			log.debug("Starting the BulkActionBISForemanFinalReplyListener is done");				
			
			log.info("Going to Start the SupplementaryServiceFinalReplyListener ");
			runSupplementaryServiceFinalReplyListener();
			log.debug("Starting the SupplementaryServiceFinalReplyListener is done");
			
			log.info("Going to Start MNP Pool Synch Service Listener");
			runMNPPoolSynchServiceListener();
			log.debug("Starting the MNP Pool Synch Service Listener is done");
			
			log.info("Going to Start MNP WC Pool Port In  Listener");
			runWCMNPPoolListener();
			log.debug("Starting MNP WC Pool Port In  Listener is done");
			
			
			log.info("Going to Start Supplementary Service Final Status Timer ");
			//*//SupplementaryServiceFinalStatusTimer suppServiceFinalStatusTimer = new SupplementaryServiceFinalStatusTimer();
			log.debug("Starting Supplementary Service Final Status Timer is done");
			
			log.info("Going to Start the BulkActionTimer ");
			//*//BulkActionTimer bulkActionTimer = new BulkActionTimer();
			log.debug("Starting the BulkActionTimer is done");

///////////////////////////////////////////////						
////			log.info("Going to Start the FMSAlarmListener ");
////			runFMSAlarmListener();
////			log.debug("Starting the FMSAlarmListener is done");
//			
			log.info("Going to Start the NewFMSAlarmListener ");
			runNewFMSAlarmListener();
			log.debug("Starting the NewFMSAlarmListener is done");	
			

//
//			log.info("Going to Start the FMSCloseAlarmListener ");
//			runFMSCloseAlarmListener();
//			log.debug("Starting the FMSCloseAlarmListener is done");
//						
//			log.info("Going to Start the runSimahServiceRequestListener ");
//			runSimahServiceRequestListener();
//			log.debug("Starting the runSimahServiceRequestListener is done");
//			
//			log.info("Going to Start the runSimahInquiryListener ");
//			runSimahInquiryListener();
//			log.debug("Starting the runSimahInquiryListener is done");
//			
//			log.info("Going to Start the Cancel BMS Service Order Listener ");
//			runCancelBmsServiceOrderListener();
//			log.debug("Starting the Cancel BMS Service Order Listener is done");
//
//
//			log.info("Going to Start the BMS Campaign Service Listener ");
//			runMBICampaignServiceListener();
//			log.debug("Starting the BMS Campaign Service Listener is done");
//			
//			log.info("Going to Start the Wimax Mediation Response Listener ");
//			runWimaxMediationResponseListener();
//			log.debug("Starting the Wimax Mediation Response Listener is done");
//			
//			log.info("Going to Start Scheduled Tasks ");
//			//*//startScheduledTasks();
//			log.debug("Starting Scheduled Tasks is done");
//			
//			log.info("Going to Start the runSetCustomLimitValidatorListener ");
//			runSetCustomLimitValidatorListener();
//			log.debug("Starting the runSetCustomLimitValidatorListener is done");
//			
//			log.info("Going to Start the runSiebel1100SMSAdminListener ");
//			runSiebel1100SMSAdminListener();
//			log.debug("Starting the runSiebel1100SMSAdminListener is done");
//			
//			log.info("Going to Start the runWelcomeCallPoolCustomerPickListener ");
//			runWelcomeCallPoolCustomerPickListener();
//			log.debug("Starting the runWelcomeCallPoolCustomerPickListener is done");
//			
//			log.info("Going to Start the FollowupUploadTimer ");
//			FollowupUploadTimer followupUploadTimer=new FollowupUploadTimer();
//			log.debug("Starting the FollowupUploadTimer is done");
			
			/*				
			CorporateStatisticsSynchronizer corporateStatisticsSynchronizer = new CorporateStatisticsSynchronizer();
			corporateStatisticsSynchronizer.execute();
		
			MGateUserMigrationTimer mGateUserMigrationTimer = new MGateUserMigrationTimer();
			mGateUserMigrationTimer.execute();

*/
			
//			String reply = MQHelper.instance().sendToMQWithReply("<MOBILY_BSL_SR><SR_HEADER><FuncId>Create_PTP</FuncId><SecurityKey/><ChannelTransId>SR_14501145685</ChannelTransId><MsgVersion>0000</MsgVersion><RequestorChannelId>CRM</RequestorChannelId><SrDate>20100103182322</SrDate></SR_HEADER><MSISDN>966565663993</MSISDN><UserId>nabieh</UserId><PTPType>0</PTPType><SystemId>4</SystemId><FollowupReason>45</FollowupReason><Comment>User Comments</Comment><Installements><InstallementDetails><DueDate>20090314182322</DueDate><DueAmount>1000</DueAmount></InstallementDetails><InstallementDetails><DueDate>20090314182322</DueDate><DueAmount>4534</DueAmount></InstallementDetails></Installements></MOBILY_BSL_SR>", "MOBILY.FUNC.FUS.PTP.REQUEST", "MOBILY.FUNC.FUS.BULKACTION.BIS.FOREMAN.REPLY", "EESO131I");
//			MQHelper.instance().sendToMQ("<?xml version=\"1.0\" encoding=\"UTF-8\"?><EE_EAI_MESSAGE><EE_EAI_HEADER><MsgFormat>1100SMS_TO_BMS</MsgFormat><MsgVersion>0000</MsgVersion><Operation>Update</Operation><RequestorChannelId>SIEBEL</RequestorChannelId><RequestorChannelFunction></RequestorChannelFunction><RequestorUserId></RequestorUserId><RequestorLanguage></RequestorLanguage><RequestorSecurityInfo></RequestorSecurityInfo></EE_EAI_HEADER><TemplateId></TemplateId><MainServiceRowId>1-67GFYV5</MainServiceRowId><SubServiceRowId></SubServiceRowId><ServiceTypeRowId></ServiceTypeRowId><SMSRowId></SMSRowId><Language>English</Language><MainService>Dubai</MainService><SubService></SubService><ServiceType></ServiceType><SMSScript></SMSScript></EE_EAI_MESSAGE>", "MOBILY.FUNC.BMS.SMS.ADMIN.REQUEST");
//			System.out.println("reply from PTP: "+reply);

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
	
	public static void runSimahInquiryListener() {
		log.info("Simah Inquiry Listener .. Starting Action");
		try {
			
			log.info("Simah Inquiry Listener thread was started successfully.");
			int ClientCount = Integer.parseInt((String)CacheManager.getCachedValue("com.mobily.followup.simah.inquiry.threads"));
			for (int i = 0; i < ClientCount; i++) {
				Thread thread = new Thread(new SimahInquiryListener());
				thread.start();
				log.info("Simah Inquiry Listener thread #"+i+" was started successfully.");
			}			
		} catch (Exception e) {
			log.error("Exception ...", e);
		}	
	}	
	
	public static void runSetCustomLimitValidatorListener() {
		log.info("Set Custom Limit Validator Listener .. Starting Action");
		try {
			
			log.info("Set Custom Limit Validator Listener thread was started successfully.");
			int ClientCount = Integer.parseInt((String)CacheManager.getCachedValue("com.mobily.followup.customLimit.validator.threads"));
			for (int i = 0; i < ClientCount; i++) {
				Thread thread = new Thread(new SetCustomLimitValidatorListener());
				thread.start();
				log.info("Set Custom Limit Validator Listener thread #"+i+" was started successfully.");
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
	
	public static void runPTPServicesListener() {
		log.info("PTP Services .. Starting Action");
		try {
			
			log.info("PTP Services thread was started successfully.");
			int ClientCount = Integer.parseInt((String)CacheManager.getCachedValue("com.mobily.followup.ptpservices.threads"));
			for (int i = 0; i < ClientCount; i++) {
				Thread thread = new Thread(new PTPServiceListener());
				thread.start();
				log.info("PTP Services thread #"+i+" was started successfully.");
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
	
	public static void runBulkActionGraceFinalReplyListener() {
		log.info("BulkActionGraceFinalReplyListener .. Starting Action");
		try {
			int numOfThreads = Integer.parseInt((String)CacheManager.getCachedValue("com.mobily.followup.ba.grace.finalreply.threads"));
			
			for(int ncount=0 ; ncount < numOfThreads ; ncount++)
			{
				Thread thread = new Thread(new BulkActionGraceFinalReplyListener());
				thread.start();
				log.info("BulkActionGraceFinalReplyListener thread was started successfully.");
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
	
	public static void runBulkActionWIMAXSusFinalReplyListener() {
		log.info("Suspense Final Reply Listener .. Starting Action");
		try {
			int numOfThreads = Integer.parseInt((String)CacheManager.getCachedValue("com.mobily.cc.wimax.sus.finalreply.threads"));
			
			for(int ncount=0 ; ncount < numOfThreads ; ncount++)
			{
				Thread thread = new Thread(new BulkActionWIMAXSusFinalReplyListener());
				thread.start();
				log.info("Suspense Final Reply Listener thread was started successfully.");
			}
		} catch (Exception e) {
			log.error("Exception ...", e);
		}
	}	
	
	public static void runBulkActionWIMAXUnsusFinalReplyListener() {
		log.info("UnSuspense Final Reply Listener .. Starting Action");
		try {
			int numOfThreads = Integer.parseInt((String)CacheManager.getCachedValue("com.mobily.cc.wimax.unsuspense.finalreply.threads"));
			
			for(int ncount=0 ; ncount < numOfThreads ; ncount++)
			{
				Thread thread = new Thread(new BulkActionWIMAXUnsusFinalReplyListener());
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
	
	public static void runBulkActionSoftPhoneFinalReplyListener() {
		log.info("Soft Phone Final Reply Listener .. Starting Action");
		try {
			int numOfThreads = Integer.parseInt((String)CacheManager.getCachedValue("com.mobily.followup.ba.softphone.finalreply.threads"));
			
			for(int ncount=0 ; ncount < numOfThreads ; ncount++)
			{
				Thread thread = new Thread(new BulkSoftPhoneFinalReplyListener());
				thread.start();
				log.info("Soft Phone Final Reply Listener thread was started successfully.");
			}
		} catch (Exception e) {
			log.error("Exception ...", e);
		}
	}	
	
	public static void runBulkActionBISForemanFinalReplyListener() {
		log.info("BIS Foreman Final Reply Listener .. Starting Action");
		try {
			int numOfThreads = Integer.parseInt((String)CacheManager.getCachedValue("com.mobily.followup.ba.bis.foreman.finalreply.threads"));
			
			for(int ncount=0 ; ncount < numOfThreads ; ncount++)
			{
				Thread thread = new Thread(new BulkBISForemanFinalReplyListener());
				thread.start();
				log.info("BIS Foreman Final Reply Listener thread was started successfully.");
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
	
	public static void runNewFMSAlarmListener() {
		log.info("New FMS Alarm Listener .. Starting Action");
		try {
			int numOfThreads = Integer.parseInt((String)CacheManager.getCachedValue("com.mobily.followup.fms.alarm.threads"));
			
			for(int ncount=0 ; ncount < numOfThreads ; ncount++)
			{
				Thread thread = new Thread(new NewFMSAlarmListener());
				thread.start();
				log.info("New FMS Alarm Listener thread was started successfully.");
			}
		} catch (Exception e) {
			log.error("Exception ...", e);
		}
	}
	
	public static void runCCFMSAlarmListener() {
		log.info("CC FMS Alarm Listener .. Starting Action");
		try {
			int numOfThreads = Integer.parseInt((String)CacheManager.getCachedValue("com.mobily.followup.cc.fms.alarm.threads"));
			
			for(int ncount=0 ; ncount < numOfThreads ; ncount++)
			{
				Thread thread = new Thread(new CCFMSAlarmListener());
				thread.start();
				log.info("CC FMS Alarm Listener thread was started successfully.");
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
	
	public static void runMBIWebAuditFileServiceListener() {
		log.info("MBI WebAudit .. Starting Action");
		try {
			
			log.info("MBI WebAudite thread was started successfully.");
			int ClientCount = 5;//Integer.parseInt((String)CacheManager.getCachedValue("com.mobily.mbi.webaudit.threads"));
			for (int i = 0; i < ClientCount; i++) {
				Thread thread = new Thread(new WebAuditListenerFile());
				thread.start();
				log.info("MBI WebAudit #"+i+" was started successfully.");
			}			
		} catch (Exception e) {
			log.error("Exception ...", e);
		}	
	}	


	public static void runCancelBmsServiceOrderListener() {
		log.info("Cancel BMS Service Order Listener .. Starting Action");
		try {
			
			log.info("Cancel BMS Service Order Listener thread was started successfully.");
			int ClientCount = Integer.parseInt((String)CacheManager.getCachedValue("com.mobily.bms.cancel.service.order.threads"));
			for (int i = 0; i < ClientCount; i++) {
				Thread thread = new Thread(new CancelServiceOrderListener());
				thread.start();
				log.info("Cancel BMS Service Order Listener thread #"+i+" was started successfully.");
			}			
		} catch (Exception e) {
			log.error("Exception ...", e);
		}	
	}
	
	public static void runMBICampaignServiceListener() {
		log.info("MBI Campaign Service.. Starting Action");
		try {
			//int ClientCount = Integer.parseInt((String)CacheManager.getCachedValue("com.mobily.mbi.webaudit.threads"));
			int ClientCount=1;
			for (int i = 0; i < ClientCount; i++) {
				Thread thread = new Thread(new CampaignListener());
				thread.start();
				log.info("MBI Campaign Service#"+i+" was started successfully.");
			}			
		} catch (Exception e) {
			log.error("Exception ...", e);
		}	
	}	
	
	public static void runWimaxMediationResponseListener() {
		log.info("Wimax Mediation Response Service.. Starting Action");
		try {
			int ClientCount = Integer.parseInt((String)CacheManager.getCachedValue("com.mobily.bms.wimax.mediation.response.threads"));
			for (int i = 0; i < ClientCount; i++) {
				Thread thread = new Thread(new WimaxMediationResponseListener());
				thread.start();
				log.info("Wimax Mediation Response Service thread #"+i+" was started successfully.");
			}			
		} catch (Exception e) {
			log.error("Exception ...", e);
		}	
	}
	
	public static void runSiebel1100SMSAdminListener() {
		log.info("Set Custom Limit Validator Listener .. Starting Action");
		try {
			
			log.info("Siebel1100SMSAdminListener thread was started successfully.");
			int ClientCount = Integer.parseInt((String)CacheManager.getCachedValue("com.mobily.followup.service.1100sms.threads"));
			for (int i = 0; i < ClientCount; i++) {
				Thread thread = new Thread(new Siebel1100SMSAdminListener());
				thread.start();
				log.info("Siebel1100SMSAdminListener thread #"+i+" was started successfully.");
			}			
		} catch (Exception e) {
			log.error("Exception ...", e);
		}	
	}
	
		
	public static void runWelcomeCallPoolCustomerPickListener() {
		log.info("Welcome Call Pool Customer PickL istener .. Starting Action");
		try {
			
			log.info("WelcomeCallPoolCustomerPickListener thread was started successfully.");
			int ClientCount = Integer.parseInt((String)CacheManager.getCachedValue("com.mobily.followup.welocome.pool.customer.pick.listener.threads"));
			for (int i = 0; i < ClientCount; i++) {
				Thread thread = new Thread(new WelcomeCallPoolCustomerPickListener());
				thread.start();
				log.info("WelcomeCallPoolCustomerPickListener thread #"+i+" was started successfully.");
			}			
		} catch (Exception e) {
			log.error("Exception ...", e);
		}	
	}
	
	public static void runMNPPoolSynchServiceListener() {
		log.info("MNP Pool Synch Listener .. Starting Action");
		try {
		    log.info("MNP Pool Synch Listener thread was started successfully.");
			int ClientCount = Integer.parseInt((String)CacheManager.getCachedValue("com.mobily.followup.mnp.synch.service.threads"));
			for(int i = 0; i < ClientCount; i++)
			{
			    Thread thread = new Thread(new MNPPoolSynchServiceListener());
			    thread.start();
			    log.info((new StringBuilder("MNP Pool Synch Listener thread #")).append(i).append(" was started successfully.").toString());
			}
		
		} catch(Exception e) {
		    log.error("Exception ...", e);
		}
	}	
	
	private static void startScheduledTasks()
    {
		try {
			  String ccPoolExpiryTimerCronExpression 		= (String)CacheManager.getCachedValue("com.mobily.pool.expiry.timer.job");
			  String ccPtpExpiryTimerCronExpression 		= (String)CacheManager.getCachedValue("com.mobily.ptp.expiry.timer.job");
			  String ccPoolValidationTimerCronExpression 	= (String)CacheManager.getCachedValue("com.mobily.pool.validator.timer.job");
			  //String ccBackLogPaymentStatTimerCronExpression 	= CacheManager.getCachedValue("com.mobily.backlog.payments.timer.job").toString();
			  String ccDynamicPoolTimerCronExpression 		=  (String)CacheManager.getCachedValue("com.mobily.dynamic.pool.timer.job");
			  String ccTadawulTimerCronExpression 			= (String)CacheManager.getCachedValue("com.mobily.mbi.tadawul.timer.job");
			  String ccUserMigrationTimerCronExpression		= (String)CacheManager.getCachedValue("com.mobily.mbi.user.migration.timer.job");
			  String ccReportTimerCronExpression 			= (String)CacheManager.getCachedValue("com.mobily.mbi.collreport.timer.job");
			  
			  String ccKeeperCorpSynchExpression 			= (String)CacheManager.getCachedValue("com.mobily.mbi.keeper.corpsynch.timer.job");
			  String ccKeeperDWHCorpStatsExpression 	    = (String)CacheManager.getCachedValue("com.mobily.mbi.keeper.dwh.timer.job");
			  
			  
			  SchedulerFactory schedFact = new org.quartz.impl.StdSchedulerFactory();
		      Scheduler sched = schedFact.getScheduler();
		      sched.start();
		      /// Define the BackLog Payment Stat Timer JOB DETAILS
/*		      JobDetail backLogPaymentStatTimerJobDetail   = new JobDetail( "BackLog_Payment_Stat_Timer_JOB" , Scheduler.DEFAULT_GROUP, BackLogPaymentStatTimer.class );
		      CronTrigger backLogPaymentStatTimerTrigger   = new CronTrigger("BackLog_Payment_Stat_Timer_TRIGGER_NAME" , Scheduler.DEFAULT_GROUP, ccBackLogPaymentStatTimerCronExpression );		      		      	      		      		      
*/

		      /// Define the Pool Expiry Timer JOB DETAILS
		      JobDetail poolExpiryTimerJobDetail   = new JobDetail( "Pool_Expiry_Timer_JOB" , Scheduler.DEFAULT_GROUP, PoolExpiryTimer.class );
		      CronTrigger poolExpiryTimerTrigger   = new CronTrigger("Pool_Expiry_Timer_TRIGGER_NAME" , Scheduler.DEFAULT_GROUP, ccPoolExpiryTimerCronExpression );
		      
		      /// Define the PTP Expiry Timer JOB DETAILS
		      JobDetail ptpExpiryTimerJobDetail   = new JobDetail( "Ptp_Expiry_Timer_JOB" , Scheduler.DEFAULT_GROUP, ExpirePtpTimer.class );
		      CronTrigger ptpExpiryTimerTrigger   = new CronTrigger("Ptp_Expiry_Timer_TRIGGER_NAME" , Scheduler.DEFAULT_GROUP, ccPtpExpiryTimerCronExpression );
		      
		      /// Define the Pool Validator Timer JOB DETAILS
		      JobDetail poolValidationTimerJobDetail   = new JobDetail( "Pool_Validation_Timer_JOB" , Scheduler.DEFAULT_GROUP, PoolCustomerValidatorTimer.class );
		      CronTrigger poolValidationTimerTrigger   = new CronTrigger("Pool_Validation_Timer_TRIGGER_NAME" , Scheduler.DEFAULT_GROUP, ccPoolValidationTimerCronExpression );		      
	      		     
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
		      
		      log.debug("Going to schedule the DynamicPoolTimer job");
		      sched.scheduleJob( dynamicPoolTimerJobDetail  , dynamicPoolTimerTrigger );
		      log.debug("DynamicPoolTimer job is scheduled successfully");

		      
		      log.debug("Going to schedule the TadawulTimer job");
		      sched.scheduleJob( tadawulMigrationTimerJobDetail  , tadawulMigrationTimerTrigger );
		      log.debug("TadawulTimer job is scheduled successfully");

		      log.debug("Going to schedule the Report job");
		      sched.scheduleJob( reportsTimerJobDetail  , reportsTimerTrigger );
		      log.debug("TadawulTimer job is Report successfully");		      
		      
		      log.debug("Going to schedule the Keeper Corp DWH job");
		      sched.scheduleJob( keeperCorpDWHTimerJobDetail  , keeperCorpDWHTimerTrigger );
		      log.debug("Keeper Corp DWH job is started successfully");


		      log.debug("Going to schedule the Keeper Corp Synch job");
		      sched.scheduleJob( keeperCorpSynchTimerJobDetail  , keeperCorpSynchTimerTrigger );
		      log.debug("Keeper Corp Synch job is started successfully");		      
		      		      
		      
		      /*		      
			      log.debug("Going to schedule the UserMigrationTimer job");
			      sched.scheduleJob( userMigrationTimerJobDetail  , userMigrationTimerTrigger );
			      log.debug("UserMigrationTimer job is scheduled successfully");
	         */		      
		      
		      
		      
		      /// Add the JOB to the scheduled task
		     // sched.scheduleJob( backLogPaymentStatTimerJobDetail  , backLogPaymentStatTimerTrigger );
		}catch(Exception ex)
		{
			log.error("FAILED TO START THE SCHEDULED TASKS" ,ex);	
			ex.printStackTrace();
		}    	
	}	
	
	public static void runWCMNPPoolListener() {
		log.info("MNP Pool Synch Listener .. Starting Action");
		try {		
			log.info("WC MNP Pool Listener thread was started successfully.");
			int ClientCount = Integer.parseInt((String)CacheManager.getCachedValue("com.mobily.followup.wc.mnp.port.in.service.threads"));
			for (int i = 0; i < ClientCount; i++) {
				Thread thread = new Thread(new WCMNPPoolListener());
				thread.start();
				log.info("WC MNP Pool Listener thread #"+i+" was started successfully.");
			}			
		} catch (Exception e) {
			log.error("Exception ...", e);
		}	
	}	
}

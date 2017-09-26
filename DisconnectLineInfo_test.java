dfjsdjfsfjsdfj
dsffasdfdsfsdfsdfasdf
fsdfasdfsdfsdsdfsdf
dfsjdfkjsdfjldjfsdjf
fsdfasfasfsdfsdfdsffdf//testint//
testing
testing from home network
//test//
cool
public class CollectionMobileDataDeactivationSyncReplyListener extends Thread implements CreditControlIfc{
	private static final Logger log = Logger.getLogger(CollectionMobileDataDeactivationSyncReplyListener.class);
	
//	static{
//		CCProperties.p_Settings =  Util.readProperty("config/smsintegration");
//	}

	private String listenQueue   	= (CCProperties.queueProperties != null && CCProperties.queueProperties.get("com.mobily.col.eai.md.line.deactivate.async.queue") != null) ? CCProperties.queueProperties.get("com.mobily.col.eai.md.line.deactivate.async.queue").toString() : null;
	private static LineDeactivationReplyParser ldParser = new LineDeactivationReplyParser();
	Timestamp nowTime 				= null;
	HuccDBFacade db_HuccDBFacade    = null;
	HuccServices  huccServices		= null;

	public CollectionMobileDataDeactivationSyncReplyListener() {
		db_HuccDBFacade = new HuccDBFacade("pool");
		huccServices = new HuccServices(db_HuccDBFacade, null);
	}

	public void run() {
		while (true) {
			try {
				log.info("[MD LD] Start of listen MQ "+listenQueue  );
				listenToMQ();
				log.info("[MD LD]End processing Message"+listenQueue );
			}
			catch (Exception e) {
				log.error("[EXCEPTION] Exception In run during listen to queue"+listenQueue);
				e.printStackTrace();
			}
		}
	}

 	private void listenToMQ() throws Exception {
 		MQStringMessage mqStringMessage = null;
 		String resMsg = null;
 		
		try {
			log.info(" Start Listen to MQ Queue: "  + listenQueue);
			mqStringMessage = MQHelper.instance().listenToMQ(listenQueue);
			log.info("[MD LD] RECIEVED MESSAGE ["+mqStringMessage.getMesseageContent()+"]");
			
			LineDeactivationReplyInfo ldResp = null;
			try {
				resMsg = mqStringMessage.getMesseageContent();
				// resMsg = getMsg();
				ldResp = ldParser.parseLineDeactivationReplyInfo(resMsg);
			}
			catch(Exception ex) {
				log.error("[EXCEPTION]Failed to parse SUSPENSE ASync Reply Message");
				ex.printStackTrace();
				throw ex;
			}

			processMessage(ldResp ,resMsg);
			log.info("END PROCESSING SUSPENSE SYNC REPLY MESSAGE ["+resMsg+"]");
		}
		catch (MQListenException ex) {
			log.error("[EXCEPTION]An MQSeries error occurred : Completion code " + ex.getMessage() );
			log.error(ex);
			ex.printStackTrace();
			throw ex;
		} 
		catch (Exception ex)  {
			log.error("[EXCEPTION]Exception in Listen to MQ",ex);
			ex.printStackTrace();
			throw ex;
		}
	}

 	private void processMessage(LineDeactivationReplyInfo ldResp, String message) {
 		CollectionTransInfo collectionTransInfo = null;
 		
 		CollectionMobileDataMainInfo customerMDStatus = null;
		CustomerStatusInfo statusInfo = null;
		GregorianCalendar calendar = new GregorianCalendar();
		Date now = calendar.getTime();
		nowTime = new Timestamp(now.getTime());
		String msisdn = null, runMode = null; //, comments = null, funcName = null;
		int statusId = -1, reasonId = -1;

		try {
			if(ldResp != null ) {
				log.info("DEBUG ACTION: LINE DEACTIVATION RECEIVED SYNC REPLY MESSAGE [" +message+"]"+ ldResp);
				
				String channelTransId = ldResp.getChannelTransId();
				if(channelTransId != null){
					// update record in temp table
					collectionTransInfo = db_HuccDBFacade.getColTransByChannelTransId(channelTransId);
					if(collectionTransInfo != null)
						msisdn = collectionTransInfo.getMsisdn();
					
					statusInfo = db_HuccDBFacade.getCustomerStatusInfo(msisdn, null);
					int oldReason = 0;
					// int oldStatus = 1;
					
					if(statusInfo != null) {
						oldReason = statusInfo.getReasonID();
						// oldStatus = statusInfo.getStatus();
					}

					if(collectionTransInfo != null){
						runMode = collectionTransInfo.getBatchMode();
						collectionTransInfo.setBeStatus(ldResp.getSrStatus());
						collectionTransInfo.setReplyMsg(message);
						collectionTransInfo.setReturnMsg(ldResp.getErrorCode() + " "+ ldResp.getErrorMsg());
						collectionTransInfo.setSrId(ldResp.getServiceRequestId());
						db_HuccDBFacade.updateCollectionSyncReplyInfo(collectionTransInfo);
						huccServices.handleRetryTrans(collectionTransInfo);
					}

					if(runMode != null && !runMode.equals("") && runMode.equals(MD_COLL_GP4)){
						String logstatus = "INITIAL";
						customerMDStatus = db_HuccDBFacade.getMobileDataCollectionDetails(channelTransId, runMode);
						log.info("["+runMode+"] MESSAGE REPLY ["+message+"]");
	
						if(customerMDStatus == null && customerMDStatus == null){
							log.info("[SKIP COL DB UPDATE] REPLY MSG["+message+"] MOBILE DATA INFO IS NOT FOUND");
							return;
						}
						else{
							if(customerMDStatus != null)
								msisdn = customerMDStatus.getMsisdn();
						}

						if(ldResp.getSrStatus() == BSLConstantsIfc.BSL_STATUS_SUCCEDED)
							logstatus = MD_COLL_GP4_ACTION_SUCCESS;
						else
							logstatus = MD_COLL_GP4_ACTION_FAILURE;
	
						log.info("["+runMode+"] MOBILE DATA DEACTIVATION ["+customerMDStatus.getDataSegment()+"] REPLY STATUS IS  ["+logstatus+"] FOR MSISDN ["+msisdn+"] CHANNEL TRANS ID ["+channelTransId+"]");
						
						updateDatabase(customerMDStatus, ldResp, logstatus, statusInfo, runMode);
					}
					
					log.info("["+runMode+"]TABLE UPDATED SUCCESSFULLY FOR CHANNEL_TRANS_ID ["+channelTransId+"] MSISDN ["+msisdn+"]");
				}
				else{
					log.info("["+runMode+"][DKIP DB UPDATE] CHANNEL_TRANS_ID ["+channelTransId+"] NOT FOUND FOR MSISDN["+msisdn+"] FROM REPLY MSG ["+message+"]");
				}
			}
			else {
				log.info("["+runMode+"] reply object is null for Message ["+message+"] -- Can Not Process SUSPENSE Sync Reply Message");
			}
		} 
		catch (Exception ex) {
			log.error("[EXCEPTION]["+runMode+"]Exception While Processing SYNC Reply Message ["+message+"]",ex);
			ex.printStackTrace();
		}
 	}

 	public void updateDatabase(CollectionMobileDataMainInfo customerMDStatus, LineDeactivationReplyInfo ldResp, String logstatus, CustomerStatusInfo statusInfo, String mode) {
 		int oldReason = 0;
		int oldStatus = 1;

		if(statusInfo != null) {
			oldReason = statusInfo.getReasonID();
			oldStatus = statusInfo.getStatus();
		}
		
		customerMDStatus.setGp4ErrorCode(""+ldResp.getErrorCode());
		customerMDStatus.setGp4ErrorMsg(ldResp.getErrorMsg());
		customerMDStatus.setGp4OldStatus(oldStatus);
		customerMDStatus.setGp4OldReason(oldReason);
		customerMDStatus.setGp4SrId(ldResp.getServiceRequestId());
		customerMDStatus.setGp4Status(logstatus);
		customerMDStatus.setIsGP4Processed(1);
		customerMDStatus.setIsGP4Sent(1);
		customerMDStatus.setGp4FinishTime(new Timestamp((new Date()).getTime()));
		db_HuccDBFacade.updateeMobileDataCollectionReply(customerMDStatus, mode);
 	}

// 	String getMsg(){
// 		String msg = "<MOBILY_BSL_SR_REPLY><SR_HEADER_REPLY><FuncId>DISCONNECT_LINE</FuncId><SecurityKey/><MsgVersion>0000</MsgVersion><RequestorChannelId>MNP</RequestorChannelId><ReplierChannelId>BSL</ReplierChannelId><SrDate>20150211125255</SrDate><SrRcvDate>20150211123442</SrRcvDate><SrStatus>6</SrStatus></SR_HEADER_REPLY><ChannelTransId>5996231</ChannelTransId><ServiceRequestId>SR_8791912975</ServiceRequestId><ReturnCode><ErrorCode>0</ErrorCode></ReturnCode><ErrorMsg/><UpdateInfo/></MOBILY_BSL_SR_REPLY>";
// 		return msg;
// 	}
// 	
// 	public static void main(String[] args) {
//		try {
//			PropertyUtil.loadDummyMainSystemProperties(CreditControlIfc.HUCC_SERVICE_TIMER);
//			
//			Thread tt = new Thread (new CollectionMobileDataDeactivationSyncReplyListener());
//			tt.start();
//		}
//		catch(Exception e) {
//			e.printStackTrace();
//		}
//	}
}

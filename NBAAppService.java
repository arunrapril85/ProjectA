testing
package com.mobily.mbi.appservice.customer;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import com.mobily.adapters.mqadapter.exception.TimeOutException;
import com.mobily.mbi.appservice.administration.MBIAdminAppService;
import com.mobily.mbi.appservice.serviceorder.BMSServiceOrderAppService;
import com.mobily.mbi.constant.BMSServicesIfc;
import com.mobily.mbi.constant.BackendNameIfc;
import com.mobily.mbi.constant.LoggersNamesIfc;
import com.mobily.mbi.dao.customer.NbaDAO;
import com.mobily.mbi.exception.BaseSystemException;
import com.mobily.mbi.exception.DataNotFoundException;
import com.mobily.mbi.exception.MBIMQException;
import com.mobily.mbi.resources.ApplicationResources;
import com.mobily.mbi.resources.PropertyConstantsIfc;
import com.mobily.mbi.util.LogHelper;
import com.mobily.mbi.util.MQHelper;
import com.mobily.mbi.util.NBAServicesDocHandler;
import com.mobily.mbi.valueobject.customer.nba.NBAChoiceVO;
import com.mobily.mbi.valueobject.customer.nba.NBACustomerProfileVO;
import com.mobily.mbi.valueobject.customer.nba.NBAHistoryVO;
import com.mobily.mbi.valueobject.customer.nba.NBAOfferVO;
import com.mobily.xmlmapping.nba.AdvisoryMessage;
import com.mobily.xmlmapping.nba.BodyRq;
import com.mobily.xmlmapping.nba.CommandImpl;
import com.mobily.xmlmapping.nba.DynamicParams;
import com.mobily.xmlmapping.nba.HistoryWSReturnType;
import com.mobily.xmlmapping.nba.MsgRqHdr;
import com.mobily.xmlmapping.nba.MsisdnType;
import com.mobily.xmlmapping.nba.NameValuePair;
import com.mobily.xmlmapping.nba.NameValuePairImpl;
import com.mobily.xmlmapping.nba.Offer;
import com.mobily.xmlmapping.nba.OfferList;
import com.mobily.xmlmapping.nba.PackageInfo;
import com.mobily.xmlmapping.nba.Param;
import com.mobily.xmlmapping.nba.PromoDetailes;
import com.mobily.xmlmapping.nba.Response;
import com.mobily.xmlmapping.nba.ExecuteBatchDocument.ExecuteBatch;
import com.mobily.xmlmapping.nba.ExecuteBatchResponseDocument.ExecuteBatchResponse;
import com.mobily.xmlmapping.nba.GetHistoryWSDocument.GetHistoryWS;
import com.mobily.xmlmapping.nba.GetHistoryWSResponseDocument.GetHistoryWSResponse;
import com.mobily.xmlmapping.nba.GetProfileResponseDocument.GetProfileResponse;
import com.mobily.xmlmapping.nba.SubmitCustomerChoiceRqDocument.SubmitCustomerChoiceRq;
import com.mobily.xmlmapping.nba.SubmitCustomerChoiceRsDocument.SubmitCustomerChoiceRs;
import com.mobily.xmlmapping.nba.impl.ParamImpl;
import com.mobily.xmlmapping.productinquiry.reply.FeeInfoVO;
import com.mobily.xmlmapping.productinquiry.reply.ProductInquiryReplyVO;
import com.mobily.xmlmapping.productinquiry.request.FeeIdsVO;
import com.mobily.xmlmapping.productinquiry.request.OutputFilterVO;
import com.mobily.xmlmapping.productinquiry.request.ProductInquiryRequestHeaderVO;
import com.mobily.xmlmapping.productinquiry.request.ProductInquiryRequestVO;
import com.mobily.xmlmapping.productinquiry.request.ProductVO;

import java.text.SimpleDateFormat;

public class NBAAppService {

    private NBAServicesDocHandler handler = null;

    private static final String SET_CALL_REASON_EVENT = "SET_CALL_REASON";
    private static final String HISTORIZE_CONTACT_EVENT = "HISTORIZE_CONTACT";
    private static final String RECORD_ACCEPTED_EVENT = "RECORD_ACCEPTED";
    private static final String RECORD_DECLINE_EVENT = "RECORD_DECLINE";
    private static final String RECORD_LATER_EVENT = "RECORD_LATER";

    private static final int STATUS_CODE_SUCCESS = 0;
    private static final int STATUS_CODE_WARNING = 1;

    private static Logger logger = LogHelper.getLogger(LoggersNamesIfc.NBALogger);

    public NBAAppService() {
        handler = NBAServicesDocHandler.getInstance();
    }

    public ArrayList startSessionAndGetOffers(String sessionId, String msisdn, String userLoginName,
                                              String interactiveChannel) throws DataNotFoundException {
        return startSessionAndGetOffers(sessionId, msisdn, userLoginName, interactiveChannel, "BMS");
    }

    public ArrayList startSessionAndGetOffers(String sessionId, String msisdn, String userLoginName, String interactiveChannel,
                                              String interactionPoint) throws DataNotFoundException {
        logger.debug("Calling startSessionAndGetOffers method with parameters[" + msisdn + ", " + userLoginName + ", " +
                     interactiveChannel + "]");
        ArrayList offersList = null;
        String requestMessage =
            startSessionAndGetOffersRequest(sessionId, msisdn, userLoginName, interactiveChannel, interactionPoint);
        ExecuteBatchResponse response = sendNBARequest(requestMessage);
        Response[] responses = response.getReturn().getResponsesArray();

        if (responses != null) {

            for (int i = 0; i < responses.length; i++) {
                if (responses[i].getStatusCode() != STATUS_CODE_SUCCESS &&
                    responses[i].getStatusCode() != STATUS_CODE_WARNING) {
                    AdvisoryMessage[] advMessages = responses[i].getAdvisoryMessagesArray();
                    for (int j = 0; j < advMessages.length; j++) {
                        String[] advMessage = getAdvisoryMessage(advMessages[j]);
                        if (advMessage != null) {
                            throw new BaseSystemException(advMessage[1]);
                        }
                    }
                }

            }
            offersList = populateOffersList(responses[1], responses[0].getSessionID());
        }


        if (offersList == null || offersList.size() == 0)
            throw new BaseSystemException("No offers avialable ");

        return offersList;

    }

    private ArrayList populateOffersList(Response offerResponse, String sessionId) throws DataNotFoundException {
        ArrayList offersList = new ArrayList();
        OfferList offers = offerResponse.getOfferList();
        NBAOfferVO offer = null;
        if (offers != null) {
            if (offers.getDefaultString() != null && !"".equals(offers.getDefaultString().trim()))
                throw new DataNotFoundException(offers.getDefaultString());
            Offer[] recOffers = offers.getRecommendedOffersArray();
            if (recOffers != null) {
                for (int i = 0; i < recOffers.length; i++) {
                    offer = new NBAOfferVO();
                    offer.setSessionId(sessionId);
                    offer.setName(recOffers[i].getOfferName());
                    offer.setDescription(recOffers[i].getDescription());
                    offer.setScore("" + recOffers[i].getScore());
                    offer.setTreatmentCode("" + recOffers[i].getTreatmentCode());
                    offer.setCode(recOffers[i].getOfferCodeArray(0));
                    NameValuePair[] attribs = recOffers[i].getAdditionalAttributesArray();
                    if (attribs != null) {
                        HashMap params = new HashMap();
                        for (int j = 0; j < attribs.length; j++) {
                            Method[] offerMethods = offer.getClass().getMethods();

                            for (int k = 0; k < offerMethods.length; k++) {
                                if (offerMethods[k].getName().startsWith("get")) {
                                    String attribName =
                                        offerMethods[k].getName().substring(3, 4).toLowerCase() +
                                        offerMethods[k].getName().substring(4);
                                    if (attribs[j].getName().equalsIgnoreCase(attribName)) {
                                        String setMethodName = offerMethods[k].getName().replaceAll("get", "set");
                                        Method setMethod = null;
                                        try {
                                            if (attribs[j].getValueDataType().equalsIgnoreCase("string")) {
                                                setMethod = offer.getClass().getDeclaredMethod(setMethodName, new Class[] {
                                                                                               String.class });
                                                setMethod.invoke(offer, new String[] { attribs[j].getValueAsString() });
                                            } else if (attribs[j].getValueDataType().equalsIgnoreCase("date")) {
                                                setMethod = offer.getClass().getDeclaredMethod(setMethodName, new Class[] {
                                                                                               Date.class });
                                                setMethod.invoke(offer, new Date[] {
                                                                 attribs[j].getValueAsDate().getTime() });
                                            }
                                        } catch (Exception ex) {
                                            throw new BaseSystemException("Error occured during populating an offer",
                                                                          ex, LoggersNamesIfc.NBALogger);

                                        }

                                    }
                                }
                            }

                            if (attribs[j].getName().equalsIgnoreCase("ACTIVATION_FEE"))
                                offer.setActivationCost("" + attribs[j].getValueAsNumeric());
                            if (attribs[j].getName().equalsIgnoreCase("DESTINATION_PACKAGE"))
                                offer.setDestinationPackage("" + attribs[j].getValueAsString());

                            if (attribs[j].getValueDataType().equalsIgnoreCase("string"))
                                params.put(attribs[j].getName(), attribs[j].getValueAsString());
                            else if (attribs[j].getValueDataType().equalsIgnoreCase("date"))
                                params.put(attribs[j].getName(), attribs[j].getValueAsDate().getTime());
                            else if (attribs[j].getValueDataType().equalsIgnoreCase("numeric"))
                                params.put(attribs[j].getName(), String.valueOf(attribs[j].getValueAsNumeric()));


                        }
                        offer.setParams(params);
                    }
                    offersList.add(offer);
                }

            }
        }
        return offersList;
    }

    public boolean endSession(String sessionId) {
        String requestMessage = endSessionRequest(sessionId);
        ExecuteBatchResponse response = sendNBARequest(requestMessage);
        return (response.getReturn().getResponsesArray()[0].getStatusCode() == STATUS_CODE_SUCCESS ||
                response.getReturn().getResponsesArray()[0].getStatusCode() == STATUS_CODE_WARNING);
    }

    public NBACustomerProfileVO getCustomerProfile(String sessionId) {
        String requestMessage = getProfileRequest(sessionId);
        ExecuteBatchResponse response = sendGetProfileRequest(requestMessage);


        NBACustomerProfileVO profile = new NBACustomerProfileVO();
        NameValuePair[] values = response.getReturn().getResponsesArray(0).getProfileRecordArray();
        if (values != null) {
            for (int i = 0; i < values.length; i++) {
                if (values[i].getName().equalsIgnoreCase("n_churn_value")) {
                    profile.setChurnScore(values[i].getValueAsNumeric());
                }
                if (values[i].getName().equalsIgnoreCase("n_customer_tenure")) {
                    profile.setTenure(values[i].getValueAsNumeric());
                }
                if (values[i].getName().equalsIgnoreCase("v_customer_value_segment")) {
                    profile.setCustomerValue(values[i].getValueAsString());
                }
            }
        }


        return profile;

    }

    public ArrayList getHistory(String msisdn) {
        ArrayList historyList = new ArrayList();
        String requestMessage = getHisttoryWsRequest(msisdn);
        GetHistoryWSResponse response = sendNBAHistoryRequest(requestMessage);
        HistoryWSReturnType[] historyData = response.getGetHistoryWSReturnArray();
        if (historyData != null) {
            NBAHistoryVO historyEntry = null;
            for (int i = 0; i < historyData.length; i++) {
                historyEntry = new NBAHistoryVO();
                historyEntry.setCallReason(historyData[i].getCallReason());
                historyEntry.setDate(historyData[i].getDate());
                historyEntry.setOfferName(historyData[i].getOfferName());
                historyEntry.setResponseType(historyData[i].getResponseType());

                historyEntry.setAgentId(historyData[i].getAgentId());
                historyEntry.setReason(historyData[i].getReason());

                historyList.add(historyEntry);
            }
        }

        return historyList;
    }

    public ArrayList postCallReasonEvent(String sessionId, String callReason) throws DataNotFoundException {
        return postCallReasonEvent(sessionId, callReason, "BMS");
    }

    public ArrayList postCallReasonEvent(String sessionId, String callReason,
                                         String interactionPoint) throws DataNotFoundException {
        logger.debug("Calling postCallReasonEvent method with parameters[" + sessionId + ", " + callReason + ", "+ interactionPoint +"]");


        ArrayList offersList = new ArrayList();
        HashMap parameters = new HashMap();

        parameters.put("CTX_CALL_REASON", callReason);

        String requestMessage = postEventRequest(sessionId, SET_CALL_REASON_EVENT, parameters, interactionPoint);
        ExecuteBatchResponse response = sendNBARequest(requestMessage);
        Response[] responses = response.getReturn().getResponsesArray();
        for (int i = 0; i < responses.length; i++) {
            AdvisoryMessage[] advMessages = responses[i].getAdvisoryMessagesArray();
            for (int j = 0; j < advMessages.length; j++) {
                if (responses[i].getStatusCode() != STATUS_CODE_SUCCESS &&
                    responses[i].getStatusCode() != STATUS_CODE_WARNING) {
                    String[] advMessage = getAdvisoryMessage(advMessages[j]);
                    if (advMessage != null) {
                        throw new BaseSystemException(advMessage[1]);
                    }
                }
            }
            if (i == 1) {
                offersList = populateOffersList(responses[i], sessionId);
            }

        }
        return offersList;

    }

    public ArrayList postAcceptEvent(String sessionId, NBAChoiceVO choice, String time) throws DataNotFoundException {
        return postAcceptEvent(sessionId, choice, time, "BMS");
    }

    public ArrayList postAcceptEvent(String sessionId, NBAChoiceVO choice, String time,
                                     String interactionPoint) throws DataNotFoundException {
        logger.debug("Calling postAcceptEvent method with parameters[" + sessionId + ", choice" + choice + ", " + time +
                     "]");

        ArrayList offersList = new ArrayList();
        HashMap parameters = new HashMap();
        parameters.put("UACIOfferTrackingCode", choice.getTreatmentCode());
        parameters.put("UACIResponseTypeCode", "CMT");
        parameters.put("CTX_CST_TRTMT_TIME", time);
        parameters.put("ValidationFlag", choice.getValidationFlag());
        parameters.put("RqUID", choice.getSrId());
        parameters.put("UACILogToLearning", new Integer("1"));

        String requestMessage =
            getCustomerChoiceRequest(sessionId, RECORD_ACCEPTED_EVENT, parameters, choice, interactionPoint);

        SubmitCustomerChoiceRs response = sendNBAChoiceRequest(requestMessage);
        Response[] responses = response.getBody().getExecuteBatchResponse().getReturnArray(0).getResponsesArray();
        for (int i = 0; i < responses.length; i++) {
            AdvisoryMessage[] advMessages = responses[i].getAdvisoryMessagesArray();
            for (int j = 0; j < advMessages.length; j++) {
                if (responses[i].getStatusCode() != STATUS_CODE_SUCCESS &&
                    responses[i].getStatusCode() != STATUS_CODE_WARNING) {
                    String[] advMessage = getAdvisoryMessage(advMessages[j]);
                    if (advMessage != null) {
                        throw new BaseSystemException(advMessage[1]);
                    }
                }
            }
            if (i == 1) {
                offersList = populateOffersList(responses[i], sessionId);
            }

        }
        return offersList;

    }

    public ArrayList postDeclineEvent(String sessionId, NBAChoiceVO choice, String time,
                                      String refusalReason) throws DataNotFoundException {
        return postDeclineEvent(sessionId, choice, time, refusalReason, "BMS");
    }

    public ArrayList postDeclineEvent(String sessionId, NBAChoiceVO choice, String time, String refusalReason,
                                      String interactionPoint) throws DataNotFoundException {
        logger.debug("Calling postDeclineEvent method with parameters[" + sessionId + ", choice" + choice + ", " +
                     time + " ," + refusalReason + "]");

        ArrayList offersList = new ArrayList();
        HashMap parameters = new HashMap();
        parameters.put("UACIOfferTrackingCode", choice.getTreatmentCode());
        parameters.put("UACIResponseTypeCode", "RJT");
        parameters.put("CTX_CST_TRTMT_TIME", time);
        parameters.put("CTX_REFUSAL_REASON", refusalReason);


        String requestMessage =
            getCustomerChoiceRequest(sessionId, RECORD_DECLINE_EVENT, parameters, choice, interactionPoint);

        SubmitCustomerChoiceRs response = sendNBAChoiceRequest(requestMessage);
        Response[] responses = response.getBody().getExecuteBatchResponse().getReturnArray(0).getResponsesArray();

        for (int i = 0; i < responses.length; i++) {
            AdvisoryMessage[] advMessages = responses[i].getAdvisoryMessagesArray();
            for (int j = 0; j < advMessages.length; j++) {
                if (responses[i].getStatusCode() != STATUS_CODE_SUCCESS &&
                    responses[i].getStatusCode() != STATUS_CODE_WARNING) {

                    String[] advMessage = getAdvisoryMessage(advMessages[j]);
                    if (advMessage != null) {
                        throw new BaseSystemException(advMessage[1]);
                    }
                }
            }
            if (i == 1) {
                offersList = populateOffersList(responses[i], sessionId);
            }

        }
        return offersList;

    }

    public ArrayList postLaterEvent(String sessionId, NBAChoiceVO choice, String time,
                                    String laterReason) throws DataNotFoundException {
        logger.debug("Calling postLaterEvent method with parameters[" + sessionId + ", choice" + choice + ", " + time +
                     " ," + laterReason + "]");


        ArrayList offersList = new ArrayList();
        HashMap parameters = new HashMap();
        parameters.put("UACIOfferTrackingCode", choice.getTreatmentCode());
        parameters.put("UACIResponseTypeCode", "CON");
        parameters.put("CTX_CST_TRTMT_TIME", time);
        parameters.put("CTX_DELAY_REASON", laterReason);


        String requestMessage = getCustomerChoiceRequest(sessionId, RECORD_LATER_EVENT, parameters, choice, "BMS");

        SubmitCustomerChoiceRs response = sendNBAChoiceRequest(requestMessage);
        Response[] responses = response.getBody().getExecuteBatchResponse().getReturnArray(0).getResponsesArray();
        for (int i = 0; i < responses.length; i++) {
            AdvisoryMessage[] advMessages = responses[i].getAdvisoryMessagesArray();
            for (int j = 0; j < advMessages.length; j++) {
                if (responses[i].getStatusCode() != STATUS_CODE_SUCCESS &&
                    responses[i].getStatusCode() != STATUS_CODE_WARNING) {
                    String[] advMessage = getAdvisoryMessage(advMessages[j]);
                    if (advMessage != null) {
                        throw new BaseSystemException(advMessage[1]);
                    }
                }
            }
            if (i == 1) {
                offersList = populateOffersList(responses[i], sessionId);
            }

        }
        return offersList;

    }

    public ArrayList retrieveCallReasonsList() {
        logger.debug("Calling retrieveCallReasonsList method");

        NbaDAO dao = new NbaDAO();
        return dao.retrieveCallReasonsList();
    }


    public ArrayList retrieveOnSpotCallReasonsList() {
        logger.debug("Calling retrieveOnSPotCallReasonsList method");

        NbaDAO dao = new NbaDAO();
        return dao.retrieveOnSpotCallReasonsList();
    }

    public ArrayList retrieveOnSpotDeclineCallReasonsList() {
        logger.debug("Calling retrieveOnSpotDeclineCallReasonsList method");

        NbaDAO dao = new NbaDAO();
        return dao.retrieveOnSpotDeclineReasonsList();
    }

    private SubmitCustomerChoiceRq populateCustomerChoiceRequest(NBAChoiceVO choice) {

        SubmitCustomerChoiceRq customerChoice = SubmitCustomerChoiceRq.Factory.newInstance();
        MsgRqHdr msgReq = MsgRqHdr.Factory.newInstance();
        msgReq.setRqUID(choice.getSrId());
        msgReq.setSCId(choice.getScId());
        msgReq.setMsgSpecProp(choice.getMessageSpecProp());
        msgReq.setUsrId(choice.getUserId());
        msgReq.setClientDt(choice.getClientDate());
        msgReq.setFuncId(choice.getFunctionId());

        BodyRq body = BodyRq.Factory.newInstance();
        body.setMSISDN(choice.getMsisdn());
        body.setOfferName(choice.getOfferName());
        body.setOfferDetail(choice.getOfferDetail());
        body.setOfferType(choice.getOfferType());
        body.setOfferFee(choice.getOfferFee());
        body.setOfferPlan(choice.getOfferPlan());
        body.setValidationFlag(choice.getValidationFlag());
        body.setAgentType(choice.getAgentType());
        body.setSMSIndexAr(choice.getSmsIndexAr());
        body.setSMSIndexEn(choice.getSmsindexEn());
        body.setCustomerChoice(choice.getCustomerChoice());

        PackageInfo packageInfo = PackageInfo.Factory.newInstance();
        packageInfo.setBundleName(choice.getBundleName());
        packageInfo.setDestinationPackage(choice.getDestinationPackage());
        packageInfo.setPackageId(choice.getPackageId());
        packageInfo.setPayType(choice.getPayType());
        body.setPackageInfo(packageInfo);

        if (choice.getParams() != null && choice.getParams().size() > 0) {
            PromoDetailes promoDetails = PromoDetailes.Factory.newInstance();
            promoDetails.setPromoID(choice.getOfferId());
            DynamicParams dynamicParams = DynamicParams.Factory.newInstance();
            Param[] params = new ParamImpl[choice.getParams().size()];
            ArrayList paramsList = new ArrayList();
            Iterator iterator = choice.getParams().keySet().iterator();
            while (iterator.hasNext()) {
                Param p = Param.Factory.newInstance();
                Object key = iterator.next();
                Object value = choice.getParams().get(key);
                p.setKey(String.valueOf(key));
                p.setValue(StringEscapeUtils.escapeXml(String.valueOf(value)));
                paramsList.add(p);

            }
            paramsList.toArray(params);
            dynamicParams.setParamArray(params);
            promoDetails.setDynamicParams(dynamicParams);
            body.setPromotionDetailes(promoDetails);


        }

        customerChoice.setMsgRqHdr(msgReq);
        customerChoice.setBody(body);

        return customerChoice;
    }


    private ExecuteBatchResponse sendNBARequest(String requestMessage) {

        ApplicationResources applicationResources = ApplicationResources.getInstance();
        String requestQueue = applicationResources.getPropertyValue(PropertyConstantsIfc.NBA_REQUEST_QUEUE);
        String replyQueue = applicationResources.getPropertyValue(PropertyConstantsIfc.NBA_REPLY_QUEUE);
        String queueMgr = applicationResources.getPropertyValue(PropertyConstantsIfc.NBA_QMGR);
        int mqInquiryTimeOut =
            Integer.parseInt(applicationResources.getPropertyValue(PropertyConstantsIfc.MQ_INQUIRY_TIMEOUT));


        String replyMessage = null;
        try {
            replyMessage =
                MQHelper.instance().sendToMQWithReplyWithExpiry(requestMessage, requestQueue, replyQueue, queueMgr,
                                                                mqInquiryTimeOut);
           
            ExecuteBatchResponse reply = (ExecuteBatchResponse)handler.parseExecuteBatchResponse(replyMessage);

            logger.debug("[sendNBARequest]: Successfull NBA inquiry: request[" + requestMessage + "], Reply[" +
                         replyMessage + "]");

            return reply;

        } catch (MBIMQException e) {
            if (e.getCause() instanceof TimeOutException)
                logger.error("MQ TimeOutException while trying to call MQ service [NBA Inquiry]", e);
            else
                logger.error("An MQException while trying to call MQ service [NBA Inquiry]", e);

            throw new MBIMQException("Error occured during sending NBA soap request ", e, LoggersNamesIfc.NBALogger,
                                     "NBA", BackendNameIfc.BSL, MBIMQException.OPERATION_SERVICE_TYPE, requestQueue,
                                     replyQueue, requestMessage);
        } catch (Exception e) {
            throw new BaseSystemException("Error occured during sending NBA soap request ", e,
                                          LoggersNamesIfc.NBALogger);
        }
    }

    private SubmitCustomerChoiceRs sendNBAChoiceRequest(String requestMessage) {

        ApplicationResources applicationResources = ApplicationResources.getInstance();
        String requestQueue = applicationResources.getPropertyValue(PropertyConstantsIfc.NBA_NOTIFY_REQUEST_QUEUE);
        String replyQueue = applicationResources.getPropertyValue(PropertyConstantsIfc.NBA_NOTIFY_REPLY_QUEUE);
        String queueMgr = applicationResources.getPropertyValue(PropertyConstantsIfc.NBA_NOTIFY_QMGR);
        int mqInquiryTimeOut =
            Integer.parseInt(applicationResources.getPropertyValue(PropertyConstantsIfc.MQ_INQUIRY_TIMEOUT));


        String replyMessage = null;
        try {
            replyMessage =
                MQHelper.instance().sendToMQWithReplyWithExpiry(requestMessage, requestQueue, replyQueue, queueMgr,
                                                                mqInquiryTimeOut);
></additionalAttributes><additionalAttributes><name>ON_NET_SMS_MONTHLY_FEE</name><valueAsNumeric>0.0</valueAsNumeric><valueDataType>
            SubmitCustomerChoiceRs reply = (SubmitCustomerChoiceRs)handler.parseCustomerChoiceRequest(replyMessage);

            logger.debug("[sendNBAChoiceRequest]: Successfull NBA choice inquiry: request[" + requestMessage +
                         "], Reply[" + replyMessage + "]");


            return reply;

        } catch (MBIMQException e) {
            if (e.getCause() instanceof TimeOutException)
                logger.error("MQ TimeOutException while trying to call MQ service [Submit NBA Choice]", e);
            else
                logger.error("An MQException while trying to call MQ service [Submit NBA Choice]", e);

            throw new MBIMQException("Error occured during sending NBA Choice request ", e, LoggersNamesIfc.NBALogger,
                                     "NBA", BackendNameIfc.BSL, MBIMQException.OPERATION_SERVICE_TYPE, requestQueue,
                                     replyQueue, requestMessage);
        } catch (Exception e) {
            throw new BaseSystemException("Error occured during sending NBA Choice request ", e,
                                          LoggersNamesIfc.NBALogger);
        }
    }

    private ExecuteBatchResponse sendGetProfileRequest(String requestMessage) {
        ApplicationResources applicationResources = ApplicationResources.getInstance();
        String requestQueue = applicationResources.getPropertyValue(PropertyConstantsIfc.NBA_REQUEST_QUEUE);
        String replyQueue = applicationResources.getPropertyValue(PropertyConstantsIfc.NBA_REPLY_QUEUE);
        String queueMgr = applicationResources.getPropertyValue(PropertyConstantsIfc.NBA_QMGR);
        int mqInquiryTimeOut =
            Integer.parseInt(applicationResources.getPropertyValue(PropertyConstantsIfc.MQ_INQUIRY_TIMEOUT));


        String replyMessage = null;
        try {
            replyMessage =
                MQHelper.instance().sendToMQWithReplyWithExpiry(requestMessage, requestQueue, replyQueue, queueMgr,
                                                                mqInquiryTimeOut);

            ExecuteBatchResponse reply = (ExecuteBatchResponse)handler.parseExecuteBatchResponse(replyMessage);

            return reply;

        } catch (MBIMQException e) {
            throw new MBIMQException("Error occured during sending NBA get profile soap request ", e,
                                     LoggersNamesIfc.NBALogger, "NBA", BackendNameIfc.BSL,
                                     MBIMQException.OPERATION_SERVICE_TYPE, requestQueue, replyQueue, requestMessage);
        } catch (Exception e) {
            throw new BaseSystemException("Error occured during sending NBA get profile soap request ", e,
                                          LoggersNamesIfc.NBALogger);
        }
    }

    private GetHistoryWSResponse sendNBAHistoryRequest(String requestMessage) {

        ApplicationResources applicationResources = ApplicationResources.getInstance();
        String requestQueue = applicationResources.getPropertyValue(PropertyConstantsIfc.NBA_HIS_REQUEST_QUEUE);
        String replyQueue = applicationResources.getPropertyValue(PropertyConstantsIfc.NBA_HIS_REPLY_QUEUE);
        String queueMgr = applicationResources.getPropertyValue(PropertyConstantsIfc.NBA_HIS_QMGR);
        int mqInquiryTimeOut =
            Integer.parseInt(applicationResources.getPropertyValue(PropertyConstantsIfc.MQ_INQUIRY_TIMEOUT));


        String replyMessage = null;
        try {
            replyMessage =
                MQHelper.instance().sendToMQWithReplyWithExpiry(requestMessage, requestQueue, replyQueue, queueMgr,
                                                                mqInquiryTimeOut);
            GetHistoryWSResponse reply = (GetHistoryWSResponse)handler.parseHistoryResponse(replyMessage);

            logger.debug("[sendNBAHistoryRequest]: Successfull NBA history inquiry: request[" + requestMessage +
                         "], Reply[" + replyMessage + "]");
            return reply;

        } catch (MBIMQException e) {
            if (e.getCause() instanceof TimeOutException)
                logger.error("MQ TimeOutException while trying to call MQ service [Get NBA History]", e);
            else
                logger.error("An MQException while trying to call MQ service [Get NBA History]", e);

            throw new MBIMQException("Error occured while retrieving NBA history request ", e,
                                     LoggersNamesIfc.NBALogger, "NBA", BackendNameIfc.BSL,
                                     MBIMQException.OPERATION_SERVICE_TYPE, requestQueue, replyQueue, requestMessage);
        } catch (Exception e) {
            throw new BaseSystemException("Error occured while retrieving NBA history request ", e,
                                          LoggersNamesIfc.NBALogger);
        }
    }

    private String startSessionAndGetOffersRequest(String sessionId, String msisdn, String userLoginName, String interactiveChannel,
                                                   String interactionPoint) throws BaseSystemException {

        ExecuteBatch batch = ExecuteBatch.Factory.newInstance();
        CommandImpl sessionCommand = CommandImpl.Factory.newInstance();
        NameValuePairImpl audienceNv = NameValuePairImpl.Factory.newInstance();
        audienceNv.setName("PK_MSISDN_ID");
        audienceNv.setValueAsString(msisdn);
        audienceNv.setValueDataType("string");
        sessionCommand.setAudienceLevel("MSISDN");
        sessionCommand.setDebug(false);
        sessionCommand.setInteractionPoint(interactionPoint);
        sessionCommand.setInteractiveChannel(interactiveChannel);
        sessionCommand.setMethodIdentifier("startSession");
        sessionCommand.setRelyOnExistingSession(false);

        NameValuePairImpl eventParamNv1 = NameValuePairImpl.Factory.newInstance();
        eventParamNv1.setName("CTX_CALL_REASON");
        eventParamNv1.setValueDataType("string");
        eventParamNv1.setValueAsString("NO_CALL_REASON");

        NameValuePairImpl eventParamNv2 = NameValuePairImpl.Factory.newInstance();
        eventParamNv2.setName("CTX_LATER_REASON");
        eventParamNv2.setValueDataType("string");

        NameValuePairImpl eventParamNv3 = NameValuePairImpl.Factory.newInstance();
        eventParamNv3.setName("CTX_REFUSAL_REASON");
        eventParamNv3.setValueDataType("string");

        NameValuePairImpl eventParamNv4 = NameValuePairImpl.Factory.newInstance();
        eventParamNv4.setName("CTX_DELAY_REASON");
        eventParamNv4.setValueDataType("string");

        NameValuePairImpl eventParamNv5 = NameValuePairImpl.Factory.newInstance();
        eventParamNv5.setName("CTX_AGENT");
        eventParamNv5.setValueDataType("string");
        eventParamNv5.setValueAsString(userLoginName);

        NameValuePairImpl eventParamNv6 = NameValuePairImpl.Factory.newInstance();
        eventParamNv6.setName("CTX_CST_TRTMT_TIME");
        eventParamNv6.setValueAsNumeric(0);
        eventParamNv6.setValueDataType("numeric");


        batch.setSessionID(sessionId);
        sessionCommand.setAudienceIDArray(new NameValuePairImpl[] { audienceNv });
        sessionCommand.setEventParametersArray(new NameValuePairImpl[] {
                                               eventParamNv1, eventParamNv2, eventParamNv3, eventParamNv4,
                                               eventParamNv5, eventParamNv6
        });

        CommandImpl offersCommand = CommandImpl.Factory.newInstance();
        offersCommand.setAudienceIDArray(new NameValuePairImpl[] { NameValuePairImpl.Factory.newInstance() });
        offersCommand.setEventParametersArray(new NameValuePairImpl[] { NameValuePairImpl.Factory.newInstance() });

        offersCommand.setDebug(false);
        offersCommand.setInteractionPoint(interactionPoint);
        offersCommand.setMethodIdentifier("getOffers");
        offersCommand.setRelyOnExistingSession(true);
        offersCommand.setNumberRequested(3);

        batch.setCommandsArray(new CommandImpl[] { sessionCommand, offersCommand });

        try {
            return handler.generateExecuteBatch(batch);
        } catch (Exception e) {
            throw new BaseSystemException("Error occured during preparing NBA soap request for MSISDN [" + msisdn + "]",
                                          e, LoggersNamesIfc.NBALogger);
        }

    }


    private String getProfileRequest(String sessionId) throws BaseSystemException {

        ExecuteBatch batch = ExecuteBatch.Factory.newInstance();
        CommandImpl sessionCommand = CommandImpl.Factory.newInstance();
        NameValuePairImpl audienceNv = NameValuePairImpl.Factory.newInstance();
        sessionCommand.setInteractionPoint("");
        sessionCommand.setInteractiveChannel("");
        sessionCommand.setMethodIdentifier("getProfile");
        sessionCommand.setRelyOnExistingSession(false);


        batch.setSessionID(sessionId);
        sessionCommand.setAudienceIDArray(new NameValuePairImpl[] { audienceNv });


        batch.setCommandsArray(new CommandImpl[] { sessionCommand });

        try {
            return handler.generateExecuteBatch(batch);
        } catch (Exception e) {
            throw new BaseSystemException("Error occured during preparing NBA soap request for ending session +[" +
                                          sessionId + "]", e, LoggersNamesIfc.NBALogger);
        }


    }

    private String endSessionRequest(String sessionId) throws BaseSystemException {

        ExecuteBatch batch = ExecuteBatch.Factory.newInstance();
        CommandImpl sessionCommand = CommandImpl.Factory.newInstance();
        NameValuePairImpl audienceNv = NameValuePairImpl.Factory.newInstance();
        sessionCommand.setInteractionPoint("");
        sessionCommand.setInteractiveChannel("");
        sessionCommand.setMethodIdentifier("endSession");
        sessionCommand.setRelyOnExistingSession(false);


        batch.setSessionID(sessionId);
        sessionCommand.setAudienceIDArray(new NameValuePairImpl[] { audienceNv });


        batch.setCommandsArray(new CommandImpl[] { sessionCommand });

        try {
            return handler.generateExecuteBatch(batch);
        } catch (Exception e) {
            throw new BaseSystemException("Error occured during preparing NBA soap request for ending session +[" +
                                          sessionId + "]", e, LoggersNamesIfc.NBALogger);
        }


    }

    private String getHisttoryWsRequest(String msisdn) throws BaseSystemException {
        GetHistoryWS history = GetHistoryWS.Factory.newInstance();

        history.setMSISDN(msisdn);
        try {
            return handler.generateHistory(history);
        } catch (Exception e) {
            throw new BaseSystemException("Error occured during preparing NBA soap request for MSISDN [" + msisdn + "]",
                                          e, LoggersNamesIfc.NBALogger);
        }
    }

    private String getCustomerChoiceRequest(String sessionId, String eventType, HashMap eventParameters,
                                            NBAChoiceVO choice, String interactionPoint) throws BaseSystemException {
        SubmitCustomerChoiceRq customerChoiceRequest = populateCustomerChoiceRequest(choice);
        String batchRequestMessage = postEventRequest(sessionId, eventType, eventParameters, interactionPoint);
        try {
            return handler.generateCustomerChoiceRequest(customerChoiceRequest, batchRequestMessage);
        } catch (Exception e) {
            throw new BaseSystemException("Error occured during preparing NBA soap request for session [" + sessionId +
                                          "]", e, LoggersNamesIfc.NBALogger);
        }
    }

    private String postEventRequest(String sessionId, String eventType, HashMap eventParameters,
                                    String interactionPoint) throws BaseSystemException {

        ExecuteBatch batch = ExecuteBatch.Factory.newInstance();
        CommandImpl postEventCommand = CommandImpl.Factory.newInstance();
        NameValuePairImpl audienceNv = NameValuePairImpl.Factory.newInstance();


        postEventCommand.setMethodIdentifier("postEvent");
        postEventCommand.setEvent(eventType);

        NameValuePairImpl eventParamNv = null;
        Iterator paramsIt = eventParameters.keySet().iterator();
        ArrayList eventParamslist = new ArrayList();

        while (paramsIt.hasNext()) {
            String key = String.valueOf(paramsIt.next());
            eventParamNv = NameValuePairImpl.Factory.newInstance();
            eventParamNv.setName(key);
            if (eventParameters.get(key) instanceof Integer) {
                eventParamNv.setValueDataType("numeric");
                eventParamNv.setValueAsNumeric(((Integer)eventParameters.get(key)).doubleValue());

            } else {
                eventParamNv.setValueDataType("string");
                eventParamNv.setValueAsString(String.valueOf(eventParameters.get(key)));
            }
            eventParamslist.add(eventParamNv);
        }


        NameValuePairImpl[] eventParamsArr = new NameValuePairImpl[eventParameters.size()];
        eventParamslist.toArray(eventParamsArr);
        batch.setSessionID(sessionId);
        postEventCommand.setAudienceIDArray(new NameValuePairImpl[] { audienceNv });
        postEventCommand.setEventParametersArray(eventParamsArr);

        CommandImpl offersCommand = CommandImpl.Factory.newInstance();
        offersCommand.setAudienceIDArray(new NameValuePairImpl[] { NameValuePairImpl.Factory.newInstance() });
        offersCommand.setEventParametersArray(new NameValuePairImpl[] { NameValuePairImpl.Factory.newInstance() });

        offersCommand.setDebug(false);
        offersCommand.setInteractionPoint(interactionPoint);
        offersCommand.setMethodIdentifier("getOffers");
        offersCommand.setNumberRequested(5);
        offersCommand.setRelyOnExistingSession(true);

        CommandImpl historizeContactCommand = CommandImpl.Factory.newInstance();
        historizeContactCommand.setEvent("HISTORIZE_CONTACT");
        historizeContactCommand.setMethodIdentifier("postEvent");

        batch.setCommandsArray(new CommandImpl[] { postEventCommand, offersCommand, historizeContactCommand });

        try {
            return handler.generateExecuteBatch(batch);
        } catch (Exception e) {
            throw new BaseSystemException("Error occured during preparing NBA soap request for session [" + sessionId +
                                          "]", e, LoggersNamesIfc.NBALogger);
        }

    }

    private boolean isSessionTimedout(Response[] responses) {
        if (responses != null) {
            for (int i = 0; i < responses.length; i++) {
                AdvisoryMessage[] advMessages = responses[i].getAdvisoryMessagesArray();
                if (advMessages != null) {
                    for (int j = 0; j < advMessages.length; j++) {
                        String[] advMessageDetails = getAdvisoryMessage(advMessages[j]);
                        if (advMessageDetails != null)
                            return advMessageDetails[0].equals("INVALID_SESSION_ID");
                    }
                }
            }
        }
        return false;

    }


    private String[] getAdvisoryMessage(AdvisoryMessage advMessage) {
        if (advMessage == null)
            return null;
        return new String[] { "" + advMessage.getMessageCode(), advMessage.getDetailMessage() };
    }

    public double getPackageMRC(String packageId) throws Exception {
        MBIAdminAppService mbiAdminAppService = new MBIAdminAppService();

        ProductInquiryRequestVO request = ProductInquiryRequestVO.Factory.newInstance();
        ProductInquiryRequestHeaderVO header = request.addNewProductInquiryRequestHeader();
        OutputFilterVO outputFilter = request.addNewOutputFilter();
        FeeIdsVO fee = outputFilter.addNewFeeIds();
        outputFilter.addNewConditionList();
        ProductVO product = outputFilter.addNewProduct();

        SimpleDateFormat format = new SimpleDateFormat();
        format.applyPattern("yyyyMMddHHmmss");


        header.setMsgFormat("MDM_Product_Inq");
        header.setMsgVersion("0");
        header.setRequestorChannelId("BSL");
        header.setRequestorChannelFunction("MDM_Product_Inq");
        header.setRequestorSecurityInfo("Secure");
        header.setChannelTransactionId("MBI_" + mbiAdminAppService.getNewChannelTransID());
        header.setSrDate(format.format(new Date()));

        request.setMDMUser("OfferInquiry");
        request.setProductId(packageId);
        request.setProductIdSystem("21");


        outputFilter.setPackageId(packageId);
        outputFilter.setFeeType("");
        fee.addId("2");

        product.setProductType("");
        product.setProductId("");
        product.setProductIdSystem("");

        ProductInquiryReplyVO reply =
            (ProductInquiryReplyVO)BMSServiceOrderAppService.callService(BMSServicesIfc.BMS_SERVICE_PRODUCT_INQUIRY,
                                                                         request);

        assert reply != null && reply.getProduct() != null && reply.getProduct().getFeeList() != null;

        for (FeeInfoVO feeInfo : reply.getProduct().getFeeList().getFeeInfoArray()) {
            if (feeInfo.getFeeType().equals("2")) { //2 means MRC
                return new Double(feeInfo.getFeeAmount());
            }
        }

        return 0.0d;
    }

}

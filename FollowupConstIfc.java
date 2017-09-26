dljfkldjfa
testing
traffic generation
testing from home
testing again
/**
 * 
 */
package com.mobily.followup;
package com.mobily.followup;
package com.mobily.followup;
package com.mobily.followup;

package com.mobily.followup;
package com.mobily.followup;
package com.mobily.followup;
package com.mobily.followup;


package com.mobily.followup;
package com.mobily.followup;
package com.mobily.followup;
package com.mobily.followup;

/**
 * @author a.samir
 * 
 */
public interface FollowupConstIfc {

	public static final int CUSTOMER_STATUS_ACTIVE = 1;

	public static final int CUSTOMER_STATUS_WARNED = 2;

	public static final int CUSTOMER_STATUS_OG_BARRED = 3;

	public static final int CUSTOMER_STATUS_FULL_BARRED = 4;

	public static final int CUSTOMER_STATUS_SUSPENDED = 5;

	public static final int CUSTOMER_STATUS_DEACTIVATED = 6;

	public static final int CUSTOMER_STATUS_INTERNATIONAL_BARED = 7;

	public static final int CUSTOMER_STATUS_ROAMING_BARED = 8;

	public static final int CUSTOMER_REASON_HIGH_USAGE = 0;

	public static final int CUSTOMER_REASON_FRAUD = 1;

	public static final int CUSTOMER_REASON_BLACK_LIST = 2;

	public static final int CUSTOMER_REASON_CUSTOMER_REQUEST = 3;

	public static final int CUSTOMER_REASON_GRACE_PERIOD_I_MAIN = 4;
	public static final int CUSTOMER_REASON_GRACE_PERIOD_I_RELATED = 15;

	public static final int CUSTOMER_REASON_GRACE_PERIOD_II_MAIN = 5;

	public static final int CUSTOMER_REASON_GRACE_PERIOD_II_RELATED = 6;

	public static final int CUSTOMER_REASON_GRACE_PERIOD_III_MAIN = 7;

	public static final int CUSTOMER_REASON_GRACE_PERIOD_III_RELATED = 8;

	public static final int CUSTOMER_REASON_BAD_DEPT_MAIN = 9;

	public static final int CUSTOMER_REASON_BAD_DEPT_RELATED = 10;
	
	public static final int CUSTOMER_REASON_HU_TEAM_BLOCKING = 19;

	public static final int CUSTOMER_REASON_REGULARIRY_AFFAIR = 99;
	
	public static final int CUSTOMER_REASON_BLOCKED_BY_COLLECTION_TEAM = 110;

	public static final int CUSTOMER_MIXED_PACKAGE_SUBSCRIBER = 1;

	public static final int CUSTOMER_MIXED_PACKAGE_UNSUBSCRIBER = 0;

	public static final int BSL_STATUS__INITIAL = 1;

	public static final int BSL_STATUS_ACCEPTED = 2;

	public static final int BSL_STATUS_FAILED = 3;

	public static final int BSL_STATUS_PENDING = 4;

	public static final int BSL_STATUS_REJECTED = 5;

	public static final int BSL_STATUS_SUCCEDED = 6;
	
	public static final int BSL_STATUS_PARTIAL_SUCCEDED = 8;

	public static final String STAFF_MASTER_LINE_PLANNAME = "Master Staff Package";

	public static final String STAFF_MASTER_LINE_PLANNAME_NO_SPACE = "MasterStaffPackage";
	
	public static final String FOLLOWUP_CHANNEL_TRANS_PREFIX = "MBI-Followup-";

	// public static final String STAFF_SUPLLEMENTARY_LINE_PLANNAME = "";

	public String HUCC_DB_POOL_NAME = "HUCC_POOL";
	public static final int SYSTEM_USER = -1;
	public static final String FOLLOWUP_USER = "FOLLOWUP_SYSTEM";
	// production
	// public static final String SIM_BASED_PLAN_POID = "19620696604";

	// integration4025536830
	public static final String SIM_BASED_PLAN_POID = "4016704901";
	
	/* Pool Services Function IDs */
	public static String GENERATE_POOL_FUNCTION_ID = "Generate_Pool";
	public static String EXPIRE_POOL_FUNCTION_ID = "Expire_Pool";
	public static String CLOSE_POOL_FUNCTION_ID = "Close_Pool";
	public static String RECYCLE_POOL_FUNCTION_ID = "Recycle_Pool";
	public static String CHANGE_POOL_GROUPING_FUNCTION_ID = "Change_Pool_Grouping";
	
	/* PTP Services Function IDs */
	public static String CREATE_PTP_FUNCTION_ID = "Create_PTP";
	
	/* Invalid Values */
	public static final double INVALID_BALANCE = -10000000;
	public static final double INVALID_CREDIT_LIMIT = -1;
	public static final double INVALID_UNALLOCATED_PAYMENTS = -10000000;
	public static final double INVALID_PENDING_PAYMENTS = -10000000;
	public static final double INVALID_BILLED_AMOUNT = -10000000;
	
	/* Corporate Segments */
	public static final String CORPORATE_SEGMENT_STANDARD = "S70";
	public static final String CORPORATE_SEGMENT_SILVER = "S80";
	public static final String CORPORATE_SEGMENT_GOLD = "S90";
	
	/* Loyalty Status Codes */
	public static final int LOYALTY_STATUS_FAILED = 3;
	public static final int LOYALTY_STATUS_SUCCEDED = 2;
	
	public static final int HU_STRATEGY_UNBILLED_NOTIFY_TYPE_ID = 29;
	public static final int MNP_BUSINESS_ACCNT_NOTIFY_TYPE_ID = 96;
	public static final int CORP_COLLECTION_MANUAL_UNBAR_NOTIFY_TYPE_ID = 97;
	public static final int ACTIVE_ONE_BILL_DYNAMIC_POOL_NOTIFY_TYPE_ID = 99;
	public static final int PRS_SMS_UNBILLED_NOTIFY_TYPE_ID = 129;

	public static final int FOLLOW_UP_POOL_TYPE_WELCOME_CALL 	= 1;
	public static final int FOLLOW_UP_POOL_TYPE_MNP 			= 3;

	public static final int CONSTANT_FOR_POOL_CUSTOMER_STATUS_NOT_PROCESSED = 0;

	public static final int CONSTANT_FOR_POOL_CUSTOMER_STATUS_INVALID = 2;
	
}

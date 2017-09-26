just testing
sdfsdfdsfdsfsdfsdf
new testing
fsdfsdfsdfsdfsd
dfasfsdfsdfsdlfjlsdjfldjfldjfjd
sljdflsdjfljsd;fjsdf
jfsjdfjlsdjfljsdlfjsdljfsdjfjlfkadlfjdfjlsdjflsjdfldjfa;ljdflj
testing
test
/**
 *
 */
/**
 * 
 */
package com.mobily.followup;

public static final int CUSTOMER_STATUS_ACTIVE              = 1;
    public static final int CUSTOMER_STATUS_WARNED              = 2;
    public static final int CUSTOMER_STATUS_OG_BARRED           = 3;    
    public static final int CUSTOMER_STATUS_FULL_BARRED         = 4;    
    public static final int CUSTOMER_STATUS_SUSPENDED           = 5;        
    public static final int CUSTOMER_STATUS_DEACTIVATED         = 6;   	
    public static final int CUSTOMER_STATUS_INTERNATIONAL_BARED = 7;   	
    public static final int CUSTOMER_STATUS_ROAMING_BARED       = 8;

/**
 * @author a.samir
 *
 */
public interface CreditControlIfc { 
  
	
    public static final int CUSTOMER_STATUS_ACTIVE              = 1;
    public static final int CUSTOMER_STATUS_WARNED              = 2;
    public static final int CUSTOMER_STATUS_OG_BARRED           = 3;    
    public static final int CUSTOMER_STATUS_FULL_BARRED         = 4;    
    public static final int CUSTOMER_STATUS_SUSPENDED           = 5;        
    public static final int CUSTOMER_STATUS_DEACTIVATED         = 6;   	
    public static final int CUSTOMER_STATUS_INTERNATIONAL_BARED = 7;   	
    public static final int CUSTOMER_STATUS_ROAMING_BARED       = 8;   	
    

    public static final int CUSTOMER_REASON_HIGH_USAGE					 = 0;
    public static final int CUSTOMER_REASON_FRAUD				 		 = 1;
    public static final int CUSTOMER_REASON_BLACK_LIST			 		 = 2;
    public static final int CUSTOMER_REASON_CUSTOMER_REQUEST	 		 = 3;
    public static final int CUSTOMER_REASON_GRACE_PERIOD_I_MAIN	 		 = 4;
    public static final int CUSTOMER_REASON_GRACE_PERIOD_II_MAIN 		 = 5;
    public static final int CUSTOMER_REASON_GRACE_PERIOD_II_RELATED 	 = 6;
    public static final int CUSTOMER_REASON_GRACE_PERIOD_III_MAIN 		 = 7;
    public static final int CUSTOMER_REASON_GRACE_PERIOD_III_RELATED 	 = 8;
    public static final int CUSTOMER_REASON_BAD_DEPT_MAIN 				 = 9;
    public static final int CUSTOMER_REASON_BAD_DEPT_RELATED 			 = 10;
    public static final int CUSTOMER_REASON_HU_TEAM_BLOCKING 			 = 19;
    public static final int CUSTOMER_REASON_REGULARIRY_AFFAIR 			 = 99;
    
    public static final int CUSTOMER_MIXED_PACKAGE_SUBSCRIBER = 1;
    public static final int CUSTOMER_MIXED_PACKAGE_UNSUBSCRIBER = 0;
    
    public static final int BSL_STATUS__INITIAL 	= 1;                                                                                             
    public static final int BSL_STATUS_ACCEPTED		= 2;                                                                                            
    public static final int BSL_STATUS_FAILED		= 3;
    public static final int BSL_STATUS_PENDING		= 4;
    public static final int BSL_STATUS_REJECTED		= 5;
    public static final int BSL_STATUS_SUCCEDED		= 6;
    
    public static final String STAFF_MASTER_LINE_PLANNAME 			= "Master Staff Package";
    public static final String STAFF_MASTER_LINE_PLANNAME_NO_SPACE  = "MasterStaffPackage";
    //public static final String STAFF_SUPLLEMENTARY_LINE_PLANNAME = "";

    public  String HUCC_DB_POOL_NAME ="HUCC_POOL";
    
    //production
    //public static final String SIM_BASED_PLAN_POID 	= "19620696604";
   
    // integration4025536830
    public static final String SIM_BASED_PLAN_POID 			= "4016704901";
    public static final String MIXED_SIM_BASED_PLAN_POID 	= "4034389809";
    
    public static final float MIXED_LIMIT_THREASHOLD 		= (float)0.1;
    
    public static final int CUSTOMER_BILLING_ACTIVE         = 1;
}

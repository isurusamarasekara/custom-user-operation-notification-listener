package org.wso2.custom.user.operation.notification.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.AbstractIdentityUserOperationEventListener;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.identity.mgt.internal.IdentityMgtServiceComponent;
import org.wso2.carbon.identity.notification.mgt.NotificationManagementException;
import org.wso2.carbon.identity.notification.mgt.NotificationSender;
import org.wso2.carbon.identity.notification.mgt.bean.PublisherEvent;

import java.util.Map;

public class CustomUserOperationNotificationListener extends AbstractIdentityUserOperationEventListener {
    private static final Log log = LogFactory.getLog(CustomUserOperationNotificationListener.class);
    private final String eventName = "userEnableOperation";
    private final String usernameLabel = "username";
    private final String operationLabel = "operation";
    private final String EVENT_TYPE_PROFILE_UPDATE = "profileUpdate";
    private final String PRE_SET_CLAIM_VALUES = "PRE_SET_CLAIM_VALUES";
    private final String accountDisabledClaimUrl = "http://wso2.org/claims/identity/accountDisabled";

    @Override
    public int getExecutionOrderId() {
        int orderId = getOrderId();
        if (orderId != IdentityCoreConstants.EVENT_LISTENER_ORDER_ID) {
            return orderId;
        }
        return 80;
    }

    @Override
    public boolean doPreSetUserClaimValues(String userName, Map<String, String> claims, String profileName,
                                           UserStoreManager userStoreManager) throws UserStoreException {
        if (!isEnable()) {
            return true;
        }
        IdentityUtil.threadLocalProperties.get().put(PRE_SET_CLAIM_VALUES, claims);
        return true;
    }

    @Override
    public boolean doPostSetUserClaimValues(String username,
                                            Map<String, String> claims, String profileName,
                                            UserStoreManager userStoreManager)
            throws UserStoreException {
        if (!isEnable()) {
            return true;
        }
        Map<String, String> preSetClaims = (Map<String, String>) IdentityUtil.threadLocalProperties.get().get(PRE_SET_CLAIM_VALUES);
        if (preSetClaims.containsKey(accountDisabledClaimUrl) && !preSetClaims.get(accountDisabledClaimUrl).trim().isEmpty()) {
            switch(preSetClaims.get(accountDisabledClaimUrl)) {
                case "true":

                    break;
                case "false":

                    break;
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Sending user claim values update notification for user " + username);
        }
//        sendNotification(EVENT_TYPE_PROFILE_UPDATE, username);
        return true;
    }

    private void sendNotification(String operation, String username) {
        NotificationSender notificationSender = IdentityMgtServiceComponent.getNotificationSender();
        if (notificationSender != null) {
            try {
                PublisherEvent event = new PublisherEvent(eventName);
                event.addEventProperty(operationLabel, operation);
                event.addEventProperty(usernameLabel, username);
                if (log.isDebugEnabled()) {
                    log.debug("Invoking notification sender");
                }
                notificationSender.invoke(event);
            } catch (NotificationManagementException e) {
                log.error("Error while sending notifications on user operations", e);
            }
        } else {
            log.error("No registered notification sender found. Notification sending aborted");
        }
    }
}

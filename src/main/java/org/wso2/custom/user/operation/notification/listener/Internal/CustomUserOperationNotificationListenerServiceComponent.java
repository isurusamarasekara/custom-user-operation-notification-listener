package org.wso2.custom.user.operation.notification.listener.Internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;
import org.wso2.carbon.identity.mgt.IdentityMgtEventListener;
import org.wso2.carbon.identity.mgt.listener.TenantManagementListener;
import org.wso2.carbon.identity.mgt.listener.UserSessionTerminationListener;
import org.wso2.carbon.stratos.common.listeners.TenantMgtListener;
import org.wso2.carbon.user.core.listener.UserOperationEventListener;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.custom.user.operation.notification.listener.CustomUserOperationNotificationListener;

@Component(
        name = "org.wso2.custom.user.operation.notification.listener",
        immediate = true
)
public class CustomUserOperationNotificationListenerServiceComponent {

    private static Log log = LogFactory.getLog(CustomUserOperationNotificationListenerServiceComponent.class);
    private static RealmService realmService;

    @Activate
    protected void activate(ComponentContext context) {
        BundleContext bundleContext = context.getBundleContext();
        bundleContext.registerService(UserOperationEventListener.class.getName(), new CustomUserOperationNotificationListener(), null);
        log.info("CustomUserOperationNotificationListener bundle activated successfully.");

        ServiceRegistration serviceRegistration = context.getBundleContext().registerService
                (UserOperationEventListener.class.getName(), new IdentityMgtEventListener(), null);
        if (serviceRegistration != null) {
            if (log.isDebugEnabled()) {
                log.debug("Identity Management - UserOperationEventListener registered.");
            }
        } else {
            log.error("Identity Management - UserOperationEventListener could not be registered.");
        }

        CustomUserOperationNotificationListener notificationListener =
                new CustomUserOperationNotificationListener();
        ServiceRegistration customUserOperationNotificationSR = context.getBundleContext().registerService(
                UserOperationEventListener.class.getName(), notificationListener, null);
        context.getBundleContext().registerService(TenantMgtListener.class.getName(), new TenantManagementListener()
                , null);
        context.getBundleContext().registerService(UserOperationEventListener.class.getName(),
                new UserSessionTerminationListener(), null);

        if (customUserOperationNotificationSR != null) {
            if (log.isDebugEnabled()) {
                log.debug("Identity Management - CustomUserOperationNotificationListener registered.");
            }
        } else {
            log.error("Identity Management - CustomUserOperationNotificationListener could not be registered.");
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {
        if (log.isDebugEnabled()) {
            log.info("CustomUserOperationNotificationListener bundle is deactivated.");
        }
    }

    @Reference(
            name = "user.realmservice.default",
            service = RealmService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRealmService"
    )
    protected void setRealmService(RealmService realmService) {
        log.debug("Setting the Realm Service");
        CustomUserOperationNotificationListenerServiceComponent.realmService = realmService;
    }

    protected void unsetRealmService(RealmService realmService) {
        log.debug("UnSetting the Realm Service");
        CustomUserOperationNotificationListenerServiceComponent.realmService = null;
    }

    public static RealmService getRealmService() {
        return realmService;
    }
}

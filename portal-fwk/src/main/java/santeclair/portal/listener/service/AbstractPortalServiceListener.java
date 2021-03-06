package santeclair.portal.listener.service;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractPortalServiceListener<T> implements PortalServiceListener<T>, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 3958686169798794491L;

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractPortalServiceListener.class);

    protected BundleContext bundleContext;

    private boolean isRegistered = false;

    @SuppressWarnings("unchecked")
    @Override
    public void serviceChanged(ServiceEvent serviceEvent) {
        if (serviceEvent != null) {
            T service = (T) bundleContext.getService(serviceEvent.getServiceReference());
            if (service != null) {
                switch (serviceEvent.getType()) {
                case ServiceEvent.MODIFIED:
                    serviceModified(service, serviceEvent);
                    break;
                case ServiceEvent.MODIFIED_ENDMATCH:
                    serviceModifiedEndmatch(service, serviceEvent);
                    break;
                case ServiceEvent.REGISTERED:
                    isRegistered = true;
                    serviceRegistered(service, serviceEvent);
                    break;
                case ServiceEvent.UNREGISTERING:
                    isRegistered = false;
                    serviceUnregistering(service, serviceEvent);
                    break;
                default:
                    LOGGER.warn("Le type d'event lev� ({})n'est pas g�r�", serviceEvent.getType());
                    break;
                }
            } else {
                LOGGER.warn("Le service de type {} n'a pas �t� trouv� dans le contexte OSGi / FILTRE : [{}] ", getServiceType(), getFilter());
            }

        } else {
            LOGGER.warn("Le service event lev� est null");
        }
    }

    @Override
    public final void registerItself(BundleContext bundleContext) throws InvalidSyntaxException {
        if (bundleContext != null) {
            LOGGER.debug("Enregistrement du service {} dans le bundle context", this.getClass().getName());
            bundleContext.addServiceListener(this, this.getFilter());
            this.bundleContext = bundleContext;
        } else {
            LOGGER.error("Le bundle context est vide. Le service {} ne sera pas enregistr�.", this.getClass().getName());
        }
    }

    @Override
    public void serviceModified(T service, ServiceEvent serviceEvent) {
        LOGGER.info("serviceModified method default behavior does nothing. Override this method to execute custom code.");
    }

    @Override
    public void serviceModifiedEndmatch(T service, ServiceEvent serviceEvent) {
        LOGGER.info("serviceModifiedEndmatch method default behavior does nothing. Override this method to execute custom code.");
    }

    @Override
    public void serviceRegistered(T service, ServiceEvent serviceEvent) {
        LOGGER.info("serviceRegistered method default behavior does nothing. Override this method to execute custom code.");
    }

    @Override
    public void serviceUnregistering(T service, ServiceEvent serviceEvent) {
        LOGGER.info("serviceUnregistering method default behavior does nothing. Override this method to execute custom code.");
    }

    @Override
    public String getFilter() {
        Class<T> clazz = getServiceType();
        String filter = "(objectClass=" + clazz.getName() + ")";
        LOGGER.debug("getFilter() => {}", filter);
        return filter;
    }

    public boolean isServiceRegistered() {
        return isRegistered;
    }

    @SuppressWarnings("unchecked")
    private Class<T> getServiceType() {
        return ((Class<T>) ((ParameterizedType) getClass()
                        .getGenericSuperclass()).getActualTypeArguments()[0]);
    }

}

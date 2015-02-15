package santeclair.portal.webapp.vaadin;

import static santeclair.portal.event.EventDictionaryConstant.EVENT_STARTED;
import static santeclair.portal.event.EventDictionaryConstant.EVENT_STOPPED;
import static santeclair.portal.event.EventDictionaryConstant.PROPERTY_KEY_EVENT_HANDLER_ID;
import static santeclair.portal.event.EventDictionaryConstant.PROPERTY_KEY_EVENT_NAME;
import static santeclair.portal.event.EventDictionaryConstant.TOPIC_MODULE_UI_FACTORY;
import static santeclair.portal.event.EventDictionaryConstant.TOPIC_PORTAL;

import java.util.Date;
import java.util.Dictionary;
import java.util.Hashtable;

import org.apache.commons.lang.time.DateFormatUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.EventAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.context.support.WebApplicationContextUtils;

import santeclair.portal.event.EventDictionaryConstant;
import santeclair.portal.vaadin.module.ModuleUiFactory;
import santeclair.portal.webapp.HostActivator;
import santeclair.portal.webapp.event.handler.AbstractEventHandler;
import santeclair.portal.webapp.event.handler.EventArg;
import santeclair.portal.webapp.event.handler.PortalEventHandler;
import santeclair.portal.webapp.event.handler.Subscriber;
import santeclair.portal.webapp.vaadin.view.LeftSideMenu;
import santeclair.portal.webapp.vaadin.view.Main;
import santeclair.portal.webapp.vaadin.view.Tabs;

import com.vaadin.annotations.PreserveOnRefresh;
import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.server.DefaultErrorHandler;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.shared.communication.PushMode;
import com.vaadin.shared.ui.ui.Transport;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.UI;

/**
 * The Application's "verticalButtonMain" class
 */
@PreserveOnRefresh
@Title("Portail Santeclair")
@Theme("santeclair")
@Push(value = PushMode.MANUAL, transport = Transport.WEBSOCKET)
public class PortalApp extends UI implements PortalEventHandler {

    private static final long serialVersionUID = -5547062232353913227L;
    private static final Logger LOGGER = LoggerFactory.getLogger(PortalApp.class);

    // Container global
    private Main main;
    // Container des boutons sur le menu � gauche permettant de naviguer
    private LeftSideMenu leftSideMenu;
    // Container des onglets
    private Tabs tabs;

    /*
     * D�but du Code UI
     */
    @Override
    public void init(VaadinRequest request) {
        LOGGER.info("Initialisation de l'UI");

        ApplicationContext applicationContext = WebApplicationContextUtils.
                        getRequiredWebApplicationContext(VaadinServlet.getCurrent().getServletContext());
        HostActivator hostActivator = applicationContext.getBean(HostActivator.class);
        BundleContext context = hostActivator.getBundleContext();

        String pid = this.getEmbedId();

        // initialisation de l'IHM
        this.setSizeFull();
        this.setErrorHandler();

        // Cr�ation du composant contenant le menu � gauche avec les boutons
        LOGGER.info("Initialisation du menu gauche");
        leftSideMenu = new LeftSideMenu(pid);
        leftSideMenu.init(context);

        // Cr�ation du composant contenant les tabsheet
        LOGGER.info("Initialisation du container d'onglet");
        tabs = new Tabs();
        tabs.init();

        // Cr�ation du container principal
        LOGGER.info("Cr�ation du container principal");
        main = new Main(leftSideMenu, tabs);

        // Enregistrement des listeners d'event dans le portalEventBus
        LOGGER.info("Enregistrement des listeners d'event dans le portalEventBus");

        registerEventHandlerItself(context);

        ServiceReference<EventAdmin> ref = context.getServiceReference(EventAdmin.class);
        if (ref != null) {
            EventAdmin eventAdmin = context.getService(ref);
            Dictionary<String, Object> properties = new Hashtable<>();
            properties.put(PROPERTY_KEY_EVENT_NAME, EVENT_STARTED);
            properties.put(PROPERTY_KEY_EVENT_HANDLER_ID, pid);
            org.osgi.service.event.Event portalStartedEvent = new org.osgi.service.event.Event(TOPIC_PORTAL, properties);
            eventAdmin.sendEvent(portalStartedEvent);
        }

        this.setContent(main);

        LOGGER.debug("Fin Initialisation de l'UI");
    }

    @Override
    public void detach() {
        LOGGER.info("Detachement de l'UI");
        ApplicationContext applicationContext = WebApplicationContextUtils.
                        getRequiredWebApplicationContext(VaadinServlet.getCurrent().getServletContext());
        HostActivator hostActivator = applicationContext.getBean(HostActivator.class);
        unregisterEventHandlerItSelf(hostActivator.getBundleContext());
        super.detach();
    }

    @Override
    public void registerEventHandlerItself(BundleContext bundleContext) {
        try {
            AbstractEventHandler.registerEventHandler(bundleContext, this);
        } catch (InvalidSyntaxException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void unregisterEventHandlerItSelf(BundleContext bundleContext) {
        try {
            AbstractEventHandler.unregisterEventHandler(bundleContext, this);
        } catch (InvalidSyntaxException e) {
            e.printStackTrace();
        }
    }

    @Subscriber(topic = TOPIC_MODULE_UI_FACTORY, filter = "(" + PROPERTY_KEY_EVENT_NAME + "="
                    + EVENT_STARTED + ")")
    public void addModuleUiFactory(org.osgi.service.event.Event event,
                    @EventArg(name = EventDictionaryConstant.PROPERTY_KEY_MODULE_UI_FACTORY) final ModuleUiFactory<?> moduleUiFactory,
                    @EventArg(name = PROPERTY_KEY_EVENT_HANDLER_ID, required = false) final String eventHandlerID) {
        if (getPushConfiguration() != null && getPushConfiguration().getPushMode() != null && !getPushConfiguration().getPushMode().equals(PushMode.DISABLED)
                        && eventHandlerID == null) {
            access(new Runnable() {
                @Override
                public void run() {
                    String moduleUiName = moduleUiFactory.getName();
                    Notification notification = new Notification(moduleUiName + " charg�", "Le module " + moduleUiName + " est d�someais disponible.", Type.TRAY_NOTIFICATION);
                    notification.show(Page.getCurrent());
                    push();
                }
            });
        }
    }

    @Subscriber(topic = TOPIC_MODULE_UI_FACTORY, filter = "(" + PROPERTY_KEY_EVENT_NAME + "="
                    + EVENT_STOPPED + ")")
    public void removeModuleUiFactory(org.osgi.service.event.Event event,
                    @EventArg(name = EventDictionaryConstant.PROPERTY_KEY_MODULE_UI_FACTORY) final ModuleUiFactory<?> moduleUiFactory) {
        if (getPushConfiguration() != null && getPushConfiguration().getPushMode() != null && !getPushConfiguration().getPushMode().equals(PushMode.DISABLED)) {
            access(new Runnable() {
                @Override
                public void run() {
                    String moduleUiName = moduleUiFactory.getName();
                    Notification notification = new Notification(moduleUiName + " d�charg�", "Le module " + moduleUiName + " est d�someais indisponible.", Type.TRAY_NOTIFICATION);
                    notification.show(Page.getCurrent());
                    push();
                }
            });
        }
    }

    // private List<String> getCurrentUserRoles() {
    // Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    // LOGGER.info("Portal authentication : " + authentication);
    // @SuppressWarnings("unchecked")
    // Collection<GrantedAuthority> grantedAuthorities = (Collection<GrantedAuthority>) authentication.getAuthorities();
    // LOGGER.debug("current user roles : " + grantedAuthorities);
    // List<String> roles = new ArrayList<>();
    // for (GrantedAuthority grantedAuthority : grantedAuthorities) {
    // roles.add(grantedAuthority.getAuthority());
    // }
    // return roles;
    // }

    private void setErrorHandler() {
        this.setErrorHandler(new DefaultErrorHandler() {
            private static final long serialVersionUID = -6689459084265117649L;

            @Override
            public void error(com.vaadin.server.ErrorEvent event) {
                String uriFragment = Page.getCurrent().getUriFragment();
                StringBuilder sbCodeErreur = new StringBuilder(uriFragment);
                sbCodeErreur.append(DateFormatUtils.format(new Date(), "yyyyMMddHHmmss"));
                String codeErreur = sbCodeErreur.toString();
                Throwable t = event.getThrowable();
                if (findAccesDeniedExceptionException(t) != null) {
                    Notification.show("Vous n'avez pas les droits suffisant pour acc�der � la ressource demand�e.", Type.WARNING_MESSAGE);
                } else {
                    LOGGER.error("Erreur inattendu dans le portail : " + codeErreur, t);
                    Notification.show("Erreur inattendue de l'application.\n",
                                    "Merci de prendre une capture d'�cran et de cr�er une intervention avec cette capture et le code suivant :\n" + codeErreur,
                                    Type.ERROR_MESSAGE);
                }
            }
        });
    }

    /**
     * Fonction recursive pour rechercher une cause dans les exceptions filles
     * de type AccessDeniedException.
     * 
     * @param throwable
     *            Exception a controler
     * @return L'exception de type AccessDeniedException ou null si aucune.
     */
    private AccessDeniedException findAccesDeniedExceptionException(Throwable throwable) {
        if (throwable != null) {
            if (AccessDeniedException.class.isAssignableFrom(throwable.getClass())) {
                return AccessDeniedException.class.cast(throwable);
            } else {
                return findAccesDeniedExceptionException(throwable.getCause());
            }
        }
        return null;
    }

}

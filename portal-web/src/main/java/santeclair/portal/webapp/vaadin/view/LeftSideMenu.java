package santeclair.portal.webapp.vaadin.view;

import java.util.Map;
import java.util.TreeSet;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.jouni.animator.AnimatorProxy;

import santeclair.portal.event.handler.AbstractEventHandler;
import santeclair.portal.event.handler.PortalEventHandler;
import santeclair.portal.listener.service.impl.EventAdminServiceListener;
import santeclair.portal.module.ModuleUi;
import santeclair.portal.view.ViewUi;
import santeclair.portal.webapp.vaadin.view.component.ModuleUiButon;

import com.vaadin.ui.Component;
import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.VerticalLayout;

public class LeftSideMenu extends CustomLayout implements PortalEventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(LeftSideMenu.class);
    private static final long serialVersionUID = -4748523363216844520L;
    private final VerticalLayout buttonContainer;

    private final EventAdminServiceListener eventAdminServiceListener;
    private final String uiId;
    private final AnimatorProxy proxy;

    public LeftSideMenu(EventAdminServiceListener eventAdminServiceListener, String uiId) {
        super("sidebarLayout");
        this.eventAdminServiceListener = eventAdminServiceListener;
        this.uiId = uiId;
        buttonContainer = new VerticalLayout();
        proxy = new AnimatorProxy();
        this.addComponent(proxy);
    }

    public void init(BundleContext bundleContext) {
        this.setSizeFull();
        buttonContainer.setSizeFull();
        this.addComponent(buttonContainer, "buttonLayout");
        registerEventHandlerItself(bundleContext);
    }

    public synchronized void addModuleUi(final ModuleUi moduleUi, final Map<String, ViewUi> viewUiMap) {
        LOGGER.debug("global addModuleUi");
        ModuleUiButon moduleUiButon = new ModuleUiButon(moduleUi, viewUiMap, eventAdminServiceListener, uiId, proxy);
        TreeSet<Component> butonModuleUi = new TreeSet<>();
        boolean moduleUiCodeAlreadyExist = false;
        for (Component component : buttonContainer) {
            if (ModuleUiButon.class.isAssignableFrom(component.getClass())) {
                ModuleUiButon currentMainButonModuleUi = ModuleUiButon.class.cast(component);
                moduleUiCodeAlreadyExist = currentMainButonModuleUi.getModuleUi().getCode().equals(moduleUi.getCode());
                butonModuleUi.add(component);
            }
        }
        if (!moduleUiCodeAlreadyExist) {
            butonModuleUi.add(moduleUiButon);
        } else {
            LOGGER.warn("A ModuleUi with code {} already exist. Please choose a different one. ModuleUi hasn't been activated.", moduleUi.getCode());
        }
        buttonContainer.removeAllComponents();
        buttonContainer.addComponents(butonModuleUi.toArray(new Component[]{}));
    }

    public synchronized boolean removeModuleUi(final String moduleUiCode) {
        LOGGER.debug("removeModuleUi : {}", moduleUiCode);
        boolean modulehasBeenRemoved = false;
        for (Component component : buttonContainer) {
            if (ModuleUiButon.class.isAssignableFrom(component.getClass())) {
                ModuleUiButon currentComponent = ModuleUiButon.class.cast(component);
                if (currentComponent.getModuleUi().getCode().equals(moduleUiCode)) {
                    buttonContainer.removeComponent(component);
                    modulehasBeenRemoved = true;
                }
            }
        }
        return modulehasBeenRemoved;
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

}

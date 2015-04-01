package santeclair.portal.webapp.vaadin.view;

import static santeclair.portal.event.EventDictionaryConstant.EVENT_CONTEXT_TABS;
import static santeclair.portal.event.EventDictionaryConstant.EVENT_NAME_ASKING_CLOSED;
import static santeclair.portal.event.EventDictionaryConstant.EVENT_NAME_CLOSED;
import static santeclair.portal.event.EventDictionaryConstant.EVENT_NAME_NEW;
import static santeclair.portal.event.EventDictionaryConstant.EVENT_NAME_STARTED;
import static santeclair.portal.event.EventDictionaryConstant.PROPERTY_KEY_EVENT_CONTEXT;
import static santeclair.portal.event.EventDictionaryConstant.PROPERTY_KEY_EVENT_NAME;
import static santeclair.portal.event.EventDictionaryConstant.PROPERTY_KEY_MODULE_UI_CODE;
import static santeclair.portal.event.EventDictionaryConstant.PROPERTY_KEY_PORTAL_CURRENT_USER_ROLES;
import static santeclair.portal.event.EventDictionaryConstant.PROPERTY_KEY_PORTAL_SESSION_ID;
import static santeclair.portal.event.EventDictionaryConstant.PROPERTY_KEY_TAB_HASH;
import static santeclair.portal.event.EventDictionaryConstant.PROPERTY_KEY_VIEW_UI_CODE;
import static santeclair.portal.event.EventDictionaryConstant.TOPIC_COMPONENT_UI;
import static santeclair.portal.event.EventDictionaryConstant.TOPIC_MODULE_UI;
import static santeclair.portal.event.EventDictionaryConstant.TOPIC_NAVIGATOR;
import static santeclair.portal.event.EventDictionaryConstant.TOPIC_VIEW_UI;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import santeclair.portal.event.publisher.callback.TabsCallback;
import santeclair.portal.listener.service.impl.EventAdminServiceListener;
import santeclair.portal.listener.service.impl.EventAdminServiceListener.DataPublisher;
import santeclair.portal.listener.service.impl.EventAdminServiceListener.Publisher;
import santeclair.portal.webapp.vaadin.navigator.NavigatorEventHandler;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontIcon;
import com.vaadin.ui.Component;
import com.vaadin.ui.HasComponents.ComponentDetachListener;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.CloseHandler;
import com.vaadin.ui.TabSheet.SelectedTabChangeListener;
import com.vaadin.ui.themes.ValoTheme;

public class Tabs extends TabSheet implements View, SelectedTabChangeListener, CloseHandler,
                ComponentDetachListener, TabsCallback {

    private static final long serialVersionUID = 5672663000761618207L;

    private static final Logger LOGGER = LoggerFactory.getLogger(Tabs.class);

    private static final String PARAMS_URI_FRAGMENT = "params";

    private final DataPublisher<Tabs, TabsCallback> dataPublisherToModuleUiTopic;
    private final DataPublisher<Tabs, TabsCallback> dataPublisherToViewUiTopic;
    private final DataPublisher<Tabs, TabsCallback> dataPublisherToComponentUiTopic;
    private final Publisher<Tabs> publisherToNavigatorTopic;

    private final EventAdminServiceListener eventAdminServiceListener;
    private final String sessionId;
    private final List<String> currentUserRoles;

    private Boolean keepView = false;

    public Tabs(final EventAdminServiceListener eventAdminServiceListener, final String sessionId, final List<String> currentUserRoles) {
        this.eventAdminServiceListener = eventAdminServiceListener;
        this.sessionId = sessionId;
        this.currentUserRoles = currentUserRoles;
        this.dataPublisherToModuleUiTopic = eventAdminServiceListener.registerDataPublisher(this, TabsCallback.class, TOPIC_MODULE_UI);
        this.dataPublisherToViewUiTopic = eventAdminServiceListener.registerDataPublisher(this, TabsCallback.class, TOPIC_VIEW_UI);
        this.dataPublisherToComponentUiTopic = eventAdminServiceListener.registerDataPublisher(this, TabsCallback.class, TOPIC_COMPONENT_UI);
        this.publisherToNavigatorTopic = eventAdminServiceListener.registerPublisher(this, TOPIC_NAVIGATOR);
    }

    public void init() {
        this.setSizeFull();
        this.addSelectedTabChangeListener(this);
        this.setCloseHandler(this);
        this.addStyleName(ValoTheme.TABSHEET_FRAMED);
        this.addStyleName(ValoTheme.TABSHEET_PADDED_TABBAR);

        Dictionary<String, Object> props = new Hashtable<>();
        props.put(PROPERTY_KEY_EVENT_CONTEXT, EVENT_CONTEXT_TABS);
        props.put(PROPERTY_KEY_EVENT_NAME, EVENT_NAME_STARTED);
        props.put(PROPERTY_KEY_PORTAL_SESSION_ID, sessionId);

        dataPublisherToViewUiTopic.publishEventDataAndDictionnarySynchronously(this, props);
    }

    @Override
    public void detach() {
        super.detach();
        eventAdminServiceListener.unregisterPublisher(dataPublisherToModuleUiTopic, dataPublisherToViewUiTopic, dataPublisherToComponentUiTopic, publisherToNavigatorTopic);
    }

    @Override
    public void enter(ViewChangeEvent event) {
        LOGGER.debug("enter : " + event);
        String parameters = event.getParameters();
        StringTokenizer st = new StringTokenizer(parameters, "/");
        String tabHashSt = null;
        String moduleUiCode = "";
        String viewUiCode = "";

        int count = 0;
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (count == 0) {
                tabHashSt = token;
            }
            if (count == 2) {
                moduleUiCode = token;
            }
            if (count == 4) {
                viewUiCode = token;
            }
            count++;
        }
        if ("NEW".equalsIgnoreCase(tabHashSt) && StringUtils.isNotBlank(moduleUiCode) && StringUtils.isNotBlank(viewUiCode)) {
            Dictionary<String, Object> props = new Hashtable<>();

            props.put(PROPERTY_KEY_EVENT_CONTEXT, EVENT_CONTEXT_TABS);
            props.put(PROPERTY_KEY_EVENT_NAME, EVENT_NAME_NEW);
            props.put(PROPERTY_KEY_MODULE_UI_CODE, moduleUiCode);
            props.put(PROPERTY_KEY_VIEW_UI_CODE, viewUiCode);
            props.put(PROPERTY_KEY_PORTAL_SESSION_ID, sessionId);
            props.put(PROPERTY_KEY_PORTAL_CURRENT_USER_ROLES, currentUserRoles);
            addExtractedParams(parameters, props);

            dataPublisherToModuleUiTopic.publishEventDataAndDictionnarySynchronously(this, props);
        } else if (StringUtils.isNumeric(tabHashSt)) {
            Integer tabHash = new Integer(tabHashSt);
            Integer numberOfTab = this.getComponentCount();
            for (int i = 0; i < numberOfTab; i++) {
                Tab tab = this.getTab(i);
                if (tab != null && tab.hashCode() == tabHash) {
                    this.setSelectedTab(tab);
                    break;
                }
            }
        }
    }

    @Override
    public int addView(String caption, FontIcon icon, Boolean closable, Component moduleUiView) {
        Tab tab = this.getTab(moduleUiView);
        int tabHash = -1;
        if (tab != null) {
            tabHash = tab.hashCode();
            tab.setCaption(caption);
            tab.setIcon(icon);
            tab.setClosable(closable);
            publisherToNavigatorTopic.publishEventSynchronously(NavigatorEventHandler.getNavigateEventProperty("container/" + tabHash, sessionId));
        } else {
            tab = this.addTab(moduleUiView, caption);
            tab.setIcon(icon);
            tab.setClosable(closable);
            tabHash = tab.hashCode();
            publisherToNavigatorTopic.publishEventSynchronously(NavigatorEventHandler.getNavigateEventProperty("container/" + tabHash, sessionId));
        }
        return tabHash;
    }

    @Override
    public void componentDetachedFromContainer(ComponentDetachEvent event) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onTabClose(TabSheet tabsheet, Component tabContent) {

        keepView = Boolean.FALSE;
        Tab tab = tabsheet.getTab(tabContent);
        Integer tabHash = tab.hashCode();

        Dictionary<String, Object> props = new Hashtable<>();
        props.put(PROPERTY_KEY_EVENT_CONTEXT, EVENT_CONTEXT_TABS);
        props.put(PROPERTY_KEY_EVENT_NAME, EVENT_NAME_ASKING_CLOSED);
        props.put(PROPERTY_KEY_PORTAL_SESSION_ID, sessionId);
        props.put(PROPERTY_KEY_TAB_HASH, tabHash);

        dataPublisherToComponentUiTopic.publishEventDataAndDictionnarySynchronously(this, props);
        dataPublisherToViewUiTopic.publishEventDataAndDictionnarySynchronously(this, props);
        dataPublisherToModuleUiTopic.publishEventDataAndDictionnarySynchronously(this, props);

        if (!keepView) {
            removeView(tabHash);
        }
    }

    @Override
    public void keepView() {
        keepView = Boolean.TRUE;
    }

    @Override
    public void removeView(int tabHash) {
        Integer numberOfTab = this.getComponentCount();
        for (int i = 0; i < numberOfTab; i++) {
            Tab tab = this.getTab(i);
            if (tab != null && tab.hashCode() == tabHash) {

                this.removeTab(tab);

                Dictionary<String, Object> props = new Hashtable<>();
                props.put(PROPERTY_KEY_EVENT_CONTEXT, EVENT_CONTEXT_TABS);
                props.put(PROPERTY_KEY_EVENT_NAME, EVENT_NAME_CLOSED);
                props.put(PROPERTY_KEY_PORTAL_SESSION_ID, sessionId);
                props.put(PROPERTY_KEY_TAB_HASH, tabHash);

                dataPublisherToComponentUiTopic.publishEventSynchronously(props);
                dataPublisherToViewUiTopic.publishEventSynchronously(props);
                dataPublisherToModuleUiTopic.publishEventSynchronously(props);

                break;
            }
        }
    }

    @Override
    public void selectedTabChange(SelectedTabChangeEvent event) {
        TabSheet tabSheet = event.getTabSheet();
        Tab tab = tabSheet.getTab(tabSheet.getSelectedTab());
        if (tab != null) {
            publisherToNavigatorTopic.publishEventSynchronously(NavigatorEventHandler.getNavigateEventProperty("container/" + tab.hashCode(), sessionId));
        }
    }

    private void addExtractedParams(String fragment, Dictionary<String, Object> props) {
        if (fragment != null && fragment.contains(PARAMS_URI_FRAGMENT)) {
            String paramsUriFragement = fragment.split(PARAMS_URI_FRAGMENT
                            + "/")[1];
            if (StringUtils.isNotBlank(paramsUriFragement)) {
                addParsedParameters(paramsUriFragement, props);
            }
        }
    }

    /**
     * @param parameters
     * @param paramsAsArray
     */
    private void addParsedParameters(String paramsFromUri, Dictionary<String, Object> props) {
        String[] paramsAsArray = paramsFromUri.split("/");
        String currentKey = null;
        for (int i = 0; i < paramsAsArray.length; i++) {
            if (i % 2 == 0) {
                currentKey = paramsAsArray[i];
            } else {
                props.put(currentKey, paramsAsArray[i]);
            }
        }
    }

}

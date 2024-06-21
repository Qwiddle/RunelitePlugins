package ${PACKAGE_NAME}.${PLUGIN_NAME};

import com.example.EthanApiPlugin.EthanApiPlugin;
import com.example.Packets.*;
import com.google.inject.Inject;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.HotkeyListener;
import ${PACKAGE_NAME}.${PLUGIN_NAME}.data.State;

@PluginDescriptor(
        name = "${PLUGIN_NAME}",
        description = "${PLUGIN_DESCRIPTION}",
        enabledByDefault = false,
        tags = {"spin", "plugin"}
)
@Slf4j
public class ${PLUGIN_NAME}Plugin extends Plugin {
    @Inject
    private Client client;
    @Inject
    private ${PLUGIN_NAME}Config config;
    @Inject
    private ${PLUGIN_NAME}Overlay overlay;
    @Inject
    private KeyManager keyManager;
    @Inject
    private OverlayManager overlayManager;
    @Inject

    private ClientThread clientThread;
    private boolean started = false;
    public int timeout = 0;
    public State playerState;

    @Provides
    private ${PLUGIN_NAME}Config getConfig(ConfigManager configManager) {
        return configManager.getConfig(${PLUGIN_NAME}Config.class);
    }

    @Override
    protected void startUp() throws Exception {
        keyManager.registerKeyListener(toggle);
        overlayManager.add(overlay);
        timeout = 0;
    }

    @Override
    protected void shutDown() throws Exception {
        keyManager.unregisterKeyListener(toggle);
        overlayManager.remove(overlay);
        timeout = 0;
    }


    @Subscribe
    private void onGameTick(GameTick event) {
        if (timeout > 0) {
            timeout--;
            return;
        }
        if (!EthanApiPlugin.loggedIn() || !started) {
            return;
        }

        state = getState();
        handleState();
    }

    private State getState() {
        if(client.getLocalPlayer().getAnimation() != -1) {
            return State.ANIMATING;
        }

        return State.WAITING;
    }

    private void handleState() {
        switch (state) {
            case ANIMATING:
                break;
            case WAITING:
                break;
            case BREAK:
                break;
            case TIMEOUT:
                break;
            default:
                break;
        }
    }

    private final HotkeyListener toggle = new HotkeyListener(() -> config.toggle()) {
        @Override
        public void hotkeyPressed() {
            toggle();
        }
    };

    public void toggle() {
        if (!EthanApiPlugin.loggedIn()) {
            return;
        }
        started = !started;
    }
}
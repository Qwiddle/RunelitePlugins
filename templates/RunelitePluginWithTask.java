package ${PACKAGE_NAME}.${PLUGIN_NAME};

import com.example.EthanApiPlugin.EthanApiPlugin;
import com.example.Packets.*;
import com.google.inject.Inject;
import com.google.inject.Provides;
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
import com.piggyplugins.PiggyUtils.strategy.TaskManager;
import com.piggyplugins.PiggyUtils.strategy.AbstractTask;
import ${PACKAGE_NAME}.${PLUGIN_NAME}.data.State;

@PluginDescriptor(
        name = "${PLUGIN_NAME}",
        description = "${PLUGIN_DESCRIPTION}",
        enabledByDefault = false,
        tags = {"spin", "plugin"}
)

public class ${PLUGIN_NAME}Plugin extends Plugin {
    private static final Logger log = LoggerFactory.getLogger(${PLUGIN_NAME}Plugin.class);
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

    public State playerState;
    public TaskManager taskManager = new TaskManager();

    public boolean bankPin = false;
    public boolean started = false;

    public int idleTicks = 0;
    public int timeout = 0;

    @Provides
    private ${PLUGIN_NAME}Config getConfig(ConfigManager configManager) {
        return configManager.getConfig(${PLUGIN_NAME}Config.class);
    }

    private final HotkeyListener toggle = new HotkeyListener(() -> config.toggle()) {
        @Override
        public void hotkeyPressed() {
            toggle();
        }
    };

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
        if (this.client.getLocalPlayer().isInteracting() || this.client.getLocalPlayer().getAnimation() == -1) {
            idleTicks++;
        } else {
            idleTicks = 0;
        }

        if (!EthanApiPlugin.loggedIn() || !started) {
            return;
        }

        if (timeout > 0) {
            timeout--;
            return;
        }

        if (taskManager.hasTasks()) {
            for (AbstractTask t : taskManager.getTasks()) {
                if (t.validate()) {
                    t.execute();
                    return;
                }
            }
        }
    }

    public void toggle() {
        if (!EthanApiPlugin.loggedIn()) {
            return;
        }

        started = !started;

        if (started) {
            //taskManager.addTask(new ...Task(this, config));
        } else {
            taskManager.clearTasks();
        }
    }
}
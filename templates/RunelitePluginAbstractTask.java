package ${PACKAGE_NAME}.${PLUGIN_NAME}.tasks;

import com.piggyplugins.PiggyUtils.strategy.AbstractTask;
import com.spinplugins.IronBuddy.IronBuddyConfig;
import com.spinplugins.IronBuddy.IronBuddyPlugin;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Optional;

@Slf4j
public class ${TASK_NAME}Task extends AbstractTask<${PLUGIN_NAME}Plugin, ${PLUGIN_NAME}Config> {
    public ${TASK_NAME}Task(${PLUGIN_NAME}Plugin plugin, ${PLUGIN_NAME}Config config) {
        super(plugin, config);
    }

    @Override
    public boolean validate() {
        return true;
    }

    @Override
    public void execute() {
        return;
    }
}

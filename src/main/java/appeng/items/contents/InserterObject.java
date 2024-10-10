package appeng.items.contents;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import appeng.api.config.InserterMode;
import appeng.api.config.Settings;
import appeng.api.implementations.guiobjects.IGuiItemObject;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.items.tools.ToolInserter;
import appeng.util.ConfigManager;
import appeng.util.Platform;

public class InserterObject implements IGuiItemObject, IConfigurableObject {

    private final ItemStack stack;
    private final int slot;

    private final IConfigManager configManager;

    public InserterObject(final ItemStack stack, int slot) {
        this.stack = stack;
        this.slot = slot;
        this.configManager = createConfigManager();
    }

    @Override
    public IConfigManager getConfigManager() {
        return configManager;
    }

    private IConfigManager createConfigManager() {
        final ConfigManager out = new ConfigManager((manager, settingName, newValue) -> {
            final NBTTagCompound data = Platform.openNbtData(this.stack);
            manager.writeToNBT(data);
        });

        out.registerSetting(Settings.INSERTER_MODE, InserterMode.INSERT);

        out.readFromNBT((NBTTagCompound) Platform.openNbtData(this.stack).copy());
        return out;
    }

    @Override
    public ItemStack getItemStack() {
        return this.stack;
    }

    public InserterMode getMode() {
        return (InserterMode) configManager.getSetting(Settings.INSERTER_MODE);
    }

    public void setMode(InserterMode newValue) {
        configManager.putSetting(Settings.INSERTER_MODE, newValue);
    }

    public int getInventorySlot() {
        return slot;
    }
}

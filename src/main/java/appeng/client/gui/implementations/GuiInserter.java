package appeng.client.gui.implementations;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import org.lwjgl.input.Mouse;

import appeng.api.AEApi;
import appeng.api.definitions.IDefinitions;
import appeng.api.definitions.IParts;
import appeng.api.storage.ITerminalHost;
import appeng.api.config.InserterMode;
import appeng.api.config.Settings;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.container.implementations.ContainerInserter;
import appeng.core.localization.GuiColors;
import appeng.core.localization.GuiText;
import appeng.core.sync.GuiBridge;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketPatternValueSet;
import appeng.core.sync.packets.PacketConfigButton;
import appeng.helpers.Reflected;
import appeng.parts.reporting.PartPatternTerminal;
import appeng.parts.reporting.PartPatternTerminalEx;
import appeng.items.contents.InserterObject;

public class GuiInserter extends GuiAmount {

    private final int valueIndex;
    private final int originalAmount;

    private final ContainerInserter container;
    private GuiImgButton mode;

    public GuiInserter(final InventoryPlayer inventoryPlayer, final InserterObject host) {
        super(new ContainerInserter(inventoryPlayer, host));
        this.container = (ContainerInserter) this.inventorySlots;
        GuiContainer gui = (GuiContainer) Minecraft.getMinecraft().currentScreen;
        if (gui != null && gui.theSlot != null && gui.theSlot.getHasStack()) {
            Slot slot = gui.theSlot;
            originalAmount = slot.getStack().stackSize;
            valueIndex = slot.slotNumber;
        } else {
            valueIndex = -1;
            originalAmount = 0;
        }
    }

    @Override
    public void initGui() {
        super.initGui();
        this.amountTextField.setText(String.valueOf(originalAmount));
        this.amountTextField.setSelectionPos(0);

        this.mode = new GuiImgButton(
            this.guiLeft - 18,
            this.guiTop + 8,
            Settings.INSERTER_MODE,
            InserterMode.INSERT);
        this.buttonList.add(this.mode);
    }

    @Override
    protected void setOriginGUI(Object target) {
        final IDefinitions definitions = AEApi.instance().definitions();
        final IParts parts = definitions.parts();

        if (target instanceof PartPatternTerminal) {
            for (final ItemStack stack : parts.patternTerminal().maybeStack(1).asSet()) {
                myIcon = stack;
            }
            this.originalGui = GuiBridge.GUI_PATTERN_TERMINAL;
        }

        if (target instanceof PartPatternTerminalEx) {
            for (final ItemStack stack : parts.patternTerminalEx().maybeStack(1).asSet()) {
                myIcon = stack;
            }
            this.originalGui = GuiBridge.GUI_PATTERN_TERMINAL_EX;
        }
    }

    @Override
    public void drawFG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        this.fontRendererObj
                .drawString(GuiText.SelectAmount.getLocal(), 8, 6, GuiColors.CraftAmountSelectAmount.getColor());
        if (this.mode != null) {
            this.mode.set(this.container.getToolMode());
        }
    }

    @Override
    public void drawBG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        super.drawBG(offsetX, offsetY, mouseX, mouseY);
        this.nextBtn.displayString = GuiText.Set.getLocal();
        this.nextBtn.enabled = valueIndex >= 0;

        try {
            int resultI = getAmount();
            this.nextBtn.enabled = resultI > 0;
        } catch (final NumberFormatException e) {
            this.nextBtn.enabled = false;
        }

        this.amountTextField.drawTextBox();
    }

    @Override
    protected void actionPerformed(final GuiButton btn) {
        super.actionPerformed(btn);

        final boolean backwards = Mouse.isButtonDown(1);

        if (btn == this.mode) {
            NetworkHandler.instance.sendToServer(new PacketConfigButton(Settings.INSERTER_MODE, backwards));
        }
    }

    protected String getBackground() {
        return "guis/craftAmt.png";
    }
}

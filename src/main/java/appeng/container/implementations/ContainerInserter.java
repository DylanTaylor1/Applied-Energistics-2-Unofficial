/*
 * This file is part of Applied Energistics 2. Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved. Applied
 * Energistics 2 is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version. Applied Energistics 2 is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details. You should have received a copy of the GNU Lesser General Public License along with
 * Applied Energistics 2. If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.container.implementations;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import appeng.api.config.InserterMode;
import appeng.api.config.Settings;
import appeng.api.util.IConfigManager;
import appeng.container.guisync.GuiSync;
import appeng.container.AEBaseContainer;
import appeng.items.contents.InserterObject;
import appeng.util.Platform;

public class ContainerInserter extends AEBaseContainer {

    private final InserterObject host;

    @GuiSync(3)
    public InserterMode toolMode = InserterMode.INSERT;

    public ContainerInserter(final InventoryPlayer ip, final InserterObject host) {
        super(ip, host);
        this.host = host;
        this.lockPlayerInventorySlot(host.getInventorySlot());
    }

    @Override
    public void detectAndSendChanges() {
        if (Platform.isServer()) {
            final IConfigManager cm = this.host.getConfigManager();
            this.setToolMode((InserterMode) cm.getSetting(Settings.INSERTER_MODE));
        }

        final ItemStack currentItem = this.getPlayerInv().getStackInSlot(this.host.getInventorySlot());

        if (currentItem != this.host.getItemStack()) {
            if (currentItem != null) {
                if (Platform.isSameItem(this.host.getItemStack(), currentItem)) {
                    this.getPlayerInv()
                            .setInventorySlotContents(this.host.getInventorySlot(), this.host.getItemStack());
                } else {
                    this.setValidContainer(false);
                }
            } else {
                this.setValidContainer(false);
            }
        }

        super.detectAndSendChanges();
    }

    public void setToolMode(InserterMode mode) {
        this.toolMode = mode;
    }

    public InserterMode getToolMode() {
        return this.toolMode;
    }
}

package appeng.items.tools;

import java.util.EnumSet;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.config.InserterMode;
import appeng.api.config.InsertionMode;
import appeng.api.config.SecurityPermissions;
import appeng.api.config.Settings;
import appeng.api.implementations.guiobjects.IGuiItem;
import appeng.api.implementations.guiobjects.IGuiItemObject;
import appeng.api.implementations.items.IStorageCell;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.ISecurityGrid;
import appeng.api.storage.ICellInventory;
import appeng.api.storage.ICellInventoryHandler;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.core.AELog;
import appeng.core.features.AEFeature;
import appeng.core.localization.GuiText;
import appeng.core.localization.PlayerMessages;
import appeng.core.sync.GuiBridge;
import appeng.items.AEBaseItem;
import appeng.items.contents.InserterObject;
import appeng.items.contents.CellConfig;
import appeng.me.storage.CellInventoryHandler;
import appeng.items.contents.CellUpgrades;
import appeng.parts.AEBasePart;
import appeng.tile.AEBaseTile;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.IterationCounter;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.inv.AdaptorIInventory;
import appeng.util.inv.WrapperInvSlot;
import appeng.util.item.AEItemStack;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ToolInserter extends AEBaseItem 
        implements IGuiItem, IStorageCell {

    private int uses;
    private BaseActionSource mySrc;
    // public static final int NUMBER_OF_STORAGE_SLOTS = 1;
    // private final AppEngInternalInventory storage = new AppEngInternalInventory(this, NUMBER_OF_STORAGE_SLOTS);
    // private final WrapperInvSlot slotInv = new WrapperInvSlot(this.storage);

    public ToolInserter() {
        this.setFeature(EnumSet.of(AEFeature.Core));
        this.setMaxStackSize(1);
    }

    @Override
    protected void addCheckedInformation(ItemStack stack, EntityPlayer player, List<String> lines,
            boolean displayMoreInfo) {
        super.addCheckedInformation(stack, player, lines, displayMoreInfo);

        lines.add(GuiText.InserterTooltip.getLocal());

        GuiText mode = switch (getMode(stack)) {
            case INSERT -> GuiText.InserterTooltipModeInsert;
            case EXTRACT -> GuiText.InserterTooltipModeExtract;
        };
        lines.add(mode.getLocal());

        final IMEInventory<IAEItemStack> cdi = AEApi.instance().registries().cell()
                .getCellInventory(stack, null, StorageChannel.ITEMS);

        if (cdi instanceof CellInventoryHandler) {
            final ICellInventory cd = ((ICellInventoryHandler) cdi).getCellInv();
            if (cd != null) {
                lines.add(
                        cd.getUsedBytes() + " "
                                + GuiText.Of.getLocal()
                                + ' '
                                + cd.getTotalBytes()
                                + ' '
                                + GuiText.BytesUsed.getLocal());
                lines.add(
                        cd.getStoredItemTypes() + " "
                                + GuiText.Of.getLocal()
                                + ' '
                                + cd.getTotalItemTypes()
                                + ' '
                                + GuiText.Types.getLocal());
            }
        }
    }

    // @Override
    // public ItemStack onItemRightClick(final ItemStack it, final World w, final EntityPlayer p) {
    //     if (p.isSneaking()) {
    //         if (Platform.isServer()) {
    //             Platform.openGUI(p, null, ForgeDirection.UNKNOWN, GuiBridge.GUI_INSERTER);
    //         }
    //     }
    //     return it;
    // }

    @Override
    public boolean onItemUse(final ItemStack is, final EntityPlayer p, final World w, final int x, final int y,
            final int z, final int side, final float hitX, final float hitY, final float hitZ) {
        if (Platform.isServer()) {
            TileEntity te = w.getTileEntity(x, y, z);

            // System.out.println(x);
            // System.out.println(y);
            // System.out.println(z);

            if (te instanceof IInventory) {
                IMEInventory<IAEItemStack> inv = AEApi.instance().registries().cell()
                .getCellInventory(is, null, StorageChannel.ITEMS);

                if (inv != null) {
                    final IAEItemStack ais = inv.getAvailableItems(
                        AEApi.instance().storage().createItemList(), IterationCounter.fetchNewId()).getFirstItem();

                        // System.out.println(ais);

                    if (ais != null) {
                        final ItemStack item = ais.getItemStack();
                        item.stackSize = 1;

                        final ForgeDirection orientation = ForgeDirection.getOrientation(side);
                        InventoryAdaptor ad = InventoryAdaptor.getAdaptor(te, orientation);

                        // this.uses += 1;
                        // System.out.println(this.uses);

                        ItemStack sim = ad.simulateAdd(item); // Null means it CAN insert
                        if (sim == null) {
                            ad.addItems(item, InsertionMode.DEFAULT);
                            inv.extractItems(AEItemStack.create(item), Actionable.MODULATE, this.mySrc);
                        }
                    }
                }
            

                // INITIAL IMPLEMENTATION

                // int i = findEmpty(inventory);

                // if (i >= 0) {
                //     final IMEInventory<IAEItemStack> inv = AEApi.instance().registries().cell()
                //             .getCellInventory(is, null, StorageChannel.ITEMS);

                //     if (inv != null) {
                //         IAEItemStack ais = inv.getAvailableItems(
                //             AEApi.instance().storage().createItemList(), IterationCounter.fetchNewId()).getFirstItem();

                //         if (ais != null) {
                //             ItemStack item = ais.getItemStack();
                //             item.stackSize = 1;

                //             final ForgeDirection orientation = ForgeDirection.getOrientation(side);
                //             InventoryAdaptor ad = InventoryAdaptor.getAdaptor(te, orientation);

                //             // System.out.println("TEST2");

                //             final ItemStack o = ad.simulateAdd(item);
                //             final long canFit = o == null ? item.stackSize : item.stackSize - o.stackSize;


                //             ais = ais.copy();
                //             ais.setStackSize(canFit);
                //             final IAEItemStack itemsToAdd = inv.extractItems(ais, Actionable.MODULATE, this.mySrc);

                //             if (canFit > 0) {
                                

                //                 if (itemsToAdd != null) {

                //                     // System.out.println("TEST4");

                //                     final ItemStack failed = ad.addItems(item, InsertionMode.DEFAULT);
                //                     // if (failed != null) {
                //                     //     ais.setStackSize(failed.stackSize);
                //                     //     //inv.injectItems(ais, Actionable.MODULATE, this.mySrc);
                //                     // }
                //                 }
                //             }


                //             // if (ad != null) {
                //             //     result = ad.addItems(item, InsertionMode.DEFAULT);
                //             //     System.out.println(result);
                //             // }

                //             // if (result != null) {
                //             //     inv.extractItems(AEItemStack.create(item), Actionable.MODULATE, new BaseActionSource());
                //             //     return true;
                //             // }
                //         }
                //     }
                // }

            } else {
                Platform.openGUI(p, null, ForgeDirection.UNKNOWN, GuiBridge.GUI_INSERTER);
                return true;
            }
        }
        return false;
    }

    public static int findEmpty(IInventory inv) {
        int size = inv.getSizeInventory();

        if (size == 0) {
            return -1;
        }

        for (int i = 0; i < size; i++) {
            if (inv.getStackInSlot(i) == null) {
                return i;
            }
        }
        return -1;
    }

    // private InventoryAdaptor getAdaptor(final int slot) {
    //     return new AdaptorIInventory(this.slotInv.getWrapper(slot));
    // }

    @Override
    public IGuiItemObject getGuiObject(final ItemStack is, final World world, final int x, final int y, final int z) {
        return new InserterObject(is, x);
    }

    // public static void handleUse(EntityPlayer player, AEBaseTile tile, ItemStack stack, ForgeDirection side) {
    //     if (Platform.isClient()) {
    //         return;
    //     }
    // }

    // public static void handleUse(EntityPlayer player, AEBasePart part, ItemStack stack, ForgeDirection side) {
    //     if (Platform.isClient()) {
    //         return;
    //     }
    // }

    // private static void handleUse(EntityPlayer player, TileEntity tile, ItemStack stack, ForgeDirection side) {
    //     if (Platform.isClient()) {
    //         return;
    //     }
    // }

    private static boolean securityCheck(final IActionHost actionHost, final EntityPlayer player) {
        final IGridNode gn = actionHost.getActionableNode();
        if (gn != null) {
            final IGrid g = gn.getGrid();
            if (g != null) {
                final ISecurityGrid sg = g.getCache(ISecurityGrid.class);
                return sg.hasPermission(player, SecurityPermissions.BUILD);
            }
        }
        return false;
    }

    private static InserterMode getMode(ItemStack stack) {
        // Setting is stored via ConfigManager and directly retrieved here
        NBTTagCompound tagCompound = Platform.openNbtData(stack);
        try {
            if (tagCompound.hasKey(Settings.INSERTER_MODE.name())) {
                return InserterMode.valueOf(tagCompound.getString(Settings.INSERTER_MODE.name()));
            }
        } catch (final IllegalArgumentException e) {
            AELog.debug(e);
        }
        return InserterMode.INSERT;
    }

    @Override
    public int getBytes(final ItemStack cellItem) {
        return 4096;
    }

    @Override
    public int BytePerType(final ItemStack cell) {
        return 8;
    }

    @Override
    public int getBytesPerType(final ItemStack cellItem) {
        return 8;
    }

    @Override
    public int getTotalTypes(final ItemStack cellItem) {
        return 1;
    }

    @Override
    public boolean storableInStorageCell() {
        return true;
    }

    @Override
    public boolean isStorageCell(final ItemStack i) {
        return true;
    }

    @Override
    public double getIdleDrain() {
        return 0.5;
    }

    @Override
    public boolean isBlackListed(final ItemStack cellItem, final IAEItemStack requestedAddition) {
        return false;
    }

    @Override
    public boolean isEditable(final ItemStack is) {
        return true;
    }

    @Override
    public IInventory getUpgradesInventory(final ItemStack is) {
        return new CellUpgrades(is, 2);
    }

    @Override
    public IInventory getConfigInventory(final ItemStack is) {
        return new CellConfig(is);
    }

    @Override
    public FuzzyMode getFuzzyMode(final ItemStack is) {
        return FuzzyMode.fromItemStack(is);
    }

    @Override
    public void setFuzzyMode(final ItemStack is, final FuzzyMode fzMode) {
        Platform.openNbtData(is).setString("FuzzyMode", fzMode.name());
    }

    @Override
    public String getOreFilter(ItemStack is) {
        return Platform.openNbtData(is).getString("OreFilter");
    }

    @Override
    public void setOreFilter(ItemStack is, String filter) {
        Platform.openNbtData(is).setString("OreFilter", filter);
    }
}

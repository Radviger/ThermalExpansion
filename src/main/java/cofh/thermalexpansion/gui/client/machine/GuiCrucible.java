package cofh.thermalexpansion.gui.client.machine;

import cofh.lib.gui.element.*;
import cofh.thermalexpansion.gui.client.GuiPoweredBase;
import cofh.thermalexpansion.gui.container.machine.ContainerCrucible;
import cofh.thermalexpansion.gui.element.ElementSlotOverlay;
import cofh.thermalexpansion.init.TEProps;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

public class GuiCrucible extends GuiPoweredBase {

	public static final ResourceLocation TEXTURE = new ResourceLocation(TEProps.PATH_GUI_MACHINE + "crucible.png");

	private ElementBase slotInput;
	private ElementBase slotOutput;
	private ElementFluid progressFluid;
	private ElementDualScaled progressOverlay;
	private ElementDualScaled speed;

	public GuiCrucible(InventoryPlayer inventory, TileEntity tile) {

		super(new ContainerCrucible(inventory, tile), tile, inventory.player, TEXTURE);

		generateInfo("tab.thermalexpansion.machine.crucible", 3);
	}

	@Override
	public void initGui() {

		super.initGui();

		slotInput = addElement(new ElementSlotOverlay(this, 53, 26).setSlotInfo(0, 0, 2));
		slotOutput = addElement(new ElementSlotOverlay(this, 152, 9).setSlotInfo(3, 3, 2));

		addElement(new ElementEnergyStored(this, 8, 8, baseTile.getEnergyStorage()));
		addElement(new ElementFluidTank(this, 152, 9, baseTile.getTank()).setGauge(1).setAlwaysShow(true));
		progressFluid = (ElementFluid) addElement(new ElementFluid(this, 103, 34).setFluid(baseTile.getTankFluid()).setSize(24, 16));
		progressOverlay = (ElementDualScaled) addElement(new ElementDualScaled(this, 103, 34).setMode(1).setBackground(false).setSize(24, 16).setTexture(TEX_DROP_RIGHT, 64, 16));
		speed = (ElementDualScaled) addElement(new ElementDualScaled(this, 53, 44).setSize(16, 16).setTexture(TEX_FLAME, 32, 16));
	}

	@Override
	protected void updateElementInformation() {

		super.updateElementInformation();

		slotInput.setVisible(baseTile.hasSide(1));
		slotOutput.setVisible(baseTile.hasSide(2));

		progressFluid.setFluid(baseTile.getTankFluid());
		progressFluid.setSize(baseTile.getScaledProgress(PROGRESS), 16);
		progressOverlay.setQuantity(baseTile.getScaledProgress(PROGRESS));
		speed.setQuantity(baseTile.getScaledSpeed(SPEED));
	}

}

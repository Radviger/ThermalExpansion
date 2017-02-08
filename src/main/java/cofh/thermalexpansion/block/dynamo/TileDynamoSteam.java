package cofh.thermalexpansion.block.dynamo;

import codechicken.lib.texture.TextureUtils;
import cofh.core.fluid.FluidTankCore;
import cofh.core.init.CoreProps;
import cofh.core.network.PacketCoFHBase;
import cofh.lib.inventory.ComparableItemStack;
import cofh.thermalexpansion.ThermalExpansion;
import cofh.thermalexpansion.gui.client.dynamo.GuiDynamoSteam;
import cofh.thermalexpansion.gui.container.dynamo.ContainerDynamoSteam;
import cofh.thermalexpansion.init.TEProps;
import cofh.thermalfoundation.init.TFFluids;
import gnu.trove.map.hash.TObjectIntHashMap;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.common.registry.GameRegistry;

import javax.annotation.Nullable;

public class TileDynamoSteam extends TileDynamoBase {

	private static final int TYPE = BlockDynamo.Type.STEAM.getMetadata();

	public static void initialize() {

		GameRegistry.registerTileEntity(TileDynamoSteam.class, "thermalexpansion.dynamo_steam");

		config();
	}

	public static void config() {

		String category = "Dynamo.Steam";
		BlockDynamo.enable[TYPE] = ThermalExpansion.CONFIG.get(category, "Enable", true);
	}

	private static final int STEAM_MIN = 2000;

	private FluidTankCore steamTank = new FluidTankCore(TEProps.MAX_FLUID_SMALL);
	private FluidTankCore waterTank = new FluidTankCore(TEProps.MAX_FLUID_SMALL);

	private int currentFuelRF = DEFAULT_RF;
	private int steamAmount = defaultEnergyConfig[TYPE].maxPower / 2;

	private FluidStack steam = new FluidStack(FluidRegistry.getFluid("steam"), steamAmount);

	public TileDynamoSteam() {

		super();
		inventory = new ItemStack[1];
	}

	@Override
	public int getType() {

		return TYPE;
	}

	protected boolean canStart() {

		return steamTank.getFluidAmount() >= STEAM_MIN;
	}

	protected void processStart() {

		processRem += getEnergyValue(inventory[0]) * energyMod / ENERGY_BASE;
	}


//	@Override
//	protected boolean canGenerate() {
//
//		if (steamTank.getFluidAmount() > STEAM_MIN) {
//			return true;
//		}
//		if (waterTank.getFluidAmount() < energyConfig.maxPower) {
//			return false;
//		}
//		return processRem > 0 || getEnergyValue(inventory[0]) > 0;
//	}
//
//	@Override
//	public void generate() {
//
//		if (steamTank.getFluidAmount() >= STEAM_MIN + steamAmount) {
//			int energy = calcEnergy();
//			energyStorage.modifyEnergyStored(energy);
//			steamTank.drain(energy >> 1, true);
//		} else {
//			if (processRem <= 0 && inventory[0] != null) {
//				int energy = getEnergyValue(inventory[0]) * energyMod / ENERGY_BASE;
//				processRem += energy;
//				currentFuelRF = energy;
//				inventory[0] = ItemHelper.consumeItem(inventory[0]);
//			}
//			if (processRem > 0) {
//				int filled = steamTank.fill(steam, true);
//				processRem -= filled << 1;
//				if (timeCheck()) {
//					waterTank.drain(filled, true);
//				}
//			}
//			if (steamTank.getFluidAmount() > STEAM_MIN) {
//				int energy = Math.min((steamTank.getFluidAmount() - STEAM_MIN) << 1, calcEnergy());
//				energyStorage.modifyEnergyStored(energy);
//				steamTank.drain(energy >> 1, true);
//			}
//			return;
//		}
//		if (processRem > 0) {
//			int filled = steamTank.fill(steam, true);
//			processRem -= filled << 1;
//			if (timeCheck()) {
//				waterTank.drain(filled, true);
//			}
//		}
//	}



	@Override
	public TextureAtlasSprite getActiveIcon() {

		return TextureUtils.getTexture(TFFluids.fluidSteam.getStill());
	}

	/* GUI METHODS */
	@Override
	public Object getGuiClient(InventoryPlayer inventory) {

		return new GuiDynamoSteam(inventory, this);
	}

	@Override
	public Object getGuiServer(InventoryPlayer inventory) {

		return new ContainerDynamoSteam(inventory, this);
	}

	@Override
	public int getScaledDuration(int scale) {

		if (currentFuelRF <= 0) {
			currentFuelRF = DEFAULT_RF;
		}
		return processRem * scale / currentFuelRF;
	}

	@Override
	public FluidTankCore getTank(int tankIndex) {

		if (tankIndex == 0) {
			return steamTank;
		}
		return waterTank;
	}

	/* NBT METHODS */
	@Override
	public void readFromNBT(NBTTagCompound nbt) {

		super.readFromNBT(nbt);

		currentFuelRF = nbt.getInteger("FuelMax");
		steamTank.readFromNBT(nbt.getCompoundTag("SteamTank"));
		waterTank.readFromNBT(nbt.getCompoundTag("WaterTank"));

		if (currentFuelRF <= 0) {
			currentFuelRF = DEFAULT_RF;
		}
		steam.amount = steamAmount;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {

		super.writeToNBT(nbt);

		nbt.setInteger("FuelMax", currentFuelRF);
		nbt.setTag("SteamTank", steamTank.writeToNBT(new NBTTagCompound()));
		nbt.setTag("WaterTank", waterTank.writeToNBT(new NBTTagCompound()));
		return nbt;
	}

	/* NETWORK METHODS */
	@Override
	public PacketCoFHBase getGuiPacket() {

		PacketCoFHBase payload = super.getGuiPacket();

		payload.addInt(currentFuelRF);
		payload.addFluidStack(steamTank.getFluid());
		payload.addFluidStack(waterTank.getFluid());

		return payload;
	}

	@Override
	protected void handleGuiPacket(PacketCoFHBase payload) {

		super.handleGuiPacket(payload);

		currentFuelRF = payload.getInt();
		steamTank.setFluid(payload.getFluidStack());
		waterTank.setFluid(payload.getFluidStack());
	}

	/* HELPERS */

	/* IEnergyInfo */
	@Override
	public int getInfoEnergyPerTick() {

		return steamTank.getFluidAmount() >= STEAM_MIN ? calcEnergy() : 0;
	}

	/* IInventory */
	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {

		return getEnergyValue(stack) > 0;
	}

	/* ISidedInventory */
	@Override
	public int[] getSlotsForFace(EnumFacing side) {

		return side.ordinal() != facing || augmentCoilDuct ? CoreProps.SINGLE_INVENTORY : CoreProps.EMPTY_INVENTORY;
	}

	/* CAPABILITIES */
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing from) {

		return super.hasCapability(capability, from) || capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;
	}

	@Override
	public <T> T getCapability(Capability<T> capability, final EnumFacing from) {

		if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
			return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(new IFluidHandler() {
				@Override
				public IFluidTankProperties[] getTankProperties() {

					return FluidTankProperties.convert(new FluidTankInfo[] { steamTank.getInfo(), waterTank.getInfo() });
				}

				@Override
				public int fill(FluidStack resource, boolean doFill) {

					if (resource == null || (from != null && from.ordinal() == facing && !augmentCoilDuct)) {
						return 0;
					}
					if (resource.getFluid() == steam.getFluid()) {
						return steamTank.fill(resource, doFill);
					}
					if (resource.getFluid() == FluidRegistry.WATER) {
						return waterTank.fill(resource, doFill);
					}
					return 0;
				}

				@Nullable
				@Override
				public FluidStack drain(FluidStack resource, boolean doDrain) {

					if (resource == null || from == null || !augmentCoilDuct && from.ordinal() == facing) {
						return null;
					}
					if (resource.getFluid() == FluidRegistry.WATER) {
						return waterTank.drain(resource.amount, doDrain);
					}
					return null;
				}

				@Nullable
				@Override
				public FluidStack drain(int maxDrain, boolean doDrain) {

					if (!augmentCoilDuct && from.ordinal() == facing) {
						return null;
					}
					return waterTank.drain(maxDrain, doDrain);
				}
			});
		}
		return super.getCapability(capability, from);
	}

	/* FUEL MANAGER */
	private static TObjectIntHashMap<ComparableItemStack> fuels = new TObjectIntHashMap<ComparableItemStack>();

	private static int DEFAULT_RF = 48000;

	public static boolean addFuel(ItemStack stack, int energy) {

		if (stack == null || energy < 640 || energy > 200000000) {
			return false;
		}
		fuels.put(new ComparableItemStack(stack), energy);
		return true;
	}

	public static boolean removeFuel(ItemStack stack) {

		fuels.remove(new ComparableItemStack(stack));
		return true;
	}

	public static int getEnergyValue(ItemStack stack) {

		if (stack == null) {
			return 0;
		}
		int energy = fuels.get(new ComparableItemStack(stack));

		return energy > 0 ? energy : GameRegistry.getFuelValue(stack) * CoreProps.RF_PER_MJ * 3 / 2;
	}

}

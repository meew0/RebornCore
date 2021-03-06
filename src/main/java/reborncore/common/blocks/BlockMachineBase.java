package reborncore.common.blocks;

import me.modmuss50.jsonDestroyer.api.IFakeTexturedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDynamicLiquid;
import net.minecraft.block.BlockStaticLiquid;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidBase;
import reborncore.api.tile.IUpgrade;
import reborncore.api.tile.IUpgradeable;
import reborncore.common.BaseTileBlock;
import reborncore.common.tile.TileMachineBase;
import reborncore.common.util.InventoryHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class BlockMachineBase extends BaseTileBlock implements IFakeTexturedBlock {

	public static PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
	public static PropertyBool ACTIVE = PropertyBool.create("active");

	public static ItemStack machineStack;
	public static ItemStack advancedMachineStack;
	boolean hasCustomStaes;

	public BlockMachineBase() {
		this(false);
	}

	public BlockMachineBase(boolean hasCustomStates) {
		super(Material.ROCK);
		setHardness(2f);
		setSoundType(SoundType.METAL);
		this.hasCustomStaes = hasCustomStates;
		if (!hasCustomStates) {
			this.setDefaultState(
				this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(ACTIVE, false));
		}
	}

	protected BlockStateContainer createBlockState() {

		FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
		ACTIVE = PropertyBool.create("active");
		return new BlockStateContainer(this, FACING, ACTIVE);
	}

	@Override
	public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
		//return new TileEnergyMachine();
		return null;
	}

	@Deprecated
	public void onBlockAdded(World world, int x, int y, int z) {
	}

	@Override
	public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
		super.onBlockAdded(worldIn, pos, state);
		onBlockAdded(worldIn, pos.getX(), pos.getY(), pos.getZ());
		this.setDefaultFacing(worldIn, pos, state);
	}

	private void setDefaultFacing(World worldIn, BlockPos pos, IBlockState state) {
		if (hasCustomStaes) {
			return;
		}
		if (!worldIn.isRemote) {
			IBlockState sate = worldIn.getBlockState(pos.north());
			Block block = sate.getBlock();
			IBlockState state1 = worldIn.getBlockState(pos.south());
			Block block1 = state1.getBlock();
			IBlockState state2 = worldIn.getBlockState(pos.west());
			Block block2 = state2.getBlock();
			IBlockState state3 = worldIn.getBlockState(pos.east());
			Block block3 = state3.getBlock();
			EnumFacing enumfacing = (EnumFacing) state.getValue(FACING);

			if (enumfacing == EnumFacing.NORTH && block.isFullBlock(state) && !block1.isFullBlock(state1)) {
				enumfacing = EnumFacing.SOUTH;
			} else if (enumfacing == EnumFacing.SOUTH && block1.isFullBlock(state1) && !block.isFullBlock(state)) {
				enumfacing = EnumFacing.NORTH;
			} else if (enumfacing == EnumFacing.WEST && block2.isFullBlock(state2) && !block3.isFullBlock(state2)) {
				enumfacing = EnumFacing.EAST;
			} else if (enumfacing == EnumFacing.EAST && block3.isFullBlock(state3) && !block2.isFullBlock(state2)) {
				enumfacing = EnumFacing.WEST;
			}

			worldIn.setBlockState(pos, state.withProperty(FACING, enumfacing), 2);
		}
	}

	@Deprecated
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack itemstack) {

	}

	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer,
	                            ItemStack stack) {
		super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
		onBlockPlacedBy(worldIn, pos.getX(), pos.getY(), pos.getZ(), placer, stack);
		setFacing(placer.getHorizontalFacing().getOpposite(), worldIn, pos);
	}

	public boolean canCreatureSpawn(EnumCreatureType type, World world, int x, int y, int z) {
		return false;
	}

	@Deprecated
	public void breakBlock(World world, int x, int y, int z, Block block, int meta) {

	}

	@Override
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
		dropInventory(worldIn, pos);
		super.breakBlock(worldIn, pos, state);
		breakBlock(worldIn, pos.getX(), pos.getY(), pos.getZ(), state.getBlock(), 0);
	}

	protected void dropInventory(World world, BlockPos pos) {
		TileEntity tileEntity = world.getTileEntity(pos);

		if (tileEntity == null) {
			return;
		}
		if (!(tileEntity instanceof IInventory)) {
			return;
		}

		IInventory inventory = (IInventory) tileEntity;

		List<ItemStack> items = new ArrayList<ItemStack>();

		addItemsToList(inventory, items);
		if(tileEntity instanceof IUpgradeable){
			addItemsToList(((IUpgradeable) tileEntity).getUpgradeInvetory(), items);
		}

		for (ItemStack itemStack : items) {
			Random rand = new Random();

			float dX = rand.nextFloat() * 0.8F + 0.1F;
			float dY = rand.nextFloat() * 0.8F + 0.1F;
			float dZ = rand.nextFloat() * 0.8F + 0.1F;

			EntityItem entityItem = new EntityItem(world, pos.getX() + dX, pos.getY() + dY, pos.getZ() + dZ,
				itemStack.copy());

			if (itemStack.hasTagCompound()) {
				entityItem.getEntityItem().setTagCompound((NBTTagCompound) itemStack.getTagCompound().copy());
			}

			float factor = 0.05F;
			entityItem.motionX = rand.nextGaussian() * factor;
			entityItem.motionY = rand.nextGaussian() * factor + 0.2F;
			entityItem.motionZ = rand.nextGaussian() * factor;
			world.spawnEntity(entityItem);
			itemStack.setCount(0);
		}
	}

	private void addItemsToList(IInventory inventory, List<ItemStack> items){
		for (int i = 0; i < inventory.getSizeInventory(); i++) {
			ItemStack itemStack = inventory.getStackInSlot(i);

			if (itemStack == ItemStack.EMPTY) {
				continue;
			}
			if (itemStack != ItemStack.EMPTY && itemStack.getCount() > 0) {
				if (itemStack.getItem() instanceof ItemBlock) {
					if (((ItemBlock) itemStack.getItem()).block instanceof BlockFluidBase
						|| ((ItemBlock) itemStack.getItem()).block instanceof BlockStaticLiquid
						|| ((ItemBlock) itemStack.getItem()).block instanceof BlockDynamicLiquid) {
						continue;
					}
				}
			}
			items.add(itemStack.copy());
		}
	}


	@Override
	public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		List<ItemStack> items = new ArrayList<ItemStack>();
		items.add(isAdvanced() ? advancedMachineStack.copy() : machineStack.copy());
		return items;
	}

	public boolean isAdvanced() {
		return false;
	}

	@Override
	public boolean rotateBlock(World world, BlockPos pos, EnumFacing axis) {
		if (axis == null) {
			return false;
		} else {
			TileEntity tile = world.getTileEntity(pos);
			if (tile != null && tile instanceof TileMachineBase) {
				world.setBlockState(pos,
					world.getBlockState(pos).withProperty(FACING, axis));
				return true;
			}
			return false;
		}
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
	                                EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (fillBlockWithFluid(worldIn, pos, playerIn)) {
			return true;
		}
		if(playerIn.isSneaking()){
			ItemStack stack = playerIn.getHeldItem(EnumHand.MAIN_HAND);
			if(!stack.isEmpty() && stack.getItem() instanceof IUpgrade){
				TileEntity tileEntity = worldIn.getTileEntity(pos);
				if(tileEntity instanceof IUpgradeable){
					if(((IUpgradeable) tileEntity).canBeUpgraded()){
						if(InventoryHelper.testInventoryInsertion(((IUpgradeable) tileEntity).getUpgradeInvetory(), stack, null) > 0){
							InventoryHelper.insertItemIntoInventory(((IUpgradeable) tileEntity).getUpgradeInvetory(), stack);
							playerIn.setHeldItem(EnumHand.MAIN_HAND, ItemStack.EMPTY);
							return true;
						}
					}
				}
			}
		}
		if (onBlockActivated(worldIn, pos.getX(), pos.getY(), pos.getZ(), playerIn, side.getIndex(), hitX, hitY, hitZ)) {
			return true;
		}
		return super.onBlockActivated(worldIn, pos, state, playerIn, hand, side, hitX, hitY, hitZ);
	}

	@Deprecated
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityplayer, int side, float hitX,
	                                float hitY, float hitZ) {
		return false;
	}

	//TODO 1.11 rewrite when I have something to test with
	public boolean fillBlockWithFluid(World world, BlockPos pos, EntityPlayer entityplayer) {
		return false;
	}

	//	public boolean fillBlockWithFluid(World world, BlockPos pos, EntityPlayer entityplayer)
	//	{
	//		ItemStack current = entityplayer.inventory.getCurrentItem();
	//
	//		if (current != ItemStack.EMPTY)
	//		{
	//			TileEntity tile = world.getTileEntity(pos);
	//
	//			if (tile instanceof IFluidHandler)
	//			{
	//				IFluidHandler tank = (IFluidHandler) tile;
	//				// Handle FluidContainerRegistry
	//				if (FluidContainerRegistry.isContainer(current))
	//				{
	//					FluidStack liquid = FluidContainerRegistry.getFluidForFilledItem(current);
	//					// Handle filled containers
	//					if (liquid != null)
	//					{
	//						int qty = tank.fill(null, liquid, true);
	//
	//						if (qty != 0 && !entityplayer.capabilities.isCreativeMode)
	//						{
	//							if (current.getCount() > 1)
	//							{
	//								if (!entityplayer.inventory
	//										.addItemStackToInventory(FluidContainerRegistry.drainFluidContainer(current)))
	//								{
	//									entityplayer.dropItem(
	//											FluidContainerRegistry.drainFluidContainer(current), false);
	//								}
	//
	//								entityplayer.inventory.setInventorySlotContents(entityplayer.inventory.currentItem,
	//										consumeItem(current));
	//							} else
	//							{
	//								entityplayer.inventory.setInventorySlotContents(entityplayer.inventory.currentItem,
	//										FluidContainerRegistry.drainFluidContainer(current));
	//							}
	//						}
	//
	//						return true;
	//
	//						// Handle empty containers
	//					} else
	//					{
	//						FluidStack available = tank.getTankInfo(null)[0].fluid;
	//
	//						if (available != null)
	//						{
	//							ItemStack filled = FluidContainerRegistry.fillFluidContainer(available, current);
	//
	//							liquid = FluidContainerRegistry.getFluidForFilledItem(filled);
	//
	//							if (liquid != null)
	//							{
	//								if (!entityplayer.capabilities.isCreativeMode)
	//								{
	//									if (current.getCount() > 1)
	//									{
	//										if (!entityplayer.inventory.addItemStackToInventory(filled))
	//										{
	//											return false;
	//										} else
	//										{
	//											entityplayer.inventory.setInventorySlotContents(
	//													entityplayer.inventory.currentItem, consumeItem(current));
	//										}
	//									} else
	//									{
	//										entityplayer.inventory.setInventorySlotContents(
	//												entityplayer.inventory.currentItem, consumeItem(current));
	//										entityplayer.inventory
	//												.setInventorySlotContents(entityplayer.inventory.currentItem, filled);
	//									}
	//								}
	//
	//								tank.drain(null, liquid.amount, true);
	//
	//								return true;
	//							}
	//						}
	//					}
	//				} else if (current.getItem() instanceof IFluidContainerItem)
	//				{
	//					if (current.getCount() != 1)
	//					{
	//						return false;
	//					}
	//
	//					if (!world.isRemote)
	//					{
	//						IFluidContainerItem container = (IFluidContainerItem) current.getItem();
	//						FluidStack liquid = container.getFluid(current);
	//						FluidStack tankLiquid = tank.getTankInfo(null)[0].fluid;
	//						boolean mustDrain = liquid == null || liquid.amount == 0;
	//						boolean mustFill = tankLiquid == null || tankLiquid.amount == 0;
	//						if (mustDrain && mustFill)
	//						{
	//							// Both are empty, do nothing
	//						} else if (mustDrain || !entityplayer.isSneaking())
	//						{
	//							liquid = tank.drain(null, 1000, false);
	//							int qtyToFill = container.fill(current, liquid, true);
	//							tank.drain(null, qtyToFill, true);
	//						} else if (mustFill || entityplayer.isSneaking())
	//						{
	//							if (liquid.amount > 0)
	//							{
	//								int qty = tank.fill(null, liquid, false);
	//								tank.fill(null, container.drain(current, qty, true), true);
	//							}
	//						}
	//					}
	//
	//					return true;
	//				}
	//			}
	//		}
	//		return false;
	//	}

	public static ItemStack consumeItem(ItemStack stack) {
		if (stack.getCount() == 1) {
			if (stack.getItem().hasContainerItem(stack)) {
				return stack.getItem().getContainerItem(stack);
			} else {
				return ItemStack.EMPTY;
			}
		} else {
			stack.splitStack(1);

			return stack;
		}
	}

	@Override
	public String getTextureNameFromState(IBlockState blockState, EnumFacing facing) {
		if (this instanceof IRotationTexture) {
			IRotationTexture rotationTexture = (IRotationTexture) this;
			if (getFacing(blockState) == facing) {
				return isActive(blockState) ? rotationTexture.getFrontOn() : rotationTexture.getFrontOff();
			}
			if (facing == EnumFacing.UP) {
				return rotationTexture.getTop();
			}
			if (facing == EnumFacing.DOWN) {
				return rotationTexture.getBottom();
			}
			return rotationTexture.getSide();
		}
		if (this instanceof IAdvancedRotationTexture) {
			IAdvancedRotationTexture advancedRotationTexture = (IAdvancedRotationTexture) this;
			if (getFacing(blockState) == facing) {
				return advancedRotationTexture.getFront(isActive(blockState));
			}
			if (facing == EnumFacing.UP) {
				return advancedRotationTexture.getTop(isActive(blockState));
			}
			if (facing == EnumFacing.DOWN) {
				return advancedRotationTexture.getBottom(isActive(blockState));
			}
			return advancedRotationTexture.getSide(isActive(blockState));
		}
		return "techreborn:blocks/machine/machine_side";
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		int facingInt = getSideFromEnum(state.getValue(FACING));
		int activeInt = state.getValue(ACTIVE) ? 0 : 4;
		return facingInt + activeInt;
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		boolean active = false;
		int facingInt = meta;
		if (facingInt > 4) {
			active = true;
			facingInt = facingInt - 4;
		}
		EnumFacing facing = getSideFromint(facingInt);
		return this.getDefaultState().withProperty(FACING, facing).withProperty(ACTIVE, active);
	}

	public boolean isActive(IBlockState state) {
		return state.getValue(ACTIVE);
	}

	public EnumFacing getFacing(IBlockState state) {
		return state.getValue(FACING);
	}

	public void setFacing(EnumFacing facing, World world, BlockPos pos) {
		if (hasCustomStaes) {
			return;
		}
		world.setBlockState(pos, world.getBlockState(pos).withProperty(FACING, facing));
	}

	public void setActive(Boolean active, World world, BlockPos pos) {
		if (hasCustomStaes) {
			return;
		}
		EnumFacing facing = world.getBlockState(pos).getValue(FACING);
		IBlockState state = world.getBlockState(pos).withProperty(ACTIVE, active).withProperty(FACING, facing);
		world.setBlockState(pos, state, 3);
	}

	@Override
	public int amountOfStates() {
		return 9; // 0-3 off nsew, 4-8 on nsew
	}

	public EnumFacing getSideFromint(int i) {
		if (i == 0) {
			return EnumFacing.NORTH;
		} else if (i == 1) {
			return EnumFacing.SOUTH;
		} else if (i == 2) {
			return EnumFacing.EAST;
		} else if (i == 3) {
			return EnumFacing.WEST;
		}
		return EnumFacing.NORTH;
	}

	public int getSideFromEnum(EnumFacing facing) {
		if (facing == EnumFacing.NORTH) {
			return 0;
		} else if (facing == EnumFacing.SOUTH) {
			return 1;
		} else if (facing == EnumFacing.EAST) {
			return 2;
		} else if (facing == EnumFacing.WEST) {
			return 3;
		}
		return 0;
	}
}

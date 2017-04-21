package reborncore.common.logic;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockDynamicLiquid;
import net.minecraft.block.BlockStaticLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import reborncore.api.tile.IUpgradeable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Gigabit101 on 08/04/2017.
 */
public class LogicBlock extends BlockContainer
{
    @Nonnull
    LogicController logicController;

    public LogicBlock(LogicController logicController)
    {
        super(Material.IRON);
        this.logicController = logicController;
        this.setUnlocalizedName(logicController.getName());
        this.logicController.initBlock(this);
        this.setHardness(logicController.getHardness());
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta)
    {
        if(logicController != null)
        {
            return logicController.createNewTileEntity(worldIn, meta);
        }
        return null;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        return logicController.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> tooltip, boolean advanced)
    {
        super.addInformation(stack, player, tooltip, advanced);
        logicController.addInformation(stack, player, tooltip, advanced);
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state)
    {
        if(logicController != null)
        {
            logicController.getRenderType(state);
        }
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos)
    {
        if(logicController != null)
        {
            return logicController.getBoundingBox(state, source, pos);
        }
        return super.getBoundingBox(state, source, pos);
    }

    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state)
    {
        logicController.onBlockAdded(worldIn, pos, state);
        super.onBlockAdded(worldIn, pos, state);
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
    {
        logicController.onBlockPlacedBy(worldIn, pos, state, placer, stack);
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state)
    {
        if(dropInv())
        {
            dropInventory(worldIn, pos);
        }
        super.breakBlock(worldIn, pos, state);
    }

    public boolean dropInv()
    {
        return logicController.dropInv();
    }

    protected void dropInventory(World world, BlockPos pos) {
        TileEntity tileEntity = world.getTileEntity(pos);

        if (tileEntity == null) {
            return;
        }
        if (!(tileEntity.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null))) {
            return;
        }

        ItemStackHandler inventory = (ItemStackHandler) tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);

        List<ItemStack> items = new ArrayList<ItemStack>();

        addItemsToList(inventory, items);

        for (ItemStack itemStack : items) {
            Random rand = new Random();

            float dX = rand.nextFloat() * 0.8F + 0.1F;
            float dY = rand.nextFloat() * 0.8F + 0.1F;
            float dZ = rand.nextFloat() * 0.8F + 0.1F;

            EntityItem entityItem = new EntityItem(world, pos.getX() + dX, pos.getY() + dY, pos.getZ() + dZ, itemStack.copy());

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

    private void addItemsToList(IItemHandler inventory, List<ItemStack> items){
        for (int i = 0; i < inventory.getSlots(); i++) {
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
}
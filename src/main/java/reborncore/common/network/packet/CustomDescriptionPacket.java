package reborncore.common.network.packet;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import reborncore.RebornCore;
import reborncore.common.network.ExtendedPacketBuffer;
import reborncore.common.network.INetworkPacket;
import reborncore.common.registration.impl.PacketRegistry;

import java.io.IOException;


@PacketRegistry(proccessingSide = Side.CLIENT)
public class CustomDescriptionPacket implements INetworkPacket<CustomDescriptionPacket> {

	private BlockPos blockPos;
	private NBTTagCompound nbt;

	public CustomDescriptionPacket(BlockPos blockPos, NBTTagCompound nbt) {
		this.blockPos = blockPos;
		this.nbt = nbt;
	}

	public CustomDescriptionPacket() {
	}

	public CustomDescriptionPacket(TileEntity tileEntity) {
		this.blockPos = tileEntity.getPos();
		this.nbt = tileEntity.writeToNBT(new NBTTagCompound());
	}

	@Override
	public void writeData(ExtendedPacketBuffer buffer) throws IOException {
		buffer.writeBlockPos(blockPos);
		buffer.writeCompoundTag(nbt);
	}

	@Override
	public void readData(ExtendedPacketBuffer buffer) throws IOException {
		blockPos = buffer.readBlockPos();
		nbt = buffer.readCompoundTag();
	}

	@Override
	public void processData(CustomDescriptionPacket message, MessageContext context) {
		if (message.blockPos == null || message.nbt == null) {
			return;
		}
		World world = RebornCore.proxy.getClientWorld();
		if (world.isBlockLoaded(message.blockPos)) {
			TileEntity tileentity = world.getTileEntity(message.blockPos);
			if (tileentity != null && message.nbt != null) {
				tileentity.readFromNBT(message.nbt);
			}
		}
	}
}

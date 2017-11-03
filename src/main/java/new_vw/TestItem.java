package new_vw;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TestItem extends Item {
	
	@Override
	public EnumActionResult onItemUse(EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if(!worldIn.isRemote) {
			Moving3DChunkEntity chunkEntity = new Moving3DChunkEntity(worldIn);
			chunkEntity.setPosition(pos.getX(), pos.getY(), pos.getZ());
			worldIn.spawnEntity(chunkEntity);
		}
		System.out.println("Hehehehehe");
		return EnumActionResult.SUCCESS;
	}
}

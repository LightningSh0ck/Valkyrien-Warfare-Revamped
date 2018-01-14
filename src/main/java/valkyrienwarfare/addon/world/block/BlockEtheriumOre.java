/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2015-2017 the Valkyrien Warfare team
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income unless it is to be used as a part of a larger project (IE: "modpacks"), nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from the Valkyrien Warfare team.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: The Valkyrien Warfare team), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package valkyrienwarfare.addon.world.block;

import valkyrienwarfare.addon.world.EntityFallingUpBlock;
import valkyrienwarfare.addon.world.ValkyrienWarfareWorld;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.material.Material;
//import net.minecraft.block.state.IBlockState;
import comp1_7_10.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class BlockEtheriumOre extends Block {

	public BlockEtheriumOre(Material materialIn) {
		super(materialIn);
	}

	// Ripped from BlockFalling class for consistancy with game mechanics
	public static boolean canFallThrough(IBlockState state) {
		Block block = state.getBlock();
		Material material = state.getMaterial();
		return block == Blocks.FIRE || material == Material.air || material == Material.water || material == Material.lava;
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List itemInformation, boolean par4) {
		itemInformation.add(TextFormatting.ITALIC + "" + TextFormatting.RED + TextFormatting.ITALIC + "Unfinished until v_0.91_alpha");
	}

	@Override
	public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
		worldIn.scheduleUpdate(pos, this, this.tickRate(worldIn));
	}

	@Override
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
		worldIn.scheduleUpdate(pos, this, this.tickRate(worldIn));
	}

	@Override
	public int tickRate(World worldIn) {
		return 2;
	}

	public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
		if (!worldIn.isRemote) {
			tryFallingUp(worldIn, pos);
		}
	}

	private void tryFallingUp(World worldIn, BlockPos pos) {
		BlockPos downPos = pos.up();
		if ((worldIn.isAirBlock(downPos) || canFallThrough(worldIn.getBlockState(downPos))) && pos.getY() >= 0) {
			int i = 32;

			if (!BlockFalling.fallInstantly && worldIn.isAreaLoaded(pos.add(-32, -32, -32), pos.add(32, 32, 32))) {
				if (!worldIn.isRemote) {
					// Start falling up
					EntityFallingUpBlock entityfallingblock = new EntityFallingUpBlock(worldIn, (double) pos.getX() + 0.5D, (double) pos.getY(), (double) pos.getZ() + 0.5D, worldIn.getBlockState(pos));
					worldIn.spawnEntity(entityfallingblock);
				}
			} else {
				IBlockState state = worldIn.getBlockState(pos);
				worldIn.setBlockToAir(pos);
				BlockPos blockpos;

				for (blockpos = pos.up(); (worldIn.isAirBlock(blockpos) || canFallThrough(worldIn.getBlockState(blockpos))) && blockpos.getY() < 255; blockpos = blockpos.up()) {
					;
				}

				if (blockpos.getY() < 255) {
					worldIn.setBlockState(blockpos.down(), state, 3);
				}
			}
		}
	}

	//Ore Properties Start Here
	@Nullable
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return ValkyrienWarfareWorld.INSTANCE.etheriumCrystal;
	}

	public int quantityDroppedWithBonus(int fortune, Random random) {
		return this.quantityDropped(random) + random.nextInt(fortune + 1);
	}

	/**
	 * Returns the quantity of items to drop on block destruction.
	 */
	public int quantityDropped(Random random) {
		return 4 + random.nextInt(4);
	}

	@Override
	public int getExpDrop(IBlockState state, net.minecraft.world.IBlockAccess world, BlockPos pos, int fortune) {
		if (this.getItemDropped(state, RANDOM, fortune) != Item.getItemFromBlock(this)) {
			return 16 + RANDOM.nextInt(10);
		}
		return 0;
	}


}

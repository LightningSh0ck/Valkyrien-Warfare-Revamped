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

package valkyrienwarfare.api.block.ethercompressor;

import valkyrienwarfare.api.IBlockForceProvider;
import valkyrienwarfare.api.Vector;
import valkyrienwarfare.physicsmanagement.PhysicsObject;
import valkyrienwarfare.physicsmanagement.PhysicsWrapperEntity;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
//import net.minecraft.block.state.IBlockState;
import comp1_7_10.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class BlockEtherCompressor extends Block implements ITileEntityProvider, IBlockForceProvider {

	public double enginePower = 25000D;

	public BlockEtherCompressor(Material materialIn, double enginePower) {
		super(materialIn);
		this.enginePower = enginePower;
	}

	@Override
	public Vector getBlockForceInShipSpace(World world, BlockPos pos, IBlockState state, Entity shipEntity, double secondsToApply) {
		PhysicsWrapperEntity wrapper = (PhysicsWrapperEntity) shipEntity;
		PhysicsObject obj = wrapper.wrapping;
		IBlockState controllerState = obj.VKChunkCache.getBlockState(pos);
		TileEntity worldTile = obj.VKChunkCache.getTileEntity(pos);
		if (worldTile == null) {
			return null;
		}
		if (worldTile instanceof TileEntityEtherCompressor) {
			TileEntityEtherCompressor engineTile = (TileEntityEtherCompressor) worldTile;
			return engineTile.getForceOutputUnoriented(secondsToApply);
		}
		return null;
	}

	@Override
	public boolean shouldLocalForceBeRotated(World world, BlockPos pos, IBlockState state, double secondsToApply) {
		return false;
	}

}

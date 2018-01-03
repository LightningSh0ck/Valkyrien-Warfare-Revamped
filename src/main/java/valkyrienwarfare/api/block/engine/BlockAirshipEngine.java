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

package valkyrienwarfare.api.block.engine;

import valkyrienwarfare.api.IBlockForceProvider;
import valkyrienwarfare.api.Vector;
import valkyrienwarfare.addon.control.tileentity.TileEntityPropellerEngine;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
//import net.minecraft.block.state.IBlockState;
import comp1_7_10.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 * All engines should extend this class, that way other kinds of engines can be made without making tons of new classes for them. Only engines that add new functionality should have their own class.
 */
public abstract class BlockAirshipEngine extends Block implements IBlockForceProvider, ITileEntityProvider {

	public static final PropertyDirection FACING = PropertyDirection.create("facing");
	public double enginePower = 4000D;

	public BlockAirshipEngine(Material materialIn, double enginePower) {
		super(materialIn);
		this.enginePower = enginePower;
	}

	@Override
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		return this.getDefaultState().withProperty(FACING, EnumFacing.getDirectionFromEntityLiving(pos, placer));
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, FACING);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(FACING, EnumFacing.getFront(meta));
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		int i = state.getValue(FACING).getIndex();
		return i;
	}

	@Override
	public Vector getBlockForceInShipSpace(World world, BlockPos pos, IBlockState state, Entity shipEntity, double secondsToApply) {
		EnumFacing enumfacing = state.getValue(FACING);
		Vector acting = new Vector(0, 0, 0);
		if (!world.isBlockPowered(pos)) {
			return acting;
		}

		TileEntity tileEntity = world.getTileEntity(pos);
		if (tileEntity instanceof TileEntityPropellerEngine) {
			//Just set the Thrust to be the maximum
			((TileEntityPropellerEngine) tileEntity).setThrust(this.getEnginePower(world, pos, state, shipEntity));
			((TileEntityPropellerEngine) tileEntity).updateTicksSinceLastRecievedSignal();
			return ((TileEntityPropellerEngine) tileEntity).getForceOutputUnoriented(secondsToApply);
		}

		return acting;
		//Being moved into the new Nodes control system
	    /*double power = this.getEnginePower(world, pos, state, shipEntity) * secondsToApply;
        switch (enumfacing) {
            case DOWN:
                acting = new Vector(0, power, 0);
                break;
            case UP:
                acting = new Vector(0, -power, 0);
                break;
            case EAST:
                acting = new Vector(-power, 0, 0);
                break;
            case NORTH:
                acting = new Vector(0, 0, power);
                break;
            case WEST:
                acting = new Vector(power, 0, 0);
                break;
            case SOUTH:
                acting = new Vector(0, 0, -power);
                break;
        }
        return acting;*/
	}

	@Override
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.CUTOUT;
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing face) {
		return true;
	}

	/**
	 * Used for calculating force applied to the airship by an engine. Override this in your subclasses to make engines that are more dynamic than simply being faster engines.
	 *
	 * @return
	 */
	public double getEnginePower(World world, BlockPos pos, IBlockState state, Entity shipEntity) {
		return this.enginePower;
	}

	public TileEntity createNewTileEntity(World worldIn, int meta) {
		Vector normalVector = new Vector(1, 0, 0);
		IBlockState state = getStateFromMeta(meta);
		EnumFacing facing = state.getValue(FACING);
		switch (facing) {
			case DOWN:
				normalVector = new Vector(0, 1, 0);
				break;
			case UP:
				normalVector = new Vector(0, -1, 0);
				break;
			case EAST:
				normalVector = new Vector(-1, 0, 0);
				break;
			case NORTH:
				normalVector = new Vector(0, 0, 1);
				break;
			case WEST:
				normalVector = new Vector(1, 0, 0);
				break;
			case SOUTH:
				normalVector = new Vector(0, 0, -1);
		}

		return new TileEntityPropellerEngine(normalVector, true, enginePower);
	}

}

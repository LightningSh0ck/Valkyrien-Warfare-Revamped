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

package valkyrienwarfare.physics;

import valkyrienwarfare.api.IBlockMassProvider;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
//import net.minecraft.block.state.IBlockState;
import comp1_7_10.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashMap;

public class BlockMass {

	// 80N, Something like ~ 20lbs
	private final static double defaultMass = 50D;
	public static BlockMass basicMass = new BlockMass();
	public HashMap<Block, Double> blockToMass = new HashMap<Block, Double>();
	public HashMap<Material, Double> materialMass = new HashMap<Material, Double>();

	public BlockMass() {
		generateMaterialMasses();
	}

	public static void registerBlockMass(Block block, double mass) {
		basicMass.blockToMass.put(block, mass);
	}

	private void generateMaterialMasses() {
		materialMass.put(Material.air, 0D);
		materialMass.put(Material.anvil, 200D);
		//materialMass.put(Material.BARRIER, 0D);
		materialMass.put(Material.cactus, 15D);
		materialMass.put(Material.cake, 10D);
		materialMass.put(Material.carpet, 5D);
		materialMass.put(Material.circuits, 15D);
		materialMass.put(Material.clay, 40D);
		materialMass.put(Material.cloth, 20D);
		materialMass.put(Material.coral, 70D);
		materialMass.put(Material.craftedSnow, 20D);
		materialMass.put(Material.dragonEgg, 20D);
		materialMass.put(Material.fire, 0D);
		materialMass.put(Material.glass, 20D);
		materialMass.put(Material.gourd, 50D);
		materialMass.put(Material.grass, 30D);
		materialMass.put(Material.ground, 70D);
		materialMass.put(Material.ice, 50D);
		materialMass.put(Material.iron, 250D);
		materialMass.put(Material.lava, 0D);
		materialMass.put(Material.leaves, 10D);
		materialMass.put(Material.packedIce, 40D);
		materialMass.put(Material.piston, 15D);
		materialMass.put(Material.plants, 10D);
		materialMass.put(Material.portal, 0D);
		materialMass.put(Material.redstoneLight, 10D);
		materialMass.put(Material.rock, 220D);
		materialMass.put(Material.sand, 45D);
		materialMass.put(Material.snow, 20D);
		materialMass.put(Material.sponge, 20D);
		//materialMass.put(Material.STRUCTURE_VOID, 0D);
		materialMass.put(Material.tnt, 30D);
		materialMass.put(Material.vine, 5D);
		materialMass.put(Material.water, 0D);
		materialMass.put(Material.web, 10D);
		materialMass.put(Material.wood, 25D);
	}

	public double getMassFromState(IBlockState state, BlockPos pos, World world) {
		Block block = state.getBlock();
		if (block instanceof IBlockMassProvider) {
			return ((IBlockMassProvider) block).getBlockMass(world, pos, state);
		} else {
			Double fromMap = blockToMass.get(block);
			if (fromMap != null) {
				return fromMap.doubleValue();
			} else {
				Double newMass = generateMassForBlock(block);
				blockToMass.put(block, newMass);
				return newMass;
			}
		}
	}

	public double getMassFromMaterial(Material material) {
		Double mass = materialMass.get(material);
		if (mass == null) {
			mass = defaultMass;
			materialMass.put(material, mass);
		}
		return mass;
	}

	public Double generateMassForBlock(Block block) {
		if (block instanceof BlockLiquid) {
			return 0D;
		}
		Material material = block.blockMaterial;

		return getMassFromMaterial(material);
		//Old formula
//		double hardness = block.blockHardness;
//		double resistance = block.blockResistance;
//		return hardness * 50D + 2 * math.pow(resistance, 1 / 4);
	}

}

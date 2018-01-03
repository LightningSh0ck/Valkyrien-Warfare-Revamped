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

package valkyrienwarfare.addon.control.renderer;

import valkyrienwarfare.api.block.engine.BlockAirshipEngine;
import valkyrienwarfare.render.FastBlockModelRenderer;
import valkyrienwarfare.addon.control.tileentity.TileEntityPropellerEngine;
import valkyrienwarfare.addon.control.ValkyrienWarfareControl;
//import net.minecraft.block.state.IBlockState;
import comp1_7_10.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;
import org.lwjgl.opengl.GL11;

public class PropellerEngineTileEntityRenderer extends TileEntitySpecialRenderer<TileEntityPropellerEngine> {

	@Override
	public void renderTileEntityAt(TileEntityPropellerEngine tileentity, double x, double y, double z, float partialTick, int destroyStage) {

		IBlockState state = tileentity.getWorld().getBlockState(tileentity.getPos());
		if (state.getBlock() instanceof BlockAirshipEngine) {
			EnumFacing facing = state.getValue(BlockAirshipEngine.FACING);

			IBlockState engineRenderState = getRenderState(state);
			IBlockState propellerRenderState = ValkyrienWarfareControl.INSTANCE.shipWheel.getStateFromMeta(14);

			this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			GlStateManager.pushMatrix();
			GlStateManager.disableLighting();
			Tessellator tessellator = Tessellator.getInstance();
			VertexBuffer vertexbuffer = tessellator.getBuffer();

			double oldX = vertexbuffer.xOffset;
			double oldY = vertexbuffer.yOffset;
			double oldZ = vertexbuffer.zOffset;

			vertexbuffer.setTranslation(0, 0, 0);
			GL11.glTranslated(x, y, z);
			GlStateManager.disableAlpha();
			GlStateManager.disableBlend();

			int brightness = tileentity.getWorld().getCombinedLight(tileentity.getPos(), 0);

//	        GL11.glScaled(1.2D, 1.2D, 1.2D);

			GL11.glTranslated(0.5D, 0.5D, 0.5D);

			switch (facing) {
				case UP:
					GL11.glRotated(-90, 1, 0, 0);
					break;
				case DOWN:
					GL11.glRotated(90, 1, 0, 0);
					break;
				case NORTH:
					GL11.glRotated(180, 0, 1, 0);
					break;
				case EAST:
					GL11.glRotated(90, 0, 1, 0);
					break;
				case SOUTH:
					GL11.glRotated(0, 0, 1, 0);
					break;
				case WEST:
					GL11.glRotated(270, 0, 1, 0);
					break;

			}

			GL11.glTranslated(-0.5D, -0.5D, -0.5D);

			FastBlockModelRenderer.renderBlockModel(vertexbuffer, tessellator, tileentity.getWorld(), engineRenderState, brightness);

			GL11.glPushMatrix();

			GL11.glTranslated(0.5D, 0.21D, 0.5D);
			GL11.glRotated(Math.random() * 360D, 0, 0, 1);
			GL11.glScaled(1.5D, 1.5D, 1);
			GL11.glTranslated(-0.5D, -0.21D, -0.5D);


			FastBlockModelRenderer.renderBlockModel(vertexbuffer, tessellator, tileentity.getWorld(), propellerRenderState, brightness);

			GL11.glPopMatrix();

			GL11.glPopMatrix();

			vertexbuffer.setTranslation(oldX, oldY, oldZ);
		}
	}

	private IBlockState getRenderState(IBlockState inWorldState) {
		if (inWorldState.getBlock() == ValkyrienWarfareControl.INSTANCE.ultimateEngine) {
			return ValkyrienWarfareControl.INSTANCE.shipWheel.getStateFromMeta(9);
		}
		if (inWorldState.getBlock() == ValkyrienWarfareControl.INSTANCE.redstoneEngine) {
			return ValkyrienWarfareControl.INSTANCE.shipWheel.getStateFromMeta(10);
		}
		if (inWorldState.getBlock() == ValkyrienWarfareControl.INSTANCE.eliteEngine) {
			return ValkyrienWarfareControl.INSTANCE.shipWheel.getStateFromMeta(11);
		}
		if (inWorldState.getBlock() == ValkyrienWarfareControl.INSTANCE.basicEngine) {
			return ValkyrienWarfareControl.INSTANCE.shipWheel.getStateFromMeta(12);
		}
		if (inWorldState.getBlock() == ValkyrienWarfareControl.INSTANCE.advancedEngine) {
			return ValkyrienWarfareControl.INSTANCE.shipWheel.getStateFromMeta(13);
		}

		return ValkyrienWarfareControl.INSTANCE.shipWheel.getStateFromMeta(9);
	}
}

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

import valkyrienwarfare.addon.control.block.BlockShipTelegraph;
import valkyrienwarfare.addon.control.tileentity.TileEntityShipTelegraph;
import valkyrienwarfare.addon.control.ValkyrienWarfareControl;
import valkyrienwarfare.render.FastBlockModelRenderer;
//import net.minecraft.block.state.IBlockState;
import comp1_7_10.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;

public class ShipTelegraphTileEntityRenderer extends TileEntitySpecialRenderer<TileEntityShipTelegraph> {

	private final Class renderedTileEntityClass;

	public ShipTelegraphTileEntityRenderer(Class toRender) {
		renderedTileEntityClass = toRender;
	}

	@Override
	public void renderTileEntityAt(TileEntityShipTelegraph tileentity, double x, double y, double z, float partialTick, int destroyStage) {
		IBlockState telegraphState = tileentity.getWorld().getBlockState(tileentity.getPos());

		if (telegraphState.getBlock() != ValkyrienWarfareControl.INSTANCE.shipTelegraph) {
			return;
		}

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

		BlockPos originPos = tileentity.getPos();

		IBlockState glassState = ValkyrienWarfareControl.INSTANCE.shipWheel.getStateFromMeta(8);
		IBlockState dialState = ValkyrienWarfareControl.INSTANCE.shipWheel.getStateFromMeta(7);
		IBlockState leftHandleState = ValkyrienWarfareControl.INSTANCE.shipWheel.getStateFromMeta(6);
		IBlockState rightHandleState = ValkyrienWarfareControl.INSTANCE.shipWheel.getStateFromMeta(5);
		IBlockState helmStateToRender = ValkyrienWarfareControl.INSTANCE.shipWheel.getStateFromMeta(4);
		int brightness = tileentity.getWorld().getCombinedLight(tileentity.getPos(), 0);

		double multiplier = 1.5D;

		GL11.glTranslated((1D - multiplier) / 2.0D, 0, (1D - multiplier) / 2.0D);
		GL11.glScaled(multiplier, multiplier, multiplier);
		EnumFacing enumfacing = telegraphState.getValue(BlockShipTelegraph.FACING);
		double wheelAndCompassStateRotation = enumfacing.getHorizontalAngle();

		GL11.glTranslated(0.5D, 0, 0.5D);
		GL11.glRotated(wheelAndCompassStateRotation, 0, 1, 0);
		GL11.glTranslated(-0.5D, 0, -0.5D);

		FastBlockModelRenderer.renderBlockModel(vertexbuffer, tessellator, tileentity.getWorld(), helmStateToRender, brightness);

		FastBlockModelRenderer.renderBlockModel(vertexbuffer, tessellator, tileentity.getWorld(), dialState, brightness);

		GL11.glPushMatrix();

		GL11.glTranslated(0.497D, 0.857D, 0.5D);
		GL11.glRotated(tileentity.getHandleRenderRotation(), 0D, 0D, 1D);
		GL11.glTranslated(-0.497D, -0.857D, -0.5D);

		FastBlockModelRenderer.renderBlockModel(vertexbuffer, tessellator, tileentity.getWorld(), rightHandleState, brightness);
		FastBlockModelRenderer.renderBlockModel(vertexbuffer, tessellator, tileentity.getWorld(), leftHandleState, brightness);

		GL11.glPopMatrix();

		GlStateManager.enableAlpha();
		GlStateManager.enableBlend();
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		FastBlockModelRenderer.renderBlockModel(vertexbuffer, tessellator, tileentity.getWorld(), glassState, brightness);
		GlStateManager.disableAlpha();
		GlStateManager.disableBlend();

		GL11.glPopMatrix();
		vertexbuffer.setTranslation(oldX, oldY, oldZ);

		GlStateManager.enableLighting();
		GlStateManager.resetColor();
	}

}

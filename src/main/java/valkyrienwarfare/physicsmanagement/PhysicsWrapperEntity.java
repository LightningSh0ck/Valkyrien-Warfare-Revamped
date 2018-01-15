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

package valkyrienwarfare.physicsmanagement;

import valkyrienwarfare.api.Vector;
import valkyrienwarfare.capability.IAirshipCounterCapability;
import valkyrienwarfare.collision.Polygon;
import valkyrienwarfare.interaction.ShipNameUUIDData;
import valkyrienwarfare.schematics.SchematicReader.Schematic;
import valkyrienwarfare.ValkyrienWarfareMod;
import valkyrienwarfare.addon.combat.entity.EntityMountingWeaponBase;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

/**
 * This entity's only purpose is to use the functionality of sending itself to nearby players, all other operations are handled by the PhysicsObject class
 *
 * @author thebest108
 */
public class PhysicsWrapperEntity extends Entity implements IEntityAdditionalSpawnData {
	
	public static final DataParameter<Boolean> IS_NAME_CUSTOM = EntityDataManager.<Boolean>createKey(Entity.class, DataSerializers.BOOLEAN);
	private static final AxisAlignedBB zeroBB = new AxisAlignedBB(0, 0, 0, 0, 0, 0);
	public PhysicsObject wrapping;
	public double pitch;
	public double yaw;
	public double roll;
	public double prevPitch;
	public double prevYaw;
	public double prevRoll;
	
	public PhysicsWrapperEntity(World worldIn) {
		super(worldIn);
		wrapping = new PhysicsObject(this);
		dataManager.register(IS_NAME_CUSTOM, Boolean.valueOf(false));
	}
	
	public PhysicsWrapperEntity(World worldIn, double x, double y, double z, @Nullable EntityPlayer creator, int detectorID, ShipType shipType) {
		this(worldIn);
		posX = x;
		posY = y;
		posZ = z;
		wrapping.creator = creator.entityUniqueID.toString();
		wrapping.detectorID = detectorID;
		wrapping.shipType = shipType;
		wrapping.processChunkClaims(creator);
		
		IAirshipCounterCapability counter = creator.getCapability(ValkyrienWarfareMod.airshipCounter, null);
		counter.onCreate();
		
		setCustomNameTagInitial(creator.getName() + ":" + counter.getAirshipCountEver());
		ShipNameUUIDData.get(worldIn).placeShipInRegistry(this, getCustomNameTag());
	}
	
	public PhysicsWrapperEntity(World worldIn, double x, double y, double z, ShipType type, Schematic schematic) {
		super(worldIn);
		posX = x;
		posY = y;
		posZ = z;
		
		wrapping = new PhysicsObject(this);
		
		wrapping.creator = "god";
		wrapping.detectorID = 0;
		wrapping.shipType = type;
		
		wrapping.processChunkClaims(schematic);
		
		setCustomNameTagInitial("Obummer" + ":" + Math.random() * 10000000);
		ShipNameUUIDData.get(worldIn).placeShipInRegistry(this, getCustomNameTag());
	}
	
	@Override
	public void onUpdate() {
		if (isDead) {
			return;
		}
		if (world.isRemote) {
			wrapping.isNameCustom = dataManager.get(IS_NAME_CUSTOM);
		}
		// super.onUpdate();
		wrapping.onTick();
		
		firstUpdate = false;
	}
	
	@Override
	public void updatePassenger(Entity passenger) {
		Vector inLocal = wrapping.getLocalPositionForEntity(passenger);
		
		if (inLocal != null) {
			Vector newEntityPosition = new Vector(inLocal);
			float f = passenger.width / 2.0F;
			float f1 = passenger.height;
			AxisAlignedBB inLocalAABB = new AxisAlignedBB(newEntityPosition.X - (double) f, newEntityPosition.Y, newEntityPosition.Z - (double) f, newEntityPosition.X + (double) f, newEntityPosition.Y + (double) f1, newEntityPosition.Z + (double) f);
			wrapping.coordTransform.fromLocalToGlobal(newEntityPosition);
			passenger.setPosition(newEntityPosition.X, newEntityPosition.Y, newEntityPosition.Z);
			Polygon entityBBPoly = new Polygon(inLocalAABB, wrapping.coordTransform.lToWTransform);
			
			AxisAlignedBB newEntityBB = entityBBPoly.getEnclosedAABB();
			passenger.setEntityBoundingBox(newEntityBB);
			if (passenger instanceof EntityMountingWeaponBase) {
				passenger.onUpdate();
				
				for (Entity e : passenger.riddenByEntities) {
					if (wrapping.isEntityFixed(e)) {
						Vector inLocalAgain = wrapping.getLocalPositionForEntity(e);
						if (inLocalAgain != null) {
							Vector newEntityPositionAgain = new Vector(inLocalAgain);
							wrapping.coordTransform.fromLocalToGlobal(newEntityPositionAgain);
							
							e.setPosition(newEntityPositionAgain.X, newEntityPositionAgain.Y, newEntityPositionAgain.Z);
						}
					}
				}
			}
		}
	}
	
	@Override
	protected void addPassenger(Entity passenger) {
		// System.out.println("entity just mounted");
		super.addPassenger(passenger);
	}
	
	@Override
	protected void removePassenger(Entity toRemove) {
		// System.out.println("entity just dismounted");
		super.removePassenger(toRemove);
	}
	
	@Override
	protected void entityInit() {
		
	}
	
	@Override
	public AxisAlignedBB getEntityBoundingBox() {
		return wrapping.collisionBB;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getRenderBoundingBox() {
		return wrapping.collisionBB;
	}
	
	@Override
	public void setPosition(double x, double y, double z) {
	}
	
	@Override
	public void setLocationAndAngles(double x, double y, double z, float yaw, float pitch) {
	}
	
	@Override
	protected boolean canFitPassenger(Entity passenger) {
		return true;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void setPositionAndRotationDirect(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean teleport) {
	}
	
	@SideOnly(Side.CLIENT)
	public boolean getAlwaysRenderNameTagForRender() {
		return wrapping.isNameCustom;
	}
	
	public void setCustomNameTagInitial(String name) {
		super.setCustomNameTag(name);
	}
	
	@Override
	public void setCustomNameTag(String name) {
		if (!world.isRemote) {
			if (getCustomNameTag() != null && !getCustomNameTag().equals("")) {
				//Update the name registry
				boolean didRenameSuccessful = ShipNameUUIDData.get(world).renameShipInRegsitry(this, name, getCustomNameTag());
				if (didRenameSuccessful) {
					super.setCustomNameTag(name);
					wrapping.isNameCustom = true;
					dataManager.set(IS_NAME_CUSTOM, true);
				}
			} else {
				super.setCustomNameTag(name);
			}
		} else {
			super.setCustomNameTag(name);
		}
	}
	
	@Override
	public void setPositionAndUpdate(double x, double y, double z) {
	}
	
	@Override
	protected void readEntityFromNBT(NBTTagCompound tagCompound) {
		wrapping.readFromNBTTag(tagCompound);
	}
	
	@Override
	protected void writeEntityToNBT(NBTTagCompound tagCompound) {
		wrapping.writeToNBTTag(tagCompound);
	}
	
	@Override
	public void writeSpawnData(ByteBuf buffer) {
		wrapping.preloadNewPlayers();
		wrapping.writeSpawnData(buffer);
	}
	
	@Override
	public void readSpawnData(ByteBuf additionalData) {
		wrapping.readSpawnData(additionalData);
	}
}

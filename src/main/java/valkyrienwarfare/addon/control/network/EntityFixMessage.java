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

package valkyrienwarfare.addon.control.network;

import valkyrienwarfare.api.Vector;
import valkyrienwarfare.physicsmanagement.PhysicsWrapperEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import cpw.mods.fml.common.network.simpleimpl.IMessage;

public class EntityFixMessage implements IMessage {

	public int shipId, entityUUID;
	// If true, then entity is mounting; if false entity is dismounting
	public boolean isFixing;
	public Vector localPosition;

	public EntityFixMessage() {
	}

	public EntityFixMessage(PhysicsWrapperEntity toFixOn, Entity toFix, boolean isFixing, Vector localPos) {
		shipId = toFixOn.getEntityId();
		entityUUID = toFix.getPersistentID().hashCode();
		this.isFixing = isFixing;
		localPosition = localPos;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		shipId = buf.readInt();
		entityUUID = buf.readInt();
		isFixing = buf.readBoolean();
		if (isFixing) {
			localPosition = new Vector(buf);
		}
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(shipId);
		buf.writeInt(entityUUID);
		buf.writeBoolean(isFixing);
		if (isFixing) {
			localPosition.writeToByteBuf(buf);
		}
	}

}

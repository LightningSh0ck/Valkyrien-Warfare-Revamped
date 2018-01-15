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

package valkyrienwarfare.network;

import valkyrienwarfare.api.Vector;
import valkyrienwarfare.physicsmanagement.PhysicsWrapperEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.IThreadListener;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class EntityRelativePositionMessageHandler implements IMessageHandler<EntityRelativePositionMessage, IMessage> {
	
	@Override
	public IMessage onMessage(EntityRelativePositionMessage message, MessageContext ctx) {
		if (Minecraft.getMinecraft().player == null) {
			return null;
		}
		
		IThreadListener mainThread = Minecraft.getMinecraft();
		mainThread.addScheduledTask(new Runnable() {
			@Override
			public void run() {
				Entity ent = Minecraft.getMinecraft().world.getEntityByID(message.wrapperEntityId);
				if (ent != null && ent instanceof PhysicsWrapperEntity) {
					PhysicsWrapperEntity wrapper = (PhysicsWrapperEntity) ent;
					double[] lToWTransform = wrapper.wrapping.coordTransform.lToWTransform;
					
					for (int i = 0; i < message.listSize; i++) {
						int entityID = message.entitiesToSendIDs.get(i);
						Vector entityPosition = message.entitiesRelativePosition.get(i);
						
						Entity entity = Minecraft.getMinecraft().world.getEntityByID(entityID);
						
						if (entity != null && entity != Minecraft.getMinecraft().player) {
//                    		System.out.println("worked");
							entityPosition.transform(lToWTransform);

//                    		entity.setPosition(entityPosition.X, entityPosition.Y, entityPosition.Z);
						}
					}
				}
			}
		});
		return null;
	}
	
}

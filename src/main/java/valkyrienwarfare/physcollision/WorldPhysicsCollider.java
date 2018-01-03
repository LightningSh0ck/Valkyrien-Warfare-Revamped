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

package valkyrienwarfare.physcollision;

import valkyrienwarfare.api.RotationMatrices;
import valkyrienwarfare.api.Vector;
import valkyrienwarfare.collision.PhysCollisionObject;
import valkyrienwarfare.collision.PhysPolygonCollider;
import valkyrienwarfare.collision.Polygon;
import valkyrienwarfare.optimization.CollisionInformationHolder;
import valkyrienwarfare.optimization.ShipCollisionTask;
import valkyrienwarfare.physcollision.BlockRammingManager.NestedBoolean;
import valkyrienwarfare.physics.PhysicsCalculations;
import valkyrienwarfare.physicsmanagement.PhysicsObject;
import valkyrienwarfare.relocation.SpatialDetector;
import valkyrienwarfare.ValkyrienWarfareMod;
import com.jackredcreeper.cannon.world.NewExp2;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.array.TIntArrayList;
//import net.minecraft.block.state.IBlockState;
import comp1_7_10.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

import java.util.ArrayList;
import java.util.Random;

public class WorldPhysicsCollider {

	private static final double expansion = 2D;
	public static double axisTolerance = .3D;
	private final MutableBlockPos mutablePos = new MutableBlockPos();
	private final Random rand = new Random();
	private final ArrayList<ShipCollisionTask> tasks = new ArrayList<ShipCollisionTask>();
	public PhysicsCalculations calculator;
	public World worldObj;
	public PhysicsObject parent;
	public double collisionCacheTickUpdateFrequency = 1D;
	public double collisionElasticity = .52D;
	public TIntArrayList cachedPotentialHits;
	public BlockPos centerPotentialHit;
	private TIntArrayList cachedHitsToRemove = new TIntArrayList();
	// Ensures this always updates the first tick after creation
	private double ticksSinceCacheUpdate = 420;
	private boolean updateCollisionTasksCache = true;

	public WorldPhysicsCollider(PhysicsCalculations calculations) {
		calculator = calculations;
		parent = calculations.parent;
		worldObj = parent.worldObj;
	}

	//Runs the collision code
	public void runPhysCollision() {
		tickUpdatingTheCollisionCache();

		processPotentialCollisionsAccurately();
	}

	public void tickUpdatingTheCollisionCache() {
		// Multiply by 20 to convert seconds (physTickSpeed) into ticks (ticksSinceCacheUpdate)
		ticksSinceCacheUpdate += 20D * calculator.physTickSpeed;

		TIntIterator iterator = cachedHitsToRemove.iterator();
		while (iterator.hasNext()) {
			cachedPotentialHits.remove(iterator.next());
		}
		cachedHitsToRemove.clear();
		if (shouldUpdateCollisonCache()) {
			updatePotentialCollisionCache();

			updateCollisionTasksCache = true;
			// Collections.shuffle(cachedPotentialHits);
		}
	}

	public void splitIntoCollisionTasks(ArrayList<ShipCollisionTask> toAdd) {
		if (updateCollisionTasksCache) {
			tasks.clear();
			int index = 0;
			int size = cachedPotentialHits.size();

			while (index < size) {
				ShipCollisionTask task = new ShipCollisionTask(this, index);
				index += ShipCollisionTask.maxTasksToCheck;
				tasks.add(task);
			}
			updateCollisionTasksCache = false;
		}

		toAdd.addAll(tasks);
	}

	public void processCollisionTask(ShipCollisionTask task) {
		MutableBlockPos inWorldPos = new MutableBlockPos();
		MutableBlockPos inLocalPos = new MutableBlockPos();

		for (CollisionInformationHolder info : task.collisionInformationGenerated) {
			inWorldPos.setPos(info.inWorldX, info.inWorldY, info.inWorldZ);
			inLocalPos.setPos(info.inLocalX, info.inLocalY, info.inLocalZ);
			handleActualCollision(info.collider, inWorldPos, inLocalPos, info.inWorldState, info.inLocalState);
		}

		task.collisionInformationGenerated.clear();
	}

	// Runs through the cache ArrayList, checking each possible BlockPos for SOLID blocks that can collide, if it finds any it will
	// move to the next method


	//TODO: Optimize from here, this is taking 10x the processing time of updating collision cache!
	private void processPotentialCollisionsAccurately() {
		final MutableBlockPos localCollisionPos = new MutableBlockPos();
		final Vector inWorld = new Vector();

		int minX, minY, minZ, maxX, maxY, maxZ, x, y, z;

		final double rangeCheck = .65D;

		TIntIterator intIterator = cachedPotentialHits.iterator();

		while (intIterator.hasNext()) {
			// Converts the int to a mutablePos
			SpatialDetector.setPosWithRespectTo(intIterator.next(), centerPotentialHit, mutablePos);

			inWorld.X = mutablePos.getX() + .5;
			inWorld.Y = mutablePos.getY() + .5;
			inWorld.Z = mutablePos.getZ() + .5;
			parent.coordTransform.fromGlobalToLocal(inWorld);

			minX = MathHelper.floor(inWorld.X - rangeCheck);
			minY = MathHelper.floor(inWorld.Y - rangeCheck);
			minZ = MathHelper.floor(inWorld.Z - rangeCheck);

			maxX = MathHelper.floor(inWorld.X + rangeCheck);
			maxY = MathHelper.floor(inWorld.Y + rangeCheck);
			maxZ = MathHelper.floor(inWorld.Z + rangeCheck);

			/**
			 * Something here is causing the game to freeze :/
			 */

			int minChunkX = minX >> 4;
			int minChunkY = minY >> 4;
			int minChunkZ = minZ >> 4;

			int maxChunkX = maxX >> 4;
			int maxChunkY = maxY >> 4;
			int maxChunkZ = maxZ >> 4;

			entireLoop:
			if (!(minChunkY > 15 || maxChunkY < 0)) {
				for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
					for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
						if (parent.ownsChunk(chunkX, chunkZ)) {
							final Chunk chunkIn = parent.VKChunkCache.getChunkAt(chunkX, chunkZ);

							int minXToCheck = chunkX << 4;
							int maxXToCheck = minXToCheck + 15;

							int minZToCheck = chunkZ << 4;
							int maxZToCheck = minZToCheck + 15;

							minXToCheck = Math.max(minXToCheck, minX);
							maxXToCheck = Math.min(maxXToCheck, maxX);

							minZToCheck = Math.max(minZToCheck, minZ);
							maxZToCheck = Math.min(maxZToCheck, maxZ);

							for (int chunkY = minChunkY; chunkY <= maxChunkY; chunkY++) {
								ExtendedBlockStorage storage = chunkIn.storageArrays[chunkY];
								if (storage != null) {
									int minYToCheck = chunkY << 4;
									int maxYToCheck = minYToCheck + 15;

									minYToCheck = Math.max(minYToCheck, minY);
									maxYToCheck = Math.min(maxYToCheck, maxY);

									for (x = minXToCheck; x <= maxXToCheck; x++) {
										for (z = minZToCheck; z <= maxZToCheck; z++) {
											for (y = minYToCheck; y <= maxYToCheck; y++) {
												final IBlockState state = storage.get(x & 15, y & 15, z & 15);
												if (state.getMaterial().isSolid()) {


													//Inject the multithreaded code here


													localCollisionPos.setPos(x, y, z);

													boolean brokeAWorldBlock = handleLikelyCollision(mutablePos, localCollisionPos, parent.surroundingWorldChunksCache.getBlockState(mutablePos), state);

													if (brokeAWorldBlock) {
														int positionRemoved = SpatialDetector.getHashWithRespectTo(mutablePos.getX(), mutablePos.getY(), mutablePos.getZ(), centerPotentialHit);
														cachedHitsToRemove.add(positionRemoved);
														break entireLoop;
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}

			//The old way of doing things

			/*for (x = minX; x <= maxX; x++) {
	            for (z = minZ; z <= maxZ; z++) {
					if (parent.ownsChunk(x >> 4, z >> 4)) {
						for (y = minY; y <= maxY; y++) {
							final Chunk chunkIn = parent.VKChunkCache.getChunkAt(x >> 4, z >> 4);
							final IBlockState state = chunkIn.getBlockState(x, y, z);
							if (state.getMaterial().isSolid()) {
								localCollisionPos.setPos(x, y, z);

								handleLikelyCollision(mutablePos, localCollisionPos, parent.surroundingWorldChunksCache.getBlockState(mutablePos), state);
							}
						}
					}
				}
			}*/
		}

	}

	//Tests two block positions directly against each other, and figures out whether a collision is occuring or not
	private boolean handleLikelyCollision(BlockPos inWorldPos, BlockPos inLocalPos, IBlockState inWorldState, IBlockState inLocalState) {
		// System.out.println("Handling a likely collision");
		AxisAlignedBB inLocalBB = new AxisAlignedBB(inLocalPos.getX(), inLocalPos.getY(), inLocalPos.getZ(), inLocalPos.getX() + 1, inLocalPos.getY() + 1, inLocalPos.getZ() + 1);
		AxisAlignedBB inGlobalBB = new AxisAlignedBB(inWorldPos.getX(), inWorldPos.getY(), inWorldPos.getZ(), inWorldPos.getX() + 1, inWorldPos.getY() + 1, inWorldPos.getZ() + 1);

		//This changes the box bounding box to the real bounding box, not sure if this is better or worse for this mod
//		List<AxisAlignedBB> colBB = worldObj.getCollisionBoxes(inLocalBB);
//		inLocalBB = colBB.get(0);

		Polygon shipInWorld = new Polygon(inLocalBB, parent.coordTransform.lToWTransform);
		Polygon worldPoly = new Polygon(inGlobalBB);

		PhysPolygonCollider collider = new PhysPolygonCollider(shipInWorld, worldPoly, parent.coordTransform.normals);

		if (!collider.seperated) {
			return handleActualCollision(collider, inWorldPos, inLocalPos, inWorldState, inLocalState);
		}

		return false;
	}

	//Takes the collision data along all axes generated prior, and creates the ideal value that is to be followed
	private boolean handleActualCollision(PhysPolygonCollider collider, BlockPos inWorldPos, BlockPos inLocalPos, IBlockState inWorldState, IBlockState inLocalState) {
		PhysCollisionObject toCollideWith = null;
		toCollideWith = collider.collisions[1];

		if (toCollideWith.penetrationDistance > axisTolerance || toCollideWith.penetrationDistance < -axisTolerance) {
			toCollideWith = collider.collisions[collider.minDistanceIndex];
		}


		NestedBoolean didBlockBreakInShip = new NestedBoolean(false);
		NestedBoolean didBlockBreakInWorld = new NestedBoolean(false);

		Vector positionInBody = collider.entity.getCenter();
		positionInBody.subtract(parent.wrapper.posX, parent.wrapper.posY, parent.wrapper.posZ);

		Vector velocityAtPoint = calculator.getVelocityAtPoint(positionInBody);

		double collisionSpeed = velocityAtPoint.dot(toCollideWith.axis);

		double impulseApplied = BlockRammingManager.processBlockRamming(parent.wrapper, collisionSpeed, inLocalState, inWorldState, inLocalPos, inWorldPos, didBlockBreakInShip, didBlockBreakInWorld);

		Vector[] collisionPoints = PolygonCollisionPointFinder.getPointsOfCollisionForPolygons(collider, toCollideWith, velocityAtPoint);

		impulseApplied /= collisionPoints.length;

		for (Vector collisionPos : collisionPoints) {
			Vector inBody = collisionPos.getSubtraction(new Vector(parent.wrapper.posX, parent.wrapper.posY, parent.wrapper.posZ));
			inBody.multiply(-1D);
			Vector momentumAtPoint = calculator.getVelocityAtPoint(inBody);
			Vector axis = toCollideWith.axis;
			Vector offsetVector = toCollideWith.getResponse();
			calculateCollisionImpulseForce(inBody, momentumAtPoint, axis, offsetVector, didBlockBreakInShip.getValue(), didBlockBreakInWorld.getValue(), impulseApplied);
		}

		if (didBlockBreakInShip.getValue()) {
			worldObj.destroyBlock(inLocalPos, true);
		}

		if (didBlockBreakInWorld.getValue()) {

			if (worldObj.getBlockState(inWorldPos).getBlock() instanceof com.jackredcreeper.cannon.blocks.BlockAirMine) {
				double x = inWorldPos.getX();
				double y = inWorldPos.getY();
				double z = inWorldPos.getZ();

				float size = 8F;
				float power = 0F;
				float blast = 0F;
				float damage = 100F;

				NewExp2 explosion1 = new NewExp2(worldObj, null, x, y, z, size, power, damage, blast, false, true);
				explosion1.newBoom(worldObj, null, x, y, z, size, power, damage, blast, false, true);

				worldObj.setBlockToAir(inWorldPos);
			} else

				worldObj.destroyBlock(inWorldPos, true);
			return true;
		}

		return false;
	}

	//Finally, the end of all this spaghetti code! This step takes all of the math generated before, and it directly adds the result to Ship velocities
	private void calculateCollisionImpulseForce(Vector inBody, Vector momentumAtPoint, Vector axis, Vector offsetVector, boolean didBlockBreakInShip, boolean didBlockBreakInWorld, double impulseApplied) {
		Vector firstCross = inBody.cross(axis);
		RotationMatrices.applyTransform3by3(calculator.invFramedMOI, firstCross);

		Vector secondCross = firstCross.cross(inBody);

		double impulseMagnitude = -momentumAtPoint.dot(axis) * (collisionElasticity + 1D) / (calculator.invMass + secondCross.dot(axis));

		Vector collisionImpulseForce = new Vector(axis, impulseMagnitude);

		if (didBlockBreakInShip || didBlockBreakInWorld) {
//			collisionImpulseForce.multiply(BlockRammingManager.collisionImpulseAfterRamming);
			collisionImpulseForce.multiply(impulseApplied);
		}

		//This is just an optimized way to add this force quickly to the PhysicsCalculations
		if (collisionImpulseForce.dot(offsetVector) < 0) {
//			collisionImpulseForce.multiply(1.8D);
			double collisionVelocity = momentumAtPoint.dot(axis);

			if (false && Math.abs(collisionVelocity) < 0.01D) {
				collisionImpulseForce.zero();
			} else {
				addFrictionToNormalForce(momentumAtPoint, collisionImpulseForce);
				
			}

			calculator.linearMomentum.add(collisionImpulseForce);
			Vector thirdCross = inBody.cross(collisionImpulseForce);

			RotationMatrices.applyTransform3by3(calculator.invFramedMOI, thirdCross);
			calculator.angularVelocity.add(thirdCross);
			// return true;
		}

	}

	private void addFrictionToNormalForce(Vector momentumAtPoint, Vector impulseVector) {
		double kineticFrictionMew = .1D;

		Vector contactNormal = new Vector(impulseVector);
		contactNormal.normalize();
		
		Vector frictionVector = new Vector(momentumAtPoint);
		frictionVector.normalize();
		
		frictionVector.multiply(impulseVector.length() * kineticFrictionMew);
		
		if(frictionVector.dot(momentumAtPoint) > 0) {
			frictionVector.multiply(-1D);
		}
		
		//Remove all friction components along the impulse vector
		double frictionImpulseDot = frictionVector.dot(contactNormal);
		
		Vector toRemove = contactNormal.getProduct(frictionImpulseDot);
		
		frictionVector.subtract(toRemove);
		
		impulseVector.add(frictionVector);
	}
	
	//Applies Coulumb Friction to the collision
	private void calculateCoulumbFriction(Vector inBody, Vector momentumAtPoint, Vector axis, Vector offsetVector) {
		//Some number between 0 and 1
		double coefficientOfFriction = .5D;

		Vector tangentOfSliding = new Vector();

		double dotProduct = momentumAtPoint.dot(axis);

		tangentOfSliding = new Vector(momentumAtPoint);
		tangentOfSliding.subtract(axis.getProduct(dotProduct));

		//This is probably wrong
//		tangentOfSliding = momentumAtPoint.cross(axis).cross(axis);
		tangentOfSliding.normalize();

//		System.out.println(tangentOfSliding);

		Vector firstCross = inBody.cross(tangentOfSliding);
		RotationMatrices.applyTransform3by3(calculator.invFramedMOI, firstCross);

		Vector secondCross = firstCross.cross(inBody);


		double magnitudeOfFriction = -momentumAtPoint.dot(tangentOfSliding);

		magnitudeOfFriction /= (calculator.invMass + secondCross.dot(tangentOfSliding));

		Vector simpleImpulse = new Vector(tangentOfSliding, magnitudeOfFriction);
//		System.out.println(simpleImpulse);
		if (simpleImpulse.dot(offsetVector) < 0) {
			calculator.linearMomentum.add(simpleImpulse);
			Vector thirdCross = inBody.cross(simpleImpulse);

			RotationMatrices.applyTransform3by3(calculator.invFramedMOI, thirdCross);
			calculator.angularVelocity.add(thirdCross);
			// return true;
		}
	}

	private boolean shouldUpdateCollisonCache() {
		return (ticksSinceCacheUpdate) > collisionCacheTickUpdateFrequency;
	}

	private void updatePotentialCollisionCache() {
		final AxisAlignedBB collisionBB = parent.collisionBB.expand(expansion, expansion, expansion).addCoord(calculator.linearMomentum.X * calculator.invMass, calculator.linearMomentum.Y * calculator.invMass, calculator.linearMomentum.Z * calculator.invMass);

		ticksSinceCacheUpdate = 0D;
		//This is being used to occasionally offset the collision cache update, in the hopes this will prevent multiple ships from all updating
		//in the same tick
		if (Math.random() > .5) {
			ticksSinceCacheUpdate -= .05D;
		}

		// cachedPotentialHits = new ArrayList<BlockPos>();
		cachedPotentialHits = new TIntArrayList();
		// Ship is outside of world blockSpace, just skip this all together
		if (collisionBB.maxY < 0 || collisionBB.minY > 255) {
			// internalCachedPotentialHits = new BlockPos[0];
			return;
		}

		//Has a -1 on the minY value, I hope this helps with preventing things from falling through the floor
		final BlockPos min = new BlockPos(collisionBB.minX, Math.max(collisionBB.minY - 1, 0), collisionBB.minZ);
		final BlockPos max = new BlockPos(collisionBB.maxX, Math.min(collisionBB.maxY, 255), collisionBB.maxZ);
		centerPotentialHit = new BlockPos((min.getX() + max.getX()) / 2D, (min.getY() + max.getY()) / 2D, (min.getZ() + max.getZ()) / 2D);

		final ChunkCache cache = parent.surroundingWorldChunksCache;
		final Vector inLocal = new Vector();
		int maxX, maxY, maxZ, localX, localY, localZ, x, y, z, chunkX, chunkZ;
		double rangeCheck = 1.8D;

		if (ValkyrienWarfareMod.highAccuracyCollisions) {
			rangeCheck = 3D;
		}

		Chunk chunk, chunkIn;
		ExtendedBlockStorage extendedblockstorage;
		IBlockState state, localState;

		int chunkMinX = min.getX() >> 4;
		int chunkMaxX = (max.getX() >> 4) + 1;
		int storageMinY = min.getY() >> 4;
		int storageMaxY = (max.getY() >> 4) + 1;
		int chunkMinZ = min.getZ() >> 4;
		int chunkMaxZ = (max.getZ() >> 4) + 1;

		int storageY;

		int mmX = min.getX(), mmY = min.getY(), mmZ = min.getZ(), mxX = max.getX(), mxY = max.getY(), mxZ = max.getZ();

		Vector inBody = new Vector();
		Vector speedInBody = new Vector();

		for (chunkX = chunkMinX; chunkX < chunkMaxX; chunkX++) {
			for (chunkZ = chunkMinZ; chunkZ < chunkMaxZ; chunkZ++) {

				int arrayChunkX = chunkX - cache.chunkX;
				int arrayChunkZ = chunkZ - cache.chunkZ;

				if (!(arrayChunkX < 0 || arrayChunkZ < 0 || arrayChunkX > cache.chunkArray.length - 1 || arrayChunkZ > cache.chunkArray[0].length - 1)) {
					chunk = cache.chunkArray[arrayChunkX][arrayChunkZ];
					for (storageY = storageMinY; storageY < storageMaxY; storageY++) {
						extendedblockstorage = chunk.storageArrays[storageY];
						if (extendedblockstorage != null) {
							int minStorageX = chunkX << 4;
							int minStorageY = storageY << 4;
							int minStorageZ = chunkZ << 4;

							int maxStorageX = minStorageX + 16;
							int maxStorageY = minStorageY + 16;
							int maxStorageZ = minStorageZ + 16;

							minStorageX = Math.max(minStorageX, mmX);
							minStorageY = Math.max(minStorageY, mmY);
							minStorageZ = Math.max(minStorageZ, mmZ);

							maxStorageX = Math.min(maxStorageX, mxX);
							maxStorageY = Math.min(maxStorageY, mxY);
							maxStorageZ = Math.min(maxStorageZ, mxZ);

							for (x = minStorageX; x < maxStorageX; x++) {
								for (y = minStorageY; y < maxStorageY; y++) {
									for (z = minStorageZ; z < maxStorageZ; z++) {
										state = extendedblockstorage.get(x & 15, y & 15, z & 15);

										if (state.getMaterial().isSolid()) {
											inLocal.X = x + .5D;
											inLocal.Y = y + .5D;
											inLocal.Z = z + .5D;

											parent.coordTransform.fromGlobalToLocal(inLocal);

											inBody.setSubtraction(inLocal, parent.centerCoord);

											parent.physicsProcessor.setVectorToVelocityAtPoint(inBody, speedInBody);

											speedInBody.multiply(-parent.physicsProcessor.physRawSpeed);

											if (ValkyrienWarfareMod.highAccuracyCollisions) {
												speedInBody.multiply(20D);
											}
//											System.out.println(speedInBody);

											int minX, minY, minZ;

											if (speedInBody.X > 0) {
												minX = MathHelper.floor(inLocal.X - rangeCheck);
												maxX = MathHelper.floor(inLocal.X + rangeCheck + speedInBody.X);
											} else {
												minX = MathHelper.floor(inLocal.X - rangeCheck + speedInBody.X);
												maxX = MathHelper.floor(inLocal.X + rangeCheck);
											}

											if (speedInBody.Y > 0) {
												minY = MathHelper.floor(inLocal.Y - rangeCheck);
												maxY = MathHelper.floor(inLocal.Y + rangeCheck + speedInBody.Y);
											} else {
												minY = MathHelper.floor(inLocal.Y - rangeCheck + speedInBody.Y);
												maxY = MathHelper.floor(inLocal.Y + rangeCheck);
											}

											if (speedInBody.Z > 0) {
												minZ = MathHelper.floor(inLocal.Z - rangeCheck);
												maxZ = MathHelper.floor(inLocal.Z + rangeCheck + speedInBody.Z);
											} else {
												minZ = MathHelper.floor(inLocal.Z - rangeCheck + speedInBody.Z);
												maxZ = MathHelper.floor(inLocal.Z + rangeCheck);
											}


											/** The Old Way of doing things; approx. 33% slower overall when running this code instead of new
											 for (localX = minX; localX < maxX; localX++) {
											 for (localZ = minZ; localZ < maxZ; localZ++) {
											 for (localY = minY; localY < maxY; localY++) {
											 if (parent.ownsChunk(localX >> 4, localZ >> 4)) {
											 chunkIn = parent.VKChunkCache.getChunkAt(localX >> 4, localZ >> 4);
											 localState = chunkIn.getBlockState(localX, localY, localZ);
											 if (localState.getMaterial().isSolid()) {
											 cachedPotentialHits.add(SpatialDetector.getHashWithRespectTo(x, y, z, centerPotentialHit));
											 localX = localY = localZ = Integer.MAX_VALUE - 420;
											 }
											 }
											 }
											 }
											 }
											 **/


											int shipChunkMinX = minX >> 4;
											int shipChunkMinY = Math.max(minY >> 4, 0);
											int shipChunkMinZ = minZ >> 4;

											int shipChunkMaxX = maxX >> 4;
											int shipChunkMaxY = Math.min(maxY >> 4, 15);
											int shipChunkMaxZ = maxZ >> 4;

											shipChunkMaxX++;
											shipChunkMaxY++;
											shipChunkMaxZ++;

											if (shipChunkMaxZ - shipChunkMinZ > 200 || shipChunkMaxX - shipChunkMinX > 200) {
												System.err.println("Wtf. This fucking error");
												return;
											}


											testForNearbyBlocks:
											for (int shipChunkX = shipChunkMinX; shipChunkX < shipChunkMaxX; shipChunkX++) {
												for (int shipChunkZ = shipChunkMinZ; shipChunkZ < shipChunkMaxZ; shipChunkZ++) {
													if (parent.ownsChunk(shipChunkX, shipChunkZ)) {
														chunkIn = parent.VKChunkCache.getChunkAt(shipChunkX, shipChunkZ);
														for (int shipChunkYStorage = shipChunkMinY; shipChunkYStorage < shipChunkMaxY; shipChunkYStorage++) {
															ExtendedBlockStorage storage = chunkIn.storageArrays[shipChunkYStorage];

															if (storage != null) {
																int shipStorageMinX = shipChunkX << 4;
																int shipStorageMinY = shipChunkYStorage << 4;
																int shipStorageMinZ = shipChunkZ << 4;

																int shipStorageMaxX = shipStorageMinX + 16;
																int shipStorageMaxY = shipStorageMinY + 16;
																int shipStorageMaxZ = shipStorageMinZ + 16;

																shipStorageMinX = Math.max(shipStorageMinX, minX);
																shipStorageMinY = Math.max(shipStorageMinY, minY);
																shipStorageMinZ = Math.max(shipStorageMinZ, minZ);

																shipStorageMaxX = Math.min(shipStorageMaxX, maxX);
																shipStorageMaxY = Math.min(shipStorageMaxY, maxY);
																shipStorageMaxZ = Math.min(shipStorageMaxZ, maxZ);

																for (localX = shipStorageMinX; localX < shipStorageMaxX; localX++) {
																	for (localY = shipStorageMinY; localY < shipStorageMaxY; localY++) {
																		for (localZ = shipStorageMinZ; localZ < shipStorageMaxZ; localZ++) {
																			localState = chunkIn.getBlockState(localX, localY, localZ);
																			if (localState.getMaterial().isSolid()) {
																				cachedPotentialHits.add(SpatialDetector.getHashWithRespectTo(x, y, z, centerPotentialHit));
																				break testForNearbyBlocks;
																			}
																		}
																	}
																}
															}
														}
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}


		/**
		 for (x = min.getX(); x <= max.getX(); x++) {
		 for (z = min.getZ(); z < max.getZ(); z++) {
		 chunkX = (x >> 4) - cache.chunkX;
		 chunkZ = (z >> 4) - cache.chunkZ;
		 if (!(chunkX < 0 || chunkZ < 0 || chunkX > cache.chunkArray.length - 1 || chunkZ > cache.chunkArray[0].length - 1)) {
		 chunk = cache.chunkArray[chunkX][chunkZ];
		 for (y = min.getY(); y < max.getY(); y++) {
		 extendedblockstorage = chunk.storageArrays[y >> 4];
		 if (extendedblockstorage != null) {
		 state = extendedblockstorage.get(x & 15, y & 15, z & 15);

		 if (state.getMaterial().isSolid()) {
		 inLocal.X = x + .5D;
		 inLocal.Y = y + .5D;
		 inLocal.Z = z + .5D;
		 parent.coordTransform.fromGlobalToLocal(inLocal);

		 maxX = (int) math.floor(inLocal.X + rangeCheck);
		 maxY = (int) math.floor(inLocal.Y + rangeCheck);
		 maxZ = (int) math.floor(inLocal.Z + rangeCheck);

		 for (localX = MathHelper.floor_double(inLocal.X - rangeCheck); localX < maxX; localX++) {
		 for (localZ = MathHelper.floor_double(inLocal.Z - rangeCheck); localZ < maxZ; localZ++) {
		 for (localY = MathHelper.floor_double(inLocal.Y - rangeCheck); localY < maxY; localY++) {
		 if (parent.ownsChunk(localX >> 4, localZ >> 4)) {
		 chunkIn = parent.VKChunkCache.getChunkAt(localX >> 4, localZ >> 4);
		 localState = chunkIn.getBlockState(localX, localY, localZ);
		 if (localState.getMaterial().isSolid()) {
		 cachedPotentialHits.add(SpatialDetector.getHashWithRespectTo(x, y, z, centerPotentialHit));
		 localX = localY = localZ = Integer.MAX_VALUE - 420;
		 }
		 }
		 }
		 }
		 }
		 }
		 }
		 }
		 }
		 }
		 }
		 **/

		cachedPotentialHits.shuffle(rand);
	}

}

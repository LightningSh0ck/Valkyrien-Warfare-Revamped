package ValkyrienWarfareBase.ChunkManagement;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

/**
 * This stores the chunk claims for a PhysicsObject
 * @author thebest108
 *
 */
public class ChunkSet {

	public World world;
	public int centerX,centerZ;
	public int radius;
	public int minX,maxX,minZ,maxZ;
	public boolean[][] chunkOccupiedInLocal;
	
	public ChunkSet(int x,int z,int size){
		centerX = x;
		centerZ = z;
		radius = size;
		minX = centerX - radius;
		maxX = centerX + radius;
		minZ = centerZ - radius;
		maxZ = centerZ + radius;
		chunkOccupiedInLocal = new boolean[(radius*2)+1][(radius*2)+1];
	}
	
	public ChunkSet(NBTTagCompound readFrom){
		centerX = readFrom.getInteger("centerX");
		centerZ = readFrom.getInteger("centerZ");
		radius = readFrom.getInteger("radius");
	}
	
	public void writeToNBT(NBTTagCompound toSave){
		toSave.setInteger("centerX", centerX);
		toSave.setInteger("centerZ", centerZ);
		toSave.setInteger("radius", radius);
	}
	
	@Override
	public String toString(){
		return centerX+":"+centerZ+":"+radius;
	}
	
}
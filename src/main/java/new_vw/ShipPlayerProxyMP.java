package new_vw;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.world.WorldServer;
import valkyrienwarfare.physicsmanagement.PhysicsWrapperEntity;

public class ShipPlayerProxyMP extends EntityPlayerMP {

	public EntityPlayerMP vw_realPlayer;
	public PhysicsWrapperEntity vw_proxyRegion;
	
	public ShipPlayerProxyMP(EntityPlayerMP thePlayer, PhysicsWrapperEntity theShip) {
		super(thePlayer.getServer(), (WorldServer) thePlayer.getEntityWorld(), thePlayer.getGameProfile(), new PlayerInteractionManager(thePlayer.getEntityWorld()));
	}
	
	public static ShipPlayerProxyMP createProxyPlayer(EntityPlayerMP thePlayer, PhysicsWrapperEntity theShip) {
		return new ShipPlayerProxyMP(thePlayer, theShip);
	}

	public void syncPlayerToProxy() {
		vw_realPlayer.inventory.copyInventory(this.inventory);

	}
	
	public void syncProxyToPlayer() {
		this.inventory.copyInventory(vw_realPlayer.inventory);
	}
}

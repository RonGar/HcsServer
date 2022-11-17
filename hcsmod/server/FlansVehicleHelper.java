/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  co.uk.flansmods.common.driveables.DriveableType
 *  co.uk.flansmods.common.driveables.EntitySeat
 *  co.uk.flansmods.common.driveables.EntityVehicle
 *  co.uk.flansmods.common.driveables.VehicleController
 *  co.uk.flansmods.common.network.PacketVehControlInfo
 *  co.uk.flansmods.common.vector.Vector3f
 *  cpw.mods.fml.common.network.PacketDispatcher
 *  cpw.mods.fml.common.network.Player
 *  hcsmod.player.ExtendedPlayer
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.entity.player.EntityPlayerMP
 *  net.minecraft.network.packet.Packet
 *  vintarz.core.VRP
 */
package hcsmod.server;

import co.uk.flansmods.common.driveables.DriveableType;
import co.uk.flansmods.common.driveables.EntitySeat;
import co.uk.flansmods.common.driveables.EntityVehicle;
import co.uk.flansmods.common.driveables.VehicleController;
import co.uk.flansmods.common.network.PacketVehControlInfo;
import co.uk.flansmods.common.vector.Vector3f;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import hcsmod.player.ExtendedPlayer;
import hcsmod.server.ExtendedStorage;
import java.io.DataInput;
import java.io.IOException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.packet.Packet;
import vintarz.core.VRP;

public class FlansVehicleHelper {
    static void handleControl(EntityPlayerMP p, VRP in) {
        try {
            ExtendedStorage storage = ExtendedStorage.get(ExtendedPlayer.server((EntityPlayer)p));
            if (storage.vehicleUpdated) {
                return;
            }
            storage.vehicleUpdated = true;
            if (p.o instanceof EntitySeat) {
                EntitySeat seat = (EntitySeat)p.o;
                if (seat.driveable instanceof EntityVehicle) {
                    boolean mismatch;
                    EntityVehicle vehicle = (EntityVehicle)seat.driveable;
                    int controlID = in.readUnsignedByte();
                    boolean bl = mismatch = controlID != vehicle.controlID;
                    if (!mismatch) {
                        float throttle = in.readFloat();
                        float steeringAngle = in.readFloat();
                        float limit = 1.0f;
                        if (throttle >= -limit && throttle <= limit) {
                            vehicle.throttle = throttle;
                        }
                        if (steeringAngle >= -(limit = 20.0f) && steeringAngle <= limit) {
                            vehicle.steeringAngle = steeringAngle;
                        }
                        vehicle.serverUpdate = true;
                        vehicle.l_();
                        vehicle.serverUpdate = false;
                        float fpx = in.readFloat();
                        float fpy = in.readFloat();
                        float fpz = in.readFloat();
                        float rpx = in.readFloat();
                        float rpy = in.readFloat();
                        float rpz = in.readFloat();
                        VehicleController controller = vehicle.frontController;
                        if (fpx != (float)controller.u || fpz != (float)controller.w && fpy != (float)controller.v) {
                            mismatch = true;
                        }
                        controller = vehicle.rearController;
                        if (rpx != (float)controller.u || rpz != (float)controller.w && rpy != (float)controller.v) {
                            mismatch = true;
                        }
                    }
                    if (mismatch) {
                        if (controlID == vehicle.controlID) {
                            vehicle.updateControlID();
                        }
                        PacketDispatcher.sendPacketToPlayer((Packet)PacketVehControlInfo.buildPacket((EntityVehicle)vehicle), (Player)((Player)p));
                    }
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean verify(VehicleController controller, DataInput in) throws IOException {
        float x = in.readFloat();
        float y = in.readFloat();
        float z = in.readFloat();
        boolean valid = x == (float)controller.u && z == (float)controller.w && y == (float)controller.v;
        return valid;
    }

    public static void init(EntityVehicle vehicle, DriveableType type) {
        vehicle.frontController = new VehicleController(vehicle);
        vehicle.rearController = new VehicleController(vehicle);
        vehicle.frontController.steerable = true;
        FlansVehicleHelper.initController(vehicle, type, vehicle.frontController, 2);
        FlansVehicleHelper.initController(vehicle, type, vehicle.rearController, 0);
    }

    private static void initController(EntityVehicle vehicle, DriveableType type, VehicleController controller, int wheelID) {
        controller.O = 1.6f;
        controller.P = 1.8f;
        controller.wheelID = wheelID;
        controller.springStrength = type.wheelSpringStrength;
        Vector3f wheelVector = vehicle.axes.findLocalVectorGlobally(type.wheelPositions[wheelID].position);
        controller.b(vehicle.u + (double)wheelVector.x, vehicle.v + (double)wheelVector.y + 1.0 - 0.0625, vehicle.w + (double)wheelVector.z);
    }
}


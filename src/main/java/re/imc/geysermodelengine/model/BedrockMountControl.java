package re.imc.geysermodelengine.model;

import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.geysermc.floodgate.api.FloodgateApi;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.entity.BukkitEntity;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.bone.type.Mount;
import com.ticxo.modelengine.api.mount.controller.MountController;

import re.imc.geysermodelengine.GeyserModelEngine;

public class BedrockMountControl extends BukkitRunnable {

    private static BedrockMountControl instance;

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId())) {
                continue;
            }

            float pitch = player.getLocation().getPitch();
            Pair<ActiveModel, Mount> seat = GeyserModelEngine.getInstance().getDrivers().get(player);
            if (seat != null) {
                if (pitch < -30) {
                    MountController controller = ModelEngineAPI.getMountPairManager()
                            .getController(player.getUniqueId());
                    if (controller != null) {
                        MountController.MountInput input = controller.getInput();
                        if (input != null) {
                            input.setJump(true);
                            controller.setInput(input);
                        }
                    }
                }
                if (pitch > 80) {
                    if (seat.getKey().getModeledEntity().getBase() instanceof BukkitEntity bukkitEntity) {
                        if (bukkitEntity.getOriginal().isOnGround()) {
                            return;
                        }
                    }
                    MountController controller = ModelEngineAPI.getMountPairManager()
                            .getController(player.getUniqueId());

                    if (controller != null) {
                        MountController.MountInput input = controller.getInput();
                        if (input != null) {
                            input.setSneak(true);
                            controller.setInput(input);
                        }
                    }
                }

            }
        }
    }

    public static void startTask() {
        if (instance != null) {
            instance.cancel();
        }

        instance = new BedrockMountControl();
        instance.runTaskTimerAsynchronously(GeyserModelEngine.getInstance(), 1, 1);
    }

    public static void stopTask() {
        if (instance != null) {
            instance.cancel();
            instance = null;
        }
    }

}

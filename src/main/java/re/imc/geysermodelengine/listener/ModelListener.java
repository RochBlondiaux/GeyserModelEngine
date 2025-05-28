package re.imc.geysermodelengine.listener;

import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.ticxo.modelengine.api.events.AddModelEvent;
import com.ticxo.modelengine.api.events.ModelDismountEvent;
import com.ticxo.modelengine.api.events.ModelMountEvent;
import com.ticxo.modelengine.api.model.ActiveModel;

import lombok.RequiredArgsConstructor;
import re.imc.geysermodelengine.GeyserModelEngine;
import re.imc.geysermodelengine.model.ModelEntity;

@RequiredArgsConstructor
public class ModelListener implements Listener {

    private final GeyserModelEngine plugin;

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAddModel(AddModelEvent event) {
        if (event.isCancelled() || !this.plugin.isInitialized())
            return;
        ModelEntity.create(event.getTarget(), event.getModel());
    }


    @EventHandler(priority = EventPriority.MONITOR)
    public void onModelMount(ModelMountEvent event) {
        Map<ActiveModel, ModelEntity> map = ModelEntity.ENTITIES.get(event.getVehicle().getModeledEntity().getBase().getEntityId());
        if (!event.isDriver())
            return;

        ModelEntity model = map.get(event.getVehicle());

        if (model != null && event.getPassenger() instanceof Player player) {
            this.plugin.getDrivers().put(player, Pair.of(event.getVehicle(), event.getSeat()));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onModelDismount(ModelDismountEvent event) {
        if (event.getPassenger() instanceof Player player) {
            this.plugin.getDrivers().remove(player);
        }
    }

    @EventHandler
    public void onPlayerLogin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskLater(this.plugin, () -> this.plugin.getJoinedPlayers().add(event.getPlayer()), 10);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Bukkit.getScheduler().runTaskLater(this.plugin, () -> this.plugin.getJoinedPlayers().remove(event.getPlayer()), 10);
    }
}

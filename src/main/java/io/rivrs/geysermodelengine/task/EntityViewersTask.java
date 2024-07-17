package io.rivrs.geysermodelengine.task;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.NumberConversions;
import org.geysermc.floodgate.api.FloodgateApi;

import io.rivrs.geysermodelengine.GeyserModelEngine;
import io.rivrs.geysermodelengine.configuration.Configuration;
import io.rivrs.geysermodelengine.model.BedrockEntity;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class EntityViewersTask implements Runnable {

    private final GeyserModelEngine plugin;
    private final List<UUID> gracePeriodPlayers = new CopyOnWriteArrayList<>();

    @Override
    public void run() {
        final Configuration configuration = plugin.getConfiguration();

        for (BedrockEntity entity : this.plugin.getEntities().entities()) {
            final Location location = entity.getLocation();

            // Add missing viewers
            Bukkit.getOnlinePlayers()
                    .stream()
                    .filter(player -> location.getWorld().equals(player.getWorld()))
                    .filter(player -> !entity.hasViewer(player))
                    .filter(player -> !gracePeriodPlayers.contains(player.getUniqueId()))
                    .filter(player -> FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId()))
                    .filter(player -> player.getLocation().distanceSquared(location) <= NumberConversions.square(configuration.viewDistance()))
                    .filter(player -> this.plugin.getEntities().getViewedEntitiesCount(player) < configuration.maximumModels())
                    .forEach(player -> {
                        if (plugin.getPlayers().isInGracePeriod(player.getUniqueId())) {
                            Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
                                if (player.isOnline())
                                    entity.addViewer(player);
                                this.gracePeriodPlayers.remove(player.getUniqueId());
                            }, plugin.getPlayers().getRemainingGracePeriod(player.getUniqueId()) / 1000 * 20L);
                            System.out.println("Player " + player.getName() + " is in grace period for " + plugin.getPlayers().getRemainingGracePeriod(player.getUniqueId()) + "ms.");
                            this.gracePeriodPlayers.add(player.getUniqueId());
                            return;
                        }

                        entity.addViewer(player);
                    });

            // Remove old viewers
            for (Player viewer : entity.getViewers()) {
                // Offline players or players in different worlds are removed
                if (viewer == null || !viewer.isOnline() || !location.getWorld().equals(viewer.getWorld())) {
                    entity.removeViewer(viewer);
                    continue;
                }

                // Players outside the view distance are removed
                if (viewer.getLocation().distanceSquared(location) > NumberConversions.square(configuration.viewDistance())) {
                    entity.removeViewer(viewer);
                }
            }
        }
    }
}

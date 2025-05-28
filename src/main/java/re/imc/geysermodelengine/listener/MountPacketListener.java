package re.imc.geysermodelengine.listener;

import org.apache.commons.lang3.tuple.Pair;
import org.geysermc.floodgate.api.FloodgateApi;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientEntityAction;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.bone.type.Mount;

import lombok.RequiredArgsConstructor;
import re.imc.geysermodelengine.GeyserModelEngine;

@RequiredArgsConstructor
public class MountPacketListener implements PacketListener {

    private final GeyserModelEngine plugin;

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() != PacketType.Play.Client.ENTITY_ACTION
            || !FloodgateApi.getInstance().isFloodgatePlayer(event.getUser().getUUID()))
            return;

        WrapperPlayClientEntityAction action = new WrapperPlayClientEntityAction(event);
        Pair<ActiveModel, Mount> seat = this.plugin.getDrivers().get(event.getPlayer());
        if (seat != null) {
            if (action.getAction() == WrapperPlayClientEntityAction.Action.START_SNEAKING) {
                ModelEngineAPI.getMountPairManager().tryDismount(event.getPlayer());
            }
        }
    }
}

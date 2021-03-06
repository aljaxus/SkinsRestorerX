package skinsrestorer.velocity.utils;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.util.GameProfile;
import com.velocitypowered.api.util.GameProfile.Property;
import skinsrestorer.shared.storage.SkinStorage;
import skinsrestorer.shared.utils.MojangAPI;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by McLive on 16.02.2019.
 */
public class SkinApplier {
    public static GameProfile updateProfileSkin(GameProfile profile, String skin) {
        try {
            Property textures = (Property) SkinStorage.getOrCreateSkinForPlayer(skin);
            List<Property> oldProperties = profile.getProperties();
            List<Property> newProperties = updatePropertiesSkin(oldProperties, textures);
            return new GameProfile(profile.getId(), profile.getName(), newProperties);
        } catch (MojangAPI.SkinRequestException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static List<Property> updatePropertiesSkin(List<Property> original, Property property) {
        List<Property> properties = new ArrayList<>(original);
        boolean applied = false;
        for (int i = 0; i < properties.size(); i++) {
            Property lproperty = properties.get(i);
            if ("textures".equals(lproperty.getName())) {
                properties.set(i, property);
                applied = true;
            }
        }
        if (!applied) {
            properties.add(property);
        }
        return properties;
    }

    public static void applySkin(Player player, Property property) {
        player.setGameProfileProperties(updatePropertiesSkin(player.getGameProfileProperties(), property));
    }

    public static void applySkin(final Player p, final String skin) {
        try {
            Property textures = (Property) SkinStorage.getOrCreateSkinForPlayer(skin);
            List<Property> oldProperties = p.getGameProfileProperties();
            List<Property> newProperties = updatePropertiesSkin(oldProperties, textures);

            p.setGameProfileProperties(newProperties);
            sendUpdateRequest(p, textures);
        } catch (MojangAPI.SkinRequestException e) {
            e.printStackTrace();
        }
    }

    private static void sendUpdateRequest(Player p, Property textures) {
        p.getCurrentServer().ifPresent(serverConnection -> {
            System.out.println("[SkinsRestorer] Sending skin update request for " + p.getUsername());

            ByteArrayOutputStream b = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(b);
            try {
                out.writeUTF("SkinUpdate");

                if (textures != null) {
                    out.writeUTF(textures.getName());
                    out.writeUTF(textures.getValue());
                    out.writeUTF(textures.getSignature());
                }

                serverConnection.sendPluginMessage(MinecraftChannelIdentifier.create("sr", "skinchange"), b.toByteArray());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}

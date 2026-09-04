package com.santipdr.jobsmenu.client.scene;

import net.minecraft.network.chat.Component;

/**
 * Facade for the visible level labels used by PantallaNivel.
 *
 * Levels 18-31 were reviewed against the real JPGs, but their text now lives
 * in the normal lang JSON files just like levels 0-17. Keeping one translation
 * source avoids ES/EN drift and lets Minecraft handle language fallback.
 */
public final class RotulosNivelesImagen {

    private static final int NOTAS = 3;

    private RotulosNivelesImagen() {
    }

    public static Component nombre(Nivel nivel) {
        return Component.translatable("jobsmenu." + nivel.clave + ".nombre");
    }

    public static Component nota(Nivel nivel, int variante) {
        int cual = Math.floorMod(variante, NOTAS);
        return Component.translatable("jobsmenu." + nivel.clave + ".nota" + cual);
    }
}

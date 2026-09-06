package com.santipdr.jobsmenu.client;

import com.santipdr.jobsmenu.JobsMenu;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModList;

/** Puente opcional hacia la pantalla grafica registrada por Embeddium. */
public final class CompatGraficos {

    private static final String EMBEDDIUM_ID = "embeddium";

    private static boolean falloAvisado;
    private static long aperturasEmbeddium;
    private static long fallbacksVanilla;

    private CompatGraficos() {
    }

    /**
     * Pide la pantalla al extension point oficial de Forge. Jobs no enlaza
     * clases internas de Embeddium, asi que el mod sigue arrancando sin el.
     */
    public static Screen crearPantallaEmbeddium(Minecraft minecraft, Screen anterior) {
        var contenedor = ModList.get().getModContainerById(EMBEDDIUM_ID);
        if (contenedor.isEmpty()) {
            fallbacksVanilla++;
            return null;
        }

        try {
            Screen pantalla = contenedor.get()
                    .getCustomExtension(ConfigScreenHandler.ConfigScreenFactory.class)
                    .map(factory -> factory.screenFunction().apply(minecraft, anterior))
                    .orElse(null);
            if (pantalla != null) {
                aperturasEmbeddium++;
                return pantalla;
            }
        } catch (RuntimeException | LinkageError error) {
            if (!falloAvisado) {
                falloAvisado = true;
                JobsMenu.LOG.warn("Embeddium esta instalado pero su pantalla de graficos no pudo abrirse; se usara Video Settings vanilla.", error);
            }
        }

        fallbacksVanilla++;
        return null;
    }

    public static boolean embeddiumPresenteParaDiagnostico() {
        return ModList.get().getModContainerById(EMBEDDIUM_ID).isPresent();
    }

    public static long aperturasEmbeddiumParaDiagnostico() {
        return aperturasEmbeddium;
    }

    public static long fallbacksVanillaParaDiagnostico() {
        return fallbacksVanilla;
    }
}

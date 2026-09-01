package com.santipdr.jobsmenu.client.screen;

import com.santipdr.jobsmenu.client.ui.ChromeExpediente;
import com.santipdr.jobsmenu.client.ui.ListasExpediente;
import com.santipdr.jobsmenu.client.ui.PielVanillaJobs;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.client.gui.ModListScreen;

/**
 * Pantalla Forge de mods con continuidad visual Jobs. Se conserva ModListScreen
 * integra (filtros, logos, Config, carpeta y panel de informacion).
 */
public final class PantallaModsJobs extends ModListScreen {

    private static final int PANEL_X = 12;
    private static final int PANEL_Y = 8;

    public PantallaModsJobs(Screen anterior) {
        super(anterior);
    }

    @Override
    public void init() {
        super.init();
        ListasExpediente.estilizar(this);
    }

    @Override
    public void renderBackground(GuiGraphics g) {
        ChromeExpediente.fondo(g, this.width, this.height);
        ChromeExpediente.panelArchivo(g, PANEL_X, PANEL_Y,
                this.width - PANEL_X * 2, this.height - PANEL_Y * 2);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        // Forge fija blanco puro para toda la lista y el panel de informacion.
        // El tinte global lo convierte en tinta sepia sin tocar hitboxes,
        // enlaces, logos, busqueda ni compatibilidad con pantallas de Config.
        RenderSystem.setShaderColor(0.48F, 0.40F, 0.24F, 1.0F);
        super.render(g, mouseX, mouseY, partialTick);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        PielVanillaJobs.dibujar(this, g, mouseX, mouseY);
        ChromeExpediente.pieArchivo(g, this.font, PANEL_X, PANEL_Y,
                this.width - PANEL_X * 2, this.height - PANEL_Y * 2, "MODS");
    }
}

package com.santipdr.jobsmenu.client.screen;

import com.santipdr.jobsmenu.client.ui.ChromeExpediente;
import com.santipdr.jobsmenu.client.ui.ListasExpediente;
import com.santipdr.jobsmenu.client.ui.PielVanillaJobs;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.client.gui.ModListScreen;

/**
 * Pantalla Forge de mods con continuidad visual Jobs. Se conserva ModListScreen
 * íntegra (filtros, logos, Config, carpeta y panel de información).
 */
public final class PantallaModsJobs extends ModListScreen {

    private static final int PANEL_X = 8;
    private static final int PANEL_Y = 6;

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
        ChromeExpediente.panel(g, PANEL_X, PANEL_Y,
                this.width - PANEL_X * 2, this.height - PANEL_Y * 2);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        super.render(g, mouseX, mouseY, partialTick);
        PielVanillaJobs.dibujar(this, g, mouseX, mouseY);
        ChromeExpediente.esquinas(g, PANEL_X, PANEL_Y,
                this.width - PANEL_X * 2, this.height - PANEL_Y * 2);
    }
}

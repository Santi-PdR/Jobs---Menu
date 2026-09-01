package com.santipdr.jobsmenu.client.screen;

import com.santipdr.jobsmenu.client.ui.ChromeExpediente;
import com.santipdr.jobsmenu.client.ui.ListasExpediente;
import com.santipdr.jobsmenu.client.ui.PielVanillaJobs;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;

/**
 * Selector de mundos vanilla conservado, pero presentado sobre el expediente
 * Jobs. No reemplaza la lista ni sus acciones: solo elimina el bloque de dirt
 * aislado que rompia la continuidad visual del menu.
 */
public final class PantallaMundosJobs extends SelectWorldScreen {

    private static final int PANEL_X = 8;
    private static final int PANEL_Y = 6;

    public PantallaMundosJobs(Screen anterior) {
        super(anterior);
    }

    @Override
    protected void init() {
        super.init();
        // La lista sigue siendo la de Minecraft; solo pierde sus bandas dirt.
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
        // SelectWorldScreen no necesita conocer nada del mod: preparamos la hoja,
        // dejamos que vanilla pinte sus previews/lista y luego vestimos controles.
        renderBackground(g);
        super.render(g, mouseX, mouseY, partialTick);
        PielVanillaJobs.dibujar(this, g, mouseX, mouseY);
        ChromeExpediente.esquinas(g, PANEL_X, PANEL_Y,
                this.width - PANEL_X * 2, this.height - PANEL_Y * 2);
        ChromeExpediente.pie(g, this.font, PANEL_X, PANEL_Y,
                this.width - PANEL_X * 2, this.height - PANEL_Y * 2, "WRLD-014");
    }
}

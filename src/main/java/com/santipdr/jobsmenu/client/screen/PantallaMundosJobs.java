package com.santipdr.jobsmenu.client.screen;

import com.santipdr.jobsmenu.client.ui.ChromeExpediente;
import com.santipdr.jobsmenu.client.ui.ListasExpediente;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;

/**
 * Selector de mundos vanilla conservado, pero presentado sobre el expediente
 * Jobs. No reemplaza la lista ni sus acciones: solo elimina el bloque de dirt
 * aislado que rompia la continuidad visual del menu.
 */
public final class PantallaMundosJobs extends SelectWorldScreen {

    private static final int PANEL_X = 12;
    private static final int PANEL_Y = 8;
    private EditBox busqueda;

    public PantallaMundosJobs(Screen anterior) {
        super(anterior);
    }

    @Override
    protected void init() {
        super.init();
        ListasExpediente.estilizar(this, 70, this.height - 66);
        for (var child : this.children()) {
            if (child instanceof EditBox campo) {
                campo.setY(PANEL_Y + 43);
                this.busqueda = campo;
            }
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (Screen.hasControlDown() && keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_F && this.busqueda != null) {
            this.setFocused(this.busqueda);
            this.busqueda.setFocused(true);
            return true;
        }
        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE && this.busqueda != null
                && this.busqueda.isFocused() && !this.busqueda.getValue().isEmpty()) {
            this.busqueda.setValue("");
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void renderBackground(GuiGraphics g) {
        ChromeExpediente.fondo(g, this.width, this.height);
        ChromeExpediente.panelArchivo(g, PANEL_X, PANEL_Y,
                this.width - PANEL_X * 2, this.height - PANEL_Y * 2);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // SelectWorldScreen no necesita conocer nada del mod: preparamos la hoja,
        // dejamos que vanilla pinte sus previews/lista y luego vestimos controles.
        renderBackground(g);
        super.render(g, mouseX, mouseY, partialTick);
        // SelectWorldScreen pinta su titulo vanilla sobre el marco. Se cubre
        // solo esa franja; el buscador queda debajo y conserva su hitbox real.
        int panelW = this.width - PANEL_X * 2;
        g.fill(PANEL_X + 5, PANEL_Y + 5, PANEL_X + panelW - 5, PANEL_Y + 40,
                com.santipdr.jobsmenu.client.ui.Paleta.conAlfa(
                        com.santipdr.jobsmenu.client.ui.Paleta.VANO, 0.96F));
        ChromeExpediente.cabeceraArchivo(g, this.font,
                net.minecraft.network.chat.Component.translatable("jobsmenu.interfaz.mundos.titulo"),
                net.minecraft.network.chat.Component.translatable("jobsmenu.interfaz.mundos.subtitulo"),
                PANEL_X, PANEL_Y, panelW);
        ChromeExpediente.pieArchivo(g, this.font, PANEL_X, PANEL_Y,
                panelW, this.height - PANEL_Y * 2, "SHIFTS");
    }
}

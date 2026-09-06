package com.santipdr.jobsmenu.client.screen;

import com.santipdr.jobsmenu.client.ui.ChromeExpediente;
import com.santipdr.jobsmenu.client.ui.ListasExpediente;
import com.santipdr.jobsmenu.client.ui.Paleta;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;

import org.lwjgl.glfw.GLFW;

/** Selector de mundos vanilla presentado como archivo operativo de Jobs. */
public final class PantallaMundosJobs extends SelectWorldScreen {

    private static final int PANEL_X = 12;
    private static final int PANEL_Y = 8;
    private final Screen anteriorJobs;
    private EditBox busqueda;
    private boolean cerrando;

    public PantallaMundosJobs(Screen anterior) {
        super(anterior);
        this.anteriorJobs = anterior;
    }

    @Override
    protected void init() {
        this.cerrando = false;
        this.busqueda = null;
        super.init();
        int top = Math.max(72, PANEL_Y + 64);
        int bottom = Math.max(top + 44, this.height - 68);
        ListasExpediente.estilizar(this, top, bottom);
        for (var child : this.children()) {
            if (child instanceof EditBox campo) {
                campo.setY(PANEL_Y + 43);
                campo.setWidth(Math.min(campo.getWidth(), Math.max(80, this.width - 86)));
                campo.setTextColor(Paleta.ARCHIVO_TEXTO);
                campo.setTextColorUneditable(Paleta.ARCHIVO_TEXTO_TENUE);
                campo.setBordered(false);
                this.busqueda = campo;
            }
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (Screen.hasControlDown() && keyCode == GLFW.GLFW_KEY_F && this.busqueda != null) {
            this.setFocused(this.busqueda);
            this.busqueda.setFocused(true);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE && atenderEscapeBusqueda()) {
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            volverAlMenu();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    /**
     * ESC no debe expulsar de la pantalla mientras el usuario esta trabajando
     * en el filtro. Primero limpia una busqueda escrita; si ya estaba vacia,
     * solo abandona el foco. El siguiente ESC vuelve al padre.
     */
    private boolean atenderEscapeBusqueda() {
        if (this.busqueda == null || !this.busqueda.isFocused()) return false;
        if (!this.busqueda.getValue().isEmpty()) {
            this.busqueda.setValue("");
            return true;
        }
        this.busqueda.setFocused(false);
        this.setFocused(null);
        return true;
    }

    @Override
    public void onClose() {
        volverAlMenu();
    }

    private void volverAlMenu() {
        if (this.cerrando || this.minecraft == null) return;
        this.cerrando = true;
        this.minecraft.setScreen(this.anteriorJobs != null ? this.anteriorJobs : new PantallaNivel());
    }

    @Override
    public void renderBackground(GuiGraphics g) {
        ChromeExpediente.fondo(g, this.width, this.height);
        ChromeExpediente.panelArchivo(g, PANEL_X, PANEL_Y,
                this.width - PANEL_X * 2, this.height - PANEL_Y * 2);

        int x0 = PANEL_X + 16;
        int x1 = this.width - PANEL_X - 16;
        int y0 = Math.max(70, PANEL_Y + 62);
        int y1 = Math.max(y0 + 40, this.height - 66);
        g.fill(x0, y0, x1, y1, Paleta.conAlfa(Paleta.ARCHIVO_SUPERFICIE, 0.52F));
        g.fill(x0, y0, x1, y0 + 1, Paleta.conAlfa(Paleta.ARCHIVO_ACENTO, 0.18F));
        g.fill(x0, y1 - 1, x1, y1, Paleta.conAlfa(Paleta.ARCHIVO_TEXTO_TENUE, 0.12F));
        g.fill(x0, y0, x0 + 2, y1, Paleta.conAlfa(Paleta.ARCHIVO_ACENTO, 0.28F));
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        RenderSystem.setShaderColor(0.76F, 0.76F, 0.76F, 1.0F);
        super.render(g, mouseX, mouseY, partialTick);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        ListasExpediente.renderarBarras(this, g);

        int panelW = this.width - PANEL_X * 2;
        ChromeExpediente.reemplazarCabeceraArchivo(g, this.font,
                net.minecraft.network.chat.Component.translatable("jobsmenu.interfaz.mundos.titulo"),
                net.minecraft.network.chat.Component.translatable("jobsmenu.interfaz.mundos.subtitulo"),
                PANEL_X, PANEL_Y, panelW);

        if (this.busqueda != null && this.width > 180) {
            int x = this.busqueda.getX();
            int y = this.busqueda.getY();
            int w = this.busqueda.getWidth();
            int h = this.busqueda.getHeight();
            int borde = Paleta.conAlfa(Paleta.ARCHIVO_ACENTO, this.busqueda.isFocused() ? 0.62F : 0.28F);
            g.fill(x - 3, y - 2, x + w + 3, y + h + 2, Paleta.ARCHIVO_SUPERFICIE);
            g.fill(x - 3, y - 2, x + w + 3, y - 1, borde);
            g.fill(x - 3, y + h + 1, x + w + 3, y + h + 2,
                    Paleta.conAlfa(Paleta.ARCHIVO_TEXTO_TENUE, 0.16F));
            g.fill(x - 3, y - 2, x - 1, y + h + 2, borde);
            g.fill(x + 7, y + h + 2, x + Math.min(w - 4, 34), y + h + 3,
                    Paleta.conAlfa(Paleta.ARCHIVO_ACENTO, this.busqueda.isFocused() ? 0.70F : 0.20F));
        }

        int footerY = this.height - PANEL_Y - 20;
        g.fill(PANEL_X + 18, footerY, this.width - PANEL_X - 18, footerY + 1,
                Paleta.conAlfa(Paleta.ARCHIVO_ACENTO, 0.16F));
        String ayuda = "CTRL+F  //  ESC";
        int aw = this.font.width(ayuda);
        if (this.width > aw + 70) {
            g.drawString(this.font, ayuda, this.width - PANEL_X - aw - 22, footerY + 5,
                    Paleta.conAlfa(Paleta.ARCHIVO_TEXTO_TENUE, 0.50F), false);
        }
    }
}

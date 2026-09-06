package com.santipdr.jobsmenu.client.screen;

import com.santipdr.jobsmenu.client.ui.ChromeExpediente;
import com.santipdr.jobsmenu.client.ui.ListasExpediente;
import com.santipdr.jobsmenu.client.ui.Paleta;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.gui.ModListScreen;

import org.lwjgl.glfw.GLFW;

/** Forge Mods con el registro real intacto y solo una piel Jobs exterior. */
public final class PantallaModsJobs extends ModListScreen {
    private static final int PANEL_X = 12;
    private static final int PANEL_Y = 8;

    private final Screen anteriorJobs;
    private EditBox busqueda;
    private boolean cerrando;

    public PantallaModsJobs(Screen anterior) {
        super(anterior);
        this.anteriorJobs = anterior;
    }

    @Override
    public void init() {
        this.cerrando = false;
        this.busqueda = null;
        super.init();

        // No redimensionar la lista de Forge. ModListScreen calcula su propio
        // ancho y sus columnas; forzar updateSize con el ancho total ocultaba
        // entradas y desplazaba el panel de detalle.
        ListasExpediente.estilizar(this);

        for (var child : this.children()) {
            if (child instanceof EditBox campo) {
                this.busqueda = campo;
                campo.setTextColor(Paleta.ARCHIVO_TEXTO);
                campo.setTextColorUneditable(Paleta.ARCHIVO_TEXTO_TENUE);
                campo.setBordered(false);
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

    /** Mismo contrato de busqueda que Mundos: limpiar, soltar foco y luego salir. */
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
    }

    @Override
    public void renderDirtBackground(GuiGraphics g) {
        renderBackground(g);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        super.render(g, mouseX, mouseY, partialTick);

        // Cubre solo el titulo vanilla superior. La lista y el detalle de
        // Forge quedan sin overlays delante para que nombres, logos y botones
        // sigan siendo completamente visibles.
        int panelW = this.width - PANEL_X * 2;
        g.fill(PANEL_X + 3, PANEL_Y + 1,
                PANEL_X + panelW - 3, PANEL_Y + 23, Paleta.VANO);
        Component titulo = this.title == null ? Component.literal("Mods") : this.title;
        int tw = this.font.width(titulo);
        g.drawString(this.font, titulo,
                Math.max(PANEL_X + 10, (this.width - tw) / 2), PANEL_Y + 7,
                Paleta.ARCHIVO_TEXTO, false);
        g.fill(PANEL_X + 18, PANEL_Y + 21,
                this.width - PANEL_X - 18, PANEL_Y + 22,
                Paleta.conAlfa(Paleta.ARCHIVO_ACENTO, 0.26F));

        ListasExpediente.renderarBarras(this, g);

        int railY = this.height - 24;
        if (railY > PANEL_Y + 30) {
            g.fill(PANEL_X + 16, railY,
                    this.width - PANEL_X - 16, railY + 1,
                    Paleta.conAlfa(Paleta.ARCHIVO_ACENTO, 0.18F));
            if (this.width >= 420) {
                g.drawString(this.font, "CTRL+F  //  ESC", PANEL_X + 18, railY + 5,
                        Paleta.conAlfa(Paleta.ARCHIVO_TEXTO_TENUE, 0.52F), false);
            }
        }
    }
}

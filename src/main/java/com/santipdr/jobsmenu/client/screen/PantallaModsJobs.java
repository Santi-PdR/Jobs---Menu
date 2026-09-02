package com.santipdr.jobsmenu.client.screen;

import com.santipdr.jobsmenu.client.ui.ChromeExpediente;
import com.santipdr.jobsmenu.client.ui.ListasExpediente;
import com.santipdr.jobsmenu.client.ui.Paleta;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.gui.ModListScreen;
import org.lwjgl.glfw.GLFW;

/** Forge Mods dentro de un archivo tecnico Jobs, conservando la logica real de Forge. */
public final class PantallaModsJobs extends ModListScreen {
    private static final int PANEL_X = 12;
    private static final int PANEL_Y = 8;
    private EditBox busqueda;
    private int listaArriba;
    private int listaAbajo;

    public PantallaModsJobs(Screen anterior) { super(anterior); }

    @Override
    public void init() {
        super.init();

        // La captura real mostro que la lista Forge seguia entrando por debajo
        // de la cabecera Jobs. Reservamos bandas reales, no solo decorativas.
        this.listaArriba = PANEL_Y + 54;
        this.listaAbajo = Math.max(this.listaArriba + 60, this.height - 58);
        ListasExpediente.estilizar(this, this.listaArriba, this.listaAbajo);

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
        if (keyCode == GLFW.GLFW_KEY_ESCAPE && this.busqueda != null
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

    /** Evita que Forge reinyecte el dirt vanilla en el panel de detalles. */
    @Override
    public void renderDirtBackground(GuiGraphics g) {
        renderBackground(g);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        int panelW = this.width - PANEL_X * 2;
        int panelH = this.height - PANEL_Y * 2;
        int top = Math.max(PANEL_Y + 44, this.listaArriba - 6);
        int bottom = Math.min(PANEL_Y + panelH - 42, this.listaAbajo + 4);

        // Forge usa ~220 px logicos para la lista. El pase anterior partia la
        // pantalla por la mitad y por eso el expediente no coincidia con los
        // widgets reales. Ahora la geometria visual sigue la geometria Forge.
        int split = Math.min(this.width / 2, PANEL_X + 224);
        int left = PANEL_X + 8;
        int right = PANEL_X + panelW - 8;

        g.fill(left, top, split - 5, bottom, Paleta.conAlfa(Paleta.ARCHIVO_FONDO, 0.56F));
        g.fill(split + 4, top, right, bottom, Paleta.conAlfa(Paleta.ARCHIVO_FONDO, 0.44F));
        g.fill(split - 1, top + 4, split + 1, bottom - 4,
                Paleta.conAlfa(Paleta.ARCHIVO_ACENTO, 0.30F));
        g.fill(left + 4, top + 4, left + 7, bottom - 4,
                Paleta.conAlfa(Paleta.ARCHIVO_ACENTO, 0.38F));

        // Guias de expediente en el panel de detalle: el vacio ya no se lee
        // como un bloque de dirt cuando no hay mod seleccionado.
        for (int y = top + 34; y < bottom - 8; y += 34) {
            g.fill(split + 14, y, right - 12, y + 1,
                    Paleta.conAlfa(Paleta.ARCHIVO_ACENTO, 0.075F));
        }

        RenderSystem.setShaderColor(0.76F, 0.76F, 0.76F, 1.0F);
        super.render(g, mouseX, mouseY, partialTick);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        // Cubre de frente exclusivamente la banda de titulo vanilla. La lista
        // ya empieza por debajo, asi que no se tapa ningun mod como en la captura.
        g.pose().pushPose();
        g.pose().translate(0.0F, 0.0F, 450.0F);
        g.fill(PANEL_X + 3, PANEL_Y + 1, PANEL_X + panelW - 3, PANEL_Y + 36, Paleta.VANO);
        g.pose().popPose();
        ChromeExpediente.reemplazarCabeceraArchivo(g, this.font, this.title,
                Component.translatable("jobsmenu.interfaz.mods.buscar"), PANEL_X, PANEL_Y, panelW);

        // El rotulo Search de Forge quedaba flotando sobre el footer. Se limpia
        // esa franja y se dibuja un unico rotulo Jobs alineado al campo real.
        if (this.busqueda != null) {
            int x = this.busqueda.getX();
            int y = this.busqueda.getY();
            int w = this.busqueda.getWidth();
            int h = this.busqueda.getHeight();
            int labelY = Math.max(PANEL_Y + 38, y - 12);
            g.fill(x - 2, labelY - 1, x + w + 2, y,
                    Paleta.conAlfa(Paleta.ARCHIVO_FONDO, 0.96F));
            Component rotulo = Component.translatable("jobsmenu.interfaz.mods.buscar");
            g.drawString(this.font, rotulo, x + Math.max(0, (w - this.font.width(rotulo)) / 2), labelY,
                    Paleta.ARCHIVO_TEXTO_TENUE, false);

            int borde = Paleta.conAlfa(Paleta.ARCHIVO_ACENTO,
                    this.busqueda.isFocused() ? 0.78F : 0.34F);
            g.fill(x - 2, y - 2, x + w + 2, y + h + 2, borde);
            g.fill(x - 1, y - 1, x + w + 1, y + h + 1,
                    Paleta.conAlfa(Paleta.ARCHIVO_FONDO, 0.97F));
        }

        ListasExpediente.renderarBarras(this, g);
        int railY = this.height - 28;
        g.fill(PANEL_X + 12, railY, PANEL_X + panelW - 12, railY + 1,
                Paleta.conAlfa(Paleta.ARCHIVO_ACENTO, 0.26F));
        if (this.width >= 540) {
            g.drawString(this.font, "CTRL+F  //  ESC", PANEL_X + 16, railY + 6,
                    Paleta.conAlfa(Paleta.ARCHIVO_TEXTO_TENUE, 0.56F), false);
        }
        String sello = "JOBS / MOD ARCHIVE";
        int sw = this.font.width(sello);
        if (sw + 36 < panelW) {
            g.drawString(this.font, sello, PANEL_X + panelW - sw - 16, railY + 6,
                    Paleta.conAlfa(Paleta.ARCHIVO_ACENTO, 0.56F), false);
        }
    }
}

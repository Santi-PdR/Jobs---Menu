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

/** Forge Mods convertido visualmente en un archivo técnico Jobs, sin reemplazar su lógica. */
public final class PantallaModsJobs extends ModListScreen {
    private static final int PANEL_X = 12;
    private static final int PANEL_Y = 8;
    private EditBox busqueda;

    public PantallaModsJobs(Screen anterior) { super(anterior); }

    @Override
    public void init() {
        super.init();
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
        ChromeExpediente.panelArchivo(g, PANEL_X, PANEL_Y, this.width - PANEL_X * 2, this.height - PANEL_Y * 2);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        int panelW = this.width - PANEL_X * 2;
        int panelH = this.height - PANEL_Y * 2;
        int top = PANEL_Y + 35;
        int bottom = PANEL_Y + panelH - 38;
        int split = Math.max(PANEL_X + 190, Math.min(this.width / 2, PANEL_X + panelW - 170));

        g.fill(PANEL_X + 9, top, split - 4, bottom, Paleta.conAlfa(Paleta.ARCHIVO_FONDO, 0.44F));
        g.fill(split + 4, top, PANEL_X + panelW - 9, bottom, Paleta.conAlfa(Paleta.ARCHIVO_FONDO, 0.30F));
        g.fill(split - 1, top + 4, split + 1, bottom - 4, Paleta.conAlfa(Paleta.ARCHIVO_ACENTO, 0.22F));
        g.fill(PANEL_X + 13, top + 4, PANEL_X + 16, bottom - 4, Paleta.conAlfa(Paleta.ARCHIVO_ACENTO, 0.36F));

        RenderSystem.setShaderColor(0.72F, 0.72F, 0.72F, 1.0F);
        super.render(g, mouseX, mouseY, partialTick);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        g.pose().pushPose();
        g.pose().translate(0.0F, 0.0F, 450.0F);
        g.fill(PANEL_X + 3, PANEL_Y + 1, PANEL_X + panelW - 3, PANEL_Y + 29, Paleta.VANO);
        g.pose().popPose();
        ChromeExpediente.reemplazarCabeceraArchivo(g, this.font, this.title,
                Component.translatable("jobsmenu.interfaz.mods.buscar"), PANEL_X, PANEL_Y, panelW);

        if (this.busqueda != null) {
            int x = this.busqueda.getX(), y = this.busqueda.getY(), w = this.busqueda.getWidth(), h = this.busqueda.getHeight();
            int borde = Paleta.conAlfa(Paleta.ARCHIVO_ACENTO, this.busqueda.isFocused() ? 0.72F : 0.30F);
            g.fill(x - 2, y - 2, x + w + 2, y + h + 2, borde);
            g.fill(x - 1, y - 1, x + w + 1, y + h + 1, Paleta.conAlfa(Paleta.ARCHIVO_FONDO, 0.94F));
        }

        ListasExpediente.renderarBarras(this, g);
        int railY = this.height - 28;
        g.fill(PANEL_X + 12, railY, PANEL_X + panelW - 12, railY + 1, Paleta.conAlfa(Paleta.ARCHIVO_ACENTO, 0.26F));
        g.drawString(this.font, "CTRL+F  //  ESC", PANEL_X + 16, railY + 6,
                Paleta.conAlfa(Paleta.ARCHIVO_TEXTO_TENUE, 0.56F), false);
        String sello = "JOBS / MOD ARCHIVE";
        int sw = this.font.width(sello);
        g.drawString(this.font, sello, PANEL_X + panelW - sw - 16, railY + 6,
                Paleta.conAlfa(Paleta.ARCHIVO_ACENTO, 0.56F), false);
    }
}

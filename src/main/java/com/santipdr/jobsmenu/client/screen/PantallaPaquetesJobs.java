package com.santipdr.jobsmenu.client.screen;

import com.santipdr.jobsmenu.client.ui.ChromeExpediente;
import com.santipdr.jobsmenu.client.ui.ListasExpediente;
import com.santipdr.jobsmenu.client.ui.Paleta;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.repository.PackRepository;

import java.nio.file.Path;
import java.util.function.Consumer;

/** Selector vanilla de paquetes con geometria intacta y marco Jobs exterior. */
public final class PantallaPaquetesJobs extends PackSelectionScreen {
    private static final int PANEL_X = 12;
    private static final int PANEL_Y = 8;

    public PantallaPaquetesJobs(PackRepository repo, Consumer<PackRepository> callback,
                                Path directorio, Component titulo) {
        super(repo, callback, directorio, titulo);
    }

    @Override
    protected void init() {
        super.init();

        // PackSelectionScreen administra dos listas con anchos y posiciones
        // distintos. No se les fuerza updateSize: hacerlo hacia que ambas
        // ocuparan la pantalla completa y se dibujaran una sobre otra.
        ListasExpediente.estilizar(this);
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
        ListasExpediente.renderarBarras(this, g);

        // Solo un rail exterior. No se dibujan bandejas encima de las listas
        // porque sus iconos, nombres y estados deben quedar siempre delante.
        int railY = this.height - 24;
        if (railY > PANEL_Y + 30) {
            g.fill(PANEL_X + 16, railY,
                    this.width - PANEL_X - 16, railY + 1,
                    Paleta.conAlfa(Paleta.ARCHIVO_ACENTO, 0.16F));
            String sello = "JOBS / RESOURCE ARCHIVE";
            int sw = this.font.width(sello);
            if (sw + 48 < this.width) {
                g.drawString(this.font, sello,
                        this.width - PANEL_X - sw - 18, railY + 5,
                        Paleta.conAlfa(Paleta.ARCHIVO_TEXTO_TENUE, 0.46F), false);
            }
        }
    }
}

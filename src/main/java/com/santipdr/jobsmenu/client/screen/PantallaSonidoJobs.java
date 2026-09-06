package com.santipdr.jobsmenu.client.screen;

import com.santipdr.jobsmenu.client.ui.BotonExpediente;
import com.santipdr.jobsmenu.client.ui.ChromeExpediente;
import com.santipdr.jobsmenu.client.ui.GeometriaExpediente;
import com.santipdr.jobsmenu.client.ui.ListasExpediente;
import com.santipdr.jobsmenu.client.ui.Paleta;

import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.SoundOptionsScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Field;

/** Sonido vanilla completo, presentado como expediente de instalacion Jobs. */
public final class PantallaSonidoJobs extends SoundOptionsScreen {

    private static Field campoLista;
    private static boolean campoListaResuelto;

    private OptionsList lista;
    private GeometriaExpediente.Panel panel;

    public PantallaSonidoJobs(Screen anterior, Options opciones) {
        super(anterior, opciones);
    }

    @Override
    protected void init() {
        super.init();
        this.panel = GeometriaExpediente.compacto(this.width, this.height, 430, 318);
        this.lista = resolverLista();
        if (this.lista != null) {
            this.lista.setRenderBackground(false);
            this.lista.setRenderTopAndBottom(false);
            this.lista.updateSize(this.width, this.height, panel.listaArriba(), panel.listaAbajo());
        }
        reemplazarDone();
    }

    private OptionsList resolverLista() {
        Field campo = campoLista();
        if (campo == null) return null;
        try {
            Object valor = campo.get(this);
            return valor instanceof OptionsList listaResuelta ? listaResuelta : null;
        } catch (IllegalAccessException | RuntimeException ignored) {
            return null;
        }
    }

    /**
     * La estructura de SoundOptionsScreen es estable durante toda la JVM. No
     * tiene sentido recorrer y abrir sus fields cada vez que resize recrea la
     * pantalla; el resultado se resuelve una sola vez.
     */
    private static Field campoLista() {
        if (campoListaResuelto) return campoLista;
        campoListaResuelto = true;
        try {
            for (Field campo : SoundOptionsScreen.class.getDeclaredFields()) {
                if (!campo.getType().equals(OptionsList.class)) continue;
                campo.setAccessible(true);
                campoLista = campo;
                break;
            }
        } catch (RuntimeException ignored) {
            campoLista = null;
        }
        return campoLista;
    }

    private void reemplazarDone() {
        for (var child : this.children()) {
            if (child instanceof Button b && b.getMessage().equals(CommonComponents.GUI_DONE)) {
                b.visible = false;
                b.active = false;
                break;
            }
        }
        this.addRenderableWidget(new BotonExpediente(
                this.width / 2 - 70, panel.botonY(), 140, 20,
                Component.translatable("jobsmenu.interfaz.volver"),
                BotonExpediente.Tipo.PRINCIPAL, this::onClose));
    }

    @Override
    public void renderBackground(GuiGraphics g) {
        ChromeExpediente.fondo(g, this.width, this.height);
        if (panel == null) return;

        ChromeExpediente.panel(g, panel.x(), panel.y(), panel.w(), panel.h());

        int x0 = panel.x() + 12;
        int x1 = panel.x() + panel.w() - 12;
        int y0 = panel.listaArriba() - 7;
        int y1 = panel.listaAbajo() + 5;
        int tinta = Paleta.conAlfa(Paleta.tintaSecundaria(), 0.16F);
        int tintaFina = Paleta.conAlfa(Paleta.tintaSecundaria(), 0.08F);
        int acento = Paleta.conAlfa(Paleta.UI_ACENTO, 0.42F);

        // Bandeja interior: separa mezcla de audio del marco exterior.
        g.fill(x0, y0, x1, y0 + 1, tinta);
        g.fill(x0, y1, x1, y1 + 1, tinta);
        g.fill(x0, y0, x0 + 1, y1 + 1, tintaFina);
        g.fill(x1 - 1, y0, x1, y1 + 1, tintaFina);
        g.fill(x0, y0, x0 + 3, y0 + 1, acento);

        // Marcas de canal: dan ritmo de consola sin alterar controles vanilla.
        int centro = (x0 + x1) / 2;
        g.fill(centro, y0 + 3, centro + 1, y0 + 7, tintaFina);
        g.fill(centro, y1 - 6, centro + 1, y1 - 2, tintaFina);
        for (int i = 0; i < 5; i++) {
            int mx = x0 + 8 + i * Math.max(1, (x1 - x0 - 16) / 4);
            g.fill(mx, y1 - 2, mx + 5, y1 - 1, tintaFina);
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        ListasExpediente.renderarBarras(this, g);
        ChromeExpediente.marcoSubpantalla(g, this.font, this.width, this.height,
                panel.x(), panel.y(), panel.w(), panel.h(),
                Component.translatable("jobsmenu.interfaz.sonido.subtitulo"), "AUD-020");

        // Indicadores laterales de profundidad del mezclador.
        int top = panel.listaArriba() + 4;
        int bottom = panel.listaAbajo() - 4;
        int rail = Paleta.conAlfa(Paleta.UI_ACENTO, 0.20F);
        g.fill(panel.x() + 6, top, panel.x() + 7, bottom, rail);
        g.fill(panel.x() + panel.w() - 7, top, panel.x() + panel.w() - 6, bottom, rail);
    }
}

package com.santipdr.jobsmenu.client.screen;

import com.santipdr.jobsmenu.client.ui.BotonExpediente;
import com.santipdr.jobsmenu.client.ui.ChromeExpediente;
import com.santipdr.jobsmenu.client.ui.GeometriaExpediente;

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
        try {
            for (Field f : SoundOptionsScreen.class.getDeclaredFields()) {
                if (f.getType().equals(OptionsList.class)) {
                    f.setAccessible(true);
                    Object o = f.get(this);
                    if (o instanceof OptionsList l) return l;
                }
            }
        } catch (Throwable ignored) {}
        return null;
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
        if (panel != null) ChromeExpediente.panel(g, panel.x(), panel.y(), panel.w(), panel.h());
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        ChromeExpediente.marcoSubpantalla(g, this.font, this.width, this.height,
                panel.x(), panel.y(), panel.w(), panel.h(),
                Component.translatable("jobsmenu.interfaz.sonido.subtitulo"), "AUD-012");
    }
}

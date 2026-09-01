package com.santipdr.jobsmenu.client.screen;

import com.santipdr.jobsmenu.client.ui.BotonExpediente;
import com.santipdr.jobsmenu.client.ui.ChromeExpediente;
import com.santipdr.jobsmenu.client.ui.GeometriaExpediente;

import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.SimpleOptionsSubScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

/** Opciones online basicas con la identidad administrativa del nivel. */
public final class PantallaOnlineJobs extends SimpleOptionsSubScreen {

    private GeometriaExpediente.Panel panel;

    public PantallaOnlineJobs(Screen anterior, Options opciones) {
        super(anterior, opciones, Component.translatable("options.online.title"),
                new OptionInstance<?>[] {
                        opciones.realmsNotifications(),
                        opciones.allowServerListing()
                });
    }

    @Override
    protected void init() {
        super.init();
        this.panel = GeometriaExpediente.compacto(this.width, this.height, 390, 190);
        if (this.list != null) {
            this.list.setRenderBackground(false);
            this.list.setRenderTopAndBottom(false);
            this.list.updateSize(this.width, this.height, panel.listaArriba(), panel.listaAbajo());
        }
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
                Component.translatable("jobsmenu.interfaz.online.subtitulo"), "NET-012");
    }
}

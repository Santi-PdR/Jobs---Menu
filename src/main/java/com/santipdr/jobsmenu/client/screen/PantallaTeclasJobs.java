package com.santipdr.jobsmenu.client.screen;

import com.santipdr.jobsmenu.client.ui.BotonExpediente;
import com.santipdr.jobsmenu.client.ui.ChromeExpediente;
import com.santipdr.jobsmenu.client.ui.ListasExpediente;

import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.controls.KeyBindsScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

/** Lista de teclas vanilla preservada, presentada como registro Jobs. */
public final class PantallaTeclasJobs extends KeyBindsScreen {

    public PantallaTeclasJobs(Screen anterior, Options opciones) {
        super(anterior, opciones);
    }

    @Override
    protected void init() {
        super.init();
        ListasExpediente.estilizar(this, 50, this.height - 42);
        for (var child : this.children()) {
            if (child instanceof Button b && b.getMessage().equals(CommonComponents.GUI_DONE)) {
                b.visible = false;
                b.active = false;
                break;
            }
        }
        this.addRenderableWidget(new BotonExpediente(
                this.width / 2 - 70, this.height - 28, 140, 20,
                Component.translatable("jobsmenu.interfaz.volver"),
                BotonExpediente.Tipo.PRINCIPAL, this::onClose));
    }

    @Override
    public void renderBackground(GuiGraphics g) {
        ChromeExpediente.fondo(g, this.width, this.height);
        ChromeExpediente.panel(g, 8, 6, this.width - 16, this.height - 12);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        ChromeExpediente.marcoSubpantalla(g, this.font, this.width, this.height,
                8, 6, this.width - 16, this.height - 12,
                Component.translatable("jobsmenu.interfaz.teclas.subtitulo"), "KEY-012");
    }
}

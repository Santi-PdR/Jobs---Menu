package com.santipdr.jobsmenu.client.screen;

import com.santipdr.jobsmenu.client.ui.BotonExpediente;
import com.santipdr.jobsmenu.client.ui.ChromeExpediente;
import com.santipdr.jobsmenu.config.ConfigTurno;

import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.AccessibilityOptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

/** Accesibilidad vanilla completa dentro del expediente Jobs. */
public final class PantallaAccesibilidadJobs extends AccessibilityOptionsScreen {

    public PantallaAccesibilidadJobs(Screen anterior, Options opciones) {
        super(anterior, opciones);
    }

    private static OptionInstance<Boolean> interruptor(String clave, boolean valor,
                                                       java.util.function.Consumer<Boolean> fijar) {
        return OptionInstance.createBoolean(clave,
                OptionInstance.cachedConstantTooltip(Component.translatable(clave + ".detalle")),
                valor, fijar::accept);
    }

    @Override
    protected void init() {
        super.init();
        if (this.list != null) {
            this.list.addSmall(
                    interruptor("jobsmenu.ajustes.movimiento",
                            ConfigTurno.movimientoReducido(), ConfigTurno::fijarMovimientoReducido),
                    interruptor("jobsmenu.ajustes.destellos",
                            ConfigTurno.destellosReducidos(), ConfigTurno::fijarDestellosReducidos));
            this.list.addSmall(
                    interruptor("jobsmenu.ajustes.alto",
                            ConfigTurno.altoContraste(), ConfigTurno::fijarAltoContraste),
                    interruptor("jobsmenu.ajustes.grande",
                            ConfigTurno.textoGrande(), ConfigTurno::fijarTextoGrande));

            this.list.setRenderBackground(false);
            this.list.setRenderTopAndBottom(false);
            this.list.setRenderSelection(false);
            // Mas aire arriba y abajo: cabecera, ultima fila, scrollbar y Volver
            // ya no comparten pixeles incluso en GUI scale alto.
            this.list.updateSize(this.width, this.height, 54, this.height - 50);
        }

        // Ocultamos cualquier Done que haya creado vanilla, no solo el primero.
        for (var child : this.children()) {
            if (child instanceof Button b && b.getMessage().equals(CommonComponents.GUI_DONE)) {
                b.visible = false;
                b.active = false;
            }
        }

        this.addRenderableWidget(new BotonExpediente(
                this.width / 2 - 70, this.height - 30, 140, 20,
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
                Component.translatable("jobsmenu.interfaz.accesibilidad.subtitulo"), "ACC-013");
    }

    @Override
    public void removed() {
        ConfigTurno.guardarPendiente();
        super.removed();
    }
}

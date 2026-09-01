package com.santipdr.jobsmenu.client.screen;

import com.santipdr.jobsmenu.client.ui.BotonExpediente;
import com.santipdr.jobsmenu.client.ui.ChromeExpediente;
import com.santipdr.jobsmenu.client.ui.GeometriaExpediente;
import com.santipdr.jobsmenu.config.ConfigTurno;

import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.AccessibilityOptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Accesibilidad vanilla completa dentro del expediente Jobs. */
public final class PantallaAccesibilidadJobs extends AccessibilityOptionsScreen {

    private GeometriaExpediente.Panel panel;

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
        this.panel = GeometriaExpediente.compacto(this.width, this.height, 430, 318);
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
            // La cabecera Jobs y el pie propio tienen una zona exclusiva. Esto evita
            // que la ultima fila, la scrollbar o un control auxiliar caigan debajo del
            // boton de cierre incluso con GUI scale alto.
            this.list.updateSize(this.width, this.height, panel.listaArriba() + 8, panel.listaAbajo() - 8);
        }

        // AccessibilityOptionsScreen crea dos acciones de pie: Done y el enlace a la
        // guia de accesibilidad. Dentro del expediente Jobs ambas son chrome vanilla
        // duplicado; la guia era precisamente el boton que se montaba sobre Cerrar.
        // Los botones de opciones viven dentro de OptionsList, por lo que podemos
        // retirar con seguridad todos los Button directos de la franja inferior.
        for (var child : this.children()) {
            if (child instanceof Button b && b.getY() >= panel.listaAbajo() - 4) {
                b.visible = false;
                b.active = false;
            }
        }

        this.addRenderableWidget(new BotonExpediente(
                this.width / 2 - 76, panel.botonY() - 2, 152, 21,
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
                Component.translatable("jobsmenu.interfaz.accesibilidad.subtitulo"), "ACC-014");
    }

    @Override
    public void removed() {
        ConfigTurno.guardarPendiente();
        super.removed();
    }
}

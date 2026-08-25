package com.santipdr.jobsmenu.client.ui;

import com.santipdr.jobsmenu.config.ConfigTurno;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

/**
 * Un renglon del tablon de turnos.
 *
 * No es una capsula de boton: es una linea de planilla, con su numero de orden
 * a la izquierda y una barra ambar que se enciende cuando el cursor la alcanza.
 */
public class RenglonTablon extends AbstractButton {

    /** Ancho de la barra de foco, en pixeles. */
    private static final int ANCHO_BARRA = 2;

    /** Sangria de la etiqueta respecto del borde izquierdo. */
    private static final int SANGRIA_ETIQUETA = 28;

    /** Sangria del numero de orden. */
    private static final int SANGRIA_ORDEN = 11;

    private final String orden;
    private final Runnable accion;

    private float foco;

    public RenglonTablon(int x, int y, int ancho, int alto, String orden, Component etiqueta, Runnable accion) {
        super(x, y, ancho, alto, etiqueta);
        this.orden = orden;
        this.accion = accion;
        this.foco = 0.0F;
    }

    @Override
    public void onPress() {
        this.accion.run();
    }

    @Override
    public void renderWidget(GuiGraphics grafico, int ratonX, int ratonY, float parcial) {
        Minecraft cliente = Minecraft.getInstance();

        float objetivo = this.isHoveredOrFocused() ? 1.0F : 0.0F;
        if (ConfigTurno.movimientoReducido() || !ConfigTurno.escenaViva()) {
            this.foco = objetivo;
        } else {
            this.foco = this.foco + (objetivo - this.foco) * 0.25F;
            if (Math.abs(objetivo - this.foco) < 0.02F) {
                this.foco = objetivo;
            }
        }

        int x = this.getX();
        int y = this.getY();
        int ancho = this.getWidth();
        int alto = this.getHeight();

        grafico.fill(x, y, x + ancho, y + alto, Paleta.conAlfa(Paleta.HORMIGON, 0.55F + 0.30F * this.foco));
        grafico.fill(x, y, x + ancho, y + 1, Paleta.conAlfa(Paleta.HUMO, 0.60F));
        grafico.fill(x, y + alto - 1, x + ancho, y + alto, Paleta.conAlfa(Paleta.HUMO, 0.60F));

        int colorBarra = Paleta.mezclar(Paleta.SODIO_TENUE, Paleta.SODIO, this.foco);
        grafico.fill(x, y, x + ANCHO_BARRA, y + alto, Paleta.conAlfa(colorBarra, 0.35F + 0.65F * this.foco));

        int lineaBase = y + (alto - 8) / 2;
        int colorOrden = Paleta.mezclar(Paleta.HUESO_TENUE, Paleta.SODIO, this.foco);
        int colorEtiqueta = Paleta.mezclar(Paleta.HUESO_TENUE, Paleta.HUESO, this.foco);

        grafico.drawString(cliente.font, this.orden, x + SANGRIA_ORDEN, lineaBase, colorOrden, false);
        grafico.drawString(cliente.font, this.getMessage(), x + SANGRIA_ETIQUETA, lineaBase, colorEtiqueta, false);
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput salida) {
        this.defaultButtonNarrationText(salida);
    }
}

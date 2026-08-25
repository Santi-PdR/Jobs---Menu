package com.santipdr.jobsmenu.client.ui;

import com.santipdr.jobsmenu.config.ConfigTurno;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

/**
 * Un renglon del listado de turnos, tal como esta impreso en el aviso.
 *
 * No es una capsula de boton: es una linea de formulario con su numero de
 * orden, sus puntos suspensivos de relleno y una casilla al margen. Al pasar
 * el cursor, la casilla queda marcada.
 */
public class RenglonTablon extends AbstractButton {

    /** Lado de la casilla marcable, en pixeles. */
    private static final int LADO_CASILLA = 7;

    /** Sangria del numero de orden respecto del borde izquierdo. */
    private static final int SANGRIA_ORDEN = 14;

    /** Sangria de la etiqueta respecto del borde izquierdo. */
    private static final int SANGRIA_ETIQUETA = 32;

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
        int lineaBase = y + (alto - 8) / 2;

        // Al enfocar, el renglon se resalta como si lo hubiesen repasado a lapiz.
        if (this.foco > 0.0F) {
            grafico.fill(x - 3, y, x + ancho + 3, y + alto,
                    Paleta.conAlfa(Paleta.TINTA_TENUE, 0.14F * this.foco));
        }

        // Casilla al margen: vacia en reposo, marcada al enfocar.
        int casillaY = y + (alto - LADO_CASILLA) / 2;
        dibujarMarco(grafico, x, casillaY, LADO_CASILLA, Paleta.conAlfa(Paleta.TINTA_TENUE, 0.70F));
        if (this.foco > 0.35F) {
            grafico.fill(x + 2, casillaY + 2, x + LADO_CASILLA - 1, casillaY + LADO_CASILLA - 1,
                    Paleta.conAlfa(Paleta.TINTA, 0.60F + 0.40F * this.foco));
        }

        int colorOrden = Paleta.conAlfa(Paleta.TINTA_TENUE, 0.70F + 0.30F * this.foco);
        int colorEtiqueta = Paleta.mezclar(Paleta.TINTA_TENUE, Paleta.TINTA, this.foco);

        grafico.drawString(cliente.font, this.orden, x + SANGRIA_ORDEN, lineaBase, colorOrden, false);
        grafico.drawString(cliente.font, this.getMessage(), x + SANGRIA_ETIQUETA, lineaBase, colorEtiqueta, false);

        // Puntos de relleno hasta el margen derecho, como en un formulario.
        int inicioPuntos = x + SANGRIA_ETIQUETA + cliente.font.width(this.getMessage()) + 4;
        int finPuntos = x + ancho - 2;
        for (int px = inicioPuntos; px < finPuntos; px += 3) {
            grafico.fill(px, lineaBase + 6, px + 1, lineaBase + 7,
                    Paleta.conAlfa(Paleta.TINTA_TENUE, 0.28F + 0.22F * this.foco));
        }
    }

    private static void dibujarMarco(GuiGraphics grafico, int x, int y, int lado, int color) {
        grafico.fill(x, y, x + lado, y + 1, color);
        grafico.fill(x, y + lado - 1, x + lado, y + lado, color);
        grafico.fill(x, y, x + 1, y + lado, color);
        grafico.fill(x + lado - 1, y, x + lado, y + lado, color);
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput salida) {
        this.defaultButtonNarrationText(salida);
    }
}

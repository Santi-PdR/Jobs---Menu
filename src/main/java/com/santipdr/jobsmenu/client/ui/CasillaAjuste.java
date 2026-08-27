package com.santipdr.jobsmenu.client.ui;

import com.santipdr.jobsmenu.client.scene.RotacionNiveles;
import com.santipdr.jobsmenu.client.sound.MezclaAudio;
import com.santipdr.jobsmenu.client.sound.SonidosNivel;
import com.santipdr.jobsmenu.config.ConfigTurno;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * Una condicion de estancia que se marca o se desmarca, impresa en la hoja.
 *
 * Es hermana de {@link RenglonTablon}, pero con una diferencia de fondo: el
 * renglon del tablon lleva a otra pantalla y su casilla solo se marca mientras
 * el cursor esta encima; esta casilla GUARDA UN ESTADO. Marcada quiere decir
 * activado, y se queda marcada cuando el cursor se va, porque representa una
 * eleccion del ocupante y no un resalte de foco.
 *
 * Por lo demas habla el mismo idioma que el resto de la hoja: la casilla al
 * margen, la etiqueta, los puntos de relleno, el corrimiento al enfocar y el
 * apagarse con la luz del pasillo. Debajo de la etiqueta puede llevar una linea
 * de letra chica que explica que hace, en la voz de la administracion.
 */
public class CasillaAjuste extends AbstractButton {

    /** Lado de la casilla marcable, en pixeles. */
    private static final int LADO_CASILLA = 7;

    /** Sangria de la etiqueta respecto del borde izquierdo. */
    private static final int SANGRIA_ETIQUETA = 16;

    /** Cuanto se corre el renglon al enfocarlo. */
    private static final float DESPLAZAMIENTO = 3.0F;

    /** Cuanto se acerca el foco a su destino en cada fotograma. */
    private static final float SUAVIZADO = 0.25F;

    private final BooleanSupplier estado;
    private final Consumer<Boolean> alCambiar;

    /** Letra chica bajo la etiqueta, o null si no lleva. */
    private final Component detalle;

    private float foco;
    private boolean sonaba;

    public CasillaAjuste(int x, int y, int ancho, int alto, Component etiqueta,
                         Component detalle, BooleanSupplier estado, Consumer<Boolean> alCambiar) {
        super(x, y, ancho, alto, etiqueta);
        this.detalle = detalle;
        this.estado = estado;
        this.alCambiar = alCambiar;
        this.foco = 0.0F;
        this.sonaba = false;
    }

    /** Sin el clac de fabrica: el gesto propio va en onPress(). */
    @Override
    public void playDownSound(net.minecraft.client.sounds.SoundManager gestor) {
    }

    @Override
    public void onPress() {
        boolean nuevo = !this.estado.getAsBoolean();
        this.alCambiar.accept(nuevo);
        // Marcar y desmarcar no son el mismo gesto: uno pone una condicion, el
        // otro la levanta. Se distinguen con el tono, como el resto de la hoja.
        MezclaAudio.gesto(nuevo ? SonidosNivel.UI_ELEGIR : SonidosNivel.UI_ALTERNAR, 0.80F);
    }

    @Override
    public void renderWidget(GuiGraphics grafico, int ratonX, int ratonY, float parcial) {
        Minecraft cliente = Minecraft.getInstance();

        boolean encima = this.isHoveredOrFocused() && this.active;
        if (encima && !this.sonaba) {
            MezclaAudio.gesto(SonidosNivel.UI_PASAR, 0.55F);
        }
        this.sonaba = encima;

        float objetivo = encima ? 1.0F : 0.0F;
        if (ConfigTurno.movimientoReducido() || !ConfigTurno.escenaViva()) {
            this.foco = objetivo;
        } else {
            this.foco += (objetivo - this.foco) * SUAVIZADO;
            if (Math.abs(objetivo - this.foco) < 0.02F) {
                this.foco = objetivo;
            }
        }

        boolean marcada = this.estado.getAsBoolean();

        int x = this.getX() + (int) (DESPLAZAMIENTO * this.foco);
        int y = this.getY();
        int ancho = this.getWidth();
        int alto = this.getHeight();

        float tinta = this.active ? 1.0F : 0.40F;
        tinta *= 0.10F + 0.90F * RotacionNiveles.luzDisponible();

        // Resalte de foco: el mismo lapiz que repasa los renglones del tablon.
        if (this.foco > 0.0F) {
            grafico.fill(this.getX() - 3, y, this.getX() + ancho + 3, y + alto,
                    Paleta.conAlfa(Paleta.TINTA_TENUE, 0.14F * this.foco * tinta));
            grafico.fill(this.getX() - 5, y + 2, this.getX() - 4, y + alto - 2,
                    Paleta.conAlfa(Paleta.TINTA, 0.55F * this.foco * tinta));
        }

        // La casilla: su marca depende del ESTADO, no del foco. Marcada = activo.
        int casillaY = y + 3;
        dibujarMarco(grafico, x, casillaY, LADO_CASILLA,
                Paleta.conAlfa(Paleta.TINTA_TENUE, 0.70F * tinta));
        if (marcada) {
            grafico.fill(x + 2, casillaY + 2,
                    x + LADO_CASILLA - 2, casillaY + LADO_CASILLA - 2,
                    Paleta.conAlfa(Paleta.TINTA, 0.90F * tinta));
        }

        int colorEtiqueta = Paleta.conAlfa(
                Paleta.mezclar(Paleta.TINTA_TENUE, Paleta.TINTA, marcada ? 1.0F : 0.45F + 0.30F * this.foco),
                tinta);
        grafico.drawString(cliente.font, this.getMessage(), x + SANGRIA_ETIQUETA, y, colorEtiqueta, false);

        // Puntos de relleno hasta el margen: el mismo remate que el tablon.
        puntosDeRelleno(grafico, cliente, x, ancho, y, tinta);

        // La letra chica, si la lleva: explica el ajuste sin gritarlo.
        if (this.detalle != null) {
            int anchoDetalle = ancho - SANGRIA_ETIQUETA;
            int dy = y + 11;
            for (FormattedCharSequence linea : cliente.font.split(this.detalle, anchoDetalle)) {
                grafico.drawString(cliente.font, linea, x + SANGRIA_ETIQUETA, dy,
                        Paleta.conAlfa(Paleta.TINTA_TENUE, 0.55F * tinta), false);
                dy += 9;
            }
        }
    }

    /** Los puntos que van del final de la etiqueta al margen derecho. */
    private void puntosDeRelleno(GuiGraphics grafico, Minecraft cliente,
                                 int x, int ancho, int y, float tinta) {
        int inicio = x + SANGRIA_ETIQUETA + cliente.font.width(this.getMessage()) + 4;
        int fin = x + ancho - 2;
        if (fin <= inicio) {
            return;
        }
        float largo = fin - inicio;
        for (int px = inicio; px < fin; px += 3) {
            float posicion = (px - inicio) / largo;
            float local = Math.max(0.0F, Math.min(1.0F, (this.foco - posicion * 0.55F) / 0.45F));
            grafico.fill(px, y + 7, px + 1, y + 8,
                    Paleta.conAlfa(Paleta.TINTA_TENUE, (0.24F + 0.24F * local) * tinta));
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
        salida.add(net.minecraft.client.gui.narration.NarratedElementType.TITLE,
                Component.translatable(this.estado.getAsBoolean()
                        ? "jobsmenu.opciones.narracion.marcado"
                        : "jobsmenu.opciones.narracion.desmarcado", this.getMessage()));
    }

    /** Para medir la altura que necesita una casilla con su detalle. */
    public static int altoConDetalle(Component detalle, int ancho, net.minecraft.client.gui.Font font) {
        int base = 11;
        if (detalle == null) {
            return base + 4;
        }
        List<FormattedCharSequence> lineas = font.split(detalle, ancho - SANGRIA_ETIQUETA);
        return base + Math.max(1, lineas.size()) * 9 + 3;
    }
}

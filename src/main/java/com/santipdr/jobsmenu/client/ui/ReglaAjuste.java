package com.santipdr.jobsmenu.client.ui;

import com.santipdr.jobsmenu.client.scene.RotacionNiveles;
import com.santipdr.jobsmenu.client.sound.MezclaAudio;
import com.santipdr.jobsmenu.client.sound.SonidosNivel;
import com.santipdr.jobsmenu.config.ConfigTurno;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
import java.util.function.IntSupplier;

/**
 * Un volumen anotado a mano al margen de la hoja, como una regla graduada.
 *
 * No es el deslizador de barra rellena del juego: eso es lenguaje de panel de
 * control, y aca no hay panel, hay una hoja. Es una regla impresa -una linea
 * con marcas cada diez- y una corredera de papel que el ocupante arrastra. El
 * numero exacto va a la derecha, en la letra chica de siempre.
 *
 * Se apoya en AbstractSliderButton solo por el arrastre y el teclado, que ya
 * estan resueltos ahi; todo lo que se ve esta redibujado a mano para que hable
 * el idioma del aviso. El valor vive en la config: leer y escribir pasan por
 * ConfigTurno, que ademas lo guarda en el .toml.
 */
public class ReglaAjuste extends AbstractSliderButton {

    /** Sangria de la etiqueta respecto del borde izquierdo. */
    private static final int SANGRIA_ETIQUETA = 16;

    private final Component etiqueta;
    private final Consumer<Integer> alCambiar;

    /** Ultimo porcentaje que se sono, para no chasquear en cada pixel. */
    private int ultimoSonado;

    public ReglaAjuste(int x, int y, int ancho, int alto, Component etiqueta,
                       IntSupplier valor, Consumer<Integer> alCambiar) {
        super(x, y, ancho, alto, Component.empty(), valor.getAsInt() / 100.0D);
        this.etiqueta = etiqueta;
        this.alCambiar = alCambiar;
        this.ultimoSonado = valor.getAsInt();
    }

    private int porcentaje() {
        return (int) Math.round(this.value * 100.0D);
    }

    /** No hay texto de fabrica: el rotulo se dibuja a mano. */
    @Override
    protected void updateMessage() {
    }

    @Override
    protected void applyValue() {
        int p = porcentaje();
        this.alCambiar.accept(p);
        // Un chasquido de papel cada diez unidades: suficiente para sentir la
        // graduacion, no tanto como para que el arrastre suene a matraca.
        if (Math.abs(p - this.ultimoSonado) >= 10) {
            this.ultimoSonado = p;
            MezclaAudio.gesto(SonidosNivel.UI_PASAR, 0.35F);
        }
    }

    @Override
    public void renderWidget(GuiGraphics grafico, int ratonX, int ratonY, float parcial) {
        Minecraft cliente = Minecraft.getInstance();

        int x = this.getX();
        int y = this.getY();
        int ancho = this.getWidth();
        int alto = this.getHeight();

        boolean encima = this.isHoveredOrFocused();
        float tinta = 0.10F + 0.90F * RotacionNiveles.luzDisponible();

        int p = porcentaje();
        Component numero = Component.literal(p + "%");
        int anchoNumero = cliente.font.width(numero);

        // La etiqueta a la izquierda, en tinta plena si el foco esta encima.
        int colorEtiqueta = Paleta.conAlfa(
                Paleta.mezclar(Paleta.TINTA_TENUE, Paleta.TINTA, encima ? 1.0F : 0.6F), tinta);
        grafico.drawString(cliente.font, this.etiqueta, x + SANGRIA_ETIQUETA, y, colorEtiqueta, false);

        // El numero exacto, alineado al borde derecho.
        grafico.drawString(cliente.font, numero, x + ancho - anchoNumero, y,
                Paleta.conAlfa(Paleta.TINTA_TENUE, 0.75F * tinta), false);

        // La regla: una linea horizontal con marcas cada diez, bajo el texto.
        int reglaY = y + 12;
        int reglaX0 = x + SANGRIA_ETIQUETA;
        int reglaX1 = x + ancho - 2;
        int largo = reglaX1 - reglaX0;
        if (largo < 10) {
            return;
        }

        grafico.fill(reglaX0, reglaY, reglaX1, reglaY + 1,
                Paleta.conAlfa(Paleta.TINTA_TENUE, 0.55F * tinta));
        for (int i = 0; i <= 10; i++) {
            int mx = reglaX0 + Math.round(largo * (i / 10.0F));
            int altoMarca = (i % 5 == 0) ? 3 : 2;
            grafico.fill(mx, reglaY - altoMarca, mx + 1, reglaY,
                    Paleta.conAlfa(Paleta.TINTA_TENUE, 0.55F * tinta));
        }

        // La corredera: un trozo de papel con su sombra, sobre la marca actual.
        int corr = reglaX0 + Math.round(largo * (float) this.value);
        int ladoAncho = 3;
        int ladoAlto = 5;
        float realce = encima ? 1.0F : 0.80F;
        grafico.fill(corr - ladoAncho + 1, reglaY - ladoAlto + 1,
                corr + ladoAncho + 1, reglaY + ladoAlto + 1,
                Paleta.conAlfa(Paleta.VANO, 0.30F * tinta));
        grafico.fill(corr - ladoAncho, reglaY - ladoAlto, corr + ladoAncho, reglaY + ladoAlto,
                Paleta.conAlfa(Paleta.PAPEL, realce * tinta));
        // Un trazo vertical en el centro de la corredera: la marca de lectura.
        grafico.fill(corr, reglaY - ladoAlto + 1, corr + 1, reglaY + ladoAlto - 1,
                Paleta.conAlfa(Paleta.TINTA_TENUE, 0.70F * tinta));
    }
}

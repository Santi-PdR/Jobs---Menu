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

/**
 * Un renglon del listado de turnos, tal como esta impreso en el aviso.
 *
 * No es una capsula de boton: es una linea de formulario con su numero de
 * orden, sus puntos suspensivos de relleno y una casilla al margen. Al pasar el
 * cursor, la casilla queda marcada.
 *
 * LOS DETALLES QUE NO SE MIRAN
 *
 * Todo lo que pasa al enfocar un renglon esta medido para notarse sin verse:
 *
 *  - el renglon entero se corre DESPLAZAMIENTO pixeles a la derecha, como si
 *    el dedo lo hubiese empujado;
 *  - la casilla se marca desde el centro hacia afuera, no de golpe;
 *  - los puntos de relleno se oscurecen de izquierda a derecha, con un retardo
 *    proporcional a la distancia, asi el repaso parece hecho a mano;
 *  - el numero de orden gana peso, la etiqueta gana tinta.
 *
 * Cada uno por separado es invisible. Juntos son la diferencia entre una lista
 * y una lista que responde.
 */
public class RenglonTablon extends AbstractButton {

    /** Lado de la casilla marcable, en pixeles. */
    private static final int LADO_CASILLA = 7;

    /** Sangria del numero de orden respecto del borde izquierdo. */
    private static final int SANGRIA_ORDEN = 14;

    /** Sangria de la etiqueta respecto del borde izquierdo. */
    private static final int SANGRIA_ETIQUETA = 32;

    /** Cuanto se corre el renglon al enfocarlo. Tres pixeles y ni uno mas. */
    private static final float DESPLAZAMIENTO = 3.0F;

    /** Cuanto se acerca el foco a su destino en cada fotograma. */
    private static final float SUAVIZADO = 0.25F;

    private final String orden;
    private final Runnable accion;

    /** Si el renglon lleva a una pantalla o ejecuta algo sin vuelta atras. */
    private final boolean terminal;

    private float foco;

    /** Para no repetir el gesto de roce en cada fotograma que el cursor este encima. */
    private boolean sonaba;

    public RenglonTablon(int x, int y, int ancho, int alto, String orden,
                         Component etiqueta, Runnable accion, boolean terminal) {
        super(x, y, ancho, alto, etiqueta);
        this.orden = orden;
        this.accion = accion;
        this.terminal = terminal;
        this.foco = 0.0F;
        this.sonaba = false;
    }

    /**
     * Silencio del click de fabrica.
     *
     * AbstractWidget reproduce UI_BUTTON_CLICK - el "clac" de madera del menu
     * vanilla - antes de llamar a onPress(). Si no se anula, cada renglon suena
     * dos veces: el clac generico primero y el sello del mod detras. Es
     * exactamente el sonido que el aviso no quiere tener, y ademas delata que
     * abajo hay un boton comun. El gesto propio se dispara en onPress().
     */
    @Override
    public void playDownSound(net.minecraft.client.sounds.SoundManager gestor) {
    }

    @Override
    public void onPress() {
        // Lo que cierra la sesion suena distinto de lo que abre una pantalla.
        MezclaAudio.gesto(this.terminal ? SonidosNivel.UI_CONFIRMAR : SonidosNivel.UI_ELEGIR, 0.85F);
        this.accion.run();
    }

    /**
     * El renglon apagado tambien responde, y por eso existe el sonido de
     * accion invalida.
     *
     * AbstractWidget descarta el click cuando active es false: no llama a
     * onClick ni a onPress, y el widget se queda mudo. Un renglon que no hace
     * nada Y no dice nada se lee como una pantalla colgada, que es peor que
     * una negativa. Interceptando el click aca, el rele intenta cerrar y no
     * engancha: la maquina contesta que ahora no.
     */
    @Override
    public boolean mouseClicked(double ratonX, double ratonY, int boton) {
        if (!this.active && this.visible && boton == 0 && this.isMouseOver(ratonX, ratonY)) {
            MezclaAudio.gesto(SonidosNivel.UI_NEGADO, 0.70F);
            return true;
        }
        return super.mouseClicked(ratonX, ratonY, boton);
    }

    @Override
    public void renderWidget(GuiGraphics grafico, int ratonX, int ratonY, float parcial) {
        Minecraft cliente = Minecraft.getInstance();

        boolean encima = this.isHoveredOrFocused() && this.active;
        if (encima && !this.sonaba) {
            MezclaAudio.gesto(SonidosNivel.UI_PASAR, 0.60F);
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

        int x = this.getX() + (int) (DESPLAZAMIENTO * this.foco);
        int y = this.getY();
        int ancho = this.getWidth();
        int alto = this.getHeight();
        int lineaBase = y + (alto - 8) / 2;

        // Un renglon inactivo no se pinta de gris: se destine, como una fotocopia
        // que salio floja. El gris plano es lenguaje de formulario web.
        float tinta = this.active ? 1.0F : 0.40F;

        // Y cuando se corta la luz, la hoja se apaga con el pasillo. Sin esto
        // los renglones quedan flotando legibles en la oscuridad, que es el
        // detalle que rompe todo el apagon: la tinta no se lee sola.
        tinta *= 0.10F + 0.90F * RotacionNiveles.luzDisponible();

        // Al enfocar, el renglon se resalta como si lo hubiesen repasado a lapiz.
        if (this.foco > 0.0F) {
            grafico.fill(this.getX() - 3, y, this.getX() + ancho + 3, y + alto,
                    Paleta.conAlfa(Paleta.TINTA_TENUE, 0.14F * this.foco * tinta));
            // Marca al margen izquierdo, del alto exacto del renglon.
            grafico.fill(this.getX() - 5, y + 2, this.getX() - 4, y + alto - 2,
                    Paleta.conAlfa(Paleta.TINTA, 0.55F * this.foco * tinta));
        }

        // Casilla al margen: vacia en reposo, marcada al enfocar.
        int casillaY = y + (alto - LADO_CASILLA) / 2;
        dibujarMarco(grafico, x, casillaY, LADO_CASILLA,
                Paleta.conAlfa(Paleta.TINTA_TENUE, 0.70F * tinta));
        if (this.foco > 0.20F) {
            // La marca crece desde el centro: no aparece, se hace.
            float crecida = Math.min(1.0F, (this.foco - 0.20F) / 0.55F);
            int margen = Math.round(2.0F + (1.0F - crecida) * 1.5F);
            grafico.fill(x + margen, casillaY + margen,
                    x + LADO_CASILLA - margen, casillaY + LADO_CASILLA - margen,
                    Paleta.conAlfa(Paleta.TINTA, (0.55F + 0.45F * crecida) * tinta));
        }

        int colorOrden = Paleta.conAlfa(Paleta.TINTA_TENUE, (0.70F + 0.30F * this.foco) * tinta);
        int colorEtiqueta = Paleta.conAlfa(
                Paleta.mezclar(Paleta.TINTA_TENUE, Paleta.TINTA, this.foco), tinta);

        grafico.drawString(cliente.font, this.orden, x + SANGRIA_ORDEN, lineaBase, colorOrden, false);
        grafico.drawString(cliente.font, this.getMessage(), x + SANGRIA_ETIQUETA, lineaBase,
                colorEtiqueta, false);

        puntosDeRelleno(grafico, cliente, x, ancho, lineaBase, tinta);
    }

    /**
     * Los puntos que van de la etiqueta al margen derecho.
     *
     * Se oscurecen en cascada de izquierda a derecha: cada punto espera su
     * turno segun lo lejos que este. El repaso tarda lo mismo que la animacion
     * del foco, asi que se lee como un solo gesto y no como dos.
     */
    private void puntosDeRelleno(GuiGraphics grafico, Minecraft cliente,
                                 int x, int ancho, int lineaBase, float tinta) {
        int inicio = x + SANGRIA_ETIQUETA + cliente.font.width(this.getMessage()) + 4;
        int fin = x + ancho - 2;
        if (fin <= inicio) {
            return;
        }

        float largo = fin - inicio;
        for (int px = inicio; px < fin; px += 3) {
            float posicion = (px - inicio) / largo;
            // El punto se enciende cuando el foco supera su posicion en la fila.
            float local = Math.max(0.0F, Math.min(1.0F, (this.foco - posicion * 0.55F) / 0.45F));
            grafico.fill(px, lineaBase + 6, px + 1, lineaBase + 7,
                    Paleta.conAlfa(Paleta.TINTA_TENUE, (0.28F + 0.24F * local) * tinta));
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


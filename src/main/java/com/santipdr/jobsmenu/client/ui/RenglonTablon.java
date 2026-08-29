package com.santipdr.jobsmenu.client.ui;

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
    private static final float DESPLAZAMIENTO = 2.0F;

    /** Cuanto se acerca el foco a su destino en cada fotograma. */
    private static final float SUAVIZADO = 0.25F;

    private final String orden;
    private final Runnable accion;

    /** Si el renglon lleva a una pantalla o ejecuta algo sin vuelta atras. */
    private final boolean terminal;

    private float foco;

    /** Luz del snapshot que la pantalla capturo al principio del frame. */
    private float luzFrame = 1.0F;

    /** Para no repetir el gesto de roce en cada fotograma que el cursor este encima. */
    private boolean sonaba;

    /** Marca visual breve de una pulsacion aceptada, tambien por teclado. */
    private long presionadoHasta;

    /** Medida de etiqueta reutilizable mientras no cambia texto, fuente o ancho. */
    private Component etiquetaMedida = Component.empty();
    private int anchoEtiquetaMedida = -1;
    private int anchoWidgetMedido = -1;

    public RenglonTablon(int x, int y, int ancho, int alto, String orden,
                         Component etiqueta, Runnable accion, boolean terminal) {
        super(x, y, ancho, alto, etiqueta);
        this.orden = orden;
        this.accion = accion;
        this.terminal = terminal;
        this.foco = 0.0F;
        this.sonaba = false;
        this.presionadoHasta = 0L;
    }

    /** Actualiza la luz compartida sin hacer otra lectura del reloj por renglon. */
    public void setLuzFrame(float luz) {
        this.luzFrame = Math.max(0.0F, Math.min(1.0F, luz));
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
        this.presionadoHasta = System.currentTimeMillis() + 180L;
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
        float presion = 0.0F;
        if (this.presionadoHasta > 0L) {
            long restante = this.presionadoHasta - System.currentTimeMillis();
            if (restante > 0L) {
                presion = Math.min(1.0F, restante / 180.0F);
            } else {
                this.presionadoHasta = 0L;
            }
        }

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
        float escala = ConfigTurno.textoGrande() ? 1.15F : 1.0F;
        int altoTexto = Math.round(8.0F * escala);
        int lineaBase = y + Math.max(0, (alto - altoTexto) / 2);

        // Un renglon inactivo no se pinta de gris: se destine, como una fotocopia
        // que salio floja. El gris plano es lenguaje de formulario web.
        float tinta = this.active ? 1.0F : 0.40F;
        if (ConfigTurno.altoContraste() && this.active) {
            tinta = 1.0F;
        }

        // Y cuando se corta la luz, la hoja se apaga con el pasillo. Sin esto
        // los renglones quedan flotando legibles en la oscuridad, que es el
        // detalle que rompe todo el apagon: la tinta no se lee sola.
        tinta *= 0.10F + 0.90F * this.luzFrame;

        // Al enfocar, el renglon se resalta como si lo hubiesen repasado a lapiz.
        if (this.foco > 0.0F) {
            // La respuesta visual ocupa exactamente la hitbox. Antes sobresalia
            // cinco pixeles por la izquierda y tres por la derecha, de modo que
            // zonas que parecian activas no eran clicables.
            if (ConfigTurno.guiaLectura()) {
                grafico.fill(this.getX(), y, this.getX() + ancho, y + alto,
                        Paleta.conAlfa(Paleta.tintaSecundaria(), 0.14F * this.foco * tinta));
            }
            // Marca dentro del margen izquierdo de la propia region.
            grafico.fill(this.getX(), y + 2, this.getX() + 1, y + alto - 2,
                    Paleta.conAlfa(Paleta.tintaPrincipal(), 0.55F * this.foco * tinta));
        }

        // La salida no usa rojo (reservado para Executores), pero tampoco debe
        // parecer una opcion normal. Una doble regla sobria comunica que la
        // accion abandona la estancia sin cambiar hitbox ni comportamiento.
        float intensidadTerminal = Math.max(this.foco, presion);
        if (this.terminal && intensidadTerminal > 0.0F) {
            int borde = Paleta.conAlfa(Paleta.tintaPrincipal(),
                    (0.36F + 0.32F * intensidadTerminal) * tinta);
            grafico.fill(this.getX(), y, this.getX() + ancho, y + 1, borde);
            grafico.fill(this.getX(), y + alto - 1, this.getX() + ancho, y + alto, borde);
        }
        if (presion > 0.0F) {
            int largoPresion = Math.max(1, Math.round((ancho - 2) * presion));
            grafico.fill(this.getX() + 1, y + alto - 2,
                    this.getX() + 1 + largoPresion, y + alto - 1,
                    Paleta.conAlfa(Paleta.tintaPrincipal(), 0.70F * presion * tinta));
        }

        // Casilla al margen: vacia en reposo, marcada al enfocar.
        int casillaY = y + (alto - LADO_CASILLA) / 2;
        dibujarMarco(grafico, x, casillaY, LADO_CASILLA,
                Paleta.conAlfa(Paleta.tintaSecundaria(), 0.70F * tinta));
        if (this.foco > 0.20F) {
            // La marca crece desde el centro: no aparece, se hace.
            float crecida = Math.min(1.0F, (this.foco - 0.20F) / 0.55F);
            int margen = Math.round(2.0F + (1.0F - crecida) * 1.5F);
            grafico.fill(x + margen, casillaY + margen,
                    x + LADO_CASILLA - margen, casillaY + LADO_CASILLA - margen,
                    Paleta.conAlfa(Paleta.tintaPrincipal(), (0.55F + 0.45F * crecida) * tinta));
        }

        int colorOrden = Paleta.conAlfa(Paleta.tintaSecundaria(), (0.70F + 0.30F * this.foco) * tinta);
        int colorEtiqueta = Paleta.conAlfa(
                Paleta.mezclar(Paleta.tintaSecundaria(), Paleta.tintaPrincipal(), this.foco), tinta);

        dibujarTexto(grafico, cliente, this.orden, x + SANGRIA_ORDEN, lineaBase, colorOrden, escala);
        dibujarTexto(grafico, cliente, this.getMessage(), x + SANGRIA_ETIQUETA, lineaBase,
                colorEtiqueta, escala);

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
        int inicio = x + SANGRIA_ETIQUETA
                + Math.round(anchoEtiqueta(cliente) * (ConfigTurno.textoGrande() ? 1.15F : 1.0F)) + 4;
        // El contenido desplazado nunca rebasa el borde real del widget.
        int fin = this.getX() + ancho - 2;
        if (fin <= inicio) {
            return;
        }

        float largo = fin - inicio;
        for (int px = inicio; px < fin; px += 3) {
            float posicion = (px - inicio) / largo;
            // El punto se enciende cuando el foco supera su posicion en la fila.
            float local = Math.max(0.0F, Math.min(1.0F, (this.foco - posicion * 0.55F) / 0.45F));
            grafico.fill(px, lineaBase + 6, px + 1, lineaBase + 7,
                    Paleta.conAlfa(Paleta.tintaSecundaria(), (0.28F + 0.24F * local) * tinta));
        }
    }

    private int anchoEtiqueta(Minecraft cliente) {
        Component mensaje = this.getMessage();
        if (this.anchoWidgetMedido != this.getWidth() || !mensaje.equals(this.etiquetaMedida)) {
            this.anchoEtiquetaMedida = cliente.font.width(mensaje);
            this.etiquetaMedida = mensaje;
            this.anchoWidgetMedido = this.getWidth();
        }
        return this.anchoEtiquetaMedida;
    }

    private static void dibujarTexto(GuiGraphics grafico, Minecraft cliente, Component texto,
                                     int x, int y, int color, float escala) {
        if (escala == 1.0F) {
            grafico.drawString(cliente.font, texto, x, y, color, false);
            return;
        }
        grafico.pose().pushPose();
        grafico.pose().translate(x, y, 0.0D);
        grafico.pose().scale(escala, escala, 1.0F);
        grafico.drawString(cliente.font, texto, 0, 0, color, false);
        grafico.pose().popPose();
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

package com.santipdr.jobsmenu.client.screen;

import com.santipdr.jobsmenu.client.scene.EscenaNivel;
import com.santipdr.jobsmenu.client.ui.CasillaAjuste;
import com.santipdr.jobsmenu.client.ui.HojaPapel;
import com.santipdr.jobsmenu.client.ui.Paleta;
import com.santipdr.jobsmenu.client.ui.ReglaAjuste;
import com.santipdr.jobsmenu.client.ui.RenglonTablon;
import com.santipdr.jobsmenu.config.ConfigTurno;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

/**
 * Condiciones de estancia: los ajustes del mod, impresos en la hoja.
 *
 * Hasta ahora la unica forma de tocar estos ajustes era editar a mano el
 * archivo de configuracion, algo que la voz del mod no admite y que ningun
 * ocupante deberia tener que hacer. Esta pantalla los pone donde corresponde:
 * en una hoja pegada a la pared, con la misma piel, la misma tinta y el mismo
 * apagarse con la luz que el aviso principal.
 *
 * Cada casilla escribe en la config y la guarda en el acto (ver ConfigTurno):
 * lo que se marca aca sigue marcado la proxima vez, sin pasar por ningun
 * archivo. Los dos volumenes son reglas graduadas, no barras rellenas: esto es
 * una hoja, no un panel de control.
 *
 * LO DEL MOD Y LO DEL EQUIPO
 *
 * Esta hoja lleva los ajustes DEL MOD -la escena, el sonido del recinto, la
 * accesibilidad-, no los del juego. Pero el jugador tambien necesita los del
 * EQUIPO: imagen, sonido general, controles, idioma, paquetes de recursos. Por
 * eso al pie, apartado del resto, hay un renglon que abre las opciones reales
 * de Minecraft tal cual. No se pierde ninguna: se ordenan. Lo del sitio esta en
 * la hoja; lo de la maquina, a un renglon de distancia.
 *
 * Es hija del aviso: se llega desde el renglon "03 Condiciones de estancia" y
 * se vuelve con Escape o con el renglon del pie. Comparte con el la escena viva
 * de fondo, asi que el pasillo sigue rotando y apagandose detras del papel.
 */
public class PantallaCondiciones extends Screen {

    private static final int ANCHO_HOJA = 232;
    private static final int MARGEN_HOJA = 14;
    private static final int MARGEN_PANTALLA = 12;
    private static final int ALTO_TITULO = 18;
    private static final int AIRE_TITULO = 4;
    private static final int ALTO_LINEA = 11;
    private static final int AIRE_REGLA = 7;
    private static final int AIRE_BLOQUE = 10;
    private static final int AIRE_APARTE = 12;

    /** Alto del renglon de accion al pie (los ajustes del equipo). */
    private static final int ALTO_RENGLON = 20;

    /** Hueco que aparta el renglon de accion del ultimo ajuste del mod. */
    private static final int AIRE_ACCION = 14;

    /** La pantalla a la que se vuelve (el aviso). */
    private final Screen anterior;

    private int hojaX;
    private int hojaY;
    private int hojaAlto;
    private int altoCabecera;

    /**
     * Si la hoja va sin la letra chica de cada ajuste.
     *
     * Con la ventana muy baja o el GUI a escala 4 la lista entera con sus
     * explicaciones no entra, y una hoja que se sale por abajo esconde justo el
     * ultimo ajuste. Antes que recortar la lista se recorta la letra chica: la
     * etiqueta de cada opcion se basta sola para saber que hace. Asi ningun
     * ajuste queda fuera de alcance, que es la regla de accesibilidad del mod.
     */
    private boolean compacto;

    public PantallaCondiciones(Screen anterior) {
        super(Component.translatable("jobsmenu.opciones.titulo"));
        this.anterior = anterior;
    }

    @Override
    protected void init() {
        int ancho = ANCHO_HOJA - 2 * MARGEN_HOJA;

        this.altoCabecera = ALTO_TITULO + AIRE_TITULO
                + lineas("jobsmenu.opciones.subtitulo", ancho) * ALTO_LINEA
                + AIRE_REGLA + 1 + AIRE_REGLA;

        // Se arma la lista de golpe para poder medir el alto real de la hoja
        // antes de colocarla. Cada elemento sabe cuanto ocupa (una casilla con
        // detalle mide mas que una sin el), asi que el alto sale de sumar.
        this.hojaX = Math.max(14, (int) (this.width * 0.06F));
        int x = this.hojaX + MARGEN_HOJA;

        // Primero calculo el alto total con la letra chica; si no entra, se
        // recorta y se vuelve a medir sin ella. Nunca se recorta la lista.
        int disponible = this.height - 2 * MARGEN_PANTALLA;
        this.compacto = false;
        int alto = medirCuerpo(ancho);
        int altoConCabecera = MARGEN_HOJA + this.altoCabecera + AIRE_BLOQUE + alto + MARGEN_HOJA;
        if (altoConCabecera > disponible) {
            this.compacto = true;
            alto = medirCuerpo(ancho);
        }
        this.hojaAlto = MARGEN_HOJA + this.altoCabecera + AIRE_BLOQUE + alto + MARGEN_HOJA;

        if (this.hojaAlto > disponible) {
            this.hojaY = MARGEN_PANTALLA;
        } else {
            this.hojaY = Math.max(MARGEN_PANTALLA,
                    Math.min((int) (this.height * 0.08F), this.height - MARGEN_PANTALLA - this.hojaAlto));
        }

        int y = this.hojaY + MARGEN_HOJA + this.altoCabecera + AIRE_BLOQUE;
        y = colocar(x, y, ancho);
    }

    /** Solo mide: recorre los mismos elementos que colocar() pero sin crearlos. */
    private int medirCuerpo(int ancho) {
        int total = 0;
        for (Ajuste a : AJUSTES) {
            if (a.aparte) {
                total += AIRE_APARTE;
            }
            if (a.regla) {
                total += 22 + 4;
            } else {
                Component detalle = detalleDe(a);
                total += CasillaAjuste.altoConDetalle(detalle, ancho, this.font);
            }
        }
        // Al pie va, apartado, el acceso a los ajustes del equipo (las opciones
        // reales del juego): imagen, sonido general, controles, idioma. Ocupa
        // su hueco de separacion y su renglon.
        total += AIRE_ACCION + ALTO_RENGLON;
        return total;
    }

    /** El detalle de un ajuste, o null si esta compacto o el ajuste no lleva. */
    private Component detalleDe(Ajuste a) {
        if (this.compacto || a.detalle == null) {
            return null;
        }
        return Component.translatable(a.detalle);
    }

    /** Crea y ubica cada widget, devolviendo la y final. */
    private int colocar(int x, int y, int ancho) {
        for (Ajuste a : AJUSTES) {
            if (a.aparte) {
                y += AIRE_APARTE;
            }
            if (a.regla) {
                this.addRenderableWidget(new ReglaAjuste(x, y, ancho, 22,
                        Component.translatable(a.etiqueta), a.valor, a.fijarInt));
                y += 22 + 4;
            } else {
                Component detalle = detalleDe(a);
                int alto = CasillaAjuste.altoConDetalle(detalle, ancho, this.font);
                this.addRenderableWidget(new CasillaAjuste(x, y, ancho, alto,
                        Component.translatable(a.etiqueta), detalle, a.estado, a.fijarBool));
                y += alto;
            }
        }

        // El acceso a los ajustes del equipo, al pie y apartado. Es un renglon
        // de accion -no una casilla-, porque no guarda un estado: lleva a otra
        // pantalla, la de opciones del juego. Se reusa RenglonTablon, el mismo
        // widget de las acciones del aviso, asi habla el mismo idioma. No es
        // terminal: no destruye nada, solo abre lo de siempre.
        y += AIRE_ACCION;
        this.addRenderableWidget(new RenglonTablon(x, y, ancho, ALTO_RENGLON,
                ">", Component.translatable("jobsmenu.opciones.equipo"), this::abrirAjustesEquipo, false));
        y += ALTO_RENGLON;
        return y;
    }

    /** Abre las opciones reales de Minecraft: imagen, sonido, controles, idioma. */
    private void abrirAjustesEquipo() {
        Minecraft cliente = Minecraft.getInstance();
        cliente.setScreen(new OptionsScreen(this, cliente.options));
    }

    private int lineas(String clave, int ancho) {
        return Math.max(1, this.font.split(Component.translatable(clave), ancho).size());
    }

    @Override
    public void renderBackground(GuiGraphics grafico) {
        EscenaNivel.dibujar(grafico, this.width, this.height);
    }

    @Override
    public void render(GuiGraphics grafico, int ratonX, int ratonY, float parcial) {
        this.renderBackground(grafico);

        HojaPapel.dibujar(grafico, this.hojaX, this.hojaY,
                this.hojaX + ANCHO_HOJA, this.hojaY + this.hojaAlto, false);

        cabecera(grafico);
        super.render(grafico, ratonX, ratonY, parcial);
    }

    private void cabecera(GuiGraphics grafico) {
        int x = this.hojaX + MARGEN_HOJA;
        int ancho = ANCHO_HOJA - 2 * MARGEN_HOJA;
        int y = this.hojaY + MARGEN_HOJA;
        float tinta = HojaPapel.tinta();

        grafico.pose().pushPose();
        grafico.pose().translate(x, y, 0.0D);
        grafico.pose().scale(2.0F, 2.0F, 1.0F);
        grafico.drawString(this.font, Component.translatable("jobsmenu.opciones.titulo"), 0, 0,
                Paleta.conAlfa(Paleta.TINTA, tinta), false);
        grafico.pose().popPose();

        y += ALTO_TITULO + AIRE_TITULO;
        for (FormattedCharSequence linea : this.font.split(
                Component.translatable("jobsmenu.opciones.subtitulo"), ancho)) {
            grafico.drawString(this.font, linea, x, y,
                    Paleta.conAlfa(Paleta.TINTA_TENUE, tinta), false);
            y += ALTO_LINEA;
        }

        y += AIRE_REGLA;
        grafico.fill(x, y, x + ancho, y + 1, Paleta.conAlfa(Paleta.TINTA_TENUE, 0.45F * tinta));
    }

    @Override
    public void onClose() {
        // Al cerrar se vuelve al aviso, no al menu vanilla. La config ya quedo
        // guardada en cada cambio, asi que no hay nada que confirmar aca.
        this.minecraft.setScreen(this.anterior);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // ----------------------------------------------------------------------
    // La lista de ajustes
    //
    // Se declara como datos, no como codigo repetido: cada fila dice su
    // etiqueta, su detalle, como se lee y como se escribe. El orden agrupa por
    // tema -imagen, sonido, accesibilidad- y deja los volumenes como reglas.
    // ----------------------------------------------------------------------

    private static final class Ajuste {
        final String etiqueta;
        final String detalle;
        final boolean regla;
        final boolean aparte;
        final java.util.function.BooleanSupplier estado;
        final java.util.function.Consumer<Boolean> fijarBool;
        final java.util.function.IntSupplier valor;
        final java.util.function.Consumer<Integer> fijarInt;

        /** Constructor de casilla. */
        Ajuste(String etiqueta, String detalle, boolean aparte,
               java.util.function.BooleanSupplier estado,
               java.util.function.Consumer<Boolean> fijarBool) {
            this.etiqueta = etiqueta;
            this.detalle = detalle;
            this.regla = false;
            this.aparte = aparte;
            this.estado = estado;
            this.fijarBool = fijarBool;
            this.valor = null;
            this.fijarInt = null;
        }

        /** Constructor de regla graduada (volumen). */
        Ajuste(String etiqueta, boolean aparte,
               java.util.function.IntSupplier valor,
               java.util.function.Consumer<Integer> fijarInt) {
            this.etiqueta = etiqueta;
            this.detalle = null;
            this.regla = true;
            this.aparte = aparte;
            this.estado = null;
            this.fijarBool = null;
            this.valor = valor;
            this.fijarInt = fijarInt;
        }
    }

    private static final Ajuste[] AJUSTES = new Ajuste[] {
            new Ajuste("jobsmenu.opciones.escena", "jobsmenu.opciones.escena.detalle", false,
                    ConfigTurno::escenaViva, ConfigTurno::fijarEscenaViva),
            new Ajuste("jobsmenu.opciones.rotar", "jobsmenu.opciones.rotar.detalle", false,
                    ConfigTurno::rotarNivelesBruto, ConfigTurno::fijarRotarNiveles),
            new Ajuste("jobsmenu.opciones.cuenta", "jobsmenu.opciones.cuenta.detalle", false,
                    ConfigTurno::mostrarCuentaRegresivaBruto, ConfigTurno::fijarMostrarCuentaRegresiva),
            new Ajuste("jobsmenu.opciones.avisos", "jobsmenu.opciones.avisos.detalle", false,
                    ConfigTurno::avisosRotativosBruto, ConfigTurno::fijarAvisosRotativos),

            new Ajuste("jobsmenu.opciones.musica", "jobsmenu.opciones.musica.detalle", true,
                    ConfigTurno::musicaMenu, ConfigTurno::fijarMusicaMenu),
            new Ajuste("jobsmenu.opciones.volmusica", false,
                    ConfigTurno::volumenMusicaPorcentaje, ConfigTurno::fijarVolumenMusica),
            new Ajuste("jobsmenu.opciones.ambiente", "jobsmenu.opciones.ambiente.detalle", false,
                    ConfigTurno::sonidoAmbiente, ConfigTurno::fijarSonidoAmbiente),
            new Ajuste("jobsmenu.opciones.volambiente", false,
                    ConfigTurno::volumenAmbientePorcentaje, ConfigTurno::fijarVolumenAmbiente),
            new Ajuste("jobsmenu.opciones.botones", "jobsmenu.opciones.botones.detalle", false,
                    ConfigTurno::sonidoBotones, ConfigTurno::fijarSonidoBotones),
            new Ajuste("jobsmenu.opciones.credito", "jobsmenu.opciones.credito.detalle", false,
                    ConfigTurno::creditoMusica, ConfigTurno::fijarCreditoMusica),

            new Ajuste("jobsmenu.opciones.movimiento", "jobsmenu.opciones.movimiento.detalle", true,
                    ConfigTurno::movimientoReducido, ConfigTurno::fijarMovimientoReducido),
            new Ajuste("jobsmenu.opciones.destellos", "jobsmenu.opciones.destellos.detalle", false,
                    ConfigTurno::destellosReducidos, ConfigTurno::fijarDestellosReducidos),
            new Ajuste("jobsmenu.opciones.interfaz", "jobsmenu.opciones.interfaz.detalle", false,
                    ConfigTurno::interfazMinima, ConfigTurno::fijarInterfazMinima),
            new Ajuste("jobsmenu.opciones.pausa", "jobsmenu.opciones.pausa.detalle", true,
                    ConfigTurno::pausaPropia, ConfigTurno::fijarPausaPropia),
    };
}

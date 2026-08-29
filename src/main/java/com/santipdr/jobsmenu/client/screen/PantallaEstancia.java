package com.santipdr.jobsmenu.client.screen;

import com.santipdr.jobsmenu.client.sound.MezclaAudio;
import com.santipdr.jobsmenu.client.ui.HojaPapel;
import com.santipdr.jobsmenu.client.ui.Paleta;
import com.santipdr.jobsmenu.client.ui.RenglonTablon;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import com.mojang.realmsclient.RealmsMainScreen;
import org.lwjgl.glfw.GLFW;

/**
 * Estancia en suspenso: la pausa, con la piel del aviso.
 *
 * Cuando el ocupante detiene el turno, en vez del menu gris de siempre aparece
 * una hoja pegada sobre lo que estaba haciendo: la administracion tomo nota de
 * que paro. Misma hoja, misma tinta, mismos renglones de formulario que el
 * aviso del nivel.
 *
 * QUE NO SE ROMPE
 *
 * Esta pantalla NO reimplementa el guardado. El renglon de dejar el turno
 * llama exactamente a la misma secuencia que la pausa de vanilla -desconectar
 * el nivel, vaciarlo mostrando "Guardando", y volver al titulo o al
 * multijugador segun corresponda-, verificada contra el codigo de la version.
 * Por eso los mods que aceleran o respaldan el guardado en segundo plano siguen
 * funcionando igual: enganchan esa secuencia por debajo, no este boton.
 *
 * A diferencia del aviso del nivel, aca NO se dibuja la escena viva de fondo:
 * detras esta el mundo real del jugador, congelado, y taparlo con el pasillo
 * seria mentir sobre donde esta. Solo se oscurece un poco y se apoya la hoja.
 */
public class PantallaEstancia extends Screen {

    private static final int ANCHO_HOJA = 214;
    private static final int MARGEN_HOJA = 14;
    private static final int MARGEN_PANTALLA = 12;
    private static final int ALTO_TITULO = 18;
    private static final int AIRE_TITULO = 4;
    private static final int ALTO_LINEA = 11;
    private static final int AIRE_REGLA = 7;
    private static final int AIRE_CABECERA = 14;
    private static final int ALTO_RENGLON = 20;
    private static final int SEPARACION = 3;
    private static final int HUECO_APARTE = 10;

    private int hojaX;
    private int hojaY;
    private int hojaAlto;
    private int altoCabecera;

    public PantallaEstancia() {
        super(Component.translatable("jobsmenu.pausa.titulo"));
    }

    @Override
    protected void init() {
        int ancho = ANCHO_HOJA - 2 * MARGEN_HOJA;

        this.altoCabecera = ALTO_TITULO + AIRE_TITULO
                + lineas("jobsmenu.pausa.subtitulo", ancho) * ALTO_LINEA
                + AIRE_REGLA + 1 + AIRE_REGLA;

        int salto = ALTO_RENGLON + SEPARACION;
        int altoLista = 2 * salto + HUECO_APARTE + ALTO_RENGLON;

        this.hojaAlto = MARGEN_HOJA + this.altoCabecera + AIRE_CABECERA + altoLista + MARGEN_HOJA;
        this.hojaX = Math.max(14, (this.width - ANCHO_HOJA) / 2);

        int disponible = this.height - 2 * MARGEN_PANTALLA;
        if (this.hojaAlto > disponible) {
            this.hojaY = MARGEN_PANTALLA;
        } else {
            this.hojaY = (this.height - this.hojaAlto) / 2;
        }

        int x = this.hojaX + MARGEN_HOJA;
        int y = this.hojaY + MARGEN_HOJA + this.altoCabecera + AIRE_CABECERA;

        agregar(x, y, ancho, "01", "jobsmenu.pausa.reanudar", this::reanudar, false);
        agregar(x, y + salto, ancho, "02", "jobsmenu.pausa.condiciones", this::abrirCondiciones, false);
        // Dejar el turno queda apartado por el hueco, como renunciar en el
        // aviso: es lo que saca del mundo, y no se pulsa por inercia.
        agregar(x, y + 2 * salto + HUECO_APARTE, ancho, "03",
                rotuloSalida(), this::dejarTurno, true);
    }

    private void agregar(int x, int y, int ancho, String orden, String clave,
                         Runnable accion, boolean terminal) {
        this.addRenderableWidget(new RenglonTablon(
                x, y, ancho, ALTO_RENGLON, orden, Component.translatable(clave), accion, terminal));
    }

    /**
     * El texto del renglon de salida cambia con el sitio, igual que en vanilla:
     * en un mundo propio se guarda y se sale; en un servidor solo se abandona.
     */
    private String rotuloSalida() {
        return this.minecraft.isLocalServer()
                ? "jobsmenu.pausa.abandonar.local"
                : "jobsmenu.pausa.abandonar.servidor";
    }

    private int lineas(String clave, int ancho) {
        return Math.max(1, this.font.split(Component.translatable(clave), ancho).size());
    }

    // ----------------------------------------------------------------------
    // Acciones
    // ----------------------------------------------------------------------

    private void reanudar() {
        this.minecraft.setScreen(null);
        this.minecraft.mouseHandler.grabMouse();
    }

    private void abrirCondiciones() {
        this.minecraft.setScreen(new OptionsScreen(this, this.minecraft.options));
    }

    /**
     * Dejar el turno: la MISMA secuencia que la pausa de vanilla.
     *
     * Verificada contra el codigo de 1.20.1: desconectar el nivel, vaciarlo
     * -mostrando "Guardando" solo si el servidor es local, que es cuando hay
     * algo que guardar- y volver al titulo, a Realms o al multijugador segun
     * de donde se venga. No se toca el guardado en si, asi que los mods que lo
     * aceleran o respaldan siguen operando por debajo.
     */
    private void dejarTurno() {
        boolean local = this.minecraft.isLocalServer();
        boolean realms = this.minecraft.isConnectedToRealms();

        if (this.minecraft.level != null) {
            this.minecraft.level.disconnect();
        }
        if (local) {
            this.minecraft.clearLevel(new GenericDirtMessageScreen(
                    Component.translatable("menu.savingLevel")));
        } else {
            this.minecraft.clearLevel();
        }

        TitleScreen titulo = new TitleScreen();
        if (local) {
            this.minecraft.setScreen(titulo);
        } else if (realms) {
            this.minecraft.setScreen(new RealmsMainScreen(titulo));
        } else {
            this.minecraft.setScreen(new JoinMultiplayerScreen(titulo));
        }
    }

    // ----------------------------------------------------------------------
    // Teclado
    // ----------------------------------------------------------------------

    /**
     * La misma tecla M que en el aviso: silencia o restaura todo el audio del
     * mod. La pausa suele ser el momento en que se baja el volumen.
     */
    @Override
    public boolean keyPressed(int codigo, int escaneo, int modificadores) {
        if (codigo == GLFW.GLFW_KEY_M) {
            MezclaAudio.alternarSilencio();
            return true;
        }
        return super.keyPressed(codigo, escaneo, modificadores);
    }

    // ----------------------------------------------------------------------
    // Dibujo
    // ----------------------------------------------------------------------

    @Override
    public void render(GuiGraphics grafico, int ratonX, int ratonY, float parcial) {
        // Detras esta el mundo congelado del jugador: se oscurece apenas, no se
        // tapa con el pasillo. La hoja se apoya sobre lo que estaba pasando.
        this.renderBackground(grafico);
        grafico.fill(0, 0, this.width, this.height, Paleta.conAlfa(Paleta.VANO, 0.42F));

        HojaPapel.dibujar(grafico, this.hojaX, this.hojaY,
                this.hojaX + ANCHO_HOJA, this.hojaY + this.hojaAlto, true, 1.0F);

        cabecera(grafico);
        super.render(grafico, ratonX, ratonY, parcial);
    }

    private void cabecera(GuiGraphics grafico) {
        int x = this.hojaX + MARGEN_HOJA;
        int ancho = ANCHO_HOJA - 2 * MARGEN_HOJA;
        int y = this.hojaY + MARGEN_HOJA;
        // La hoja de pausa no depende de la luz del pasillo -no hay pasillo
        // detras- asi que su tinta es plena y estable.
        float tinta = 1.0F;

        grafico.pose().pushPose();
        grafico.pose().translate(x, y, 0.0D);
        grafico.pose().scale(2.0F, 2.0F, 1.0F);
        grafico.drawString(this.font, Component.translatable("jobsmenu.pausa.titulo"), 0, 0,
                Paleta.conAlfa(Paleta.TINTA, tinta), false);
        grafico.pose().popPose();

        y += ALTO_TITULO + AIRE_TITULO;
        for (FormattedCharSequence linea : this.font.split(
                Component.translatable("jobsmenu.pausa.subtitulo"), ancho)) {
            grafico.drawString(this.font, linea, x, y,
                    Paleta.conAlfa(Paleta.TINTA_TENUE, tinta), false);
            y += ALTO_LINEA;
        }

        y += AIRE_REGLA;
        grafico.fill(x, y, x + ancho, y + 1, Paleta.conAlfa(Paleta.TINTA_TENUE, 0.45F * tinta));
    }

    /** Escape reanuda el turno, como en la pausa de vanilla. */
    @Override
    public void onClose() {
        reanudar();
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }
}

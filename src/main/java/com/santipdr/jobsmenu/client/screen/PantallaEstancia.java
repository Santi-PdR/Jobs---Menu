package com.santipdr.jobsmenu.client.screen;

import com.santipdr.jobsmenu.client.SesionMenu;
import com.santipdr.jobsmenu.client.sound.MezclaAudio;
import com.santipdr.jobsmenu.client.ui.HojaPapel;
import com.santipdr.jobsmenu.client.ui.Paleta;
import com.santipdr.jobsmenu.client.ui.RenglonTablon;
import com.santipdr.jobsmenu.config.ConfigTurno;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.Collections;
import java.util.List;

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
    private int anchoHoja;
    private int margenHoja;
    private int altoCabecera;
    private int altoRenglonActual = ALTO_RENGLON;
    private int separacionActual = SEPARACION;
    private float escalaTipografia = 1.0F;
    private List<FormattedCharSequence> lineasSubtitulo = Collections.emptyList();

    public PantallaEstancia() {
        super(Component.translatable("jobsmenu.pausa.titulo"));
    }

    @Override
    protected void init() {
        // La pausa tambien debe sobrevivir a una ventana estrecha. El ancho de
        // los renglones se deriva de la hoja real, no de la constante nominal;
        // asi la hitbox y el texto siguen coincidiendo despues de un resize.
        int margenPantalla = this.width < 270 ? 6 : MARGEN_PANTALLA;
        this.anchoHoja = Math.max(1, Math.min(ANCHO_HOJA,
                this.width - 2 * margenPantalla));
        this.margenHoja = Math.min(MARGEN_HOJA, Math.max(2, (this.anchoHoja - 4) / 2));
        this.escalaTipografia = ConfigTurno.textoGrande() && this.width >= 300 && this.height >= 360
                ? 1.15F : 1.0F;
        this.altoRenglonActual = Math.round(ALTO_RENGLON * this.escalaTipografia);
        this.separacionActual = Math.round(SEPARACION * this.escalaTipografia);
        int ancho = Math.max(1, this.anchoHoja - 2 * this.margenHoja);
        int anchoMedido = Math.max(1, Math.round(ancho / this.escalaTipografia));
        this.lineasSubtitulo = this.font.split(Component.translatable("jobsmenu.pausa.subtitulo"), anchoMedido);

        this.altoCabecera = Math.round((ALTO_TITULO + AIRE_TITULO
                + AIRE_REGLA + 1 + AIRE_REGLA) * this.escalaTipografia)
                + this.lineasSubtitulo.size() * Math.round(ALTO_LINEA * this.escalaTipografia);

        int salto = this.altoRenglonActual + this.separacionActual;
        int altoLista = 2 * salto + Math.round(HUECO_APARTE * this.escalaTipografia)
                + this.altoRenglonActual;

        this.hojaAlto = this.margenHoja + this.altoCabecera + AIRE_CABECERA
                + altoLista + this.margenHoja;
        this.hojaX = Math.max(margenPantalla, (this.width - this.anchoHoja) / 2);

        int disponible = this.height - 2 * margenPantalla;
        if (this.hojaAlto > disponible) {
            this.hojaY = margenPantalla;
        } else {
            this.hojaY = Math.max(margenPantalla, (this.height - this.hojaAlto) / 2);
        }

        int x = this.hojaX + this.margenHoja;
        int y = this.hojaY + this.margenHoja + this.altoCabecera + AIRE_CABECERA;

        agregar(x, y, ancho, "01", "jobsmenu.pausa.reanudar", this::reanudar, false);
        agregar(x, y + salto, ancho, "02", "jobsmenu.pausa.condiciones", this::abrirCondiciones, false);
        // Dejar el turno queda apartado por el hueco, como renunciar en el
        // aviso: es lo que saca del mundo, y no se pulsa por inercia.
        agregar(x, y + 2 * salto + Math.round(HUECO_APARTE * this.escalaTipografia), ancho, "03",
                rotuloSalida(), this::dejarTurno, true);
    }

    private void agregar(int x, int y, int ancho, String orden, String clave,
                         Runnable accion, boolean terminal) {
        this.addRenderableWidget(new RenglonTablon(
                x, y, ancho, this.altoRenglonActual, orden, Component.translatable(clave), accion, terminal));
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

    // ----------------------------------------------------------------------
    // Acciones
    // ----------------------------------------------------------------------

    private void reanudar() {
        this.minecraft.setScreen(null);
        this.minecraft.mouseHandler.grabMouse();
    }

    private void abrirCondiciones() {
        // Dentro de un mundo SesionMenu.activa() es false por diseno, asi que
        // la redireccion global de OptionsScreen no se dispara. Se abre el hub
        // Jobs de forma explicita para que la pausa no vuelva al gris vanilla.
        this.minecraft.setScreen(new PantallaOpcionesJobs(this, this.minecraft.options));
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

        // Al abandonar el mundo se corta inmediatamente todo audio del menu.
        // EscuchaCliente reconduce el destino vanilla al menu Jobs.
        SesionMenu.cerrar();

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
                this.hojaX + this.anchoHoja, this.hojaY + this.hojaAlto, true, 1.0F);

        cabecera(grafico);
        super.render(grafico, ratonX, ratonY, parcial);
    }

    private void cabecera(GuiGraphics grafico) {
        int x = this.hojaX + this.margenHoja;
        int ancho = Math.max(1, this.anchoHoja - 2 * this.margenHoja);
        int y = this.hojaY + this.margenHoja;
        // La hoja de pausa no depende de la luz del pasillo -no hay pasillo
        // detras- asi que su tinta es plena y estable.
        float tinta = 1.0F;

        grafico.pose().pushPose();
        grafico.pose().translate(x, y, 0.0D);
        grafico.pose().scale(2.0F * this.escalaTipografia, 2.0F * this.escalaTipografia, 1.0F);
        grafico.drawString(this.font, Component.translatable("jobsmenu.pausa.titulo"), 0, 0,
                Paleta.conAlfa(Paleta.tintaPrincipal(), tinta), false);
        grafico.pose().popPose();

        y += Math.round((ALTO_TITULO + AIRE_TITULO) * this.escalaTipografia);
        for (FormattedCharSequence linea : this.lineasSubtitulo) {
            dibujarLinea(grafico, linea, x, y, Paleta.conAlfa(Paleta.tintaSecundaria(), tinta));
            y += Math.round(ALTO_LINEA * this.escalaTipografia);
        }

        y += Math.round(AIRE_REGLA * this.escalaTipografia);
        grafico.fill(x, y, x + ancho, y + 1,
                Paleta.conAlfa(Paleta.tintaSecundaria(), 0.45F * tinta));
    }

    private void dibujarLinea(GuiGraphics grafico, FormattedCharSequence linea,
                              int x, int y, int color) {
        if (this.escalaTipografia == 1.0F) {
            grafico.drawString(this.font, linea, x, y, color, false);
            return;
        }
        grafico.pose().pushPose();
        grafico.pose().translate(x, y, 0.0D);
        grafico.pose().scale(this.escalaTipografia, this.escalaTipografia, 1.0F);
        grafico.drawString(this.font, linea, 0, 0, color, false);
        grafico.pose().popPose();
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

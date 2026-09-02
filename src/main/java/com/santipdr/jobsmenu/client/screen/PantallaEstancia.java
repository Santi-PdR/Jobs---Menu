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

/** Pausa propia de Jobs, conservando el mundo real detras de la hoja. */
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
        this.hojaY = this.hojaAlto > disponible
                ? margenPantalla
                : Math.max(margenPantalla, (this.height - this.hojaAlto) / 2);

        int x = this.hojaX + this.margenHoja;
        int y = this.hojaY + this.margenHoja + this.altoCabecera + AIRE_CABECERA;

        agregar(x, y, ancho, "01", "jobsmenu.pausa.reanudar", this::reanudar, false);
        agregar(x, y + salto, ancho, "02", "jobsmenu.pausa.condiciones", this::abrirCondiciones, false);
        agregar(x, y + 2 * salto + Math.round(HUECO_APARTE * this.escalaTipografia), ancho, "03",
                rotuloSalida(), this::dejarTurno, true);
    }

    private void agregar(int x, int y, int ancho, String orden, String clave,
                         Runnable accion, boolean terminal) {
        this.addRenderableWidget(new RenglonTablon(
                x, y, ancho, this.altoRenglonActual, orden, Component.translatable(clave), accion, terminal));
    }

    private String rotuloSalida() {
        return this.minecraft.isLocalServer()
                ? "jobsmenu.pausa.abandonar.local"
                : "jobsmenu.pausa.abandonar.servidor";
    }

    private void reanudar() {
        this.minecraft.setScreen(null);
        this.minecraft.mouseHandler.grabMouse();
    }

    private void abrirCondiciones() {
        this.minecraft.setScreen(new PantallaOpcionesJobs(this, this.minecraft.options));
    }

    private void dejarTurno() {
        boolean local = this.minecraft.isLocalServer();
        boolean realms = this.minecraft.isConnectedToRealms();

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

    @Override
    public boolean keyPressed(int codigo, int escaneo, int modificadores) {
        if (codigo == GLFW.GLFW_KEY_M) {
            MezclaAudio.alternarSilencio();
            return true;
        }
        return super.keyPressed(codigo, escaneo, modificadores);
    }

    @Override
    public void render(GuiGraphics grafico, int ratonX, int ratonY, float parcial) {
        this.renderBackground(grafico);

        // Oscurecido por capas: el centro mantiene contexto y los laterales
        // caen un poco mas para empujar visualmente la mirada hacia la hoja.
        grafico.fill(0, 0, this.width, this.height, Paleta.conAlfa(Paleta.VANO, 0.34F));
        int banda = Math.max(12, (this.width - this.anchoHoja) / 2);
        grafico.fill(0, 0, banda, this.height, Paleta.conAlfa(Paleta.VANO, 0.16F));
        grafico.fill(this.width - banda, 0, this.width, this.height, Paleta.conAlfa(Paleta.VANO, 0.16F));

        // Guias de suspension alrededor de la hoja. Son estaticas y no tocan hitboxes.
        int rail = Paleta.conAlfa(Paleta.UI_ACENTO, 0.22F);
        int railFino = Paleta.conAlfa(Paleta.UI_ACENTO, 0.10F);
        int izquierda = Math.max(3, this.hojaX - 8);
        int derecha = Math.min(this.width - 4, this.hojaX + this.anchoHoja + 7);
        grafico.fill(izquierda, this.hojaY + 8, izquierda + 1, this.hojaY + this.hojaAlto - 8, rail);
        grafico.fill(derecha, this.hojaY + 8, derecha + 1, this.hojaY + this.hojaAlto - 8, rail);
        grafico.fill(izquierda - 3, this.hojaY + 18, izquierda, this.hojaY + 19, railFino);
        grafico.fill(derecha + 1, this.hojaY + this.hojaAlto - 19,
                derecha + 4, this.hojaY + this.hojaAlto - 18, railFino);

        // Sombra mecanica mas profunda que separa la hoja del mundo pausado.
        grafico.fill(this.hojaX + 4, this.hojaY + 5,
                this.hojaX + this.anchoHoja + 5, this.hojaY + this.hojaAlto + 5,
                Paleta.conAlfa(Paleta.VANO, 0.24F));

        HojaPapel.dibujar(grafico, this.hojaX, this.hojaY,
                this.hojaX + this.anchoHoja, this.hojaY + this.hojaAlto, true, 1.0F);

        // Marcas de registro sobre la hoja: arriba y abajo, como expediente suspendido.
        int marca = Paleta.conAlfa(Paleta.tintaSecundaria(), 0.18F);
        int centro = this.hojaX + this.anchoHoja / 2;
        grafico.fill(centro - 8, this.hojaY + 5, centro + 9, this.hojaY + 6, marca);
        grafico.fill(centro, this.hojaY + 3, centro + 1, this.hojaY + 8, marca);
        grafico.fill(centro - 5, this.hojaY + this.hojaAlto - 6,
                centro + 6, this.hojaY + this.hojaAlto - 5, marca);

        cabecera(grafico);
        super.render(grafico, ratonX, ratonY, parcial);
    }

    private void cabecera(GuiGraphics grafico) {
        int x = this.hojaX + this.margenHoja;
        int ancho = Math.max(1, this.anchoHoja - 2 * this.margenHoja);
        int y = this.hojaY + this.margenHoja;
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
        grafico.fill(x, y + 2, x + Math.max(14, ancho / 5), y + 3,
                Paleta.conAlfa(Paleta.UI_ACENTO, 0.28F));
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

    @Override
    public void onClose() {
        reanudar();
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }
}

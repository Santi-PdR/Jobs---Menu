package com.santipdr.jobsmenu.client.screen;

import com.santipdr.jobsmenu.JobsMenu;
import com.santipdr.jobsmenu.client.scene.EscenaDeposito;
import com.santipdr.jobsmenu.client.ui.Paleta;
import com.santipdr.jobsmenu.client.ui.RelojAparicion;
import com.santipdr.jobsmenu.client.ui.RenglonTablon;
import com.santipdr.jobsmenu.config.ConfigTurno;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.network.chat.Component;

/**
 * Pantalla de titulo del servidor Jobs: el tablon de turnos del deposito.
 */
public class PantallaTurno extends Screen {

    /** Cantidad de avisos disponibles en los archivos de idioma. */
    private static final int AVISOS = 8;

    /** Cada cuantos milisegundos rota el aviso del pie. */
    private static final long ROTACION_AVISO_MS = 7_000L;

    private static final int ANCHO_RENGLON = 190;
    private static final int ALTO_RENGLON = 20;
    private static final int SEPARACION = 4;

    public PantallaTurno() {
        super(Component.translatable("jobsmenu.pantalla.turno"));
    }

    @Override
    protected void init() {
        int x = Math.max(16, (int) (this.width * 0.10F));
        int y = Math.max(70, (int) (this.height * 0.42F));

        agregar(x, y, "01", "jobsmenu.tablon.solitario", this::abrirSolitario);
        agregar(x, y + (ALTO_RENGLON + SEPARACION), "02", "jobsmenu.tablon.complejo", this::abrirComplejo);
        agregar(x, y + 2 * (ALTO_RENGLON + SEPARACION), "03", "jobsmenu.tablon.contrato", this::abrirContrato);
        agregar(x, y + 3 * (ALTO_RENGLON + SEPARACION), "04", "jobsmenu.tablon.abandonar", this::abandonar);
    }

    private void agregar(int x, int y, String orden, String clave, Runnable accion) {
        this.addRenderableWidget(new RenglonTablon(
                x, y, ANCHO_RENGLON, ALTO_RENGLON, orden, Component.translatable(clave), accion));
    }

    private void abrirSolitario() {
        Minecraft cliente = Minecraft.getInstance();
        cliente.setScreen(new SelectWorldScreen(this));
    }

    private void abrirComplejo() {
        Minecraft cliente = Minecraft.getInstance();
        cliente.setScreen(new JoinMultiplayerScreen(this));
    }

    private void abrirContrato() {
        Minecraft cliente = Minecraft.getInstance();
        cliente.setScreen(new OptionsScreen(this, cliente.options));
    }

    private void abandonar() {
        Minecraft.getInstance().stop();
    }

    @Override
    public void render(GuiGraphics grafico, int ratonX, int ratonY, float parcial) {
        this.renderBackground(grafico);
        super.render(grafico, ratonX, ratonY, parcial);
        cabecera(grafico);

        if (ConfigTurno.mostrarCuentaRegresiva()) {
            cuentaRegresiva(grafico);
        }
        if (ConfigTurno.avisosRotativos()) {
            aviso(grafico);
        }
        if (!ConfigTurno.interfazMinima()) {
            sello(grafico);
        }
    }

    @Override
    public void renderBackground(GuiGraphics grafico) {
        EscenaDeposito.dibujar(grafico, this.width, this.height, 1.0F);
    }

    private void cabecera(GuiGraphics grafico) {
        int x = Math.max(16, (int) (this.width * 0.10F));
        int y = Math.max(34, (int) (this.height * 0.20F));

        grafico.pose().pushPose();
        grafico.pose().translate(x, y, 0.0D);
        grafico.pose().scale(3.0F, 3.0F, 1.0F);
        grafico.drawString(this.font, Component.translatable("jobsmenu.titulo"), 0, 0, Paleta.HUESO, false);
        grafico.pose().popPose();

        int lineaSubtitulo = y + 30;
        grafico.drawString(this.font, Component.translatable("jobsmenu.subtitulo"), x, lineaSubtitulo,
                Paleta.HUESO_TENUE, false);
        grafico.fill(x, lineaSubtitulo + 12, x + 120, lineaSubtitulo + 13, Paleta.conAlfa(Paleta.SODIO_TENUE, 0.75F));
    }

    private void cuentaRegresiva(GuiGraphics grafico) {
        boolean destellosReducidos = ConfigTurno.destellosReducidos() || !ConfigTurno.escenaViva();

        Component rotulo;
        if (RelojAparicion.enAparicion()) {
            rotulo = Component.translatable("jobsmenu.reloj.aparicion");
        } else if (RelojAparicion.inminente()) {
            rotulo = Component.translatable("jobsmenu.reloj.inminente");
        } else {
            rotulo = Component.translatable("jobsmenu.reloj.proxima");
        }

        String tiempo = RelojAparicion.formatoRestante();
        int color = RelojAparicion.color(destellosReducidos);
        int margen = 12;

        int anchoRotulo = this.font.width(rotulo);
        int anchoTiempo = this.font.width(tiempo);

        grafico.drawString(this.font, rotulo, this.width - margen - anchoRotulo, margen, Paleta.HUESO_TENUE, false);
        grafico.drawString(this.font, tiempo, this.width - margen - anchoTiempo, margen + 12, color, false);
    }

    private void aviso(GuiGraphics grafico) {
        int indice = (int) (Math.floorDiv(System.currentTimeMillis(), ROTACION_AVISO_MS) % AVISOS);
        Component texto = Component.translatable("jobsmenu.aviso." + indice);
        int x = Math.max(16, (int) (this.width * 0.10F));
        int y = this.height - 22;

        grafico.drawString(this.font, texto, x, y, Paleta.HUESO_TENUE, false);
    }

    private void sello(GuiGraphics grafico) {
        String texto = "jobsmenu " + JobsMenu.VERSION;
        int ancho = this.font.width(texto);
        grafico.drawString(this.font, texto, this.width - 12 - ancho, this.height - 14,
                Paleta.conAlfa(Paleta.HUESO_TENUE, 0.55F), false);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}

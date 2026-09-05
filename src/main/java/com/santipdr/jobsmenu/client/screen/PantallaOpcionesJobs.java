package com.santipdr.jobsmenu.client.screen;

import com.santipdr.jobsmenu.client.ui.BotonExpediente;
import com.santipdr.jobsmenu.client.ui.ChromeExpediente;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.VideoSettingsScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.repository.PackRepository;

/** Centro de control de Jobs: la configuracion del mod tiene jerarquia propia. */
public final class PantallaOpcionesJobs extends Screen {

    private static final int PANEL_MAX_W = 404;
    private static final int PANEL_MAX_H = 288;

    private final Screen anterior;
    private final Options opciones;
    private int panelX, panelY, panelW, panelH;
    private boolean compacta;

    public PantallaOpcionesJobs(Screen anterior, Options opciones) {
        super(Component.translatable("jobsmenu.interfaz.opciones.titulo"));
        this.anterior = anterior;
        this.opciones = opciones;
    }

    @Override
    protected void init() {
        this.panelW = Math.max(1, Math.min(PANEL_MAX_W, this.width - 12));
        this.panelH = Math.max(1, Math.min(PANEL_MAX_H, this.height - 12));
        this.panelX = (this.width - this.panelW) / 2;
        this.panelY = Math.max(4, (this.height - this.panelH) / 2);
        this.compacta = this.panelH < 262 || this.panelW < 350;

        int gap = compacta ? 6 : 8;
        int margen = compacta ? 14 : 20;
        int anchoUtil = Math.max(1, this.panelW - margen * 2);
        int bw = Math.max(70, (anchoUtil - gap) / 2);
        int bh = compacta ? 19 : 22;
        int x0 = this.panelX + margen;
        int x1 = x0 + bw + gap;
        int y0 = this.panelY + (compacta ? 47 : 56);
        int paso = compacta ? 22 : 25;

        BotonExpediente ajustesJobs = this.addRenderableWidget(new BotonExpediente(
                x0, y0, anchoUtil, bh,
                Component.translatable("jobsmenu.ajustes.boton"),
                BotonExpediente.Tipo.JOBS, this::abrirAviso));
        ajustesJobs.setTooltip(Tooltip.create(Component.translatable("jobsmenu.ajustes.boton.detalle")));

        int sy = y0 + bh + (compacta ? 5 : 10);

        boton(x0, sy, bw, bh, "options.skinCustomisation", "jobsmenu.tooltip.piel", this::abrirPiel);
        boton(x1, sy, bw, bh, "options.sounds", "jobsmenu.tooltip.sonido", this::abrirSonido);
        boton(x0, sy + paso, bw, bh, "options.video", "jobsmenu.tooltip.video", this::abrirVideo);
        boton(x1, sy + paso, bw, bh, "controls.title", "jobsmenu.tooltip.controles", this::abrirControles);
        boton(x0, sy + paso * 2, bw, bh, "options.language", "jobsmenu.tooltip.idioma", this::abrirIdioma);
        boton(x1, sy + paso * 2, bw, bh, "options.chat.title", "jobsmenu.tooltip.chat", this::abrirChat);
        boton(x0, sy + paso * 3, bw, bh, "resourcePack.title", "jobsmenu.tooltip.recursos", this::abrirPaquetes);
        boton(x1, sy + paso * 3, bw, bh, "options.accessibility.title", "jobsmenu.tooltip.accesibilidad", this::abrirAccesibilidad);
        boton(x0, sy + paso * 4, anchoUtil, bh,
                "options.online.title", "jobsmenu.tooltip.online", this::abrirOnline);

        int volverH = compacta ? 19 : 22;
        int volverY = this.panelY + this.panelH - volverH - 8;
        int volverW = Math.min(160, anchoUtil);

        BotonExpediente volver = this.addRenderableWidget(new BotonExpediente(
                this.width / 2 - volverW / 2, volverY, volverW, volverH,
                Component.translatable("jobsmenu.interfaz.volver"),
                BotonExpediente.Tipo.PRINCIPAL, this::onClose));
        volver.setTooltip(Tooltip.create(Component.translatable("jobsmenu.tooltip.volver")));
    }

    private void boton(int x, int y, int w, int h, String clave, String ayuda, Runnable accion) {
        BotonExpediente boton = this.addRenderableWidget(new BotonExpediente(
                x, y, w, h, Component.translatable(clave), BotonExpediente.Tipo.NORMAL, accion));
        boton.setTooltip(Tooltip.create(Component.translatable(ayuda)));
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        ChromeExpediente.fondo(g, this.width, this.height);
        ChromeExpediente.panel(g, panelX, panelY, panelW, panelH);
        ChromeExpediente.cabecera(g, this.font, this.title,
                Component.translatable("jobsmenu.interfaz.opciones.subtitulo"), panelX, panelY, panelW);

        ChromeExpediente.esquinas(g, panelX, panelY, panelW, panelH);
        ChromeExpediente.pie(g, this.font, panelX, panelY, panelW, panelH, "CFG-014");
        super.render(g, mouseX, mouseY, partialTick);
    }

    private void abrirPiel() {
        this.minecraft.setScreen(new PantallaPielJobs(this, this.opciones));
    }

    private void abrirSonido() {
        this.minecraft.setScreen(new PantallaSonidoJobs(this, this.opciones));
    }

    private void abrirVideo() {
        // Esta pantalla es deliberadamente vanilla. No se reconstruye, no se
        // recoloca su lista y no se adivinan APIs de mods mediante reflection.
        this.minecraft.setScreen(new VideoSettingsScreen(this, this.opciones));
    }

    private void abrirControles() {
        this.minecraft.setScreen(new PantallaControlesJobs(this, this.opciones));
    }

    private void abrirIdioma() {
        this.minecraft.setScreen(new PantallaIdiomaJobs(this, this.opciones,
                this.minecraft.getLanguageManager()));
    }

    private void abrirChat() {
        this.minecraft.setScreen(new PantallaChatJobs(this, this.opciones));
    }

    private void abrirPaquetes() {
        PackRepository repo = this.minecraft.getResourcePackRepository();
        java.util.function.Consumer<PackRepository> callback = r -> {
            this.minecraft.options.updateResourcePacks(r);
            this.minecraft.setScreen(this);
        };
        this.minecraft.setScreen(new PantallaPaquetesJobs(
                repo, callback, this.minecraft.getResourcePackDirectory(),
                Component.translatable("resourcePack.title")));
    }

    private void abrirAccesibilidad() {
        this.minecraft.setScreen(new PantallaAccesibilidadJobs(this, this.opciones));
    }

    private void abrirOnline() {
        this.minecraft.setScreen(new PantallaOnlineJobs(this, this.opciones));
    }

    private void abrirAviso() {
        this.minecraft.setScreen(new PantallaAjustesAviso(this, this.opciones));
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(this.anterior);
    }

    @Override
    public void renderBackground(GuiGraphics g) {
        // ChromeExpediente renders the complete background.
    }
}

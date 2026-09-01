package com.santipdr.jobsmenu.client.screen;

import com.santipdr.jobsmenu.client.ui.BotonExpediente;
import com.santipdr.jobsmenu.client.ui.ChromeExpediente;
import com.santipdr.jobsmenu.client.ui.SliderExpediente;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
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
    private int configY;
    private int sistemaY;

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

        this.configY = y0;
        BotonExpediente ajustesJobs = this.addRenderableWidget(new BotonExpediente(
                x0, y0, anchoUtil, bh,
                Component.translatable("jobsmenu.ajustes.boton"),
                BotonExpediente.Tipo.JOBS, this::abrirAviso));
        ajustesJobs.setTooltip(Tooltip.create(Component.translatable("jobsmenu.ajustes.boton.detalle")));

        this.sistemaY = y0 + bh + (compacta ? 5 : 10);
        int sy = this.sistemaY;

        boton(x0, sy, bw, bh, "options.skinCustomisation", this::abrirPiel);
        boton(x1, sy, bw, bh, "options.sounds", this::abrirSonido);
        boton(x0, sy + paso, bw, bh, "options.video", this::abrirVideo);
        boton(x1, sy + paso, bw, bh, "controls.title", this::abrirControles);
        boton(x0, sy + paso * 2, bw, bh, "options.language", this::abrirIdioma);
        boton(x1, sy + paso * 2, bw, bh, "options.chat.title", this::abrirChat);
        boton(x0, sy + paso * 3, bw, bh, "resourcePack.title", this::abrirPaquetes);
        boton(x1, sy + paso * 3, bw, bh, "options.accessibility.title", this::abrirAccesibilidad);
        boton(x0, sy + paso * 4, bw, bh, "options.online.title", this::abrirOnline);

        int volverH = compacta ? 19 : 22;
        int volverY = this.panelY + this.panelH - volverH - 8;
        int volverW = Math.min(160, anchoUtil);

        int fovY = sy + paso * 4;
        if (fovY + bh <= volverY - 5) {
            int fov = this.opciones.fov().get();
            this.addRenderableWidget(new SliderExpediente(
                    x1, fovY, bw, bh, 30, 110, fov,
                    v -> Component.translatable("jobsmenu.interfaz.fov",
                            Component.translatable("options.fov"), v),
                    v -> {
                        this.opciones.fov().set(v);
                        this.opciones.save();
                    }));
        }

        this.addRenderableWidget(new BotonExpediente(
                this.width / 2 - volverW / 2, volverY, volverW, volverH,
                Component.translatable("jobsmenu.interfaz.volver"),
                BotonExpediente.Tipo.PRINCIPAL, this::onClose));
    }

    private void boton(int x, int y, int w, int h, String clave, Runnable accion) {
        this.addRenderableWidget(new BotonExpediente(
                x, y, w, h, Component.translatable(clave), BotonExpediente.Tipo.NORMAL, accion));
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        ChromeExpediente.fondo(g, this.width, this.height);
        ChromeExpediente.panel(g, panelX, panelY, panelW, panelH);
        ChromeExpediente.cabecera(g, this.font, this.title,
                Component.translatable("jobsmenu.interfaz.opciones.subtitulo"), panelX, panelY, panelW);

        if (!this.compacta) {
            int margen = 20;
            ChromeExpediente.seccion(g, this.font, panelX + margen, panelX + panelW - margen,
                    this.configY - 9, Component.translatable("jobsmenu.titulo"));
            ChromeExpediente.seccion(g, this.font, panelX + margen, panelX + panelW - margen,
                    this.sistemaY - 10, Component.translatable("options.title"));

        }

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
        try {
            Class<?> pagesCls = Class.forName("me.jellysquid.mods.sodium.client.gui.SodiumGameOptionPages");
            Class<?> screenCls = Class.forName("org.embeddedt.embeddium.gui.EmbeddiumVideoOptionsScreen");
            java.util.List<Object> pages = new java.util.ArrayList<>();
            pages.add(pagesCls.getMethod("general").invoke(null));
            pages.add(pagesCls.getMethod("quality").invoke(null));
            pages.add(pagesCls.getMethod("performance").invoke(null));
            pages.add(pagesCls.getMethod("advanced").invoke(null));
            java.lang.reflect.Constructor<?> ctor = screenCls.getConstructor(Screen.class, java.util.List.class);
            this.minecraft.setScreen((Screen) ctor.newInstance(this, pages));
        } catch (Throwable ignored) {
            this.minecraft.setScreen(new PantallaVideoJobs(this, this.opciones));
        }
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

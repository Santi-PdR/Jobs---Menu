package com.santipdr.jobsmenu.client.screen;

import com.santipdr.jobsmenu.client.ui.BotonExpediente;
import com.santipdr.jobsmenu.client.ui.ChromeExpediente;
import com.santipdr.jobsmenu.client.ui.SliderExpediente;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.VideoSettingsScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.repository.PackRepository;

/** Hub de configuracion de Jobs: conserva opciones reales, cambia la experiencia. */
public final class PantallaOpcionesJobs extends Screen {

    private static final int PANEL_MAX_W = 392;
    private static final int PANEL_MAX_H = 292;

    private final Screen anterior;
    private final Options opciones;
    private int panelX, panelY, panelW, panelH;

    public PantallaOpcionesJobs(Screen anterior, Options opciones) {
        super(Component.translatable("jobsmenu.interfaz.opciones.titulo"));
        this.anterior = anterior;
        this.opciones = opciones;
    }

    @Override
    protected void init() {
        this.panelW = Math.min(PANEL_MAX_W, Math.max(220, this.width - 24));
        this.panelH = Math.min(PANEL_MAX_H, Math.max(238, this.height - 20));
        this.panelX = (this.width - this.panelW) / 2;
        this.panelY = Math.max(6, (this.height - this.panelH) / 2);

        int gap = 8;
        int margen = 20;
        int anchoUtil = this.panelW - margen * 2;
        int bw = Math.max(88, (anchoUtil - gap) / 2);
        int bh = 22;
        int x0 = this.panelX + margen;
        int x1 = x0 + bw + gap;
        int y0 = this.panelY + 58;
        int paso = 27;

        boton(x0, y0, bw, bh, "options.skinCustomisation", this::abrirPiel);
        boton(x1, y0, bw, bh, "options.sounds", this::abrirSonido);
        boton(x0, y0 + paso, bw, bh, "options.video", this::abrirVideo);
        boton(x1, y0 + paso, bw, bh, "controls.title", this::abrirControles);
        boton(x0, y0 + paso * 2, bw, bh, "options.language", this::abrirIdioma);
        boton(x1, y0 + paso * 2, bw, bh, "options.chat.title", this::abrirChat);
        boton(x0, y0 + paso * 3, bw, bh, "resourcePack.title", this::abrirPaquetes);
        boton(x1, y0 + paso * 3, bw, bh, "options.accessibility.title", this::abrirAccesibilidad);
        boton(x0, y0 + paso * 4, bw, bh, "options.online.title", this::abrirOnline);
        this.addRenderableWidget(new BotonExpediente(
                x1, y0 + paso * 4, bw, bh,
                Component.translatable("jobsmenu.ajustes.boton"),
                BotonExpediente.Tipo.PRINCIPAL, this::abrirAviso));

        int fovY = y0 + paso * 5 + 2;
        int fovW = Math.max(120, anchoUtil);
        int fov = this.opciones.fov().get();
        this.addRenderableWidget(new SliderExpediente(
                x0, fovY, fovW, 22, 30, 110, fov,
                v -> Component.translatable("jobsmenu.interfaz.fov",
                        Component.translatable("options.fov"), v),
                v -> {
                    this.opciones.fov().set(v);
                    this.opciones.save();
                }));

        int volverY = Math.min(this.panelY + this.panelH - 31, fovY + 30);
        int volverW = Math.min(160, anchoUtil);
        this.addRenderableWidget(new BotonExpediente(
                this.width / 2 - volverW / 2, volverY, volverW, 22,
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

        Component nota = Component.translatable("jobsmenu.interfaz.opciones.nota");
        int nw = this.font.width(nota);
        if (nw < this.panelW - 24) {
            g.drawString(this.font, nota, this.width / 2 - nw / 2, panelY + 45,
                    com.santipdr.jobsmenu.client.ui.Paleta.conAlfa(
                            com.santipdr.jobsmenu.client.ui.Paleta.tintaSecundaria(), 0.64F), false);
        }

        ChromeExpediente.esquinas(g, panelX, panelY, panelW, panelH);
        ChromeExpediente.pie(g, this.font, panelX, panelY, panelW, panelH, "CFG-012");
        super.render(g, mouseX, mouseY, partialTick);
    }

    private void abrirPiel() {
        this.minecraft.setScreen(new PantallaPielJobs(this, this.opciones));
    }

    private void abrirSonido() {
        this.minecraft.setScreen(new PantallaSonidoJobs(this, this.opciones));
    }

    private void abrirVideo() {
        // Embeddium, cuando existe, tiene su propia pantalla. No se sustituye
        // por una copia incompleta: se abre y el decorador global de Jobs le da
        // contexto. Si no existe, se usa el wrapper vanilla tematizado.
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
        // El render completo ya lo maneja ChromeExpediente.
    }
}

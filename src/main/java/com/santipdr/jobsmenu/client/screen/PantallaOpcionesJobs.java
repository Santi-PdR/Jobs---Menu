package com.santipdr.jobsmenu.client.screen;

import com.santipdr.jobsmenu.client.sound.MezclaAudio;
import com.santipdr.jobsmenu.client.sound.SonidosNivel;
import com.santipdr.jobsmenu.client.ui.BotonExpediente;
import com.santipdr.jobsmenu.client.ui.ChromeExpediente;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.repository.PackRepository;

/** Centro de control de Jobs: la configuracion conserva el flujo real de OptionsScreen. */
public final class PantallaOpcionesJobs extends OptionsScreen {

    private static final int PANEL_MAX_W = 404;
    private static final int PANEL_MAX_H = 288;
    private static final Component TITULO_JOBS =
            Component.translatable("jobsmenu.interfaz.opciones.titulo");

    private final Screen anterior;
    private final Options opciones;
    private AbstractButton botonVideoNatural;
    private boolean integracionNaturalFinalizada;
    private boolean ranuraVideoConocida;
    private int videoX, videoY, videoW, videoH;
    private int panelX, panelY, panelW, panelH;
    private boolean compacta;

    public PantallaOpcionesJobs(Screen anterior, Options opciones) {
        super(anterior, opciones);
        this.anterior = anterior;
        this.opciones = opciones;
    }

    @Override
    protected void init() {
        this.botonVideoNatural = null;
        this.integracionNaturalFinalizada = false;
        this.ranuraVideoConocida = false;

        // Construye primero el OptionsScreen real. Mixins y eventos del
        // modpack trabajan sobre esta misma instancia antes de que Jobs use su
        // comportamiento como backend.
        super.init();
        sincronizarControlesNaturales();

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

    /**
     * Conserva los controles naturales como fuente de comportamiento, pero no
     * los deja visibles debajo del chrome Jobs. La primera pasada memoriza la
     * ranura vanilla de Graficos; una pasada posterior puede reconocer un boton
     * sustituto aunque otro mod le haya cambiado el texto.
     */
    private void sincronizarControlesNaturales() {
        String video = Component.translatable("options.video").getString();
        AbstractButton porTexto = null;
        AbstractButton porRanura = null;
        boolean actualSiguePresente = false;

        for (var child : this.children()) {
            String clase = child.getClass().getName();
            boolean jobs = clase.startsWith("com.santipdr.jobsmenu.");
            if (jobs) continue;

            if (child == this.botonVideoNatural) {
                actualSiguePresente = true;
            }
            if (child instanceof AbstractButton boton) {
                if (video.equals(boton.getMessage().getString())) {
                    porTexto = boton;
                } else if (this.ranuraVideoConocida && coincideRanuraVideo(boton)) {
                    porRanura = boton;
                }
            }
            if (child instanceof AbstractWidget widget) {
                widget.visible = false;
            }
        }

        if (porTexto != null) {
            recordarVideo(porTexto);
        } else if (!actualSiguePresente && porRanura != null) {
            this.botonVideoNatural = porRanura;
        } else if (!actualSiguePresente) {
            this.botonVideoNatural = null;
        }
    }

    private void recordarVideo(AbstractButton boton) {
        this.botonVideoNatural = boton;
        this.videoX = boton.getX();
        this.videoY = boton.getY();
        this.videoW = boton.getWidth();
        this.videoH = boton.getHeight();
        this.ranuraVideoConocida = true;
    }

    private boolean coincideRanuraVideo(AbstractButton boton) {
        return Math.abs(boton.getX() - this.videoX) <= 4
                && Math.abs(boton.getY() - this.videoY) <= 4
                && Math.abs(boton.getWidth() - this.videoW) <= 8
                && Math.abs(boton.getHeight() - this.videoH) <= 4;
    }

    private void boton(int x, int y, int w, int h, String clave, String ayuda, Runnable accion) {
        BotonExpediente boton = this.addRenderableWidget(new BotonExpediente(
                x, y, w, h, Component.translatable(clave), BotonExpediente.Tipo.NORMAL, accion));
        boton.setTooltip(Tooltip.create(Component.translatable(ayuda)));
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // Init.Post de Forge ya termino cuando llega el primer render. Esta
        // segunda captura recoge botones sustituidos/agregados por otros mods.
        if (!this.integracionNaturalFinalizada) {
            sincronizarControlesNaturales();
            this.integracionNaturalFinalizada = true;
        }

        ChromeExpediente.fondo(g, this.width, this.height);
        ChromeExpediente.panel(g, panelX, panelY, panelW, panelH);
        ChromeExpediente.cabecera(g, this.font, TITULO_JOBS,
                Component.translatable("jobsmenu.interfaz.opciones.subtitulo"), panelX, panelY, panelW);

        ChromeExpediente.esquinas(g, panelX, panelY, panelW, panelH);
        ChromeExpediente.pie(g, this.font, panelX, panelY, panelW, panelH, "CFG-014");

        // OptionsScreen.render() vuelve a dibujar fondo y titulo vanilla. No se
        // llama aqui: solo se renderizan los widgets Jobs ya registrados.
        for (var child : this.children()) {
            if (!child.getClass().getName().startsWith("com.santipdr.jobsmenu.")) continue;
            if (child instanceof Renderable renderable) {
                renderable.render(g, mouseX, mouseY, partialTick);
            }
        }
    }

    private void abrirPiel() {
        this.minecraft.setScreen(new PantallaPielJobs(this, this.opciones));
    }

    private void abrirSonido() {
        this.minecraft.setScreen(new PantallaSonidoJobs(this, this.opciones));
    }

    private void abrirVideo() {
        // Nunca construye una pantalla grafica por su cuenta. Ejecuta el boton
        // que el OptionsScreen real termino teniendo despues de los hooks del
        // modpack. Si el proveedor sustituyo el control y cambio su etiqueta,
        // la ranura memorizada permite conservar igualmente su callback.
        sincronizarControlesNaturales();
        if (this.botonVideoNatural != null && this.botonVideoNatural.active) {
            this.botonVideoNatural.onPress();
        } else {
            MezclaAudio.gesto(SonidosNivel.UI_NEGADO, 0.42F);
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

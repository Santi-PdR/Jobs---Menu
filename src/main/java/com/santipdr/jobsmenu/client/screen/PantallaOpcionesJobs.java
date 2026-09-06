package com.santipdr.jobsmenu.client.screen;

import com.santipdr.jobsmenu.client.ui.BotonExpediente;
import com.santipdr.jobsmenu.client.ui.ChromeExpediente;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
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
    private long aperturasVideoNaturales;
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

        // Construye primero el OptionsScreen real. Mixins que sustituyen la
        // accion de Graficos trabajan sobre esta misma instancia Jobs.
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
     * los deja visibles ni clickeables debajo del chrome Jobs. Se repite en el
     * primer render porque Forge permite que otros mods alteren OptionsScreen
     * despues de que init() haya terminado.
     */
    private void sincronizarControlesNaturales() {
        String video = Component.translatable("options.video").getString();
        AbstractButton encontrado = null;

        for (var child : this.children()) {
            String clase = child.getClass().getName();
            boolean jobs = clase.startsWith("com.santipdr.jobsmenu.");
            if (jobs) continue;

            if (child instanceof AbstractButton boton
                    && video.equals(boton.getMessage().getString())) {
                encontrado = boton;
            }
            if (child instanceof AbstractWidget widget) {
                widget.visible = false;
            }
        }

        if (encontrado != null) {
            this.botonVideoNatural = encontrado;
        }
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
        super.render(g, mouseX, mouseY, partialTick);
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
        // modpack. Esto conserva Embeddium y cualquier otra integracion.
        sincronizarControlesNaturales();
        if (this.botonVideoNatural != null && this.botonVideoNatural.active) {
            this.aperturasVideoNaturales++;
            this.botonVideoNatural.onPress();
        }
    }

    public boolean videoNaturalDisponibleParaDiagnostico() {
        return this.botonVideoNatural != null;
    }

    public long aperturasVideoNaturalesParaDiagnostico() {
        return this.aperturasVideoNaturales;
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

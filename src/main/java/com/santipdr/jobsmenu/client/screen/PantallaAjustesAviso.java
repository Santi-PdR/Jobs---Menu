package com.santipdr.jobsmenu.client.screen;

import com.santipdr.jobsmenu.client.ui.BotonExpediente;
import com.santipdr.jobsmenu.client.ui.ChromeExpediente;
import com.santipdr.jobsmenu.client.ui.Paleta;
import com.santipdr.jobsmenu.client.ui.SliderExpediente;
import com.santipdr.jobsmenu.client.ui.ToggleExpediente;
import com.santipdr.jobsmenu.config.ConfigTurno;

import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Configuracion nativa de Jobs. No usa OptionsList ni widgets visuales vanilla. */
public class PantallaAjustesAviso extends Screen {

    private enum Categoria {
        VISUAL("jobsmenu.ajustes.categoria.visual", "options.video", "jobsmenu.ajustes.escena.detalle"),
        NIVEL("jobsmenu.ajustes.categoria.nivel", "jobsmenu.ajustes.nivelfijo", "jobsmenu.ajustes.nivelfijo.detalle"),
        AUDIO("jobsmenu.ajustes.categoria.audio", "soundCategory.music", "jobsmenu.ajustes.volambiente.detalle"),
        ACCESIBILIDAD("jobsmenu.ajustes.categoria.accesibilidad", "options.accessibility.title", "jobsmenu.ajustes.perfil.detalle"),
        SISTEMA("jobsmenu.ajustes.categoria.sistema", "jobsmenu.ajustes.categoria.sistema", "jobsmenu.ajustes.menu.detalle");

        private final String pestana;
        private final String titulo;
        private final String detalle;

        Categoria(String pestana, String titulo, String detalle) {
            this.pestana = pestana;
            this.titulo = titulo;
            this.detalle = detalle;
        }
    }

    private final Screen anterior;
    private final Options opciones;
    private final Categoria categoria;

    private int panelX;
    private int panelY;
    private int panelW;
    private int panelH;
    private boolean compacta;
    private int contentY;
    private int tabsX, tabsY, tabW, tabH, tabGap;

    public PantallaAjustesAviso(Screen anterior, Options opciones) {
        this(anterior, opciones, Categoria.VISUAL);
    }

    private PantallaAjustesAviso(Screen anterior, Options opciones, Categoria categoria) {
        super(Component.translatable("jobsmenu.ajustes.titulo"));
        this.anterior = anterior;
        this.opciones = opciones;
        this.categoria = categoria == null ? Categoria.VISUAL : categoria;
    }

    @Override
    protected void init() {
        this.panelW = Math.max(1, Math.min(480, this.width - 18));
        this.panelH = Math.max(1, Math.min(300, this.height - 16));
        this.panelX = (this.width - this.panelW) / 2;
        this.panelY = Math.max(4, (this.height - this.panelH) / 2);
        this.compacta = this.panelW < 430 || this.panelH < 296;

        int margen = compacta ? 14 : 20;
        int gap = compacta ? 5 : 7;
        int anchoUtil = Math.max(1, panelW - margen * 2);
        int tabsY = panelY + (compacta ? 48 : 53);
        int tabH = compacta ? 18 : 20;
        int tabW = Math.max(44, (anchoUtil - gap * 4) / 5);
        this.tabsX = panelX + margen;
        this.tabsY = tabsY;
        this.tabW = tabW;
        this.tabH = tabH;
        this.tabGap = gap;

        int x = panelX + margen;
        for (Categoria c : Categoria.values()) {
            BotonExpediente boton = new BotonExpediente(
                    x, tabsY, tabW, tabH,
                    Component.translatable(c.pestana),
                    c == this.categoria ? BotonExpediente.Tipo.JOBS : BotonExpediente.Tipo.NORMAL,
                    () -> abrirCategoria(c));
            boton.setTooltip(Tooltip.create(Component.translatable(c.detalle)));
            this.addRenderableWidget(boton);
            x += tabW + gap;
        }

        this.contentY = tabsY + tabH + (compacta ? 9 : 16);
        switch (this.categoria) {
            case VISUAL -> construirVisual(margen, gap);
            case NIVEL -> construirNivel(margen, gap);
            case AUDIO -> construirAudio(margen, gap);
            case ACCESIBILIDAD -> construirAccesibilidad(margen, gap);
            case SISTEMA -> construirSistema(margen, gap);
        }

        int volverH = compacta ? 19 : 21;
        int volverW = Math.min(170, anchoUtil);
        this.addRenderableWidget(new BotonExpediente(
                this.width / 2 - volverW / 2,
                panelY + panelH - volverH - 8,
                volverW, volverH,
                Component.translatable("jobsmenu.interfaz.volver"),
                BotonExpediente.Tipo.PRINCIPAL,
                this::onClose));
    }

    private void construirVisual(int margen, int gap) {
        Grid grid = new Grid(margen, gap);
        grid.togglePar("jobsmenu.ajustes.escena", ConfigTurno::escenaViva, ConfigTurno::fijarEscenaViva,
                "jobsmenu.ajustes.estado", ConfigTurno::mostrarEstadoInstalacion, ConfigTurno::fijarMostrarEstadoInstalacion);
        grid.togglePar("jobsmenu.ajustes.respiracion", ConfigTurno::respiracionCamara, ConfigTurno::fijarRespiracionCamara,
                "jobsmenu.ajustes.presencia", ConfigTurno::presenciaFondo, ConfigTurno::fijarPresenciaFondo);
        grid.togglePar("jobsmenu.ajustes.eventos", ConfigTurno::eventosAmbientales, ConfigTurno::fijarEventosAmbientales,
                "jobsmenu.ajustes.papel", ConfigTurno::papelLimpio, ConfigTurno::fijarPapelLimpio);
        grid.togglePar("jobsmenu.ajustes.guia", ConfigTurno::guiaLectura, ConfigTurno::fijarGuiaLectura,
                "jobsmenu.ajustes.interfaz", ConfigTurno::interfazMinima, ConfigTurno::fijarInterfazMinima);
        grid.togglePar("jobsmenu.ajustes.alto", ConfigTurno::altoContraste, ConfigTurno::fijarAltoContraste,
                "jobsmenu.ajustes.grande", ConfigTurno::textoGrande, ConfigTurno::fijarTextoGrande);
    }

    private void construirNivel(int margen, int gap) {
        Grid grid = new Grid(margen, gap);
        grid.togglePar("jobsmenu.ajustes.rotar", ConfigTurno::rotarNivelesBruto, ConfigTurno::fijarRotarNiveles,
                "jobsmenu.ajustes.cuenta", ConfigTurno::mostrarCuentaRegresivaBruto, ConfigTurno::fijarMostrarCuentaRegresiva);
        grid.slider("jobsmenu.ajustes.nivelfijo.detalle", 0, 17,
                ConfigTurno.nivelFijo(), ConfigTurno::fijarNivelFijo,
                v -> Component.translatable("jobsmenu.ajustes.nivelvalor",
                        Component.translatable("jobsmenu.ajustes.nivelfijo"), v));
        grid.slider("jobsmenu.ajustes.estancia.detalle", 15, 90,
                ConfigTurno.duracionEstancia(), ConfigTurno::fijarDuracionEstancia,
                v -> Component.translatable("jobsmenu.ajustes.segundos",
                        Component.translatable("jobsmenu.ajustes.estancia"), v));
        grid.togglePar("jobsmenu.ajustes.rotacioncalma", ConfigTurno::rotacionCalma, ConfigTurno::fijarRotacionCalma,
                "jobsmenu.ajustes.avisos", ConfigTurno::avisosRotativosBruto, ConfigTurno::fijarAvisosRotativos);
        grid.slider("jobsmenu.ajustes.duracion.detalle", 4, 15,
                ConfigTurno.duracionAvisos(), ConfigTurno::fijarDuracionAvisos,
                v -> Component.translatable("jobsmenu.ajustes.segundos",
                        Component.translatable("jobsmenu.ajustes.duracion"), v));
        grid.toggleCompleto("jobsmenu.ajustes.fecha", ConfigTurno::mostrarFechaBruto, ConfigTurno::fijarMostrarFecha);
    }

    private void construirAudio(int margen, int gap) {
        Grid grid = new Grid(margen, gap);
        grid.slider("jobsmenu.ajustes.volaviso.detalle", 0, 100,
                ConfigTurno.volumenAvisoPorcentaje(), ConfigTurno::fijarVolumenAviso,
                v -> Component.translatable("jobsmenu.ajustes.porciento",
                        Component.translatable("jobsmenu.ajustes.volaviso"), v));
        grid.slider("jobsmenu.ajustes.volmusica.detalle", 0, 100,
                ConfigTurno.volumenMusicaPorcentaje(), ConfigTurno::fijarVolumenMusica,
                v -> Component.translatable("jobsmenu.ajustes.porciento",
                        Component.translatable("jobsmenu.ajustes.volmusica"), v));
        grid.slider("jobsmenu.ajustes.volambiente.detalle", 0, 100,
                ConfigTurno.volumenAmbientePorcentaje(), ConfigTurno::fijarVolumenAmbiente,
                v -> Component.translatable("jobsmenu.ajustes.porciento",
                        Component.translatable("jobsmenu.ajustes.volambiente"), v));
        grid.togglePar("jobsmenu.ajustes.musica", ConfigTurno::musicaMenu, ConfigTurno::fijarMusicaMenu,
                "jobsmenu.ajustes.ambiente", ConfigTurno::sonidoAmbiente, ConfigTurno::fijarSonidoAmbiente);
        grid.togglePar("jobsmenu.ajustes.botones", ConfigTurno::sonidoBotones, ConfigTurno::fijarSonidoBotones,
                "jobsmenu.ajustes.credito", ConfigTurno::creditoMusica, ConfigTurno::fijarCreditoMusica);
    }

    private void construirAccesibilidad(int margen, int gap) {
        Grid grid = new Grid(margen, gap);
        grid.toggleCompleto("jobsmenu.ajustes.perfil", ConfigTurno::perfilAccesible, ConfigTurno::fijarPerfilAccesible);
        grid.togglePar("jobsmenu.ajustes.movimiento", ConfigTurno::movimientoReducido, ConfigTurno::fijarMovimientoReducido,
                "jobsmenu.ajustes.destellos", ConfigTurno::destellosReducidos, ConfigTurno::fijarDestellosReducidos);
        grid.togglePar("jobsmenu.ajustes.bajoconsumo", ConfigTurno::bajoConsumo, ConfigTurno::fijarBajoConsumo,
                "jobsmenu.ajustes.alto", ConfigTurno::altoContraste, ConfigTurno::fijarAltoContraste);
        grid.togglePar("jobsmenu.ajustes.grande", ConfigTurno::textoGrande, ConfigTurno::fijarTextoGrande,
                "jobsmenu.ajustes.papel", ConfigTurno::papelLimpio, ConfigTurno::fijarPapelLimpio);
        grid.toggleCompleto("jobsmenu.ajustes.guia", ConfigTurno::guiaLectura, ConfigTurno::fijarGuiaLectura);
    }

    private void construirSistema(int margen, int gap) {
        Grid grid = new Grid(margen, gap);
        grid.toggleCompleto("jobsmenu.ajustes.suspension", ConfigTurno::suspensionRara, ConfigTurno::fijarSuspensionRara);
        grid.togglePar("jobsmenu.ajustes.menu", ConfigTurno::menuPropio, ConfigTurno::fijarMenuPropio,
                "jobsmenu.ajustes.pausa", ConfigTurno::pausaPropia, ConfigTurno::fijarPausaPropia);
        grid.toggleCompleto("jobsmenu.ajustes.rotacioncalma", ConfigTurno::rotacionCalma, ConfigTurno::fijarRotacionCalma);
    }

    private void abrirCategoria(Categoria nueva) {
        if (nueva == this.categoria) return;
        ConfigTurno.guardarPendiente();
        this.minecraft.setScreen(new PantallaAjustesAviso(this.anterior, this.opciones, nueva));
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        ChromeExpediente.fondo(g, this.width, this.height);
        ChromeExpediente.panel(g, panelX, panelY, panelW, panelH);
        ChromeExpediente.cabecera(g, this.font, this.title,
                Component.translatable("jobsmenu.interfaz.aviso.subtitulo"), panelX, panelY, panelW);

        int margen = compacta ? 14 : 20;
        ChromeExpediente.seccion(g, this.font, panelX + margen, panelX + panelW - margen,
                this.contentY - (compacta ? 6 : 9), Component.translatable(this.categoria.titulo));

        int indice = this.categoria.ordinal();
        int seleccionadoX = this.tabsX + indice * (this.tabW + this.tabGap);
        g.fill(seleccionadoX + 5, this.tabsY + this.tabH - 2,
                seleccionadoX + this.tabW - 5, this.tabsY + this.tabH,
                Paleta.conAlfa(Paleta.FLUOR, 0.62F));
        String pagina = String.format(java.util.Locale.ROOT, "%02d / %02d", indice + 1, Categoria.values().length);
        int paginaW = this.font.width(pagina) + 10;
        int paginaX = panelX + panelW - margen - paginaW;
        int paginaY = panelY + 12;
        g.fill(paginaX, paginaY, paginaX + paginaW, paginaY + 14,
                Paleta.conAlfa(Paleta.PARED_ALTA, 0.10F));
        g.drawString(this.font, pagina, paginaX + 5, paginaY + 3,
                Paleta.conAlfa(Paleta.tintaSecundaria(), 0.62F), false);

        ChromeExpediente.esquinas(g, panelX, panelY, panelW, panelH);
        ChromeExpediente.pie(g, this.font, panelX, panelY, panelW, panelH, "JOBS-0161");
        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode >= org.lwjgl.glfw.GLFW.GLFW_KEY_1 && keyCode <= org.lwjgl.glfw.GLFW.GLFW_KEY_5) {
            abrirCategoria(Categoria.values()[keyCode - org.lwjgl.glfw.GLFW.GLFW_KEY_1]);
            return true;
        }
        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT) {
            abrirCategoria(Categoria.values()[Math.floorMod(this.categoria.ordinal() - 1, Categoria.values().length)]);
            return true;
        }
        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT) {
            abrirCategoria(Categoria.values()[(this.categoria.ordinal() + 1) % Categoria.values().length]);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        ConfigTurno.guardarPendiente();
        if (this.minecraft != null) this.minecraft.setScreen(this.anterior);
    }

    @Override
    public void removed() {
        ConfigTurno.guardarPendiente();
        super.removed();
    }

    @Override
    public void renderBackground(GuiGraphics g) {
        // ChromeExpediente renders the complete background.
    }

    private final class Grid {
        private final int x0;
        private final int x1;
        private final int colW;
        private final int gap;
        private final int alto;
        private final int paso;
        private int fila;

        Grid(int margen, int gap) {
            int ancho = Math.max(1, panelW - margen * 2);
            this.gap = gap;
            this.colW = Math.max(70, (ancho - gap) / 2);
            this.x0 = panelX + margen;
            this.x1 = x0 + colW + gap;
            this.alto = compacta ? 19 : 22;
            this.paso = compacta ? 23 : 27;
        }

        void togglePar(String claveA, java.util.function.BooleanSupplier leerA,
                       java.util.function.Consumer<Boolean> fijarA,
                       String claveB, java.util.function.BooleanSupplier leerB,
                       java.util.function.Consumer<Boolean> fijarB) {
            int y = contentY + fila * paso;
            registrarAyuda(addRenderableWidget(new ToggleExpediente(x0, y, colW, alto,
                    Component.translatable(claveA), leerA, fijarA)), claveA + ".detalle");
            registrarAyuda(addRenderableWidget(new ToggleExpediente(x1, y, colW, alto,
                    Component.translatable(claveB), leerB, fijarB)), claveB + ".detalle");
            fila++;
        }

        void toggleCompleto(String clave, java.util.function.BooleanSupplier leer,
                            java.util.function.Consumer<Boolean> fijar) {
            int y = contentY + fila * paso;
            registrarAyuda(addRenderableWidget(new ToggleExpediente(x0, y, colW * 2 + gap, alto,
                    Component.translatable(clave), leer, fijar)), clave + ".detalle");
            fila++;
        }

        void slider(String detalle, int min, int max, int valor,
                    java.util.function.IntConsumer fijar,
                    java.util.function.IntFunction<Component> rotulo) {
            int y = contentY + fila * paso;
            registrarAyuda(addRenderableWidget(new SliderExpediente(x0, y, colW * 2 + gap, alto,
                    min, max, valor, rotulo, fijar)), detalle);
            fila++;
        }
    }

    private static <T extends AbstractWidget> T registrarAyuda(T widget, String claveDetalle) {
        widget.setTooltip(Tooltip.create(Component.translatable(claveDetalle)));
        return widget;
    }
}

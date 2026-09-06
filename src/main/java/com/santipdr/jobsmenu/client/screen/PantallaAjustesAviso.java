package com.santipdr.jobsmenu.client.screen;

import com.santipdr.jobsmenu.client.sound.MezclaAudio;
import com.santipdr.jobsmenu.client.sound.SonidosNivel;
import com.santipdr.jobsmenu.client.ui.BotonExpediente;
import com.santipdr.jobsmenu.client.ui.ChromeExpediente;
import com.santipdr.jobsmenu.client.ui.Paleta;
import com.santipdr.jobsmenu.client.ui.PulidoInterfazJobs;
import com.santipdr.jobsmenu.client.ui.SliderExpediente;
import com.santipdr.jobsmenu.client.ui.ToggleExpediente;
import com.santipdr.jobsmenu.config.ConfigTurno;
import com.santipdr.jobsmenu.config.PerfilesJobs;

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
        SISTEMA("jobsmenu.ajustes.categoria.sistema", "jobsmenu.ajustes.categoria.sistema", "jobsmenu.ajustes.menu.detalle"),
        PERFILES("jobsmenu.ajustes.perfil", "jobsmenu.ajustes.perfil", "jobsmenu.ajustes.perfil.detalle");

        private final String pestana;
        private final String titulo;
        private final String detalle;

        Categoria(String pestana, String titulo, String detalle) {
            this.pestana = pestana;
            this.titulo = titulo;
            this.detalle = detalle;
        }
    }

    private static Categoria ultimaCategoria = Categoria.VISUAL;

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
    private boolean cerrando;

    public PantallaAjustesAviso(Screen anterior, Options opciones) {
        this(anterior, opciones, ultimaCategoria);
    }

    private PantallaAjustesAviso(Screen anterior, Options opciones, Categoria categoria) {
        super(Component.translatable("jobsmenu.ajustes.titulo"));
        this.anterior = anterior;
        this.opciones = opciones;
        this.categoria = categoria == null ? Categoria.VISUAL : categoria;
        ultimaCategoria = this.categoria;
    }

    @Override
    protected void init() {
        this.cerrando = false;
        this.panelW = Math.max(1, Math.min(500, this.width - 18));
        this.panelH = Math.max(1, Math.min(312, this.height - 16));
        this.panelX = (this.width - this.panelW) / 2;
        this.panelY = Math.max(4, (this.height - this.panelH) / 2);
        this.compacta = this.panelW < 448 || this.panelH < 304;

        int margen = compacta ? 12 : 18;
        int gap = compacta ? 4 : 6;
        int anchoUtil = Math.max(1, panelW - margen * 2);
        int tabsY = panelY + (compacta ? 47 : 52);
        int tabH = compacta ? 18 : 20;
        int cantidadTabs = Categoria.values().length;
        int tabW = Math.max(42, (anchoUtil - gap * (cantidadTabs - 1)) / cantidadTabs);
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

        this.contentY = tabsY + tabH + (compacta ? 9 : 15);
        switch (this.categoria) {
            case VISUAL -> construirVisual(margen, gap);
            case NIVEL -> construirNivel(margen, gap);
            case AUDIO -> construirAudio(margen, gap);
            case ACCESIBILIDAD -> construirAccesibilidad(margen, gap);
            case SISTEMA -> construirSistema(margen, gap);
            case PERFILES -> construirPerfiles(margen, gap);
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
        grid.slider("jobsmenu.ajustes.nivelfijo.detalle", 0, 31,
                ConfigTurno.nivelFijo(), ConfigTurno::fijarNivelFijo,
                v -> Component.translatable("jobsmenu.ajustes.nivelvalor",
                        Component.translatable("jobsmenu.ajustes.nivelfijo"), v),
                v -> Component.literal(v + "/31"));
        grid.slider("jobsmenu.ajustes.estancia.detalle", 15, 90,
                ConfigTurno.duracionEstancia(), ConfigTurno::fijarDuracionEstancia,
                v -> Component.translatable("jobsmenu.ajustes.segundos",
                        Component.translatable("jobsmenu.ajustes.estancia"), v),
                v -> Component.literal(v + " s"));
        grid.togglePar("jobsmenu.ajustes.rotacioncalma", ConfigTurno::rotacionCalma, ConfigTurno::fijarRotacionCalma,
                "jobsmenu.ajustes.avisos", ConfigTurno::avisosRotativosBruto, ConfigTurno::fijarAvisosRotativos);
        grid.slider("jobsmenu.ajustes.duracion.detalle", 4, 15,
                ConfigTurno.duracionAvisos(), ConfigTurno::fijarDuracionAvisos,
                v -> Component.translatable("jobsmenu.ajustes.segundos",
                        Component.translatable("jobsmenu.ajustes.duracion"), v),
                v -> Component.literal(v + " s"));
        grid.toggleCompleto("jobsmenu.ajustes.fecha", ConfigTurno::mostrarFechaBruto, ConfigTurno::fijarMostrarFecha);
    }

    private void construirAudio(int margen, int gap) {
        Grid grid = new Grid(margen, gap);
        grid.slider("jobsmenu.ajustes.volaviso.detalle", 0, 100,
                ConfigTurno.volumenAvisoPorcentaje(), ConfigTurno::fijarVolumenAviso,
                v -> Component.translatable("jobsmenu.ajustes.porciento",
                        Component.translatable("jobsmenu.ajustes.volaviso"), v),
                v -> Component.literal(v + "%"));
        grid.slider("jobsmenu.ajustes.volmusica.detalle", 0, 100,
                ConfigTurno.volumenMusicaPorcentaje(), ConfigTurno::fijarVolumenMusica,
                v -> Component.translatable("jobsmenu.ajustes.porciento",
                        Component.translatable("jobsmenu.ajustes.volmusica"), v),
                v -> Component.literal(v + "%"));
        grid.slider("jobsmenu.ajustes.volambiente.detalle", 0, 100,
                ConfigTurno.volumenAmbientePorcentaje(), ConfigTurno::fijarVolumenAmbiente,
                v -> Component.translatable("jobsmenu.ajustes.porciento",
                        Component.translatable("jobsmenu.ajustes.volambiente"), v),
                v -> Component.literal(v + "%"));
        grid.slider("jobsmenu.ajustes.pista.detalle", 0, 3,
                ConfigTurno.pistaMusica(), ConfigTurno::fijarPistaMusica,
                v -> Component.translatable("jobsmenu.ajustes.pista.valor", nombrePista(v)),
                v -> Component.literal(v + "/3"));
        grid.toggleCuatro(
                "jobsmenu.ajustes.musica", ConfigTurno::musicaMenu, ConfigTurno::fijarMusicaMenu,
                "jobsmenu.ajustes.ambiente", ConfigTurno::sonidoAmbiente, ConfigTurno::fijarSonidoAmbiente,
                "jobsmenu.ajustes.botones", ConfigTurno::sonidoBotones, PantallaAjustesAviso::fijarSonidoBotones,
                "jobsmenu.ajustes.credito", ConfigTurno::creditoMusica, ConfigTurno::fijarCreditoMusica);
    }

    /**
     * Al reactivar los gestos, confirma el cambio despues de persistirlo.
     * El click previo se omite porque la opcion aun estaba apagada.
     */
    private static void fijarSonidoBotones(boolean activo) {
        ConfigTurno.fijarSonidoBotones(activo);
        if (activo) {
            MezclaAudio.gesto(SonidosNivel.UI_ALTERNAR, 0.52F);
        }
    }

    private static Component nombrePista(int pista) {
        return Component.translatable(switch (pista) {
            case 1 -> "jobsmenu.ajustes.pista.absurdism";
            case 2 -> "jobsmenu.ajustes.pista.requiem";
            case 3 -> "jobsmenu.ajustes.pista.upon";
            default -> "jobsmenu.ajustes.pista.aleatoria";
        });
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

    private void construirPerfiles(int margen, int gap) {
        Grid grid = new Grid(margen, gap);
        PerfilesJobs.Perfil actual = PerfilesJobs.actual();
        PerfilesJobs.Perfil[] perfiles = PerfilesJobs.Perfil.values();
        for (int i = 0; i < perfiles.length; i += 2) {
            PerfilesJobs.Perfil a = perfiles[i];
            PerfilesJobs.Perfil b = i + 1 < perfiles.length ? perfiles[i + 1] : null;
            grid.botonPerfiles(a, b, actual);
        }
    }

    private void aplicarPerfil(PerfilesJobs.Perfil perfil) {
        PerfilesJobs.aplicar(perfil);
        PulidoInterfazJobs.confirmarCambio();
        if (this.minecraft != null) {
            this.minecraft.setScreen(new PantallaAjustesAviso(this.anterior, this.opciones, Categoria.PERFILES));
        }
    }

    private void abrirCategoria(Categoria nueva) {
        if (nueva == this.categoria || this.minecraft == null) return;
        ultimaCategoria = nueva;
        ConfigTurno.guardarPendiente();
        this.minecraft.setScreen(new PantallaAjustesAviso(this.anterior, this.opciones, nueva));
    }

    void abrirCategoriaDesdeBusqueda(int indice) {
        if (this.minecraft == null) return;
        Categoria[] categorias = Categoria.values();
        if (indice < 0 || indice >= categorias.length) return;

        Categoria nueva = categorias[indice];
        ultimaCategoria = nueva;
        ConfigTurno.guardarPendiente();
        if (nueva == this.categoria) {
            this.minecraft.setScreen(this);
            return;
        }
        this.minecraft.setScreen(new PantallaAjustesAviso(this.anterior, this.opciones, nueva));
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        ChromeExpediente.fondo(g, this.width, this.height);
        ChromeExpediente.panel(g, panelX, panelY, panelW, panelH);
        ChromeExpediente.cabecera(g, this.font, this.title,
                Component.translatable("jobsmenu.interfaz.aviso.subtitulo"), panelX, panelY, panelW);

        int margen = compacta ? 12 : 18;
        ChromeExpediente.seccion(g, this.font, panelX + margen, panelX + panelW - margen,
                this.contentY - (compacta ? 6 : 9), Component.translatable(this.categoria.titulo));

        int indice = this.categoria.ordinal();
        int seleccionadoX = this.tabsX + indice * (this.tabW + this.tabGap);
        g.fill(seleccionadoX + 4, this.tabsY + this.tabH - 2,
                seleccionadoX + this.tabW - 4, this.tabsY + this.tabH,
                Paleta.conAlfa(Paleta.UI_ACENTO_FUERTE, 0.76F));
        g.fill(seleccionadoX + 1, this.tabsY + 3, seleccionadoX + 2, this.tabsY + this.tabH - 3,
                Paleta.conAlfa(Paleta.UI_ACENTO, 0.34F));

        String pagina = String.format(java.util.Locale.ROOT, "%02d / %02d", indice + 1, Categoria.values().length);
        int paginaW = this.font.width(pagina) + 10;
        int paginaX = panelX + panelW - margen - paginaW;
        int paginaY = panelY + 12;
        g.fill(paginaX, paginaY, paginaX + paginaW, paginaY + 14,
                Paleta.conAlfa(Paleta.UI_ACENTO, 0.14F));
        g.drawString(this.font, pagina, paginaX + 5, paginaY + 3,
                Paleta.conAlfa(Paleta.tintaSecundaria(), 0.66F), false);

        dibujarEstadoGlobal(g, margen);
        dibujarAyudaBusqueda(g, margen);
        if (this.categoria == Categoria.PERFILES) dibujarAyudaPerfiles(g, margen);

        ChromeExpediente.esquinas(g, panelX, panelY, panelW, panelH);
        ChromeExpediente.pie(g, this.font, panelX, panelY, panelW, panelH, "JOBS-CONFIG");
        super.render(g, mouseX, mouseY, partialTick);
    }

    private void dibujarEstadoGlobal(GuiGraphics g, int margen) {
        PerfilesJobs.Perfil actual = PerfilesJobs.actual();
        String estado = actual == null
                ? "CUSTOM"
                : Component.translatable(actual.claveNombre()).getString();
        estado = ChromeExpediente.ajustar(this.font, estado, Math.max(48, panelW / 4));
        int w = Math.min(panelW / 3, this.font.width(estado) + 12);
        int x = panelX + margen;
        int y = panelY + 12;
        g.fill(x, y, x + w, y + 14, Paleta.conAlfa(Paleta.UI_ACENTO, 0.10F));
        g.fill(x, y, x + 2, y + 14, Paleta.conAlfa(Paleta.UI_ACENTO_FUERTE, 0.44F));
        g.drawString(this.font, ChromeExpediente.ajustar(this.font, estado, w - 8), x + 6, y + 3,
                Paleta.conAlfa(Paleta.tintaSecundaria(), 0.68F), false);
    }

    private void dibujarAyudaBusqueda(GuiGraphics g, int margen) {
        if (this.panelW < 330) return;
        int y = panelY + panelH - (compacta ? 41 : 44);
        String ayuda = "CTRL+F";
        g.drawString(this.font, ayuda, panelX + margen, y + 5,
                Paleta.conAlfa(Paleta.tintaSecundaria(), 0.48F), false);
    }

    private void dibujarAyudaPerfiles(GuiGraphics g, int margen) {
        int y = panelY + panelH - (compacta ? 41 : 44);
        int x0 = panelX + margen;
        int x1 = panelX + panelW - margen;
        g.fill(x0, y, x1, y + 1, Paleta.conAlfa(Paleta.tintaSecundaria(), 0.13F));
        String ayuda = "F1 - F5";
        int tw = this.font.width(ayuda);
        g.drawString(this.font, ayuda, x0 + (x1 - x0 - tw) / 2, y + 5,
                Paleta.conAlfa(Paleta.tintaSecundaria(), 0.54F), false);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        int max = Categoria.values().length;
        if (keyCode >= org.lwjgl.glfw.GLFW.GLFW_KEY_1
                && keyCode < org.lwjgl.glfw.GLFW.GLFW_KEY_1 + max) {
            abrirCategoria(Categoria.values()[keyCode - org.lwjgl.glfw.GLFW.GLFW_KEY_1]);
            return true;
        }
        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT) {
            abrirCategoria(Categoria.values()[Math.floorMod(this.categoria.ordinal() - 1, max)]);
            return true;
        }
        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT) {
            abrirCategoria(Categoria.values()[(this.categoria.ordinal() + 1) % max]);
            return true;
        }
        if (this.categoria == Categoria.PERFILES
                && keyCode >= org.lwjgl.glfw.GLFW.GLFW_KEY_F1
                && keyCode <= org.lwjgl.glfw.GLFW.GLFW_KEY_F5) {
            aplicarPerfil(PerfilesJobs.Perfil.values()[keyCode - org.lwjgl.glfw.GLFW.GLFW_KEY_F1]);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        if (this.cerrando || this.minecraft == null) return;
        this.cerrando = true;
        ConfigTurno.guardarPendiente();
        this.minecraft.setScreen(this.anterior);
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

        void toggleCuatro(
                String claveA, java.util.function.BooleanSupplier leerA, java.util.function.Consumer<Boolean> fijarA,
                String claveB, java.util.function.BooleanSupplier leerB, java.util.function.Consumer<Boolean> fijarB,
                String claveC, java.util.function.BooleanSupplier leerC, java.util.function.Consumer<Boolean> fijarC,
                String claveD, java.util.function.BooleanSupplier leerD, java.util.function.Consumer<Boolean> fijarD) {
            int y = contentY + fila * paso;
            int total = colW * 2 + gap;
            int gapCorto = Math.max(2, gap - 2);
            int w = Math.max(44, (total - gapCorto * 3) / 4);
            String[] claves = {claveA, claveB, claveC, claveD};
            java.util.function.BooleanSupplier[] lecturas = {leerA, leerB, leerC, leerD};
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<Boolean>[] setters =
                    (java.util.function.Consumer<Boolean>[]) new java.util.function.Consumer<?>[] {
                            fijarA, fijarB, fijarC, fijarD
                    };
            for (int i = 0; i < 4; i++) {
                int x = x0 + i * (w + gapCorto);
                registrarAyuda(addRenderableWidget(new ToggleExpediente(x, y, w, alto,
                        Component.translatable(claves[i]), lecturas[i], setters[i])),
                        claves[i] + ".detalle");
            }
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
                    java.util.function.IntFunction<Component> rotulo,
                    java.util.function.IntFunction<Component> lecturaCorta) {
            int y = contentY + fila * paso;
            registrarAyuda(addRenderableWidget(new SliderExpediente(x0, y, colW * 2 + gap, alto,
                    min, max, valor, rotulo, fijar, lecturaCorta)), detalle);
            fila++;
        }

        void botonPerfiles(PerfilesJobs.Perfil a, PerfilesJobs.Perfil b, PerfilesJobs.Perfil actual) {
            int y = contentY + fila * paso;
            botonPerfil(x0, y, colW, a, actual);
            if (b != null) botonPerfil(x1, y, colW, b, actual);
            fila++;
        }

        private void botonPerfil(int x, int y, int w, PerfilesJobs.Perfil perfil, PerfilesJobs.Perfil actual) {
            BotonExpediente.Tipo tipo = perfil == actual
                    ? BotonExpediente.Tipo.JOBS : BotonExpediente.Tipo.NORMAL;
            BotonExpediente boton = new BotonExpediente(x, y, w, alto,
                    Component.translatable(perfil.claveNombre()), tipo,
                    () -> aplicarPerfil(perfil));
            boton.setTooltip(Tooltip.create(Component.translatable(perfil.claveDetalle())));
            addRenderableWidget(boton);
        }
    }

    private static <T extends AbstractWidget> T registrarAyuda(T widget, String claveDetalle) {
        widget.setTooltip(Tooltip.create(Component.translatable(claveDetalle)));
        return widget;
    }
}

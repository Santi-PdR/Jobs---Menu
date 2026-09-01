package com.santipdr.jobsmenu.client.screen;

import com.santipdr.jobsmenu.client.ui.BotonExpediente;
import com.santipdr.jobsmenu.client.ui.ChromeExpediente;
import com.santipdr.jobsmenu.config.ConfigTurno;

import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Ajustes propios del aviso, integrados visualmente con el resto de Jobs. */
public class PantallaAjustesAviso extends OptionsSubScreen {

    private OptionsList lista;

    public PantallaAjustesAviso(Screen anterior, Options opciones) {
        super(anterior, opciones, Component.translatable("jobsmenu.ajustes.titulo"));
    }

    private static OptionInstance<Boolean> interruptor(String clave, boolean valor,
                                                       java.util.function.Consumer<Boolean> fijar) {
        return OptionInstance.createBoolean(clave,
                OptionInstance.cachedConstantTooltip(Component.translatable(clave + ".detalle")),
                valor, fijar::accept);
    }

    private static OptionInstance<Integer> deslizador(String clave, int valor,
                                                      java.util.function.IntConsumer fijar) {
        return deslizador(clave, valor, 0, 100,
                "jobsmenu.ajustes.porciento", fijar);
    }

    private static OptionInstance<Integer> deslizador(String clave, int valor,
                                                      int minimo, int maximo,
                                                      String formato,
                                                      java.util.function.IntConsumer fijar) {
        return new OptionInstance<>(clave,
                OptionInstance.cachedConstantTooltip(Component.translatable(clave + ".detalle")),
                (caption, v) -> Component.translatable(formato, caption, v),
                new OptionInstance.IntRange(minimo, maximo),
                Math.max(minimo, Math.min(maximo, valor)),
                (v) -> fijar.accept(v));
    }

    private static OptionInstance<Integer> selectorNivel(int valor) {
        String clave = "jobsmenu.ajustes.nivelfijo";
        return new OptionInstance<>(clave,
                OptionInstance.cachedConstantTooltip(Component.translatable(clave + ".detalle")),
                (caption, v) -> Component.translatable("jobsmenu.ajustes.nivelvalor", caption, v),
                new OptionInstance.IntRange(0, 17),
                Math.max(0, Math.min(17, valor)),
                ConfigTurno::fijarNivelFijo);
    }

    @Override
    protected void init() {
        // Cabecera 0-47 y footer desde height-48 quedan reservados. Ninguna fila
        // de opciones puede entrar en el hitbox de Volver o en el pie del papel.
        this.lista = new OptionsList(this.minecraft, this.width, this.height, 50, this.height - 48, 25);
        this.lista.setRenderBackground(false);
        this.lista.setRenderTopAndBottom(false);

        this.lista.addBig(interruptor("jobsmenu.ajustes.escena",
                ConfigTurno.escenaViva(), ConfigTurno::fijarEscenaViva));
        this.lista.addSmall(
                interruptor("jobsmenu.ajustes.alto",
                        ConfigTurno.altoContraste(), ConfigTurno::fijarAltoContraste),
                interruptor("jobsmenu.ajustes.grande",
                        ConfigTurno.textoGrande(), ConfigTurno::fijarTextoGrande));
        this.lista.addSmall(
                interruptor("jobsmenu.ajustes.papel",
                        ConfigTurno.papelLimpio(), ConfigTurno::fijarPapelLimpio),
                interruptor("jobsmenu.ajustes.guia",
                        ConfigTurno.guiaLectura(), ConfigTurno::fijarGuiaLectura));
        this.lista.addSmall(
                interruptor("jobsmenu.ajustes.estado",
                        ConfigTurno.mostrarEstadoInstalacion(), ConfigTurno::fijarMostrarEstadoInstalacion),
                interruptor("jobsmenu.ajustes.respiracion",
                        ConfigTurno.respiracionCamara(), ConfigTurno::fijarRespiracionCamara));
        this.lista.addBig(interruptor("jobsmenu.ajustes.bajoconsumo",
                ConfigTurno.bajoConsumo(), ConfigTurno::fijarBajoConsumo));
        this.lista.addSmall(
                interruptor("jobsmenu.ajustes.rotar",
                        ConfigTurno.rotarNivelesBruto(), ConfigTurno::fijarRotarNiveles),
                interruptor("jobsmenu.ajustes.cuenta",
                        ConfigTurno.mostrarCuentaRegresivaBruto(), ConfigTurno::fijarMostrarCuentaRegresiva));
        this.lista.addBig(selectorNivel(ConfigTurno.nivelFijo()));
        this.lista.addBig(deslizador("jobsmenu.ajustes.estancia",
                ConfigTurno.duracionEstancia(), 15, 90,
                "jobsmenu.ajustes.segundos", ConfigTurno::fijarDuracionEstancia));
        this.lista.addSmall(
                interruptor("jobsmenu.ajustes.rotacioncalma",
                        ConfigTurno.rotacionCalma(), ConfigTurno::fijarRotacionCalma),
                interruptor("jobsmenu.ajustes.avisos",
                        ConfigTurno.avisosRotativosBruto(), ConfigTurno::fijarAvisosRotativos));
        this.lista.addBig(deslizador("jobsmenu.ajustes.duracion",
                ConfigTurno.duracionAvisos(), 4, 15,
                "jobsmenu.ajustes.segundos", ConfigTurno::fijarDuracionAvisos));
        this.lista.addSmall(
                interruptor("jobsmenu.ajustes.fecha",
                        ConfigTurno.mostrarFechaBruto(), ConfigTurno::fijarMostrarFecha),
                interruptor("jobsmenu.ajustes.interfaz",
                        ConfigTurno.interfazMinima(), ConfigTurno::fijarInterfazMinima));

        this.lista.addBig(deslizador("jobsmenu.ajustes.volaviso",
                ConfigTurno.volumenAvisoPorcentaje(), ConfigTurno::fijarVolumenAviso));
        this.lista.addBig(deslizador("jobsmenu.ajustes.volmusica",
                ConfigTurno.volumenMusicaPorcentaje(), ConfigTurno::fijarVolumenMusica));
        this.lista.addBig(deslizador("jobsmenu.ajustes.volambiente",
                ConfigTurno.volumenAmbientePorcentaje(), ConfigTurno::fijarVolumenAmbiente));
        this.lista.addSmall(
                interruptor("jobsmenu.ajustes.musica",
                        ConfigTurno.musicaMenu(), ConfigTurno::fijarMusicaMenu),
                interruptor("jobsmenu.ajustes.ambiente",
                        ConfigTurno.sonidoAmbiente(), ConfigTurno::fijarSonidoAmbiente));
        this.lista.addSmall(
                interruptor("jobsmenu.ajustes.botones",
                        ConfigTurno.sonidoBotones(), ConfigTurno::fijarSonidoBotones),
                interruptor("jobsmenu.ajustes.credito",
                        ConfigTurno.creditoMusica(), ConfigTurno::fijarCreditoMusica));

        this.lista.addBig(interruptor("jobsmenu.ajustes.perfil",
                ConfigTurno.perfilAccesible(), ConfigTurno::fijarPerfilAccesible));
        this.lista.addSmall(
                interruptor("jobsmenu.ajustes.movimiento",
                        ConfigTurno.movimientoReducido(), ConfigTurno::fijarMovimientoReducido),
                interruptor("jobsmenu.ajustes.destellos",
                        ConfigTurno.destellosReducidos(), ConfigTurno::fijarDestellosReducidos));
        this.lista.addSmall(
                interruptor("jobsmenu.ajustes.presencia",
                        ConfigTurno.presenciaFondo(), ConfigTurno::fijarPresenciaFondo),
                interruptor("jobsmenu.ajustes.eventos",
                        ConfigTurno.eventosAmbientales(), ConfigTurno::fijarEventosAmbientales));
        this.lista.addSmall(
                interruptor("jobsmenu.ajustes.suspension",
                        ConfigTurno.suspensionRara(), ConfigTurno::fijarSuspensionRara),
                interruptor("jobsmenu.ajustes.menu",
                        ConfigTurno.menuPropio(), ConfigTurno::fijarMenuPropio));
        this.lista.addBig(interruptor("jobsmenu.ajustes.pausa",
                ConfigTurno.pausaPropia(), ConfigTurno::fijarPausaPropia));

        this.addWidget(this.lista);
        this.addRenderableWidget(new BotonExpediente(
                this.width / 2 - 80, this.height - 30, 160, 20,
                Component.translatable("jobsmenu.interfaz.volver"),
                BotonExpediente.Tipo.PRINCIPAL,
                () -> this.minecraft.setScreen(this.lastScreen)));
    }

    @Override
    public void renderBackground(GuiGraphics g) {
        ChromeExpediente.fondo(g, this.width, this.height);
        ChromeExpediente.panel(g, 8, 6, this.width - 16, this.height - 12);
    }

    @Override
    public void render(GuiGraphics grafico, int ratonX, int ratonY, float parcial) {
        this.basicListRender(grafico, this.lista, ratonX, ratonY, parcial);
        ChromeExpediente.marcoSubpantalla(grafico, this.font, this.width, this.height,
                8, 6, this.width - 16, this.height - 12,
                Component.translatable("jobsmenu.interfaz.aviso.subtitulo"), "JOBS-013");
    }

    @Override
    public void removed() {
        ConfigTurno.guardarPendiente();
        super.removed();
    }
}

package com.santipdr.jobsmenu.client.screen;

import com.santipdr.jobsmenu.client.scene.EscenaNivel;
import com.santipdr.jobsmenu.client.ui.Paleta;
import com.santipdr.jobsmenu.config.ConfigTurno;

import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

/**
 * Los ajustes del aviso, dentro del mismo menu de opciones del juego.
 *
 * POR QUE ASI Y NO UNA HOJA APARTE
 *
 * La version anterior tenia una pantalla de ajustes propia, con la piel del
 * aviso. Quedaba linda, pero eran DOS menus de configuracion: el del mod por un
 * lado y el del juego -imagen, sonido, controles, idioma- por otro. Dos sitios
 * donde buscar un ajuste es uno de mas.
 *
 * Esta pantalla usa las MISMAS piezas que las opciones de vanilla: la lista con
 * barra de desplazamiento (OptionsList), los interruptores y los deslizadores
 * (OptionInstance), el boton Listo al pie. Se llega desde un boton que el mod
 * agrega a la pantalla de opciones del juego (ver AjustesAviso), asi que es una
 * subpantalla mas de las opciones, hermana de "Musica y sonidos" o "Controles".
 * Un solo menu de ajustes, con una seccion mas.
 *
 * Cada control lee y escribe la config del mod, que se guarda sola. Los dos
 * volumenes son deslizadores 0-100 con su numero al lado, igual que los de
 * sonido del juego.
 */
public class PantallaAjustesAviso extends OptionsSubScreen {

    private OptionsList lista;

    public PantallaAjustesAviso(Screen anterior, Options opciones) {
        super(anterior, opciones, Component.translatable("jobsmenu.ajustes.titulo"));
    }

    /** Un interruptor si/no, en el formato de los de vanilla. */
    private static OptionInstance<Boolean> interruptor(String clave, boolean valor,
                                                       java.util.function.Consumer<Boolean> fijar) {
        return OptionInstance.createBoolean(clave,
                OptionInstance.cachedConstantTooltip(Component.translatable(clave + ".detalle")),
                valor, fijar::accept);
    }

    /** Un deslizador de 0 a 100 con su valor al lado, como los de volumen. */
    private static OptionInstance<Integer> deslizador(String clave, int valor,
                                                      java.util.function.IntConsumer fijar) {
        return new OptionInstance<>(clave,
                OptionInstance.cachedConstantTooltip(Component.translatable(clave + ".detalle")),
                (caption, v) -> Component.translatable("jobsmenu.ajustes.porciento", caption, v),
                new OptionInstance.IntRange(0, 100),
                valor,
                (v) -> fijar.accept(v));
    }

    /** Selector entero de nivel; el knob y el valor comparten exactamente el rango 0-9. */
    private static OptionInstance<Integer> selectorNivel(int valor) {
        String clave = "jobsmenu.ajustes.nivelfijo";
        return new OptionInstance<>(clave,
                OptionInstance.cachedConstantTooltip(Component.translatable(clave + ".detalle")),
                (caption, v) -> Component.translatable("jobsmenu.ajustes.nivelvalor", caption, v),
                new OptionInstance.IntRange(0, 9),
                Math.max(0, Math.min(9, valor)),
                ConfigTurno::fijarNivelFijo);
    }

    @Override
    protected void init() {
        this.lista = new OptionsList(this.minecraft, this.width, this.height, 32, this.height - 32, 25);

        // Imagen del recinto.
        this.lista.addBig(interruptor("jobsmenu.ajustes.escena",
                ConfigTurno.escenaViva(), ConfigTurno::fijarEscenaViva));
        this.lista.addSmall(
                interruptor("jobsmenu.ajustes.rotar",
                        ConfigTurno.rotarNivelesBruto(), ConfigTurno::fijarRotarNiveles),
                interruptor("jobsmenu.ajustes.cuenta",
                        ConfigTurno.mostrarCuentaRegresivaBruto(), ConfigTurno::fijarMostrarCuentaRegresiva));
        this.lista.addBig(selectorNivel(ConfigTurno.nivelFijo()));
        this.lista.addSmall(
                interruptor("jobsmenu.ajustes.avisos",
                        ConfigTurno.avisosRotativosBruto(), ConfigTurno::fijarAvisosRotativos),
                interruptor("jobsmenu.ajustes.interfaz",
                        ConfigTurno.interfazMinima(), ConfigTurno::fijarInterfazMinima));

        // Sonido del recinto y del menu.
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

        // Accesibilidad y comportamiento.
        this.lista.addSmall(
                interruptor("jobsmenu.ajustes.movimiento",
                        ConfigTurno.movimientoReducido(), ConfigTurno::fijarMovimientoReducido),
                interruptor("jobsmenu.ajustes.destellos",
                        ConfigTurno.destellosReducidos(), ConfigTurno::fijarDestellosReducidos));
        this.lista.addSmall(
                interruptor("jobsmenu.ajustes.menu",
                        ConfigTurno.menuPropio(), ConfigTurno::fijarMenuPropio),
                interruptor("jobsmenu.ajustes.pausa",
                        ConfigTurno.pausaPropia(), ConfigTurno::fijarPausaPropia));

        this.addWidget(this.lista);

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (b) -> {
            this.minecraft.setScreen(this.lastScreen);
        }).bounds(this.width / 2 - 100, this.height - 27, 200, 20).build());
    }

    @Override
    public void render(GuiGraphics grafico, int ratonX, int ratonY, float parcial) {
        // basicListRender es el render estandar de las subpantallas de opciones:
        // fondo, la lista con su barra, el titulo centrado arriba y los widgets.
        this.basicListRender(grafico, this.lista, ratonX, ratonY, parcial);
    }

    @Override
    public void renderBackground(GuiGraphics grafico) {
        if (this.minecraft != null && this.minecraft.level == null) {
            EscenaNivel.dibujar(grafico, this.width, this.height);
            grafico.fill(0, 0, this.width, this.height, Paleta.conAlfa(Paleta.VANO, .48F));
        } else {
            super.renderBackground(grafico);
        }
    }
}

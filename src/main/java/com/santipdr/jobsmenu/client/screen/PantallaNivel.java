package com.santipdr.jobsmenu.client.screen;

import com.santipdr.jobsmenu.JobsMenu;
import com.santipdr.jobsmenu.client.scene.EscenaNivel;
import com.santipdr.jobsmenu.client.scene.Nivel;
import com.santipdr.jobsmenu.client.scene.RotacionNiveles;
import com.santipdr.jobsmenu.client.sound.SonidosNivel;
import com.santipdr.jobsmenu.client.sound.ZumbidoNivel;
import com.santipdr.jobsmenu.client.ui.Paleta;
import com.santipdr.jobsmenu.client.ui.RelojAparicion;
import com.santipdr.jobsmenu.client.ui.RenglonTablon;
import com.santipdr.jobsmenu.config.ConfigTurno;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;

/**
 * Pantalla de titulo: el aviso pegado a la pared del nivel.
 *
 * Es una hoja fotocopiada, con la tarifa de salida, la lista de turnos
 * disponibles y la hora de la proxima ronda. Alguien la pego hace mucho.
 */
public class PantallaNivel extends Screen {

    /** Cantidad de avisos disponibles en los archivos de idioma. */
    private static final int AVISOS = 8;

    /** Cada cuantos milisegundos rota el aviso del pie. */
    private static final long ROTACION_AVISO_MS = 7_000L;

    private static final int ANCHO_HOJA = 214;
    private static final int ALTO_RENGLON = 20;
    private static final int SEPARACION = 3;

    /** Cuanto tarda el rotulo del nivel nuevo en terminar de aparecer. */
    private static final long ENTRADA_ROTULO_MS = 900L;

    private int hojaX;
    private int hojaY;
    private int hojaAlto;

    /** Ultimo nivel visto, para saber cuando cambio sin llevar temporizadores. */
    private int nivelVisto;

    /** Si ya sono el apagon de la transicion en curso. */
    private boolean apagonSonado;

    /** Momento en que se instalo el nivel actual, para la entrada del rotulo. */
    private long desdeCambio;

    public PantallaNivel() {
        super(Component.translatable("jobsmenu.pantalla.nivel"));
        this.nivelVisto = RotacionNiveles.indiceActual();
        this.apagonSonado = false;
        this.desdeCambio = System.currentTimeMillis();
    }

    @Override
    protected void init() {
        arrancarAmbiente();

        this.hojaX = Math.max(14, (int) (this.width * 0.07F));
        this.hojaY = Math.max(16, (int) (this.height * 0.13F));
        this.hojaAlto = Math.min(this.height - this.hojaY - 16, 208);

        int x = this.hojaX + 12;
        int y = this.hojaY + 88;
        int ancho = ANCHO_HOJA - 24;

        agregar(x, y, ancho, "01", "jobsmenu.tablon.turno", this::abrirTurno);
        agregar(x, y + (ALTO_RENGLON + SEPARACION), ancho, "02", "jobsmenu.tablon.cuadrilla", this::abrirCuadrilla);
        agregar(x, y + 2 * (ALTO_RENGLON + SEPARACION), ancho, "03", "jobsmenu.tablon.condiciones", this::abrirCondiciones);
        agregar(x, y + 3 * (ALTO_RENGLON + SEPARACION), ancho, "04", "jobsmenu.tablon.renunciar", this::renunciar);
    }

    private void agregar(int x, int y, int ancho, String orden, String clave, Runnable accion) {
        this.addRenderableWidget(new RenglonTablon(
                x, y, ancho, ALTO_RENGLON, orden, Component.translatable(clave), accion));
    }

    /** El zumbido del pasillo, si no hay uno sonando ya. */
    private void arrancarAmbiente() {
        if (!ConfigTurno.sonidoAmbiente()) {
            return;
        }
        Minecraft.getInstance().getSoundManager().play(new ZumbidoNivel());
    }

    /** Interruptor de pared: se usa al saltar a cualquier pantalla de Minecraft. */
    private void sonarPesado() {
        if (!ConfigTurno.sonidoBotones()) {
            return;
        }
        Minecraft.getInstance().getSoundManager()
                .play(SimpleSoundInstance.forUI(SonidosNivel.PESADO.get(), 1.0F, 0.60F));
    }

    private void abrirTurno() {
        sonarPesado();
        Minecraft cliente = Minecraft.getInstance();
        cliente.setScreen(new SelectWorldScreen(this));
    }

    private void abrirCuadrilla() {
        sonarPesado();
        Minecraft cliente = Minecraft.getInstance();
        cliente.setScreen(new JoinMultiplayerScreen(this));
    }

    private void abrirCondiciones() {
        sonarPesado();
        Minecraft cliente = Minecraft.getInstance();
        cliente.setScreen(new OptionsScreen(this, cliente.options));
    }

    private void renunciar() {
        Minecraft.getInstance().stop();
    }

    @Override
    public void render(GuiGraphics grafico, int ratonX, int ratonY, float parcial) {
        seguirTransicion();
        this.renderBackground(grafico);

        if (!ConfigTurno.interfazMinima()) {
            hoja(grafico);
        }

        cabecera(grafico);
        super.render(grafico, ratonX, ratonY, parcial);

        if (ConfigTurno.mostrarCuentaRegresiva()) {
            ronda(grafico);
        }
        if (ConfigTurno.avisosRotativos()) {
            aviso(grafico);
        }
        if (!ConfigTurno.interfazMinima()) {
            rotuloNivel(grafico);
            sello(grafico);
        }
    }

    /**
     * Acompana el cambio de nivel con sonido.
     *
     * Nadie anuncia el cambio: se apaga la luz, se oye el tubo rendirse y,
     * cuando vuelve, el pasillo ya es otro. Los dos golpes se disparan una
     * sola vez por transicion.
     */
    private void seguirTransicion() {
        boolean cambiando = RotacionNiveles.enTransicion();

        if (cambiando && !this.apagonSonado) {
            this.apagonSonado = true;
            sonarNivel(SonidosNivel.APAGON.get(), 0.55F);
        }
        if (!cambiando) {
            this.apagonSonado = false;
        }

        int ahora = RotacionNiveles.indiceActual();
        if (ahora != this.nivelVisto) {
            this.nivelVisto = ahora;
            this.desdeCambio = System.currentTimeMillis();
            sonarNivel(SonidosNivel.ENCENDIDO.get(), 0.50F);
        }
    }

    private void sonarNivel(SoundEvent evento, float volumen) {
        if (!ConfigTurno.sonidoAmbiente()) {
            return;
        }
        Minecraft.getInstance().getSoundManager()
                .play(SimpleSoundInstance.forUI(evento, 1.0F, volumen));
    }

    /**
     * El nombre del nivel donde esta parado el jugador, abajo a la izquierda.
     * Aparece con la luz nueva y se queda: es un cartel de pared, no un aviso.
     */
    private void rotuloNivel(GuiGraphics grafico) {
        Nivel nivel = RotacionNiveles.actual();

        float entrada = (System.currentTimeMillis() - this.desdeCambio) / (float) ENTRADA_ROTULO_MS;
        entrada = Math.max(0.0F, Math.min(1.0F, entrada));
        float alfa = entrada * RotacionNiveles.luzDisponible();
        if (alfa <= 0.02F) {
            return;
        }

        Component nombre = Component.translatable("jobsmenu." + nivel.clave + ".nombre");
        Component nota = Component.translatable("jobsmenu." + nivel.clave + ".nota");

        int x = 12;
        int y = this.height - 30;

        grafico.drawString(this.font, nombre, x, y,
                Paleta.conAlfa(Paleta.PAPEL, 0.85F * alfa), false);
        grafico.drawString(this.font, nota, x, y + 11,
                Paleta.conAlfa(Paleta.PAPEL, 0.45F * alfa), false);
    }

    @Override
    public void renderBackground(GuiGraphics grafico) {
        EscenaNivel.dibujar(grafico, this.width, this.height);
    }

    /** La hoja fotocopiada pegada a la pared, con su sombra y su cinta. */
    private void hoja(GuiGraphics grafico) {
        int x0 = this.hojaX;
        int y0 = this.hojaY;
        int x1 = x0 + ANCHO_HOJA;
        int y1 = y0 + this.hojaAlto;

        grafico.fill(x0 + 3, y0 + 4, x1 + 3, y1 + 4, Paleta.conAlfa(Paleta.VANO, 0.30F));
        grafico.fill(x0, y0, x1, y1, Paleta.conAlfa(Paleta.PAPEL, 0.94F));
        grafico.fill(x0, y0, x1, y0 + 1, Paleta.conAlfa(Paleta.MOHO, 0.35F));
        grafico.fill(x0, y1 - 1, x1, y1, Paleta.conAlfa(Paleta.MOHO, 0.45F));
        grafico.fill(x0, y0, x0 + 1, y1, Paleta.conAlfa(Paleta.MOHO, 0.35F));
        grafico.fill(x1 - 1, y0, x1, y1, Paleta.conAlfa(Paleta.MOHO, 0.35F));

        int cinta = 22;
        int centro = (x0 + x1) / 2;
        grafico.fill(centro - cinta, y0 - 4, centro + cinta, y0 + 4, Paleta.conAlfa(Paleta.PAPEL, 0.45F));
    }

    /** Titulo del aviso, nivel actual y tarifa de salida. */
    private void cabecera(GuiGraphics grafico) {
        int x = this.hojaX + 12;
        int y = this.hojaY + 12;

        grafico.pose().pushPose();
        grafico.pose().translate(x, y, 0.0D);
        grafico.pose().scale(2.0F, 2.0F, 1.0F);
        grafico.drawString(this.font, Component.translatable("jobsmenu.titulo"), 0, 0, Paleta.TINTA, false);
        grafico.pose().popPose();

        grafico.drawString(this.font, Component.translatable("jobsmenu.subtitulo"), x, y + 20,
                Paleta.TINTA_TENUE, false);

        grafico.fill(x, y + 33, x + ANCHO_HOJA - 24, y + 34, Paleta.conAlfa(Paleta.TINTA_TENUE, 0.45F));

        grafico.drawString(this.font, Component.translatable("jobsmenu.nivel.actual"), x, y + 42,
                Paleta.TINTA_TENUE, false);
        grafico.drawString(this.font, Component.translatable("jobsmenu.nivel.tarifa"), x, y + 54,
                Paleta.TINTA, false);
    }

    /** Cuanto falta para la proxima ronda de los Executores. */
    private void ronda(GuiGraphics grafico) {
        boolean destellosReducidos = ConfigTurno.destellosReducidos() || !ConfigTurno.escenaViva();

        Component rotulo;
        if (RelojAparicion.enRonda()) {
            rotulo = Component.translatable("jobsmenu.reloj.encurso");
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
        int x0 = this.width - margen - Math.max(anchoRotulo, anchoTiempo);

        grafico.fill(x0 - 8, margen - 6, this.width - margen + 6, margen + 26,
                Paleta.conAlfa(Paleta.VANO, 0.45F));

        grafico.drawString(this.font, rotulo, this.width - margen - anchoRotulo, margen,
                Paleta.conAlfa(Paleta.PAPEL, 0.80F), false);
        grafico.drawString(this.font, tiempo, this.width - margen - anchoTiempo, margen + 13, color, false);
    }

    /** La linea que rota al pie de la hoja. */
    private void aviso(GuiGraphics grafico) {
        int indice = (int) (Math.floorDiv(System.currentTimeMillis(), ROTACION_AVISO_MS) % AVISOS);
        Component texto = Component.translatable("jobsmenu.aviso." + indice);

        int x = this.hojaX + 12;
        int y = this.hojaY + this.hojaAlto - 26;
        int ancho = ANCHO_HOJA - 24;

        for (net.minecraft.util.FormattedCharSequence linea : this.font.split(texto, ancho)) {
            grafico.drawString(this.font, linea, x, y, Paleta.TINTA_TENUE, false);
            y += 10;
        }
    }

    private void sello(GuiGraphics grafico) {
        String texto = "jobsmenu " + JobsMenu.VERSION;
        int ancho = this.font.width(texto);
        grafico.drawString(this.font, texto, this.width - 12 - ancho, this.height - 14,
                Paleta.conAlfa(Paleta.PAPEL, 0.45F), false);
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

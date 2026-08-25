package com.santipdr.jobsmenu.client.screen;

import com.santipdr.jobsmenu.JobsMenu;
import com.santipdr.jobsmenu.client.scene.EscenaNivel;
import com.santipdr.jobsmenu.client.scene.Nivel;
import com.santipdr.jobsmenu.client.scene.RotacionNiveles;
import com.santipdr.jobsmenu.client.sound.GestorAmbiente;
import com.santipdr.jobsmenu.client.sound.GestorMusica;
import com.santipdr.jobsmenu.client.ui.NotaAviso;
import com.santipdr.jobsmenu.client.ui.Paleta;
import com.santipdr.jobsmenu.client.ui.RelojAparicion;
import com.santipdr.jobsmenu.client.ui.RenglonTablon;
import com.santipdr.jobsmenu.config.ConfigTurno;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.gui.ModListScreen;

/**
 * Pantalla de titulo: el aviso pegado a la pared del nivel.
 *
 * Es una hoja fotocopiada, con la tarifa de salida, la lista de turnos
 * disponibles y la hora de la proxima ronda. Alguien la pego hace mucho.
 *
 * COMO ESTA ORDENADA LA LISTA
 *
 * Los renglones no siguen el orden del menu vanilla sino el de un tablon de
 * verdad, que es el de la frecuencia con que la gente los usa:
 *
 *   01  Unirse a una cuadrilla   - a lo que se entra todos los dias
 *   02  Registro de intervenciones - la lista de mods
 *   03  Condiciones de estancia  - opciones, se tocan una vez y no se vuelve
 *   --  (hueco)
 *   04  Renunciar al nivel       - lo unico irreversible, apartado del resto
 *
 * La partida de un jugador no figura: se abre con Control + S, sin pasar por
 * la lista. Es la salida de servicio, y las salidas de servicio no se anuncian
 * en el tablon; se aclara al pie, en letra chica, para quien la necesite.
 *
 * El hueco antes de renunciar no es decorativo: separar lo destructivo del
 * resto es lo que evita que alguien lo pulse por inercia bajando la lista.
 */
public class PantallaNivel extends Screen {

    private static final int ANCHO_HOJA = 214;
    private static final int ALTO_RENGLON = 20;
    private static final int SEPARACION = 3;

    /** Hueco extra que aisla el ultimo renglon del bloque de arriba. */
    private static final int HUECO_APARTE = 10;

    /** Cuanto tarda el rotulo del nivel nuevo en terminar de aparecer. */
    private static final long ENTRADA_ROTULO_MS = 900L;

    private int hojaX;
    private int hojaY;
    private int hojaAlto;

    /** Ultimo nivel visto, para saber cuando cambio sin llevar temporizadores. */
    private int nivelVisto;

    /** Momento en que se instalo el nivel actual, para la entrada del rotulo. */
    private long desdeCambio;

    public PantallaNivel() {
        super(Component.translatable("jobsmenu.pantalla.nivel"));
        this.nivelVisto = RotacionNiveles.indiceActual();
        this.desdeCambio = System.currentTimeMillis();
    }

    @Override
    protected void init() {
        // Los dos gestores son idempotentes: init() se vuelve a llamar cada vez
        // que cambia el tamano de la ventana, y ninguno de los dos apila una
        // segunda copia de lo que ya esta sonando.
        GestorAmbiente.abrir();
        GestorMusica.asegurar();

        this.hojaX = Math.max(14, (int) (this.width * 0.07F));
        this.hojaY = Math.max(16, (int) (this.height * 0.13F));
        this.hojaAlto = Math.min(this.height - this.hojaY - 16, 216);

        int x = this.hojaX + 12;
        int y = this.hojaY + 88;
        int ancho = ANCHO_HOJA - 24;
        int salto = ALTO_RENGLON + SEPARACION;

        agregar(x, y, ancho, "01", "jobsmenu.tablon.cuadrilla", this::abrirCuadrilla, false);
        agregar(x, y + salto, ancho, "02", "jobsmenu.tablon.registro", this::abrirRegistro, false);
        agregar(x, y + 2 * salto, ancho, "03", "jobsmenu.tablon.condiciones", this::abrirCondiciones, false);
        agregar(x, y + 3 * salto + HUECO_APARTE, ancho, "04", "jobsmenu.tablon.renunciar", this::renunciar, true);

        // El aviso del pie es un widget y no un dibujo: se puede pasar a mano y
        // entra en el recorrido del tabulador como cualquier otro renglon.
        if (ConfigTurno.avisosRotativos()) {
            this.addRenderableWidget(new NotaAviso(x, this.hojaY + this.hojaAlto - 26, ancho, 20));
        }
    }

    private void agregar(int x, int y, int ancho, String orden, String clave,
                         Runnable accion, boolean terminal) {
        this.addRenderableWidget(new RenglonTablon(
                x, y, ancho, ALTO_RENGLON, orden, Component.translatable(clave), accion, terminal));
    }

    // ----------------------------------------------------------------------
    // Acciones
    // ----------------------------------------------------------------------

    private void abrirCuadrilla() {
        Minecraft cliente = Minecraft.getInstance();
        cliente.setScreen(new JoinMultiplayerScreen(this));
    }

    /** La lista de mods de Forge, tal cual, sin envoltorio propio. */
    private void abrirRegistro() {
        Minecraft cliente = Minecraft.getInstance();
        cliente.setScreen(new ModListScreen(this));
    }

    private void abrirCondiciones() {
        Minecraft cliente = Minecraft.getInstance();
        cliente.setScreen(new OptionsScreen(this, cliente.options));
    }

    private void renunciar() {
        GestorAmbiente.cerrar();
        GestorMusica.soltar();
        Minecraft.getInstance().stop();
    }

    @Override
    public void removed() {
        // Al irse a otra pantalla el ambiente se suelta, pero la musica no: si
        // el jugador va a las opciones y vuelve, el tema tiene que seguir donde
        // estaba y no arrancar de cero.
        GestorAmbiente.cerrar();
        super.removed();
    }

    // ----------------------------------------------------------------------
    // Dibujo
    // ----------------------------------------------------------------------

    @Override
    public void render(GuiGraphics grafico, int ratonX, int ratonY, float parcial) {
        GestorAmbiente.atender();
        seguirNivel();

        this.renderBackground(grafico);

        if (!ConfigTurno.interfazMinima()) {
            hoja(grafico);
        }

        cabecera(grafico);
        super.render(grafico, ratonX, ratonY, parcial);

        if (ConfigTurno.mostrarCuentaRegresiva()) {
            ronda(grafico);
        }
        if (!ConfigTurno.interfazMinima()) {
            atajo(grafico);
            rotuloNivel(grafico);
            sello(grafico);
        }
    }

    /**
     * Cuanta tinta se lee ahora mismo, de 0.10 a 1.0.
     *
     * Todo lo impreso pasa por aca. Cuando el pasillo se queda sin luz, la hoja
     * no puede seguir legible: el papel no emite. Se deja un diez por ciento
     * para que la composicion no desaparezca del todo y el ojo sepa que la hoja
     * sigue ahi, en la penumbra.
     */
    private static float tinta() {
        return 0.10F + 0.90F * RotacionNiveles.luzDisponible();
    }

    /**
     * Se entera de que el pasillo cambio de nivel.
     *
     * Solo lleva la cuenta para la entrada del rotulo. Los sonidos de la
     * transicion los dispara el gestor de ambiente, que es quien conoce los
     * tiempos exactos; la pantalla no tiene por que saber de eso.
     */
    private void seguirNivel() {
        int ahora = RotacionNiveles.indiceActual();
        if (ahora != this.nivelVisto) {
            this.nivelVisto = ahora;
            this.desdeCambio = System.currentTimeMillis();
        }
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

        // El papel se oscurece con el pasillo. No es tinta: es el blanco de la
        // hoja, que sin fluorescente encima deja de ser blanco.
        float luz = RotacionNiveles.luzDisponible();
        int papel = Paleta.iluminar(Paleta.PAPEL, 0.22F + 0.78F * luz);

        grafico.fill(x0 + 3, y0 + 4, x1 + 3, y1 + 4, Paleta.conAlfa(Paleta.VANO, 0.30F));
        grafico.fill(x0, y0, x1, y1, Paleta.conAlfa(papel, 0.94F));
        grafico.fill(x0, y0, x1, y0 + 1, Paleta.conAlfa(Paleta.MOHO, 0.35F));
        grafico.fill(x0, y1 - 1, x1, y1, Paleta.conAlfa(Paleta.MOHO, 0.45F));
        grafico.fill(x0, y0, x0 + 1, y1, Paleta.conAlfa(Paleta.MOHO, 0.35F));
        grafico.fill(x1 - 1, y0, x1, y1, Paleta.conAlfa(Paleta.MOHO, 0.35F));

        int cinta = 22;
        int centro = (x0 + x1) / 2;
        grafico.fill(centro - cinta, y0 - 4, centro + cinta, y0 + 4, Paleta.conAlfa(papel, 0.45F));
    }

    /** Titulo del aviso, nivel actual y tarifa de salida. */
    private void cabecera(GuiGraphics grafico) {
        int x = this.hojaX + 12;
        int y = this.hojaY + 12;
        float tinta = tinta();

        grafico.pose().pushPose();
        grafico.pose().translate(x, y, 0.0D);
        grafico.pose().scale(2.0F, 2.0F, 1.0F);
        grafico.drawString(this.font, Component.translatable("jobsmenu.titulo"), 0, 0,
                Paleta.conAlfa(Paleta.TINTA, tinta), false);
        grafico.pose().popPose();

        grafico.drawString(this.font, Component.translatable("jobsmenu.subtitulo"), x, y + 20,
                Paleta.conAlfa(Paleta.TINTA_TENUE, tinta), false);

        grafico.fill(x, y + 33, x + ANCHO_HOJA - 24, y + 34,
                Paleta.conAlfa(Paleta.TINTA_TENUE, 0.45F * tinta));

        grafico.drawString(this.font, Component.translatable("jobsmenu.nivel.actual"), x, y + 42,
                Paleta.conAlfa(Paleta.TINTA_TENUE, tinta), false);
        grafico.drawString(this.font, Component.translatable("jobsmenu.nivel.tarifa"), x, y + 54,
                Paleta.conAlfa(Paleta.TINTA, tinta), false);
    }

    /**
     * La nota al pie que explica la salida de servicio.
     *
     * Va en la hoja, con la tipografia de la letra chica, y no como un aviso
     * flotante: forma parte del documento, no de la interfaz. Un atajo que no
     * esta escrito en ningun lado no existe para nadie.
     */
    private void atajo(GuiGraphics grafico) {
        int x = this.hojaX + 12;
        int y = this.hojaY + this.hojaAlto - 40;
        grafico.drawString(this.font, Component.translatable("jobsmenu.tablon.atajo"), x, y,
                Paleta.conAlfa(Paleta.TINTA_TENUE, 0.70F * tinta()), false);
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

    private void sello(GuiGraphics grafico) {
        String texto = "jobsmenu " + JobsMenu.VERSION;
        int ancho = this.font.width(texto);
        grafico.drawString(this.font, texto, this.width - 12 - ancho, this.height - 14,
                Paleta.conAlfa(Paleta.PAPEL, 0.45F), false);
    }

    // ----------------------------------------------------------------------
    // Teclado
    // ----------------------------------------------------------------------

    /**
     * Navegacion por teclado.
     *
     * No hace falta interceptar nada: Minecraft ya mueve el foco con Tab y las
     * flechas, y RenglonTablon emite su gesto al recibir el foco igual que al
     * recibir el cursor, porque mira isHoveredOrFocused y no solo el raton. El
     * teclado suena, entonces, exactamente igual que el raton.
     *
     * Escape esta anulado en shouldCloseOnEsc: de la pantalla de titulo no se
     * sale con Escape, no hay adonde ir.
     */
    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}

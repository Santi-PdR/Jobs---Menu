package com.santipdr.jobsmenu.client.screen;

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
import net.minecraft.util.FormattedCharSequence;
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

    /** Cuantas notas rotativas tiene cada nivel (jobsmenu.<clave>.nota0..N-1). */
    private static final int NOTAS_POR_NIVEL = 3;
    private static final int ALTO_RENGLON = 20;
    private static final int SEPARACION = 3;

    /** Hueco extra que aisla el ultimo renglon del bloque de arriba. */
    private static final int HUECO_APARTE = 10;

    // ---- Metrica de la hoja ----------------------------------------------
    // Todas las distancias verticales salen de aca. Estan agrupadas porque la
    // proporcion entre ellas es lo que hace que la hoja se lea como un
    // documento y no como una lista de botones: el aire entre bloques distintos
    // tiene que ser mayor que el aire dentro de un bloque, siempre.

    /** Margen de papel entre el borde de la hoja y lo impreso. */
    private static final int MARGEN_HOJA = 12;

    /** Alto de una linea de texto normal. */
    private static final int ALTO_LINEA = 11;

    /** Alto del titulo, que va al doble de escala. */
    private static final int ALTO_TITULO = 18;

    /** Aire entre el titulo y el subtitulo: pertenecen al mismo bloque. */
    private static final int AIRE_TITULO = 4;

    /** Aire a cada lado de la regla horizontal. */
    private static final int AIRE_REGLA = 7;

    /** Aire entre la cabecera y el primer renglon: cambia de bloque. */
    private static final int AIRE_CABECERA = 14;

    /** Aire entre el ultimo renglon y la nota rotativa del pie. */
    private static final int AIRE_PIE = 16;

    /** Margen minimo entre la hoja y el borde de la pantalla. */
    private static final int MARGEN_PANTALLA = 12;

    /** Cuanto tarda el rotulo del nivel nuevo en terminar de aparecer. */
    private static final long ENTRADA_ROTULO_MS = 900L;

    private int hojaX;
    private int hojaY;
    private int hojaAlto;

    /** Alto medido de la cabecera con el idioma actual. */
    private int altoCabecera;

    /** Alto reservado para la nota rotativa, o 0 si esta desactivada. */
    private int altoAviso;

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

        // La hoja se mide de arriba hacia abajo, sumando lo que ocupa cada
        // bloque, y solo despues se decide donde empieza. Antes se hacia al
        // reves -alto fijo de 216 px y el pie colgado del borde de abajo-, y
        // por eso la nota al pie caia encima del ultimo renglon en TODAS las
        // resoluciones: dos anclajes independientes dentro de la misma caja
        // siempre terminan chocando. Aca solo hay un anclaje, el de arriba.
        this.altoCabecera = medirCabecera();

        int salto = ALTO_RENGLON + SEPARACION;
        int altoLista = 3 * salto + HUECO_APARTE + ALTO_RENGLON;

        this.altoAviso = ConfigTurno.avisosRotativos() ? medirAviso() : 0;

        int altoPie = MARGEN_HOJA
                + this.altoCabecera
                + AIRE_CABECERA
                + altoLista
                + (this.altoAviso > 0 ? AIRE_PIE + this.altoAviso : 0)
                + MARGEN_HOJA;

        this.hojaAlto = altoPie;

        // Si la ventana es tan baja que la hoja no entra, se sube el margen
        // superior en vez de recortar la hoja: es preferible que asome por
        // arriba a que el contenido se pise. Con GUI scale 4 en una ventana
        // chica esto pasa de verdad.
        int disponible = this.height - 2 * MARGEN_PANTALLA;
        if (this.hojaAlto > disponible) {
            this.hojaY = MARGEN_PANTALLA;
        } else {
            this.hojaY = Math.max(MARGEN_PANTALLA,
                    Math.min((int) (this.height * 0.13F), this.height - MARGEN_PANTALLA - this.hojaAlto));
        }

        int x = this.hojaX + MARGEN_HOJA;
        int ancho = ANCHO_HOJA - 2 * MARGEN_HOJA;
        int y = this.hojaY + MARGEN_HOJA + this.altoCabecera + AIRE_CABECERA;

        // ORDEN DE LOS RENGLONES
        //
        // No es el orden en que se fueron escribiendo: es por cuantas veces se
        // usa cada uno. A este menu se entra a jugar, asi que unirse a una
        // cuadrilla va primero y solo. Las condiciones -las opciones del
        // juego- se tocan seguido, sobre todo el volumen, asi que van segundas.
        // El registro de intervenciones -la lista de mods- se abre una vez cada
        // tantas sesiones: va tercero, no segundo como estaba.
        //
        // Renunciar queda separado por HUECO_APARTE y marcado como terminal. Es
        // la unica accion de la hoja que no se puede deshacer, y en un tablon
        // de verdad tampoco estaria pegada al resto.
        agregar(x, y, ancho, "01", "jobsmenu.tablon.cuadrilla", this::abrirCuadrilla, false);
        agregar(x, y + salto, ancho, "02", "jobsmenu.tablon.condiciones", this::abrirCondiciones, false);
        agregar(x, y + 2 * salto, ancho, "03", "jobsmenu.tablon.registro", this::abrirRegistro, false);
        agregar(x, y + 3 * salto + HUECO_APARTE, ancho, "04", "jobsmenu.tablon.renunciar",
                this::renunciar, true);

        // El aviso del pie es un widget y no un dibujo: se puede pasar a mano y
        // entra en el recorrido del tabulador como cualquier otro renglon. Va
        // directo debajo de la lista: el atajo de servicio ya no ocupa el pie.
        if (this.altoAviso > 0) {
            this.addRenderableWidget(new NotaAviso(
                    x, y + altoLista + AIRE_PIE, ancho, this.altoAviso));
        }
    }

    /**
     * Cuanto alto necesita la cabecera con el idioma que este puesto.
     *
     * Se mide, no se supone. El subtitulo en ingles -NOTICE TO THE OCCUPANTS OF
     * THIS LEVEL- son 205 px contra los 190 que tiene la hoja de ancho, asi que
     * ocupa dos lineas y no una; la tarifa en espanol tampoco entra en una.
     * Con las alturas escritas a mano, cualquiera de las dos se comia el
     * renglon de abajo. Preguntarle al motor cuantas lineas van a salir es lo
     * unico que aguanta un idioma nuevo sin volver a tocar numeros.
     */
    private int medirCabecera() {
        int ancho = ANCHO_HOJA - 2 * MARGEN_HOJA;
        int alto = ALTO_TITULO + AIRE_TITULO;
        alto += lineas("jobsmenu.subtitulo", ancho) * ALTO_LINEA;
        alto += AIRE_REGLA + 1 + AIRE_REGLA;
        alto += lineas("jobsmenu.nivel.actual", ancho) * ALTO_LINEA;
        alto += lineas("jobsmenu.nivel.tarifa", ancho) * ALTO_LINEA;
        return alto;
    }

    /**
     * Cuanto alto reservar para la nota rotativa.
     *
     * Se toma el aviso MAS LARGO de los ocho y no el que toca ahora: si se
     * midiera el actual, la hoja cambiaria de tamano sola cada siete segundos
     * al rotar el texto, y los renglones bailarian debajo del cursor.
     */
    private int medirAviso() {
        int ancho = ANCHO_HOJA - 2 * MARGEN_HOJA;
        int maximo = 1;
        for (int i = 0; i < NotaAviso.AVISOS; i++) {
            maximo = Math.max(maximo, lineas("jobsmenu.aviso." + i, ancho));
        }
        return maximo * ALTO_LINEA + 2;
    }

    /** En cuantas lineas parte el motor esta clave con el ancho dado. */
    private int lineas(String clave, int ancho) {
        return Math.max(1, this.font.split(Component.translatable(clave), ancho).size());
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
            rotuloNivel(grafico);
        }
        credito(grafico);
    }

    /**
     * El credito de la pista, arriba a la derecha, al empezar a sonar.
     *
     * Aparece una sola vez por sesion -entra suave, se sostiene, se va- y en la
     * misma esquina que el reloj de ronda, pero debajo: si el reloj esta, el
     * credito se corre hacia abajo lo que haga falta para no pisarlo. Es un
     * gesto de pantalla de titulo de juego: quien compuso lo que estas oyendo.
     */
    private void credito(GuiGraphics grafico) {
        float alfa = GestorMusica.creditoAlfa();
        if (alfa <= 0.02F) {
            return;
        }

        Component titulo = Component.translatable("jobsmenu.credito.titulo");
        Component autor = Component.translatable("jobsmenu.credito.autor");

        int margen = 12;
        // Si el reloj de ronda ocupa su esquina, el credito arranca debajo de
        // su placa (margen-6 .. margen+26); si no, sube al tope. Asi los dos
        // pueden estar a la vez sin tocarse en ninguna resolucion.
        int y = ConfigTurno.mostrarCuentaRegresiva() ? margen + 34 : margen;

        int anchoTitulo = this.font.width(titulo);
        int anchoAutor = this.font.width(autor);
        int ancho = Math.max(anchoTitulo, anchoAutor);
        int izq = this.width - margen - ancho;

        // Una barra fina a la IZQUIERDA del bloque, como una firma al margen.
        // No hay placa oscura detras: el credito no compite con el reloj de
        // ronda, lo acompana. Va pegada al texto y crece con la envolvente.
        grafico.fill(izq - 6, y, izq - 5, y + 19,
                Paleta.conAlfa(Paleta.PAPEL, 0.45F * alfa));

        grafico.drawString(this.font, titulo, this.width - margen - anchoTitulo, y,
                Paleta.conAlfa(Paleta.PAPEL, 0.90F * alfa), false);
        grafico.drawString(this.font, autor, this.width - margen - anchoAutor, y + 10,
                Paleta.conAlfa(Paleta.PAPEL, 0.55F * alfa), false);
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

    /**
     * Titulo del aviso, nivel actual y tarifa de salida.
     *
     * Dibuja siguiendo exactamente la misma metrica que midio medirCabecera(),
     * y parte los textos largos con font.split en vez de confiar en que
     * entren. La tarifa en espanol son 217 px sobre una hoja de 190: sin
     * partir, se salia por el borde derecho del papel.
     */
    private void cabecera(GuiGraphics grafico) {
        int x = this.hojaX + MARGEN_HOJA;
        int ancho = ANCHO_HOJA - 2 * MARGEN_HOJA;
        int y = this.hojaY + MARGEN_HOJA;
        float tinta = tinta();

        grafico.pose().pushPose();
        grafico.pose().translate(x, y, 0.0D);
        grafico.pose().scale(2.0F, 2.0F, 1.0F);
        grafico.drawString(this.font, Component.translatable("jobsmenu.titulo"), 0, 0,
                Paleta.conAlfa(Paleta.TINTA, tinta), false);
        grafico.pose().popPose();

        y += ALTO_TITULO + AIRE_TITULO;
        y = parrafo(grafico, "jobsmenu.subtitulo", x, y, ancho,
                Paleta.conAlfa(Paleta.TINTA_TENUE, tinta));

        y += AIRE_REGLA;
        grafico.fill(x, y, x + ancho, y + 1,
                Paleta.conAlfa(Paleta.TINTA_TENUE, 0.45F * tinta));
        y += 1 + AIRE_REGLA;

        y = parrafo(grafico, "jobsmenu.nivel.actual", x, y, ancho,
                Paleta.conAlfa(Paleta.TINTA_TENUE, tinta));
        parrafo(grafico, "jobsmenu.nivel.tarifa", x, y, ancho,
                Paleta.conAlfa(Paleta.TINTA, tinta));
    }

    /** Dibuja un texto partido al ancho de la hoja y devuelve donde termino. */
    private int parrafo(GuiGraphics grafico, String clave, int x, int y, int ancho, int color) {
        for (FormattedCharSequence linea : this.font.split(Component.translatable(clave), ancho)) {
            grafico.drawString(this.font, linea, x, y, color, false);
            y += ALTO_LINEA;
        }
        return y;
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
        // Cada nivel tiene varias notas y muestra una distinta por estancia: la
        // que toca sale del ciclo de rotacion, asi cada vez que el fondo vuelve
        // a este nivel el cartel dice otra cosa. Da mas voz a cada recinto sin
        // agregar nada a la composicion. NOTAS_POR_NIVEL fija cuantas hay.
        int cual = (int) (Math.floorDiv(System.currentTimeMillis(), 1000L)
                / 27L % NOTAS_POR_NIVEL);
        Component nota = Component.translatable(
                "jobsmenu." + nivel.clave + ".nota" + cual);

        int x = 12;
        int y = this.height - 30;

        // El cartel de pared va abajo a la izquierda, que es justo donde cae la
        // hoja cuando la ventana es baja: con la hoja midiendo 285 px y una
        // ventana de 300, el papel llegaba hasta y=297 y el rotulo se dibujaba
        // encima. Si no hay sitio libre debajo de la hoja, el rotulo se corre a
        // la derecha del papel en vez de superponerse. Es la clase de choque
        // que solo aparece en resoluciones que casi nadie prueba, y que arruina
        // la escena para quien las usa.
        int finHoja = this.hojaY + this.hojaAlto;
        if (!ConfigTurno.interfazMinima() && y < finHoja + 4) {
            x = this.hojaX + ANCHO_HOJA + 14;
            y = Math.max(12, this.height - 30);

            // Si tampoco cabe al costado, el cartel no se dibuja. Un rotulo
            // ilegible pisado por otra cosa informa menos que ninguno.
            int anchoNecesario = Math.max(this.font.width(nombre), this.font.width(nota));
            if (x + anchoNecesario > this.width - 12) {
                return;
            }
        }

        grafico.drawString(this.font, nombre, x, y,
                Paleta.conAlfa(Paleta.PAPEL, 0.85F * alfa), false);
        grafico.drawString(this.font, nota, x, y + 11,
                Paleta.conAlfa(Paleta.PAPEL, 0.45F * alfa), false);
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

package com.santipdr.jobsmenu.config;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Configuracion de cliente. Todo interruptor de esta clase debe dejar un menu
 * usable y legible en cualquiera de sus dos posiciones.
 */
public final class ConfigTurno {

    public static final ForgeConfigSpec SPEC;
    public static final ConfigTurno INSTANCE;

    public final ForgeConfigSpec.BooleanValue menuPropio;
    public final ForgeConfigSpec.BooleanValue pausaPropia;
    public final ForgeConfigSpec.BooleanValue escenaViva;
    public final ForgeConfigSpec.BooleanValue movimientoReducido;
    public final ForgeConfigSpec.BooleanValue destellosReducidos;
    public final ForgeConfigSpec.BooleanValue altoContraste;
    public final ForgeConfigSpec.BooleanValue textoGrande;
    public final ForgeConfigSpec.BooleanValue papelLimpio;
    public final ForgeConfigSpec.BooleanValue interfazMinima;
    public final ForgeConfigSpec.BooleanValue mostrarCuentaRegresiva;
    public final ForgeConfigSpec.BooleanValue mostrarFecha;
    public final ForgeConfigSpec.BooleanValue mostrarEstadoInstalacion;
    public final ForgeConfigSpec.BooleanValue guiaLectura;
    public final ForgeConfigSpec.BooleanValue avisosRotativos;
    public final ForgeConfigSpec.IntValue duracionAvisos;
    public final ForgeConfigSpec.BooleanValue rotarNiveles;
    public final ForgeConfigSpec.BooleanValue rotacionCalma;
    public final ForgeConfigSpec.IntValue nivelFijo;
    public final ForgeConfigSpec.BooleanValue sonidoBotones;
    public final ForgeConfigSpec.BooleanValue sonidoAmbiente;
    public final ForgeConfigSpec.IntValue volumenAmbiente;
    public final ForgeConfigSpec.IntValue volumenAviso;
    public final ForgeConfigSpec.BooleanValue musicaMenu;
    public final ForgeConfigSpec.IntValue pistaMusica;
    public final ForgeConfigSpec.IntValue volumenMusica;
    public final ForgeConfigSpec.BooleanValue creditoMusica;
    public final ForgeConfigSpec.BooleanValue eventosAmbientales;
    public final ForgeConfigSpec.BooleanValue presenciaFondo;
    public final ForgeConfigSpec.BooleanValue respiracionCamara;
    public final ForgeConfigSpec.BooleanValue suspensionRara;
    public final ForgeConfigSpec.IntValue duracionEstancia;
    public final ForgeConfigSpec.BooleanValue bajoConsumo;
    public final ForgeConfigSpec.BooleanValue perfilAccesible;

    static {
        Pair<ConfigTurno, ForgeConfigSpec> par = new ForgeConfigSpec.Builder().configure(ConfigTurno::new);
        INSTANCE = par.getLeft();
        SPEC = par.getRight();
    }

    private ConfigTurno(ForgeConfigSpec.Builder builder) {
        builder.comment("Aviso a los ocupantes - preferencias de la estancia").push("nivel");

        this.menuPropio = builder
                .comment("Sustituir la pantalla de titulo por el aviso del nivel.")
                .define("menu_propio", true);

        this.pausaPropia = builder
                .comment("Sustituir la pantalla de pausa por el aviso de estancia en suspenso.")
                .define("pausa_propia", true);

        this.escenaViva = builder
                .comment("Animar el recinto del nivel. En false la composicion queda estatica.")
                .define("escena_viva", true);

        this.movimientoReducido = builder
                .comment("Congelar la animacion de la escena y ocultar lo que se ve al fondo del recinto.")
                .define("movimiento_reducido", false);

        this.destellosReducidos = builder
                .comment("Congelar el parpadeo de los fluorescentes y el pulso de alerta.")
                .define("destellos_reducidos", false);

        this.altoContraste = builder
                .comment("Aumentar el contraste de la tinta, el papel y los indicadores del aviso.")
                .define("alto_contraste", false);

        this.textoGrande = builder
                .comment("Usar una tipografia mas grande y mas aire entre los bloques de la hoja.")
                .define("texto_grande", false);

        this.papelLimpio = builder
                .comment("Quitar cinta y sombra decorativas sin ocultar el contenido del aviso.")
                .define("papel_limpio", false);

        this.interfazMinima = builder
                .comment("Dejar solo la cabecera y los renglones, sin la hoja del aviso.")
                .define("interfaz_minima", false);

        this.mostrarCuentaRegresiva = builder
                .comment("Mostrar el tiempo estimado hasta la proxima ronda.")
                .define("mostrar_cuenta_regresiva", true);

        this.mostrarFecha = builder
                .comment("Estampar la fecha del turno en la hoja del aviso.")
                .define("mostrar_fecha", true);

        this.mostrarEstadoInstalacion = builder
                .comment("Mostrar en una esquina si la instalacion esta normal, en traslado o suspendida.")
                .define("mostrar_estado_instalacion", true);

        this.guiaLectura = builder
                .comment("Mostrar una guia fina de lectura al enfocar un renglon.")
                .define("guia_lectura", true);

        this.avisosRotativos = builder
                .comment("Mostrar los avisos de la administracion al pie de la hoja.")
                .define("avisos_rotativos", true);

        this.duracionAvisos = builder
                .comment("Segundos que dura cada aviso rotativo, de 4 a 15.")
                .defineInRange("duracion_avisos", 7, 4, 15);

        this.rotarNiveles = builder
                .comment("Ir cambiando de nivel solo, con el apagon entre uno y otro.")
                .define("rotar_niveles", true);

        this.rotacionCalma = builder
                .comment("Cada nivel se queda el doble de tiempo antes del apagon.")
                .define("rotacion_calma", false);

        this.nivelFijo = builder
                .comment("Nivel a mostrar cuando la rotacion esta apagada. 0 es el papel mural.")
                .defineInRange("nivel_fijo", 0, 0, 31);

        this.sonidoBotones = builder
                .comment("Sonar la casilla al recorrer y al marcar los renglones del aviso.")
                .define("sonido_botones", true);

        this.sonidoAmbiente = builder
                .comment("Dejar sonando el ambiente del nivel: el fondo, sus ruidos y la instalacion.")
                .define("sonido_ambiente", true);

        this.volumenAmbiente = builder
                .comment("Volumen del ambiente del nivel, de 0 a 100.")
                .defineInRange("volumen_ambiente", 55, 0, 100);

        this.volumenAviso = builder
                .comment("Volumen maestro del aviso: musica, ambiente y gestos. La tecla M en el aviso alterna silencio.")
                .defineInRange("volumen_aviso", 100, 0, 100);

        this.musicaMenu = builder
                .comment("Dejar sonando el tema del menu por debajo de todo lo demas.")
                .define("musica_menu", true);

        this.pistaMusica = builder
                .comment("Pista del menu: 0 aleatoria, 1 Absurdism, 2 REQUIEM, 3 Upon the Hill V2.")
                .defineInRange("pista_musica", 0, 0, 3);

        this.volumenMusica = builder
                .comment("Volumen del tema del menu, de 0 a 100.")
                .defineInRange("volumen_musica", 70, 0, 100);

        this.creditoMusica = builder
                .comment("Mostrar el credito de la pista (titulo y autor) al empezar a sonar, arriba a la derecha.")
                .define("credito_musica", true);

        this.eventosAmbientales = builder
                .comment("Permitir destellos, humedad, polvo y siluetas ambientales del recinto.")
                .define("eventos_ambientales", true);

        this.presenciaFondo = builder
                .comment("Permitir la presencia ambigua que aparece al fondo del recinto.")
                .define("presencia_fondo", true);

        this.respiracionCamara = builder
                .comment("Mover muy lentamente el punto de fuga, independientemente de la escena viva.")
                .define("respiracion_camara", true);

        this.suspensionRara = builder
                .comment("Permitir el apagon raro y prolongado de La Suspension.")
                .define("suspension_rara", true);

        this.duracionEstancia = builder
                .comment("Segundos que permanece cada nivel antes del apagon, de 15 a 90.")
                .defineInRange("duracion_estancia", 24, 15, 90);

        this.bajoConsumo = builder
                .comment("Modo de bajo consumo: apaga polvo, grano, presencia, eventos "
                        + "visuales y respiracion de camara para equipos modestos. "
                        + "El recinto y su audio siguen intactos.")
                .define("bajo_consumo", false);

        this.perfilAccesible = builder
                .comment("Perfil accesible: combina movimiento reducido, destellos "
                        + "reducidos, alto contraste y texto grande. Tocar cualquiera "
                        + "de esas cuatro opciones a mano desactiva el perfil.")
                .define("perfil_accesible", false);

        builder.pop();
    }

    private static boolean leer(ForgeConfigSpec.BooleanValue valor, boolean porDefecto) {
        if (!SPEC.isLoaded()) {
            return porDefecto;
        }
        return valor.get();
    }

    public static boolean menuPropio() {
        return leer(INSTANCE.menuPropio, true);
    }

    public static boolean pausaPropia() {
        return leer(INSTANCE.pausaPropia, true);
    }

    public static boolean escenaViva() {
        return leer(INSTANCE.escenaViva, true);
    }

    /**
     * Movimiento reducido, destellos reducidos, alto contraste y texto grande.
     *
     * Las cuatro admiten un perfil que las enciende juntas (perfil_accesible).
     * Mientras el perfil este activo, su valor manda sobre el de la casilla
     * individual: quien enciende el perfil no tiene que marcar cuatro opciones,
     * y quien las toque a mano desactiva el perfil (ver los setters).
     */
    public static boolean movimientoReducido() {
        return perfilAccesible() || leer(INSTANCE.movimientoReducido, false);
    }

    public static boolean destellosReducidos() {
        return perfilAccesible() || leer(INSTANCE.destellosReducidos, false);
    }

    public static boolean altoContraste() {
        return perfilAccesible() || leer(INSTANCE.altoContraste, false);
    }

    public static boolean textoGrande() {
        return perfilAccesible() || leer(INSTANCE.textoGrande, false);
    }

    public static boolean papelLimpio() {
        return leer(INSTANCE.papelLimpio, false);
    }

    public static boolean interfazMinima() {
        return leer(INSTANCE.interfazMinima, false);
    }

    public static boolean mostrarCuentaRegresiva() {
        return !interfazMinima() && leer(INSTANCE.mostrarCuentaRegresiva, true);
    }

    public static boolean avisosRotativos() {
        return !interfazMinima() && leer(INSTANCE.avisosRotativos, true);
    }

    public static boolean rotarNiveles() {
        return escenaViva() && leer(INSTANCE.rotarNiveles, true);
    }

    public static int nivelFijo() {
        if (!SPEC.isLoaded()) {
            return 0;
        }
        return INSTANCE.nivelFijo.get();
    }

    public static boolean sonidoBotones() {
        return leer(INSTANCE.sonidoBotones, true);
    }

    public static boolean sonidoAmbiente() {
        return leer(INSTANCE.sonidoAmbiente, true);
    }

    public static boolean musicaMenu() {
        return leer(INSTANCE.musicaMenu, true);
    }

    /** 0 aleatoria; 1 Absurdism; 2 REQUIEM; 3 Upon the Hill V2. */
    public static int pistaMusica() {
        return SPEC.isLoaded() ? INSTANCE.pistaMusica.get() : 0;
    }

    public static boolean creditoMusica() {
        return leer(INSTANCE.creditoMusica, true);
    }

    public static boolean eventosAmbientales() {
        return leer(INSTANCE.eventosAmbientales, true);
    }

    public static boolean presenciaFondo() {
        return leer(INSTANCE.presenciaFondo, true);
    }

    public static boolean respiracionCamara() {
        return leer(INSTANCE.respiracionCamara, true);
    }

    public static boolean suspensionRara() {
        return leer(INSTANCE.suspensionRara, true);
    }

    /** Segundos de permanencia de cada nivel antes del apagon. */
    public static int duracionEstancia() {
        if (!SPEC.isLoaded()) {
            return 24;
        }
        return INSTANCE.duracionEstancia.get();
    }

    /** Modo de bajo consumo: recorta los efectos de aire y de escena. */
    public static boolean bajoConsumo() {
        return leer(INSTANCE.bajoConsumo, false);
    }

    /** Si el perfil accesible (las cuatro opciones juntas) esta activo. */
    public static boolean perfilAccesible() {
        return leer(INSTANCE.perfilAccesible, false);
    }

    /** Volumen del tema del menu, ya convertido a la escala 0.0 - 1.0 del motor. */
    public static float volumenMusica() {
        if (!SPEC.isLoaded()) {
            return 0.70F;
        }
        return INSTANCE.volumenMusica.get() / 100.0F;
    }

    /** Volumen del ambiente ya convertido a la escala 0.0 - 1.0 del motor. */
    public static float volumenAmbiente() {
        if (!SPEC.isLoaded()) {
            return 0.55F;
        }
        return INSTANCE.volumenAmbiente.get() / 100.0F;
    }

    private static final long GUARDAR_MS = 250L;

    private static long ultimoGuardadoMs;
    private static long cambiosAplicados;
    private static long cambiosOmitidos;
    private static long guardadosRealizados;
    private static boolean guardadoPendiente;
    private static ForgeConfigSpec.ConfigValue<?> valorPendiente;

    private static void fijar(ForgeConfigSpec.BooleanValue destino, boolean valor) {
        if (!SPEC.isLoaded()) {
            return;
        }
        if (destino.get() == valor) {
            cambiosOmitidos++;
            return;
        }
        destino.set(valor);
        cambiosAplicados++;
        marcarGuardado(destino);
    }

    private static void fijar(ForgeConfigSpec.IntValue destino, int valor) {
        if (!SPEC.isLoaded()) {
            return;
        }
        if (destino.get() == valor) {
            cambiosOmitidos++;
            return;
        }
        destino.set(valor);
        cambiosAplicados++;
        marcarGuardado(destino);
    }

    private static void marcarGuardado(ForgeConfigSpec.ConfigValue<?> destino) {
        valorPendiente = destino;
        guardadoPendiente = true;
        long ahora = System.currentTimeMillis();
        if (ahora - ultimoGuardadoMs >= GUARDAR_MS) {
            volcarGuardado();
        }
    }

    public static void guardarPendiente() {
        volcarGuardado();
    }

    private static void volcarGuardado() {
        if (!guardadoPendiente || !SPEC.isLoaded() || valorPendiente == null) {
            return;
        }
        guardadoPendiente = false;
        ultimoGuardadoMs = System.currentTimeMillis();
        ForgeConfigSpec.ConfigValue<?> valor = valorPendiente;
        valorPendiente = null;
        valor.save();
        guardadosRealizados++;
    }

    public static boolean guardadoPendienteParaDiagnostico() {
        return guardadoPendiente;
    }

    public static long cambiosAplicadosParaDiagnostico() {
        return cambiosAplicados;
    }

    public static long cambiosOmitidosParaDiagnostico() {
        return cambiosOmitidos;
    }

    public static long guardadosRealizadosParaDiagnostico() {
        return guardadosRealizados;
    }

    public static long ultimoGuardadoHaceMsParaDiagnostico() {
        if (ultimoGuardadoMs <= 0L) return -1L;
        return Math.max(0L, System.currentTimeMillis() - ultimoGuardadoMs);
    }

    public static boolean rotarNivelesBruto() {
        return leer(INSTANCE.rotarNiveles, true);
    }

    public static boolean rotacionCalma() {
        return leer(INSTANCE.rotacionCalma, false);
    }

    public static boolean mostrarFecha() {
        return !interfazMinima() && leer(INSTANCE.mostrarFecha, true);
    }

    public static boolean mostrarEstadoInstalacion() {
        return !interfazMinima() && leer(INSTANCE.mostrarEstadoInstalacion, true);
    }

    public static boolean guiaLectura() {
        return leer(INSTANCE.guiaLectura, true);
    }

    public static int duracionAvisos() {
        if (!SPEC.isLoaded()) {
            return 7;
        }
        return INSTANCE.duracionAvisos.get();
    }

    public static boolean mostrarFechaBruto() {
        return leer(INSTANCE.mostrarFecha, true);
    }

    public static int volumenAvisoPorcentaje() {
        return SPEC.isLoaded() ? INSTANCE.volumenAviso.get() : 100;
    }

    public static float volumenAviso() {
        return volumenAvisoPorcentaje() / 100.0F;
    }

    public static boolean mostrarCuentaRegresivaBruto() {
        return leer(INSTANCE.mostrarCuentaRegresiva, true);
    }

    public static boolean avisosRotativosBruto() {
        return leer(INSTANCE.avisosRotativos, true);
    }

    public static int volumenMusicaPorcentaje() {
        return SPEC.isLoaded() ? INSTANCE.volumenMusica.get() : 70;
    }

    public static int volumenAmbientePorcentaje() {
        return SPEC.isLoaded() ? INSTANCE.volumenAmbiente.get() : 55;
    }

    public static void fijarMenuPropio(boolean valor) {
        fijar(INSTANCE.menuPropio, valor);
    }

    public static void fijarPausaPropia(boolean valor) {
        fijar(INSTANCE.pausaPropia, valor);
    }

    public static void fijarEscenaViva(boolean valor) {
        fijar(INSTANCE.escenaViva, valor);
    }

    public static void fijarRotarNiveles(boolean valor) {
        fijar(INSTANCE.rotarNiveles, valor);
    }

    public static void fijarRotacionCalma(boolean valor) {
        fijar(INSTANCE.rotacionCalma, valor);
    }

    public static void fijarNivelFijo(int nivel) {
        fijar(INSTANCE.nivelFijo, Math.max(0, Math.min(31, nivel)));
    }

    private static void fijarConSalidaDePerfil(ForgeConfigSpec.BooleanValue destino,
                                               boolean valor) {
        if (SPEC.isLoaded() && INSTANCE.perfilAccesible.get()) {
            fijar(INSTANCE.perfilAccesible, false);
        }
        fijar(destino, valor);
    }

    public static void fijarMovimientoReducido(boolean valor) {
        fijarConSalidaDePerfil(INSTANCE.movimientoReducido, valor);
    }

    public static void fijarDestellosReducidos(boolean valor) {
        fijarConSalidaDePerfil(INSTANCE.destellosReducidos, valor);
    }

    public static void fijarAltoContraste(boolean valor) {
        fijarConSalidaDePerfil(INSTANCE.altoContraste, valor);
    }

    public static void fijarTextoGrande(boolean valor) {
        fijarConSalidaDePerfil(INSTANCE.textoGrande, valor);
    }

    public static void fijarPapelLimpio(boolean valor) {
        fijar(INSTANCE.papelLimpio, valor);
    }

    public static void fijarInterfazMinima(boolean valor) {
        fijar(INSTANCE.interfazMinima, valor);
    }

    public static void fijarMostrarCuentaRegresiva(boolean valor) {
        fijar(INSTANCE.mostrarCuentaRegresiva, valor);
    }

    public static void fijarMostrarFecha(boolean valor) {
        fijar(INSTANCE.mostrarFecha, valor);
    }

    public static void fijarMostrarEstadoInstalacion(boolean valor) {
        fijar(INSTANCE.mostrarEstadoInstalacion, valor);
    }

    public static void fijarGuiaLectura(boolean valor) {
        fijar(INSTANCE.guiaLectura, valor);
    }

    public static void fijarDuracionAvisos(int segundos) {
        fijar(INSTANCE.duracionAvisos, Math.max(4, Math.min(15, segundos)));
    }

    public static void fijarAvisosRotativos(boolean valor) {
        fijar(INSTANCE.avisosRotativos, valor);
    }

    public static void fijarVolumenAviso(int porcentaje) {
        fijar(INSTANCE.volumenAviso, Math.max(0, Math.min(100, porcentaje)));
    }

    public static void fijarSonidoBotones(boolean valor) {
        fijar(INSTANCE.sonidoBotones, valor);
    }

    public static void fijarSonidoAmbiente(boolean valor) {
        fijar(INSTANCE.sonidoAmbiente, valor);
    }

    public static void fijarMusicaMenu(boolean valor) {
        fijar(INSTANCE.musicaMenu, valor);
    }

    public static void fijarPistaMusica(int pista) {
        fijar(INSTANCE.pistaMusica, Math.max(0, Math.min(3, pista)));
    }

    public static void fijarCreditoMusica(boolean valor) {
        fijar(INSTANCE.creditoMusica, valor);
    }

    public static void fijarEventosAmbientales(boolean valor) {
        fijar(INSTANCE.eventosAmbientales, valor);
    }

    public static void fijarPresenciaFondo(boolean valor) {
        fijar(INSTANCE.presenciaFondo, valor);
    }

    public static void fijarRespiracionCamara(boolean valor) {
        fijar(INSTANCE.respiracionCamara, valor);
    }

    public static void fijarSuspensionRara(boolean valor) {
        fijar(INSTANCE.suspensionRara, valor);
    }

    public static void fijarDuracionEstancia(int segundos) {
        fijar(INSTANCE.duracionEstancia, Math.max(15, Math.min(90, segundos)));
    }

    public static void fijarBajoConsumo(boolean valor) {
        fijar(INSTANCE.bajoConsumo, valor);
    }

    public static void fijarPerfilAccesible(boolean valor) {
        if (!SPEC.isLoaded()) return;

        boolean cambio = false;
        if (INSTANCE.perfilAccesible.get() != valor) {
            INSTANCE.perfilAccesible.set(valor);
            cambio = true;
        }
        if (valor) {
            if (!INSTANCE.movimientoReducido.get()) {
                INSTANCE.movimientoReducido.set(true);
                cambio = true;
            }
            if (!INSTANCE.destellosReducidos.get()) {
                INSTANCE.destellosReducidos.set(true);
                cambio = true;
            }
            if (!INSTANCE.altoContraste.get()) {
                INSTANCE.altoContraste.set(true);
                cambio = true;
            }
            if (!INSTANCE.textoGrande.get()) {
                INSTANCE.textoGrande.set(true);
                cambio = true;
            }
        }
        if (!cambio) {
            cambiosOmitidos++;
            return;
        }
        cambiosAplicados++;
        marcarGuardado(INSTANCE.perfilAccesible);
    }

    public static void fijarVolumenMusica(int porcentaje) {
        fijar(INSTANCE.volumenMusica, Math.max(0, Math.min(100, porcentaje)));
    }

    public static void fijarVolumenAmbiente(int porcentaje) {
        fijar(INSTANCE.volumenAmbiente, Math.max(0, Math.min(100, porcentaje)));
    }
}

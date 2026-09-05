package com.santipdr.jobsmenu.client.sound;

import com.santipdr.jobsmenu.JobsMenu;
import com.santipdr.jobsmenu.client.SesionMenu;
import com.santipdr.jobsmenu.client.scene.Presencia;
import com.santipdr.jobsmenu.client.scene.RotacionNiveles;
import com.santipdr.jobsmenu.config.ConfigTurno;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraftforge.registries.RegistryObject;

import org.lwjgl.glfw.GLFW;

/**
 * Reproductor de sesion del menu Jobs.
 *
 * La musica pertenece a la visita completa, no a una Screen concreta. Por eso
 * continua al abrir Opciones, Mods, Mundos o Recursos y se corta de forma
 * defensiva antes de dejar que exista un tick audible dentro de un mundo.
 *
 * 0.17 introduce un catalogo real de pistas. Cada pista entra y sale con su
 * propia ganancia; cuando haya mas de una disponible el gestor hace crossfade
 * sin reiniciar el ambiente ni pelearse con la musica vanilla. La primera pista
 * del catalogo es Absurdism. La segunda pista solicitada queda documentada como
 * fuente pendiente hasta que exista un archivo OGG autorizado en el proyecto.
 */
public final class GestorMusica extends AbstractTickableSoundInstance {

    private static final float SUAVIZADO_SUBIDA = 0.036F;
    private static final float SUAVIZADO_BAJADA = 0.065F;
    private static final float SUAVIZADO_CROSSFADE = 0.030F;
    private static final int RETARDO_INICIAL = 20;
    private static final int CAMBIO_MIN_TICKS = 2 * 60 * 20;
    private static final int CAMBIO_VARIACION_TICKS = 2 * 60 * 20;

    private static GestorMusica principal;
    private static GestorMusica entrante;
    private static int indiceActual;
    private static int ticksSesion;
    private static int proximoCambio = CAMBIO_MIN_TICKS;
    private static int reintento;
    private static int marcador = -1;
    private static int selectorVisto = -1;

    private final String idPista;
    private float actual;
    private float gananciaActual;
    private float gananciaObjetivo;
    private int edad;
    private int ultimaEdadVista = -1;

    private GestorMusica(String idPista, SoundEvent evento, float gananciaInicial,
                         float gananciaObjetivo, int retardo) {
        super(evento, SoundSource.MASTER, RandomSource.create());
        this.idPista = idPista;
        this.looping = true;
        this.delay = Math.max(0, retardo);
        this.volume = 0.0F;
        this.pitch = 1.0F;
        this.relative = true;
        this.attenuation = Attenuation.NONE;
        this.x = 0.0D;
        this.y = 0.0D;
        this.z = 0.0D;
        this.actual = 0.0F;
        this.gananciaActual = Math.max(0.0F, Math.min(1.0F, gananciaInicial));
        this.gananciaObjetivo = Math.max(0.0F, Math.min(1.0F, gananciaObjetivo));
    }

    private record Pista(String id, RegistryObject<SoundEvent> evento, String recurso,
                         String titulo, String autor) {
    }

    /** Catalogo real de la sesion. Cada pista conserva identidad y credito. */
    private static Pista[] catalogo() {
        return new Pista[] {
                new Pista("absurdism", SonidosNivel.MUSICA_TEMA, "musica/defecto.ogg",
                        "Absurdism", ""),
                new Pista("requiem", SonidosNivel.MUSICA_REQUIEM, "musica/requiem.ogg",
                        "REQUIEM", "Emmy Z - Forsaken OST"),
                new Pista("upon_the_hill_v2", SonidosNivel.MUSICA_UPON_HILL,
                        "musica/upon_the_hill_v2.ogg", "Upon the Hill V2",
                        "ft. @iCosmicCoffee")
        };
    }

    public static void asegurar() {
        if (!SesionMenu.activa() || !ConfigTurno.musicaMenu() || reintento > 0) return;
        if (viva(principal) || viva(entrante)) return;

        Pista[] pistas = catalogo();
        if (pistas.length == 0) return;
        int fijada = indiceFijado(pistas);
        if (fijada >= 0) indiceActual = fijada;
        selectorVisto = ConfigTurno.pistaMusica();
        indiceActual = Math.floorMod(indiceActual, pistas.length);
        principal = crear(pistas[indiceActual], 0.0F, 1.0F, RETARDO_INICIAL);
        Minecraft.getInstance().getSoundManager().play(principal);
        JobsMenu.LOG.info("[jobsmenu] Pista de menu iniciada: {} ({}).",
                pistas[indiceActual].id(), pistas[indiceActual].recurso());
    }

    private static GestorMusica crear(Pista pista, float desde, float hasta, int retardo) {
        return new GestorMusica(pista.id(), resolverPista(pista), desde, hasta, retardo);
    }

    /**
     * Mantiene explicitamente el camino de la pista actual por el guard comun.
     * El segundo branch permite que futuras pistas nominales usen el mismo
     * mecanismo sin accesos directos a RegistryObject.get().
     */
    private static SoundEvent resolverPista(Pista pista) {
        if (pista.evento() == SonidosNivel.MUSICA_TEMA) {
            return MezclaAudio.resolver(SonidosNivel.MUSICA_TEMA, SoundEvents.MUSIC_MENU.value());
        }
        return MezclaAudio.resolver(pista.evento(), SoundEvents.MUSIC_MENU.value());
    }

    private static int indiceFijado(Pista[] pistas) {
        int seleccion = ConfigTurno.pistaMusica();
        if (seleccion <= 0 || pistas.length == 0) return -1;
        return Math.min(pistas.length - 1, seleccion - 1);
    }

    /**
     * Aplica cambios del selector sin reiniciar el ambiente. Una pista fija
     * entra por crossfade; volver a Aleatoria conserva la actual y reactiva la
     * rotacion para el siguiente intervalo.
     */
    private static void sincronizarSeleccion() {
        int seleccion = ConfigTurno.pistaMusica();
        if (seleccion == selectorVisto) return;
        selectorVisto = seleccion;
        ticksSesion = 0;
        proximoCambio = CAMBIO_MIN_TICKS
                + (int) (Math.random() * CAMBIO_VARIACION_TICKS);
        if (seleccion <= 0) return;

        Pista[] pistas = catalogo();
        int objetivo = indiceFijado(pistas);
        if (objetivo < 0) return;
        Pista pista = pistas[objetivo];

        if (viva(entrante) && entrante.idPista.equals(pista.id())) {
            entrante.gananciaObjetivo = 1.0F;
            if (viva(principal)) principal.gananciaObjetivo = 0.0F;
            indiceActual = objetivo;
            return;
        }
        if (viva(principal) && principal.idPista.equals(pista.id())) {
            principal.gananciaObjetivo = 1.0F;
            if (viva(entrante)) entrante.detener(true);
            entrante = null;
            indiceActual = objetivo;
            return;
        }
        if (viva(entrante)) entrante.detener(true);
        entrante = null;
        if (viva(principal)) {
            principal.gananciaObjetivo = 0.0F;
            entrante = crear(pista, 0.0F, 1.0F, 0);
            Minecraft.getInstance().getSoundManager().play(entrante);
        } else {
            principal = crear(pista, 0.0F, 1.0F, 0);
            Minecraft.getInstance().getSoundManager().play(principal);
        }
        indiceActual = objetivo;
        JobsMenu.LOG.info("[jobsmenu] Seleccion fija aplicada: {}.", pista.id());
    }

    public static void atender() {
        if (reintento > 0) reintento--;

        Minecraft cliente = Minecraft.getInstance();
        boolean sesionMusical = SesionMenu.activa() && ConfigTurno.musicaMenu();
        if (!sesionMusical) return;

        cliente.getMusicManager().stopPlaying();
        sincronizarSeleccion();
        asegurar();
        ticksSesion++;

        boolean clienteTicando = !cliente.isPaused() && GLFW.glfwGetWindowAttrib(
                cliente.getWindow().getWindow(), GLFW.GLFW_FOCUSED) == GLFW.GLFW_TRUE;
        if (clienteTicando && (fantasma(principal) || fantasma(entrante))) {
            JobsMenu.LOG.warn("[jobsmenu] Una pista dejo de recibir ticks del motor; "
                    + "se reconstruye la sesion musical.");
            reiniciarMotor();
            return;
        }
        if (!clienteTicando) {
            if (principal != null) principal.ultimaEdadVista = -1;
            if (entrante != null) entrante.ultimaEdadVista = -1;
        }

        atenderCrossfade();
    }

    private static boolean fantasma(GestorMusica pista) {
        if (!viva(pista)) return false;
        if (pista.ultimaEdadVista >= 0 && pista.edad == pista.ultimaEdadVista) return true;
        pista.ultimaEdadVista = pista.edad;
        return false;
    }

    /**
     * Con una sola pista no hace nada. Con dos o mas, abre la siguiente a cero,
     * sube su ganancia y retira la anterior en paralelo.
     */
    private static void atenderCrossfade() {
        if (!viva(principal) && viva(entrante)) {
            principal = entrante;
            entrante = null;
        }

        Pista[] pistas = catalogo();
        boolean permiteRotacion = pistas.length > 1 && ConfigTurno.pistaMusica() == 0;

        if (permiteRotacion && entrante == null
                && ticksSesion >= proximoCambio && viva(principal)) {
            int siguiente = siguienteIndice(pistas);
            entrante = crear(pistas[siguiente], 0.0F, 1.0F, 0);
            principal.gananciaObjetivo = 0.0F;
            Minecraft.getInstance().getSoundManager().play(entrante);
            indiceActual = siguiente;
            ticksSesion = 0;
            proximoCambio = CAMBIO_MIN_TICKS
                    + (int) (Math.random() * CAMBIO_VARIACION_TICKS);
            JobsMenu.LOG.info("[jobsmenu] Crossfade hacia pista: {}.", pistas[siguiente].id());
        }

        if (entrante != null && principal != null
                && principal.gananciaActual <= 0.004F && principal.actual <= 0.004F) {
            principal.stop();
            principal = entrante;
            entrante = null;
        }
    }

    private static int siguienteIndice(Pista[] pistas) {
        if (pistas.length <= 1) return 0;
        int salto = 1 + (int) (Math.random() * (pistas.length - 1));
        return (indiceActual + salto) % pistas.length;
    }

    /** Salta manualmente a otra pista sin repetir la actual. */
    public static boolean adelantarPista() {
        Pista[] pistas = catalogo();
        if (pistas.length <= 1 || ConfigTurno.pistaMusica() != 0
                || !SesionMenu.activa() || !ConfigTurno.musicaMenu()
                || !viva(principal) || viva(entrante)) return false;
        int siguiente = siguienteIndice(pistas);
        entrante = crear(pistas[siguiente], 0.0F, 1.0F, 0);
        principal.gananciaObjetivo = 0.0F;
        Minecraft.getInstance().getSoundManager().play(entrante);
        indiceActual = siguiente;
        ticksSesion = 0;
        proximoCambio = CAMBIO_MIN_TICKS
                + (int) (Math.random() * CAMBIO_VARIACION_TICKS);
        JobsMenu.LOG.info("[jobsmenu] Cambio manual hacia pista: {}.", pistas[siguiente].id());
        return true;
    }

    public static void nuevaVisita() {
        detenerInstancias(true);
        reintento = 0;
        marcador = -1;
        Pista[] pistas = catalogo();
        selectorVisto = ConfigTurno.pistaMusica();
        int fijada = indiceFijado(pistas);
        indiceActual = fijada >= 0 ? fijada
                : (pistas.length <= 1 ? 0 : (int) (Math.random() * pistas.length));
        ticksSesion = 0;
        proximoCambio = CAMBIO_MIN_TICKS
                + (int) (Math.random() * CAMBIO_VARIACION_TICKS);
    }

    /**
     * Gameplay es frontera dura: no se hace fade aqui porque una cola bonita
     * seria precisamente el bug de musica de menu dentro del mundo.
     */
    public static void detenerAhora() {
        detenerInstancias(true);
        reintento = 0;
        ticksSesion = 0;
    }

    public static void recursosRecargados() {
        detenerInstancias(true);
        marcador = -1;
        selectorVisto = -1;
        reintento = 20;
        ticksSesion = 0;
    }

    private static void reiniciarMotor() {
        detenerInstancias(true);
        reintento = 2;
        ticksSesion = 0;
    }

    private static void detenerInstancias(boolean inmediato) {
        GestorMusica a = principal;
        GestorMusica b = entrante;
        principal = null;
        entrante = null;
        if (a != null) a.detener(inmediato);
        if (b != null && b != a) b.detener(inmediato);
    }

    private void detener(boolean inmediato) {
        if (inmediato) {
            this.actual = 0.0F;
            this.gananciaActual = 0.0F;
            this.gananciaObjetivo = 0.0F;
            this.volume = 0.0F;
            this.stop();
        } else {
            this.gananciaObjetivo = 0.0F;
        }
    }

    private static boolean viva(GestorMusica pista) {
        return pista != null && !pista.isStopped();
    }

    public static boolean sonando() {
        return viva(principal) || viva(entrante);
    }

    public static int reintentoParaDiagnostico() {
        return reintento;
    }

    public static String pistaParaDiagnostico() {
        if (viva(entrante) && entrante.gananciaActual > (principal == null ? 0.0F : principal.gananciaActual)) {
            return entrante.idPista;
        }
        return principal == null ? "-" : principal.idPista;
    }

    private static GestorMusica pistaDominante() {
        if (viva(entrante) && entrante.gananciaActual > (principal == null ? 0.0F : principal.gananciaActual)) {
            return entrante;
        }
        return principal;
    }

    private static Pista datosPista(String id) {
        if (id == null) return null;
        for (Pista pista : catalogo()) if (pista.id().equals(id)) return pista;
        return null;
    }

    public static String tituloPistaActual() {
        GestorMusica m = pistaDominante();
        Pista pista = m == null ? null : datosPista(m.idPista);
        return pista == null ? "-" : pista.titulo();
    }

    public static String autorPistaActual() {
        GestorMusica m = pistaDominante();
        Pista pista = m == null ? null : datosPista(m.idPista);
        return pista == null ? "" : pista.autor();
    }

    public static int cantidadPistas() {
        return catalogo().length;
    }

    public static float creditoAlfa() {
        GestorMusica m = pistaDominante();
        if (!viva(m) || !ConfigTurno.musicaMenu() || !ConfigTurno.creditoMusica()
                || !marcadorHorneado()) return 0.0F;

        int e = m.edad;
        final int entra0 = 40;
        final int entra1 = 90;
        final int sale0 = 300;
        final int sale1 = 360;
        if (e <= entra0 || e >= sale1) return 0.0F;
        if (e < entra1) return (e - entra0) / (float) (entra1 - entra0);
        if (e <= sale0) return 1.0F;
        return 1.0F - (e - sale0) / (float) (sale1 - sale0);
    }

    private static boolean marcadorHorneado() {
        if (marcador < 0) {
            boolean hay = Minecraft.getInstance().getResourceManager()
                    .getResource(new ResourceLocation("jobsmenu", "musica_creditada.txt")).isPresent();
            marcador = hay ? 1 : 0;
        }
        return marcador == 1;
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }

    @Override
    public void tick() {
        this.edad++;

        boolean permitido = SesionMenu.activa() && ConfigTurno.musicaMenu();
        if (!permitido) this.gananciaObjetivo = 0.0F;

        float pasoGanancia = this.gananciaObjetivo > this.gananciaActual
                ? SUAVIZADO_CROSSFADE : Math.max(SUAVIZADO_CROSSFADE, SUAVIZADO_BAJADA);
        this.gananciaActual += (this.gananciaObjetivo - this.gananciaActual) * pasoGanancia;
        if (Math.abs(this.gananciaActual - this.gananciaObjetivo) < 0.0005F) {
            this.gananciaActual = this.gananciaObjetivo;
        }

        float objetivo = 0.0F;
        if (permitido) {
            objetivo = ConfigTurno.volumenMusica() * MezclaAudio.MUSICA
                    * ConfigTurno.volumenAviso();

            RotacionNiveles.Estado estado = RotacionNiveles.capturar();
            if (estado.enTransicion()) objetivo *= 0.78F;
            if (estado.enSuspension()) objetivo *= 0.18F;
            objetivo *= 1.0F - (1.0F - MezclaAudio.AGACHE_FIGURA)
                    * 0.5F * Presencia.visibilidad(estado.ahora());
            objetivo *= this.gananciaActual;
        }

        float paso = objetivo > this.actual ? SUAVIZADO_SUBIDA : SUAVIZADO_BAJADA;
        this.actual += (objetivo - this.actual) * paso;
        if (Math.abs(this.actual - objetivo) < 0.0005F) this.actual = objetivo;
        if (this.actual < 0.0005F) this.actual = 0.0F;
        this.volume = this.actual;

        if ((!permitido || this.gananciaObjetivo <= 0.0F)
                && this.gananciaActual <= 0.004F && this.actual <= 0.004F) {
            this.stop();
            if (principal == this) principal = null;
            if (entrante == this) entrante = null;
        }
    }
}

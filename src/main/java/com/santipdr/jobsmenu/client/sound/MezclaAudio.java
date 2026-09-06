package com.santipdr.jobsmenu.client.sound;

import com.santipdr.jobsmenu.config.ConfigTurno;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.RegistryObject;

/**
 * La mesa de mezcla del menu.
 *
 * Todo lo que suena pasa por aca, y aca esta decidido de una vez cuanto pesa
 * cada cosa. El orden es deliberado y no se negocia por sonido:
 *
 *   musica    - sostiene la escena, nunca la tapa
 *   ambiente  - se escucha por debajo de la musica, siempre presente
 *   evento    - asoma y se va; no debe hacer levantar la cabeza
 *   interfaz  - lo mas breve y lo mas bajo, porque es lo que mas se repite
 *   nivel     - la transicion, unica autorizada a pisar al resto un momento
 *
 * Tener los pesos en un solo lugar es lo que hace que la mezcla se pueda
 * corregir sin salir a buscar constantes por diez archivos.
 */
public final class MezclaAudio {

    private MezclaAudio() {
    }

    /** El foco puede saltar entre widgets en el mismo frame al redimensionar. */
    private static long ultimoRoceNanos;

    /** Evita repetir el mismo aviso si un pack/arranque deja un registro incompleto. */
    private static boolean avisoRegistroFaltante;

    /**
     * MARGEN DE MEZCLA
     *
     * Con las cifras anteriores, el peor caso -cama y caracter y actividad y
     * musica y un evento y el apagon y un gesto de interfaz, todo cayendo en
     * el mismo instante- llegaba a 0.94 de pico: medio decibelio de margen.
     * Eso no es una mezcla con cabeza, es una mezcla que todavia no distorsiono
     * de casualidad, y basta con que el jugador suba el volumen maestro o que
     * un resource pack cambie una pieza para que empiece a recortar.
     *
     * Bajar tres decibelios el conjunto no se oye -el volumen maestro lo
     * compensa- y compra el margen que hace falta para que las coincidencias
     * raras no rompan nada.
     */

    /**
     * Tema del menu.
     *
     * Subido de 0.34 a 0.55 en 0.6.4. Con la pista propia sintetizada el 0.34
     * bastaba -era un lecho armonico de fondo-, pero una pista musical con
     * melodia necesita margen para escucharse. A 0.34, con la entrada lenta, el
     * jugador podia entrar, mirar y salir sin oir una nota. Sigue por debajo de
     * un evento o de la transicion: acompana, pero ahora se oye.
     */
    public static final float MUSICA = 0.55F;

    /** Ambiente base del nivel, ya multiplicado por el volumen de la config. */
    public static final float AMBIENTE = 0.66F;

    /** Eventos ocasionales del nivel. */
    public static final float EVENTO = 0.48F;

    /**
     * Gestos de interfaz.
     *
     * Se sube respecto del resto, no se baja. Las ocho piezas se remezclaron
     * con un balance propio -pasar suena siete decibelios por debajo de
     * confirmar porque suena treinta veces mas seguido- y ese balance ya deja
     * los gestos frecuentes muy abajo. Aplicarles ademas la reduccion general
     * los habria dejado por debajo del piso del ambiente.
     */
    public static final float INTERFAZ = 0.54F;

    /** Apagon y encendido. Se les permite mandar durante la transicion. */
    public static final float TRANSICION = 0.72F;

    /** La figura. Apenas por encima del piso de ruido, a proposito. */
    public static final float FIGURA = 0.40F;

    /**
     * Silencio de un toque: el volumen guardado antes de silenciar.
     *
     * Vive aca, en la mesa de mezcla, porque lo que se silencia es la mezcla
     * entera (musica + ambiente + gestos) y no un grupo en particular.
     */
    private static int silencioPrevio = 100;

    /**
     * Alterna el volumen maestro del aviso entre cero y el ultimo valor
     * recordado. Se engancha a la tecla M del aviso y de la pausa.
     */
    public static void alternarSilencio() {
        int actual = ConfigTurno.volumenAvisoPorcentaje();
        if (actual > 0) {
            silencioPrevio = actual;
            ConfigTurno.fijarVolumenAviso(0);
        } else {
            ConfigTurno.fijarVolumenAviso(silencioPrevio);
        }
    }

    /**
     * Cuanto baja el resto mientras la figura esta presente.
     *
     * No se corta nada: se afloja. Un corte se nota como un corte y delata el
     * truco; una bajada de un tercio se siente como que el aire se puso denso.
     */
    public static final float AGACHE_FIGURA = 0.62F;

    /**
     * Dispara un gesto de interfaz.
     *
     * Todos llevan una variacion minima de tono. Reproducir siempre la misma
     * muestra al mismo tono es lo que hace que un sonido de UI se vuelva
     * insoportable a los cincuenta usos: el oido aprende el archivo. Un dos por
     * ciento arriba o abajo alcanza para que no lo aprenda nunca.
     */
    public static void gesto(RegistryObject<SoundEvent> evento, float volumen) {
        if (!ConfigTurno.sonidoBotones()) {
            return;
        }
        if (evento == SonidosNivel.UI_PASAR) {
            long ahora = System.nanoTime();
            if (ahora - ultimoRoceNanos < 80_000_000L) {
                return;
            }
            ultimoRoceNanos = ahora;
        }
        SoundEvent sonido = resolverPersonalizado(evento);
        if (sonido == null) return;
        float tono = tonoGesto(evento);
        Minecraft.getInstance().getSoundManager()
                .play(SimpleSoundInstance.forUI(sonido, tono,
                        volumen * INTERFAZ * ConfigTurno.volumenAviso()));
    }

    /**
     * Sustituye el click vanilla de controles que conservan logica nativa.
     * Devuelve null si el registro aun no esta listo: es preferible silencio
     * transitorio a romper la identidad con el clac de Minecraft.
     */
    public static SoundInstance reemplazoClickVanilla() {
        SoundEvent sonido = resolverPersonalizado(SonidosNivel.UI_ELEGIR);
        if (sonido == null) return null;
        return SimpleSoundInstance.forUI(sonido, tonoGesto(SonidosNivel.UI_ELEGIR),
                0.72F * INTERFAZ * ConfigTurno.volumenAviso());
    }

    /** Cada gesto ocupa una franja de tono propia, sin sonar mecanicamente repetido. */
    private static float tonoGesto(RegistryObject<SoundEvent> evento) {
        float azar = (float) Math.random();
        if (evento == SonidosNivel.UI_PASAR) return 1.035F + azar * 0.035F;
        if (evento == SonidosNivel.UI_CONFIRMAR) return 0.955F + azar * 0.025F;
        if (evento == SonidosNivel.UI_NEGADO) return 0.915F + azar * 0.025F;
        if (evento == SonidosNivel.UI_VOLVER || evento == SonidosNivel.UI_CERRAR) {
            return 0.975F + azar * 0.025F;
        }
        return 0.990F + azar * 0.030F;
    }

    private static SoundEvent resolverPersonalizado(RegistryObject<SoundEvent> evento) {
        if (evento != null && evento.isPresent()) return evento.get();
        if (!avisoRegistroFaltante) {
            avisoRegistroFaltante = true;
            com.santipdr.jobsmenu.JobsMenu.LOG.warn(
                    "[jobsmenu] Un gesto propio aun no esta registrado; se omite sin usar audio vanilla.");
        }
        return null;
    }

    /**
     * Sonido puntual de ambiente Jobs. Si falta su registro, se omite: un FX
     * Jobs nunca debe convertirse en una cueva vanilla. La instancia se guarda
     * para poder detenerla al entrar a gameplay.
     */
    public static SoundInstance ambiental(RegistryObject<SoundEvent> evento, float volumen, float tono) {
        SoundEvent sonido = resolver(evento, null);
        if (sonido == null) return null;
        SoundInstance instancia = RastreadorAudioJobs.registrar(
                SimpleSoundInstance.forUI(sonido, tono,
                        volumen * ConfigTurno.volumenAviso()));
        Minecraft.getInstance().getSoundManager().play(instancia);
        return instancia;
    }

    /**
     * Obtiene un sonido sin convertir un registro incompleto en un crash de render.
     *
     * Un JAR viejo, un registro de otro entorno o una carga parcial de recursos
     * puede dejar un RegistryObject sin valor. get() lanza en ese caso, y el
     * camino de dibujo de un widget no debe propagarlo. El consumidor decide si
     * pasa un respaldo o si prefiere omitir el sonido con null.
     */
    public static SoundEvent resolver(RegistryObject<SoundEvent> evento, SoundEvent respaldo) {
        if (evento != null && evento.isPresent()) {
            return evento.get();
        }
        if (!avisoRegistroFaltante) {
            avisoRegistroFaltante = true;
            com.santipdr.jobsmenu.JobsMenu.LOG.warn(
                    "[jobsmenu] Falta un SoundEvent registrado; se aplicara el respaldo configurado.");
        }
        return respaldo;
    }

    /** Un reload abre una nueva ventana de diagnostico para registros faltantes. */
    public static void recursosRecargados() {
        avisoRegistroFaltante = false;
        ultimoRoceNanos = 0L;
    }
}

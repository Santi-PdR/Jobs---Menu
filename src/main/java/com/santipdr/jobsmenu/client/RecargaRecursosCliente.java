package com.santipdr.jobsmenu.client;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import com.santipdr.jobsmenu.JobsMenu;
import com.santipdr.jobsmenu.client.sound.GestorAmbiente;
import com.santipdr.jobsmenu.client.sound.GestorMusica;
import com.santipdr.jobsmenu.client.sound.MezclaAudio;
import com.santipdr.jobsmenu.client.sound.RastreadorAudioJobs;

import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Limpia instancias de sonido ligadas al SoundEngine anterior al recargar.
 *
 * El listener de recursos puede ejecutarse en el executor de recarga y no
 * necesariamente en el hilo de render. No se toca SoundInstance desde ese
 * hilo: solo se marca la recarga y el cierre se encola en el hilo del cliente.
 * Esto es especialmente importante al cambiar idioma, usar F3+T o aplicar un
 * resource pack junto a Embeddium/Better Clouds.
 */
@Mod.EventBusSubscriber(modid = JobsMenu.MOD_ID, value = Dist.CLIENT,
        bus = Mod.EventBusSubscriber.Bus.MOD)
public final class RecargaRecursosCliente {

    private static final AtomicBoolean TAREA_PENDIENTE = new AtomicBoolean();
    private static final AtomicLong GENERACION = new AtomicLong();

    private RecargaRecursosCliente() {
    }

    @SubscribeEvent
    public static void registrar(RegisterClientReloadListenersEvent evento) {
        evento.registerReloadListener((ResourceManagerReloadListener) recursos -> solicitarCierreEnCliente());
    }

    /**
     * Cada callback avanza la generacion. Una rafaga de recargas comparte una
     * sola tarea, pero si aparece otra generacion mientras la tarea se ejecuta
     * se agenda una segunda pasada. Asi una secuencia idioma -> F3+T -> pack no
     * puede quedar representada por un callback viejo que llego tarde.
     */
    private static void solicitarCierreEnCliente() {
        GENERACION.incrementAndGet();
        programarSiHaceFalta();
    }

    private static void programarSiHaceFalta() {
        if (!TAREA_PENDIENTE.compareAndSet(false, true)) {
            return;
        }
        Minecraft.getInstance().execute(RecargaRecursosCliente::cerrarUltimaGeneracion);
    }

    /** Se ejecuta exclusivamente en el hilo del cliente. */
    private static void cerrarUltimaGeneracion() {
        long procesada = GENERACION.get();
        GestorMusica.recursosRecargados();
        GestorAmbiente.recursosRecargados();
        RastreadorAudioJobs.recursosRecargados();
        MezclaAudio.recursosRecargados();
        TAREA_PENDIENTE.set(false);

        // Una recarga pudo terminar mientras se cerraban las instancias de la
        // anterior. No se pierde: se procesa en otra vuelta del hilo cliente.
        if (GENERACION.get() != procesada) {
            programarSiHaceFalta();
        }
    }

    /** Contador solo para el diagnostico oculto y pruebas de lifecycle. */
    public static long generacionParaDiagnostico() {
        return GENERACION.get();
    }
}

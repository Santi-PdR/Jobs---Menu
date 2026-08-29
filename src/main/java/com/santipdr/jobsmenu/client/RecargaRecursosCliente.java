package com.santipdr.jobsmenu.client;

import java.util.concurrent.atomic.AtomicBoolean;

import com.santipdr.jobsmenu.JobsMenu;
import com.santipdr.jobsmenu.client.sound.GestorAmbiente;
import com.santipdr.jobsmenu.client.sound.GestorMusica;

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

    private static final AtomicBoolean RECARGA_PENDIENTE = new AtomicBoolean();

    private RecargaRecursosCliente() {
    }

    @SubscribeEvent
    public static void registrar(RegisterClientReloadListenersEvent evento) {
        evento.registerReloadListener((ResourceManagerReloadListener) recursos -> solicitarCierreEnCliente());
    }

    /**
     * Pide el cierre una sola vez por tanda de recarga y lo ejecuta en el hilo
     * principal de Minecraft, no en el executor que procesa los recursos.
     */
    private static void solicitarCierreEnCliente() {
        if (!RECARGA_PENDIENTE.compareAndSet(false, true)) {
            return;
        }
        Minecraft.getInstance().execute(RecargaRecursosCliente::cerrarInstancias);
    }

    /** Se ejecuta en el hilo del cliente cuando termina la tanda de recursos. */
    private static void cerrarInstancias() {
        RECARGA_PENDIENTE.set(false);
        GestorMusica.recursosRecargados();
        GestorAmbiente.recursosRecargados();
    }
}

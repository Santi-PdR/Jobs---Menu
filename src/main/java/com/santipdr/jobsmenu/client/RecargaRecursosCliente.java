package com.santipdr.jobsmenu.client;

import com.santipdr.jobsmenu.JobsMenu;
import com.santipdr.jobsmenu.client.sound.GestorAmbiente;
import com.santipdr.jobsmenu.client.sound.GestorMusica;

import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Limpia instancias de sonido ligadas al SoundEngine anterior al recargar. */
@Mod.EventBusSubscriber(modid = JobsMenu.MOD_ID, value = Dist.CLIENT,
        bus = Mod.EventBusSubscriber.Bus.MOD)
public final class RecargaRecursosCliente {

    private RecargaRecursosCliente() {
    }

    @SubscribeEvent
    public static void registrar(RegisterClientReloadListenersEvent evento) {
        evento.registerReloadListener((ResourceManagerReloadListener) recursos -> {
            GestorMusica.recursosRecargados();
            GestorAmbiente.recursosRecargados();
        });
    }
}

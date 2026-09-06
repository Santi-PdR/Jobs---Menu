package com.santipdr.jobsmenu.client.sound;

import com.santipdr.jobsmenu.JobsMenu;
import com.santipdr.jobsmenu.client.SesionMenu;

import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Jobs posee la musica mientras su visita de menu esta activa.
 *
 * GestorMusica usa MASTER para su catalogo propio. Cualquier instancia MUSIC
 * que intente arrancar durante la visita pertenece por tanto al MusicManager
 * vanilla o a otra banda sonora de menu y se omite antes de entrar al motor.
 * Esto reemplaza el antiguo stopPlaying() ejecutado en cada tick.
 */
@Mod.EventBusSubscriber(modid = JobsMenu.MOD_ID, value = Dist.CLIENT)
public final class BloqueoMusicaVanillaJobs {

    private BloqueoMusicaVanillaJobs() {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void alReproducir(PlaySoundEvent evento) {
        if (!SesionMenu.activa() || evento.getOriginalSound() == null) return;
        if (evento.getOriginalSound().getSource() != SoundSource.MUSIC) return;
        evento.setSound(null);
    }
}

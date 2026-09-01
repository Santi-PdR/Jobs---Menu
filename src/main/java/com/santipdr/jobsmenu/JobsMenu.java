package com.santipdr.jobsmenu;

import com.mojang.logging.LogUtils;
import com.santipdr.jobsmenu.client.screen.PantallaAjustesAviso;
import com.santipdr.jobsmenu.client.sound.SonidosNivel;
import com.santipdr.jobsmenu.config.ConfigTurno;

import org.slf4j.Logger;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

/** Punto de entrada del mod, exclusivamente de cliente. */
@Mod(JobsMenu.MOD_ID)
public class JobsMenu {

    public static final String MOD_ID = "jobsmenu";
    public static final Logger LOG = LogUtils.getLogger();

    public JobsMenu() {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ModLoadingContext contexto = ModLoadingContext.get();
            contexto.registerConfig(ModConfig.Type.CLIENT, ConfigTurno.SPEC, "jobsmenu-client.toml");

            // Restaura el boton Config de Forge: Mods -> Jobs Menu -> Config.
            // La misma pantalla se usa desde el hub de opciones del propio mod.
            contexto.registerExtensionPoint(
                    ConfigScreenHandler.ConfigScreenFactory.class,
                    () -> new ConfigScreenHandler.ConfigScreenFactory(
                            (minecraft, anterior) -> new PantallaAjustesAviso(anterior, minecraft.options)));

            SonidosNivel.inscribir(FMLJavaModLoadingContext.get().getModEventBus());
        }
    }
}

package com.santipdr.jobsmenu.client;

import com.santipdr.jobsmenu.JobsMenu;
import com.santipdr.jobsmenu.client.screen.PantallaAjustesAviso;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Mete los ajustes del aviso DENTRO del menu de opciones del juego.
 *
 * La idea es que haya UN solo sitio de configuracion. En vez de una pantalla de
 * ajustes propia y aparte, el mod agrega un boton a la pantalla de opciones de
 * vanilla -la de imagen, sonido, controles, idioma- que abre una subpantalla
 * mas, hecha con las mismas piezas que las demas (ver PantallaAjustesAviso).
 * Desde el punto de vista de quien juega, es una seccion mas de las opciones.
 *
 * COMO SE INSERTA SIN PISAR NADA
 *
 * La pantalla de opciones ordena sus botones con una grilla centrada que ya
 * esta armada cuando llega este evento. Meter un boton en esa grilla a
 * posteriori no se puede sin rehacerla, asi que el boton del mod se coloca
 * suelto, arriba a la izquierda, en una esquina que la grilla no usa. Es
 * discreto y no tapa ninguno de los botones de vanilla en ninguna resolucion.
 *
 * Solo actua sobre OptionsScreen y solo si el menu propio esta activo: si el
 * jugador apago el mod, no ensucia las opciones con un boton de algo que decidio
 * no usar.
 */
@Mod.EventBusSubscriber(modid = JobsMenu.MOD_ID, value = Dist.CLIENT)
public final class AjustesAviso {

    private AjustesAviso() {
    }

    @SubscribeEvent
    public static void alArmarPantalla(ScreenEvent.Init.Post evento) {
        Screen pantalla = evento.getScreen();
        if (!(pantalla instanceof OptionsScreen)) {
            return;
        }

        Button boton = Button.builder(
                Component.translatable("jobsmenu.ajustes.boton"),
                (b) -> {
                    Minecraft cliente = Minecraft.getInstance();
                    cliente.setScreen(new PantallaAjustesAviso(pantalla, cliente.options));
                })
                .bounds(6, 6, 120, 20)
                .build();
        evento.addListener(boton);
    }
}

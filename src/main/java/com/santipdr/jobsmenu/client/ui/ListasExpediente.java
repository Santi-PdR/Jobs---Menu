package com.santipdr.jobsmenu.client.ui;

import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.screens.Screen;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

/** Reflection defensiva para integrar listas vanilla sin reimplementarlas. */
public final class ListasExpediente {

    private ListasExpediente() {
    }

    /** Solo retira dirt/gradientes. */
    public static void estilizar(Screen pantalla) {
        estilizar(pantalla, -1, -1);
    }

    /**
     * Retira dirt/gradientes y, si se pasa un rango valido, reserva una banda
     * superior para cabecera Jobs y otra inferior para navegacion/pie.
     *
     * El ancho de la lista se conserva deliberadamente: varias listas vanilla
     * calculan internamente columnas, botones o hitboxes a partir del ancho
     * original. Solo cambiamos y0/y1, que es el ajuste seguro usado por las
     * propias pantallas de Minecraft al redimensionarse.
     */
    public static void estilizar(Screen pantalla, int arriba, int abajo) {
        if (pantalla == null) return;
        Set<Object> vistos = Collections.newSetFromMap(new IdentityHashMap<>());
        Class<?> tipo = pantalla.getClass();
        while (tipo != null && tipo != Object.class) {
            for (Field f : tipo.getDeclaredFields()) {
                try {
                    if (!AbstractSelectionList.class.isAssignableFrom(f.getType())) continue;
                    f.setAccessible(true);
                    Object o = f.get(pantalla);
                    if (o instanceof AbstractSelectionList<?> lista && vistos.add(lista)) {
                        lista.setRenderBackground(false);
                        lista.setRenderTopAndBottom(false);
                        if (arriba >= 0 && abajo > arriba + 20) {
                            lista.updateSize(pantalla.width, pantalla.height, arriba, abajo);
                        }
                    }
                } catch (Throwable ignored) {
                    // Si otro mod cambia un campo, la pantalla conserva su lista vanilla.
                }
            }
            tipo = tipo.getSuperclass();
        }
    }
}

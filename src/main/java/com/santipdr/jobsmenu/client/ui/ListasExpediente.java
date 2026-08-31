package com.santipdr.jobsmenu.client.ui;

import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.screens.Screen;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

/** Reflection defensiva para quitar el fondo dirt de listas vanilla sin reimplementarlas. */
public final class ListasExpediente {

    private ListasExpediente() {
    }

    public static void estilizar(Screen pantalla) {
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
                    }
                } catch (Throwable ignored) {
                    // Si otro mod cambia un campo, la pantalla conserva su lista vanilla.
                }
            }
            tipo = tipo.getSuperclass();
        }
    }
}

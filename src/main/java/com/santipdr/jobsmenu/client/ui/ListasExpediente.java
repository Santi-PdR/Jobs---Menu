package com.santipdr.jobsmenu.client.ui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

/** Integracion defensiva de listas vanilla dentro del lenguaje visual Jobs. */
public final class ListasExpediente {

    private static final Field CAMPO_Y0;
    private static final Method METODO_SCROLLBAR_X;

    static {
        Field y0 = null;
        Method barra = null;
        try {
            // SRG: ObfuscationReflectionHelper remapea tanto en dev como en jar.
            y0 = ObfuscationReflectionHelper.findField(AbstractSelectionList.class, "f_93390_");
            barra = ObfuscationReflectionHelper.findMethod(AbstractSelectionList.class, "m_5756_");
        } catch (Throwable ignored) {
            // El fallback es simplemente conservar la scrollbar vanilla.
        }
        CAMPO_Y0 = y0;
        METODO_SCROLLBAR_X = barra;
    }

    private ListasExpediente() {
    }

    /** Solo retira dirt/gradientes. */
    public static void estilizar(Screen pantalla) {
        estilizar(pantalla, -1, -1);
    }

    /**
     * Retira dirt/gradientes y, si se pasa un rango valido, reserva una banda
     * superior para cabecera Jobs y otra inferior para navegacion/pie.
     */
    public static void estilizar(Screen pantalla, int arriba, int abajo) {
        for (AbstractSelectionList<?> lista : encontrarListas(pantalla)) {
            try {
                lista.setRenderBackground(false);
                lista.setRenderTopAndBottom(false);
                if (arriba >= 0 && abajo > arriba + 20) {
                    lista.updateSize(pantalla.width, pantalla.height, arriba, abajo);
                }
            } catch (Throwable ignored) {
                // Si otro mod cambia la lista, conserva su comportamiento original.
            }
        }
    }

    /**
     * Cubre la barra gris vanilla despues de su render y dibuja una barra Jobs
     * en exactamente el mismo hitbox. El scroll, rueda y drag siguen siendo los
     * de Minecraft: solo cambia la presentacion.
     */
    public static void renderarBarras(Screen pantalla, GuiGraphics g) {
        if (pantalla == null || g == null || CAMPO_Y0 == null || METODO_SCROLLBAR_X == null) {
            return;
        }

        for (AbstractSelectionList<?> lista : encontrarListas(pantalla)) {
            try {
                int maxScroll = lista.getMaxScroll();
                if (maxScroll <= 0) {
                    continue;
                }

                int x = (int) METODO_SCROLLBAR_X.invoke(lista);
                int y0 = CAMPO_Y0.getInt(lista);
                int y1 = lista.getScrollBottom();
                int alto = y1 - y0;
                if (alto < 24) {
                    continue;
                }

                int thumbH = (int) Math.round((double) alto * alto / (alto + maxScroll));
                thumbH = Math.max(24, Math.min(alto - 8, thumbH));
                int recorrido = Math.max(1, alto - thumbH);
                double proporcion = Math.max(0.0D, Math.min(1.0D,
                        lista.getScrollAmount() / Math.max(1.0D, maxScroll)));
                int thumbY = y0 + (int) Math.round(recorrido * proporcion);

                // Borrado opaco: la barra gris de Minecraft no queda visible debajo.
                g.fill(x - 2, y0, x + 8, y1, Paleta.PAPEL);

                int tintaSuave = Paleta.conAlfa(Paleta.tintaSecundaria(), 0.28F);
                int tinta = Paleta.conAlfa(Paleta.tintaPrincipal(), 0.78F);
                int papelActivo = Paleta.conAlfa(Paleta.FLUOR, 0.86F);

                // Carril fino de archivador.
                g.fill(x + 2, y0 + 3, x + 4, y1 - 3, tintaSuave);
                g.fill(x + 1, y0 + 3, x + 5, y0 + 4, tintaSuave);
                g.fill(x + 1, y1 - 4, x + 5, y1 - 3, tintaSuave);

                // Tirador de papel/tinta. No usa rojo: sigue reservado a Executores.
                g.fill(x, thumbY, x + 6, thumbY + thumbH, tinta);
                g.fill(x + 1, thumbY + 1, x + 5, thumbY + thumbH - 1, papelActivo);
                int marcaY = thumbY + thumbH / 2;
                g.fill(x + 1, marcaY, x + 5, Math.min(thumbY + thumbH - 1, marcaY + 1), tintaSuave);
            } catch (Throwable ignored) {
                // Fallo visual no debe impedir usar una pantalla de opciones.
            }
        }
    }

    private static List<AbstractSelectionList<?>> encontrarListas(Screen pantalla) {
        List<AbstractSelectionList<?>> resultado = new ArrayList<>();
        if (pantalla == null) {
            return resultado;
        }

        Set<Object> vistos = Collections.newSetFromMap(new IdentityHashMap<>());
        Class<?> tipo = pantalla.getClass();
        while (tipo != null && tipo != Object.class) {
            for (Field f : tipo.getDeclaredFields()) {
                try {
                    if (!AbstractSelectionList.class.isAssignableFrom(f.getType())) {
                        continue;
                    }
                    f.setAccessible(true);
                    Object o = f.get(pantalla);
                    if (o instanceof AbstractSelectionList<?> lista && vistos.add(lista)) {
                        resultado.add(lista);
                    }
                } catch (Throwable ignored) {
                    // Una lista inaccesible se deja intacta.
                }
            }
            tipo = tipo.getSuperclass();
        }
        return resultado;
    }
}

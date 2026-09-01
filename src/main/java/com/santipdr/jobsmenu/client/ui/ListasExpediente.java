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
            y0 = ObfuscationReflectionHelper.findField(AbstractSelectionList.class, "f_93390_");
            barra = ObfuscationReflectionHelper.findMethod(AbstractSelectionList.class, "m_5756_");
        } catch (Throwable ignored) {
            // En mappings/implementaciones distintas usamos coordenadas publicas.
        }
        CAMPO_Y0 = y0;
        METODO_SCROLLBAR_X = barra;
    }

    private ListasExpediente() {
    }

    public static void estilizar(Screen pantalla) {
        estilizar(pantalla, -1, -1);
    }

    /** Retira dirt/gradientes y reserva bandas de cabecera/pie cuando es seguro. */
    public static void estilizar(Screen pantalla, int arriba, int abajo) {
        for (AbstractSelectionList<?> lista : encontrarListas(pantalla)) {
            try {
                lista.setRenderBackground(false);
                lista.setRenderTopAndBottom(false);
                if (arriba >= 0 && abajo > arriba + 20) {
                    lista.updateSize(pantalla.width, pantalla.height, arriba, abajo);
                }
            } catch (Throwable ignored) {
                // Otro mod puede cambiar la implementacion; no se fuerza.
            }
        }
    }

    /**
     * Cubre la barra gris vanilla despues de su render y dibuja una barra Jobs
     * en el mismo hitbox. Rueda, click y drag siguen siendo de Minecraft.
     */
    public static void renderarBarras(Screen pantalla, GuiGraphics g) {
        if (pantalla == null || g == null) return;

        for (AbstractSelectionList<?> lista : encontrarListas(pantalla)) {
            try {
                int maxScroll = lista.getMaxScroll();
                if (maxScroll <= 0) continue;

                int x = resolverScrollbarX(lista);
                int y0 = resolverY0(lista);
                int y1 = lista.getScrollBottom();
                int alto = y1 - y0;
                if (alto < 24) continue;

                int thumbH = (int) Math.round((double) alto * alto / (alto + maxScroll));
                thumbH = Math.max(24, Math.min(alto - 8, thumbH));
                int recorrido = Math.max(1, alto - thumbH);
                double proporcion = Math.max(0.0D, Math.min(1.0D,
                        lista.getScrollAmount() / Math.max(1.0D, maxScroll)));
                int thumbY = y0 + (int) Math.round(recorrido * proporcion);

                int papel = Paleta.papelAviso();
                int tintaSuave = Paleta.conAlfa(Paleta.tintaSecundaria(), 0.28F);
                int tintaFina = Paleta.conAlfa(Paleta.tintaSecundaria(), 0.16F);
                int tinta = Paleta.conAlfa(Paleta.tintaPrincipal(), 0.76F);
                int papelActivo = Paleta.mezclar(papel, Paleta.FLUOR, 0.12F);

                // Limpia la barra vanilla y crea una canaleta de archivador.
                g.fill(x - 3, y0, x + 9, y1, papel);
                g.fill(x - 1, y0 + 1, x + 7, y1 - 1,
                        Paleta.conAlfa(Paleta.VANO, 0.05F));
                g.fill(x + 2, y0 + 5, x + 4, y1 - 5, tintaSuave);

                // Marcas de recorrido: ayudan a leer posicion sin copiar un scrollbar web.
                for (int i = 0; i <= 4; i++) {
                    int my = y0 + 5 + Math.round((alto - 10) * (i / 4.0F));
                    g.fill(x, my, x + 2, my + 1, tintaFina);
                    g.fill(x + 4, my, x + 6, my + 1, tintaFina);
                }

                // Topes mecanicos arriba y abajo.
                g.fill(x, y0 + 2, x + 6, y0 + 3, tintaSuave);
                g.fill(x + 1, y0 + 3, x + 5, y0 + 4, tintaFina);
                g.fill(x, y1 - 3, x + 6, y1 - 2, tintaSuave);
                g.fill(x + 1, y1 - 4, x + 5, y1 - 3, tintaFina);

                // Tirador: papel claro dentro de marco de tinta.
                g.fill(x - 1, thumbY - 1, x + 7, thumbY + thumbH + 1,
                        Paleta.conAlfa(Paleta.VANO, 0.16F));
                g.fill(x, thumbY, x + 6, thumbY + thumbH, tinta);
                g.fill(x + 1, thumbY + 1, x + 5, thumbY + thumbH - 1, papelActivo);

                int centro = thumbY + thumbH / 2;
                for (int d = -2; d <= 2; d += 2) {
                    int gy = centro + d;
                    if (gy > thumbY + 2 && gy < thumbY + thumbH - 2) {
                        g.fill(x + 1, gy, x + 5, gy + 1, tintaSuave);
                    }
                }
            } catch (Throwable ignored) {
                // Fallo visual no debe impedir usar una pantalla de opciones.
            }
        }
    }

    private static int resolverScrollbarX(AbstractSelectionList<?> lista) {
        if (METODO_SCROLLBAR_X != null) {
            try {
                return (int) METODO_SCROLLBAR_X.invoke(lista);
            } catch (Throwable ignored) {
            }
        }
        // AbstractWidget expone la caja real. Este fallback coincide con la
        // posicion vanilla y, sobre todo, evita dejar una columna negra gigante.
        return lista.getX() + lista.getWidth() - 6;
    }

    private static int resolverY0(AbstractSelectionList<?> lista) {
        if (CAMPO_Y0 != null) {
            try {
                return CAMPO_Y0.getInt(lista);
            } catch (Throwable ignored) {
            }
        }
        return lista.getY();
    }

    private static List<AbstractSelectionList<?>> encontrarListas(Screen pantalla) {
        List<AbstractSelectionList<?>> resultado = new ArrayList<>();
        if (pantalla == null) return resultado;

        Set<Object> vistos = Collections.newSetFromMap(new IdentityHashMap<>());
        Class<?> tipo = pantalla.getClass();
        while (tipo != null && tipo != Object.class) {
            for (Field f : tipo.getDeclaredFields()) {
                try {
                    if (!AbstractSelectionList.class.isAssignableFrom(f.getType())) continue;
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

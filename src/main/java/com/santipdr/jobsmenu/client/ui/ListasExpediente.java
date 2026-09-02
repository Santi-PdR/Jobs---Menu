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
    private static final Field CAMPO_Y1;
    private static final Method METODO_SCROLLBAR_X;

    static {
        Field y0 = null;
        Field y1 = null;
        Method barra = null;
        try {
            y0 = ObfuscationReflectionHelper.findField(AbstractSelectionList.class, "f_93390_");
            y1 = ObfuscationReflectionHelper.findField(AbstractSelectionList.class, "f_93391_");
            barra = ObfuscationReflectionHelper.findMethod(AbstractSelectionList.class, "m_5756_");
        } catch (Throwable ignored) {
        }
        CAMPO_Y0 = y0;
        CAMPO_Y1 = y1;
        METODO_SCROLLBAR_X = barra;
    }

    private ListasExpediente() {
    }

    public static void estilizar(Screen pantalla) {
        estilizar(pantalla, -1, -1);
    }

    public static void estilizar(Screen pantalla, int arriba, int abajo) {
        for (AbstractSelectionList<?> lista : encontrarListas(pantalla)) {
            try {
                lista.setRenderBackground(false);
                lista.setRenderTopAndBottom(false);
                if (arriba >= 0 && abajo > arriba + 20) {
                    lista.updateSize(pantalla.width, pantalla.height, arriba, abajo);
                }
            } catch (Throwable ignored) {
            }
        }
    }

    public static void renderarBarras(Screen pantalla, GuiGraphics g) {
        if (pantalla == null || g == null) return;

        for (AbstractSelectionList<?> lista : encontrarListas(pantalla)) {
            try {
                int maxScroll = lista.getMaxScroll();
                if (maxScroll <= 0) continue;

                int x = resolverScrollbarX(lista);
                int y0 = resolverY0(lista);
                int y1 = resolverY1(lista);
                int alto = y1 - y0;
                if (alto < 24) continue;

                int thumbH = (int) Math.round((double) alto * alto / (alto + maxScroll));
                thumbH = Math.max(24, Math.min(alto - 8, thumbH));
                int recorrido = Math.max(1, alto - thumbH);
                double proporcion = Math.max(0.0D, Math.min(1.0D,
                        lista.getScrollAmount() / Math.max(1.0D, maxScroll)));
                int thumbY = y0 + (int) Math.round(recorrido * proporcion);

                int papel = Paleta.papelAviso();
                int tintaSuave = Paleta.conAlfa(Paleta.tintaSecundaria(), 0.30F);
                int tintaFina = Paleta.conAlfa(Paleta.tintaSecundaria(), 0.16F);
                int tinta = Paleta.conAlfa(Paleta.tintaPrincipal(), 0.78F);
                int papelActivo = Paleta.mezclar(papel, Paleta.UI_ACENTO, 0.11F);

                g.fill(x - 4, y0, x + 10, y1, papel);
                g.fill(x - 4, y0, x - 2, y1, Paleta.conAlfa(Paleta.VANO, 0.08F));
                g.fill(x + 8, y0, x + 10, y1, Paleta.conAlfa(Paleta.VANO, 0.12F));
                g.fill(x - 1, y0 + 1, x + 7, y1 - 1,
                        Paleta.conAlfa(Paleta.VANO, 0.05F));
                g.fill(x + 2, y0 + 5, x + 4, y1 - 5, tintaSuave);
                g.fill(x + 3, y0 + 6, x + 4, y1 - 6,
                        Paleta.conAlfa(Paleta.UI_ACENTO, 0.08F));

                for (int i = 0; i <= 8; i++) {
                    int my = y0 + 5 + Math.round((alto - 10) * (i / 8.0F));
                    boolean mayor = i == 0 || i == 4 || i == 8;
                    int largo = mayor ? 3 : 2;
                    g.fill(x - 1, my, x - 1 + largo, my + 1,
                            Paleta.conAlfa(Paleta.tintaSecundaria(), mayor ? 0.22F : 0.12F));
                    g.fill(x + 5, my, x + 5 + largo, my + 1,
                            Paleta.conAlfa(Paleta.tintaSecundaria(), mayor ? 0.22F : 0.12F));
                }

                g.fill(x - 1, y0 + 2, x + 7, y0 + 3, tintaSuave);
                g.fill(x, y0 + 3, x + 6, y0 + 4, tintaFina);
                g.fill(x - 1, y1 - 3, x + 7, y1 - 2, tintaSuave);
                g.fill(x, y1 - 4, x + 6, y1 - 3, tintaFina);

                int marcador = y0 + Math.round((alto - 2) * (float) proporcion);
                g.fill(x - 3, marcador, x - 1, marcador + 2,
                        Paleta.conAlfa(Paleta.UI_ACENTO_FUERTE, 0.34F));

                g.fill(x - 2, thumbY - 2, x + 8, thumbY + thumbH + 2,
                        Paleta.conAlfa(Paleta.VANO, 0.12F));
                g.fill(x - 1, thumbY - 1, x + 7, thumbY + thumbH + 1,
                        Paleta.conAlfa(Paleta.VANO, 0.18F));
                g.fill(x, thumbY, x + 6, thumbY + thumbH, tinta);
                g.fill(x + 1, thumbY + 1, x + 5, thumbY + thumbH - 1, papelActivo);
                g.fill(x + 1, thumbY + 1, x + 5, thumbY + 2,
                        Paleta.conAlfa(Paleta.UI_PAPEL_FOCO, 0.30F));
                g.fill(x + 1, thumbY + thumbH - 2, x + 5, thumbY + thumbH - 1,
                        Paleta.conAlfa(Paleta.VANO, 0.10F));

                int centro = thumbY + thumbH / 2;
                for (int d = -3; d <= 3; d += 2) {
                    int gy = centro + d;
                    if (gy > thumbY + 2 && gy < thumbY + thumbH - 2) {
                        g.fill(x + 1, gy, x + 5, gy + 1,
                                Paleta.conAlfa(Paleta.tintaSecundaria(), 0.30F));
                    }
                }
                g.fill(x + 2, centro - 1, x + 4, centro + 1,
                        Paleta.conAlfa(Paleta.UI_ACENTO, 0.18F));
            } catch (Throwable ignored) {
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
        return lista.getRowRight() + 4;
    }

    private static int resolverY0(AbstractSelectionList<?> lista) {
        if (CAMPO_Y0 != null) {
            try {
                return CAMPO_Y0.getInt(lista);
            } catch (Throwable ignored) {
            }
        }
        return 0;
    }

    private static int resolverY1(AbstractSelectionList<?> lista) {
        if (CAMPO_Y1 != null) {
            try {
                return CAMPO_Y1.getInt(lista);
            } catch (Throwable ignored) {
            }
        }
        return resolverY0(lista);
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
                }
            }
            tipo = tipo.getSuperclass();
        }
        return resultado;
    }
}

package com.santipdr.jobsmenu.client.ui;

import com.santipdr.jobsmenu.client.SesionMenu;
import com.santipdr.jobsmenu.config.ConfigTurno;
import com.santipdr.jobsmenu.config.PerfilesJobs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Locale;

/**
 * Instrumentacion compartida de Jobs. Todo es posterior al contenido: no
 * cambia hitboxes, callbacks, scroll, listas ni el background de la pantalla.
 */
public final class CapaProfesionalJobs {

    private static final Deque<String> RUTA = new ArrayDeque<>();
    private static Screen ultimaRuta;
    private static long visitaRuta = -1L;

    private CapaProfesionalJobs() {
    }

    public static void dibujar(Screen pantalla, GuiGraphics g, int mouseX, int mouseY, long ahora) {
        if (pantalla == null || g == null || pantalla.width < 140 || pantalla.height < 90) return;

        SesionMenu.registrarPantalla(pantalla);
        int w = pantalla.width;
        int h = pantalla.height;
        int m = Math.max(6, Math.min(12, Math.min(w, h) / 28));
        Font font = Minecraft.getInstance().font;
        String codigo = codigoPantalla(pantalla);
        boolean minima = ConfigTurno.interfazMinima();
        boolean reducida = ConfigTurno.movimientoReducido() || ConfigTurno.bajoConsumo();
        float contraste = ConfigTurno.altoContraste() ? 1.30F : 1.0F;

        List<AbstractWidget> widgets = new ArrayList<>();
        List<AbstractWidget> activos = new ArrayList<>();
        AbstractWidget enfocado = null;
        AbstractWidget hover = null;
        for (var child : pantalla.children()) {
            if (child instanceof AbstractWidget widget && widget.visible) {
                widgets.add(widget);
                if (widget.active) {
                    activos.add(widget);
                    if (widget.isFocused()) enfocado = widget;
                    if (widget.isMouseOver(mouseX, mouseY)) hover = widget;
                }
            }
        }

        actualizarRuta(pantalla, codigo);
        int tenue = Paleta.conAlfa(Paleta.UI_TINTA_TENUE, limitar(0.10F * contraste));
        int acento = Paleta.conAlfa(Paleta.UI_ACENTO, limitar(0.16F * contraste));

        cabecera(g, font, pantalla, codigo, widgets.size(), activos.size(), m, contraste);
        if (!minima) {
            ruta(g, font, w, m, contraste);
            sesion(g, font, w, m, contraste);
        }

        AbstractWidget objetivo = enfocado != null ? enfocado : hover;
        boolean teclado = enfocado != null;
        if (objetivo != null) {
            foco(g, objetivo, teclado, ahora, reducida, contraste);
        }

        if (!minima && w >= 390 && h >= 170) {
            barraContextual(g, font, pantalla, codigo, activos, objetivo, teclado,
                    m, contraste);
        } else if (w >= 260 && h >= 130) {
            railCompacto(g, font, codigo, m, h, contraste);
        }

        // Registros laterales no compiten con widgets y dan continuidad a
        // pantallas que conservan layouts vanilla/Forge muy distintos.
        if (h >= 170) {
            int cy = h / 2;
            g.fill(m - 2, cy - 12, m - 1, cy + 13, tenue);
            g.fill(m - 4, cy, m + 2, cy + 1, acento);
            g.fill(w - m + 1, cy - 8, w - m + 2, cy + 9,
                    Paleta.conAlfa(Paleta.UI_TINTA_TENUE, 0.06F));
            g.fill(w - m - 2, cy + 2, w - m + 4, cy + 3,
                    Paleta.conAlfa(Paleta.UI_ACENTO, 0.08F));
        }

        actividadSuperior(g, w, m, ahora, reducida, minima);
        secretos(g, font, w, h, m);
    }

    private static void cabecera(GuiGraphics g, Font font, Screen pantalla, String codigo,
                                 int visibles, int activos, int m, float contraste) {
        int w = pantalla.width;
        if (w < 260) return;

        String cabecera = "JOBS // " + codigo;
        g.drawString(font, cabecera, m + 5, m + 5,
                Paleta.conAlfa(Paleta.UI_TINTA_TENUE, limitar(0.44F * contraste)), false);
        int cw = font.width(cabecera);
        g.fill(m + 5, m + 16, m + 5 + Math.max(18, cw / 2), m + 17,
                Paleta.conAlfa(Paleta.UI_ACENTO, limitar(0.18F * contraste)));
        g.fill(m + 5 + Math.max(18, cw / 2) + 3, m + 16,
                Math.min(w / 2, m + 5 + cw + 38), m + 17,
                Paleta.conAlfa(Paleta.UI_TINTA_TENUE, limitar(0.09F * contraste)));

        // Titulo real: ayuda a distinguir pantallas que comparten el mismo
        // codigo visual sin duplicar el encabezado principal del layout.
        if (w >= 470) {
            String titulo = pantalla.getTitle() == null ? "" : pantalla.getTitle().getString();
            titulo = ChromeExpediente.ajustar(font, titulo, Math.min(180, w / 4));
            if (!titulo.isBlank()) {
                g.drawString(font, titulo, m + 5, m + 22,
                        Paleta.conAlfa(Paleta.UI_TINTA_TENUE,
                                limitar(0.28F * contraste)), false);
            }
        }

        if (w >= 330) {
            String contador = String.format(Locale.ROOT, "%02d/%02d", activos, visibles);
            int tw = font.width(contador);
            int x = w - m - tw - 7;
            g.drawString(font, contador, x, m + 5,
                    Paleta.conAlfa(Paleta.UI_TINTA_TENUE, limitar(0.40F * contraste)), false);
            g.fill(x - 12, m + 7, x - 5, m + 8,
                    Paleta.conAlfa(Paleta.UI_ACENTO, limitar(0.18F * contraste)));
            g.fill(x - 8, m + 4, x - 7, m + 11,
                    Paleta.conAlfa(Paleta.UI_TINTA_TENUE, 0.10F));

            int barW = Math.min(48, Math.max(22, w / 14));
            int bx = w - m - barW - 5;
            int by = m + 17;
            g.fill(bx, by, bx + barW, by + 1,
                    Paleta.conAlfa(Paleta.UI_TINTA_TENUE, 0.07F));
            float ratio = visibles <= 0 ? 0.0F : Math.min(1.0F, activos / (float) visibles);
            g.fill(bx, by, bx + Math.max(1, Math.round(barW * ratio)), by + 1,
                    Paleta.conAlfa(Paleta.UI_ACENTO, limitar(0.20F * contraste)));
        }
    }

    private static void sesion(GuiGraphics g, Font font, int w, int m, float contraste) {
        if (w < 430) return;
        long ms = SesionMenu.duracionVisitaMs();
        long totalSeg = ms / 1000L;
        String tiempo = String.format(Locale.ROOT, "T+%02d:%02d",
                (totalSeg / 60L) % 100L, totalSeg % 60L);
        String pantallas = String.format(Locale.ROOT, "S%02d", Math.min(99, SesionMenu.pantallasVisitadas()));
        int volumen = ConfigTurno.volumenAvisoPorcentaje();
        String audio = volumen <= 0 ? "MUTE" : String.format(Locale.ROOT, "V%03d", volumen);
        String texto = tiempo + "  " + pantallas + "  " + audio;
        int tw = font.width(texto);
        int x = w - m - tw - 7;
        int y = m + 23;
        g.fill(x - 5, y - 2, x + tw + 5, y + 10,
                Paleta.conAlfa(Paleta.VANO, 0.10F));
        g.fill(x - 5, y + 9, x + tw + 5, y + 10,
                Paleta.conAlfa(volumen <= 0 ? Paleta.UI_TINTA_TENUE : Paleta.UI_ACENTO,
                        limitar((volumen <= 0 ? 0.11F : 0.17F) * contraste)));
        g.drawString(font, texto, x, y,
                Paleta.conAlfa(Paleta.UI_TINTA_TENUE, limitar(0.43F * contraste)), false);
    }

    private static void actualizarRuta(Screen pantalla, String codigo) {
        long visita = SesionMenu.numeroVisita();
        if (visitaRuta != visita) {
            visitaRuta = visita;
            RUTA.clear();
            ultimaRuta = null;
        }
        if (pantalla == ultimaRuta) return;
        ultimaRuta = pantalla;
        if (RUTA.isEmpty() || !codigo.equals(RUTA.peekLast())) {
            RUTA.addLast(codigo);
            while (RUTA.size() > 3) RUTA.removeFirst();
        }
    }

    private static void ruta(GuiGraphics g, Font font, int w, int m, float contraste) {
        if (w < 590 || RUTA.size() < 2) return;
        String texto = String.join(" > ", RUTA);
        int max = Math.min(220, w / 3);
        texto = ChromeExpediente.ajustar(font, texto, max);
        int tw = font.width(texto);
        int x = (w - tw) / 2;
        int y = m + 5;
        g.drawString(font, texto, x, y,
                Paleta.conAlfa(Paleta.UI_TINTA_TENUE, limitar(0.28F * contraste)), false);
        g.fill(x, y + 11, x + tw, y + 12,
                Paleta.conAlfa(Paleta.UI_ACENTO, limitar(0.07F * contraste)));
        int cx = x + tw / 2;
        g.fill(cx, y + 10, cx + 1, y + 13,
                Paleta.conAlfa(Paleta.UI_ACENTO_FUERTE, 0.08F));
    }

    private static void barraContextual(GuiGraphics g, Font font, Screen pantalla, String codigo,
                                        List<AbstractWidget> activos, AbstractWidget objetivo,
                                        boolean teclado, int m, float contraste) {
        int w = pantalla.width;
        int h = pantalla.height;
        int y = h - m - 15;
        int x0 = m + 5;
        int x1 = w - m - 5;

        g.fill(x0, y - 2, x1, y + 12, Paleta.conAlfa(Paleta.VANO, 0.075F));
        g.fill(x0, y - 2, x1, y - 1,
                Paleta.conAlfa(Paleta.UI_TINTA_TENUE, 0.065F));
        g.fill(x0, y + 11, x1, y + 12,
                Paleta.conAlfa(Paleta.UI_ACENTO, limitar(0.10F * contraste)));

        int x = x0 + 4;
        for (String tecla : atajos(codigo)) {
            int usado = tecla(g, font, x, y, tecla, contraste);
            x += usado + 4;
            if (x > w / 2 - 40) break;
        }

        if (objetivo != null && w >= 520) {
            int indice = activos.indexOf(objetivo);
            String pos = indice >= 0
                    ? String.format(Locale.ROOT, "%02d/%02d", indice + 1, activos.size())
                    : String.format(Locale.ROOT, "--/%02d", activos.size());
            String modo = teclado ? "KEY" : "PTR";
            String tipo = tipoWidget(objetivo);
            String meta = modo + "  " + tipo + "  " + pos;
            int mw = font.width(meta);
            int mx = w - m - mw - 10;
            g.drawString(font, meta, mx, y + 1,
                    Paleta.conAlfa(Paleta.UI_TINTA_TENUE,
                            limitar((teclado ? 0.56F : 0.38F) * contraste)), false);
            g.fill(mx - 8, y + 4, mx - 3, y + 5,
                    Paleta.conAlfa(teclado ? Paleta.UI_ACENTO_FUERTE : Paleta.UI_ACENTO,
                            limitar((teclado ? 0.32F : 0.15F) * contraste)));

            String nombre = objetivo.getMessage() == null ? "" : objetivo.getMessage().getString();
            if (!nombre.isBlank()) {
                int izquierda = Math.max(x + 12, w / 2 - 105);
                int derecha = Math.min(mx - 14, w / 2 + 105);
                if (derecha - izquierda > 50) {
                    nombre = ChromeExpediente.ajustar(font, nombre, derecha - izquierda);
                    int nw = font.width(nombre);
                    int nx = (izquierda + derecha - nw) / 2;
                    g.drawString(font, nombre, nx, y + 1,
                            Paleta.conAlfa(Paleta.UI_TINTA_TENUE,
                                    limitar(0.50F * contraste)), false);
                    g.fill(nx, y + 10, nx + nw, y + 11,
                            Paleta.conAlfa(Paleta.UI_ACENTO,
                                    limitar(0.11F * contraste)));
                }
            }
        } else if (w >= 500) {
            PerfilesJobs.Perfil perfil = PerfilesJobs.actual();
            String perfilTxt = perfil == null ? "PROFILE // CUSTOM" : codigoPerfil(perfil);
            int pw = font.width(perfilTxt);
            g.drawString(font, perfilTxt, x1 - pw - 4, y + 1,
                    Paleta.conAlfa(Paleta.UI_TINTA_TENUE,
                            limitar(0.34F * contraste)), false);
        }
    }

    private static void railCompacto(GuiGraphics g, Font font, String codigo,
                                     int m, int h, float contraste) {
        int x = m + 5;
        int y = h - m - 11;
        String[] atajos = atajos(codigo);
        for (int i = 0; i < Math.min(3, atajos.length); i++) {
            x += tecla(g, font, x, y, atajos[i], contraste) + 4;
        }
    }

    private static int tecla(GuiGraphics g, Font font, int x, int y,
                             String texto, float contraste) {
        int tw = font.width(texto);
        int w = tw + 8;
        g.fill(x, y, x + w, y + 10, Paleta.conAlfa(Paleta.VANO, 0.11F));
        g.fill(x, y + 9, x + w, y + 10,
                Paleta.conAlfa(Paleta.UI_TINTA_TENUE, limitar(0.13F * contraste)));
        g.fill(x + 2, y + 2, x + 3, y + 7,
                Paleta.conAlfa(Paleta.UI_ACENTO, limitar(0.10F * contraste)));
        g.drawString(font, texto, x + 4, y + 1,
                Paleta.conAlfa(Paleta.UI_TINTA_TENUE, limitar(0.46F * contraste)), false);
        return w;
    }

    private static String[] atajos(String codigo) {
        return switch (codigo) {
            case "MAIN" -> new String[]{"1-4", "F", "M", "N", "TAB", "ENTER"};
            case "PAUSE" -> new String[]{"ESC", "M", "TAB", "ENTER"};
            case "WORLDS", "MODS", "LANG" -> new String[]{"CTRL+F", "ESC", "TAB", "ENTER"};
            case "MULTI" -> new String[]{"F5", "ESC", "TAB", "ENTER"};
            case "CONFIG" -> new String[]{"F1-F5", "TAB", "ENTER", "ESC"};
            default -> new String[]{"TAB", "ENTER", "ESC"};
        };
    }

    private static String tipoWidget(AbstractWidget widget) {
        if (widget instanceof ToggleExpediente) return "TOG";
        if (widget instanceof SliderExpediente || widget instanceof AbstractSliderButton) return "SLD";
        if (widget instanceof EditBox) return "TXT";
        if (widget instanceof RenglonTablon) return "ROW";
        if (widget instanceof BotonExpediente) return "BTN";
        if (widget instanceof AbstractButton) return "BTN";
        return "CTL";
    }

    private static void foco(GuiGraphics g, AbstractWidget widget, boolean teclado,
                             long ahora, boolean fijo, float contraste) {
        int x = widget.getX();
        int y = widget.getY();
        int w = widget.getWidth();
        int h = widget.getHeight();
        if (w < 4 || h < 4) return;

        float pulso = 1.0F;
        if (teclado && !fijo) {
            pulso = 0.86F + 0.14F * (float) ((Math.sin(ahora / 285.0D) + 1.0D) * 0.5D);
        }
        int base = teclado ? Paleta.UI_ACENTO_FUERTE : Paleta.UI_ACENTO;
        float alfa = (teclado ? 0.54F : 0.21F) * pulso * contraste;
        int c = Paleta.conAlfa(base, limitar(alfa));
        int l = Math.min(13, Math.max(5, h / 2));

        g.fill(x - 3, y - 3, x + l, y - 2, c);
        g.fill(x - 3, y - 3, x - 2, y + l, c);
        g.fill(x + w - l, y + h + 2, x + w + 3, y + h + 3, c);
        g.fill(x + w + 2, y + h - l, x + w + 3, y + h + 3, c);

        int cy = y + h / 2;
        if (teclado) {
            g.fill(x - 5, cy - 2, x - 3, cy + 3,
                    Paleta.conAlfa(base, limitar(0.48F * pulso * contraste)));
            g.fill(x + w + 3, cy - 1, x + w + 5, cy + 2,
                    Paleta.conAlfa(base, limitar(0.28F * pulso * contraste)));
            if (w >= 40) {
                int cx = x + w / 2;
                g.fill(cx - 7, y - 2, cx + 8, y - 1,
                        Paleta.conAlfa(base, limitar(0.28F * pulso * contraste)));
                g.fill(cx, y - 4, cx + 1, y - 1,
                        Paleta.conAlfa(base, limitar(0.20F * pulso * contraste)));
            }
        } else {
            g.fill(x + 4, y + h + 2, x + Math.min(w - 4, 18), y + h + 3,
                    Paleta.conAlfa(base, limitar(0.16F * contraste)));
        }
    }

    private static void actividadSuperior(GuiGraphics g, int w, int m, long ahora,
                                          boolean reducida, boolean minima) {
        if (minima || w < 360) return;
        int x0 = m + 105;
        int x1 = w - m - 105;
        if (x1 <= x0 + 20) return;
        if (reducida) {
            int cx = (x0 + x1) / 2;
            g.fill(cx - 14, m + 3, cx + 15, m + 4,
                    Paleta.conAlfa(Paleta.UI_ACENTO, 0.05F));
            g.fill(cx, m + 1, cx + 1, m + 6,
                    Paleta.conAlfa(Paleta.UI_TINTA_TENUE, 0.05F));
            return;
        }

        float ciclo = (ahora % 9800L) / 9800.0F;
        int largo = Math.max(12, Math.min(42, (x1 - x0) / 7));
        int px = x0 + Math.round((x1 - x0 - largo) * ciclo);
        g.fill(px, m + 3, px + largo, m + 4,
                Paleta.conAlfa(Paleta.UI_ACENTO, 0.042F));
        g.fill(px + largo / 2, m + 1, px + largo / 2 + 1, m + 5,
                Paleta.conAlfa(Paleta.UI_ACENTO_FUERTE, 0.044F));
    }

    private static void secretos(GuiGraphics g, Font font, int w, int h, int m) {
        if (SecretosJobs.expedienteRaro() && w >= 320 && h >= 180) {
            String secreto = SecretosJobs.codigoExpediente();
            int sw = font.width(secreto);
            int sx = w - m - sw - 5;
            int sy = h / 2 + 54;
            g.fill(sx - 6, sy - 3, sx + sw + 5, sy + 10,
                    Paleta.conAlfa(Paleta.VANO, 0.12F));
            g.fill(sx - 6, sy - 3, sx - 5, sy + 10,
                    Paleta.conAlfa(Paleta.UI_ACENTO, 0.12F));
            g.drawString(font, secreto, sx, sy,
                    Paleta.conAlfa(Paleta.UI_TINTA_TENUE, 0.30F), false);
        }
        if (SecretosJobs.hora333() && w >= 360 && h >= 200) {
            int cx = w / 2;
            g.fill(cx - 18, m + 25, cx + 19, m + 26,
                    Paleta.conAlfa(Paleta.UI_ACENTO_FUERTE, 0.10F));
            g.fill(cx, m + 22, cx + 1, m + 29,
                    Paleta.conAlfa(Paleta.UI_ACENTO_FUERTE, 0.12F));
        }
        if (SecretosJobs.minuto13() && w >= 430 && h >= 220) {
            int y = h - m - 31;
            g.fill(w / 2 - 13, y, w / 2 + 14, y + 1,
                    Paleta.conAlfa(Paleta.UI_TINTA_TENUE, 0.055F));
            g.fill(w / 2, y - 2, w / 2 + 1, y + 3,
                    Paleta.conAlfa(Paleta.UI_ACENTO, 0.070F));
        }
    }

    private static String codigoPantalla(Screen pantalla) {
        String n = pantalla.getClass().getSimpleName().toUpperCase(Locale.ROOT);
        if (n.contains("NIVEL")) return "MAIN";
        if (n.contains("ESTANCIA")) return "PAUSE";
        if (n.contains("OPCIONES")) return "OPTIONS";
        if (n.contains("AJUSTES")) return "CONFIG";
        if (n.contains("MUNDOS")) return "WORLDS";
        if (n.contains("MULTIJUGADOR")) return "MULTI";
        if (n.contains("MODS")) return "MODS";
        if (n.contains("PAQUETES")) return "RESOURCES";
        if (n.contains("IDIOMA")) return "LANG";
        if (n.contains("SONIDO")) return "AUDIO";
        if (n.contains("VIDEO")) return "VIDEO";
        if (n.contains("CONTROLES")) return "CONTROLS";
        if (n.contains("TECLAS")) return "KEYS";
        if (n.contains("MOUSE")) return "MOUSE";
        if (n.contains("CHAT")) return "CHAT";
        if (n.contains("ACCESIBILIDAD")) return "ACCESS";
        if (n.contains("PIEL")) return "APPEARANCE";
        return "FILE";
    }

    private static String codigoPerfil(PerfilesJobs.Perfil perfil) {
        return switch (perfil) {
            case EQUILIBRADO -> "PROFILE // EQ";
            case INMERSIVO -> "PROFILE // IMM";
            case RENDIMIENTO -> "PROFILE // PERF";
            case ACCESIBLE -> "PROFILE // ACC";
            case MINIMO -> "PROFILE // MIN";
        };
    }

    private static float limitar(float valor) {
        return Math.max(0.0F, Math.min(1.0F, valor));
    }
}

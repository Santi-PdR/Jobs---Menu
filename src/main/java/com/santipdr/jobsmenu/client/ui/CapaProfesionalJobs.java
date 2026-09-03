package com.santipdr.jobsmenu.client.ui;

import com.santipdr.jobsmenu.config.ConfigTurno;
import com.santipdr.jobsmenu.config.PerfilesJobs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;

/**
 * Capa de instrumentacion visual compartida. No cambia hitboxes ni fondos:
 * solo agrega contexto, foco, registro y ayudas de navegacion sobre pantallas Jobs.
 */
public final class CapaProfesionalJobs {

    private CapaProfesionalJobs() {
    }

    public static void dibujar(Screen pantalla, GuiGraphics g, int mouseX, int mouseY, long ahora) {
        if (pantalla == null || g == null || pantalla.width < 140 || pantalla.height < 90) return;

        int w = pantalla.width;
        int h = pantalla.height;
        int m = Math.max(6, Math.min(12, Math.min(w, h) / 28));
        Font font = Minecraft.getInstance().font;
        String codigo = codigoPantalla(pantalla);
        boolean minima = ConfigTurno.interfazMinima();
        boolean reducida = ConfigTurno.movimientoReducido() || ConfigTurno.bajoConsumo();
        float contraste = ConfigTurno.altoContraste() ? 1.28F : 1.0F;

        int visibles = 0;
        int activos = 0;
        AbstractWidget enfocado = null;
        AbstractWidget hover = null;
        for (var child : pantalla.children()) {
            if (child instanceof AbstractWidget widget && widget.visible) {
                visibles++;
                if (widget.active) activos++;
                if (widget.active && widget.isFocused()) enfocado = widget;
                if (widget.active && widget.isMouseOver(mouseX, mouseY)) hover = widget;
            }
        }

        int tenue = Paleta.conAlfa(Paleta.UI_TINTA_TENUE, limitar(0.10F * contraste));
        int acento = Paleta.conAlfa(Paleta.UI_ACENTO, limitar(0.16F * contraste));
        int fuerte = Paleta.conAlfa(Paleta.UI_ACENTO_FUERTE, limitar(0.24F * contraste));

        // Cabecera tecnica muy fina: identifica la pantalla sin competir con su titulo.
        if (w >= 260) {
            String cabecera = "JOBS // " + codigo;
            g.drawString(font, cabecera, m + 5, m + 5,
                    Paleta.conAlfa(Paleta.UI_TINTA_TENUE, limitar(0.42F * contraste)), false);
            int cw = font.width(cabecera);
            g.fill(m + 5, m + 16, m + 5 + Math.max(18, cw / 2), m + 17, acento);
            g.fill(m + 5 + Math.max(18, cw / 2) + 3, m + 16,
                    Math.min(w / 2, m + 5 + cw + 28), m + 17, tenue);
        }

        // Contador de controles: sirve como lectura de expediente y da sensacion de sistema vivo.
        if (!minima && w >= 330) {
            String contador = String.format(java.util.Locale.ROOT, "%02d/%02d", activos, visibles);
            int cw = font.width(contador);
            int cx = w - m - cw - 7;
            g.drawString(font, contador, cx, m + 5,
                    Paleta.conAlfa(Paleta.UI_TINTA_TENUE, limitar(0.38F * contraste)), false);
            g.fill(cx - 11, m + 7, cx - 5, m + 8, acento);
            g.fill(cx - 8, m + 4, cx - 7, m + 11, tenue);
        }

        // Perfil reconocido, expresado como codigo estable para no agregar texto no localizado.
        PerfilesJobs.Perfil perfil = PerfilesJobs.actual();
        if (!minima && perfil != null && w >= 430) {
            String p = codigoPerfil(perfil);
            int pw = font.width(p) + 12;
            int px = w - m - pw;
            int py = m + 20;
            g.fill(px, py, px + pw, py + 12, Paleta.conAlfa(Paleta.VANO, 0.16F));
            g.fill(px, py + 11, px + pw, py + 12, Paleta.conAlfa(Paleta.UI_ACENTO, 0.22F));
            g.drawString(font, p, px + 6, py + 2,
                    Paleta.conAlfa(Paleta.UI_TINTA_TENUE, limitar(0.54F * contraste)), false);
        }

        // Modulos de audio: tres indicadores compactos y legibles de un vistazo.
        if (!minima && w >= 370 && h >= 150) {
            int x = w - m - 39;
            int y = h - m - 27;
            modulo(g, x, y, "UI", ConfigTurno.sonidoBotones(), contraste);
            modulo(g, x + 14, y, "A", ConfigTurno.sonidoAmbiente(), contraste);
            modulo(g, x + 28, y, "M", ConfigTurno.musicaMenu(), contraste);
        }

        // Rail de navegacion. Los simbolos son teclas, por lo que son independientes del idioma.
        if (w >= 300 && h >= 140) {
            int y = h - m - 12;
            int x = m + 5;
            tecla(g, font, x, y, "TAB", contraste); x += 30;
            tecla(g, font, x, y, "ENTER", contraste); x += 42;
            tecla(g, font, x, y, "ESC", contraste); x += 30;
            if ("MAIN".equals(codigo) && w >= 410) {
                tecla(g, font, x, y, "F", contraste); x += 17;
                tecla(g, font, x, y, "M", contraste);
            } else if ("PAUSE".equals(codigo) && w >= 390) {
                tecla(g, font, x, y, "M", contraste);
            }
        }

        // Linea de estado inferior segmentada. No consume input ni tapa controles.
        if (w >= 220) {
            int y = h - m - 2;
            int x0 = m + 5;
            int x1 = w - m - 5;
            g.fill(x0, y, x1, y + 1, Paleta.conAlfa(Paleta.UI_TINTA_TENUE, 0.055F));
            int segmentos = Math.max(3, Math.min(12, visibles));
            int usable = Math.max(1, x1 - x0);
            for (int i = 0; i < segmentos; i++) {
                int sx = x0 + Math.round(usable * (i / (float) segmentos));
                int ex = x0 + Math.round(usable * ((i + 0.62F) / segmentos));
                g.fill(sx, y, Math.max(sx + 1, ex), y + 1,
                        Paleta.conAlfa(i < activos ? Paleta.UI_ACENTO : Paleta.UI_TINTA_TENUE,
                                i < activos ? 0.13F : 0.045F));
            }
        }

        // Foco real: se dibuja fuera del hitbox y diferencia teclado de hover.
        if (enfocado != null) {
            foco(g, enfocado, true, ahora, reducida, contraste);
        } else if (hover != null) {
            foco(g, hover, false, ahora, true, contraste);
        }

        // Marcas de registro laterales para dar continuidad de instrumento.
        if (h >= 170) {
            int cy = h / 2;
            g.fill(m - 2, cy - 12, m - 1, cy + 13, tenue);
            g.fill(m - 4, cy, m + 2, cy + 1, acento);
            g.fill(w - m + 1, cy - 8, w - m + 2, cy + 9, Paleta.conAlfa(Paleta.UI_TINTA_TENUE, 0.06F));
            g.fill(w - m - 2, cy + 2, w - m + 4, cy + 3, Paleta.conAlfa(Paleta.UI_ACENTO, 0.08F));
        }

        // Actividad superior sutil. En reduccion de movimiento se sustituye por una marca fija.
        if (!minima && w >= 360) {
            int x0 = m + 95;
            int x1 = w - m - 95;
            if (x1 > x0 + 20) {
                if (reducida) {
                    int cx = (x0 + x1) / 2;
                    g.fill(cx - 14, m + 3, cx + 15, m + 4, Paleta.conAlfa(Paleta.UI_ACENTO, 0.05F));
                } else {
                    float ciclo = (ahora % 9200L) / 9200.0F;
                    int largo = Math.max(12, Math.min(42, (x1 - x0) / 7));
                    int px = x0 + Math.round((x1 - x0 - largo) * ciclo);
                    g.fill(px, m + 3, px + largo, m + 4, Paleta.conAlfa(Paleta.UI_ACENTO, 0.045F));
                    g.fill(px + largo / 2, m + 1, px + largo / 2 + 1, m + 5,
                            Paleta.conAlfa(Paleta.UI_ACENTO_FUERTE, 0.045F));
                }
            }
        }

        // Easter eggs administrativos: discretos, sin gameplay y sin afectar layout.
        if (SecretosJobs.expedienteRaro() && w >= 320 && h >= 180) {
            String secreto = SecretosJobs.codigoExpediente();
            int sw = font.width(secreto);
            int sx = w - m - sw - 5;
            int sy = h / 2 + 54;
            g.fill(sx - 6, sy - 3, sx + sw + 5, sy + 10, Paleta.conAlfa(Paleta.VANO, 0.12F));
            g.fill(sx - 6, sy - 3, sx - 5, sy + 10, Paleta.conAlfa(Paleta.UI_ACENTO, 0.12F));
            g.drawString(font, secreto, sx, sy,
                    Paleta.conAlfa(Paleta.UI_TINTA_TENUE, 0.30F), false);
        }
        if (SecretosJobs.hora333() && w >= 360 && h >= 200) {
            int cx = w / 2;
            g.fill(cx - 18, m + 25, cx + 19, m + 26, Paleta.conAlfa(Paleta.UI_ACENTO_FUERTE, 0.10F));
            g.fill(cx, m + 22, cx + 1, m + 29, Paleta.conAlfa(Paleta.UI_ACENTO_FUERTE, 0.12F));
        }
    }

    private static void modulo(GuiGraphics g, int x, int y, String texto, boolean activo, float contraste) {
        int c = activo ? Paleta.UI_ACENTO_FUERTE : Paleta.UI_TINTA_TENUE;
        float a = activo ? 0.34F : 0.10F;
        g.fill(x, y, x + 11, y + 10, Paleta.conAlfa(Paleta.VANO, 0.12F));
        g.fill(x, y + 9, x + 11, y + 10, Paleta.conAlfa(c, limitar(a * contraste)));
        Font font = Minecraft.getInstance().font;
        int tw = font.width(texto);
        g.drawString(font, texto, x + (11 - tw) / 2, y + 1,
                Paleta.conAlfa(c, limitar((activo ? 0.62F : 0.25F) * contraste)), false);
    }

    private static void tecla(GuiGraphics g, Font font, int x, int y, String texto, float contraste) {
        int tw = font.width(texto);
        int w = tw + 8;
        g.fill(x, y, x + w, y + 11, Paleta.conAlfa(Paleta.VANO, 0.10F));
        g.fill(x, y + 10, x + w, y + 11,
                Paleta.conAlfa(Paleta.UI_TINTA_TENUE, limitar(0.12F * contraste)));
        g.drawString(font, texto, x + 4, y + 1,
                Paleta.conAlfa(Paleta.UI_TINTA_TENUE, limitar(0.44F * contraste)), false);
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
            pulso = 0.82F + 0.18F * (float) ((Math.sin(ahora / 270.0D) + 1.0D) * 0.5D);
        }
        int base = teclado ? Paleta.UI_ACENTO_FUERTE : Paleta.UI_ACENTO;
        float alfa = (teclado ? 0.52F : 0.20F) * pulso * contraste;
        int c = Paleta.conAlfa(base, limitar(alfa));
        int l = Math.min(12, Math.max(5, h / 2));
        g.fill(x - 3, y - 3, x + l, y - 2, c);
        g.fill(x - 3, y - 3, x - 2, y + l, c);
        g.fill(x + w - l, y + h + 2, x + w + 3, y + h + 3, c);
        g.fill(x + w + 2, y + h - l, x + w + 3, y + h + 3, c);
        if (teclado && w >= 40) {
            int cx = x + w / 2;
            g.fill(cx - 6, y - 2, cx + 7, y - 1, Paleta.conAlfa(base, limitar(0.28F * pulso * contraste)));
        }
    }

    private static String codigoPantalla(Screen pantalla) {
        String n = pantalla.getClass().getSimpleName().toUpperCase(java.util.Locale.ROOT);
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

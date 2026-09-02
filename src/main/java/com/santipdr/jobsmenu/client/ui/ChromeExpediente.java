package com.santipdr.jobsmenu.client.ui;

import com.santipdr.jobsmenu.JobsMenu;
import com.santipdr.jobsmenu.client.scene.EscenaNivel;
import com.santipdr.jobsmenu.client.scene.RotacionNiveles;
import com.santipdr.jobsmenu.config.ConfigTurno;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fml.ModList;

/** Lenguaje visual compartido por las pantallas administrativas de Jobs. */
public final class ChromeExpediente {

    private ChromeExpediente() {
    }

    /** Fondo comun. No abre/cierra audio: solo dibuja el recinto vigente. */
    public static void fondo(GuiGraphics g, int ancho, int alto) {
        g.fill(0, 0, ancho, alto, 0xFF000000);
        EscenaNivel.dibujar(g, ancho, alto);

        g.fill(0, 0, ancho, alto, Paleta.conAlfa(Paleta.VANO,
                ConfigTurno.altoContraste() ? 0.58F : 0.46F));

        // El ruido de monitor pertenece a la interfaz, no anima los PNG. Usa
        // acento UI neutro: el fluorescente fisico nunca tinta el expediente.
        if (!ConfigTurno.bajoConsumo() && !ConfigTurno.papelLimpio()) {
            for (int y = 1; y < alto; y += 5) {
                g.fill(0, y, ancho, y + 1, Paleta.conAlfa(Paleta.UI_ACENTO, 0.010F));
            }
        }

        // Vignette administrativa estatica para separar escena y documento.
        int sombra = Paleta.conAlfa(Paleta.VANO, ConfigTurno.altoContraste() ? 0.34F : 0.20F);
        int banda = Math.max(10, Math.min(28, Math.min(ancho, alto) / 10));
        g.fill(0, 0, ancho, banda, sombra);
        g.fill(0, alto - banda, ancho, alto, sombra);
        g.fill(0, banda, Math.max(1, banda / 2), alto - banda, sombra);
        g.fill(ancho - Math.max(1, banda / 2), banda, ancho, alto - banda, sombra);
    }

    /** Papel principal del expediente con profundidad, borde y marcas de archivo. */
    public static void panel(GuiGraphics g, int x, int y, int w, int h) {
        int sombra = Paleta.conAlfa(Paleta.VANO, 0.34F);
        g.fill(x + 4, y + 5, x + w + 5, y + h + 6, sombra);
        g.fill(x + 2, y + 3, x + w + 3, y + h + 4, Paleta.conAlfa(Paleta.VANO, 0.16F));

        HojaPapel.dibujar(g, x, y, x + w, y + h, false, 0.94F);

        int borde = Paleta.conAlfa(Paleta.tintaSecundaria(), 0.42F);
        int bordeFino = Paleta.conAlfa(Paleta.tintaSecundaria(), 0.16F);
        g.fill(x + 4, y + 4, x + w - 4, y + 5, borde);
        g.fill(x + 4, y + h - 5, x + w - 4, y + h - 4, borde);
        g.fill(x + 4, y + 4, x + 5, y + h - 4, borde);
        g.fill(x + w - 5, y + 4, x + w - 4, y + h - 4, borde);
        g.fill(x + 7, y + 7, x + w - 7, y + 8, bordeFino);

        int agujero = Paleta.conAlfa(Paleta.VANO, 0.28F);
        for (int i = 0; i < 3; i++) {
            int py = y + h / 4 + i * h / 4;
            g.fill(x + 7, py - 1, x + 10, py + 1, agujero);
            g.fill(x + 8, py, x + 9, py + 1, Paleta.conAlfa(Paleta.UI_PAPEL, 0.32F));
        }

        // El papel ya no queda como un rectangulo vacio: unas reglas casi
        // imperceptibles aportan estructura sin competir con widgets o texto.
        if (!ConfigTurno.papelLimpio() && h > 120 && w > 180) {
            int regla = Paleta.conAlfa(Paleta.tintaSecundaria(), 0.035F);
            for (int ry = y + 58; ry < y + h - 28; ry += 44) {
                g.fill(x + 18, ry, x + w - 18, ry + 1, regla);
            }
            int pliegue = Paleta.conAlfa(Paleta.tintaSecundaria(), 0.022F);
            int cx = x + w / 2;
            g.fill(cx, y + 50, cx + 1, y + h - 24, pliegue);
        }

        // Pequena pestana de archivador, sin texto duro en Java.
        int tabW = Math.min(54, Math.max(28, w / 7));
        g.fill(x + w - tabW - 16, y + 1, x + w - 16, y + 4,
                Paleta.conAlfa(Paleta.tintaSecundaria(), 0.24F));
    }

    /**
     * Marco de archivo para pantallas con listas grandes. Mantiene la identidad
     * Jobs sin convertir cada vista en otra hoja de papel gigante.
     */
    public static void panelArchivo(GuiGraphics g, int x, int y, int w, int h) {
        int sombra = Paleta.conAlfa(Paleta.VANO, 0.48F);
        g.fill(x + 4, y + 5, x + w + 5, y + h + 6, sombra);
        g.fill(x, y, x + w, y + h,
                Paleta.conAlfa(Paleta.ARCHIVO_FONDO,
                        ConfigTurno.altoContraste() ? 0.96F : 0.90F));

        int borde = Paleta.conAlfa(Paleta.ARCHIVO_ACENTO, 0.46F);
        int bordeFino = Paleta.conAlfa(Paleta.ARCHIVO_TEXTO_TENUE, 0.16F);
        g.fill(x, y, x + w, y + 1, borde);
        g.fill(x, y + h - 1, x + w, y + h, borde);
        g.fill(x, y, x + 1, y + h, borde);
        g.fill(x + w - 1, y, x + w, y + h, borde);
        g.fill(x + 5, y + 5, x + w - 5, y + 6, bordeFino);
        g.fill(x + 5, y + h - 6, x + w - 5, y + h - 5, bordeFino);

        // Marcas de inventario: metal pintado neutro, no color de pared.
        int marca = Paleta.conAlfa(Paleta.ARCHIVO_ACENTO, 0.30F);
        for (int i = 0; i < 3; i++) {
            int py = y + h / 4 + i * h / 4;
            g.fill(x + 7, py - 2, x + 9, py + 2, marca);
        }
        g.fill(x + w - 48, y + 1, x + w - 14, y + 4,
                Paleta.conAlfa(Paleta.ARCHIVO_TEXTO_TENUE, 0.34F));
    }

    /** Cabecera clara para el marco oscuro de archivo. */
    public static void cabeceraArchivo(GuiGraphics g, Font font, Component titulo, Component subtitulo,
                                       int panelX, int panelY, int panelW) {
        int centro = panelX + panelW / 2;
        String tituloVisible = ajustar(font, titulo == null ? "" : titulo.getString(),
                Math.max(24, panelW - 80));
        int tw = font.width(tituloVisible);
        int ty = panelY + 12;
        g.drawString(font, tituloVisible, centro - tw / 2, ty,
                Paleta.conAlfa(Paleta.ARCHIVO_TEXTO, 0.92F), false);

        if (subtitulo != null) {
            String texto = ajustar(font, subtitulo.getString(), Math.max(20, panelW - 52));
            int sw = font.width(texto);
            g.drawString(font, texto, centro - sw / 2, ty + 13,
                    Paleta.conAlfa(Paleta.ARCHIVO_TEXTO_TENUE, 0.82F), false);
        }
        int linea = Paleta.conAlfa(Paleta.ARCHIVO_ACENTO, 0.34F);
        g.fill(panelX + 18, panelY + 41, panelX + panelW - 18, panelY + 42, linea);
        g.fill(centro - 12, panelY + 40, centro + 12, panelY + 43,
                Paleta.conAlfa(Paleta.ARCHIVO_TEXTO_TENUE, 0.54F));
    }

    /**
     * Borra de forma opaca la cabecera vanilla y dibuja una sola cabecera Jobs.
     * La opacidad es intencional: un velo translucido dejaba sangrar glifos
     * blancos y producia los titulos dobles vistos dentro del juego.
     */
    public static void reemplazarCabeceraArchivo(GuiGraphics g, Font font,
                                                 Component titulo, Component subtitulo,
                                                 int panelX, int panelY, int panelW) {
        g.pose().pushPose();
        g.pose().translate(0.0F, 0.0F, 450.0F);
        g.fill(panelX + 1, panelY + 1, panelX + panelW - 1, panelY + 43, Paleta.ARCHIVO_FONDO);
        cabeceraArchivo(g, font, titulo, subtitulo, panelX, panelY, panelW);
        g.pose().popPose();
    }

    /** Cabecera de una sola linea que deja intactos los rotulos de listas vanilla. */
    public static void reemplazarRotuloArchivo(GuiGraphics g, Font font, Component titulo,
                                               int panelX, int panelY, int panelW) {
        g.pose().pushPose();
        g.pose().translate(0.0F, 0.0F, 450.0F);
        g.fill(panelX + 1, panelY + 1, panelX + panelW - 1, panelY + 35, Paleta.ARCHIVO_FONDO);
        String texto = ajustar(font, titulo == null ? "" : titulo.getString(), panelW - 70);
        int tw = font.width(texto);
        g.drawString(font, texto, panelX + (panelW - tw) / 2, panelY + 12,
                Paleta.conAlfa(Paleta.ARCHIVO_TEXTO, 0.92F), false);
        g.fill(panelX + 18, panelY + 31, panelX + panelW - 18, panelY + 32,
                Paleta.conAlfa(Paleta.ARCHIVO_ACENTO, 0.30F));
        g.pose().popPose();
    }

    /** Rotulo pequeno para pantallas vanilla/Forge donde no se debe tapar la lista. */
    public static void rotuloArchivoCompacto(GuiGraphics g, Font font, int ancho,
                                             Component titulo, String codigo) {
        String texto = ajustar(font, titulo == null ? "" : titulo.getString(),
                Math.max(40, Math.min(230, ancho - 90)));
        int tw = font.width(texto);
        int w = Math.min(ancho - 32, Math.max(112, tw + 46));
        int x = (ancho - w) / 2;
        g.fill(x, 7, x + w, 28, Paleta.ARCHIVO_FONDO);
        g.fill(x, 27, x + w, 28, Paleta.conAlfa(Paleta.ARCHIVO_ACENTO, 0.46F));
        g.fill(x + 7, 11, x + 9, 24, Paleta.conAlfa(Paleta.ARCHIVO_ACENTO, 0.60F));
        g.drawString(font, texto, x + 15, 13, Paleta.conAlfa(Paleta.ARCHIVO_TEXTO, 0.92F), false);
        if (codigo != null && w > 170) {
            int cw = font.width(codigo);
            g.drawString(font, codigo, x + w - cw - 8, 13,
                    Paleta.conAlfa(Paleta.ARCHIVO_TEXTO_TENUE, 0.62F), false);
        }
    }

    /** Pie discreto para archivos oscuros. */
    public static void pieArchivo(GuiGraphics g, Font font, int x, int y, int w, int h,
                                  String formulario) {
        int nivel = RotacionNiveles.capturar().indice();
        String texto = formulario + "  //  N" + String.format(java.util.Locale.ROOT, "%02d", nivel)
                + "  //  v" + version();
        texto = ajustar(font, texto, Math.max(20, w - 34));
        g.drawString(font, texto, x + 14, y + h - 15,
                Paleta.conAlfa(Paleta.ARCHIVO_TEXTO_TENUE, 0.58F), false);
    }

    /** Cabecera de una pantalla propia. */
    public static void cabecera(GuiGraphics g, Font font, Component titulo, Component subtitulo,
                                int panelX, int panelY, int panelW) {
        int centro = panelX + panelW / 2;
        int ty = panelY + 13;
        int tw = font.width(titulo);

        int linea = Paleta.conAlfa(Paleta.tintaSecundaria(), 0.28F);
        int espacio = Math.min(48, Math.max(18, (panelW - tw) / 5));
        g.fill(panelX + 18, ty + 4, Math.max(panelX + 19, centro - tw / 2 - espacio), ty + 5, linea);
        g.fill(Math.min(panelX + panelW - 19, centro + tw / 2 + espacio), ty + 4,
                panelX + panelW - 18, ty + 5, linea);
        g.drawString(font, titulo, centro - tw / 2, ty, Paleta.tintaPrincipal(), false);

        if (subtitulo != null) {
            String texto = ajustar(font, subtitulo.getString(), Math.max(16, panelW - 48));
            int sw = font.width(texto);
            g.drawString(font, texto, centro - sw / 2, ty + 13,
                    Paleta.conAlfa(Paleta.tintaSecundaria(), 0.82F), false);
        }
        divisor(g, panelX + 18, panelX + panelW - 18, panelY + 42);
    }

    /** Rotulo de seccion con regla interrumpida, reutilizable en hubs propios. */
    public static void seccion(GuiGraphics g, Font font, int x0, int x1, int y, Component rotulo) {
        if (x1 <= x0 || rotulo == null) return;
        String texto = ajustar(font, rotulo.getString(), Math.max(12, (x1 - x0) / 2));
        int tw = font.width(texto);
        int color = Paleta.conAlfa(Paleta.tintaSecundaria(), 0.66F);
        int linea = Paleta.conAlfa(Paleta.tintaSecundaria(), 0.20F);
        g.drawString(font, texto, x0, y - font.lineHeight / 2, color, false);
        int lx = x0 + tw + 7;
        if (lx < x1) {
            g.fill(lx, y + 1, x1, y + 2, linea);
            int marca = Math.min(x1, lx + 18);
            g.fill(lx, y, marca, y + 1, Paleta.conAlfa(Paleta.tintaPrincipal(), 0.28F));
        }
    }

    /** Decoracion ligera para una subpantalla vanilla que conserva su titulo. */
    public static void marcoSubpantalla(GuiGraphics g, Font font, int ancho, int alto,
                                        int panelX, int panelY, int panelW, int panelH,
                                        Component subtitulo, String formulario) {
        if (subtitulo != null && panelH > 90) {
            String texto = ajustar(font, subtitulo.getString(), Math.max(18, panelW - 48));
            int sw = font.width(texto);
            g.drawString(font, texto, ancho / 2 - sw / 2, panelY + 28,
                    Paleta.conAlfa(Paleta.tintaSecundaria(), 0.70F), false);
            divisor(g, panelX + 18, panelX + panelW - 18, panelY + 40);
        }
        esquinas(g, panelX, panelY, panelW, panelH);
        pie(g, font, panelX, panelY, panelW, panelH, formulario);
    }

    public static void divisor(GuiGraphics g, int x0, int x1, int y) {
        if (x1 <= x0) return;
        int medio = (x0 + x1) / 2;
        int fuerte = Paleta.conAlfa(Paleta.tintaSecundaria(), 0.40F);
        int suave = Paleta.conAlfa(Paleta.tintaSecundaria(), 0.08F);
        int q = Math.max(1, (x1 - x0) / 4);
        g.fill(x0, y, x0 + q, y + 1, suave);
        g.fill(x0 + q, y, medio, y + 1, fuerte);
        g.fill(medio, y, x1 - q, y + 1, fuerte);
        g.fill(x1 - q, y, x1, y + 1, suave);
        g.fill(medio - 1, y - 1, medio + 1, y + 2,
                Paleta.conAlfa(Paleta.tintaPrincipal(), 0.38F));
    }

    public static void esquinas(GuiGraphics g, int x, int y, int w, int h) {
        int c = Paleta.conAlfa(Paleta.tintaSecundaria(), 0.36F);
        int m = 8;
        int l = 10;
        g.fill(x + m, y + m, x + m + l, y + m + 1, c);
        g.fill(x + m, y + m, x + m + 1, y + m + l, c);
        g.fill(x + w - m - l, y + m, x + w - m, y + m + 1, c);
        g.fill(x + w - m - 1, y + m, x + w - m, y + m + l, c);
        g.fill(x + m, y + h - m - 1, x + m + l, y + h - m, c);
        g.fill(x + m, y + h - m - l, x + m + 1, y + h - m, c);
        g.fill(x + w - m - l, y + h - m - 1, x + w - m, y + h - m, c);
        g.fill(x + w - m - 1, y + h - m - l, x + w - m, y + h - m, c);
    }

    /** Pie seguro: reserva la esquina derecha para badges/overlays de otros mods. */
    public static void pie(GuiGraphics g, Font font, int x, int y, int w, int h, String formulario) {
        int nivel = RotacionNiveles.capturar().indice();
        String version = version();
        int ty = y + h - 15;
        int color = Paleta.conAlfa(Paleta.tintaSecundaria(), 0.54F);
        String nivelTexto = String.format(java.util.Locale.ROOT, "%02d", nivel);
        int margen = 13;
        int reservaDerecha = w >= 420 ? 42 : 24;

        String registroCompleto = Component.translatable(
                "jobsmenu.interfaz.formulario", formulario, nivelTexto, version).getString();
        int maxRegistro = Math.max(0, w - margen * 2 - reservaDerecha);
        if (w >= 500 && font.width(registroCompleto) <= maxRegistro) {
            g.drawString(font, registroCompleto, x + margen, ty, color, false);
        } else {
            String codigo = formulario + " - N" + nivelTexto;
            String revision = "v" + version;
            int rw = font.width(revision);
            int maxCodigo = Math.max(0, w - margen * 2 - reservaDerecha - rw - 18);
            String codigoVisible = ajustar(font, codigo, maxCodigo);
            String revisionVisible = ajustar(font, revision, Math.max(24, w / 5));

            if (!codigoVisible.isEmpty()) {
                g.drawString(font, codigoVisible, x + margen, ty, color, false);
            }
            if (!revisionVisible.isEmpty()) {
                int vrw = font.width(revisionVisible);
                int rx = Math.max(x + margen, x + w - margen - reservaDerecha - vrw);
                g.drawString(font, revisionVisible, rx, ty, color, false);
            }
        }

        int centro = x + w / 2;
        int reservaCentral = Math.min(94, Math.max(54, w / 5));
        int marca = Paleta.conAlfa(Paleta.tintaSecundaria(), 0.16F);
        g.fill(centro - reservaCentral, ty + 3, centro - reservaCentral + 4, ty + 4, marca);
        g.fill(centro + reservaCentral - 4, ty + 3, centro + reservaCentral, ty + 4, marca);
    }

    public static String ajustar(Font font, String texto, int maximo) {
        if (texto == null || maximo <= 8) return "";
        if (font.width(texto) <= maximo) return texto;
        String puntos = "...";
        return font.plainSubstrByWidth(texto, Math.max(0, maximo - font.width(puntos))) + puntos;
    }

    /** Banda discreta para pantallas menores que siguen siendo de otra clase. */
    public static void bandaContextual(GuiGraphics g, Font font, int ancho, int alto) {
        int altoBanda = 19;
        g.fill(0, 0, ancho, altoBanda, Paleta.conAlfa(Paleta.UI_PAPEL, 0.94F));
        g.fill(0, altoBanda - 2, ancho, altoBanda,
                Paleta.conAlfa(Paleta.tintaSecundaria(), 0.38F));
        g.fill(5, 4, 7, altoBanda - 5, Paleta.conAlfa(Paleta.UI_ACENTO, 0.68F));

        Component rotulo = Component.translatable("jobsmenu.interfaz.banda");
        g.drawString(font, rotulo, 11, 5, Paleta.tintaSecundaria(), false);

        Component estado = Component.translatable("jobsmenu.interfaz.estado");
        int ew = font.width(estado);
        g.drawString(font, estado, Math.max(11, ancho - ew - 10), 5,
                Paleta.conAlfa(Paleta.tintaSecundaria(), 0.68F), false);

        g.fill(0, alto - 2, ancho, alto,
                Paleta.conAlfa(Paleta.tintaSecundaria(), 0.18F));
    }

    private static String version() {
        try {
            return ModList.get().getModContainerById(JobsMenu.MOD_ID)
                    .map(c -> c.getModInfo().getVersion().toString())
                    .orElse("?");
        } catch (Throwable ignored) {
            return "?";
        }
    }
}

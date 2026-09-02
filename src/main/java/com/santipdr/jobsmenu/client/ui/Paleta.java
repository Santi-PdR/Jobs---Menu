package com.santipdr.jobsmenu.client.ui;

import com.santipdr.jobsmenu.config.ConfigTurno;

/**
 * Paleta unica del mod. Colores en ARGB con alfa explicito.
 *
 * Hay dos familias deliberadamente separadas:
 *
 *  - ESCENA: paredes, techo, fluorescente, humedad y materiales del recinto.
 *  - INTERFAZ: papel frio, grafito y gris verdoso. Nunca hereda el amarillo
 *    ambiental de la escena.
 *
 * El rojo pertenece solo a los Executores.
 */
public final class Paleta {

    private Paleta() {
    }

    // ---------------------------------------------------------------------
    // Escena. Estos tonos pertenecen al edificio, no a widgets ni archivos.
    // ---------------------------------------------------------------------

    /** Papel mural del nivel. El color de la casa. */
    public static final int PARED = 0xFFD8C24F;

    /** Pared cerca del fluorescente, lavada por la luz. */
    public static final int PARED_ALTA = 0xFFE6D264;

    /** Pared cerca del zocalo, donde la luz ya no llega. */
    public static final int PARED_BAJA = 0xFF9A8630;

    /** Humedad, moho, filtraciones. Tambien sirve de borde de escena. */
    public static final int MOHO = 0xFF5E5222;

    /** Alfombra humeda. */
    public static final int ALFOMBRA = 0xFF8A7638;

    /** Alfombra en sombra y bajo los marcos de puerta. */
    public static final int ALFOMBRA_OSCURA = 0xFF4C401E;

    /** Placas del cielorraso. */
    public static final int TECHO = 0xFFD5CB9B;

    /** El tubo fluorescente. La unica luz fisica del recinto. */
    public static final int FLUOR = 0xFFFFF7D2;

    /** Papel fisico de la escena; se conserva calido. */
    public static final int PAPEL = 0xFFF0E9CE;

    /** Tinta fisica de carteles/escena. */
    public static final int TINTA = 0xFF14120C;

    /** Tinta fisica secundaria. */
    public static final int TINTA_TENUE = 0xFF4A422A;

    /** El vano que da al nivel siguiente. Nunca se aclara. */
    public static final int VANO = 0xFF0D0B07;

    // ---------------------------------------------------------------------
    // Interfaz. Neutra por contrato: no reutilizar PARED/PARED_ALTA/FLUOR.
    // ---------------------------------------------------------------------

    /** Papel frio de formularios y controles. */
    public static final int UI_PAPEL = 0xFFE1E6E2;

    /** Papel frio ligeramente elevado por foco. */
    public static final int UI_PAPEL_FOCO = 0xFFEDF1EE;

    /** Tinta principal de interfaz. */
    public static final int UI_TINTA = 0xFF171B18;

    /** Tinta secundaria de interfaz. */
    public static final int UI_TINTA_TENUE = 0xFF56615B;

    /** Acento administrativo neutro. */
    public static final int UI_ACENTO = 0xFFAAB6AF;

    /** Acento administrativo para foco de teclado y confirmaciones. */
    public static final int UI_ACENTO_FUERTE = 0xFFC4CEC8;

    /** Fondo base de los archivos oscuros. */
    public static final int ARCHIVO_FONDO = 0xFF101411;

    /** Superficie de control dentro de un archivo oscuro. */
    public static final int ARCHIVO_SUPERFICIE = 0xFF1A211D;

    /** Superficie de control enfocada dentro de un archivo oscuro. */
    public static final int ARCHIVO_SUPERFICIE_FOCO = 0xFF27312B;

    /** Archivo oscuro: acento gris verdoso, nunca amarillo ni blanco puro. */
    public static final int ARCHIVO_ACENTO = 0xFFB2BBB5;

    /** Texto principal de archivo oscuro. */
    public static final int ARCHIVO_TEXTO = 0xFFD0D5D1;

    /** Texto secundario de archivo oscuro. */
    public static final int ARCHIVO_TEXTO_TENUE = 0xFF8D9791;

    /** Executores. Exclusivo. */
    public static final int ALERTA = 0xFF8E1B12;

    /** Executores, pulso de ronda inminente. */
    public static final int ALERTA_BRILLO = 0xFFC42B18;

    /** Tinta legible; alto contraste refuerza el negro sin usar blanco puro. */
    public static int tintaPrincipal() {
        return ConfigTurno.altoContraste() ? 0xFF080A09 : UI_TINTA;
    }

    /** Tinta secundaria neutra y legible. */
    public static int tintaSecundaria() {
        return ConfigTurno.altoContraste() ? 0xFF303833 : UI_TINTA_TENUE;
    }

    /** Papel de UI frio; alto contraste aclara sin llegar a blanco puro. */
    public static int papelAviso() {
        return ConfigTurno.altoContraste() ? 0xFFF5F7F5 : UI_PAPEL;
    }

    /** Devuelve el mismo color con el alfa indicado (0.0 a 1.0). */
    public static int conAlfa(int color, float alfa) {
        int a = (int) (Math.max(0.0F, Math.min(1.0F, alfa)) * 255.0F);
        return (a << 24) | (color & 0x00FFFFFF);
    }

    /** Mezcla lineal entre dos colores ARGB. */
    public static int mezclar(int desde, int hasta, float t) {
        float f = Math.max(0.0F, Math.min(1.0F, t));
        int a = (int) (((desde >> 24) & 0xFF) + (((hasta >> 24) & 0xFF) - ((desde >> 24) & 0xFF)) * f);
        int r = (int) (((desde >> 16) & 0xFF) + (((hasta >> 16) & 0xFF) - ((desde >> 16) & 0xFF)) * f);
        int g = (int) (((desde >> 8) & 0xFF) + (((hasta >> 8) & 0xFF) - ((desde >> 8) & 0xFF)) * f);
        int b = (int) ((desde & 0xFF) + ((hasta & 0xFF) - (desde & 0xFF)) * f);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    /**
     * Aplica el brillo del fluorescente a un color de la escena.
     * Con factor 1.0 el color queda tal cual; por debajo se apaga hacia el vano.
     */
    public static int iluminar(int color, float factor) {
        if (factor >= 1.0F) {
            return color;
        }
        return mezclar(VANO, color, Math.max(0.0F, factor));
    }
}

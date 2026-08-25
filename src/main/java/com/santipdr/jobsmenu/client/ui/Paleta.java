package com.santipdr.jobsmenu.client.ui;

/**
 * Paleta unica del mod. Colores en ARGB con alfa explicito.
 *
 * Reglas: el rojo pertenece solo a los Executores, el ambar es la unica fuente
 * de luz de la escena y nunca se usa blanco puro.
 */
public final class Paleta {

    private Paleta() {
    }

    public static final int FONDO_PROFUNDO = 0xFF0B0C0E;
    public static final int FONDO_ALTO = 0xFF15181C;
    public static final int HORMIGON = 0xFF232830;
    public static final int HUMO = 0xFF3A414B;
    public static final int SODIO = 0xFFD9922E;
    public static final int SODIO_TENUE = 0xFF8A5E1C;
    public static final int HUESO = 0xFFE8E4DA;
    public static final int HUESO_TENUE = 0xFF9A968E;
    public static final int ALERTA = 0xFFB3261E;
    public static final int ALERTA_BRILLO = 0xFFE8442F;

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
}

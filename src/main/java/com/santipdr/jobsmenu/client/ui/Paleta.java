package com.santipdr.jobsmenu.client.ui;

/**
 * Paleta unica del mod. Colores en ARGB con alfa explicito.
 *
 * El menu es luminoso a proposito: el terror de este servidor no es la
 * oscuridad, es el amarillo que nunca se apaga. Reglas de la paleta:
 *
 *  - El rojo pertenece solo a los Executores. Nada mas puede usarlo.
 *  - La unica fuente de luz es el fluorescente del techo.
 *  - Nunca blanco puro: el techo mas limpio sigue siendo hueso viejo.
 */
public final class Paleta {

    private Paleta() {
    }

    /** Papel mural del nivel. El color de la casa. */
    public static final int PARED = 0xFFD8C24F;

    /** Pared cerca del fluorescente, lavada por la luz. */
    public static final int PARED_ALTA = 0xFFE6D264;

    /** Pared cerca del zocalo, donde la luz ya no llega. */
    public static final int PARED_BAJA = 0xFF9A8630;

    /** Humedad, moho, filtraciones. Tambien sirve de borde. */
    public static final int MOHO = 0xFF5E5222;

    /** Alfombra humeda. */
    public static final int ALFOMBRA = 0xFF8A7638;

    /** Alfombra en sombra y bajo los marcos de puerta. */
    public static final int ALFOMBRA_OSCURA = 0xFF4C401E;

    /** Placas del cielorraso. */
    public static final int TECHO = 0xFFD5CB9B;

    /** El tubo fluorescente. La unica luz que existe. */
    public static final int FLUOR = 0xFFFFF7D2;

    /** Papel de los avisos pegados a la pared. */
    public static final int PAPEL = 0xFFF0E9CE;

    /** Tinta principal. Todo lo que se lee. */
    public static final int TINTA = 0xFF14120C;

    /** Tinta secundaria: sellos, notas al pie, letra chica. */
    public static final int TINTA_TENUE = 0xFF4A422A;

    /** El vano que da al nivel siguiente. Nunca se aclara. */
    public static final int VANO = 0xFF0D0B07;

    /** Executores. Exclusivo. */
    public static final int ALERTA = 0xFF8E1B12;

    /** Executores, pulso de ronda inminente. */
    public static final int ALERTA_BRILLO = 0xFFC42B18;

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

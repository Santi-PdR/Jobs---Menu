package com.santipdr.jobsmenu.client.scene;

/**
 * El encuadre de la escena: donde esta el punto de fuga y de que tamano es la
 * pared del fondo.
 *
 * Es lo unico que todas las plantas tienen en comun. Un corredor, una nave y
 * un natatorio se dibujan de maneras completamente distintas, pero los tres
 * arrancan de la misma pregunta: donde converge la perspectiva y cuanto mide
 * el rectangulo del fondo. Todo lo demas lo decide cada planta.
 *
 * La convencion, valida para las cuatro:
 *
 *     dx = |x - fx| / w     para columnas de pantalla
 *     dy = |y - fy| / h     para filas de pantalla
 *
 * En el borde de la pared del fondo dx = dy = 1. Cuanto mayor el valor, mas
 * cerca de la camara. La profundidad aparente es lejos = 1/dx, que va de 0
 * (pegado al ojo) a 1 (el fondo).
 *
 * @param ancho ancho de la pantalla en pixeles
 * @param alto  alto de la pantalla en pixeles
 * @param fx    punto de fuga horizontal
 * @param fy    punto de fuga vertical
 * @param w     semiancho de la pared del fondo
 * @param h     semialto de la pared del fondo
 */
public record Marco(int ancho, int alto, float fx, float fy, float w, float h) {

    /** Profundidad aparente de una columna, de 0 (cerca) a 1 (el fondo). */
    public float lejosColumna(float x) {
        float dx = Math.abs(x - this.fx) / this.w;
        if (dx <= 0.0F) {
            return 1.0F;
        }
        return Math.min(1.0F, 1.0F / dx);
    }

    /** dx de una columna: 1 en el borde del fondo, mas grande hacia el ojo. */
    public float dx(float x) {
        return Math.abs(x - this.fx) / this.w;
    }

    /** Borde superior del recinto a una profundidad dada. */
    public float techoEn(float dx) {
        return this.fy - this.h * dx;
    }

    /** Borde inferior del recinto a una profundidad dada. */
    public float sueloEn(float dx) {
        return this.fy + this.h * dx;
    }
}

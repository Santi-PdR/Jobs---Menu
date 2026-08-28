package com.santipdr.jobsmenu.client.scene;

/**
 * El encuadre de la escena: desde donde se mira el recinto.
 *
 * Es lo unico que todas las plantas tienen en comun. Un salon, una nave y un
 * natatorio se dibujan de maneras completamente distintas, pero los tres
 * arrancan de la misma pregunta: donde converge la perspectiva y como se abre
 * el recinto alrededor de esa fuga.
 *
 * <h2>Por que hay cuatro bordes y no dos</h2>
 *
 * Hasta la 0.4.0 el marco tenia un solo semiancho y un solo semialto. Esa era
 * la razon real de que los cuatro niveles se vieran iguales: con un unico
 * semiancho, la pared izquierda y la derecha estan OBLIGADAS a converger con
 * la misma pendiente, y con un unico semialto pasa lo mismo con el techo y el
 * suelo. El resultado es siempre el mismo tunel simetrico con la fuga en el
 * centro, se dibuje encima lo que se dibuje. Cambiar los pilares, los props o
 * la paleta no arregla eso, porque el problema no era la decoracion: era la
 * camara.
 *
 * Ahora los cuatro bordes son independientes:
 *
 * <pre>
 *     wi  semiancho hacia la izquierda      ha  semialto hacia arriba
 *     wd  semiancho hacia la derecha        hb  semialto hacia abajo
 * </pre>
 *
 * Con eso un recinto puede estar visto desde un rincon (wi != wd), tener el
 * horizonte alto o bajo (ha != hb), y dejar de leerse como un pasillo. Cada
 * nivel trae su propia fuga y sus propios cuatro semiejes, asi que un nivel ya
 * no es otro repintado: es otro lugar, mirado desde otro lado.
 *
 * <h2>Convencion de profundidad</h2>
 *
 * Vale para las cuatro plantas: dx = 1 cae sobre la pared del fondo y crece
 * hacia la camara. La profundidad aparente es lejos = 1/dx, de 0 (pegado al
 * ojo) a 1 (el fondo). {@link #dx(float)} y {@link #dy(float)} eligen solos el
 * semieje del lado que corresponde, asi que el codigo de las plantas no tiene
 * que saber si esta a la izquierda o a la derecha de la fuga.
 *
 * <h2>Regla de uso</h2>
 *
 * Para UBICAR una pared, un techo o un suelo, siempre {@link #izq(float)},
 * {@link #der(float)}, {@link #techoEn(float)} o {@link #sueloEn(float)}.
 * {@link #w()} y {@link #h()} son promedios y existen solo para escalar cosas
 * que no son geometria del recinto: grosores de linea, tamanos de detalle.
 * Nunca para decidir donde cae una superficie.
 *
 * @param ancho ancho de la pantalla en pixeles
 * @param alto  alto de la pantalla en pixeles
 * @param fx    punto de fuga horizontal
 * @param fy    punto de fuga vertical
 * @param wi    semiancho hacia la izquierda
 * @param wd    semiancho hacia la derecha
 * @param ha    semialto hacia arriba
 * @param hb    semialto hacia abajo
 */
public record Marco(int ancho, int alto, float fx, float fy,
                    float wi, float wd, float ha, float hb) {

    /** Pared izquierda a una profundidad dada. */
    public float izq(float dx) {
        return this.fx - this.wi * dx;
    }

    /** Pared derecha a una profundidad dada. */
    public float der(float dx) {
        return this.fx + this.wd * dx;
    }

    /** Borde superior del recinto a una profundidad dada. */
    public float techoEn(float dx) {
        return this.fy - this.ha * dx;
    }

    /** Borde inferior del recinto a una profundidad dada. */
    public float sueloEn(float dx) {
        return this.fy + this.hb * dx;
    }

    /**
     * Un punto sobre la pared izquierda (signo &lt; 0) o la derecha (signo &gt; 0).
     *
     * Sirve para lo que va apoyado o repetido contra los laterales -pilares,
     * estanterias, canerias-: cada lado usa su propio semiancho, asi que en un
     * recinto visto de esquina la hilera de la izquierda queda mas abierta que
     * la de la derecha, como corresponde.
     */
    public float lado(float signo, float dx, float fraccion) {
        return signo > 0.0F ? der(dx * fraccion) : izq(dx * fraccion);
    }

    /** Igual que {@link #lado(float, float, float)} contra la pared misma. */
    public float lado(float signo, float dx) {
        return lado(signo, dx, 1.0F);
    }

    /** Eje visual del recinto a esa profundidad. Ojo: no es la fuga. */
    public float centro(float dx) {
        return (izq(dx) + der(dx)) * 0.5F;
    }

    /** Ancho completo del recinto a esa profundidad. */
    public float anchoEn(float dx) {
        return der(dx) - izq(dx);
    }

    /**
     * Punto transversal del recinto, con fraccion de -1 (izquierda) a +1 (derecha).
     *
     * Es lo que hay que usar para todo lo que se reparte a lo ancho -placas del
     * cielorraso, corridas de losa, calles de la pileta-. Interpolar entre las
     * dos paredes reales, en vez de escalar un semiancho unico, es lo que hace
     * que esas series sigan la forma del recinto cuando la camara esta
     * descentrada.
     */
    public float enX(float dx, float fraccion) {
        return izq(dx) + (der(dx) - izq(dx)) * (fraccion + 1.0F) * 0.5F;
    }

    /** dx de una columna: 1 en el borde del fondo, mas grande hacia el ojo. */
    public float dx(float x) {
        float semieje = x < this.fx ? this.wi : this.wd;
        return Math.abs(x - this.fx) / semieje;
    }

    /** dy de una fila: 1 en el borde del fondo, mas grande hacia el ojo. */
    public float dy(float y) {
        float semieje = y < this.fy ? this.ha : this.hb;
        return Math.abs(y - this.fy) / semieje;
    }

    /** Semiancho medio. Solo para escalar grosores, nunca para ubicar. */
    public float w() {
        return (this.wi + this.wd) * 0.5F;
    }

    /** Semialto medio. Solo para escalar grosores, nunca para ubicar. */
    public float h() {
        return (this.ha + this.hb) * 0.5F;
    }
}


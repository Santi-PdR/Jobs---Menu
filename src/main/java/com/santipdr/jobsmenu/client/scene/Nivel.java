package com.santipdr.jobsmenu.client.scene;

import com.santipdr.jobsmenu.client.scene.planta.Nave;
import com.santipdr.jobsmenu.client.scene.planta.Natatorio;
import com.santipdr.jobsmenu.client.scene.planta.Planta;
import com.santipdr.jobsmenu.client.scene.planta.Sala;
import com.santipdr.jobsmenu.client.scene.planta.Servicio;

/**
 * Un nivel del servidor, visto desde donde esta pegado el aviso.
 *
 * Un nivel son dos cosas: un TIPO DE RECINTO -que decide la geometria, que hay
 * construido y como se dibuja- y una piel de colores y proporciones.
 *
 * Hasta la version anterior era solo lo segundo: los cuatro niveles eran el
 * mismo corredor repintado, y se notaba. Ahora cada uno tiene su
 * {@link Planta}: una sala ancha y baja, una nave con pilares, un pasillo de
 * servicio que dobla y un natatorio con agua. Cambiar de nivel es cambiar de
 * lugar, no de paleta.
 *
 * No hay texturas: todo se dibuja con rectangulos.
 */
public final class Nivel {

    /** Clave del nivel. Sirve de sufijo para las cadenas de idioma. */
    public final String clave;

    /** Que clase de recinto es. Decide toda la geometria. */
    public final Planta planta;

    /** Pared junto al cielorraso, donde pega la luz. */
    public final int paredAlta;

    /** Pared junto al zocalo, donde la luz ya no llega. */
    public final int paredBaja;

    /** Linea que separa un panel de pared del siguiente. */
    public final int junta;

    /** Suelo a los pies del que mira. */
    public final int suelo;

    /** Suelo contra la abertura del fondo. */
    public final int sueloLejos;

    /** Transversales del suelo. */
    public final int sueloJunta;

    /** Placa del cielorraso. */
    public final int techo;

    /** Perfileria que sostiene las placas. */
    public final int techoJunta;

    /** A donde se va todo con la distancia. */
    public final int niebla;

    /** Color de la luminaria de este nivel. */
    public final int luz;

    /** Lo que se ve por la abertura del fondo. Nunca se aclara. */
    public final int fondo;

    /** Alto del corredor dividido su ancho. Menor de 1 = pasillo achatado. */
    public final float proporcion;

    /** Semiancho de la abertura del fondo, en fraccion del ancho de pantalla. */
    public final float semiancho;

    /** Cuanta luz devuelve el suelo, de 0.0 a 1.0. */
    public final float reflejo;

    /** Cantidad de filtraciones en las paredes, de 0.0 a 1.0. */
    public final float humedad;

    private Nivel(String clave, Planta planta, int paredAlta, int paredBaja, int junta,
                  int suelo, int sueloLejos, int sueloJunta,
                  int techo, int techoJunta, int niebla, int luz, int fondo,
                  float proporcion, float semiancho, float reflejo, float humedad) {
        this.clave = clave;
        this.planta = planta;
        this.paredAlta = paredAlta;
        this.paredBaja = paredBaja;
        this.junta = junta;
        this.suelo = suelo;
        this.sueloLejos = sueloLejos;
        this.sueloJunta = sueloJunta;
        this.techo = techo;
        this.techoJunta = techoJunta;
        this.niebla = niebla;
        this.luz = luz;
        this.fondo = fondo;
        this.proporcion = proporcion;
        this.semiancho = semiancho;
        this.reflejo = reflejo;
        this.humedad = humedad;
    }

    /**
     * Los niveles por los que rota el fondo del menu, en orden de profundidad.
     * El Nivel 0 va primero porque es donde empieza todo el mundo.
     */
    public static final Nivel[] CATALOGO = new Nivel[] {

            // Nivel 0 - El papel mural. La postal del servidor: amarillo que no
            // se apaga. Sala ancha y baja, de las que se cruzan sin pensarlo.
            new Nivel("nivel0", new Sala(),
                    0xFFE6D264, 0xFF9A8630, 0xFF5E5222,
                    0xFF8A7638, 0xFF6E5C2A, 0xFF4C401E,
                    0xFFD5CB9B, 0xFF8E8760,
                    0xFFC9B455, 0xFFFFF7D2, 0xFF0D0B07,
                    0.62F, 0.150F, 0.16F, 1.00F),

            // Nivel 1 - El deposito. Hormigon, altura y demasiado espacio para
            // uno solo. Nave con pilares, cerchas y campanas a medio prender.
            new Nivel("nivel1", new Nave(),
                    0xFFB6BAAE, 0xFF74786C, 0xFF4A4E43,
                    0xFF80847A, 0xFF5A5E54, 0xFF3C4036,
                    0xFF9EA298, 0xFF5C6055,
                    0xFF6E7268, 0xFFE8F0FF, 0xFF171B1D,
                    0.50F, 0.115F, 0.30F, 0.35F),

            // Nivel 2 - Servicio. Estrecho, caliente, con las canerias a la
            // vista. El unico que sigue siendo un pasillo, y dobla.
            new Nivel("nivel2", new Servicio(),
                    0xFF6E4A28, 0xFF3E2A17, 0xFF241609,
                    0xFF413025, 0xFF2A1F16, 0xFF1B120C,
                    0xFF4A3520, 0xFF2A1C0E,
                    0xFF54371C, 0xFFFFB65E, 0xFF0B0703,
                    1.35F, 0.058F, 0.22F, 0.75F),

            // Nivel 3 - Las piscinas. Azulejo, agua tibia y un eco que tarda de
            // mas. Aca la mitad de abajo de la escena no es suelo: es agua.
            new Nivel("nivel3", new Natatorio(),
                    0xFFE4EFEC, 0xFFA9C6C2, 0xFF7EA5A2,
                    0xFF63B6B4, 0xFF2F7E82, 0xFF3E9A9A,
                    0xFFE8F2F0, 0xFFB2CCC9,
                    0xFFBEDCD9, 0xFFF4FFFD, 0xFF08171A,
                    0.62F, 0.118F, 0.62F, 0.15F),
    };

    /** Cuantos niveles hay en la rotacion. */
    public static int cantidad() {
        return CATALOGO.length;
    }

    /** Devuelve un nivel del catalogo, dando la vuelta si el indice se pasa. */
    public static Nivel porIndice(int indice) {
        int n = CATALOGO.length;
        return CATALOGO[((indice % n) + n) % n];
    }
}

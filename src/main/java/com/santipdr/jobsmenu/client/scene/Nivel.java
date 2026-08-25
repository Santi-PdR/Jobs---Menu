package com.santipdr.jobsmenu.client.scene;

/**
 * Un nivel del servidor, visto desde el corredor donde esta pegado el aviso.
 *
 * Cada nivel define la piel completa del pasillo: colores, proporciones y que
 * cosas hay colgadas de las paredes. La geometria es siempre la misma, porque
 * siempre es el mismo edificio; lo que cambia es de que esta hecho.
 *
 * No hay texturas: todo se dibuja con rectangulos. Un nivel es, literalmente,
 * esta lista de numeros.
 */
public final class Nivel {

    /** Clave del nivel. Sirve de sufijo para las cadenas de idioma. */
    public final String clave;

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

    /** Si el nivel tiene zocalo corrido al pie de las paredes. */
    public final boolean zocalo;

    /** Cantidad de filtraciones en las paredes, de 0.0 a 1.0. */
    public final float humedad;

    /** Tuberias corridas bajo el cielorraso. */
    public final boolean tuberias;

    /** Vanos abiertos en las paredes laterales. */
    public final boolean marcos;

    private Nivel(String clave, int paredAlta, int paredBaja, int junta,
                  int suelo, int sueloLejos, int sueloJunta,
                  int techo, int techoJunta, int niebla, int luz, int fondo,
                  float proporcion, float semiancho, float reflejo,
                  boolean zocalo, float humedad, boolean tuberias, boolean marcos) {
        this.clave = clave;
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
        this.zocalo = zocalo;
        this.humedad = humedad;
        this.tuberias = tuberias;
        this.marcos = marcos;
    }

    /**
     * Los niveles por los que rota el fondo del menu, en orden de profundidad.
     * El Nivel 0 va primero porque es donde empieza todo el mundo.
     */
    public static final Nivel[] CATALOGO = new Nivel[] {

            // Nivel 0 - El papel mural. La postal del servidor: amarillo que no se apaga.
            new Nivel("nivel0",
                    0xFFE6D264, 0xFF9A8630, 0xFF5E5222,
                    0xFF8A7638, 0xFF6E5C2A, 0xFF4C401E,
                    0xFFD5CB9B, 0xFF8E8760,
                    0xFFC9B455, 0xFFFFF7D2, 0xFF0D0B07,
                    0.92F, 0.082F, 0.16F, true, 1.00F, false, true),

            // Nivel 1 - El deposito. Hormigon, altura y demasiado espacio para uno solo.
            new Nivel("nivel1",
                    0xFFB6BAAE, 0xFF74786C, 0xFF4A4E43,
                    0xFF80847A, 0xFF5A5E54, 0xFF3C4036,
                    0xFF9EA298, 0xFF5C6055,
                    0xFF6E7268, 0xFFE8F0FF, 0xFF171B1D,
                    0.98F, 0.132F, 0.30F, false, 0.35F, false, true),

            // Nivel 2 - Servicio. Estrecho, caliente, con las canerias a la vista.
            new Nivel("nivel2",
                    0xFF6E4A28, 0xFF3E2A17, 0xFF241609,
                    0xFF413025, 0xFF2A1F16, 0xFF1B120C,
                    0xFF4A3520, 0xFF2A1C0E,
                    0xFF54371C, 0xFFFFB65E, 0xFF0B0703,
                    0.78F, 0.070F, 0.22F, false, 0.75F, true, false),

            // Nivel 3 - Las piscinas. Azulejo, agua tibia y un eco que tarda de mas.
            new Nivel("nivel3",
                    0xFFE4EFEC, 0xFFA9C6C2, 0xFF7EA5A2,
                    0xFF63B6B4, 0xFF2F7E82, 0xFF3E9A9A,
                    0xFFE8F2F0, 0xFFB2CCC9,
                    0xFFBEDCD9, 0xFFF4FFFD, 0xFF08171A,
                    1.02F, 0.098F, 0.62F, false, 0.15F, false, true),
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

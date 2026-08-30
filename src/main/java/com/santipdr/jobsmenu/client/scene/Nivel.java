package com.santipdr.jobsmenu.client.scene;

import com.santipdr.jobsmenu.client.scene.planta.Biblioteca;
import com.santipdr.jobsmenu.client.scene.planta.Catacumba;
import com.santipdr.jobsmenu.client.scene.planta.Cisterna;
import com.santipdr.jobsmenu.client.scene.planta.Cripta;
import com.santipdr.jobsmenu.client.scene.planta.Invernadero;
import com.santipdr.jobsmenu.client.scene.planta.Nave;
import com.santipdr.jobsmenu.client.scene.planta.Natatorio;
import com.santipdr.jobsmenu.client.scene.planta.Planta;
import com.santipdr.jobsmenu.client.scene.planta.PlantaImagen;
import com.santipdr.jobsmenu.client.scene.planta.Sala;
import com.santipdr.jobsmenu.client.scene.planta.Servicio;
import com.santipdr.jobsmenu.client.scene.planta.Trono;

/**
 * Un nivel del servidor visto desde donde esta pegado el aviso.
 *
 * Los niveles 0-9 conservan plantas procedurales. Los niveles 10-17 usan
 * imagenes suministradas para el proyecto y pasan por PlantaImagen, por lo que
 * participan de luz, apagones, Suspension y tratamiento ambiental.
 */
public final class Nivel {

    public final String clave;
    public final Planta planta;
    public final int paredAlta;
    public final int paredBaja;
    public final int junta;
    public final int suelo;
    public final int sueloLejos;
    public final int sueloJunta;
    public final int techo;
    public final int techoJunta;
    public final int niebla;
    public final int luz;
    public final int fondo;
    public final float fugaX;
    public final float fugaY;
    public final float semiIzq;
    public final float semiDer;
    public final float semiAlto;
    public final float semiBajo;
    public final float reflejo;
    public final float humedad;

    private Nivel(String clave, Planta planta, int paredAlta, int paredBaja, int junta,
                  int suelo, int sueloLejos, int sueloJunta,
                  int techo, int techoJunta, int niebla, int luz, int fondo,
                  float fugaX, float fugaY, float semiIzq, float semiDer,
                  float semiAlto, float semiBajo, float reflejo, float humedad) {
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
        this.fugaX = fugaX;
        this.fugaY = fugaY;
        this.semiIzq = semiIzq;
        this.semiDer = semiDer;
        this.semiAlto = semiAlto;
        this.semiBajo = semiBajo;
        this.reflejo = reflejo;
        this.humedad = humedad;
    }

    /** Catalogo completo en el orden de la rotacion automatica. */
    public static final Nivel[] CATALOGO = new Nivel[] {
            new Nivel("nivel0", new Sala(),
                    0xFFE6D264, 0xFF9A8630, 0xFF5E5222,
                    0xFF8A7638, 0xFF6E5C2A, 0xFF4C401E,
                    0xFFD5CB9B, 0xFF8E8760,
                    0xFFC9B455, 0xFFFFF7D2, 0xFF0D0B07,
                    0.680F, 0.470F, 0.330F, 0.105F, 0.150F, 0.135F,
                    0.16F, 1.00F),

            new Nivel("nivel1", new Nave(),
                    0xFFB6BAAE, 0xFF74786C, 0xFF4A4E43,
                    0xFF80847A, 0xFF5A5E54, 0xFF3C4036,
                    0xFF9EA298, 0xFF5C6055,
                    0xFF6E7268, 0xFFE8F0FF, 0xFF171B1D,
                    0.505F, 0.720F, 0.235F, 0.255F, 0.300F, 0.098F,
                    0.30F, 0.35F),

            new Nivel("nivel2", new Servicio(),
                    0xFF6E4A28, 0xFF3E2A17, 0xFF241609,
                    0xFF413025, 0xFF2A1F16, 0xFF1B120C,
                    0xFF4A3520, 0xFF2A1C0E,
                    0xFF54371C, 0xFFFFB65E, 0xFF0B0703,
                    0.395F, 0.505F, 0.062F, 0.078F, 0.108F, 0.098F,
                    0.22F, 0.75F),

            new Nivel("nivel3", new Natatorio(),
                    0xFFE4EFEC, 0xFFA9C6C2, 0xFF7EA5A2,
                    0xFF63B6B4, 0xFF2F7E82, 0xFF3E9A9A,
                    0xFFE8F2F0, 0xFFB2CCC9,
                    0xFFBEDCD9, 0xFFF4FFFD, 0xFF08171A,
                    0.455F, 0.330F, 0.300F, 0.270F, 0.080F, 0.124F,
                    0.62F, 0.30F),

            new Nivel("nivel4", new Cripta(),
                    0xFF9A7444, 0xFF5E4227, 0xFF34220F,
                    0xFF6E5432, 0xFF463320, 0xFF2C1C0C,
                    0xFF836540, 0xFF4E3822,
                    0xFF4A3520, 0xFFFFC070, 0xFF0A0603,
                    0.505F, 0.500F, 0.150F, 0.150F, 0.185F, 0.150F,
                    0.20F, 0.55F),

            new Nivel("nivel5", new Biblioteca(),
                    0xFF7C6142, 0xFF4E3B26, 0xFF2C2013,
                    0xFF5A4A34, 0xFF3C3020, 0xFF241B10,
                    0xFF6E5C42, 0xFF3E3020,
                    0xFF433624, 0xFFE9D8A0, 0xFF120E08,
                    0.500F, 0.500F, 0.140F, 0.140F, 0.150F, 0.140F,
                    0.14F, 0.45F),

            new Nivel("nivel6", new Invernadero(),
                    0xFF8A9A6E, 0xFF566040, 0xFF3B3B22,
                    0xFF4C5436, 0xFF343A24, 0xFF20240E,
                    0xFFC8D4B0, 0xFF6E7A50,
                    0xFF7E8C64, 0xFFF2F6E0, 0xFF141810,
                    0.500F, 0.500F, 0.165F, 0.165F, 0.175F, 0.130F,
                    0.18F, 0.60F),

            new Nivel("nivel7", new Catacumba(),
                    0xFF6A7078, 0xFF3C4248, 0xFF23282C,
                    0xFF43484C, 0xFF2A2E32, 0xFF181B1E,
                    0xFF565C62, 0xFF303539,
                    0xFF32383E, 0xFFFFDC96, 0xFF06080A,
                    0.470F, 0.470F, 0.070F, 0.082F, 0.130F, 0.112F,
                    0.24F, 0.85F),

            new Nivel("nivel8", new Cisterna(),
                    0xFF4A5A6E, 0xFF2A3644, 0xFF17202A,
                    0xFF1E2A38, 0xFF121A24, 0xFF0A0F16,
                    0xFF3A4A5C, 0xFF22303E,
                    0xFF1E2A38, 0xFFFFC878, 0xFF05080C,
                    0.500F, 0.500F, 0.190F, 0.190F, 0.092F, 0.118F,
                    0.80F, 0.55F),

            new Nivel("nivel9", new Trono(),
                    0xFF6C6A82, 0xFF3E3C50, 0xFF242234,
                    0xFF46445A, 0xFF2C2A3C, 0xFF181628,
                    0xFF56546A, 0xFF302E44,
                    0xFF34324A, 0xFFE8C878, 0xFF0A0812,
                    0.470F, 0.530F, 0.160F, 0.140F, 0.185F, 0.140F,
                    0.26F, 0.55F),

            // Los PNG empaquetados para 10-14 son 256x144. Antes se declaraban
            // como ~576x28x; las UV terminaban fuera del recurso y varios fondos
            // no se dibujaban de forma fiable.
            new Nivel("nivel10", new PlantaImagen("nivel10.png", 256, 144, 10),
                    0xFF5C2420, 0xFF241014, 0xFF13090B,
                    0xFF241114, 0xFF13090B, 0xFF090406,
                    0xFF3D1717, 0xFF190A0D,
                    0xFF2C0C0F, 0xFFFF6A38, 0xFF050203,
                    0.500F, 0.500F, 0.220F, 0.220F, 0.160F, 0.140F,
                    0.12F, 0.18F),

            new Nivel("nivel11", new PlantaImagen("nivel11.png", 256, 144, 11),
                    0xFF355438, 0xFF17281D, 0xFF0B130E,
                    0xFF28261B, 0xFF15130D, 0xFF090A07,
                    0xFF243427, 0xFF111B13,
                    0xFF122116, 0xFF8DFF72, 0xFF030604,
                    0.520F, 0.500F, 0.250F, 0.250F, 0.155F, 0.135F,
                    0.30F, 0.72F),

            new Nivel("nivel12", new PlantaImagen("nivel12.png", 192, 108, 12),
                    0xFF304A32, 0xFF142218, 0xFF09100C,
                    0xFF252A22, 0xFF101510, 0xFF070A08,
                    0xFF26362B, 0xFF101A13,
                    0xFF132018, 0xFF73F86D, 0xFF020503,
                    0.505F, 0.505F, 0.240F, 0.240F, 0.160F, 0.140F,
                    0.34F, 0.36F),

            new Nivel("nivel13", new PlantaImagen("nivel13.png", 256, 144, 13),
                    0xFF806038, 0xFF3E2D1A, 0xFF21150B,
                    0xFF4B3825, 0xFF2A1D11, 0xFF130C06,
                    0xFF5F472D, 0xFF302116,
                    0xFF392816, 0xFFFFBC63, 0xFF070402,
                    0.500F, 0.500F, 0.240F, 0.240F, 0.175F, 0.145F,
                    0.22F, 0.42F),

            new Nivel("nivel14", new PlantaImagen("nivel14.png", 256, 127, 14),
                    0xFF315136, 0xFF14251A, 0xFF09110C,
                    0xFF263027, 0xFF111912, 0xFF070B08,
                    0xFF28412F, 0xFF111D16,
                    0xFF13271B, 0xFF68FF67, 0xFF020603,
                    0.500F, 0.515F, 0.245F, 0.245F, 0.170F, 0.145F,
                    0.28F, 0.30F),

            // Interferencia de Executor. El rojo sigue reservado al mismo eje
            // narrativo de contencion/peligro, sin convertirse en color global.
            new Nivel("nivel15", new PlantaImagen("nivel15.png", 192, 108, 15),
                    0xFF6B1C12, 0xFF270907, 0xFF150504,
                    0xFF26100D, 0xFF120706, 0xFF080303,
                    0xFF45120D, 0xFF1A0806,
                    0xFF2D0907, 0xFFFF5B2A, 0xFF040101,
                    0.500F, 0.500F, 0.230F, 0.230F, 0.165F, 0.145F,
                    0.10F, 0.14F),

            // Archivo del prisma: frio, casi monocromo, con muy poca humedad.
            new Nivel("nivel16", new PlantaImagen("nivel16.png", 192, 108, 16),
                    0xFFBFC5C8, 0xFF3C4145, 0xFF171A1C,
                    0xFF303438, 0xFF17191B, 0xFF090A0B,
                    0xFF70767A, 0xFF2A2E31,
                    0xFF565C60, 0xFFE6EEEE, 0xFF030405,
                    0.500F, 0.500F, 0.220F, 0.220F, 0.160F, 0.140F,
                    0.18F, 0.08F),

            // Galeria de sombra: frio azulado y muy oscuro, sin agresion roja.
            new Nivel("nivel17", new PlantaImagen("nivel17.png", 192, 108, 17),
                    0xFF30446E, 0xFF11182D, 0xFF080B16,
                    0xFF172038, 0xFF0B1020, 0xFF050810,
                    0xFF263452, 0xFF11182A,
                    0xFF17243E, 0xFF7FB8FF, 0xFF02040A,
                    0.500F, 0.505F, 0.235F, 0.235F, 0.165F, 0.145F,
                    0.24F, 0.26F),
    };

    public static int cantidad() {
        return CATALOGO.length;
    }

    public static Nivel porIndice(int indice) {
        int n = CATALOGO.length;
        return CATALOGO[((indice % n) + n) % n];
    }

    /** Numero de nivel derivado de la clave, por ejemplo nivel17 -> 17. */
    public int numero() {
        int n = 0;
        for (int i = 0; i < this.clave.length(); i++) {
            char c = this.clave.charAt(i);
            if (c >= '0' && c <= '9') {
                n = n * 10 + (c - '0');
            }
        }
        return n;
    }
}

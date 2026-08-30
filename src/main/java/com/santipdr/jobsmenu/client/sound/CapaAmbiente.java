package com.santipdr.jobsmenu.client.sound;

import com.santipdr.jobsmenu.client.scene.Presencia;
import com.santipdr.jobsmenu.client.scene.RotacionNiveles;
import com.santipdr.jobsmenu.config.ConfigTurno;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

/**
 * Una cama de sonido continuo de un nivel, en bucle mientras el nivel este a la vista.
 *
 * Hay una instancia viva por nivel, no una sola que cambia de archivo: asi los
 * cuatro ambientes pueden solaparse durante la transicion y el pasillo nuevo
 * empieza a escucharse mientras el viejo todavia se esta yendo. Cambiar el
 * archivo de una unica instancia obligaria a cortar en seco, y el corte se oye.
 *
 * Cada capa se ocupa sola de tres cosas:
 *
 *   - subir cuando su nivel esta en pantalla y bajar cuando no;
 *   - seguir a la luz, porque casi todo lo que suena en el pasillo es electrico
 *     y si los tubos se apagan la instalacion se apaga con ellos;
 *   - agacharse cuando hay una presencia al fondo.
 *
 * Se apaga sola y se descarta cuando termino de bajar del todo.
 *
 * TRES CAMAS POR NIVEL
 *
 * Cada nivel monta tres instancias de esta clase, con papeles distintos (ver
 * {@link Papel}). No son capas duplicadas: es la forma barata de que el fondo
 * no se vuelva reconocible. Un bucle solo, por largo que sea, termina
 * aprendiendose; varios bucles de duracion prima entre si -20, 32 y 51
 * segundos en el nivel 0- tardan horas en volver a sonar en la misma
 * combinacion, y en ese rato el oyente nunca escucha dos veces lo mismo.
 *
 * El reparto responde a una pregunta por capa, y no a un criterio de volumen:
 *
 *   BASE       que hay en este sitio          aire, sala, zumbido
 *   CARACTER   que esta funcionando           ventilacion, agua, corriente
 *   ACTIVIDAD  que esta pasando               sucesos lejanos, estructura
 *
 * Las dos primeras evitan el silencio. Eso no alcanza: a los pocos minutos el
 * oido archiva cualquier cama continua como "el silencio de esta escena" y
 * deja de contarla. Lo que sostiene un sitio durante diez minutos no es que
 * suene siempre algo, es que cada tanto PASE algo, y que no se pueda predecir
 * cuando. De eso se ocupa la tercera.
 */
public class CapaAmbiente extends AbstractTickableSoundInstance {

    /** Cuanto se tarda en llegar al volumen pedido, por tick. Lento a proposito. */
    private static final float SUAVIZADO_SUBIDA = 0.035F;

    /** Bajar es mas rapido que subir, pero no tanto como para que se note. */
    private static final float SUAVIZADO_BAJADA = 0.055F;

    /**
     * Que papel cumple esta cama dentro del nivel.
     *
     * La diferencia no es solo de volumen. La BASE es la nota del sitio: el
     * volumen de aire, la sala, el zumbido de la instalacion, y depende mucho
     * de la luz porque casi todo lo que la produce esta enchufado. El CARACTER
     * es lo que se mueve -el aire corriendo, el agua desplazandose, el goteo- y
     * apenas depende de la luz, porque el agua sigue moviendose a oscuras. Que
     * las dos reaccionen distinto al apagon es lo que hace que la transicion
     * suene a corte de corriente y no a bajada de volumen general.
     */
    public enum Papel {

        // Los pesos no suman uno ni tienen por que: dos ruidos que no estan
        // relacionados se suman en potencia, no en amplitud. Con 0.82 y 0.66 el
        // conjunto queda en la raiz de 0.82^2+0.66^2, o sea 1.05, que es
        // practicamente el mismo volumen que tenia la cama unica de antes. Con
        // los valores intuitivos -1.00 y 0.82- el ambiente subia casi un tercio
        // y se comia la musica.

        /** La cama estable. Sostiene el sitio. */
        BASE(0.82F, 0.30F, 0.083F, 0.06F),

        /** La cama viva. Sostiene la sensacion de que el lugar funciona. */
        CARACTER(0.66F, 0.72F, 0.061F, 0.09F),

        // La tercera cama esta casi siempre en silencio, asi que su peso no
        // suma al ambiente de la misma forma: lo que se oye no es su nivel
        // medio sino los picos, y por eso puede ir alta sin engordar la mezcla.
        //
        // Su piso con la luz apagada es 0.88, mas alto que el de las otras dos.
        // No es un descuido: el edificio no deja de moverse porque se corte la
        // corriente. Que en el apagon se caigan la base y el caracter y quede
        // esta capa sola -sin zumbido, sin ventilacion, solo la estructura
        // crujiendo en el vacio- es el momento mas incomodo del ciclo, y sale
        // gratis. Casi no respira (0.02) porque un suceso suelto no tiene
        // volumen medio que hacer subir y bajar.
        /** Los sucesos lejanos. Sostiene la sensacion de que el sitio esta habitado. */
        ACTIVIDAD(0.74F, 0.88F, 0.037F, 0.02F);

        /** Cuanto pesa esta cama dentro de la mezcla de ambiente del nivel. */
        private final float peso;

        /** Que fraccion del volumen sobrevive con la luz apagada del todo. */
        private final float pisoSinLuz;

        /** Velocidad de la respiracion lenta del volumen. */
        private final float respiracion;

        /** Cuanto se mueve esa respiracion. */
        private final float vaiven;

        Papel(float peso, float pisoSinLuz, float respiracion, float vaiven) {
            this.peso = peso;
            this.pisoSinLuz = pisoSinLuz;
            this.respiracion = respiracion;
            this.vaiven = vaiven;
        }
    }

    private final int nivel;
    private final Papel papel;
    private float actual;

    /** Ticks vividos, para la respiracion lenta del volumen. */
    private int edad;

    public CapaAmbiente(SoundEvent evento, int nivel, Papel papel) {
        super(evento, SoundSource.AMBIENT, RandomSource.create());
        this.nivel = nivel;
        this.papel = papel;
        this.looping = true;
        this.delay = 0;
        this.volume = 0.0F;
        this.relative = true;
        this.attenuation = Attenuation.NONE;
        this.x = 0.0D;
        this.y = 0.0D;
        this.z = 0.0D;
        this.actual = 0.0F;
        this.edad = 0;

        // Cada nivel tiene su propia red electrica y su propio tamano: un tono
        // LIGERAMENTE distinto por nivel. Antes el desvio llegaba a 1.23 en el
        // nivel 9 (+23 %), que se oia como cinta acelerada y no como otro
        // lugar; con este rango (0.975-1.011) el matiz existe sin cambiarle
        // el material a nada.
        this.pitch = 0.975F + 0.004F * nivel;

        // Las camas del mismo nivel no arrancan con la misma edad. Si lo
        // hicieran, sus respiraciones subirian y bajarian juntas y el conjunto
        // volveria a tener un pulso unico y audible.
        if (papel == Papel.CARACTER) {
            this.edad = 617;
            this.pitch *= 0.995F;
        } else if (papel == Papel.ACTIVIDAD) {
            this.edad = 1_483;
            // Sin correr el tono. Estos sucesos son objetos reconocibles
            // -chapa, vidrio, madera- y no un lecho de ruido: moverles la
            // altura les cambia el material y el tamano, que es justamente lo
            // que se eligio a mano al disenarlos.
            this.pitch = 1.0F;
        }
    }

    /** El papel que cumple esta cama. */
    public Papel papel() {
        return this.papel;
    }

    /** El nivel al que pertenece esta capa. */
    public int nivel() {
        return this.nivel;
    }

    /**
     * Autoriza a nacer en silencio.
     *
     * Es la linea de la que dependia todo el ambiente del menu. El motor de
     * sonido descarta cualquier instancia cuyo volumen sea cero en el momento
     * de arrancar, y no la vuelve a mirar nunca: se pierde en el mismo
     * fotograma en que se la crea. Como esta capa entra siempre desde cero
     * para poder subir sin escalon, sin esto no llegaba a sonar jamas, por muy
     * bien registrada que estuviera en sounds.json.
     */
    @Override
    public boolean canStartSilent() {
        return true;
    }

    /** Si ya se apago del todo y se puede tirar. */
    public boolean agotada() {
        return this.isStopped();
    }

    /** Detencion explicita para cierres y reemplazos del SoundEngine. */
    public void detenerAhora() {
        this.stop();
    }

    @Override
    public void tick() {
        this.edad++;

        RotacionNiveles.Estado estado = RotacionNiveles.capturar();
        // La cama sigue viva mientras dure la VISITA, no solo mientras el aviso
        // sea la pantalla activa: abrir Opciones o Mods ya no reinicia el bucle
        // del recinto. Al entrar a un mundo la visita se cierra y esta cama se
        // detiene (via GestorAmbiente.cerrar o por su propia bajada).
        boolean enMenu = com.santipdr.jobsmenu.client.SesionMenu.activa();
        boolean mia = estado.indice() == this.nivel;
        boolean permitido = enMenu && mia && ConfigTurno.sonidoAmbiente();

        float objetivo = 0.0F;
        if (permitido) {
            // El volumen maestro del aviso (tecla M) gobierna la cama entera.
            objetivo = ConfigTurno.volumenAmbiente() * MezclaAudio.AMBIENTE
                    * this.papel.peso * ConfigTurno.volumenAviso();

            // La instalacion depende de la luz, y cada papel a su manera: la
            // base se va casi del todo con el apagon, el caracter aguanta.
            // La Suspension es mas extrema que el corte normal: deja solo la
            // cama ACTIVIDAD en su piso sonoro y hunde las otras dos casi a
            // cero. El agua/estructura no desaparece; el edificio respira
            // abajo, sin una pared de ambiente encima del suspiro.
            float luz = estado.luz();
            float factorLuz;
            if (estado.enSuspension()) {
                factorLuz = this.papel == Papel.ACTIVIDAD ? this.papel.pisoSinLuz : 0.015F;
            } else {
                factorLuz = this.papel.pisoSinLuz + (1.0F - this.papel.pisoSinLuz) * luz;
            }
            objetivo *= factorLuz;

            // Respiracion muy lenta, del orden del minuto. Es lo que impide que
            // un bucle de veinte segundos se sienta como un bucle.
            float t = this.edad / 20.0F;
            objetivo *= 1.0F + this.papel.vaiven * (float) Math.sin(t * this.papel.respiracion)
                    + 0.03F * (float) Math.sin(t * 0.031F + 1.7F);

            // Algo al fondo del pasillo: el ambiente se retira. La capa de
            // actividad NO se retira, y ese es el punto: cuando la base y el
            // caracter se agachan, lo que queda arriba son los sucesos del
            // edificio. El sitio no se calla porque haya algo mirando; se
            // calla todo lo demas y se sigue oyendo la estructura.
            if (this.papel != Papel.ACTIVIDAD) {
                objetivo *= 1.0F - (1.0F - MezclaAudio.AGACHE_FIGURA)
                        * Presencia.visibilidad(estado.ahora());
            }
        }

        float paso = objetivo > this.actual ? SUAVIZADO_SUBIDA : SUAVIZADO_BAJADA;
        this.actual += (objetivo - this.actual) * paso;
        if (this.actual < 0.0008F) {
            this.actual = 0.0F;
        }
        this.volume = this.actual;

        if (this.actual <= 0.0F && (!enMenu || !mia)) {
            this.stop();
        }
    }
}

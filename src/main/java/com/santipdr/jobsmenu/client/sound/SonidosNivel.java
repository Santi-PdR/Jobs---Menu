package com.santipdr.jobsmenu.client.sound;

import com.santipdr.jobsmenu.JobsMenu;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Catalogo de sonidos del mod.
 *
 * Todo lo que suena aca esta sintetizado desde cero en tools/sonidos.py: no
 * hay una sola muestra de terceros. Los nombres estan agrupados por familia
 * (ui, ambiente, evento, nivel, figura, musica) y esa agrupacion se respeta
 * tanto en sounds.json como en la carpeta de archivos.
 */
public final class SonidosNivel {

    private SonidosNivel() {
    }

    public static final DeferredRegister<SoundEvent> REGISTRO =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, JobsMenu.MOD_ID);

    // ---- Interfaz -------------------------------------------------------
    // Ocho gestos, todos del mismo universo mecanico y con la misma sala.

    /** El cursor pasa por encima de algo. Roce de papel. */
    public static final RegistryObject<SoundEvent> UI_PASAR = registrar("ui.pasar");

    /** Se elige un renglon. Sello de goma. */
    public static final RegistryObject<SoundEvent> UI_ELEGIR = registrar("ui.elegir");

    /** Se confirma y se cambia de pantalla. Interruptor de pared. */
    public static final RegistryObject<SoundEvent> UI_CONFIRMAR = registrar("ui.confirmar");

    /** Se vuelve atras. El mismo interruptor, soltado. */
    public static final RegistryObject<SoundEvent> UI_VOLVER = registrar("ui.volver");

    /** Cambia el valor de una opcion. Rueda dentada. */
    public static final RegistryObject<SoundEvent> UI_ALTERNAR = registrar("ui.alternar");

    /** Se abre el aviso. */
    public static final RegistryObject<SoundEvent> UI_ABRIR = registrar("ui.abrir");

    /** Se cierra el aviso. */
    public static final RegistryObject<SoundEvent> UI_CERRAR = registrar("ui.cerrar");

    /** La accion no se puede hacer. Un rele que no engancha. */
    public static final RegistryObject<SoundEvent> UI_NEGADO = registrar("ui.negado");

    // ---- Ambientes base -------------------------------------------------
    // Un bucle largo por nivel. Ninguno dura lo mismo que otro.

    public static final RegistryObject<SoundEvent> AMBIENTE_NIVEL0 = registrar("ambiente.nivel0");
    public static final RegistryObject<SoundEvent> AMBIENTE_NIVEL1 = registrar("ambiente.nivel1");
    public static final RegistryObject<SoundEvent> AMBIENTE_NIVEL2 = registrar("ambiente.nivel2");
    public static final RegistryObject<SoundEvent> AMBIENTE_NIVEL3 = registrar("ambiente.nivel3");
    public static final RegistryObject<SoundEvent> AMBIENTE_NIVEL4 = registrar("ambiente.nivel4");
    public static final RegistryObject<SoundEvent> AMBIENTE_NIVEL5 = registrar("ambiente.nivel5");
    public static final RegistryObject<SoundEvent> AMBIENTE_NIVEL6 = registrar("ambiente.nivel6");
    public static final RegistryObject<SoundEvent> AMBIENTE_NIVEL7 = registrar("ambiente.nivel7");
    public static final RegistryObject<SoundEvent> AMBIENTE_NIVEL8 = registrar("ambiente.nivel8");
    public static final RegistryObject<SoundEvent> AMBIENTE_NIVEL9 = registrar("ambiente.nivel9");

    // ---- Capa de caracter -----------------------------------------------
    // La segunda cama continua de cada nivel. Suena siempre, a la vez que la
    // base, y dura distinto que ella a proposito: como los dos bucles no son
    // multiplos, la combinacion que se oye tarda cuartos de hora en repetirse.

    public static final RegistryObject<SoundEvent> CARACTER_NIVEL0 = registrar("caracter.nivel0");
    public static final RegistryObject<SoundEvent> CARACTER_NIVEL1 = registrar("caracter.nivel1");
    public static final RegistryObject<SoundEvent> CARACTER_NIVEL2 = registrar("caracter.nivel2");
    public static final RegistryObject<SoundEvent> CARACTER_NIVEL3 = registrar("caracter.nivel3");
    public static final RegistryObject<SoundEvent> CARACTER_NIVEL4 = registrar("caracter.nivel4");
    public static final RegistryObject<SoundEvent> CARACTER_NIVEL5 = registrar("caracter.nivel5");
    public static final RegistryObject<SoundEvent> CARACTER_NIVEL6 = registrar("caracter.nivel6");
    public static final RegistryObject<SoundEvent> CARACTER_NIVEL7 = registrar("caracter.nivel7");
    public static final RegistryObject<SoundEvent> CARACTER_NIVEL8 = registrar("caracter.nivel8");
    public static final RegistryObject<SoundEvent> CARACTER_NIVEL9 = registrar("caracter.nivel9");

    // ---- Capa de actividad ----------------------------------------------
    // La tercera cama. Al reves que las otras dos, esta casi todo el tiempo en
    // silencio: es un bucle de un minuto donde cada tanto pasa algo lejos, al
    // fondo del edificio. Las camas continuas evitan el silencio, pero se
    // vuelven mobiliario; lo que mantiene el sitio habitado son los sucesos.
    // Va en bucle y no como evento suelto para que se solape con lo demas en
    // vez de esperar turno, y para que no pueda terminar y dejar silencio.

    public static final RegistryObject<SoundEvent> ACTIVIDAD_NIVEL0 = registrar("actividad.nivel0");
    public static final RegistryObject<SoundEvent> ACTIVIDAD_NIVEL1 = registrar("actividad.nivel1");
    public static final RegistryObject<SoundEvent> ACTIVIDAD_NIVEL2 = registrar("actividad.nivel2");
    public static final RegistryObject<SoundEvent> ACTIVIDAD_NIVEL3 = registrar("actividad.nivel3");
    public static final RegistryObject<SoundEvent> ACTIVIDAD_NIVEL4 = registrar("actividad.nivel4");
    public static final RegistryObject<SoundEvent> ACTIVIDAD_NIVEL5 = registrar("actividad.nivel5");
    public static final RegistryObject<SoundEvent> ACTIVIDAD_NIVEL6 = registrar("actividad.nivel6");
    public static final RegistryObject<SoundEvent> ACTIVIDAD_NIVEL7 = registrar("actividad.nivel7");
    public static final RegistryObject<SoundEvent> ACTIVIDAD_NIVEL8 = registrar("actividad.nivel8");
    public static final RegistryObject<SoundEvent> ACTIVIDAD_NIVEL9 = registrar("actividad.nivel9");

    // ---- Eventos --------------------------------------------------------
    // Se disparan solos, con separacion y volumen variables.

    public static final RegistryObject<SoundEvent> EV_N0_TUBO = registrar("evento.nivel0_tubo");
    public static final RegistryObject<SoundEvent> EV_N0_PLACA = registrar("evento.nivel0_placa");
    public static final RegistryObject<SoundEvent> EV_N0_PUERTA = registrar("evento.nivel0_puerta");

    public static final RegistryObject<SoundEvent> EV_N1_METAL = registrar("evento.nivel1_metal");
    public static final RegistryObject<SoundEvent> EV_N1_ESTRUCTURA = registrar("evento.nivel1_estructura");
    public static final RegistryObject<SoundEvent> EV_N1_LEJANO = registrar("evento.nivel1_lejano");

    public static final RegistryObject<SoundEvent> EV_N2_CANO = registrar("evento.nivel2_cano");
    public static final RegistryObject<SoundEvent> EV_N2_VALVULA = registrar("evento.nivel2_valvula");
    public static final RegistryObject<SoundEvent> EV_N2_GOTEO = registrar("evento.nivel2_goteo");

    public static final RegistryObject<SoundEvent> EV_N3_GOTA = registrar("evento.nivel3_gota");
    public static final RegistryObject<SoundEvent> EV_N3_ONDAS = registrar("evento.nivel3_ondas");
    public static final RegistryObject<SoundEvent> EV_N3_VENTILACION = registrar("evento.nivel3_ventilacion");
    public static final RegistryObject<SoundEvent> EV_N3_LEJANO = registrar("evento.nivel3_lejano");

    public static final RegistryObject<SoundEvent> EV_N4_ANTORCHA = registrar("evento.nivel4_antorcha");
    public static final RegistryObject<SoundEvent> EV_N4_CADENA = registrar("evento.nivel4_cadena");
    public static final RegistryObject<SoundEvent> EV_N4_PIEDRA = registrar("evento.nivel4_piedra");

    public static final RegistryObject<SoundEvent> EV_N5_LIBRO = registrar("evento.nivel5_libro");
    public static final RegistryObject<SoundEvent> EV_N5_SUSURRO = registrar("evento.nivel5_susurro");
    public static final RegistryObject<SoundEvent> EV_N5_RELOJ = registrar("evento.nivel5_reloj");

    public static final RegistryObject<SoundEvent> EV_N6_VIDRIO = registrar("evento.nivel6_vidrio");
    public static final RegistryObject<SoundEvent> EV_N6_GOTA = registrar("evento.nivel6_gota");
    public static final RegistryObject<SoundEvent> EV_N6_HOJAS = registrar("evento.nivel6_hojas");

    public static final RegistryObject<SoundEvent> EV_N7_GOTA = registrar("evento.nivel7_gota");
    public static final RegistryObject<SoundEvent> EV_N7_PIEDRA = registrar("evento.nivel7_piedra");
    public static final RegistryObject<SoundEvent> EV_N7_VIENTO = registrar("evento.nivel7_viento");

    public static final RegistryObject<SoundEvent> EV_N8_GOTA = registrar("evento.nivel8_gota");
    public static final RegistryObject<SoundEvent> EV_N8_CHAPOTEO = registrar("evento.nivel8_chapoteo");
    public static final RegistryObject<SoundEvent> EV_N8_COLUMNA = registrar("evento.nivel8_columna");

    public static final RegistryObject<SoundEvent> EV_N9_CASCOTE = registrar("evento.nivel9_cascote");
    public static final RegistryObject<SoundEvent> EV_N9_ESTANDARTE = registrar("evento.nivel9_estandarte");
    public static final RegistryObject<SoundEvent> EV_N9_PUERTA = registrar("evento.nivel9_puerta");

    // ---- Transicion entre niveles ---------------------------------------

    /** El tubo duda, un instante antes del corte. */
    public static final RegistryObject<SoundEvent> NIVEL_TITILEO = registrar("nivel.titileo");

    /** Se corta la alimentacion. */
    public static final RegistryObject<SoundEvent> NIVEL_APAGON = registrar("nivel.apagon");

    /** El tubo del nivel nuevo arranca en frio. */
    public static final RegistryObject<SoundEvent> NIVEL_ENCENDIDO = registrar("nivel.encendido");

    // ---- La figura ------------------------------------------------------

    /** Caida de presion cuando algo cruza el fondo. No es un golpe. */
    public static final RegistryObject<SoundEvent> FIGURA_PRESENCIA = registrar("figura.presencia");

    // ---- Musica ---------------------------------------------------------

    /** Absurdism, pista base historica del menu. */
    public static final RegistryObject<SoundEvent> MUSICA_TEMA = registrar("musica.tema");

    /** REQUIEM - Emmy Z / Forsaken OST. */
    public static final RegistryObject<SoundEvent> MUSICA_REQUIEM = registrar("musica.requiem");

    /** Upon the Hill V2, pista adicional del catalogo. */
    public static final RegistryObject<SoundEvent> MUSICA_UPON_HILL = registrar("musica.upon_hill");

    private static RegistryObject<SoundEvent> registrar(String nombre) {
        return REGISTRO.register(nombre,
                () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(JobsMenu.MOD_ID, nombre)));
    }

    public static void inscribir(IEventBus bus) {
        REGISTRO.register(bus);
    }
}
